package org.indilib.i4j.driver;

/*
 * #%L
 * INDI for Java Driver Library
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

import org.indilib.i4j.Constants.SwitchStatus;
import org.indilib.i4j.INDIBLOBValue;
import org.indilib.i4j.INDIProtocolParser;
import org.indilib.i4j.INDIProtocolReader;
import org.indilib.i4j.driver.annotation.InjectExtension;
import org.indilib.i4j.driver.connection.INDIConnectionExtension;
import org.indilib.i4j.driver.event.IEventHandler;
import org.indilib.i4j.driver.util.INDIPropertyBuilder;
import org.indilib.i4j.driver.util.INDIPropertyInjector;
import org.indilib.i4j.protocol.*;
import org.indilib.i4j.protocol.api.INDIConnection;
import org.indilib.i4j.protocol.api.INDIInputStream;
import org.indilib.i4j.protocol.api.INDIOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

import static org.indilib.i4j.INDIDateFormat.dateFormat;

/**
 * A class representing a Driver in the INDI Protocol. INDI Drivers should
 * extend this class. It is in charge of stablishing the connection to the
 * clients and parsing / formating any incoming / leaving messages.
 * 
 * @author S. Alonso (Zerjillo) [zerjioi at ugr.es]
 * @author Richard van Nieuwenhoven
 */
public abstract class INDIDriver implements INDIProtocolParser {

    /**
     * The property tab for the main controls of the driver.
     */
    public static final String GROUP_MAIN_CONTROL = "Main Control";

    /**
     * The property tab for the options of the driver.
     */
    public static final String GROUP_OPTIONS = "Options";

    /**
     * The logger to log to.
     */
    private static final Logger LOG = LoggerFactory.getLogger(INDIDriver.class);

    /**
     * the connection extension that controls the connect and disconnect
     * property.
     */
    @InjectExtension
    protected INDIConnectionExtension connectionExtension;

    /**
     * the driver streamConnection (in out xml stream of messages).
     */
    private INDIConnection connection;

    /**
     * A list of Properties for this Driver.
     */
    private Map<String, INDIProperty<?>> properties;

    /**
     * The protokol reader thread that reads asynchron from the input stream,
     * and reacts on the comming indi protokol messages.
     */
    private INDIProtocolReader reader;

    /**
     * To know if the driver has already been started or not.
     */
    private boolean started;

    /**
     * A list of subdrivers.
     */
    private List<INDIDriver> subdrivers;

    /**
     * Constructs a INDIDriver with a particular <code>inputStream</code> from
     * which to read the incoming messages (from clients) and a
     * <code>outputStream</code> to write the messages to the clients.
     * 
     * @param connection
     *            The indi connection streams.
     */
    protected INDIDriver(INDIConnection connection) {
        this.connection = connection;
        subdrivers = new ArrayList<INDIDriver>();
        started = false;
        properties = new LinkedHashMap<>();
        INDIPropertyInjector.initialize(this, this);
    }

    /**
     * Adds a new Property to the Device. A message about it will be send to the
     * clients. Drivers must call this method if they want to define a new
     * Property.
     * 
     * @param property
     *            The Property to be added.
     */
    public void addProperty(INDIProperty<?> property) {
        addProperty(property, null);
    }

    @Override
    public void finishReader() {
        LOG.info("DRIVER " + getName() + " finishing");

        if (reader != null) {
            reader.setStop(true);
        }
    }

    @Override
    public INDIInputStream getInputStream() {
        try {
            return connection.getINDIInputStream();
        } catch (IOException e) {
            LOG.error("could not get input stream", e);
            return null;
        }
    }

    /**
     * Gets the name of the Driver.
     * 
     * @return The name of the Driver.
     */
    public abstract String getName();

    /**
     * Gets the <code>INDIOutputStream</code> of the driver (useful for
     * subdrivers).
     * 
     * @return The <code>INDIOutputStream</code> of the driver.
     */
    public INDIOutputStream getOutputStream() {
        try {
            return connection.getINDIOutputStream();
        } catch (IOException e) {
            LOG.error("could not get output stream", e);
            return null;
        }
    }

    /**
     * Gets a list of all the Properties in the Driver.
     * 
     * @return A List of all the Properties in the Driver.
     */
    public List<INDIProperty> getPropertiesAsList() {
        return new ArrayList<INDIProperty>(properties.values());
    }

