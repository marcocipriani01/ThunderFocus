package org.indilib.i4j.server.api;

/*
 * #%L
 * INDI for Java Server Library
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
/**
 * This interface represents a connected device in the indiserver.
 * 
 * @author Richard van Nieuwenhoven
 */
public interface INDIDeviceInterface {

    /**
     * Gets a Device identifier.
     * 
     * @return A Device identifier.
     */
    String getDeviceIdentifier();

    /**
     * Gets the names that the Device is attending (might be more than one in
     * Network Devices).
     * 
     * @return the names that the Device is attending.
     */
    String[] getNames();

}
