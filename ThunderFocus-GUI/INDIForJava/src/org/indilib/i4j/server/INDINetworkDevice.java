package org.indilib.i4j.server;

/*
 * #%L
 * INDI for Java Server Library
 * %%
 * Copyright (C) 2013 - 2014 indiforjava
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

import org.indilib.i4j.INDIException;
import org.indilib.i4j.protocol.api.INDIConnection;
import org.indilib.i4j.protocol.api.INDIInputStream;
import org.indilib.i4j.protocol.api.INDIOutputStream;
import org.indilib.i4j.protocol.io.INDISocketConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A class that represent a Network Device (another INDI server).
 * 
 * @author S. Alonso (Zerjillo) [zerjioi at ugr.es]
 * @author Richard van Nieuwenhoven
 */
public class INDINetworkDevice extends INDIDevice {

    /**
     * The logger to log to.
     */
    private static final Logger LOG = LoggerFactory.getLogger(INDINetworkDevice.class);

    /**
     * The host to connect for the INDI Server.
     */
    private String host;

    /**
     * A list of names of the Device (it may be more than one).
     */
    private List<String> names;

    /**
     * The port to connect for the INDI Server.
     */
    private int port;

    /**
     * The socket to connect for the INDI Server.
     */
    private INDIConnection socketConnection;

    /**
     * Constructs a new Network Device and connects to it.
     * 
     * @param server
     *            The server which listens to this Device.
     * @param host
     *            The host to connect for the Device.
     * @param port
     *            The port to connect for the Device.
     * @throws INDIException
     *             if there is any problem with the connection.
     */
    protected INDINetworkDevice(INDIServer server, String host, int port) throws INDIException {
        super(server);

        names = new ArrayList<String>();

        this.host = host;
        this.port = port;

        try {

            socketConnection = new INDISocketConnection(host, port);
        } catch (IOException e) {
            throw new INDIException("Problem connecting to " + host + ":" + port);
        }
    }

    @Override
    public void closeConnections() {
        try {
            socketConnection.close();
        } catch (IOException e) {
            LOG.warn("close connection error", e);
        }
    }

    @Override
    public String getDeviceIdentifier() {
        return getNetworkName();
    }

    @Override
    public INDIInputStream getInputStream() {
        try {
            return socketConnection.getINDIInputStream();
        } catch (IOException e) {
            LOG.warn("could not open input stream", e);
        }
        return null;
    }

    @Override
    public String[] getNames() {
        return names.toArray(new String[names.size()]);
    }

    @Override
    public INDIOutputStream getOutputStream() {
        try {
            return socketConnection.getINDIOutputStream();
        } catch (IOException e) {
            LOG.warn("could not open output stream", e);
        }
        return null;
    }

    @Override
    public void isBeingDestroyed() {
    }

    @Override
    public boolean isDevice(String deviceIdentifier) {
        return getDeviceIdentifier().equals(deviceIdentifier);
    }

    /**
     * Gets a String representation of the Device.
     * 
     * @return A String representation of the Device.
     */
    @Override
    public String toString() {
        return "Network Device: " + this.getNetworkName() + " - " + Arrays.toString(getNames());
    }

    /**
     * Deals with a possible new Device name, adding it if it is new.
     * 
     * @param possibleNewName
     *            The new possible new name.
     */
    @Override
    protected void dealWithPossibleNewDeviceName(String possibleNewName) {
        if (!names.contains(possibleNewName)) {
            names.add(possibleNewName);
        }
    }

    /**
     * Checks if the Device has a particular name.
     * 
     * @param name
     *            The name to check.
     * @return <code>true</code> if the Device respond to <code>name</code>.
     *         <code>false</code> otherwise.
     */
    @Override
    protected boolean hasName(String name) {
        return names.contains(name);
    }

    /**
     * Gets a String with the host and port of the connection.
     * 
     * @return A String with the host and port of the connection.
     */
    private String getNetworkName() {
        return host + ":" + port;
    }
}
