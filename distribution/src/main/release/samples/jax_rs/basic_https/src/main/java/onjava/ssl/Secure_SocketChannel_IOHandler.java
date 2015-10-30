/*
 * Copyright 2004 WIT-Software, Lda. 
 * - web: http://www.wit-software.com 
 * - email: info@wit-software.com
 *
 * All rights reserved. Relased under terms of the 
 * Creative Commons' Attribution-NonCommercial-ShareAlike license.
 */
package onjava.ssl;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.logging.Logger;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLEngineResult;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;

import onjava.handlers.ServerSocketChannel_IOHandler;
import onjava.handlers.ChannelListener;
import onjava.io.SelectorThread;

/**
 * @author Nuno Santos
 */
public class Secure_SocketChannel_IOHandler extends ServerSocketChannel_IOHandler {
    private final static Logger log = Logger.getLogger("handlers");
    // private final static Logger log = new Logger();
    private final SSLSession session;
    private final SSLEngine engine;

    /**
     * Application data decrypted from the data received from the peer. This buffer must have enough space for
     * a full unwrap operation, so we can't use the buffer provided by the application, since we have no
     * control over its size.
     */
    private final ByteBuffer inboundDecryptedData;
    
    /** Network data received from the peer. Encrypted. */
    private final ByteBuffer inboundEncryptedData;
    
    /** Network data to be sent to the peer. Encrypted. */
    private final ByteBuffer outboundEncryptedData;

    /**
     * Whether the listener is interested in read events
     */
    private boolean appReadInterestSet = false;
    /**
     * Whether the listener is interested in write events
     */
    private boolean appWriteInterestSet = false;
    private boolean channelReadInterestSet = false;
    private boolean channelWriteInterestSet = false;

    /**
     * Set to true during the initial handshake. The initial handshake is special since no application data
     * can flow during it. Subsequent handshake are dealt with in a somewhat different way.
     */
    private boolean initialHandshake = false;
    private SSLEngineResult.HandshakeStatus hsStatus;
    /** Used during handshake, for the operations that don't consume any data */
    private ByteBuffer dummy;
    private boolean shutdown = false;
    private boolean closed = false;
    /**
     * Stores the result from the last operation performed by the SSLEngine
     */
    private SSLEngineResult.Status status = null;

    /**
     * If an error occurs while processing a callback from the selector thread, the exception is saved in this
     * field to be thrown to the application the next time it calls a public method of this class.
     */
    private IOException asynchException = null;

    /**
     * @param st
     * @param sc
     * @param listener
     * @throws Exception
     */
    public Secure_SocketChannel_IOHandler(SelectorThread st, SocketChannel sc, ChannelListener listener, SSLEngine engine) throws Exception {
        super(st, sc, listener);
        this.engine = engine;
        session = engine.getSession();
        inboundEncryptedData = ByteBuffer.allocate(session.getPacketBufferSize());
        inboundDecryptedData = ByteBuffer.allocate(session.getApplicationBufferSize());
        outboundEncryptedData = ByteBuffer.allocate(session.getPacketBufferSize());
        log.fine("peerNetData: " + inboundEncryptedData.capacity() + ", peerAppData: " + inboundDecryptedData.capacity()
                 + ", netData: " + outboundEncryptedData.capacity());

        // The peerNetData buffer is assumed to be ready to be written,
        // while the other buffers are assumed to be ready to be read from.

        // Change the position of the buffers so that a
        // call to hasRemaining() returns false. A buffer is considered
        // empty when the position is set to its limit, that is when
        // hasRemaining() returns false.
        inboundDecryptedData.position(inboundDecryptedData.limit());
        outboundEncryptedData.position(outboundEncryptedData.limit());
        st.registerChannelNow(sc, 0, this);// registra SocketChannel presso selectorThread cosi che i suoi
                                           // events vengano processati dal selectorThread con questa istanza come hadler.

        log.fine("Starting handshake");
        engine.beginHandshake();
        hsStatus = engine.getHandshakeStatus();
        initialHandshake = true;
        dummy = ByteBuffer.allocate(0);
        doHandshake();
    }

    private void checkChannelStillValid() throws IOException {
        if (closed) {
            throw new ClosedChannelException();
        }
        if (asynchException != null) {
            IOException ioe = new IOException("Asynchronous failure: " + asynchException.getMessage());
            ioe.initCause(asynchException);
            throw ioe;
        }
    }

