package org.indilib.i4j.driver;

/*
 * #%L INDI for Java Driver Library %% Copyright (C) 2012 - 2014 indiforjava %%
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version. This program is distributed in the hope that it will be
 * useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Lesser Public License for more details. You should have received a copy of
 * the GNU General Lesser Public License along with this program. If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>. #L%
 */

import org.indilib.i4j.Constants;
import org.indilib.i4j.Constants.PropertyPermissions;
import org.indilib.i4j.Constants.PropertyStates;
import org.indilib.i4j.FileUtils;
import org.indilib.i4j.INDIException;
import org.indilib.i4j.driver.event.IEventHandler;
import org.indilib.i4j.driver.util.INDIElementBuilder;
import org.indilib.i4j.driver.util.INDIPropertyBuilder;
import org.indilib.i4j.protocol.DefVector;
import org.indilib.i4j.protocol.INDIProtocol;
import org.indilib.i4j.protocol.SetVector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

import static org.indilib.i4j.INDIDateFormat.dateFormat;

/**
 * A class representing a INDI Property. The subclasses
 * <code>INDIBLOBProperty</code>, <code>INDILightProperty</code>,
 * <code>INDINumberProperty</code>, <code>INDISwitchProperty</code> and
 * <code>INDITextProperty</code> define the basic Properties that a INDI Drivers
 * may contain according to the INDI protocol.
 * 
 * @param <Element>
 *            der element type in this property.
 * @author S. Alonso (Zerjillo) [zerjioi at ugr.es]
 */
public abstract class INDIProperty<Element extends INDIElement> implements Serializable, Iterable<Element> {

    /**
     * Serialization id.
     */
    private static final long serialVersionUID = 7974207228775777212L;

    /**
     * the log to write messages to.
     */
    private static final Logger LOG = LoggerFactory.getLogger(INDIProperty.class);

    /**
     * The Driver to which this property belongs.
     */
    private transient INDIDriver driver;

    /**
     * This Property name.
     */
    private String name;

    /**
     * This Property label.
     */
    private String label;

    /**
     * The group to which this Property might be assigned.
     */
    private String group;

    /**
     * The current state of this Property.
     */
    private PropertyStates state;

    /**
     * The permission of this Property.
     */
    private PropertyPermissions permission;

    /**
     * The timeout of this Property.
     */
    private int timeout;

    /**
     * A list of Elements for this Property.
     */
    private LinkedHashMap<String, Element> elements;

    /**
     * <code>true</code> if property has completely init (sent to any client).
     */
    private boolean isInit;

    /**
     * To save / retrieve properties from this directory. It will be stored
     * inside the default I4J directory.
     */
    private static final String PROPERTIES_DIR_NAME = "properties";

    /**
     * It marks if the property should be saved each time that it is changed.
     */
    private boolean saveable;

    /**
     * Event handlere for simpler event definitions.
     */
    private transient IEventHandler<? extends INDIProperty<Element>, Element, ?> eventHandler;

    /**
     * Constructs an instance of a <code>INDIProperty</code>. Called by its
     * sub-classes. Using the settings from the builder.
     * 
     * @param builder
     *            the builder with all the settings.
     */
    protected INDIProperty(INDIPropertyBuilder<?> builder) {
        this.driver = builder.driver();
        this.name = builder.name();
        if (this.name.isEmpty()) {
            throw new IllegalArgumentException("No name for the Property");
        }
        this.label = builder.label();
        if (this.label.isEmpty()) {
            this.label = this.name;
        }
        this.group = builder.group();
        this.state = builder.state();
        this.permission = builder.permission();
        this.timeout = builder.timeout();
        this.saveable = builder.saveable();
        this.elements = new LinkedHashMap<String, Element>();
        isInit = false;
    }

    /**
     * Set the property to be saveable. Should only be called by property
     * factories.
     * 
     * @param saveable
     *            new value.
     */
    public void setSaveable(boolean saveable) {
        this.saveable = saveable;
    }

    /**
     * Sets the Driver of the Property.
     * 
     * @param driver
     *            new value.
     */
    private void setDriver(INDIDriver driver) {
        this.driver = driver;
    }

    /**
     * Gets the Driver of the Property.
     * 
     * @return The Driver of the Property.
     */
    public INDIDriver getDriver() {
        return driver;
    }

