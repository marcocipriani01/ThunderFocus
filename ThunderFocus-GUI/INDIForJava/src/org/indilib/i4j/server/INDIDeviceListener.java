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

import org.indilib.i4j.Constants.BLOBEnables;
import org.indilib.i4j.INDIProtocolParser;
import org.indilib.i4j.protocol.GetProperties;
import org.indilib.i4j.protocol.INDIProtocol;

import java.util.ArrayList;
import java.util.List;

/**
 * A class that represents a listener to devices. It is used to include both
 * usual Clients and Devices, as Drivers can also snoop Properties from other
 * Devices according to the INDI protocol.
 * 
 * @author S. Alonso (Zerjillo) [zerjioi at ugr.es]
 * @author Richard van Nieuwenhoven
 */
public abstract class INDIDeviceListener implements INDIProtocolParser {

    /**
     * A list of BLOBEnable rules.
     */
    private List<DevicePropertyBLOBEnableTuple> bLOBEnableRules;

    /**
     * A list of devices that are listened.
     */
    private List<DevicePropertyBLOBEnableTuple> devicesToListen;

    /**
     * Determines if the object listens to all devices.
     */
    private boolean listenToAllDevices;

    /**
     * A list of properties that are listened.
     */
    private List<DevicePropertyBLOBEnableTuple> propertiesToListen;

    /**
     * Constructs a new <code>INDIDeviceListener</code>.
     */
    protected INDIDeviceListener() {
        listenToAllDevices = false;

        devicesToListen = new ArrayList<DevicePropertyBLOBEnableTuple>();
        propertiesToListen = new ArrayList<DevicePropertyBLOBEnableTuple>();
        bLOBEnableRules = new ArrayList<DevicePropertyBLOBEnableTuple>();
    }

    /**
     * @return <code>true</code> if the listener listens to all the devices.
     *         <code>false</code> otherwise.
     */
    public boolean listensToAllDevices() {
        return listenToAllDevices;
    }

    /**
     * Add a new BLOB Enable rule for a whole Device.
     * 
     * @param deviceName
     *            The Device name
     * @param enable
     *            The rule
     */
    protected void addBLOBEnableRule(String deviceName, BLOBEnables enable) {
        DevicePropertyBLOBEnableTuple aux = getBLOBEnableRule(deviceName);

        if (aux != null) {
            bLOBEnableRules.remove(aux);
        }

        bLOBEnableRules.add(new DevicePropertyBLOBEnableTuple(deviceName, enable));
    }

    /**
     * Add a new BLOB Enable rule for a particular BLOB Property.
     * 
     * @param deviceName
     *            The Device name
     * @param propertyName
     *            The Property name
     * @param enable
     *            The rule
     */
    protected void addBLOBEnableRule(String deviceName, String propertyName, BLOBEnables enable) {
        DevicePropertyBLOBEnableTuple aux = getBLOBEnableRule(deviceName, propertyName);

        if (aux != null) {
            bLOBEnableRules.remove(aux);
        }

        bLOBEnableRules.add(new DevicePropertyBLOBEnableTuple(deviceName, propertyName, enable));
    }

    /**
     * Adds a new Device to be listened.
     * 
     * @param deviceName
     *            The Device name to be listened.
     */
    protected void addDeviceToListen(String deviceName) {
        devicesToListen.add(new DevicePropertyBLOBEnableTuple(deviceName));
    }

    /**
     * Adds a new Property to be listened.
     * 
     * @param deviceName
     *            The Device name owner of the Property
     * @param propertyName
     *            The Property name to be listened.
     */
    protected void addPropertyToListen(String deviceName, String propertyName) {
        propertiesToListen.add(new DevicePropertyBLOBEnableTuple(deviceName, propertyName));
    }

    /**
     * Gets information about if non BLOBs updates should be sended according to
     * the BLOB Enable rules.
     * 
     * @param deviceName
     *            The Device name
     * @return <code>true</code> if non BLOBs are accepted. <code>false</code>
     *         otherwise.
     */
    protected boolean areNonBLOBsAccepted(String deviceName) {
        DevicePropertyBLOBEnableTuple aux = getBLOBEnableRule(deviceName);

        if (aux == null) {
            return true;
        }

        if (aux.getBLOBEnable() == BLOBEnables.ONLY) {
            return false;
        }

        return true;
    }

