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
import org.indilib.i4j.driver.INDIDriver;
import org.indilib.i4j.protocol.api.INDIConnection;
import org.indilib.i4j.protocol.api.INDIInputStream;
import org.indilib.i4j.protocol.api.INDIOutputStream;
import org.indilib.i4j.protocol.io.INDIPipedConnections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

/**
 * A class that represent a Java Device (created with the INDI Driver library).
 * 
 * @author S. Alonso (Zerjillo) [zerjioi at ugr.es]
 * @author Richard van Nieuwenhoven
 */
public class INDIJavaDevice extends INDIDevice {

    /**
     * The logger to log to.
     */
    private static final Logger LOG = LoggerFactory.getLogger(INDIJavaDevice.class);

    /**
     * The Driver.
     */
    private INDIDriver driver;

    /**
     * The class of the Driver.
     */
    private Class<?> driverClass;

    /**
     * An identifier of the Java Device. Can be the name of a JAR file or any
     * other String, but it must be UNIQUE.
     */
    private String identifier;

    /**
     * A list of names of the Device (it may be more than one).
     */
    private List<String> names;

    /**
     * A buffer to send information from and to the Driver.
     */
    private INDIConnection driverConnection;

    /**
     * Constructs a new Java Device and starts listening to its messages.
     * 
     * @param server
     *            The server which listens to this Device.
     * @param driverClass
     *            The class of the Driver.
     * @param identifier
     *            The JAR file from where to load the Driver.
     * @throws INDIException
     *             if there is any problem instantiating the Driver.
     */
    protected INDIJavaDevice(INDIServer server, Class<?> driverClass, String identifier) throws INDIException {
        super(server);

        // name = null;
        names = new ArrayList<String>();
        this.identifier = identifier;
        this.driverClass = driverClass;

        INDIPipedConnections connections = new INDIPipedConnections();

        driverConnection = connections.first();
        try {
            Constructor<?> c = driverClass.getConstructor(INDIConnection.class);
            driver = (INDIDriver) c.newInstance(connections.second());
        } catch (InstantiationException ex) {
            LOG.error("Problem instantiating driver (not an INDI for Java Driver?)", ex);
            throw new INDIException("Problem instantiating driver (not an INDI for Java Driver?)", ex);
        } catch (IllegalAccessException ex) {
            LOG.error("Problem instantiating driver (not an INDI for Java Driver?)", ex);
            throw new INDIException("Problem instantiating driver (not an INDI for Java Driver?)", ex);
        } catch (NoSuchMethodException ex) {
            LOG.error("Problem instantiating driver (not an INDI for Java Driver?)", ex);
            throw new INDIException("Problem instantiating driver (not an INDI for Java Driver?)", ex);
        } catch (InvocationTargetException ex) {
            LOG.error("Problem instantiating driver (not an INDI for Java Driver?)", ex);
            throw new INDIException("Problem instantiating driver (not an INDI for Java Driver?)", ex);
        } catch (ClassCastException ex) {
            LOG.error("Problem instantiating driver (not an INDI for Java Driver?)", ex);
            throw new INDIException("Problem instantiating driver (not an INDI for Java Driver?)", ex);
        }

        driver.startListening();
    }

    @Override
    public void closeConnections() {
        try {
            driverConnection.close();
        } catch (IOException e) {
            LOG.warn("close connection error", e);
        }
    }

    @Override
    public String getDeviceIdentifier() {
        return identifier + "-+-" + driverClass.getName();
    }

    @Override
    public INDIInputStream getInputStream() {
        try {
            return driverConnection.getINDIInputStream();
        } catch (IOException e) {
            throw new IllegalStateException("could not get input stream from driver");
        }
    }

    @Override
    public String[] getNames() {
        return names.toArray(new String[names.size()]);
    }

    @Override
    public INDIOutputStream getOutputStream() {
        try {
            return driverConnection.getINDIOutputStream();
        } catch (IOException e) {
            throw new IllegalStateException("could not get output stream from driver");
        }
    }

    @Override
    public void isBeingDestroyed() {
        driver.isBeingDestroyed();
    }

    /**
     * Checks if the Device corresponds to a particular Device Identifier.
     * 
     * @param deviceIdentifier
     *            The Device Identifier to check.
     * @return <code>true</code> if the Device corresponds to the Device
     *         Identifier (that is, is in the jar file).
     */
    @Override
    public boolean isDevice(String deviceIdentifier) {
        return getDeviceIdentifier().startsWith(deviceIdentifier);
    }

    /**
     * Gets a String representation of the Device.
     * 
     * @return A String representation of the Device.
     */
    @Override
    public String toString() {
        return "Java Device: " + identifier + " - " + driverClass.getName();
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
}
