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
import org.indilib.i4j.protocol.DefNumberVector;
import org.indilib.i4j.protocol.DefVector;
import org.indilib.i4j.protocol.SetNumberVector;
import org.indilib.i4j.protocol.SetVector;

/**
 * A class representing a INDI Number Property.
 * 
 * @author S. Alonso (Zerjillo) [zerjioi at ugr.es]
 */
public class INDINumberProperty extends INDIProperty<INDINumberElement> {

    /**
     * Serialization id.
     */
    private static final long serialVersionUID = 8341274865983266472L;

    /**
     * Constructs an instance of <code>INDINumberProperty</code> with the
     * partikular setting in the builder.
     * 
     * @param builder
     *            the builder with all the properties.
     */
    public INDINumberProperty(INDIPropertyBuilder<?> builder) {
        super(builder);
    }

    @Override
    public INDINumberElement getElement(String name) {
        return super.getElement(name);
    }

    @Override
    protected DefVector<?> getXMLPropertyDefinitionInit() {
        return new DefNumberVector();
    }

    @Override
    protected SetVector<?> getXMLPropertySetInit() {
        return new SetNumberVector();
    }

    @Override
    protected Class<INDINumberElement> elementClass() {
        return INDINumberElement.class;
    }
}
