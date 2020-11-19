package org.indilib.i4j.driver.focuser;

/*
 * #%L
 * INDI for Java Abstract Focuser Driver
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

import org.indilib.i4j.Constants.PropertyStates;
import org.indilib.i4j.driver.*;
import org.indilib.i4j.properties.INDIStandardProperty;
import org.indilib.i4j.protocol.api.INDIConnection;

import java.util.Date;

import static org.indilib.i4j.properties.INDIStandardProperty.ABS_FOCUS_POSITION;

/**
 * A class representing a Focuser Driver in the INDI Protocol. INDI Focuser
 * Drivers should extend this class. It is in charge of handling the following
 * properties for Focusers:
 * <ul>
 * <li>FOCUS_SPEED - FOCUS_SPEED_VALUE (number)</li>
 * <li>ABS_FOCUS_POSITION - FOCUS_ABSOLUTE_POSITION (number)</li>
 * <li>stop_focusing (single switch)</li>
 * </ul>
 * It is <strong>VERY IMPORTANT</strong> that any subclasses use
 * <code>super.processNewSwitchValue(property, timestamp, elementsAndValues);</code>
 * and
 * <code>super.processNewNumberValue(property, timestamp, elementsAndValues);</code>
 * at the beginning of <code>processNewSwitchValue</code> and
 * <code>processNewNumberValue</code> to handle the generic focuser properties
 * correctly.
 * 
 * @author S. Alonso (Zerjillo) [zerjioi at ugr.es]
 */
public abstract class INDIFocuserDriver extends INDIDriver {

    /**
     * The last position to which the focuser has been sent (but it may have not
     * yet reached).
     */
    private int desiredAbsPosition;

    /**
     * The <code>FOCUS_SPEED</code> property.
     */
    protected INDINumberProperty focusSpeedP;

    /**
     * The <code>FOCUS_SPEED_VALUE</code> element.
     */
    protected INDINumberElement focusSpeedValueE;

    /**
     * The <code>ABS_FOCUS_POSITION</code> property.
     */
    protected INDINumberProperty absFocusPositionP;

    /**
     * The <code>FOCUS_ABSOLUTE_POSITION</code> element.
     */
    protected INDINumberElement focusAbsolutePositionE;

    /**
     * The <code>stop_focusing</code> property (not standard, but very useful).
     */
    protected INDISwitchProperty stopFocusingP;

    /**
     * Constructs a INDIFocuserDriver with a particular <code>inputStream</code>
     * from which to read the incoming messages (from clients) and a
     * <code>outputStream</code> to write the messages to the clients.
     * 
     * @param connection
     *            the indi connection to the server.
     */
    public INDIFocuserDriver(INDIConnection connection) {
        super(connection);
    }

    /**
     * Initializes the standard properties. MUST BE CALLED BY SUBDRIVERS.
     */
    protected void initializeStandardProperties() {
        absFocusPositionP = newNumberProperty().name(ABS_FOCUS_POSITION).label("Absolute").group("Control").create();
        focusAbsolutePositionE = absFocusPositionP.newElement().name("FOCUS_ABSOLUTE_POSITION").label("Focus Position").step(1).numberFormat("%.0f")
                .numberValue(getInitialAbsPos()).minimum(getMinimumAbsPos()).maximum(getMaximumAbsPos()).create();

        desiredAbsPosition = getInitialAbsPos();

        addProperty(absFocusPositionP);
    }

    /**
     * Gets the maximum speed of the focuser. Note that 0 is the minimum speed
     * for any focuser. Must be overloaded if the driver uses the
     * <code>FOCUS_SPEED</code> property.
     * 
     * @return The maximum speed of the focuser
     */
    protected int getMaximumSpeed() {
        return 0;
    }

    /**
     * Called when the <code>FOCUS_SPEED</code> property has been changed. Must
     * be overloaded if the driver uses the <code>FOCUS_SPEED</code> property.
     */
    public void speedHasBeenChanged() {
    }

    /**
     * Returns the maximum value that the <code>FOCUS_ABSOLUTE_POSITION</code>
     * element can have.
     * 
     * @return The maximum value
     */
    public abstract int getMaximumAbsPos();

    /**
     * Returns the minimum value that the <code>FOCUS_ABSOLUTE_POSITION</code>
     * element can have.
     * 
     * @return The minimum value
     */
    public abstract int getMinimumAbsPos();

    /**
     * Returns the initial value that the <code>FOCUS_ABSOLUTE_POSITION</code>
     * element shuld have.
     * 
     * @return The initial position
     */
    public abstract int getInitialAbsPos();

    /**
     * Called when the <code>ABS_FOCUS_POSITION</code> property has been
     * changed.
     */
    public abstract void absolutePositionHasBeenChanged();

    /**
     * Called when the <code>stop_focusing</code> property has been changed.
     * Must be overloaded if the driver uses the <code>stop_focusing</code>
     * property.
     */
    public void stopHasBeenRequested() {
    }

