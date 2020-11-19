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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program. If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

import org.indilib.i4j.INDIException;
import org.indilib.i4j.protocol.api.INDIConnection;
import org.indilib.i4j.protocol.api.INDIInputStream;
import org.indilib.i4j.protocol.api.INDIOutputStream;
import org.indilib.i4j.protocol.io.INDIProcessConnection;

import java.io.IOException;

/**
 * A class that represent a Native Device (created with the usual INDI library).
 * 
 * @author S. Alonso (Zerjillo) [zerjioi at ugr.es]
 * @author Richard van Nieuwenhoven
 */
public class INDINativeDevice extends INDIDevice {

    /**
     * The path of the Driver (that will be launched).
     */
    private String driverPath;

    /**
     * The name of the device. May be null if it has not been discovered through
     * a <code>defXXXVector</code> message.
     */
    private String name;

    /**
     * The process that will be launched to start the Driver.
     */
    private Process process;

    /**
     * the connection streams to the process.
     */
    private INDIConnection processConnection;

    /**
     * Constructs a new Native Device and launches it as a external process.
     * 
     * @param server
     *            The server which listens to this Device.
     * @param driverPath
     *            The path of of the Driver.
     * @throws INDIException
     *             If there is any problem launching the external process of the
     *             driver.
     */
    protected INDINativeDevice(INDIServer server, String driverPath) throws INDIException {
        super(server);

        name = null;

        this.driverPath = driverPath;

        try {
            process = Runtime.getRuntime().exec(driverPath);
            processConnection = new INDIProcessConnection(process);
        } catch (IOException e) {
            throw new INDIException("Problem executing " + driverPath);
        }
    }

    @Override
    public void closeConnections() {
        process.destroy();
    }

    @Override
    public String getDeviceIdentifier() {
        return driverPath;
    }

    /**
     * Gets the path of the Driver.
     * 
     * @return The path of the Driver.
     */
    public String getDriverPath() {
        return driverPath;
    }

    @Override
    public INDIInputStream getInputStream() {
        try {
            return processConnection.getINDIInputStream();
        } catch (IOException e) {
            throw new IllegalStateException("could not get output stream from driver");
        }
    }

    @Override
    public String[] getNames() {
        return new String[]{
            name
        };
    }

    @Override
    public INDIOutputStream getOutputStream() {
        try {
            return processConnection.getINDIOutputStream();
        } catch (IOException e) {
            throw new IllegalStateException("could not get output stream from driver");
        }
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
        return "Native Device: " + driverPath;
    }

    /**
     * Deals with a possible new Device name. If the Device already has a name,
     * the new name is discarded.
     * 
     * @param possibleNewName
     *            The new possible new name.
     */
    @Override
    protected void dealWithPossibleNewDeviceName(String possibleNewName) {
        if (name == null) {
            name = possibleNewName;
        }
    }

    /**
     * Checks if the Device has a particular name.
     * 
     * @param nameToCheck
     *            The name to check.
     * @return <code>true</code> if the Device respond to <code>name</code>.
     *         <code>false</code> otherwise.
     */
    @Override
    protected boolean hasName(String nameToCheck) {
        if (name == null) {
            return false;
        }

        if (name.equals(nameToCheck)) {
            return true;
        }

        return false;
    }
}