    public int read(ByteBuffer dst) throws IOException {
        // log.fine("");
        checkChannelStillValid();
        if (initialHandshake) {
            return 0;
        }

        // Perhaps we should always try to read some data from
        // the socket. In some situations, it might not be possible
        // to unwrap some of the data stored on the buffers before
        // reading more.

        // Check if the stream is closed.
        if (engine.isInboundDone()) {
            // We reached EOF.
            return -1;
        }

        // First check if there is decrypted data waiting in the buffers
        if (!inboundDecryptedData.hasRemaining()) {
            int appBytesProduced = readAndUnwrap();
            if (appBytesProduced == -1 || appBytesProduced == 0) {
                return appBytesProduced;
            }
        }

        // It's not certain that we will have some data decrypted ready to
        // be sent to the application. Anyway, copy as much data as possible
        int limit = Math.min(inboundDecryptedData.remaining(), dst.remaining());
        for (int i = 0; i < limit; i++) {
            dst.put(inboundDecryptedData.get());
        }
        return limit;
    }

    private int readAndUnwrap() throws IOException {
        assert !inboundDecryptedData.hasRemaining() : "Application buffer not empty";
        // No decrypted data left on the buffers.
        // Try to read from the socket. There may be some data
        // on the peerNetData buffer, but it might not be sufficient.
        int bytesRead = sc.read(inboundEncryptedData);
        log.fine("Read from socket: " + bytesRead);
        if (bytesRead == -1) {
            // We will not receive any more data. Closing the engine
            // is a signal that the end of stream was reached.
            engine.closeInbound();
            // EOF. But do we still have some useful data available?
            if (inboundEncryptedData.position() == 0 || status == SSLEngineResult.Status.BUFFER_UNDERFLOW) {
                // Yup. Either the buffer is empty or it's in underflow,
                // meaning that there is not enough data to reassemble a
                // TLS packet. So we can return EOF.
                return -1;
            }
            // Although we reach EOF, we still have some data left to
            // be decrypted. We must process it
        }

        // Prepare the application buffer to receive decrypted data
        inboundDecryptedData.clear();

        // Prepare the net data for reading.
        inboundEncryptedData.flip();
        SSLEngineResult res;
        do {
            res = engine.unwrap(inboundEncryptedData, inboundDecryptedData);
            System.out.println( "Payload decriptato: "+  new  String(inboundDecryptedData.array())  );
            log.info("Unwrapping:\n" + res);
            // During an handshake renegotiation we might need to perform
            // several unwraps to consume the handshake data.
        } while (res.getStatus() == SSLEngineResult.Status.OK
                 && res.getHandshakeStatus() == SSLEngineResult.HandshakeStatus.NEED_UNWRAP
                 && res.bytesProduced() == 0);

        // If the initial handshake finish after an unwrap, we must activate
        // the application interestes, if any were set during the handshake
        if (res.getHandshakeStatus() == SSLEngineResult.HandshakeStatus.FINISHED) {
            finishInitialHandshake();
        }

        // If no data was produced, and the status is still ok, try to read once more
        if (inboundDecryptedData.position() == 0 && res.getStatus() == SSLEngineResult.Status.OK
            && inboundEncryptedData.hasRemaining()) {
            res = engine.unwrap(inboundEncryptedData, inboundDecryptedData);
            log.info("Unwrapping:\n" + res);
        }

        /*
         * The status may be: OK - Normal operation OVERFLOW - Should never happen since the application
         * buffer is sized to hold the maximum packet size. UNDERFLOW - Need to read more data from the
         * socket. It's normal. CLOSED - The other peer closed the socket. Also normal.
         */
        status = res.getStatus();
        hsStatus = res.getHandshakeStatus();
        // Should never happen, the peerAppData must always have enough space
        // for an unwrap operation
        assert status != SSLEngineResult.Status.BUFFER_OVERFLOW : "Buffer should not overflow: "
                                                                  + res.toString();

        // The handshake status here can be different than NOT_HANDSHAKING
        // if the other peer closed the connection. So only check for it
        // after testing for closure.
        if (status == SSLEngineResult.Status.CLOSED) {
            log.fine("Connection is being closed by peer.");
            shutdown = true;
            doShutdown();
            return -1;
        }

        // Prepare the buffer to be written again.
        inboundEncryptedData.compact();
        // And the app buffer to be read.
        inboundDecryptedData.flip();

        if (hsStatus == SSLEngineResult.HandshakeStatus.NEED_TASK
            || hsStatus == SSLEngineResult.HandshakeStatus.NEED_WRAP
            || hsStatus == SSLEngineResult.HandshakeStatus.FINISHED) {
            log.fine("Rehandshaking...");
            doHandshake();
        }

        return inboundDecryptedData.remaining();
    }

