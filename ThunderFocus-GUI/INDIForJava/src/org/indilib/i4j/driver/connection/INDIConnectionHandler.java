package org.indilib.i4j.driver.connection;

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

import org.indilib.i4j.INDIException;

import java.util.Date;

/**
 * An interface for those drivers that wish to have a standard CONNECTION
 * property. Note that any INDIDriver implementing this interface will
 * automatically include the connection property. No code will be necessary in
 * the Driver code to include or manage it.
 * 
 * @author S. Alonso (Zerjillo) [zerjioi at ugr.es]
 */
public interface INDIConnectionHandler {

    /**
     * The method that will handle the connection.
     * 
     * @param timestamp
     *            when the connection message has been received.
     * @throws INDIException
     *             if the connection failed.
     */
    void driverConnect(Date timestamp) throws INDIException;

    /**
     * The method that will handle the disconnection.
     * 
     * @param timestamp
     *            when the disconnection message has been received.
     * @throws INDIException
     *             if the connection failed.
     */
    void driverDisconnect(Date timestamp) throws INDIException;
}
