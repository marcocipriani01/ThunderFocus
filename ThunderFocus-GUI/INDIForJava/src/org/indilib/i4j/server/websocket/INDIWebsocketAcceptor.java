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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program. If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

import org.glassfish.tyrus.server.Server;
import org.indilib.i4j.protocol.api.INDIConnection;
import org.indilib.i4j.protocol.websocket.INDIWebSocketStreamHandler;
import org.indilib.i4j.server.INDIServerAcceptor;
import org.indilib.i4j.server.api.INDIServerAccessLookup;
import org.indilib.i4j.server.api.INDIServerInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;

/**
 * Web socket acceptor for the indi server.
 * 
 * @author Richard van Nieuwenhoven
 */
public class INDIWebsocketAcceptor implements INDIServerAcceptor {

    /**
     * Logger to log to.
     */
    private static final Logger LOG = LoggerFactory.getLogger(INDIWebsocketAcceptor.class);

    /**
     * the local port to listen to.
     */
    private int localPort;

    /**
     * the websocket server.
     */
    private Server wss;

    @Override
    public void run() {
    }

    @Override
    public boolean acceptClient(INDIConnection clientSocket) {
        return true;
    }

    @Override
    public boolean isRunning() {
        return wss != null;
    }

    @Override
    public void start() {
        INDIServerInterface server = INDIServerAccessLookup.indiServerAccess().get();
        if (localPort <= 0) {
            localPort = INDIWebSocketStreamHandler.WEBSOCKET_DEFAULT_PORT;
        }
        wss = new Server(server.getHost(), localPort, "/", null, INDIWebsocketEndpoint.class);
        try {
            wss.start();
        } catch (Exception e) {
            wss = null;
            LOG.error("could not start websocket server", e);
        }
    }

    @Override
    public void close() {
        wss.stop();
        wss = null;
    }

    @Override
    public String getHost() {
        return INDIServerAccessLookup.indiServerAccess().get().getHost();
    }

    @Override
    public int getPort() {
        return localPort;
    }

    @Override
    public String getName() {
        return "Websocket";
    }

    @Override
    public void setArguments(Object... arguments) {
        if (arguments != null && arguments.length > 0 && arguments[0] != null) {
            try {
                localPort = Integer.parseInt(arguments[0].toString().trim());
            } catch (Exception e) {
                LOG.error("argument was not port integer", e);
            }
        }
    }

    @Override
    public boolean isLocalURL(URL url) {
        boolean hostEqual = false;
        boolean portEqual;
        String hostName = url.getHost();
        if (hostName == null || hostName.isEmpty()) {
            hostEqual = true;
        } else {
            try {
                InetAddress inetAdress = InetAddress.getByName(hostName);
                hostEqual = inetAdress.isAnyLocalAddress() || inetAdress.isLoopbackAddress();
            } catch (UnknownHostException e) {
                LOG.warn("host name lookup failed " + hostName, e);
                hostEqual = false;
            }
        }
        int port = url.getPort();
        if (port <= 0) {
            port = url.getDefaultPort();
        }
        portEqual = localPort == port;
        return portEqual && hostEqual;
    }
}
