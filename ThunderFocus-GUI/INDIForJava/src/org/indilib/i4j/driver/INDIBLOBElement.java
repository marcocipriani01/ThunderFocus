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

import org.indilib.i4j.INDIBLOBValue;
import org.indilib.i4j.driver.util.INDIElementBuilder;
import org.indilib.i4j.protocol.DefBlob;
import org.indilib.i4j.protocol.DefElement;
import org.indilib.i4j.protocol.OneBlob;
import org.indilib.i4j.protocol.OneElement;

/**
 * A class representing a INDI BLOB Element.
 * 
 * @author S. Alonso (Zerjillo) [zerjioi at ugr.es]
 */
public class INDIBLOBElement extends INDIElement {

    /**
     * Serialization id.
     */
    private static final long serialVersionUID = -2684237095700108321L;

    /**
     * The current value of the BLOB Element.
     */
    private INDIBLOBValue value;

    /**
     * Constructs an instance of a <code>INDIBLOBElement</code> with the
     * settings from the <code>builder</code>.
     * 
     * @param builder
     *            the builder to get the setting from.
     */
    public INDIBLOBElement(INDIElementBuilder<INDIBLOBElement> builder) {
        super(builder);
        value = new INDIBLOBValue(new byte[0], "");
    }

    @Override
    public INDIBLOBProperty getProperty() {
        return (INDIBLOBProperty) super.getProperty();
    }

    @Override
    public INDIBLOBValue getValue() {
        return value;
    }

    @Override
    public void setValue(Object newValue) {
        INDIBLOBValue b = null;
        try {
            b = (INDIBLOBValue) newValue;
        } catch (ClassCastException e) {
            throw new IllegalArgumentException("Value for a BLOB Element must be a INDIBLOBValue");
        }

        value = b;
    }

    @Override
    public OneElement<?> getXMLOneElement(boolean includeMinMaxStep) {
        return new OneBlob().setName(getName()).setByteContent(value.getBlobData()).setFormat(value.getFormat());
    }

    @Override
    public String getNameAndValueAsString() {
        return getName() + " - BLOB format: " + this.getValue().getFormat() + " - BLOB Size: " + this.getValue().getSize();
    }

    @Override
    protected DefElement<?> getXMLDefElement() {
        return new DefBlob().setName(this.getName()).setLabel(getLabel());
    }

    @Override
    public Object parseOneValue(OneElement<?> xml) {
        return new INDIBLOBValue((OneBlob) xml);
    }
}
