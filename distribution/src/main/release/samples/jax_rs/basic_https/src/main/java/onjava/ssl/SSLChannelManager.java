/*
 * Copyright 2004 WIT-Software, Lda. 
 * - web: http://www.wit-software.com 
 * - email: info@wit-software.com
 *
 * All rights reserved. Relased under terms of the 
 * Creative Commons' Attribution-NonCommercial-ShareAlike license.
 */
package onjava.ssl;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

/**
 * @author Nuno Santos
 */
public class SSLChannelManager {
    private final static Logger log = Logger.getLogger("handlers");

    private final Set<Secure_SocketChannel_IOHandler> readListeners = new HashSet<Secure_SocketChannel_IOHandler>();
    private final Set<Secure_SocketChannel_IOHandler> writeListeners = new HashSet<Secure_SocketChannel_IOHandler>();

    /**
     * Called by the SelectorThread to give the SecureChannels a chance to fire the events to its listeners
     */
    public void fireEvents() {
        while (!readListeners.isEmpty() || !writeListeners.isEmpty()) {
            Secure_SocketChannel_IOHandler[] sc;
            if (!readListeners.isEmpty()) {
                // Fire read events
                // Make a copy because the handlers might call one of the register
                // methods of this class, thereby changing the set.
                sc = (Secure_SocketChannel_IOHandler[])readListeners.toArray(new Secure_SocketChannel_IOHandler[readListeners.size()]);
                readListeners.clear();
                for (int i = 0; i < sc.length; i++) {
                    sc[i].fireReadEvent();
                }
            }

            if (!writeListeners.isEmpty()) {
                // Now the write listeners
                sc = (Secure_SocketChannel_IOHandler[])writeListeners.toArray(new Secure_SocketChannel_IOHandler[writeListeners.size()]);
                writeListeners.clear();
                for (int i = 0; i < sc.length; i++) {
                    sc[i].fireWriteEvent();
                }
            }
        }
    }

    public void registerForRead(Secure_SocketChannel_IOHandler l) {
        log.fine("Registering for read");
        boolean wasNotPresent = readListeners.add(l);
        // assert wasNotPresent : "SecureChannel was already registered";
    }

    public void unregisterForRead(Secure_SocketChannel_IOHandler l) {
        log.fine("Unregistering for read");
        boolean wasPresent = readListeners.remove(l);
        // assert wasPresent : "SecureChannel was not registered";
    }

    public void registerForWrite(Secure_SocketChannel_IOHandler l) {
        log.fine("Registering for write");
        boolean wasNotPresent = writeListeners.add(l);
        // assert wasNotPresent : "SecureChannel was already registered";
    }

    public void unregisterForWrite(Secure_SocketChannel_IOHandler l) {
        log.fine("Unregistering for write");
        boolean wasPresent = writeListeners.remove(l);
        // assert !wasPresent : "SecureChannel was not registered";
    }
}