    /**
     * Sets the State of the Property.
     * 
     * @param newState
     *            The new State of the Property.
     */
    public void setState(PropertyStates newState) {
        this.state = newState;
    }

    /**
     * Sets the timeout of the property.
     * 
     * @param newTimeout
     *            The new timeout of the Property. if <code>timeout</code> is
     *            less than 0.
     */
    public void setTimeout(int newTimeout) {
        if (timeout < 0) {
            throw new IllegalArgumentException("Illegal timeout for the Property");
        }

        this.timeout = newTimeout;
    }

    /**
     * Gets the Group to which this property might be assigned.
     * 
     * @return the group to which this property might be assigned.
     */
    public String getGroup() {
        return group;
    }

    /**
     * Gets the timeout for this Property.
     * 
     * @return the timeout for this Property.
     */
    public int getTimeout() {
        return timeout;
    }

    /**
     * Gets the label for this Property.
     * 
     * @return the label for this Property.
     */
    public String getLabel() {
        return label;
    }

    /**
     * Gets the name of this Property.
     * 
     * @return the name of this Property
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the number of Elements in this Property.
     * 
     * @return the number of Elements in this Property.
     */
    public int getElementCount() {
        return elements.size();
    }

    /**
     * Gets the Permission of this Property.
     * 
     * @return the Permission of this Property.
     */
    public PropertyPermissions getPermission() {
        return permission;
    }

    /**
     * Set the permission of a property.
     * 
     * @param permission
     *            the new value.
     */
    public void setPermission(PropertyPermissions permission) {
        this.permission = permission;
    }

    /**
     * Gets the State of this Property.
     * 
     * @return the State of this Property.
     */
    public PropertyStates getState() {
        return state;
    }

    /**
     * Adds a new Element to this Property (if it there is no other Element with
     * the same name and it is already being init [not sended to clients]).
     * Drivers must not call this method directly as it is called when
     * constructing the Element.
     * 
     * @param element
     *            the Element to be added.
     */
    protected void addElement(Element element) {
        if (this instanceof INDITextProperty && !(element instanceof INDITextElement)) {
            throw new IllegalArgumentException("Text Element cannot be added to Text Property");
        }

        if (this instanceof INDISwitchProperty && !(element instanceof INDISwitchElement)) {
            throw new IllegalArgumentException("Switch Element cannot be added to Switch Property");
        }

        if (this instanceof INDINumberProperty && !(element instanceof INDINumberElement)) {
            throw new IllegalArgumentException("Number Element cannot be added to Number Property");
        }

        if (this instanceof INDILightProperty && !(element instanceof INDILightElement)) {
            throw new IllegalArgumentException("Light Element cannot be added to Light Property");
        }

        if (this instanceof INDIBLOBProperty && !(element instanceof INDIBLOBElement)) {
            throw new IllegalArgumentException("BLOB Element cannot be added to BLOB Property");
        }

        // We still can add new properties and it does not jet exist.
        if (!isInit && !elements.containsKey(element.getName())) {
            elements.put(element.getName(), element);
        }
    }

    /**
     * Gets a particular Element of this Property by its name.
     * 
     * @param elementName
     *            The name of the Element to be returned
     * @return The Element of this Property with the given <code>name</code>.
     *         <code>null</code> if there is no Element with that
     *         <code>name</code>.
     */
    public Element getElement(String elementName) {
        return elements.get(elementName);
    }

    /**
     * Gets a <code>List</code> with all the Elements of this Property.
     * 
     * @return the <code>List</code> of Elements belonging to this Property.
     */
    public List<Element> getElementsAsList() {
        return new ArrayList<Element>(elements.values());
    }

    /**
     * Gets a <code>List</code> with all the Elements of this Property.
     * 
     * @return the <code>List</code> of Elements belonging to this Property.
     */
    public Element firstElement() {
        return elements.values().iterator().next();
    }

    /**
     * Gets the names of the Elements of this Property.
     * 
     * @return the names of the Elements of this Property.
     */
    public String[] getElementNames() {
        String[] names = new String[size()];
        int index = 0;
        for (Element l : this) {
            names[index] = l.getName();
        }
        return names;
    }

