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

import onjava.io.SelectorThread;

/**
 * @author Nuno Santos
 */
public interface ChannelFactory {
	
	public ServerSocketChannel_IOHandler createSocketChannel_IOHandler(
			SocketChannel sc, 
			SelectorThread st, 
			ChannelListener l) throws Exception;
}
