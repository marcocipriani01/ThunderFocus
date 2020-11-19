package org.indilib.i4j.server.api;

/*
 * #%L
 * INDI for Java Server API
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

/**
 * This interface allows access to the indiserver without a direct classpath.
 * this interface can be got over the java service loder system.
 * 
 * @author Richard van Nieuwenhoven
 */
public interface INDIServerAccess {

    /**
     * create a new server liseing to the specified host / port. or get an
     * aready running server. Attention if the server is already running the
     * host and port will be ignored.
     * 
     * @param host
     *            the requested host to listen
     * @param port
     *            the requested port to listen
     * @return the interface to the server.
     */
    INDIServerInterface createOrGet(String host, Integer port);

    /**
     * @return the currently started server or null if no server was started.
     */
    INDIServerInterface get();
}
