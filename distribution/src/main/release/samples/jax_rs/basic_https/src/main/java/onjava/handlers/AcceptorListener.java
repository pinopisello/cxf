/*
 * Copyright 2004 WIT-Software, Lda. 
 * - web: http://www.wit-software.com 
 * - email: info@wit-software.com
 *
 * All rights reserved. Relased under terms of the 
 * Creative Commons' Attribution-NonCommercial-ShareAlike license.
 */
package  onjava.handlers;

import java.nio.channels.SocketChannel;

/**
 * Callback interface for receiving events from an Acceptor. 
 * 
 * @author Nuno Santos
 */
public interface AcceptorListener {
  /**
   * Called when a connection is established.
   * @param acceptor The acceptor that originated this event. 
   * @param sc The newly connected socket.
   */
  public void socketConnected(ServerAcceptor acceptor, SocketChannel sc);
  /**
   * Called when an error occurs on the Acceptor.
   * @param acceptor The acceptor where the error occured.
   * @param ex The exception representing the error.
   */
  public void socketError(ServerAcceptor acceptor, Exception ex);
}
