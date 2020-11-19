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
import org.indilib.i4j.protocol.DefText;
import org.indilib.i4j.protocol.OneElement;
import org.indilib.i4j.protocol.OneText;

/**
 * A class representing a INDI Text Element.
 * 
 * @author S. Alonso (Zerjillo) [zerjioi at ugr.es]
 */
public class INDITextElement extends INDIElement {

    /**
     * Serialization id.
     */
    private static final long serialVersionUID = -4149843767292854200L;

    /**
     * The current value of the Text Element.
     */
    private String value;

    /**
     * Constructs an instance of a <code>INDITextElement</code>. Using the
     * settings from the builder.
     * 
     * @param builder
     *            the builder with all the settings.
     */
    public INDITextElement(INDIElementBuilder<INDITextElement> builder) {
        super(builder);
        value = builder.textValue();
    }

    @Override
    public INDITextProperty getProperty() {
        return (INDITextProperty) super.getProperty();
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public void setValue(Object newValue) {
        String v = null;

        try {
            v = (String) newValue;
        } catch (ClassCastException e) {
            throw new IllegalArgumentException("Value for a Text Element must be a String");
        }

        value = v;
    }

    @Override
    public OneElement<?> getXMLOneElement(boolean includeMinMaxStep) {
        return new OneText().setName(getName()).setTextContent(value);
    }

    @Override
    public String getNameAndValueAsString() {
        return getName() + " - " + getValue();
    }

    @Override
    protected DefElement<?> getXMLDefElement() {
        return new DefText().setName(getName()).setLabel(getLabel()).setTextContent(value);
    }

    @Override
    public String parseOneValue(OneElement<?> xml) {
        return xml.getTextContent().trim();
    }
}
