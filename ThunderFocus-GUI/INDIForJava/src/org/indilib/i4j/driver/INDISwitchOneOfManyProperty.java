package org.indilib.i4j.driver;

/*s
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
 * A class representing a INDI One Of Many Switch Property. It simplifies
 * dealing with Switch elements and so on.
 * 
 * @author S. Alonso (Zerjillo) [zerjioi at ugr.es]
 */
public class INDISwitchOneOfManyProperty extends INDISwitchProperty {

    /**
     * Serialization id.
     */
    private static final long serialVersionUID = -7669211544834222712L;

    /**
     * Constructs an instance of a <code>INDISwitchOneOfManyProperty</code>.
     * Called by its sub-classes. useing the settings from the builder.
     * 
     * @param builder
     *            the builder with all the settings.
     */
    public INDISwitchOneOfManyProperty(INDIPropertyBuilder<INDISwitchProperty> builder) {
        super(builder.switchRule(SwitchRules.ONE_OF_MANY));
    }

    /**
     * Gets the name of the selected element.
     * 
     * @return The name of the selected eleent
     */
    public String getSelectedValue() {
        INDISwitchElement e = getSelectedElement();

        return e.getName();
    }

    /**
     * Gets the selected element.
     * 
     * @return The selected element
     */
    private INDISwitchElement getSelectedElement() {
        for (INDISwitchElement e : this) {
            if (e.getValue() == SwitchStatus.ON) {
                return e;
            }
        }
        return null; // Should never happen
    }

    /**
     * Gets the index of the selected element.
     * 
     * @return The index of the selected element
     */
    public int getSelectedIndex() {
        int index = 0;
        for (INDISwitchElement e : this) {
            if (e.getValue() == SwitchStatus.ON) {
                return index;
            }
            index++;
        }

        return -1; // Should never happen
    }

    /**
     * Gets the index of the element that should be selected according to some
     * Elements and Values pairs. This method DOES NOT change the selected index
     * nor returns the really selected element index.
     * 
     * @param ev
     *            The pairs of elements and values
     * @return The index of the element that would be selected according to the
     *         pairs of elements and values.
     */
    public int getSelectedIndex(INDISwitchElementAndValue[] ev) {
        INDISwitchElementAndValue indiSwitchElementAndValue = getSelectedElement(ev);
        int index = 0;
        for (INDISwitchElement e : this) {
            if (e == indiSwitchElementAndValue.getElement()) {
                return index;
            }
            index++;
        }
        return -1;
    }

    /**
     * get the element form the specified array that is selected.
     * 
     * @param ev
     *            the array of element and values
     * @return the first element that is on
     */
    public INDISwitchElementAndValue getSelectedElement(INDISwitchElementAndValue[] ev) {
        for (INDISwitchElementAndValue element : ev) {
            if (element.getValue() == SwitchStatus.ON) {
                return element;
            }
        }
        return null;
    }

    /**
     * Gets the element that should be selected according to some Elements and
     * Values pairs. This method DOES NOT change the selected index nor returns
     * the really selected element.
     * 
     * @param ev
     *            The pairs of elements and values
     * @return The element that would be selected according to the pairs of
     *         elements and values.
     */
    public String getSelectedValue(INDISwitchElementAndValue[] ev) {
        INDISwitchElementAndValue indiSwitchElementAndValue = getSelectedElement(ev);
        for (INDISwitchElement e : this) {
            if (e == indiSwitchElementAndValue.getElement()) {
                return e.getName();
            }
        }
        return null;
    }

    /**
     * Sets the selected Element to the one specified in an array of elements
     * and values.
     * 
     * @param ev
     *            The pairs of elements and values
     */
    public void setSelectedIndex(INDISwitchElementAndValue[] ev) {
        INDISwitchElementAndValue selected = getSelectedElement(ev);
        resetAllSwitches();
        selected.getElement().setOn();
    }
}