    /**
	 *
	 */
    public int packetChannelWrite(ByteBuffer src) throws IOException {
        checkChannelStillValid();
        if (initialHandshake) {
            log.fine("Writing not possible during handshake");
            // Not ready to write
            return 0;
        }
        log.fine("Trying to write");

        // First, check if we still have some data waiting to be sent.
        if (outboundEncryptedData.hasRemaining()) {
            // There is. Don't try to send it. We should be registered
            // waiting for a write event from the selector thread
            assert channelWriteInterestSet : "Write interest should be active" + outboundEncryptedData;
            return 0;
        }
        assert !channelWriteInterestSet : "Write interest should not be active";

        // There is no data left to be sent. Clear the buffer and get
        // ready to encrypt more data.
        outboundEncryptedData.clear();
        SSLEngineResult res = engine.wrap(src, outboundEncryptedData);
        log.info("Wrapping:\n" + res);
        // Prepare the buffer for reading
        outboundEncryptedData.flip();
        flushData();

        // Return the number of bytes read
        // from the source buffer
        return res.bytesConsumed();
    }

    /**
     * This method may result in a read attempt from the socket.
     */
    public void registerForRead() throws IOException {
        checkChannelStillValid();
        if (!appReadInterestSet) {
            appReadInterestSet = true;
            if (initialHandshake) {
                // Wait for handshake to finish
                return;

            } else {
                if (inboundDecryptedData.hasRemaining()) {
                    // There is decrypted data available, so prepare
                    // to fire the read event to the application
                    st.getSscManager().registerForRead(this);

                } else {
                    // There is no decrypted data. But there may be some
                    // encrypted data.
                    if (inboundEncryptedData.position() == 0 || status == SSLEngineResult.Status.BUFFER_UNDERFLOW) {
                        // Must read more data since either there is no encrypted
                        // data available or there is data available but not
                        // enough to reassemble a packet.
                        selectorRegisterForRead();
                    } else {
                        // There is encrypted data available. It may or may not
                        // be enough to reassemble a full packet. We have to check it.
                        if (readAndUnwrap() == 0) {
                            // Not possible to reassemble a full packet.
                            selectorRegisterForRead();
                        } else {
                            // either EOF or there is application data ready. In both
                            // cases we must inform the application
                            st.getSscManager().registerForRead(this);
                        }
                    }
                }
            }
        }
    }

    public void unregisterForRead() throws IOException {
        checkChannelStillValid();
        appReadInterestSet = false;
        st.getSscManager().unregisterForRead(this);
    }

    /**
     * Raises an event when there is space available for writing more data to this socket.
     */
    public void registerForWrite() throws IOException {
        checkChannelStillValid();
        if (!appWriteInterestSet) {
            log.fine("Activating write interest");
            appWriteInterestSet = true;
            if (initialHandshake) {
                return;
            } else {
                // Check if we can write now
                if (outboundEncryptedData.hasRemaining()) {
                    assert channelWriteInterestSet : "Write interest should be active";
                    // The buffer is full, the application can't write anymore.
                    // The write interest must be set...
                } else {
                    assert !channelWriteInterestSet : "Write interest should not be active";
                    // netData is empty. But don't fire the write event right
                    // now. Instead, register with the SecureChannelManager and
                    // wait for the SelectorThread to call for these events.
                    st.getSscManager().registerForWrite(this);
                }
            }
        }
    }

    /**
     * Cancel the write interest.
     */
    public void unregisterForWrite() throws IOException {
        checkChannelStillValid();
        appWriteInterestSet = false;
        st.getSscManager().unregisterForWrite(this);
    }

    /**
     * Called from the SSLChannelManager when it's time for launching the application events
     */
    void fireReadEvent() {
        appReadInterestSet = false;
        listener.handleRead();
    }

    /**
     * Called from the SSLChannelManager when it's time for launching the application events
     */
    void fireWriteEvent() {
        appWriteInterestSet = false;
        listener.handleWrite();
    }

