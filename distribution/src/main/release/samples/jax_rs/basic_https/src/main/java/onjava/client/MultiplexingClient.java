/*
 * Copyright 2004 WIT-Software, Lda. 
 * - web: http://www.wit-software.com 
 * - email: info@wit-software.com
 *
 * All rights reserved. Relased under terms of the 
 * Creative Commons' Attribution-NonCommercial-ShareAlike license.
 */
package onjava.client;


import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import onjava.handlers.ChannelFactory;
import onjava.handlers.ClientConnector;
import onjava.handlers.ConnectorListener;
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
 * A simple test client for the I/O Multiplexing based server. This class simulates several clients using an
 * imaginary request- reply protocol to connect to a server. Several connections can be established at the
 * same time. Each one, generates a random packet to simulate a request, sends it to the server and waits for
 * the answer before sending the next packet. This implementation is based on I/O Multiplexing, so it should
 * be able to handle several thousand of connections. Using Redhat Linux 9.0 I was able to establish about
 * 10.000 connections before running out of memory (the system had only 256Mb of RAM).
 * 
 * @author Nuno Santos
 */
public class MultiplexingClient implements ConnectorListener, PacketChannelListener {

    private static final String TRUSTSTORE = "/Users/glocon/Miei/local_git/nike_repo/CXF_forked/cxf/distribution/src/main/release/samples/jax_rs/basic_https/src/main/java/onjava/keystore.ks";
    private static final String TRUSTSTORE_PASSWORD = "password";
    /** A single selector for all clients */
    private static SelectorThread st;
    /** Maximum size of the packets sent */
    private static int maxPcktSize;
    /** Minimum size of the packets sent */
    private static final int MIN_SIZE = 128;
    /** How many packets each client should send */
    private static int packetsToSend;
    /** For generating random packet sizes. */
    private static final Random r = new Random();
    /** How many connections to created */
    private static int connectionCount;
    /** How many connections were opened so far */
    private static int connectionsEstablished = 0;
    private static int connectionsFailed = 0;
    /** How many connections were disconnected so far */
    private static int connectionsClosed = 0;

    private static long testStart;
    /**
     * Keeps a list of MultiplexedClients which are connected
     */
    private static List connectedClients = new ArrayList(512);
    /** Defines the type of channels to use */
    private static ChannelFactory channelFactory;

    /** How many packets each instance sent so far. */
    private int packetsSent = 0;
    private PacketChannel pc;
    private ByteBuffer sentPacket;

    /**
     * Initiates a non-blocking connection attempt to the given address.
     * 
     * @param remotePoint Where to try to connect.
     * @param ssl TODO
     * @throws Exception
     */
    public MultiplexingClient(InetSocketAddress remotePoint) throws Exception {
        ClientConnector connector = new ClientConnector(st, remotePoint, this);
        connector.connect();
        System.out.println("[" + connector + "] Connecting...");
    }

    /**
     * Creates a new packet with a size chosen randomly between MIN_SIZE and MAX_SIZE.
     */
    private ByteBuffer generateNextPacket() {
        // Generate a random size between
        int size = MIN_SIZE + r.nextInt(maxPcktSize - MIN_SIZE);
        ByteBuffer buffer = ByteBuffer.allocate(size);
 
        buffer.put(SimpleProtocolDecoder.STX);
       
        
       // for (int i = 0; i < size - 2; i++) {
       //     buffer.put((byte)('0' + (i % 10)));
       //}
        
        
        buffer.put(("Messaggio <"+packetsSent+">").getBytes());
        
        buffer.put(SimpleProtocolDecoder.ETX);
        buffer.limit(buffer.position());
        buffer.flip();
        return buffer;
    }

    // ////////////////////////////////////////
    // Implementation of the callbacks from the
    // Acceptor and PacketChannel classes
    // ////////////////////////////////////////
    /**
     * A new client connected. Creates a PacketChannel to handle it.
     */
    public void connectionEstablished(ClientConnector connector, SocketChannel sc) {
        try {
            // We should reduce the size of the TCP buffers or else we will
            // easily run out of memory when accepting several thousands of
            // connctions
            sc.socket().setReceiveBufferSize(2 * 1024);
            sc.socket().setSendBufferSize(2 * 1024);
            // The contructor enables reading automatically.
            this.pc = new PacketChannel(sc, channelFactory, st, new SimpleProtocolDecoder(), this);

            // Do not start sending packets right away. Waits for all sockets
            // to connect. Otherwise, the load created by sending and receiving
            // packets will increase dramatically the time taken for all
            // connections to be established. It is better to establish all
            // connections and only then to start sending packets.
            connectedClients.add(this);
            connectionsEstablished++;
            System.out.println("[" + connector + "] Connected: " + sc.socket().getInetAddress() + " ("
                               + connectionsEstablished + "/" + connectionCount + ")");
            // If if all connections are established.
            sendMessage();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void connectionFailed(ClientConnector connector, Exception cause) {
        System.out.println("[" + connector + "] Error: " + cause.getMessage());
        connectionsFailed++;
        sendMessage();
    }

    private String bufferToString(ByteBuffer bb) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < bb.limit(); i++) {
            sb.append((char)bb.get(i));
        }
        return sb.toString();
    }

