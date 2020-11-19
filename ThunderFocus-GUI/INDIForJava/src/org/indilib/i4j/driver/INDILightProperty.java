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
import org.indilib.i4j.protocol.DefLightVector;
import org.indilib.i4j.protocol.DefVector;
import org.indilib.i4j.protocol.SetLightVector;
import org.indilib.i4j.protocol.SetVector;

/**
 * A class representing a INDI Light Property.
 * 
 * @author S. Alonso (Zerjillo) [zerjioi at ugr.es]
 */
public class INDILightProperty extends INDIProperty<INDILightElement> {

    /**
     * Serialization id.
     */
    private static final long serialVersionUID = -3655076563473257286L;

    /**
     * Constructs an instance of a <code>INDILightProperty</code>. Called by its
     * sub-classes. useing the settings from the builder.
     * 
     * @param builder
     *            the builder with all the settings.
     */
    public INDILightProperty(INDIPropertyBuilder<?> builder) {
        super(builder);
    }

    @Override
    public INDILightElement getElement(String name) {
        return super.getElement(name);
    }

    @Override
    protected DefVector<?> getXMLPropertyDefinitionInit() {
        return new DefLightVector();
    }

    @Override
    protected SetVector<?> getXMLPropertySetInit() {
        return new SetLightVector();
    }

    @Override
    protected Class<INDILightElement> elementClass() {
        return INDILightElement.class;
    }
}