    /**
     * A method that should be implemented when the driver is being destroyed to
     * stop threads, kill sub-drivers, etc. By default it calls
     * <code>removeDevice</code>.
     * 
     * @see #removeDevice
     */
    public void isBeingDestroyed() {
        finishReader();
        removeDevice("Removing " + getName());
    }

    /**
     * Gets the started or not state of the Driver.
     * 
     * @return <code>true</code> if the Driver has already started to listen to
     *         messages. <code>false</code> otherwise.
     */
    public boolean isStarted() {
        return started;
    }

    /**
     * @return a property builder for a blob property.
     */
    public INDIPropertyBuilder<INDIBLOBProperty> newBlobProperty() {
        return newProperty(INDIBLOBProperty.class);
    }

    /**
     * @return a property builder for a light property.
     */
    public INDIPropertyBuilder<INDILightProperty> newLightProperty() {
        return newProperty(INDILightProperty.class);
    }

    /**
     * @return a property builder for a number property.
     */
    public INDIPropertyBuilder<INDINumberProperty> newNumberProperty() {
        return newProperty(INDINumberProperty.class);
    }

    /**
     * @param clazz
     *            the property class.
     * @param <PropertyClass>
     *            the property class.
     * @return a new property builder.
     */
    public <PropertyClass extends INDIProperty<?>> INDIPropertyBuilder<PropertyClass> newProperty(Class<PropertyClass> clazz) {
        return new INDIPropertyBuilder<PropertyClass>(clazz).driver(this);
    }

    /**
     * @return a property builder for a switch property.
     */
    public INDIPropertyBuilder<INDISwitchProperty> newSwitchProperty() {
        return newProperty(INDISwitchProperty.class);
    }

    /**
     * @return a property builder for a text property.
     */
    public INDIPropertyBuilder<INDITextProperty> newTextProperty() {
        return newProperty(INDITextProperty.class);
    }

    /**
     * Called when a new BLOB Vector message has been received from a Client.
     * Must be implemented in Drivers to take care of the new values sent by
     * clients. It will be called with correct Properties and Elements. Any
     * incorrect BLOB Message received will be discarded and this method will
     * not be called.It is not abstract anymore because of the alternative way
     * to attach event handler directly to the properties.
     * 
     * @param property
     *            The BLOB Property asked to change.
     * @param timestamp
     *            The timestamp of the received message
     * @param elementsAndValues
     *            An array of pairs of BLOB Elements and its requested values to
     *            be parsed.
     */
    public void processNewBLOBValue(INDIBLOBProperty property, Date timestamp, INDIBLOBElementAndValue[] elementsAndValues) {
    }

    /**
     * Called when a new Number Vector message has been received from a Client.
     * Must be implemented in Drivers to take care of the new values sent by
     * clients. It will be called with correct Properties and Elements. Any
     * incorrect Number Message received will be discarded and this method will
     * not be called.It is not abstract anymore because of the alternative way
     * to attach event handler directly to the properties.
     * 
     * @param property
     *            The Number Property asked to change.
     * @param timestamp
     *            The timestamp of the received message
     * @param elementsAndValues
     *            An array of pairs of Number Elements and its requested values
     *            to be parsed.
     */
    public void processNewNumberValue(INDINumberProperty property, Date timestamp, INDINumberElementAndValue[] elementsAndValues) {

    }

    /**
     * Called when a new Switch Vector message has been received from a Client.
     * Must be implemented in Drivers to take care of the new values sent by
     * clients. It will be called with correct Properties and Elements. Any
     * incorrect Switch Message received will be discarded and this method will
     * not be called.It is not abstract anymore because of the alternative way
     * to attach event handler directly to the properties.
     * 
     * @param property
     *            The Switch Property asked to change.
     * @param timestamp
     *            The timestamp of the received message
     * @param elementsAndValues
     *            An array of pairs of Switch Elements and its requested values
     *            to be parsed.
     */
    public void processNewSwitchValue(INDISwitchProperty property, Date timestamp, INDISwitchElementAndValue[] elementsAndValues) {
    }

    /**
     * Called when a new Text Vector message has been received from a Client.
     * Must be implemented in Drivers to take care of the new values sent by
     * clients. It will be called with correct Properties and Elements. Any
     * incorrect Text Message received will be discarded and this method will
     * not be called. It is not abstract anymore because of the alternative way
     * to attach event handler directly to the properties.
     * 
     * @param property
     *            The Text Property asked to change.
     * @param timestamp
     *            The timestamp of the received message
     * @param elementsAndValues
     *            An array of pairs of Text Elements and its requested values to
     *            be parsed.
     */
    public void processNewTextValue(INDITextProperty property, Date timestamp, INDITextElementAndValue[] elementsAndValues) {
    }

