/*
 * Copyright 2004 WIT-Software, Lda. 
 * - web: http://www.wit-software.com 
 * - email: info@wit-software.com
 *
 * All rights reserved. Relased under terms of the 
 * Creative Commons' Attribution-NonCommercial-ShareAlike license.
 */
package onjava.server;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import onjava.handlers.ServerAcceptor;
import onjava.handlers.AcceptorListener;
import onjava.handlers.ChannelFactory;
import onjava.handlers.PacketChannel;
import onjava.handlers.PacketChannelListener;
import onjava.handlers.PlainChannelFactory;
import onjava.handlers.SimpleProtocolDecoder;
import onjava.io.SelectorThread;
import onjava.ssl.SSLChannelFactory;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;

/**
 * A simple server for demonstrating the IO Multiplexing framework in action. After accepting a connection, it
 * will read packets as defined by the SimpleProtocolDecoder class and echo them back. This server can accept
 * and manage large numbers of incoming connections. For added fun remove the System.out statements and try it
 * with several thousand (>10.000) clients. You might have to increase the maximum number of sockets allowed
 * by the operating system.
 * 
 * @author Nuno Santos
 */
public class Server implements AcceptorListener, PacketChannelListener {
    private final SelectorThread st;
    private static ChannelFactory channelFactory;
    private static final String KEYSTORE = "/Users/glocon/Miei/local_git/nike_repo/CXF_forked/cxf/distribution/src/main/release/samples/jax_rs/basic_https/src/main/java/onjava/keystore.ks";
    private static final String KEYSTORE_PASSWORD = "password";
    private static int connections = 0;

    /**
     * Starts the server.
     * 
     * @param listenPort The port where to listen for incoming connections.
     * @throws Exception
     */
    public Server(int listenPort) throws Exception {
        st = new SelectorThread();//SelectorThread e' lanciato immediatamente  esegue un infinite loop: doInvocations
                                  //                                                                    sscManager.fireEvents
                                  //                                                                    itera per le selector.selectedKeys e epr ognuna invoca l handler associato
        ServerAcceptor acceptor = new ServerAcceptor(listenPort, st, this);
        acceptor.openServerSocket();
        System.out.println("Listening on port: " + listenPort + ". SSL? " + (channelFactory instanceof SSLChannelFactory));
    }

    private static void showUsage(Options options) {
        HelpFormatter helpFormatter = new HelpFormatter();
        helpFormatter.printHelp("MultiplexingEchoServer [options] <listenPort>", options);
    }

    public static void main(String[] args) throws Exception {
        //System.setProperty("javax.net.debug", "all");
        // Prepare to parse the command line
        Options options = new Options();
        Option sslOpt = new Option("s", "ssl", false, "Use SSL");
        Option debugOpt = new Option("d", true, "Debug level (NONE, FINER, FINE, CONFIG, INFO, WARNING, SEVERE. Default INFO.");
        Option help = new Option("h", "print this message");

        options.addOption(help);
        options.addOption(debugOpt);
        options.addOption(sslOpt);
        CommandLineParser parser = new PosixParser();
        // parse the command line arguments
        CommandLine line = parser.parse(options, args,true);

        if (line.hasOption(help.getOpt()) || args.length < 1) {
            showUsage(options);
            return;
        }

        if (line.hasOption(sslOpt.getOpt())) {
            channelFactory = new SSLChannelFactory(false, KEYSTORE, KEYSTORE_PASSWORD);
        } else {
            channelFactory = new PlainChannelFactory();
        }

        // Setup the logging context when using Log4j. Extracts the last
        // number in the thread
        // NDC.push(Thread.currentThread().getName());

        try {
            int listenPort = Integer.parseInt(line.getArgs()[0]);
            new Server(listenPort);
        } catch (Exception e) {
            showUsage(options);
        }
    }

    // ////////////////////////////////////////
    // Implementation of the callbacks from the
    // Acceptor and PacketChannel classes
    // ////////////////////////////////////////
    /**
     * A new client connected. Creates a PacketChannel to handle it.
     */
    public void socketConnected(ServerAcceptor acceptor, SocketChannel sc) {
        System.out.println("[" + acceptor + "] Socket connected: " + sc.socket().getInetAddress());
        try {
            // We should reduce the size of the TCP buffers or else we will
            // easily run out of memory when accepting several thousands of
            // connctions
            sc.socket().setReceiveBufferSize(2 * 1024);
            sc.socket().setSendBufferSize(2 * 1024);
            // The contructor enables reading automatically.
            PacketChannel pc = new PacketChannel(sc, channelFactory, st, new SimpleProtocolDecoder(), this);
            pc.resumeReading();
            connections++;
        } catch (Exception e) {
            e.printStackTrace();
            try {
                sc.close();
            } catch (IOException e1) {
                e1.printStackTrace();
                // Ignore
            }
        }
    }

    public void socketError(ServerAcceptor acceptor, Exception ex) {
        System.out.println("[" + acceptor + "] Error: " + ex.getMessage());
    }

    public void packetArrived(PacketChannel pc, ByteBuffer pckt) {
        StringBuffer sb = new StringBuffer("[" + pc.toString() + "] Packet received. Size: "
        + pckt.remaining() + "    <"+ new  String(pckt.array())+">" );
        // int limit = Math.min(pckt.remaining(), 20);
        // for (int i = 0; i < limit; i++) {
        // sb.append((char)pckt.get(i));
        // }
        System.out.println(sb.toString());
        
        pc.sendPacket(pckt);
    }

    public void socketException(PacketChannel pc, Exception ex) {
        connections--;
        System.out.println("[" + pc.toString() + "] Error: ");
        ex.printStackTrace();
        System.out.println(" Active connections: " + connections);
    }

    public void socketDisconnected(PacketChannel pc) {
        connections--;
        System.out.println("[" + pc.toString() + "] Disconnected. Active connections: " + connections);
    }

    /**
     * The answer to a request was sent. Prepare to read the next request.
     */
    public void packetSent(PacketChannel pc, ByteBuffer pckt) {
        try {
            pc.resumeReading();
        } catch (Exception e) {
            socketException(pc, e);
        }
    }
}
