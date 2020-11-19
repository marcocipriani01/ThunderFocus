package org.indilib.i4j.driver.util;

/*
 * #%L
 * INDI for Java Driver Library
 * %%
 * Copyright (C) 2012 - 2014 indiforjava
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program. If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

import org.indilib.i4j.Constants.LightStates;
import org.indilib.i4j.Constants.SwitchStatus;
import org.indilib.i4j.driver.INDIElement;
import org.indilib.i4j.driver.INDIProperty;
import org.indilib.i4j.driver.annotation.InjectElement;
import org.indilib.i4j.properties.INDIStandardElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Builder utility to build a element for a property.
 * 
 * @param <ElementClass>
 *            property class to build.
 * @author Richard van Nieuwenhoven
 */
public class INDIElementBuilder<ElementClass extends INDIElement> {

    /**
     * Logger to log to.
     */
    private static final Logger LOG = LoggerFactory.getLogger(INDIElementBuilder.class);

    /**
     * the element class to instanciate.
     */
    private final Class<ElementClass> clazz;

    /**
     * the property to create an element for.
     */
    private INDIProperty<?> indiProperty;

    /**
     * the name of the element (mandatory).
     */
    private String name = "";

    /**
     * the label of the element (mandatory).
     */
    private String label = "";

    /**
     * the default value of the element when it is a number field, defaults to
     * 0.
     */
    private double numberValue;

    /**
     * the default value of the element when it is a text field, defaults to an
     * empty string.
     */
    private String textValue = "";

    /**
     * the minimal value of the element when it is a number field, defaults to
     * 0.
     */
    private double minimum;

    /**
     * the maximal value of the element when it is a number field, defaults to
     * 0.
     */
    private double maximum;

    /**
     * the step value of the element when it is a number field, defaults to 0.
     */
    private double step;

    /**
     * the number format value of the element when it is a number field,
     * defaults to %g.
     */
    private String numberFormat = "%g";

    /**
     * the index number replacement for the lowercase 'n' character..
     */
    private int nIndex = 1;

    /**
     * the default value of the element when it is a switch field, defaults to
     * an empty string.
     */
    private SwitchStatus switchValue = SwitchStatus.OFF;

    /**
     * constructor for the property builder.
     * 
     * @param clazz
     *            the class of the element.
     * @param indiProperty
     *            the property to create an element for.
     */
    public INDIElementBuilder(Class<ElementClass> clazz, INDIProperty<?> indiProperty) {
        this.clazz = clazz;
        this.indiProperty = indiProperty;
    }

    /**
     * the default value of the element when it is a light field, defaults to an
     * empty string.
     */
    private LightStates state = LightStates.IDLE;

    /**
     * @return the name of the element (mandatory).
     */
    public String name() {
        if (name.isEmpty()) {
            return indiProperty.getName();
        }
        return name;
    }

    /**
     * @return the label of the element (mandatory).
     */
    public String label() {
        if (label.isEmpty()) {
            if (name.isEmpty()) {
                return indiProperty.getLabel();
            } else {
                return name;
            }
        }
        return label;
    }

    /**
     * @return the default value of the element when it is a number field,
     *         defaults to 0.
     */
    public double numberValue() {
        return numberValue;
    }

    /**
     * @return the default value of the element when it is a text field,
     *         defaults to an empty string.
     */
    public String textValue() {
        return textValue;
    }

    /**
     * @return the minimal value of the element when it is a number field,
     *         defaults to 0.
     */
    public double minimum() {
        return minimum;
    }

    /**
     * @return the maximal value of the element when it is a number field,
     *         defaults to 0.
     */
    public double maximum() {
        return maximum;
    }

    /**
     * @return the step value of the element when it is a number field, defaults
     *         to 0.
     */
    public double step() {
        return step;
    }

    /**
     * @return the number format value of the element when it is a number field,
     *         defaults to %g.
     */
    public String numberFormat() {
        return numberFormat;
    }

    /**
     * @return the default value of the element when it is a switch field,
     *         defaults to an empty string.
     */
    public SwitchStatus switchValue() {
        return switchValue;
    }

    /**
     * @return the default value of the element when it is a light field,
     *         defaults to an empty string.
     */
    public LightStates state() {
        return state;
    }

    /**
     * @return the indi proerty to connect this element.
     */
    public INDIProperty<?> indiProperty() {
        return indiProperty;
    }

    /**
     * set the name of the element (mandatory).
     * 
     * @param nameValue
     *            the new value
     * @return the builder itself.
     */
    public INDIElementBuilder<ElementClass> name(String nameValue) {
        if (nameValue != null) {
            name = nameValue.trim();
        }
        return this;
    }

    /**
     * set the name of the element (mandatory).
     * 
     * @param nameValue
     *            the new value
     * @return the builder itself.
     */
    public INDIElementBuilder<ElementClass> name(INDIStandardElement nameValue) {
        if (nameValue != null) {
            name = nameValue.name();
        }
        return this;
    }

    /**
     * set the label of the element (mandatory).
     * 
     * @param labelValue
     *            the new value
     * @return the builder itself.
     */
    public INDIElementBuilder<ElementClass> label(String labelValue) {
        if (labelValue != null) {
            label = labelValue.trim();
        }
        return this;
    }