    @Override
    public void processProtokolMessage(INDIProtocol<?> xml) {
        INDIDriver subd = getSubdriver(xml);

        if (subd != null) {
            subd.processProtokolMessage(xml);
        } else {

            if (xml instanceof GetProperties) {
                processGetProperties((GetProperties) xml);
            } else if (xml instanceof NewTextVector) {
                processNewTextVector((NewTextVector) xml);
            } else if (xml instanceof NewSwitchVector) {
                processNewSwitchVector((NewSwitchVector) xml);
            } else if (xml instanceof NewNumberVector) {
                processNewNumberVector((NewNumberVector) xml);
            } else if (xml instanceof NewBlobVector) {
                processNewBLOBVector((NewBlobVector) xml);
            }
        }
    }

    /**
     * Removes a Property from the Device. A XML message about it will be send
     * to the clients. Drivers must call this method if they want to remove a
     * Property.
     * 
     * @param property
     *            The property to be removed
     */
    public void removeProperty(INDIProperty<?> property) {
        removeProperty(property, null);
    }

    /**
     * Sends all the properties to the clients.
     */
    public void sendAllProperties() {
        List<INDIProperty> props = getPropertiesAsList();

        for (int i = 0; i < props.size(); i++) {
            sendDefXXXVectorMessage(props.get(i), null);
        }

        propertiesRequested();
    }

    /**
     * Starts listening to inputStream. It creates a new Thread to make the
     * readings. Thus, the normal execution of the code is not stopped. This
     * method is not usually called by the Driver itself but the encapsulating
     * class (for example <code>INDIDriverRunner</code>).
     */
    public void startListening() {
        started = true;

        reader = new INDIProtocolReader(this, "driver reader " + connection.getURL());
        reader.start();
    }

    /**
     * Notifies the clients about the property and its values. Drivres must call
     * this method when the values of the Elements of the property are updated
     * in order to notify the clients.
     * 
     * @param property
     *            The Property whose values have change and about which the
     *            clients must be notified.
     * @return true if the update was successful.
     */
    public boolean updateProperty(INDIProperty<?> property) {
        return updateProperty(property, null);
    }

    /**
     * Notifies the clients about the property and its values with an additional
     * <code>message</code>. Drivres must call this method when the values of
     * the Elements of the property are updated in order to notify the clients.
     * 
     * @param property
     *            The Property whose values have change and about which the
     *            clients must be notified.
     * @param includeMinMax
     *            sould the Min Max Step values be included.
     * @param message
     *            The message to be sended to the clients with the udpate
     *            message.
     * @return true if the update was successful.
     */
    public boolean updateProperty(INDIProperty<?> property, boolean includeMinMax, String message) {
        if (properties.containsValue(property)) {
            if (property instanceof INDISwitchProperty) {
                INDISwitchProperty sp = (INDISwitchProperty) property;

                if (!sp.checkCorrectValues()) {
                    LOG.error("Switch (" + property.getName() + ") value not value (not following its rule).");
                    return false;
                }
            }

            SetVector<?> msg = property.getXMLPropertySet(includeMinMax, message);

            sendXML(msg);
            return true;
        } else {
            if (connectionExtension.isActive() && !connectionExtension.isConnected()) {
                // ok, this is normal behavior when a property is set but nobody
                // has yet conneted the driver.
                return true;
            }
            LOG.debug("The Property is not from this driver " + getName() + ". Maybe you forgot to add it? " + property.getName());
            return false;
        }
    }

    /**
     * Notifies the clients about the property and its values with an additional
     * <code>message</code>. Drivres must call this method when the values of
     * the Elements of the property are updated in order to notify the clients.
     * 
     * @param property
     *            The Property whose values have change and about which the
     *            clients must be notified.
     * @param message
     *            The message to be sended to the clients with the udpate
     *            message.
     * @return true if the update was successful.
     */
    public boolean updateProperty(INDIProperty<?> property, String message) {
        return updateProperty(property, false, message);
    }

