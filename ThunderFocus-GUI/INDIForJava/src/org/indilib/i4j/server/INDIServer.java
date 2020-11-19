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

import org.indilib.i4j.Constants;
import org.indilib.i4j.INDIException;
import org.indilib.i4j.driver.INDIDriver;
import org.indilib.i4j.protocol.DelProperty;
import org.indilib.i4j.protocol.GetProperties;
import org.indilib.i4j.protocol.INDIProtocol;
import org.indilib.i4j.protocol.SetBlobVector;
import org.indilib.i4j.protocol.api.INDIConnection;
import org.indilib.i4j.protocol.io.INDIPipedConnections;
import org.indilib.i4j.server.api.INDIDeviceInterface;
import org.indilib.i4j.server.api.INDIServerEventHandler;
import org.indilib.i4j.server.api.INDIServerInterface;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.IndexView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.*;

/**
 * A class representing a INDI Server. It is in charge of dealing with several
 * Drivers and Clients which interexchange messages. Check the INDI
 * documentation to better understand its pourpose. It has the appropriate
 * methods to load / unload several kinds of Drivers: Java Drivers (created with
 * the INDI for Java Driver library, Native Drivers (probably created with the
 * original INDI Library and launched as external processes) and Network Devices
 * (which consist on other INDI Servers).
 * 
 * @author S. Alonso (Zerjillo) [zerjioi at ugr.es]
 * @author Richard van Nieuwenhoven
 */
public final class INDIServer implements INDIServerInterface {

    /**
     * Driver class id prefix.
     */
    private static final String DRIVER_CLASS_ID_PREFIX = "class+-+";

    /**
     * Logger to log to.
     */
    private static final Logger LOG = LoggerFactory.getLogger(INDIServer.class);

    /**
     * A list of clients (and devices if they are snooping) connected to the
     * server.
     */
    private List<INDIDeviceListener> clients;

    /**
     * A list of Devices loaded by the server.
     */
    private List<INDIDevice> devices;

    /**
     * the list with listeners to server events.
     */
    private List<INDIServerEventHandler> eventHandlers = Collections.synchronizedList(new ArrayList<INDIServerEventHandler>());

    /**
     * The basis acceptor, normally a socket acceptor the basic indi protocol.
     */
    private INDIServerAcceptor baseAcceptor;

    /**
     * the list with additional loaded acceptors.
     */
    private List<INDIServerAcceptor> additionalAcceptors = new ArrayList<INDIServerAcceptor>();

    /**
     * Constructs a new Server. The Server begins to listen to the default port.
     */
    protected INDIServer() {
        this(Constants.INDI_DEFAULT_PORT);
    }

    /**
     * Constructs a new Server. The Server begins to listen to a particular
     * port.
     * 
     * @param listeningPort
     *            The port to which the Server will listen.
     */
    protected INDIServer(Integer listeningPort) {
        baseAcceptor = new INDIServerSocketAcceptor() {

            @Override
            public boolean acceptClient(INDIConnection clientConnection) {
                return acceptINDIConnection(clientConnection);
            }
        };
        baseAcceptor.setArguments(listeningPort);
        initServer();
    }

    @Override
    public void addEventHandler(INDIServerEventHandler eventHandler) {
        eventHandlers.add(eventHandler);
    }

    @Override
    public synchronized void destroyJavaDriver(String className) throws INDIException {
        Class<?> driverclass = loadDriverClassByName(className);
        if (driverclass != null) {
            destroyJavaDriver(driverclass);
        } else {
            throw new INDIException("No driver found with name " + className);
        }
    }

    @Override
    public synchronized void destroyNativeDriver(String driverPath) {
        LOG.info("Removing native driver " + driverPath);

        destroyIdentifiedDrivers(driverPath);
    }

    @Override
    public synchronized void destroyNetworkDriver(String host, int port) {
        String networkName = host + ":" + port;

        LOG.info("Removing network driver " + networkName);

        destroyIdentifiedDrivers(networkName);
    }

