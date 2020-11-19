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

/**
 * A interface representing a pair of a INDIElement and a value for the Element.
 * 
 * @param <Element>
 *            the element type for this element and value holder.
 * @param <Type>
 *            the type for this element and value holder.
 * @author S. Alonso (Zerjillo) [zerjioi at ugr.es]
 */
public class INDIElementAndValue<Element extends INDIElement, Type> {

    /**
     * The Number element.
     */
    private final Element element;

    /**
     * The Number value.
     */
    private final Type value;

    /**
     * Constructs an instance of a <code>INDINumberElementAndValue</code>. This
     * class should not usually be instantiated by specific Drivers.
     * 
     * @param element
     *            The Number Element
     * @param value
     *            The number
     */
    public INDIElementAndValue(Element element, Type value) {
        this.element = element;
        this.value = value;
    }

    /**
     * @return the element.
     */
    public Element getElement() {
        return element;
    }

    /**
     * @return the value.
     */
    public Type getValue() {
        return value;
    }
}
