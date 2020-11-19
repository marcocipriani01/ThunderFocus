package org.indilib.i4j.server.zip;

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
import org.indilib.i4j.protocol.io.INDIZipSocketConnection;
import org.indilib.i4j.protocol.url.INDIURLZipStreamHandler;
import org.indilib.i4j.server.INDIServerSocketAcceptor;
import org.indilib.i4j.server.api.INDIServerAccessLookup;

import java.net.Socket;

/**
 * a compressed acceptor for connections thru a reduced bandwith. The xml stream
 * is transported zipped.
 * 
 * @author Richard van Nieuwenhoven
 */
public class INDIServerZipSocketAcceptor extends INDIServerSocketAcceptor {

    @Override
    public boolean acceptClient(INDIConnection clientSocket) {
        return INDIServerAccessLookup.indiServerAccess().get().addConnection(clientSocket);
    }

    @Override
    protected INDIConnection createINDIConnection(Socket clientSocket) {
        return new INDIZipSocketConnection(clientSocket);
    }

    @Override
    public String getName() {
        return "zip";
    }

    @Override
    protected int getDefaultPort() {
        return INDIURLZipStreamHandler.INDI_DEFAULT_PORT;
    }
}
