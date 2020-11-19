package org.indilib.i4j.server;

/*
 * #%L
 * INDI for Java Server Library
 * %%
 * Copyright (C) 2012 - 2014 indiforjava
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

import org.indilib.i4j.protocol.api.INDIConnection;

import java.net.URL;

/**
 * This is the server side wrapper interface around different ways to connect to
 * the server. It is always a runnabble that will be started as a thread using
 * the start method and stopped with the close method.
 * 
 * @author Richard van Nieuwenhoven
 */
public interface INDIServerAcceptor extends Runnable {

    /**
     * A client connection was established, handle accordingly.
     * 
     * @param clientSocket
     *            the socket the client is connected to.
     * @return true if the client is accepted.
     */
    boolean acceptClient(INDIConnection clientSocket);

    /**
     * @return is the thread running?
     */
    boolean isRunning();

    /**
     * start the thread if it is not jet started.
     */
    void start();

    /**
     * stop the threat and close any open resources. .
     */
    void close();

    /**
     * @return the host name this acceptor is binded with.
     */
    String getHost();

    /**
     * @return the port number this acceptor is listening to.
     */
    int getPort();

    /**
     * @return human readable name of the server acceptor.
     */
    String getName();

    /**
     * the arguments to initialize the acceptor.
     * 
     * @param arguments
     *            the array of arguments.
     */
    void setArguments(Object... arguments);

    /**
     * is the specified url pointing to this acceptor?
     * 
     * @param url
     *            the url to check
     * @return true if the acceptor is listening there.
     */
    boolean isLocalURL(URL url);
}