    public void packetArrived(PacketChannel pc, ByteBuffer pckt) {
        sentPacket.rewind();
        if (!sentPacket.equals(pckt)) {
            System.out.println("Received packet is different from sent packet");
            System.out.println("Sent: " + bufferToString(sentPacket));
            System.out.println("Rcvd: " + bufferToString(pckt));
            System.exit(1);
        }

        // System.out.println("["+ pc.toString() + "] Packet arrived");
        if (packetsSent >= packetsToSend) {
            // This connection sent all packets that it was supposed to send.
            // Close.
            System.out.println("[" + pc.getSocketChannel().socket().getLocalPort()
                               + "] Closed. Packets sent " + packetsToSend + ". Connection: "
                               + (connectionsClosed + 1) + "/" + connectionsEstablished);
            pc.close();
            connectionClosed();
        } else {
            // Still more packets to send.
            sendPacket();
        }
    }

    public void socketException(PacketChannel pc, Exception ex) {
        System.out.println("[" + pc.toString() + "] Error: " + ex.getMessage());
        connectionClosed();
    }

    public void socketDisconnected(PacketChannel pc) {
        System.out.println("[" + pc.toString() + "] Disconnected.");
        connectionClosed();
    }

    /**
     * The request was sent. Prepare to read the answer.
     */
    public void packetSent(PacketChannel pc, ByteBuffer pckt) {
        // System.out.println("[" + pc.toString() + "] Packet sent.");
        try {
            pc.resumeReading();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // //////////////////////////
    // Helper methods
    // //////////////////////////
    /**
     * Called when a connection is closed. Checks if all connections have been closed and if so exits the
     * virtual machine.
     */
    private void connectionClosed() {
        connectionsClosed++;
        if (connectionsClosed >= connectionsEstablished) {
            System.out.println("Done. Time taken: " + (System.currentTimeMillis() - testStart));
            // st.requestClose();
            // System.exit(1);
        }
    }

    /**
     * Sends a newly generated packet using the given PacketChannel
     * 
     * @param pc
     */
    private void sendPacket() {
        sentPacket = generateNextPacket();
        packetsSent++;
        System.out.println("[" + pc.getSocketChannel().socket().getLocalPort() + "] Sending packet "
                           + packetsSent + " of " + packetsToSend + ". Size: " + sentPacket.limit());
        pc.sendPacket(sentPacket);
    }

    /**
     * Checks if all connections have been established. If so, starts them all by sending an initial packet to
     * all of them.
     */
    private void sendMessage() {
        // Starts sending packets only after all connections are established.
        if ((connectionsEstablished + connectionsFailed) == connectionCount) {
            System.out.println("MultiplexClient start sending packets to the servers: MultiplexClient.checkAllConnected()");
            testStart = System.currentTimeMillis();
            for (int i = 0; i < connectedClients.size(); i++) {
                MultiplexingClient client = (MultiplexingClient)connectedClients.get(i);
                client.sendPacket();
            }
            connectedClients.clear();
        }
    }

    private static void showUsage(Options options) {
        HelpFormatter helpFormatter = new HelpFormatter();
        helpFormatter.printHelp("MultiplexingClient [options] <host:port>", options);
    }

    public static void main(String[] args) throws Exception {
        // Prepare to parse the command line
        Options options = new Options();
        Option sslOpt = new Option("s", "ssl", false, "Use SSL");
        Option debugOpt = new Option("d", true,
                                     "Debug level (NONE, FINER, FINE, CONFIG, INFO, WARNING, SEVERE. Default INFO.");
        Option numConnectionsOpt = new Option("n", true, "Number of connections to establish. [Default: 1]");
        Option numPcktOpt = new Option("p", true,
                                       "Number of packets to send in each connection. [Default: 20]");
        Option pcktMaxSizeOpt = new Option("m", true, "Maximum size of packets. [Default: 4096]");
        Option help = new Option("h", "print this message");

        options.addOption(help);
        options.addOption(debugOpt);
        options.addOption(numConnectionsOpt);
        options.addOption(numPcktOpt);
        options.addOption(pcktMaxSizeOpt);
        options.addOption(sslOpt);
        CommandLineParser parser = new PosixParser();
        // parse the command line arguments
        CommandLine line = parser.parse(options, args);

       // if (line.hasOption(help.getOpt()) || line.getArgs().length < 1) {
        //    showUsage(options);
        //    return;
        //}

        if (line.hasOption(sslOpt.getOpt())) {
            channelFactory = new SSLChannelFactory(true, TRUSTSTORE, TRUSTSTORE_PASSWORD);
        } else {
            channelFactory = new PlainChannelFactory();
        }

        if (line.hasOption(numConnectionsOpt.getOpt())) {
            connectionCount = Integer.parseInt(line.getOptionValue(numConnectionsOpt.getOpt()));
        } else {
            connectionCount = 1;
        }

        if (line.hasOption(numPcktOpt.getOpt())) {
            packetsToSend = Integer.parseInt(line.getOptionValue(numPcktOpt.getOpt()));
        } else {
            packetsToSend = 20;
        }

        if (line.hasOption(pcktMaxSizeOpt.getOpt())) {
            maxPcktSize = Integer.parseInt(line.getOptionValue(pcktMaxSizeOpt.getOpt()));
        } else {
            maxPcktSize = 4096;
        }

        InetSocketAddress remotePoint;
        try {
            String host = line.getArgs()[0];
            int colonIndex = host.indexOf(':');
            remotePoint = new InetSocketAddress(host.substring(0, colonIndex), Integer.parseInt(host.substring(colonIndex + 1)));
        } catch (Exception e) {
            showUsage(options);
            return;
        }

        // Setups the logging context for Log4j
        // NDC.push(Thread.currentThread().getName());

        st = new SelectorThread();
        for (int i = 0; i < connectionCount; i++) {
            new MultiplexingClient(remotePoint);
            // Must sleep for a while between opening connections in order
            // to give the remote host enough time to handle them. Otherwise,
            // the remote host backlog will get full and the connection
            // attemps will start to be refused.
            Thread.sleep(100);
        }
    }
}
