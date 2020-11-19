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

import org.indilib.i4j.Constants.BLOBEnables;

/**
 * A class that represents a tuple of Device and Property names and a
 * BLOBEnable.
 * 
 * @author S. Alonso (Zerjillo) [zerjioi at ugr.es]
 */
public class DevicePropertyBLOBEnableTuple {

    /**
     * The Device name.
     */
    private String device;

    /**
     * The BLOB enable.
     */
    private BLOBEnables enable;

    /**
     * The Property name.
     */
    private String property;

    /**
     * Constructs a new DevicePropertyBLOBEnableTuple with a Property name and
     * BLOB Enable set to <code>null</code>.
     * 
     * @param device
     *            The Device name.
     */
    protected DevicePropertyBLOBEnableTuple(String device) {
        this.device = device;
        property = null;
        enable = null;
    }

    /**
     * Constructs a new DevicePropertyBLOBEnableTuple.
     * 
     * @param device
     *            The Device name.
     * @param enable
     *            The BLOB Enable.
     */
    protected DevicePropertyBLOBEnableTuple(String device, BLOBEnables enable) {
        this.device = device;
        property = null;
        this.enable = enable;
    }

    /**
     * Constructs a new DevicePropertyBLOBEnableTuple with a BLOB Enable set to
     * <code>null</code>.
     * 
     * @param device
     *            The Device name.
     * @param property
     *            The Property name.
     */
    protected DevicePropertyBLOBEnableTuple(String device, String property) {
        this.device = device;
        this.property = property;
        enable = null;
    }

    /**
     * Constructs a new DevicePropertyBLOBEnableTuple.
     * 
     * @param device
     *            The Device name.
     * @param property
     *            The Property name.
     * @param enable
     *            The BLOB Enable.
     */
    protected DevicePropertyBLOBEnableTuple(String device, String property, BLOBEnables enable) {
        this.device = device;
        this.property = property;
        this.enable = enable;
    }

    /**
     * Gets the BLOB Enable.
     * 
     * @return the BLOB Enable.
     */
    protected BLOBEnables getBLOBEnable() {
        return enable;
    }

    /**
     * Gets the Device name.
     * 
     * @return the Device name.
     */
    protected String getDevice() {
        return device;
    }

    /**
     * Gets the Property name.
     * 
     * @return the Device name.
     */
    protected String getProperty() {
        return property;
    }

    /**
     * Checks if the Device has a particular name.
     * 
     * @param deviceName
     *            The name of the Device to check.
     * @return <code>true</code> if the name of the Device coincides.
     *         <code>false</code> otherwise.
     */
    protected boolean isDevice(String deviceName) {
        if (device.equals(deviceName)) {
            return true;
        }

        return false;
    }

    /**
     * Checks if the Device has a particular name and the Property has a
     * particular name.
     * 
     * @param deviceName
     *            The name of the Device to check.
     * @param propertyName
     *            The name of the Property to check.
     * @return <code>true</code> if the name of the Device coincides and the
     *         name of the Property coincides. <code>false</code> otherwise.
     */
    protected boolean isProperty(String deviceName, String propertyName) {
        if (device.equals(deviceName)) {
            if (propertyName == null && property == null) {
                return true;
            } else if (propertyName == null && property != null) {
                return false;
            } else if (propertyName != null && property == null) {
                return false;
            } else if (property.equals(propertyName)) {
                return true;
            }
        }
        return false;
    }
}
