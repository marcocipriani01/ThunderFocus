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

import org.indilib.i4j.driver.util.INDIPropertyBuilder;
import org.indilib.i4j.protocol.DefBlobVector;
import org.indilib.i4j.protocol.DefVector;
import org.indilib.i4j.protocol.SetBlobVector;

/**
 * A class representing a INDI BLOB Property.
 * 
 * @author S. Alonso (Zerjillo) [zerjioi at ugr.es]
 */
public class INDIBLOBProperty extends INDIProperty<INDIBLOBElement> {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    /**
     * Constructs an instance of <code>INDIBLOBProperty</code> with the
     * properties collected by the builder.
     * 
     * @param builder
     *            the builder containing the properties.
     */
    public INDIBLOBProperty(INDIPropertyBuilder<INDIBLOBProperty> builder) {
        super(builder);
    }

    @Override
    public INDIBLOBElement getElement(String name) {
        return super.getElement(name);
    }

    @Override
    protected DefVector<?> getXMLPropertyDefinitionInit() {
        return new DefBlobVector();
    }

    @Override
    protected SetBlobVector getXMLPropertySetInit() {
        return new SetBlobVector();
    }

    @Override
    protected Class<INDIBLOBElement> elementClass() {
        return INDIBLOBElement.class;
    }
}
