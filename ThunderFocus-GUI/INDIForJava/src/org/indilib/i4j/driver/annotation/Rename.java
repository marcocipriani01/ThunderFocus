package org.indilib.i4j.driver.annotation;

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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program. If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

/**
 * Rename a property or element in an driver extension. This can be necessary if
 * the extension in included multiple times in one driver.
 * 
 * @author Richard van Nieuwenhoven
 */
public @interface Rename {

    /**
     * @return the original name of the property or element.
     */
    String name();

    /**
     * @return the new name in case of the current injection, (The prefix will
     *         not be applied).
     */
    String to();

}
