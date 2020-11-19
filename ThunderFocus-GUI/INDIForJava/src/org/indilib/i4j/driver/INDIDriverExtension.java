package org.indilib.i4j.driver;

/*
 * #%L
 * INDI for Java Driver Library
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

import org.indilib.i4j.driver.util.INDIPropertyInjector;

/**
 * When you want to encapsulate a few properties and or elements together with
 * the connected handling code. For example to reduce the complexity of a
 * driver, or to write code only once when properties repeat them self
 * (Especially when the functionality behind the properties is also repetitive.
 * Another use is to make it more easy to disable and enable hole parts of the
 * driver at once.
 * 
 * @author Richard van Nieuwenhoven
 * @param <Driver>
 *            the driver class this extension is written for
 */
public abstract class INDIDriverExtension<Driver extends INDIDriver> {

    /**
     * the driver instance itself, it is free to access in the extension
     * subclassses.
     */
    protected final Driver driver;

    /**
     * Constructor of the extension, mostly only called by the injectors, be
     * very careful when you do this manually.
     * 
     * @param driver
     *            the driver instance.
     */
    public INDIDriverExtension(Driver driver) {
        this.driver = driver;
        if (isActive()) {
            INDIPropertyInjector.initialize(this.driver, this);
        }
    }

    /**
     * @return true if the driver extension is activated, if this property is
     *         false no properties will be instantiated.
     */
    public boolean isActive() {
        return true;
    }

    /**
     * the driver was connected, add the properties to the driver.
     */
    public void connect() {
    }

    /**
     * The driver was disconnected so remove the properties.
     */
    public void disconnect() {
    }

    /**
     * Notifies the clients about the property and its values. Extensions must
     * call this method when the values of the Elements of the property are
     * updated in order to notify the clients.
     * 
     * @param property
     *            The Property whose values have change and about which the
     *            clients must be notified.
     * @return true im successful
     */
    public boolean updateProperty(INDIProperty<?> property) {
        return driver.updateProperty(property);
    }

    /**
     * Notifies the clients about the property and its values with an additional
     * <code>message</code>. Extensions must call this method when the values of
     * the Elements of the property are updated in order to notify the clients.
     * 
     * @param property
     *            The Property whose values have change and about which the
     *            clients must be notified.
     * @param message
     *            The message to be sended to the clients with the udpate
     *            message.
     * @return true im successful
     */
    public boolean updateProperty(INDIProperty<?> property, String message) {
        return driver.updateProperty(property, message);
    }

    /**
     * Notifies the clients about the property and its values with an additional
     * <code>message</code>. Extensions must call this method when the values of
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
     * @return true im successful
     */
    protected boolean updateProperty(INDIProperty<?> property, boolean includeMinMax, String message) {
        return driver.updateProperty(property, includeMinMax, message);
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
        driver.addProperty(property, null);
    }

    /**
     * Adds a new Property to the Device with a <code>message</code> to the
     * client. A message about it will be send to the clients. Extensions must
     * call this method if they want to define a new Property.
     * 
     * @param property
     *            The Property to be added.
     * @param message
     *            The message to be sended to the clients with the definition
     *            message.
     */
    protected void addProperty(INDIProperty<?> property, String message) {
        driver.addProperty(property, message);
    }

    /**
     * Removes a Property from the Device. A XML message about it will be send
     * to the clients. Extensions must call this method if they want to remove a
     * Property.
     * 
     * @param property
     *            The property to be removed
     */
    public void removeProperty(INDIProperty<?> property) {
        driver.removeProperty(property, null);
    }

    /**
     * Removes a Property from the Device with a <code>message</code>. A XML
     * message about it will be send to the clients. Extensions must call this
     * method if they want to remove a Property.
     * 
     * @param property
     *            The property to be removed
     * @param message
     *            A message that will be included in the XML message to the
     *            client.
     */
    protected void removeProperty(INDIProperty<?> property, String message) {
        driver.removeProperty(property, message);
    }

}