    private void doShutdown() throws IOException {
        // log.fine("");
        assert !outboundEncryptedData.hasRemaining() : "Buffer was not empty.";
        // Either shutdown was initiated now or we are on the middle
        // of shutting down and this method was called after emptying
        // the out buffer

        // If the engine has nothing else to do, close the socket. If
        // this socket is dead because of an exception, close it
        // immediately
        if (asynchException != null || engine.isOutboundDone()) {
            log.fine("Outbound is done. Closing socket");
            try {
                // If no data was produced by the call to wrap, shutdown is complete
                sc.close();
            } catch (IOException e) { /* Ignore. */
            }
            return;
        }

        // The engine has more things to send
        /*
         * By RFC 2616, we can "fire and forget" our close_notify message, so that's what we'll do here.
         */
        outboundEncryptedData.clear();
        try {
            SSLEngineResult res = engine.wrap(dummy, outboundEncryptedData);
            log.info("Wrapping:\n" + res);
        } catch (SSLException e1) {
            // Problems with the engine. Probably it is dead. So close
            // the socket and forget about it.
            log.warning("Error during shutdown.\n" + e1.toString());
            try {
                sc.close();
            } catch (IOException e) { /* Ignore. */
            }
            return;
        }
        outboundEncryptedData.flip();
        flushData();
    }

    /**
	 *
	 */
    public void close() throws IOException {
        log.fine("Shutting down SSL Channel");
        if (shutdown) {
            log.fine("Shutdown already in progress");
            return;
        }
        // Initiate the shutdown process
        shutdown = true;
        closed = true;
        // We don't need it anymore
        asynchException = null;
        engine.closeOutbound();
        if (outboundEncryptedData.hasRemaining()) {
            // If this method is called after an exception, we should
            // close the socket regardless having some data to send.
            assert channelWriteInterestSet : "Data to be sent but no write interest.";
            log.fine("There is some data left to be sent. Waiting: " + outboundEncryptedData);
            // We are waiting to send the data
            return;
        } else {
            doShutdown();
        }
    }

    private void finishInitialHandshake() throws IOException {
        // Only during the initial handshake is it necessary to check
        // these flags
        initialHandshake = false;
        // Activate interest
        if (appReadInterestSet) {
            // We are not sure that there is data left to be read on the
            // socket. Therefore, wait for a selector event.
            selectorRegisterForRead();
        }
        if (appWriteInterestSet) {
            assert !outboundEncryptedData.hasRemaining() : "There is data left to send after handshake!";
            // We don't need to register with the selector, since we
            // know that the netData buffer is empty after the handshake.
            // Just send the write event to the application.
            st.getSscManager().registerForWrite(this);
        }
    }

    private void doHandshake() throws IOException {
        while (true) {
            SSLEngineResult res;
            log.fine(hsStatus.toString());
            switch (hsStatus) {
            case FINISHED:
                if (initialHandshake) {
                    finishInitialHandshake();
                }
                return;

            case NEED_TASK:
                doTasks();
                // The hs status was updated, so go back to the switch
                break;

            case NEED_UNWRAP:
                readAndUnwrap();
                // During normal operation a call to readAndUnwrap() that results in underflow
                // does not cause the channel to activate read interest with the selector.
                // Therefore, if we are at the initial handshake, we must activate the read
                // insterest explicitily.
                if (initialHandshake && status == SSLEngineResult.Status.BUFFER_UNDERFLOW) {
                    selectorRegisterForRead();
                }
                return;

            case NEED_WRAP:
                // First make sure that the out buffer is completely empty. Since we
                // cannot call wrap with data left on the buffer
                if (outboundEncryptedData.hasRemaining()) {
                    assert channelWriteInterestSet : "Write interest should be active: " + outboundEncryptedData;
                    return;
                }

                // Prepare to write
                outboundEncryptedData.clear();
                res = engine.wrap(dummy, outboundEncryptedData);
                log.info("Wrapping:\n" + res);
                assert res.bytesProduced() != 0 : "No net data produced during handshake wrap.";
                assert res.bytesConsumed() == 0 : "App data consumed during handshake wrap.";
                hsStatus = res.getHandshakeStatus();
                outboundEncryptedData.flip();

                // Now send the data and come back here only when
                // the data is all sent
                if (!flushData()) {
                    // There is data left to be send. Wait for it
                    return;
                }
                // All data was sent. Break from the switch but don't
                // exit this method. It will loop again, since there may be more
                // operations that can be done without blocking.
                break;

            case NOT_HANDSHAKING:
                assert false : "doHandshake() should never reach the NOT_HANDSHAKING state";
                return;
            }
        }
    }

