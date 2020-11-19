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

import org.indilib.i4j.Constants;
import org.indilib.i4j.driver.util.INDIPropertyBuilder;

import static org.indilib.i4j.properties.INDIStandardElement.PORT;
import static org.indilib.i4j.properties.INDIStandardProperty.DEVICE_PORT;

/**
 * A class representing a the standard INDI PORT Property. Depricated pleas use
 * the serial extention in furure.
 * 
 * @author S. Alonso (Zerjillo) [zerjioi at ugr.es]
 */
@Deprecated
public class INDIPortProperty extends INDITextProperty {

    /**
     * servial version id.
     */
    private static final long serialVersionUID = -7091582464434917050L;

    /**
     * The PORT element.
     */
    private INDITextElement portE;

    /**
     * Constructs an instance of a <code>INDIPortProperty</code>. Called by its
     * sub-classes. useing the settings from the builder.
     * 
     * @param builder
     *            the builder with all the settings.
     */
    public INDIPortProperty(INDIPropertyBuilder<INDIPortProperty> builder) {
        super(builder);
    }

    /**
     * Constructs an instance of a PORTS property, with its PORT element. If the
     * default value is null, "/dev/ttyUSB0" is assumed.
     * 
     * @param driver
     *            the driver to create the property.
     * @param defaultValue
     *            the default value for the port.
     * @return the new port property.
     */
    public static INDIPortProperty create(INDIDriver driver, String defaultValue) {
        INDIPortProperty result = driver.newProperty(INDIPortProperty.class).saveable(true).name(DEVICE_PORT).label("Ports").group(INDIDriver.GROUP_MAIN_CONTROL).create();
        result.portE = result.newElement().name(PORT).label("Port").textValue(defaultValue == null ? "/dev/ttyUSB0" : defaultValue).create();
        return result;
    }

    /**
     * Gets the PORT element value.
     * 
     * @return The PORT element value
     */
    public String getPort() {
        return portE.getValue();
    }

    /**
     * Sets the PORT element value.
     * 
     * @param port
     *            The new value for the PORT element
     */
    public void setPort(String port) {
        portE.setValue(port);
        this.setState(Constants.PropertyStates.OK);

        getDriver().updateProperty(this);
    }

    /**
     * Sets the PORT element value if the <code>property</code> corresponds to
     * this object. This method is a convenience one that can be placed in
     * <code>INDIDriver.processNewTextValue</code> safely.
     * 
     * @param property
     *            If this property corresponds to this PORTS property, the
     *            property will be updated
     * @param elementsAndValues
     *            An array of pairs of Text Elements and its requested values to
     *            be parsed and updated if <code>property</code> corresponds to
     *            this PORTS property
     * @see INDIDriver#processNewTextValue
     */
    public void processTextValue(INDITextProperty property, INDITextElementAndValue[] elementsAndValues) {
        if (property == this) {
            String port = elementsAndValues[0].getValue();

            setPort(port);
        }
    }
}