    /**
     * Adds a new Property to the Device with a <code>message</code> to the
     * client. A message about it will be send to the clients. Drivers must call
     * this method if they want to define a new Property.
     * 
     * @param property
     *            The Property to be added.
     * @param message
     *            The message to be sended to the clients with the definition
     *            message.
     */
    protected void addProperty(INDIProperty<?> property, String message) {
        if (!properties.containsValue(property)) {
            properties.put(property.getName(), property);

            sendDefXXXVectorMessage(property, message);
        }
    }

    /**
     * @return undelaying indi connection.
     */
    protected INDIConnection getINDIConnection() {
        return connection;
    }

    /**
     * Gets a Property of the Driver given its name.
     * 
     * @param propertyName
     *            The name of the Property to be retrieved.
     * @return The Property with <code>propertyName</code> name.
     *         <code>null</code> if there is no property with that name.
     */
    protected INDIProperty getProperty(String propertyName) {
        return properties.get(propertyName);
    }

    /**
     * This method is called when all the properties are requested. A driver
     * with subdrivers may override it to be notified about this kind of
     * requests (and ask the subdriver to also send the properties).
     */
    protected void propertiesRequested() {
    }

    /**
     * Registers a subdriver that may receive messages.
     * 
     * @param driver
     *            The subdriver to register.
     */
    protected void registerSubdriver(INDIDriver driver) {
        subdrivers.add(driver);
    }

    /**
     * Removes the Device from the clients with a <code>message</code>. A XML
     * message about it will be send to the clients. Drivers must call this
     * method if they want to remove the entire device from the clients. It
     * should be used if the Driver is ending.
     * 
     * @param message
     *            The message to be sended to the clients. It can be
     *            <code>null</code> if there is nothing special to say.
     */
    protected void removeDevice(String message) {
        sendDelPropertyMessage(message);
    }

    /**
     * Removes a Property from the Device with a <code>message</code>. A XML
     * message about it will be send to the clients. Drivers must call this
     * method if they want to remove a Property.
     * 
     * @param property
     *            The property to be removed
     * @param message
     *            A message that will be included in the XML message to the
     *            client.
     */
    protected void removeProperty(INDIProperty<?> property, String message) {
        if (properties.containsValue(property)) {
            properties.remove(property.getName());

            sendDelPropertyMessage(property, message);
        }
    }

    /**
     * Unregister a subdriver that may not receive any other message.
     * 
     * @param driver
     *            The subdriver to unregister.
     */
    protected void unregisterSubdriver(INDIDriver driver) {
        subdrivers.remove(driver);
    }

    /**
     * This is the replacement for peeking, only more powerfull, the property
     * specified can be empty (first device of the specified type will fit. a
     * name the device with the specified name will be used. or an device url to
     * a local or external server.
     * 
     * @param element
     *            the element to fill with data from an other driver.
     * @param deviceType
     *            the type of the device we want to observe
     * @param elementName
     *            the element name in that device.
     */
    protected void wireElement(INDIElement element, String deviceType, String elementName) {

    }

    /**
     * Returns the subdriver to which a xml message is sent (if any).
     * <code>null</code> if it is not directed to any subdriver.
     * 
     * @param xml
     *            The XML message
     * @return The subdriver to which the message is directed.
     */
    private INDIDriver getSubdriver(INDIProtocol<?> xml) {
        if (!xml.hasDevice()) {
            return null;
        }

        String deviceName = xml.getDevice();

        return getSubdriver(deviceName);
    }

    /**
     * Gets a device by its name. Returns <code>null</code> if there is no
     * subdriver with that name.
     * 
     * @param name
     *            The name of the subdriver to return
     * @return The subdriver or <code>null</code> if there is no subdrive with
     *         this name.
     */
    private INDIDriver getSubdriver(String name) {
        for (int i = 0; i < subdrivers.size(); i++) {
            INDIDriver d = subdrivers.get(i);

            if (d.getName().compareTo(name) == 0) {
                return d;
            }
        }

        return null;
    }

    /**
     * Processes a &lt;getProperties&gt; message.
     * 
     * @param xml
     *            The XML message
     */
    private void processGetProperties(GetProperties xml) {
        if (xml.getVersion() == null || xml.getVersion().trim().isEmpty()) {
            LOG.error("getProperties: no version specified");

            return;
        }

        if (xml.getName() != null && !xml.getName().trim().isEmpty()) {
            String propertyName = xml.getName();
            INDIProperty<?> p = getProperty(propertyName);

            if (p != null) {
                sendDefXXXVectorMessage(p, null);
            }
        } else { // Send all of them
            sendAllProperties();
        }
    }