    /**
     * Called from the selector thread.
     */
    public void selectorThreadHandleRead() {
        assert initialHandshake || appReadInterestSet : "Trying to read when there is no read interest set";
        assert channelReadInterestSet : "Method called when no read interest was set";
        channelReadInterestSet = false;
        // log.fine("");
        try {
            if (initialHandshake) {
                doHandshake();

            } else if (shutdown) {
                doShutdown();

            } else {
                // The read interest is always set when this method is called
                assert appReadInterestSet : "handleRead() called without read interest being set";

                int bytesUnwrapped = readAndUnwrap();
                if (bytesUnwrapped == -1) {
                    // End of stream.
                    assert engine.isInboundDone() : "End of stream but engine inbound is not closed";

                    // We must inform the client of the EOF
                    st.getSscManager().registerForRead(this);

                } else if (bytesUnwrapped == 0) {
                    // Must read more data
                    selectorRegisterForRead();

                } else {
                    // There is data to be read by the application. Notify it.
                    st.getSscManager().registerForRead(this);
                }
            }
        } catch (IOException e) {
            handleAsynchException(e);
        }
    }

    /**
     * Tries to write the data on the netData buffer to the socket. If not all data is sent, the write
     * interest is activated with the selector thread.
     * 
     * @return True if all data was sent. False otherwise.
     */
    private boolean flushData() throws IOException {
        assert outboundEncryptedData.hasRemaining() : "Trying to write but netData buffer is empty";
        int written;
        try {
            written = sc.write(outboundEncryptedData);
        } catch (IOException ioe) {
            // Clear the buffer. If write failed, the socket is dead. Clearing
            // the buffer indicates that no more write should be attempted.
            outboundEncryptedData.position(outboundEncryptedData.limit());
            throw ioe;
        }
        log.fine("Written to socket: " + written);
        if (outboundEncryptedData.hasRemaining()) {
            // The buffer is not empty. Register again with the selector
            selectorRegisterForWrite();
            return false;
        } else {
            return true;
        }
    }

    /**
     * Called from the selector thread.
     */
    public void selectorThreadHandleWrite() {
        assert channelWriteInterestSet : "Write event when no write interest set";
        channelWriteInterestSet = false;
        // log.fine("");
        try {
            if (flushData()) {
                // The buffer was sent completely
                if (initialHandshake) {
                    doHandshake();

                } else if (shutdown) {
                    doShutdown();

                } else {
                    // If the listener is interested in writing,
                    // prepare to fire the event.
                    if (appWriteInterestSet) {
                        st.getSscManager().registerForWrite(this);
                    }
                }
            } else {
                // There is still more data to be sent. Wait for another
                // write event. Calling flush data already resulted in the
                // write interest being reactivated.
            }
        } catch (IOException e) {
            handleAsynchException(e);
        }
    }

    private void handleAsynchException(IOException e) {
        // Will be sent back to the application next time a public
        // method is called
        asynchException = e;
        // If the application has any interest set, fire an event.
        // Otherwise, the event will be fired next time a public method
        // is called.
        if (appWriteInterestSet) {
            st.getSscManager().registerForWrite(this);
        }
        if (appReadInterestSet) {
            st.getSscManager().registerForRead(this);
        }
        // We won't be sending any more data.
        engine.closeOutbound();
    }

    private void selectorRegisterForRead() throws IOException {
        log.fine("");
        if (channelReadInterestSet) {
            return;
        }
        channelReadInterestSet = true;
        st.addChannelInterestNow(sc, SelectionKey.OP_READ);
    }

    private void selectorRegisterForWrite() throws IOException {
        log.fine("");
        if (channelWriteInterestSet) {
            return;
        }
        channelWriteInterestSet = true;
        st.addChannelInterestNow(sc, SelectionKey.OP_WRITE);
    }

    /**
     * Execute delegated tasks in the main thread. These are compute intensive tasks, so there's no point in
     * scheduling them in a different thread.
     */
    private void doTasks() {
        Runnable task;
        while ((task = engine.getDelegatedTask()) != null) {
            task.run();
        }
        hsStatus = engine.getHandshakeStatus();
    }
}
