package org.indilib.i4j.server.api;

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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program. If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

import org.indilib.i4j.protocol.api.INDIConnection;

/**
 * This interface is allows others to monitor server events. as soon as one of
 * the events happen the apropriate event method is called.
 * 
 * @author Richard van Nieuwenhoven
 */
public interface INDIServerEventHandler {

    /**
     * Must return <code>true</code> is the Client that established this
     * connection must be allowed in the Server. Otherwise the connection will
     * be closed.
     * 
     * @param clientSocket
     *            The socket created with a possible client.
     * @return <code>true</code> if the Client is allowed to connect to the
     *         server. <code>false</code> otherwise.
     */
    boolean acceptClient(INDIConnection clientSocket);

    /**
     * Used to notify extending Servers that a Client connection has been
     * broken.
     * 
     * @param client
     *            The Client whose connection has been broken.
     */
    void connectionWithClientBroken(INDIClientInterface client);

    /**
     * Used to notify extending Servers that a Client connection has been
     * established.
     * 
     * @param client
     *            The Client whose connection has been established.
     */
    void connectionWithClientEstablished(INDIClientInterface client);

    /**
     * Used to notify extending Servers that some device Names are no longer
     * available.
     * 
     * @param device
     *            the device that disconnected
     */
    void driverDisconnected(INDIDeviceInterface device);
}
