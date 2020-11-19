package org.indilib.i4j.driver.event;

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

import org.indilib.i4j.driver.INDIElement;
import org.indilib.i4j.driver.INDIElementAndValue;
import org.indilib.i4j.driver.INDIProperty;

import java.util.Date;

/**
 * This is the definition of a direct event handler for a property. All generics
 * are set so the method can be as specific as possible. The method will be
 * called when a new value for the property was set.
 * 
 * @param <Prop>
 *            the property type for this event.
 * @param <Elem>
 *            the element type that property uses.
 * @param <Type>
 *            the value type the property uses.
 * @author Richard van Nieuwenhoven
 */
public interface IEventHandler<Prop extends INDIProperty<Elem>, Elem extends INDIElement, Type> {

    /**
     * The property got an new value from the client, if there is any processing
     * needed do it here. use the property parameter only if working ob some
     * generic stuff. it's better to have direkt references to the field.
     * Attention the new values where not processed yet!
     * 
     * @param property
     *            the property that was set
     * @param date
     *            the time it was set
     * @param elementsAndValues
     *            the new values for the elements of this property
     */
    void processNewValue(Prop property, Date date, INDIElementAndValue<Elem, Type>[] elementsAndValues);
}
