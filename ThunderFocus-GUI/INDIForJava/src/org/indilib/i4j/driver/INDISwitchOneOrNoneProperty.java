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

import org.indilib.i4j.Constants.SwitchRules;
import org.indilib.i4j.Constants.SwitchStatus;
import org.indilib.i4j.driver.util.INDIPropertyBuilder;

/**
 * A class representing a INDI One or None Switch Property (aka a simple
 * button). It simplifies dealing with Switch elements and so on.
 * 
 * @author S. Alonso (Zerjillo) [zerjioi at ugr.es]
 */
public class INDISwitchOneOrNoneProperty extends INDISwitchProperty {

    /**
     * Serialization id.
     */
    private static final long serialVersionUID = -7203487459293368286L;

    /**
     * Constructs an instance of a <code>INDISwitchOneOrNoneProperty</code>.
     * Called by its sub-classes. useing the settings from the builder.
     * 
     * @param builder
     *            the builder with all the settings.
     */
    public INDISwitchOneOrNoneProperty(INDIPropertyBuilder<INDISwitchProperty> builder) {
        super(builder.switchRule(SwitchRules.AT_MOST_ONE));
    }

    /**
     * Sets the status of the Element of the Property.
     * 
     * @param newStatus
     *            The new status
     */
    public void setStatus(SwitchStatus newStatus) {
        firstElement().setValue(newStatus);
    }

    /**
     * Sets the status of the Element of the property according to a pair of ev
     * and values.
     * 
     * @param ev
     *            The pairs of ev and values (only one except some error /
     *            strange behaviour).
     */
    public void setStatus(INDISwitchElementAndValue[] ev) {
        INDISwitchElement firstElement = firstElement();
        for (INDISwitchElementAndValue element : ev) {
            if (element.getElement() == firstElement) {
                firstElement.setValue(element.getValue());
            }
        }
    }

    /**
     * Gets the status of the Element of the Property.
     * 
     * @return The status of the Element of the Property
     */
    public SwitchStatus getStatus() {
        return firstElement().getValue();
    }

    /**
     * Gets the status of the Element of the Property that would be set
     * according to some pairs of ev and values. This method DOES NOT change the
     * status of the Element NOR it gives the actual status of it.
     * 
     * @param ev
     *            The pairs of ev and values (only one except some error /
     *            strange behaviour).
     * @return The status of the Element of the Property that would be set
     */
    public SwitchStatus getStatus(INDISwitchElementAndValue[] ev) {
        INDISwitchElement firstElement = firstElement();
        for (INDISwitchElementAndValue element : ev) {
            if (element.getElement() == firstElement) {
                return element.getValue();
            }
        }

        return SwitchStatus.OFF;
    }
}
