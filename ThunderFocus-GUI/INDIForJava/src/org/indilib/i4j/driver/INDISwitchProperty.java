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
import org.indilib.i4j.Constants.PropertyPermissions;
import org.indilib.i4j.Constants.PropertyStates;
import org.indilib.i4j.Constants.SwitchRules;
import org.indilib.i4j.Constants.SwitchStatus;
import org.indilib.i4j.driver.util.INDIPropertyBuilder;
import org.indilib.i4j.protocol.DefSwitchVector;
import org.indilib.i4j.protocol.DefVector;
import org.indilib.i4j.protocol.SetSwitchVector;
import org.indilib.i4j.protocol.SetVector;

/**
 * A class representing a INDI Switch Property.
 * 
 * @author S. Alonso (Zerjillo) [zerjioi at ugr.es]
 */
public class INDISwitchProperty extends INDIProperty<INDISwitchElement> {

    /**
     * Serialization id.
     */
    private static final long serialVersionUID = -6801320230512062046L;

    /**
     * The current Rule for this Switch Property.
     */
    private SwitchRules rule;

    /**
     * Constructs an instance of a <code>INDISwitchProperty</code>. Called by
     * its sub-classes. useing the settings from the builder.
     * 
     * @param builder
     *            the builder with all the settings.
     */
    public INDISwitchProperty(INDIPropertyBuilder<INDISwitchProperty> builder) {
        super(builder);
        if (builder.permission() == PropertyPermissions.WO) {
            throw new IllegalArgumentException("Switch Properties cannot be Write Only");
        }
        rule = builder.switchRule();
    }

    /**
     * Resets to OFF al switch elements. Should not usually be called by
     * drivers. Used by <code>INDISwitchElement.setValue()</code> to ensure that
     * if the Switch rule allows only one ON simultaneous value the condition is
     * hold.
     */
    public void resetAllSwitches() {
        for (INDIElement element : this) {
            if (element.getValue() == SwitchStatus.ON) {
                ((INDISwitchElement) element).setValue(SwitchStatus.OFF);
            }
        }
    }

    /**
     * Resets to OFF al switch elements but one which is set to ON. Very useful
     * when dealing with <code>SwitchRules.ONE_OF_MANY</code> tipe of Switches.
     * 
     * @param element
     *            The element that is turned ON.
     * @return <code>true</code> if the selected element has been changed.
     *         <code>false</code> otherwise.
     */
    public boolean setOnlyOneSwitchOn(INDISwitchElement element) {
        boolean changed = false;

        if (element.getValue() == SwitchStatus.OFF) {
            changed = true;
        }

        resetAllSwitches();

        element.setValue(SwitchStatus.ON);

        return changed;
    }

    /**
     * Gets the current Rule for this Switch Property.
     * 
     * @return the current Rule for this Switch Property
     */
    public SwitchRules getRule() {
        return rule;
    }

    /**
     * Checks if the Rule of this Switch property holds.
     * 
     * @return <code>true</code> if the values of the Elements of this Property
     *         comply with the Rule. <code>false</code> otherwise.
     */
    protected boolean checkCorrectValues() {
        if (getState() == PropertyStates.OK) {

            int selectedCount = getSelectedCount();

            if (rule == SwitchRules.ONE_OF_MANY && selectedCount != 1) {
                return false;
            }

            if (rule == SwitchRules.AT_MOST_ONE && selectedCount > 1) {
                return false;
            }
        }

        return true;
    }

    /**
     * Gets the number of selected Switch Elements.
     * 
     * @return the number of selected Elements.
     */
    public int getSelectedCount() {
        int selectedCount = 0;
        for (INDISwitchElement el : this) {
            if (el.getValue() == SwitchStatus.ON) {
                selectedCount++;
            }
        }
        return selectedCount;
    }

    @Override
    protected DefVector<?> getXMLPropertyDefinitionInit() {
        return new DefSwitchVector().setRule(Constants.getSwitchRuleAsString(getRule()));
    }

    @Override
    protected SetVector<?> getXMLPropertySetInit() {
        return new SetSwitchVector();
    }

    /**
     * get the selected Element when the rule is ONE_OF_MANY.
     * 
     * @return the switch element that is turned on.
     */
    public INDISwitchElement getOnElement() {
        if (rule == SwitchRules.ONE_OF_MANY) {
            throw new UnsupportedOperationException("getOnElement() is only valid with SwitchRules.ONE_OF_MANY");
        } else {
            for (INDIElement element : this) {
                if (element.getValue() == SwitchStatus.ON) {
                    return (INDISwitchElement) element;
                }
            }
            return null;
        }
    }

    @Override
    protected Class<INDISwitchElement> elementClass() {
        return INDISwitchElement.class;
    }

}