    /**
     * set the default value of the element when it is a number field, defaults
     * to 0.
     * 
     * @param newNumberValue
     *            the new value
     * @return the builder itself.
     */
    public INDIElementBuilder<ElementClass> numberValue(double newNumberValue) {
        numberValue = newNumberValue;
        return this;
    }

    /**
     * set the default value of the element when it is a text field, defaults to
     * an empty string.
     * 
     * @param newTextValue
     *            the new value
     * @return the builder itself.
     */
    public INDIElementBuilder<ElementClass> textValue(String newTextValue) {
        if (newTextValue != null) {
            textValue = newTextValue.trim();
        }
        return this;
    }

    /**
     * set the minimal value of the element when it is a number field, defaults
     * to 0.
     * 
     * @param minimumValue
     *            the new value
     * @return the builder itself.
     */
    public INDIElementBuilder<ElementClass> minimum(double minimumValue) {
        minimum = minimumValue;
        return this;
    }

    /**
     * set the maximal value of the element when it is a number field, defaults
     * to 0.
     * 
     * @param maximumValue
     *            the new value
     * @return the builder itself.
     */
    public INDIElementBuilder<ElementClass> maximum(double maximumValue) {
        maximum = maximumValue;
        return this;
    }

    /**
     * set the step value of the element when it is a number field, defaults to
     * 0.
     * 
     * @param stepValue
     *            the new value
     * @return the builder itself.
     */
    public INDIElementBuilder<ElementClass> step(double stepValue) {
        step = stepValue;
        return this;
    }

    /**
     * set the number format value of the element when it is a number field,
     * defaults to %g.
     * 
     * @param numberFormatValue
     *            the new value
     * @return the builder itself.
     */
    public INDIElementBuilder<ElementClass> numberFormat(String numberFormatValue) {
        if (numberFormatValue != null) {
            numberFormat = numberFormatValue;
        }
        return this;
    }

    /**
     * set the default value of the element when it is a switch field, defaults
     * to an empty string.
     * 
     * @param newSwitchValue
     *            the new value
     * @return the builder itself.
     */
    public INDIElementBuilder<ElementClass> switchValue(SwitchStatus newSwitchValue) {
        if (newSwitchValue != null) {
            switchValue = newSwitchValue;
        }
        return this;
    }

    /**
     * set the default value of the element when it is a light field, defaults
     * to an empty string.
     * 
     * @param stateValue
     *            the new value
     * @return the builder itself.
     */
    public INDIElementBuilder<ElementClass> state(LightStates stateValue) {
        if (stateValue != null) {
            state = stateValue;
        }
        return this;
    }

    /**
     * set the index replacement for the lowercase 'n' character.
     * 
     * @param index
     *            the new index value.
     * @return the builder itself.
     */
    public INDIElementBuilder<ElementClass> nIndex(int nIndex) {
        this.nIndex = nIndex;
        return this;
    }

    /**
     * @return the new instance of the element with all specified settings.
     */
    public ElementClass create() {
        applyNIndex();
        try {
            INDIElement existing = this.indiProperty.getElement(name());
            if (existing != null) {
                return clazz.cast(existing);
            }
        } catch (Exception e) {
            LOG.error("existing property problem", e);
        }
        try {
            return clazz.getConstructor(INDIElementBuilder.class).newInstance(this);
        } catch (Exception e) {
            LOG.error("could not instanciate element", e);
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * if the name or the label contains the string "${n}" it will be replaced
     * by the specified index (the last occurence). Same is done with any "${n}"
     * in the label.
     */
    private void applyNIndex() {
        if (name != null && nIndex >= 0) {
            int indexOfIndex = name.lastIndexOf("${n}");
            if (indexOfIndex >= 0) {
                String newName = name.substring(0, indexOfIndex) + Integer.toString(nIndex);
                if (indexOfIndex != name.length() - 4) {
                    newName += name.substring(indexOfIndex + 4);
                }
                this.name = newName;
            }
        }

        if (label != null && nIndex >= 0) {
            int indexOfIndex = label.lastIndexOf("${n}");
            if (indexOfIndex >= 0) {
                String newLabel = label.substring(0, indexOfIndex) + Integer.toString(nIndex);
                if (indexOfIndex != label.length() - 4) {
                    newLabel += label.substring(indexOfIndex + 4);
                }
                this.label = newLabel;
            }
        }
    }

    /**
     * copy all settings from the injection.
     * 
     * @param elem
     *            intection to copy the settings.
     * @return the builder itself.
     */
    public INDIElementBuilder<ElementClass> set(InjectElement elem) {
        this.maximum(elem.maximum());
        this.minimum(elem.minimum());
        if (elem.std() != INDIStandardElement.NONE) {
            this.name(elem.std());
        } else {
            this.name(elem.name());
        }
        this.label(elem.label());
        this.numberFormat(elem.numberFormat());
        this.numberValue(elem.numberValue());
        this.state(elem.state());
        this.step(elem.step());
        this.switchValue(elem.switchValue());
        this.textValue(elem.textValue());
        this.nIndex(elem.nIndex());
        return this;
    }

}