    /**
     * Returns an array of Elements and its corresponded requested values from a
     * XML message.
     * 
     * @param property
     *            The property from which to parse the Elements.
     * @param xml
     *            The XML message
     * @return An array of Elements and its corresponding requested values
     */
    private INDIElementAndValue[] processINDIElements(INDIProperty property, NewVector<?> xml) {

        Class<?> oneType;
        if (property instanceof INDITextProperty) {
            oneType = OneText.class;
        } else if (property instanceof INDIBLOBProperty) {
            oneType = OneBlob.class;
        } else if (property instanceof INDINumberProperty) {
            oneType = OneNumber.class;
        } else if (property instanceof INDISwitchProperty) {
            oneType = OneSwitch.class;
        } else {
            return new INDIElementAndValue[0];
        }

        List<INDIElementAndValue> list = new ArrayList<INDIElementAndValue>();

        for (OneElement<?> node : xml.getElements()) {
            if (oneType.isAssignableFrom(node.getClass())) {
                INDIElementAndValue ev = processOneXXX(property, node);

                if (ev != null) {
                    list.add(ev);
                }
            }

        }

        return list.toArray(new INDIElementAndValue[0]);
    }

    /**
     * Parses a &lt;newBLOBVector&gt; XML message.
     * 
     * @param xml
     *            The &lt;newBLOBVector&gt; XML message to be parsed.
     */
    private void processNewBLOBVector(NewBlobVector xml) {
        INDIProperty prop = processNewXXXVector(xml);

        if (prop == null) {
            return;
        }

        if (!(prop instanceof INDIBLOBProperty)) {
            return;
        }

        INDIElementAndValue[] evs = processINDIElements(prop, xml);

        Date timestamp = dateFormat().parseTimestamp(xml.getTimestamp());

        INDIBLOBElementAndValue[] newEvs = new INDIBLOBElementAndValue[evs.length];

        for (int i = 0; i < newEvs.length; i++) {
            newEvs[i] = (INDIBLOBElementAndValue) evs[i];
        }
        IEventHandler handler = prop.getEventHandler();
        if (handler != null) {
            handler.processNewValue(prop, timestamp, newEvs);
        }
        processNewBLOBValue((INDIBLOBProperty) prop, timestamp, newEvs);
    }

    /**
     * Parses a &lt;newNumberVector&gt; XML message.
     * 
     * @param xml
     *            The &lt;newNumberVector&gt; XML message to be parsed.
     */
    private void processNewNumberVector(NewNumberVector xml) {
        INDIProperty prop = processNewXXXVector(xml);

        if (prop == null) {
            return;
        }

        if (!(prop instanceof INDINumberProperty)) {
            return;
        }

        INDIElementAndValue[] evs = processINDIElements(prop, xml);

        Date timestamp = dateFormat().parseTimestamp(xml.getTimestamp());

        INDINumberElementAndValue[] newEvs = new INDINumberElementAndValue[evs.length];

        for (int i = 0; i < newEvs.length; i++) {
            newEvs[i] = (INDINumberElementAndValue) evs[i];
        }

        IEventHandler handler = prop.getEventHandler();
        if (handler != null) {
            handler.processNewValue(prop, timestamp, newEvs);
        }
        processNewNumberValue((INDINumberProperty) prop, timestamp, newEvs);
    }

    /**
     * Parses a &lt;newSwitchVector&gt; XML message. If the switch is the
     * standard CONNECTION property it will analyze the message and call the
     * apropriate methods.
     * 
     * @param xml
     *            The &lt;newSwitchVector&gt; XML message to be parsed.
     */
    private void processNewSwitchVector(NewSwitchVector xml) {
        INDIProperty prop = processNewXXXVector(xml);

        if (prop == null) {
            return;
        }

        if (!(prop instanceof INDISwitchProperty)) {
            return;
        }

        INDIElementAndValue[] evs = processINDIElements(prop, xml);

        Date timestamp = dateFormat().parseTimestamp(xml.getTimestamp());

        INDISwitchElementAndValue[] newEvs = new INDISwitchElementAndValue[evs.length];

        for (int i = 0; i < newEvs.length; i++) {
            newEvs[i] = (INDISwitchElementAndValue) evs[i];
        }

        IEventHandler handler = prop.getEventHandler();
        if (handler != null) {
            handler.processNewValue(prop, timestamp, newEvs);
        }
        processNewSwitchValue((INDISwitchProperty) prop, timestamp, newEvs);

    }

