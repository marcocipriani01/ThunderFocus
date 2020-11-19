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

import org.indilib.i4j.driver.INDIElementAndValue;
import org.indilib.i4j.driver.INDITextElement;
import org.indilib.i4j.driver.INDITextElementAndValue;
import org.indilib.i4j.driver.INDITextProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

/**
 * This is a convince class the reduce the number of genetics that must be
 * specified for event handler. In this case it handles it for the Text
 * properties.
 * 
 * @author Richard van Nieuwenhoven
 */
public abstract class TextEvent implements IEventHandler<INDITextProperty, INDITextElement, String> {

    /**
     * the log to write messages to.
     */
    private static final Logger LOG = LoggerFactory.getLogger(TextEvent.class);

    /**
     * the current property being changed. do not use it if nor really
     * Necessary, it's much better and readable to use direct references.
     */
    protected INDITextProperty property;

    @Override
    public final void processNewValue(INDITextProperty valueProperty, Date date, INDIElementAndValue<INDITextElement, String>[] elementsAndValues) {
        property = valueProperty;
        if (elementsAndValues instanceof INDITextElementAndValue[]) {
            processNewValue(date, INDITextElementAndValue[].class.cast(elementsAndValues));
        } else {
            LOG.error("illegal value for process new value");
        }
    }

    /**
     * the Simplified call without the property. TODO: should we also exclude
     * the date? it is almoust never used.
     * 
     * @param date
     *            the time it was set
     * @param elementsAndValues
     *            the new values for the elements of this property
     */

    public abstract void processNewValue(Date date, INDITextElementAndValue[] elementsAndValues);
}