    @Override
    public List<String> getAvailableDevices() {
        List<String> result = new ArrayList<>();
        for (ClassInfo subclass : Util.classPathIndex().getAllKnownSubclasses(DotName.createSimple(INDIDriver.class.getName()))) {
            if (!Modifier.isAbstract(subclass.flags())) {
                result.add(subclass.toString());
            }
        }
        return result;
    }

    @Override
    public List<INDIDeviceInterface> getDevices() {
        return new ArrayList<INDIDeviceInterface>(devices);
    }

    @Override
    public boolean isAlreadyLoaded(String deviceIdentifier) {
        for (INDIDevice d : staticCopyOfDevices()) {
            if (d.isDevice(deviceIdentifier)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isServerRunning() {
        return baseAcceptor.isRunning();
    }

    @Override
    public synchronized void loadJavaDriver(String className) throws INDIException {
        Class<?> driverclass = loadDriverClassByName(className);
        if (driverclass != null) {
            loadJavaDriver(driverclass);
        } else {
            throw new INDIException("No driver found with name " + className);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public synchronized void loadJavaDriversFromJAR(String jarFileName) throws INDIException {
        IndexView jarIndex = Util.extendClasspath(new File(jarFileName));
        if (jarIndex != null) {
            for (ClassInfo subclass : jarIndex.getAllKnownSubclasses(DotName.createSimple(INDIDriver.class.getName()))) {
                if (!Modifier.isAbstract(subclass.flags())) {
                    Class<INDIDriver> clazz;
                    try {
                        clazz = (Class<INDIDriver>) Thread.currentThread().getContextClassLoader().loadClass(subclass.name().toString());
                    } catch (ClassNotFoundException e) {
                        throw new INDIException("could not load diver class " + subclass.toString(), e);
                    }
                    loadJavaDriver(clazz);
                }
            }

        }
    }

    @Override
    public synchronized void loadNativeDriver(String driverPath) throws INDIException {

        if (isAlreadyLoaded(driverPath)) {
            throw new INDIException("Driver already loaded.");
        }

        LOG.info("Loading Native Driver " + driverPath);

        INDINativeDevice newDevice;

        newDevice = new INDINativeDevice(this, driverPath);

        addDevice(newDevice);
    }

    @Override
    public synchronized void loadNetworkDriver(String host, int port) throws INDIException {
        String networkName = host + ":" + port;

        if (isAlreadyLoaded(networkName)) {
            throw new INDIException("Network Driver " + networkName + " already loaded.");
        }

        LOG.info("Loading Network Driver " + networkName);

        INDINetworkDevice newDevice;

        newDevice = new INDINetworkDevice(this, host, port);

        addDevice(newDevice);
    }

    @Override
    public void stopServer() {
        // Close the socket in order to avoid accepting new connections
        if (baseAcceptor != null) {
            baseAcceptor.close();
        }
        for (INDIDeviceListener indiDeviceListener : staticCopyOfClients()) {
            if (indiDeviceListener instanceof INDIClient) {
                try {
                    ((INDIClient) indiDeviceListener).disconnect();
                } catch (Exception e) {
                    LOG.warn("problem during client diskonnect", e);
                }
            }
        }
    }

    /**
     * @return a static list to iterate without the problem of concurent
     *         modifications.
     */
    private INDIDeviceListener[] staticCopyOfClients() {
        return clients.toArray(new INDIDeviceListener[clients.size()]);
    }

    /**
     * @return a static list to iterate without the problem of concurent
     *         modifications.
     */
    private INDIDevice[] staticCopyOfDevices() {
        return devices.toArray(new INDIDevice[devices.size()]);
    }

    /**
     * @return a static list to iterate without the problem of concurent
     *         modifications.
     */
    private INDIServerEventHandler[] staticCopyOfEventHandlers() {
        return eventHandlers.toArray(new INDIServerEventHandler[eventHandlers.size()]);
    }

    /**
     * send the notification to all event handlers that a client connection was
     * broken.
     * 
     * @param client
     *            the client who's connection broke.
     */
    protected void connectionWithClientBroken(INDIClient client) {
        for (INDIServerEventHandler handler : staticCopyOfEventHandlers()) {
            handler.connectionWithClientBroken(client);
        }
    }

    /**
     * send the notification to all event handlers that a client connection was
     * establisched.
     * 
     * @param client
     *            the client who's connection was estebisched.
     */
    protected void connectionWithClientEstablished(INDIClient client) {
        for (INDIServerEventHandler handler : staticCopyOfEventHandlers()) {
            handler.connectionWithClientEstablished(client);
        }
    }

    /**
     * send the notification to all event handlers that a driver was
     * disconnected.
     * 
     * @param device
     *            the device that was disconnected.
     */
    protected void driverDisconnected(INDIDevice device) {
        for (INDIServerEventHandler handler : staticCopyOfEventHandlers()) {
            handler.driverDisconnected(device);
        }
    }

    /**
     * Gets a list of Clients that listen to a Device.
     * 
     * @param deviceName
     *            The name of the Device.
     * @return A list of Clients that specifically listen to a Device.
     */
    protected List<INDIDeviceListener> getClientsListeningToDevice(String deviceName) {
        List<INDIDeviceListener> list = new ArrayList<INDIDeviceListener>();
        for (INDIDeviceListener c : staticCopyOfClients()) {
            if (c.listensToDevice(deviceName)) {
                list.add(c);
            }
        }
        return list;
    }

    /**
     * Gets a list of Clients that listen to a Property.
     * 
     * @param deviceName
     *            The name of the Device of the Property.
     * @param propertyName
     *            The name of the Property.
     * @return A list of Clients that listen to a Property.
     */
    protected List<INDIDeviceListener> getClientsListeningToProperty(String deviceName, String propertyName) {
        List<INDIDeviceListener> list = new ArrayList<INDIDeviceListener>();
        for (INDIDeviceListener c : staticCopyOfClients()) {
            if (c.listensToProperty(deviceName, propertyName)) {
                list.add(c);
            }
        }
        return list;
    }

    /**
     * Gets a list of Clients that listen to Property updates.
     * 
     * @param deviceName
     *            The name of the Device of the Property.
     * @param propertyName
     *            The name of the Property.
     * @param isBLOB
     *            If the Property is a BLOB one.
     * @return A list of Clients that listen to a Property.
     */
    protected List<INDIDeviceListener> getClientsListeningToPropertyUpdates(String deviceName, String propertyName, boolean isBLOB) {
        List<INDIDeviceListener> list = new ArrayList<INDIDeviceListener>();
        for (INDIDeviceListener c : staticCopyOfClients()) {
            if (c.listensToProperty(deviceName, propertyName)) {
                if (isBLOB) {
                    if (c.isBLOBAccepted(deviceName, propertyName)) {
                        list.add(c);
                    }
                } else {
                    if (c.areNonBLOBsAccepted(deviceName)) {
                        list.add(c);
                    }
                }
            }
        }
        return list;
    }

    /**
     * Gets a list of Clients that specifically listen to a Property of a
     * Device.
     * 
     * @param deviceName
     *            The name of the Device.
     * @return A list of Clients that specifically listen to a Property of a
     *         Device.
     */
    protected List<INDIDeviceListener> getClientsListeningToSingleProperties(String deviceName) {
        List<INDIDeviceListener> list = new ArrayList<INDIDeviceListener>();
        for (INDIDeviceListener c : staticCopyOfClients()) {
            if (c.listensToSingleProperty(deviceName)) {
                list.add(c);
            }
        }
        return list;
    }

    /**
     * Gets a Device given its name.
     * 
     * @param deviceName
     *            The name of the Device to get.
     * @return The Device with name <code>deviceName</code>.
     */
    protected INDIDevice getDevice(String deviceName) {
        for (INDIDevice device : staticCopyOfDevices()) {
            if (device.hasName(deviceName)) {
                return device;
            }
        }
        return null;
    }

    /**
     * Does nothing since the enableBLOB message is not usually useful for
     * Devices. The control of when to send the BLOB values is automatically
     * done by the Server.
     * 
     * @param client
     *            The Client sending the message.
     * @param xml
     *            The message
     */
    protected void notifyClientListenersEnableBLOB(INDIClient client, INDIProtocol<?> xml) {
        /*
         * String device = xml.getAttribute("device").trim(); INDIDevice d =
         * this.getDevice(device); if (d != null) { d.sendXMLMessage(xml); }
         */
    }

    /**
     * Sends the <code>getProperties</code> message to the appropriate Devices.
     * 
     * @param client
     *            The Client sending the message.
     * @param xml
     *            The message
     */
    protected void notifyClientListenersGetProperties(INDIDeviceListener client, INDIProtocol<?> xml) {
        String device = xml.getDevice();

        INDIDevice d = this.getDevice(device);

        if (d == null) {
            sendXMLMessageToAllDevices(xml);
        } else {
            d.sendXMLMessage(xml);
        }
    }

    /**
     * Sends the <code>newXXXVector</code> message to the appropriate Devices.
     * 
     * @param client
     *            The Client sending the message.
     * @param xml
     *            The message
     */
    protected void notifyClientListenersNewXXXVector(INDIClient client, INDIProtocol<?> xml) {
        String device = xml.getDevice();
        INDIDevice d = this.getDevice(device);

        if (d != null) {
            d.sendXMLMessage(xml);
        }
    }

    /**
     * Sends the <code>defXXXVector</code> message to the appropriate Clients.
     * 
     * @param device
     *            The Device sending the message.
     * @param xml
     *            The message
     */
    protected void notifyDeviceListenersDefXXXVector(INDIDevice device, INDIProtocol<?> xml) {
        String deviceName = xml.getDevice();
        String propertyName = xml.getName().trim();
        for (INDIDeviceListener c : getClientsListeningToProperty(deviceName, propertyName)) {
            c.sendXMLMessage(xml);
        }
    }

    /**
     * Sends the <code>delProperty</code> message to the appropriate Clients.
     * 
     * @param device
     *            The Device sending the message.
     * @param xml
     *            The message
     */
    protected void notifyDeviceListenersDelProperty(INDIDevice device, INDIProtocol<?> xml) {
        String deviceName = xml.getDevice();
        for (INDIDeviceListener c : getClientsListeningToDevice(deviceName)) {
            c.sendXMLMessage(xml);
        }
    }

    /**
     * Sends the <code>message</code> message to the appropriate Clients..
     * 
     * @param device
     *            The Device sending the message.
     * @param xml
     *            The message
     */
    protected void notifyDeviceListenersMessage(INDIDevice device, INDIProtocol<?> xml) {
        String deviceName = xml.getDevice();

        if (deviceName.isEmpty()) {
            sendXMLMessageToAllClients(xml);
        } else {
            for (INDIDeviceListener c : getClientsListeningToDevice(deviceName)) {
                c.sendXMLMessage(xml);
            }
        }
    }

    /**
     * Sends the <code>setXXXVector</code> message to the appropriate Clients.
     * 
     * @param device
     *            The Device sending the message.
     * @param xml
     *            The message
     */
    protected void notifyDeviceListenersSetXXXVector(INDIDevice device, INDIProtocol<?> xml) {
        String deviceName = xml.getDevice();
        String propertyName = xml.getName().trim();

        boolean isBLOB = false;

        if (xml instanceof SetBlobVector) {
            isBLOB = true;
        }
        for (INDIDeviceListener c : getClientsListeningToPropertyUpdates(deviceName, propertyName, isBLOB)) {
            c.sendXMLMessage(xml);
        }
    }

    /**
     * Removes a Client from the List of clients. Called by the clients when the
     * connection is broken.
     * 
     * @param client
     *            The Client to remove.
     */
    protected void removeClient(INDIClient client) {
        clients.remove(client);

        connectionWithClientBroken(client);
    }

    /**
     * Removes a Device from the list of devices. Called by the Devices to be
     * removed when connection brokes
     * 
     * @param device
     *            The Device to be removed.
     */
    protected void removeDevice(INDIDevice device) {
        String[] names = device.getNames();

        devices.remove(device);
        clients.remove(device);

        notifyClientsDeviceRemoved(names);

        driverDisconnected(device);
    }

    /**
     * Sends a XML message to all the Clients.
     * 
     * @param xml
     *            The message to send.
     */
    protected void sendXMLMessageToAllClients(INDIProtocol<?> xml) {
        for (INDIDeviceListener c : staticCopyOfClients()) {
            if (c instanceof INDIClient) {
                c.sendXMLMessage(xml);
            }
        }
    }

    /**
     * Sends a XML message to all the Devices.
     * 
     * @param xml
     *            The message to send.
     */
    protected void sendXMLMessageToAllDevices(INDIProtocol<?> xml) {
        for (INDIDevice d : staticCopyOfDevices()) {
            d.sendXMLMessage(xml);
        }
    }

    /**
     * Must return <code>true</code> is the Client that stablished this
     * connection must be allowed in the Server. Otherwise the connection will
     * be closed.
     * 
     * @param clientSocket
     *            The socket created with a possible client.
     * @return <code>true</code> if the Client is allowed to connect to the
     *         server. <code>false</code> otherwise.
     */
    private boolean acceptClient(INDIConnection clientSocket) {
        for (INDIServerEventHandler handler : staticCopyOfEventHandlers()) {
            if (!handler.acceptClient(clientSocket)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Adds a Device to the list of devices, starts its reading process and asks
     * for the properties.
     * 
     * @param device
     *            The device to add.
     */
    private void addDevice(INDIDevice device) {
        devices.add(device);
        clients.add(device);

        device.startReading();
        // Force the device to send its properties for already connected clients
        GetProperties message = new GetProperties().setVersion("1.7");

        device.sendXMLMessage(message);
    }

    /**
     * Destroys the Devices with a particular identifier. Note that the Devices
     * will be removed from the list when their listening thread ends (which may
     * occur in the near future in another Thread).
     * 
     * @param deviceIdentifier
     *            The device identifier.
     */
    private synchronized void destroyIdentifiedDrivers(String deviceIdentifier) {
        for (INDIDevice device : getDevicesWithIdentifier(deviceIdentifier)) {
            device.destroy();
        }
    }

    /**
     * Removes a Java Driver by its class.
     * 
     * @param cls
     *            The class of the Java Driver to remove.
     */
    private synchronized void destroyJavaDriver(Class<?> cls) {
        LOG.info("Removing driver " + cls.getName());

        destroyIdentifiedDrivers(DRIVER_CLASS_ID_PREFIX + cls.getName());
    }

    /**
     * find the driver class by class name (the name is case insensitive and the
     * simple name may be used.
     * 
     * @param className
     *            the name of the class
     * @return the found driver class
     */
    private String findDriverClassByName(String className) {
        String driverclassName = null;
        for (ClassInfo subclass : Util.classPathIndex().getAllKnownSubclasses(DotName.createSimple(INDIDriver.class.getName()))) {
            if (!Modifier.isAbstract(subclass.flags())) {
                if (className.equalsIgnoreCase(subclass.toString()) || subclass.toString().endsWith("." + className)) {
                    driverclassName = subclass.toString();
                }
            }
        }
        return driverclassName;
    }

    /**
     * Gets a list of devices with a particular identifier. Note that may be
     * more than one in the case of Java Drivers as there might be more than one
     * in a single JAR file.
     * 
     * @param deviceIdentifier
     *            The device identifier.
     * @return A list of devices with a particular identifier.
     */
    private List<INDIDevice> getDevicesWithIdentifier(String deviceIdentifier) {
        List<INDIDevice> found = new ArrayList<INDIDevice>();
        for (INDIDevice device : staticCopyOfDevices()) {
            if (device.isDevice(deviceIdentifier)) {
                found.add(device);
            }
        }
        return found;
    }

    /**
     * Inits the Server and launches the listening thread.
     */
    private void initServer() {
        devices = Collections.synchronizedList(new ArrayList<INDIDevice>());
        clients = Collections.synchronizedList(new ArrayList<INDIDeviceListener>());

        startListeningToClients();
    }

    /**
     * load the driver by class name (the name is case insensitive and the
     * simple name may be used.
     * 
     * @param className
     *            the name of the class
     * @return the found driver class
     * @throws INDIException
     *             when something serius went wrong.
     */
    private Class<?> loadDriverClassByName(String className) throws INDIException {
        String driverclassName = findDriverClassByName(className);
        Class<?> driverclass;
        try {
            driverclass = Thread.currentThread().getContextClassLoader().loadClass(driverclassName);
        } catch (Exception e) {
            throw new INDIException("Could not create driver" + className, e);
        }
        return driverclass;
    }

    /**
     * Loads a particular Java Driver that is already in the classpath.
     * 
     * @param cls
     *            The Class of the driver to load.
     * @throws INDIException
     *             If there is any problem instantiating the Driver.
     */
    public synchronized void loadJavaDriver(Class<?> cls) throws INDIException {
        loadJavaDriver(cls, DRIVER_CLASS_ID_PREFIX + cls.getName());
    }

    /**
     * Loads a particular Java Driver.
     * 
     * @param cls
     *            The Class of the driver to load.
     * @param identifier
     *            A UNIQUE identifier. Please note that if there are repeated
     *            identifiers strange things may happen. MUST BE CHECKED IN THE
     *            FUTURE.
     * @throws INDIException
     *             If there is any problem instantiating the Driver.
     */
    private synchronized void loadJavaDriver(Class<?> cls, String identifier) throws INDIException {
        INDIJavaDevice newDevice = new INDIJavaDevice(this, cls, identifier);

        addDevice(newDevice);
    }

    /**
     * Notifies the listening clients that some particular Devices have been
     * removed by sending <code>delProperty</code> messages.
     * 
     * @param deviceNames
     *            The names of the Devices that have been removed.
     */
    private void notifyClientsDeviceRemoved(String[] deviceNames) {
        for (String deviceName : deviceNames) {
            DelProperty message = new DelProperty().setDevice(deviceName);
            for (INDIDeviceListener c : this.getClientsListeningToDevice(deviceName)) {
                c.sendXMLMessage(message);
            }
            for (INDIDeviceListener c : this.getClientsListeningToSingleProperties(deviceName)) {
                c.sendXMLMessage(message);
            }
        }
    }

    /**
     * Starts the listening Thread. Should not be called unless the server has
     * been explicitly stopped.
     */
    private void startListeningToClients() {
        baseAcceptor.start();
    }

    @Override
    public String getHost() {
        return baseAcceptor.getHost();
    }

    @Override
    public int getPort() {
        return baseAcceptor.getPort();
    }

    @Override
    public void activateAcceptor(String name, Object... arguments) {
        Iterator<INDIServerAcceptor> acceptors = ServiceLoader.load(INDIServerAcceptor.class).iterator();
        while (acceptors.hasNext()) {
            INDIServerAcceptor indiServerAcceptor = acceptors.next();
            if (indiServerAcceptor.getName().equalsIgnoreCase(name)) {
                additionalAcceptors.add(indiServerAcceptor);
                indiServerAcceptor.setArguments(arguments);
                indiServerAcceptor.start();
            }
        }

    }

    @Override
    public INDIConnection createConnection() {
        INDIPipedConnections connectionPair = new INDIPipedConnections();
        addConnection(connectionPair.first());
        return connectionPair.second();
    }

    @Override
    public boolean addConnection(INDIConnection indiConnection) {
        return acceptINDIConnection(indiConnection);
    }

    /**
     * test if the server accepts the connection and when it does, wrap a client
     * around it and add it to the clients.
     * 
     * @param clientConnection
     *            the client connection to check
     * @return true if the client was accepted.
     */
    protected boolean acceptINDIConnection(INDIConnection clientConnection) {
        if (INDIServer.this.acceptClient(clientConnection)) {
            INDIClient client = new INDIClient(clientConnection, INDIServer.this);

            clients.add(client);

            connectionWithClientEstablished(client);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean isLocalURL(URL url) {
        if (baseAcceptor.isLocalURL(url)) {
            return true;
        }
        for (INDIServerAcceptor acceptor : additionalAcceptors) {
            if (acceptor.isLocalURL(url)) {
                return true;
            }
        }
        return false;
    }
}