    /**
     * Parses a &lt;newTextVector&gt; XML message.
     * 
     * @param xml
     *            The &lt;newTextVector&gt; XML message to be parsed.
     */
    private void processNewTextVector(NewTextVector xml) {
        INDIProperty prop = processNewXXXVector(xml);

        if (prop == null) {
            return;
        }

        if (!(prop instanceof INDITextProperty)) {
            return;
        }

        INDIElementAndValue[] evs = processINDIElements(prop, xml);

        Date timestamp = dateFormat().parseTimestamp(xml.getTimestamp());

        INDITextElementAndValue[] newEvs = new INDITextElementAndValue[evs.length];

        for (int i = 0; i < newEvs.length; i++) {
            newEvs[i] = (INDITextElementAndValue) evs[i];
        }
        IEventHandler handler = prop.getEventHandler();
        if (handler != null) {
            handler.processNewValue(prop, timestamp, newEvs);
        }
        processNewTextValue((INDITextProperty) prop, timestamp, newEvs);
    }

    /**
     * Processes a &lt;newXXXVector&gt; message.
     * 
     * @param xml
     *            The XML message
     * @return The INDI Property to which the <code>xml</code> message refers.
     */
    private INDIProperty processNewXXXVector(NewVector<?> xml) {
        if (!xml.hasDevice() || !xml.hasName()) {
            return null;
        }

        String devName = xml.getDevice();
        String propName = xml.getName();

        if (devName.compareTo(getName()) != 0) { // If the message is not for
                                                 // this device
            return null;
        }

        INDIProperty prop = getProperty(propName);

        return prop;
    }

    /**
     * Processes a XML &lt;oneXXX&gt; message for a property.
     * 
     * @param property
     *            The property from which to parse the Element.
     * @param xml
     *            The &lt;oneXXX&gt; XML message
     * @return A Element and its corresponding requested value
     */
    private INDIElementAndValue processOneXXX(INDIProperty property, OneElement<?> xml) {
        if (!xml.hasName()) {
            return null;
        }

        String elName = xml.getName();

        INDIElement el = property.getElement(elName);

        if (el == null) {
            return null;
        }

        Object value;

        try {
            value = el.parseOneValue(xml);
        } catch (IllegalArgumentException e) {
            return null;
        }

        if (el instanceof INDITextElement) {
            return new INDITextElementAndValue((INDITextElement) el, (String) value);
        } else if (el instanceof INDISwitchElement) {
            return new INDISwitchElementAndValue((INDISwitchElement) el, (SwitchStatus) value);
        } else if (el instanceof INDINumberElement) {
            return new INDINumberElementAndValue((INDINumberElement) el, (Double) value);
        } else if (el instanceof INDIBLOBElement) {
            return new INDIBLOBElementAndValue((INDIBLOBElement) el, (INDIBLOBValue) value);
        }

        return null;
    }

    /**
     * Notifies the clients about a new property with a <code>message</code>.
     * The <code>message</code> can be <code>null</code> if there is nothing to
     * special to say.
     * 
     * @param property
     *            The property that will be notified.
     * @param message
     *            The extra message text for the client.
     */
    private void sendDefXXXVectorMessage(INDIProperty property, String message) {
        sendXML(property.getXMLPropertyDefinition(message));
    }

    /**
     * Sends a message to the client to remove a Property with a
     * <code>message</code>.
     * 
     * @param property
     *            The property that is being removed.
     * @param message
     *            The optional message (can be <code>null</code>).
     */
    private void sendDelPropertyMessage(INDIProperty property, String message) {
        sendXML(new DelProperty().setDevice(this.getName()).setName(property.getName()).setTimestamp(dateFormat().getCurrentTimestamp()).setMessage(message));
    }

    /**
     * Sends a mesage to the client to remove the entire device.
     * 
     * @param message
     *            A optional message (can be <code>null</code>).
     */
    private void sendDelPropertyMessage(String message) {
        sendXML(new DelProperty().setDevice(this.getName()).setTimestamp(dateFormat().getCurrentTimestamp()).setMessage(message));
    }

    /**
     * Sends a XML message to the clients.
     * 
     * @param xml
     *            The message to be sended.
     */
    private void sendXML(INDIProtocol<?> xml) {
        try {
            connection.getINDIOutputStream().writeObject(xml);
        } catch (IOException e) {
            throw new IllegalStateException("could not write to output stream", e);
        }
    }

}