    /**
     * Gets information about if BLOB updates should be sended according to the
     * BLOB Enable rules.
     * 
     * @param deviceName
     *            The Device name
     * @param propertyName
     *            The Property name
     * @return <code>true</code> if the BLOB is accepted. <code>false</code>
     *         otherwise.
     */
    protected boolean isBLOBAccepted(String deviceName, String propertyName) {
        DevicePropertyBLOBEnableTuple aux = getBLOBEnableRule(deviceName, propertyName);

        if (aux != null) {
            return aux.getBLOBEnable() != BLOBEnables.NEVER;
        }

        aux = getBLOBEnableRule(deviceName);

        if (aux == null) {
            return false;
        }

        if (aux.getBLOBEnable() == BLOBEnables.NEVER) {
            return false;
        }

        return true;
    }

    /**
     * Determines if the listener listens to a Device.
     * 
     * @param deviceName
     *            The Device name to check.
     * @return <code>true</code> if the listener listens to the Device.
     *         <code>false</code> otherwise.
     */
    protected boolean listensToDevice(String deviceName) {
        if (listenToAllDevices) {
            return true;
        }

        if (listensToParticularDevice(deviceName)) {
            return true;
        }

        return false;
    }

    /**
     * Determines if the listener listens to a Property.
     * 
     * @param deviceName
     *            The Device name to which the Property belongs.
     * @param propertyName
     *            The Property name to check.
     * @return <code>true</code> if the listener listens to the Property.
     *         <code>false</code> otherwise.
     */
    protected boolean listensToProperty(String deviceName, String propertyName) {
        if (listensToDevice(deviceName)) {
            return true;
        }

        if (listensToParticularProperty(deviceName, propertyName)) {
            return true;
        }

        return false;
    }

    /**
     * Determines if the listener listens to specifically one Property of a
     * Device.
     * 
     * @param deviceName
     *            The Device name to check.
     * @return <code>true</code> if the listener listens specifically to any
     *         Property of the Device. <code>false</code> otherwise.
     */
    protected boolean listensToSingleProperty(String deviceName) {
        for (DevicePropertyBLOBEnableTuple propertyToListen : propertiesToListen) {
            if (propertyToListen.isDevice(deviceName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Processes the <code>getProperties</code> XML message.
     * 
     * @param xml
     *            The <code>getProperties</code> XML message
     */
    protected void processGetProperties(GetProperties xml) {
        if (!xml.hasDevice()) {
            setListenToAllDevices(true);
        } else {
            if (!xml.hasName()) {
                addDeviceToListen(xml.getDevice());
            } else {
                addPropertyToListen(xml.getDevice(), xml.getName());
            }
        }
    }

    /**
     * Sends a String (usually containing some XML) to the listener.
     * 
     * @param message
     *            The message to be sent.
     */
    protected abstract void sendXMLMessage(INDIProtocol<?> message);

    /**
     * Sets the listenToAllDevices flag.
     * 
     * @param listenToAllDevices
     *            The new value of the flag.
     */
    protected void setListenToAllDevices(boolean listenToAllDevices) {
        this.listenToAllDevices = listenToAllDevices;
    }

    /**
     * Gets the BLOB Enable rule for a Device (if it exists).
     * 
     * @param deviceName
     *            The Device name.
     * @return The BLOB Enable rule.
     */
    private DevicePropertyBLOBEnableTuple getBLOBEnableRule(String deviceName) {
        return getBLOBEnableRule(deviceName, null);
    }

    /**
     * Gets the BLOB Enable rule for a Property (if it exists).
     * 
     * @param deviceName
     *            The Device name.
     * @param propertyName
     *            The Property name.
     * @return The BLOB Enable rule.
     */
    private DevicePropertyBLOBEnableTuple getBLOBEnableRule(String deviceName, String propertyName) {
        for (DevicePropertyBLOBEnableTuple aux : bLOBEnableRules) {
            if (aux.isProperty(deviceName, propertyName)) {
                return aux;
            }
        }
        return null;
    }

    /**
     * Checks if it is specifically listening to a particular Device.
     * 
     * @param deviceName
     *            The Device name.
     * @return <code>true</code> if the listener specifically listens to the
     *         Device. <code>false</code> otherwise.
     */
    private boolean listensToParticularDevice(String deviceName) {
        for (DevicePropertyBLOBEnableTuple device : devicesToListen) {
            if (device.isDevice(deviceName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if it is specifically listening to a particular Property of a
     * Device.
     * 
     * @param deviceName
     *            The Device name.
     * @param propertyName
     *            the property to listen to.
     * @return <code>true</code> if the listener specifically listens to the
     *         Property of a Device. <code>false</code> otherwise.
     */
    private boolean listensToParticularProperty(String deviceName, String propertyName) {
        for (DevicePropertyBLOBEnableTuple propertyToListen : propertiesToListen) {
            if (propertyToListen.isProperty(deviceName, propertyName)) {
                return true;
            }
        }
        return false;
    }
}
