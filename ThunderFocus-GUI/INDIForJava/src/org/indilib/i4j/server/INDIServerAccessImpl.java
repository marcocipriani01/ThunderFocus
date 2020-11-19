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

import org.indilib.i4j.server.api.INDIServerAccess;
import org.indilib.i4j.server.api.INDIServerInterface;

/**
 * The standart implementation of the server access api. this class instanciates
 * a server if no server was already running.
 * 
 * @author Richard van Nieuwenhoven
 */
public class INDIServerAccessImpl implements INDIServerAccess {

    /**
     * the currently running indi server.
     */
    private static INDIServerInterface server;

    @Override
    public INDIServerInterface createOrGet(String host, Integer port) {
        if (server == null || !server.isServerRunning()) {
            server = new INDIServer(port);
        }
        return server;
    }

    @Override
    public INDIServerInterface get() {
        return server;
    }
}