    /**
     * Returns a String with the name of the Property, its state and its
     * elements and values.
     * 
     * @return a String representation of the property and its values.
     */
    public String getNameStateAndValuesAsString() {
        StringBuffer aux = new StringBuffer(getName());
        aux.append(" - ");
        aux.append(getState());
        aux.append("\n");
        for (Element element : this) {
            aux.append("  ");
            aux.append(element.getNameAndValueAsString());
            aux.append("\n");
        }
        return aux.toString();
    }

    /**
     * Gets the XML code to define the property. Should not usually be called by
     * the Drivers.
     * 
     * @return The XML code to define the property.
     */
    protected INDIProtocol<?> getXMLPropertyDefinition() {
        return getXMLPropertyDefinition(null);
    }

    /**
     * Gets the XML code to define the property with a <code>message</code>.
     * Should not usually be called by the Drivers.
     * 
     * @param message
     *            An message to be sent to the client when defining the
     *            property.
     * @return The XML code to define the property.
     */
    protected DefVector<?> getXMLPropertyDefinition(String message) {
        DefVector<?> xml = getXMLPropertyDefinitionInit();
        xml.setDevice(getDriver().getName());
        xml.setName(getName());
        xml.setLabel(getLabel());
        xml.setGroup(getGroup());
        xml.setState(Constants.getPropertyStateAsString(getState()));
        xml.setPerm(Constants.getPropertyPermissionAsString(getPermission()));
        xml.setTimeout(Integer.toString(getTimeout()));
        xml.setTimestamp(dateFormat().getCurrentTimestamp());
        xml.setMessage(message);
        for (Element element : this) {
            xml.getElements().add(element.getXMLDefElement());
        }
        // The property now is initialized. No further changes allowed
        isInit = true;
        return xml;
    }

    /**
     * Gets the XML code to set the values of the property. Should not usually
     * be called by the Drivers.
     * 
     * @return The XML code to set the values of the property.
     */
    protected SetVector<?> getXMLPropertySet() {
        return getXMLPropertySet(false, null);
    }

    /**
     * Gets the XML code to set the values of the property with a
     * <code>message</code>. Should not usually be called by the Drivers.
     * 
     * @param includeMinMax
     *            include the min and max value in the xml.
     * @param message
     *            An message to be sent to the client when setting the values of
     *            the property.
     * @return The XML code to set the values of the property.
     */
    protected SetVector<?> getXMLPropertySet(boolean includeMinMax, String message) {
        if (saveable) {
            try {
                saveToFile();
            } catch (IOException e) {
                LOG.error("could not save the property to a file", e);
            }
        }
        SetVector<?> result = getXMLPropertySetInit();
        result.setDevice(getDriver().getName());
        result.setName(getName());
        result.setState(Constants.getPropertyStateAsString(getState()));
        result.setTimeout(Integer.toString(getTimeout()));
        result.setTimestamp(dateFormat().getCurrentTimestamp());
        if (message == null) {
            result.setMessage(message);
        }
        for (Element element : this) {
            result.getElements().add(element.getXMLOneElement(includeMinMax));
        }
        return result;
    }

    /**
     * Gets the opening XML Element &lt;defXXXVector&gt; for this Property.
     * 
     * @return the opening XML Element &lt;defXXXVector&gt; for this Property.
     */
    protected abstract DefVector<?> getXMLPropertyDefinitionInit();

    /**
     * Gets the opening XML Element &lt;setXXXVector&gt; for this Property.
     * 
     * @return the opening XML Element &lt;setXXXVector&gt; for this Property.
     */
    protected abstract SetVector<?> getXMLPropertySetInit();

    /**
     * Saves the property and its elements to a file. Ideal to later restore it
     * on subsecuent executions of the driver.
     * 
     * @throws IOException
     *             if the property could not be saved.
     */
    private void saveToFile() throws IOException {
        File i4jDir = FileUtils.getI4JBaseDirectory();

        File propertiesDir = new File(i4jDir, PROPERTIES_DIR_NAME);

        if (!propertiesDir.exists() && !propertiesDir.mkdir()) {
            LOG.error("could not create directory " + propertiesDir.getAbsolutePath());
        }

        if (propertiesDir.exists() && propertiesDir.isDirectory()) {
            File file = new File(propertiesDir, getPropertyNameForFile());
            FileOutputStream fos = new FileOutputStream(file);

            ObjectOutputStream oos = new ObjectOutputStream(fos);

            oos.writeObject(this);

            oos.close();
        }
    }

