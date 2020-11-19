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
import org.indilib.i4j.Constants.BLOBEnables;
import org.indilib.i4j.INDIProtocolReader;
import org.indilib.i4j.protocol.EnableBLOB;
import org.indilib.i4j.protocol.GetProperties;
import org.indilib.i4j.protocol.INDIProtocol;
import org.indilib.i4j.protocol.NewVector;
import org.indilib.i4j.protocol.api.INDIConnection;
import org.indilib.i4j.protocol.api.INDIInputStream;
import org.indilib.i4j.server.api.INDIClientInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * A class to represent a Client that connects to the Server.
 * 
 * @author S. Alonso (Zerjillo) [zerjioi at ugr.es]
 */
public class INDIClient extends INDIDeviceListener implements INDIClientInterface {

    /**
     * Logger to log to.
     */
    private static final Logger LOG = LoggerFactory.getLogger(INDIClient.class);

    /**
     * The reader.
     */
    private INDIProtocolReader reader;

    /**
     * The Server to which the Client is connected.
     */
    private INDIServer server;

    /**
     * The socket to communicate with the Client.
     */
    private INDIConnection connection;

    /**
     * Constructs a new INDIClient that connects to the server and starts
     * listening to it.
     * 
     * @param connection
     *            The socket to communicate with the Client.
     * @param server
     *            The Server to which the Client is connected.
     */
    public INDIClient(INDIConnection connection, INDIServer server) {
        this.connection = connection;
        this.server = server;

        reader = new INDIProtocolReader(this, "client reader " + connection.getURL());
        reader.start();
    }

    @Override
    public void finishReader() {
        server.removeClient(this);
    }

    /**
     * Gets a String representation of the host and port of the Client.
     * 
     * @return A String representation of the host and port of the Client.
     */
    @Override
    public String getInetAddress() {
        if (connection != null) {
            return connection.toString();
        } else {
            return "closed";
        }
    }

    @Override
    public INDIInputStream getInputStream() {
        try {
            return connection.getINDIInputStream();
        } catch (IOException e) {
            LOG.error("could not open input stream to indi-connection", e);
            return null;
        }
    }

    /**
     * Explicitly disconnects the Client.
     */
    protected void disconnect() {
        if (connection != null) {
            try {
                reader.setStop(true);
                connection.close();
                connection = null;
            } catch (Exception e) {
                LOG.error("disconnect exception", e);
            }
        }
    }

    @Override
    public void processProtokolMessage(INDIProtocol<?> message) {
        if (message instanceof GetProperties) {
            processGetProperties((GetProperties) message);
        } else if (message instanceof NewVector) {
            processNewXXXVector((NewVector<?>) message);
        } else if (message instanceof EnableBLOB) {
            processEnableBLOB((EnableBLOB) message);
        }
    }

    /**
     * notify server of get properties if they are listening.
     * 
     * @param xml
     *            the xml message
     */
    @Override
    protected void processGetProperties(GetProperties xml) {
        String version = xml.getVersion().trim();
        if (version.isEmpty()) { // Some conditions to ignore the messages
            return;
        }
        super.processGetProperties(xml);
        server.notifyClientListenersGetProperties(this, xml);
    }

    @Override
    protected void sendXMLMessage(INDIProtocol<?> message) {
        try {
            connection.getINDIOutputStream().writeObject(message);
        } catch (Exception e) {
            LOG.warn("could not send xml message to client, disconnecting client", e);
            disconnect();
        }
    }

    /**
     * Adds the appropriate BLOB Enable rules.
     * 
     * @param xml
     *            xml message
     */
    private void processEnableBLOB(EnableBLOB xml) {
        if (!xml.hasDevice()) {
            return;
        }
        String rule = xml.getTextContent();
        BLOBEnables enable;

        try {
            // TODO: move this to a xtream enum
            enable = Constants.parseBLOBEnable(rule);
        } catch (IllegalArgumentException e) {
            return;
        }

        if (!xml.hasName()) {
            if (this.listensToDevice(xml.getDevice())) {
                this.addBLOBEnableRule(xml.getDevice(), enable);
                server.notifyClientListenersEnableBLOB(this, xml);
            }
        } else {
            if (this.listensToProperty(xml.getDevice(), xml.getName())) {
                this.addBLOBEnableRule(xml.getDevice(), xml.getName(), enable);
                server.notifyClientListenersEnableBLOB(this, xml);
            }
        }
    }

    /**
     * notify clients of new XXX Vector if they are listening.
     * 
     * @param xml
     *            the xml messge.
     */
    private void processNewXXXVector(NewVector<?> xml) {
        String device = xml.getDevice();
        if (device.isEmpty()) {
            return;
        }

        String property = xml.getName().trim();
        if (property.isEmpty()) {
            return;
        }

        if (this.listensToProperty(device, property)) { // If this client does
                                                        // not listen to the
                                                        // property avoid
                                                        // changing it
            server.notifyClientListenersNewXXXVector(this, xml);
        }
    }
}
