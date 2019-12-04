/*
 * Copyright 2004 WIT-Software, Lda. 
 * - web: http://www.wit-software.com 
 * - email: info@wit-software.com
 *
 * All rights reserved. Relased under terms of the 
 * Creative Commons' Attribution-NonCommercial-ShareAlike license.
 */
package  onjava.handlers;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

import onjava.io.SelectorThread;

/**
 * @author Nuno Santos
 */
public final class PlainChannel extends ServerSocketChannel_IOHandler {
	/**
	 * @param st
	 * @param sc
	 * @param listener
	 * @throws IOException
	 */
	public PlainChannel(SelectorThread st, SocketChannel sc, ChannelListener listener) throws IOException {
		super(st, sc, listener);		
		st.registerChannelNow(sc, 0, this);
	}

	public int packetChannelRead(ByteBuffer dst) throws IOException {
		return sc.read(dst);
	}

	public int packetChannelWrite(ByteBuffer src) throws IOException {
		return sc.write(src);
	}

	public void registerForRead() throws IOException {
	        st.addChannelInterestNow(sc, SelectionKey.OP_READ);
	}

	public void registerForWrite() throws IOException {		
		st.addChannelInterestNow(sc, SelectionKey.OP_WRITE);
	}

	public void unregisterForRead() throws IOException {
		st.removeChannelInterestNow(sc, SelectionKey.OP_READ);
	}

	public void unregisterForWrite() throws IOException {
		st.removeChannelInterestNow(sc, SelectionKey.OP_WRITE);
	}
	
	public void close() throws IOException {
		sc.close();		
	}

	public void selectorThreadHandleRead() {
		listener.handleRead();		
	}

	public void selectorThreadHandleWrite() {
		listener.handleWrite();		
	}
}