package org.indilib.i4j.driver.connection;

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

import org.indilib.i4j.Constants.PropertyStates;
import org.indilib.i4j.Constants.SwitchRules;
import org.indilib.i4j.Constants.SwitchStatus;
import org.indilib.i4j.INDIException;
import org.indilib.i4j.driver.*;
import org.indilib.i4j.driver.annotation.InjectElement;
import org.indilib.i4j.driver.annotation.InjectProperty;
import org.indilib.i4j.driver.event.SwitchEvent;

import java.util.Date;

import static org.indilib.i4j.properties.INDIStandardElement.CONNECT;
import static org.indilib.i4j.properties.INDIStandardElement.DISCONNECT;
import static org.indilib.i4j.properties.INDIStandardProperty.CONNECTION;

/**
 * The standard Connection extension, is activated for any driver implementing
 * the INDIConnectionHandler interface.
 * 
 * @author Richard van Nieuwenhoven
 */
public class INDIConnectionExtension extends INDIDriverExtension<INDIDriver> {

    /**
     * The standard CONNECTION property (optional).
     */
    @InjectProperty(std = CONNECTION, label = "Connection", group = INDIDriver.GROUP_MAIN_CONTROL, timeout = 100, switchRule = SwitchRules.ONE_OF_MANY)
    private INDISwitchProperty connectionP;

    /**
     * A Switch Element for the CONNECTION property.
     */
    @InjectElement(std = CONNECT, label = "Connect")
    private INDISwitchElement connectedE;

    /**
     * A Switch Element for the CONNECTION property.
     */
    @InjectElement(std = DISCONNECT, label = "Disconnect", switchValue = SwitchStatus.ON)
    private INDISwitchElement disconnectedE;

    /**
     * the handler for the connection (normally the driver itself).
     */
    private INDIConnectionHandler connectionHandler;

    /**
     * Constructor for the connection extension.
     * 
     * @param driver
     *            the driver to connect to.
     */
    public INDIConnectionExtension(INDIDriver driver) {
        super(driver);
        if (isActive()) {
            connectionHandler = (INDIConnectionHandler) driver;
            connectionP.setEventHandler(new SwitchEvent() {

                @Override
                public void processNewValue(Date timestamp, INDISwitchElementAndValue[] elementsAndValues) {
                    handleConnectionProperty(elementsAndValues, timestamp);
                }
            });
            addProperty(connectionP);
        }
    }

    @Override
    public boolean isActive() {
        return driver instanceof INDIConnectionHandler;
    }

    /**
     * Sets the CONNECTION Property to connected or disconnected and sends the
     * changes to the clients. If the property does not exist nothing happens.
     * If the connection is already stablished ignore petition.
     * 
     * @param connected
     *            is the driver connected at the moment.
     * @param message
     *            An optional message (can be <code>null</code>)
     *            <code>true</code> if the CONNECT Element must be selected.
     *            <code>false</code> if the DISCONNECT Element must be selected.
     */
    private void setConnectionProperty(boolean connected, String message) {
        if (connectionP == null) {
            return;
        }

        connectionP.setState(PropertyStates.OK);

        if (connected) {
            connectedE.setValue(SwitchStatus.ON);
        } else {
            disconnectedE.setValue(SwitchStatus.ON);
        }

        if (message == null) {

            updateProperty(connectionP);
        } else {
            connectionP.setState(PropertyStates.ALERT);
            updateProperty(connectionP, message);
        }
    }

    /**
     * Sets the CONNECTION Property to connected or disconnected and sends the
     * changes to the clients. If the property does not exist nothing happens.
     * 
     * @param connected
     *            <code>true</code> if the CONNECT Element must be selected.
     *            <code>false</code> if the DISCONNECT Element must be selected.
     */
    private void setConnectionProperty(boolean connected) {
        setConnectionProperty(connected, null);
    }

    /**
     * Checks if the CONNECTION Property is set to <code>SwitchStatus.ON</code>.
     * 
     * @return <code>true</code> if the CONNECTION Property is set to
     *         <code>SwitchStatus.ON</code>. <code>false</code> otherwise.
     */
    public boolean isConnected() {
        if (connectedE.getValue() == SwitchStatus.ON) {
            return true;
        }

        return false;
    }

    /**
     * Handles the connection property. Called from
     * <code>processNewSwitchVector</code>.
     * 
     * @param newEvs
     *            The new Elements and Values
     * @param timestamp
     *            The timestamp of the received CONNECTION message.
     */
    private synchronized void handleConnectionProperty(INDISwitchElementAndValue[] newEvs, Date timestamp) {
        for (INDISwitchElementAndValue newEv : newEvs) {
            INDISwitchElement el = newEv.getElement();
            SwitchStatus s = newEv.getValue();

            if (el == connectedE) {
                if (s == SwitchStatus.ON) {
                    if (connectedE.isOff()) {
                        try {
                            connectionHandler.driverConnect(timestamp);

                            setConnectionProperty(true);
                        } catch (INDIException e) {
                            setConnectionProperty(false, e.getMessage());
                        }
                    } else {
                        setConnectionProperty(true);
                    }
                }
            } else if (el == disconnectedE && s == SwitchStatus.ON) {
                if (disconnectedE.isOff()) {
                    try {
                        connectionHandler.driverDisconnect(timestamp);
                        setConnectionProperty(false);
                    } catch (INDIException e) {
                        setConnectionProperty(true, e.getMessage());
                    }
                } else {
                    setConnectionProperty(false);
                }

            }
        }
    }

}