    /**
     * Shows the standard <code>FOCUS_SPEED</code> property. Must be called by
     * drivers that want to use this property.
     */
    protected void showSpeedProperty() {
        if (focusSpeedP == null) {
            focusSpeedP = newNumberProperty().saveable(true).name("FOCUS_SPEED").label("Focus Speed").group("Configuration").create();
            focusSpeedValueE = focusSpeedP.getElement("FOCUS_SPEED_VALUE");
            if (focusSpeedValueE == null) {
                focusSpeedValueE = focusSpeedP.newElement().name("").label("").numberValue(getMaximumSpeed()).maximum(getMaximumSpeed()).step(1).numberFormat("%.0f").create();
            }
        }

        addProperty(focusSpeedP);
    }

    /**
     * Shows the NON standard <code>stop_focusing</code> property. Must be
     * called by drivers that want to use this property.
     */
    protected void showStopFocusingProperty() {
        if (stopFocusingP == null) {
            stopFocusingP = newSwitchProperty().name(INDIStandardProperty.FOCUS_ABORT_MOTION).label("Stop").group("Control").create();
            stopFocusingP.newElement().name("Stop Focusing").create();
        }
        addProperty(stopFocusingP);
    }

    /**
     * Hides the <code>FOCUS_SPEED</code> property.
     */
    protected void hideSpeedProperty() {
        removeProperty(focusSpeedP);
    }

    /**
     * Hides the <code>stop_focusing</code> property.
     */
    protected void hideStopFocusingProperty() {
        removeProperty(stopFocusingP);
    }

    /**
     * Gets the value of the <code>FOCUS_SPEED_VALUE</code> element.
     * 
     * @return The current speed value
     */
    protected int getCurrentSpeed() {
        if (focusSpeedValueE != null) {
            return focusSpeedValueE.getValue().intValue();
        }
        return -1;
    }

    /**
     * Gets the desired absolute position (which may not have been reached by
     * the focuser).
     * 
     * @return The desired absolute position
     */
    protected int getDesiredAbsPosition() {
        return desiredAbsPosition;
    }

    /**
     * Must be called by drivers when the final position for the focuser has
     * been reached.
     */
    protected void finalPositionReached() {
        absFocusPositionP.setState(PropertyStates.OK);

        updateProperty(absFocusPositionP);

    }

    /**
     * Must be called by drivers when a new speed has been set.
     */
    protected void desiredSpeedSet() {
        focusSpeedP.setState(PropertyStates.OK);

        updateProperty(focusSpeedP);
    }

    /**
     * Must be called by drivers when the focuser stops (only when it has been
     * asked to stop).
     */
    protected void stopped() {
        stopFocusingP.setState(PropertyStates.OK);

        updateProperty(stopFocusingP);

    }

    /**
     * Should be called by the drivers when the focuser its moving. It can be
     * called with any frequency, but a less than one second is preferred to
     * notify the clients of the movement of the focuser.
     * 
     * @param currentPos
     *            The current position of the focuser.
     */
    protected void positionChanged(int currentPos) {
        focusAbsolutePositionE.setValue("" + currentPos);

        updateProperty(absFocusPositionP);

    }

    /**
     * Should be called by the drivers when the focuser speed changes (if for
     * example the device has a potentiometer to control the speed).
     * 
     * @param currentSpeed
     *            The current speed of the focuser.
     */
    protected void speedChanged(int currentSpeed) {
        focusSpeedValueE.setValue("" + currentSpeed);

        updateProperty(focusSpeedP);

    }

    @Override
    public void processNewSwitchValue(INDISwitchProperty property, Date timestamp, INDISwitchElementAndValue[] elementsAndValues) {
        if (property == stopFocusingP) {
            stopFocusingP.setState(PropertyStates.BUSY);
            stopFocusingP.firstElement().setOff();

            updateProperty(stopFocusingP);

            stopHasBeenRequested();
        }
    }

    @Override
    public void processNewNumberValue(INDINumberProperty property, Date timestamp, INDINumberElementAndValue[] elementsAndValues) {
        if (property == absFocusPositionP) {
            int newVal = elementsAndValues[0].getValue().intValue();

            if (newVal >= getMinimumAbsPos() && newVal <= getMaximumAbsPos()) {
                if (focusAbsolutePositionE.getValue().intValue() != newVal) {
                    absFocusPositionP.setState(PropertyStates.BUSY);

                    desiredAbsPosition = newVal;

                    updateProperty(absFocusPositionP);

                    absolutePositionHasBeenChanged();
                } else {
                    absFocusPositionP.setState(PropertyStates.OK);

                    updateProperty(absFocusPositionP);

                }
            }
        }

        if (property == focusSpeedP) {
            int newVal = elementsAndValues[0].getValue().intValue();

            if (newVal >= 0 && newVal <= getMaximumSpeed()) {
                if (focusSpeedValueE.getValue().intValue() != newVal) {
                    focusSpeedP.setState(PropertyStates.BUSY);

                    focusSpeedValueE.setValue("" + newVal);

                    updateProperty(focusSpeedP);

                    speedHasBeenChanged();
                } else {
                    focusSpeedP.setState(PropertyStates.OK);

                    updateProperty(focusSpeedP);

                }
            }
        }
    }
}
