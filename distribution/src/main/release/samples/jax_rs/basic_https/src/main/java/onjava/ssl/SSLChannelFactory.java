/*
 * Copyright 2004 WIT-Software, Lda. 
 * - web: http://www.wit-software.com 
 * - email: info@wit-software.com
 *
 * All rights reserved. Relased under terms of the 
 * Creative Commons' Attribution-NonCommercial-ShareAlike license.
 */
package onjava.ssl;

import java.io.FileInputStream;
import java.nio.channels.SocketChannel;
import java.security.KeyStore;
import java.util.logging.Logger;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.TrustManagerFactory;

import onjava.handlers.ChannelFactory;
import onjava.handlers.ChannelListener;
import onjava.io.SelectorThread;

/**
 * @author Nuno Santos
 */
public class SSLChannelFactory implements ChannelFactory {
    private final boolean clientMode;
    private final SSLContext sslContext;
    private final static Logger log = Logger.getLogger("handlers");

    private static SSLContext createSSLContext(boolean clientMode, String keystore, String password)
        throws Exception {
        // Create/initialize the SSLContext with key material
        char[] passphrase = password.toCharArray();
        // First initialize the key and trust material.
        KeyStore ks = KeyStore.getInstance("JKS");
        ks.load(new FileInputStream(keystore), passphrase);
        SSLContext sslContext = SSLContext.getInstance("TLS");

        if (clientMode) {
            // TrustManager's decide whether to allow connections.
            TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
            tmf.init(ks);
            sslContext.init(null, tmf.getTrustManagers(), null);

        } else {
            // KeyManager's decide which key material to use.
            KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
            kmf.init(ks, passphrase);
            sslContext.init(kmf.getKeyManagers(), null, null);
        }
        return sslContext;
    }

    public SSLChannelFactory(boolean clientMode, String keystore, String password) throws Exception {
        StringBuffer sb = new StringBuffer();
        sb.append("Initializing SSL context.");
        if (clientMode) {
            sb.append(" Client mode. TrustStore: " + keystore);
        } else {
            sb.append(" Server mode. KeyStore: " + keystore);
        }
        log.info(sb.toString());

        this.clientMode = clientMode;
        sslContext = createSSLContext(clientMode, keystore, password);
    }

    public onjava.handlers.ServerSocketChannel_IOHandler createSocketChannel_IOHandler(SocketChannel sc,
                                                                                        SelectorThread st,
                                                                                        ChannelListener l)throws Exception {
        log.info("Creating SecureChannel. Client mode: " + clientMode);
        SSLEngine engine = sslContext.createSSLEngine();
        engine.setUseClientMode(clientMode);

        return new Secure_SocketChannel_IOHandler(st, sc, l, engine);
    }
}