    /**
     * Convenience method to get the name of the file for a particular property
     * to be saved / retrieved.
     * 
     * @return A name for a file in which a property will be saved / restored
     */
    private String getPropertyNameForFile() {
        return removeCharacters(getDriver().getName()) + "_" + getName() + ".prop";
    }

    /**
     * Convenience method to get the name of the file for a particular property
     * to be saved / retrieved.
     * 
     * @param driver
     *            The driver of the property
     * @param propertyName
     *            The name of the property
     * @return A name for a file in which a property will be saved / restored
     */
    private static String getPropertyNameForFile(INDIDriver driver, String propertyName) {
        return removeCharacters(driver.getName()) + "_" + propertyName + ".prop";
    }

    /**
     * Removes all non letters / numbers from a String.
     * 
     * @param str
     *            An input string
     * @return The same <code>str</code>string without any non letter / numbers.
     */
    private static String removeCharacters(String str) {
        return str.replaceAll("[^a-zA-Z0-9]", "");
    }

    /**
     * Loads a property from a file.
     * 
     * @param driver
     *            The driver which will include the property
     * @param propertyName
     *            The name of the property to load
     * @return A property loaded from a file
     * @throws INDIException
     *             If there is some problem loading it (for example if the file
     *             does not exist)
     */
    public static INDIProperty<?> loadFromFile(INDIDriver driver, String propertyName) throws INDIException {
        File i4jDir = FileUtils.getI4JBaseDirectory();
        File propertiesDir = new File(i4jDir, PROPERTIES_DIR_NAME);
        File file = new File(propertiesDir, getPropertyNameForFile(driver, propertyName));
        INDIProperty<?> prop = null;
        if (file.exists()) {
            try {
                FileInputStream fis = new FileInputStream(file);
                ObjectInputStream ois = new ObjectInputStream(fis);

                prop = (INDIProperty<?>) ois.readObject();

                ois.close();
            } catch (InvalidClassException ex) {
                LOG.warn("property could not be loaded from file, because it is not compatible to the current version (reverting to default values) : " + file.getName());
                return null;
            } catch (ClassNotFoundException ex) {
                throw new INDIException("Problem when loading a property from file " + file.getName() + " - ClassNotFoundException");
            } catch (IOException ex) {
                LOG.warn("property could not be loaded from file, because it is not compatible to the current version (reverting to default values) : " + file.getName());
                return null;
            }
            prop.setDriver(driver);
        }
        return prop;
    }

    /**
     * Sets all the values in the Elements of and array of elements and values.
     * Please note that this method does not make any assumtion about the values
     * that are going to be set, so an <code>IllegalArgumentException</code> or
     * an <code>INDIValueException</code> might be thrown.
     * 
     * @param elementsAndValues
     *            The array of elements and values
     */
    public void setValues(INDIElementAndValue<Element, ?>[] elementsAndValues) {
        for (INDIElementAndValue<Element, ?> elementsAndValue : elementsAndValues) {
            INDIElement element = elementsAndValue.getElement();

            element.setValue(elementsAndValue.getValue());
        }
    }

    /**
     * set the event handler that will be invoked on a client change.
     * 
     * @param eventHandler
     *            that will be triggerd on the change.
     */
    public final void setEventHandler(IEventHandler<? extends INDIProperty<Element>, Element, ?> eventHandler) {
        this.eventHandler = eventHandler;
    }

    /**
     * @return event handler for this property.
     */
    public final IEventHandler<? extends INDIProperty<Element>, Element, ?> getEventHandler() {
        return eventHandler;
    }

    /**
     * @return a new Element builder.
     */
    public INDIElementBuilder<Element> newElement() {
        return new INDIElementBuilder<Element>(elementClass(), this);
    }

    /**
     * @return the class of the Element.
     */
    protected abstract Class<Element> elementClass();

    @Override
    public Iterator<Element> iterator() {
        return elements.values().iterator();
    }

    /**
     * @return the number of elements in this property.
     */
    public int size() {
        return elements.size();
    }
}
