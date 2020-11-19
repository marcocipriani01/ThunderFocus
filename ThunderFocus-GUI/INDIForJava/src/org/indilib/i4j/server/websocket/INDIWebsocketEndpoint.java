package org.indilib.i4j.server.websocket;

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

import org.indilib.i4j.protocol.websocket.INDIWebSocketConnection;
import org.indilib.i4j.server.api.INDIServerAccessLookup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.websocket.EndpointConfig;
import javax.websocket.OnClose;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * the websocket enpoint to accept new connections over the websocket protocol.
 * 
 * @author Richard van Nieuwenhoven
 */
@ServerEndpoint(value = "/websocket")
public class INDIWebsocketEndpoint {

    /**
     * Logger to log to.
     */
    private static final Logger LOG = LoggerFactory.getLogger(INDIWebsocketEndpoint.class);

    /**
     * the currently open connections.
     */
    private Map<Session, INDIWebSocketConnection> connections = new HashMap<Session, INDIWebSocketConnection>();

    /**
     * a new websocket connection was establisched, create a indiconnection
     * around it and attacht it to the server.
     * 
     * @param session
     *            the websocket session
     * @param config
     *            the config parameters
     */
    @OnOpen
    public synchronized void onOpen(final Session session, EndpointConfig config) {
        INDIWebSocketConnection connection = connections.get(session);
        if (connection == null) {
            connection = new INDIWebSocketConnection(session);
            connections.put(session, connection);
            if (!INDIServerAccessLookup.indiServerAccess().get().addConnection(connection)) {
                onClose(session);
                try {
                    session.close();
                } catch (IOException e) {
                    LOG.warn("exception during close");
                }
            }
        }
    }

    /**
     * the websocket connection was closed, propagate the close to the indi
     * connection.
     * 
     * @param session
     *            the session to close
     */
    @OnClose
    public synchronized void onClose(Session session) {
        INDIWebSocketConnection connection = connections.get(session);
        if (connection != null) {
            try {
                connection.close();
            } catch (IOException e) {
                LOG.warn("exception during close");
            }
        }
    }
}
