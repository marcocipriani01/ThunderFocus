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

import org.indilib.i4j.INDIException;
import org.indilib.i4j.protocol.api.INDIConnection;

import java.net.URL;
import java.util.List;

/**
 * The core indi server interface. over this interface the server can be
 * controlled completely. from starting and stopping drivers to
 * 
 * @author Richard van Niewenhoven
 */
public interface INDIServerInterface {

    /**
     * add a server event handler to the server. the handler will get
     * notifications specified there.
     * 
     * @param eventHandler
     *            the new event handler.
     */
    void addEventHandler(INDIServerEventHandler eventHandler);

    /**
     * deaktivate the java driver by it's name. the name is case insensitiv and
     * the simple name may be used (if it is unique)
     * 
     * @param className
     *            the class name
     * @throws INDIException
     *             if the classes could not be loded or not started.
     */
    void destroyJavaDriver(String className) throws INDIException;

    /**
     * Destroys a Native Driver.
     * 
     * @param driverPath
     *            The path of the Driver to be destroyed.
     */
    void destroyNativeDriver(String driverPath);

    /**
     * Destroys a Network Driver.
     * 
     * @param host
     *            The host of the Driver.
     * @param port
     *            The port of the Driver.
     */
    void destroyNetworkDriver(String host, int port);

    /**
     * Gets the list of loaded Devices.
     * 
     * @return The list of loaded Devices.
     */
    List<INDIDeviceInterface> getDevices();

    /**
     * Checks if a particular Driver is already loaded.
     * 
     * @param deviceIdentifier
     *            The device identifier.
     * @return <code>true</code> if the Driver identified by
     *         <code>deviceIdentifier</code> is already loaded.
     *         <code>false</code> otherwise.
     */
    boolean isAlreadyLoaded(String deviceIdentifier);

    /**
     * Gets if the server is listening for new Clients to connect.
     * 
     * @return <code>true</code> if the server is listening for new Clients.
     *         <code>false</code> otherwise.
     */
    boolean isServerRunning();

    /**
     * start a java driver class by it's name. the name is case insensitiv and
     * the simple name may be used (if it is unique)
     * 
     * @param className
     *            the class name
     * @throws INDIException
     *             if the classes could not be loded or not started.
     */
    void loadJavaDriver(String className) throws INDIException;

    void loadJavaDriver(Class<?> cls) throws INDIException;

    /**
     * add the jar to the classpath and load all drivers defined in it.
     * 
     * @param jarFileName
     *            the file name of the jar
     * @throws INDIException
     *             if the classes could not be loded or not started.
     */
    void loadJavaDriversFromJAR(String jarFileName) throws INDIException;

    /**
     * Loads a Native Driver.
     * 
     * @param driverPath
     *            The Driver path name. It will be executed in a separate
     *            process.
     * @throws INDIException
     *             if there is any problem executing the Driver.
     */
    void loadNativeDriver(String driverPath) throws INDIException;

    /**
     * Loads a Network Driver.
     * 
     * @param host
     *            The host of the Network Driver.
     * @param port
     *            The port of the Network Driver.
     * @throws INDIException
     *             if there is any problem with the connection.
     */
    void loadNetworkDriver(String host, int port) throws INDIException;

    /**
     * Stops the server from listening new Clients. All connections with
     * existing clients are also broken.
     */
    void stopServer();

    /**
     * @return a list of available java devices.
     */
    List<String> getAvailableDevices();

    /**
     * @return the host name where the server is listening.
     */
    String getHost();

    /**
     * @return the port where the server is listening.
     */
    int getPort();

    /**
     * Activate a server acceptor.
     * 
     * @param name
     *            the acceport name
     * @param arguments
     *            the arguments for the acceptor.
     */
    void activateAcceptor(String name, Object... arguments);

    /**
     * @return a new connection to the server. the caller is responsible to
     *         close it.
     */
    INDIConnection createConnection();

    /**
     * integrate this server connection in the server. threat it as a client
     * connection.
     * 
     * @param indiConnection
     *            the connection to add.
     * @return true if the connection was accepted by the server.
     */
    boolean addConnection(INDIConnection indiConnection);

    /**
     * is the specified url pointing to this local server?
     * 
     * @param url
     *            the url to check
     * @return true if the local server is listening there.
     */
    boolean isLocalURL(URL url);
}
