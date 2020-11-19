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

import org.indilib.i4j.driver.util.INDIElementBuilder;
import org.indilib.i4j.protocol.DefElement;
import org.indilib.i4j.protocol.OneElement;

import java.io.Serializable;

/**
 * A class representing a INDI Element. The subclasses
 * <code>INDIBLOBElement</code>, <code>INDILightElement</code>,
 * <code>INDINumberElement</code>, <code>INDISwitchElement</code> and
 * <code>INDITextElement</code> define the basic Elements that a INDI Property
 * may contain according to the INDI protocol.
 * 
 * @author S. Alonso (Zerjillo) [zerjioi at ugr.es]
 */
public abstract class INDIElement implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    /**
     * The name of the Element.
     */
    private String name;

    /**
     * The label of the Element.
     */
    private String label;

    /**
     * The Property to which this Element belongs.
     */
    private INDIProperty property;

    /**
     * Constructs an instance of <code>INDIElement</code> with properties from
     * the builder. Called by its sub-classes.
     * 
     * @param builder
     *            the builder to get the setting from.
     */
    @SuppressWarnings("unchecked")
    protected INDIElement(INDIElementBuilder<? extends INDIElement> builder) {
        property = builder.indiProperty();
        name = builder.name();
        if (name.isEmpty()) {
            throw new IllegalArgumentException("No name for Element");
        }
        label = builder.label();
        property.addElement(this);
    }

    /**
     * Gets the Property to which this Element belongs.
     * 
     * @return The Property to which this Element belongs
     */
    public INDIProperty<?> getProperty() {
        return property;
    }

    /**
     * Gets the label of the Element.
     * 
     * @return The label of the Element.
     */
    public String getLabel() {
        return label;
    }

    /**
     * Gets the name of the Element.
     * 
     * @return The name of the Element.
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the current value of the Element.
     * 
     * @return The current value of the Element.
     */
    public abstract Object getValue();

    /**
     * Parses a &lt;oneXXX&gt; XML message and gets the desired value in it.
     * 
     * @param xml
     *            The XML element to be parsed.
     * @return The value of the element described in the <code>XML</code>
     *         element.
     */
    public abstract Object parseOneValue(OneElement<?> xml);

    /**
     * Sets the value of the Element to <code>newValue</code>.
     * 
     * @param newValue
     *            The new value for the Element. if the <code>newValue</code> is
     *            not a valid one for the type of the Element.
     */
    public abstract void setValue(Object newValue);

    /**
     * Gets a &lt;oneXXX&gt; XML string describing the current value of the
     * Element.
     * 
     * @param includeMinMaxStep
     *            include the new min max value in the xml
     * @return the &lt;oneXXX&gt; XML string describing the current value of the
     *         Element.
     */
    protected abstract OneElement<?> getXMLOneElement(boolean includeMinMaxStep);

    /**
     * Gets a &lt;defXXX&gt; XML string describing the current value and
     * properties of the Element.
     * 
     * @return The &lt;defXXX&gt; XML string describing the current value and
     *         properties of the Element.
     */
    protected abstract DefElement<?> getXMLDefElement();

    /**
     * Gets the name of the element and its current value.
     * 
     * @return a String with the name of the Element and Its Value
     */
    public abstract String getNameAndValueAsString();
}
