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

import org.indilib.i4j.INDISexagesimalFormatter;
import org.indilib.i4j.driver.util.INDIElementBuilder;
import org.indilib.i4j.protocol.DefElement;
import org.indilib.i4j.protocol.DefNumber;
import org.indilib.i4j.protocol.OneElement;
import org.indilib.i4j.protocol.OneNumber;

import java.util.Formatter;
import java.util.Locale;

/**
 * A class representing a INDI Number Element.
 * 
 * @author S. Alonso (Zerjillo) [zerjioi at ugr.es]
 */
public class INDINumberElement extends INDIElement {

    /**
     * Serialization id.
     */
    private static final long serialVersionUID = 3955841862567283769L;

    /**
     * The current value of this Number Element.
     */
    private double value;

    /**
     * The number format of this Number Element.
     */
    private String numberFormat;

    /**
     * The minimum value for this Number Element.
     */
    private double min;

    /**
     * The maximum value for this Number Element.
     */
    private double max;

    /**
     * The step for this Number Element.
     */
    private double step;

    /**
     * A formatter used to parse and format the values.
     */
    private INDISexagesimalFormatter sFormatter;

    /**
     * Constructs an instance of a <code>INDINumberElement</code>. Using the
     * settings from the builder.
     * 
     * @param builder
     *            the builder with all the settings.
     */
    public INDINumberElement(INDIElementBuilder<INDINumberElement> builder) {
        super(builder);
        setNumberFormat(builder.numberFormat());
        min = builder.minimum();
        max = builder.maximum();
        step = builder.step();
        setValueAsdouble(builder.numberValue());
    }

    @Override
    public INDINumberProperty getProperty() {
        return (INDINumberProperty) super.getProperty();
    }

    /**
     * Set the number format for this Number Element.
     * 
     * @param newNumberFormat
     *            The new number format. if the number format is not correct.
     */
    private void setNumberFormat(String newNumberFormat) {
        newNumberFormat = newNumberFormat.trim();

        if (!newNumberFormat.startsWith("%")) {
            throw new IllegalArgumentException("Number format not starting with %\n");
        }

        if (!newNumberFormat.endsWith("f") && !newNumberFormat.endsWith("e") && !newNumberFormat.endsWith("E") && !newNumberFormat.endsWith("g")
                && !newNumberFormat.endsWith("G") && !newNumberFormat.endsWith("m")) {
            throw new IllegalArgumentException("Number format not recognized%\n");
        }

        if (newNumberFormat.endsWith("m")) {
            sFormatter = new INDISexagesimalFormatter(newNumberFormat);
        }

        numberFormat = newNumberFormat;
    }

    /**
     * Gets the maximum for this Number Element.
     * 
     * @return The maximum for this Number Element.
     */
    public double getMax() {
        return max;
    }

    /**
     * Gets the maximum for this Number Element formated as a String according
     * to the number format.
     * 
     * @return The maximum for this Number Element formatted as a String.
     */
    public String getMaxAsString() {
        return getNumberAsString(getMax());
    }

    /**
     * Gets the minimum for this Number Element.
     * 
     * @return The minimum for this Number Element.
     */
    public double getMin() {
        return min;
    }

    /**
     * Gets the minimum for this Number Element formated as a String according
     * to the number format.
     * 
     * @return The minimum for this Number Element formatted as a String.
     */
    public String getMinAsString() {
        return getNumberAsString(getMin());
    }

    /**
     * Gets the number format of this Number Element.
     * 
     * @return the number format of this Number Element.
     */
    public String getNumberFormat() {
        return numberFormat;
    }

    /**
     * Gets the step for this Number Element.
     * 
     * @return The step for this Number Element.
     */
    public double getStep() {
        return step;
    }

    /**
     * Gets the step for this Number Element formated as a String according to
     * the number format.
     * 
     * @return The step for this Number Element formatted as a String.
     */
    public String getStepAsString() {
        return getNumberAsString(getStep());
    }

    /**
     * Gets the value of this Number Element formated as a String according to
     * the number format.
     * 
     * @return The value of this Number Element formatted as a String.
     */
    public String getValueAsString() {
        return getNumberAsString(getValue());
    }

    /**
     * Returns a number formatted according to the Number Format of this Number
     * Element.
     * 
     * @param number
     *            the number to be formatted.
     * @return the number formatted according to the Number Format of this
     *         Number Element.
     */
    private String getNumberAsString(double number) {
        String aux;

        if (numberFormat.endsWith("m")) {
            aux = sFormatter.format(number);
        } else {
            Formatter formatter = new Formatter(Locale.US);
            aux = formatter.format(getNumberFormat(), number).toString();
            formatter.close();
        }

        return aux;
    }

    @Override
    public Double getValue() {
        return value;
    }

    /**
     * @return the value as an primitiv int.
     */
    public int getIntValue() {
        return (int) Math.round(value);
    }

    @Override
    public void setValue(Object newValue) {
        if (newValue instanceof String) {
            setValueAsString((String) newValue);
        } else if (newValue instanceof Double) {
            setValueAsdouble(((Double) newValue).doubleValue());
        } else if (newValue instanceof Integer) {
            setValueAsdouble(((Integer) newValue).doubleValue());
        } else if (newValue instanceof Float) {
            setValueAsdouble(((Float) newValue).doubleValue());
        } else {
            throw new IllegalArgumentException("Value for a Number Element must be a String or a Double");
        }
    }

    /**
     * A convenience method to set the value represented by a String.
     * 
     * @param valueS
     *            The value if it is not a value within the limits.
     */
    private void setValueAsString(String valueS) {
        value = parseNumber(valueS);

        if (value < min || value > max) {
            throw new IllegalArgumentException(getName() + " ; " + "Number (" + valueS + ") not in range [" + min + ", " + max + "]");
        }
    }

    /**
     * A covenience method to set the value represented by a double.
     * 
     * @param doubleValue
     *            The value if it is not a value within the limits.
     */
    private void setValueAsdouble(double doubleValue) {
        value = doubleValue;

        if (doubleValue < min || doubleValue > max) {
            throw new IllegalArgumentException(getName() + " ; " + "Number (" + doubleValue + ") not in range [" + min + ", " + max + "]");
        }
    }

    /**
     * Parses a number according to the Number Format of this Number Element.
     * 
     * @param number
     *            The number to be parsed.
     * @return the parsed number
     * @throw IllegalArgumentException if the <code>number</code> is not
     *        correctly formatted.
     */
    private double parseNumber(String number) {
        double res;

        if (numberFormat.endsWith("m")) {
            res = sFormatter.parseSexagesimal(number);
        } else {
            try {
                res = Double.parseDouble(number);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Number value not correct");
            }
        }

        return res;
    }

    @Override
    public OneElement<?> getXMLOneElement(boolean includeMinMaxStep) {
        OneNumber result = new OneNumber().setName(getName()).setTextContent(Double.toString(value));
        if (includeMinMaxStep) {
            return result.setMin(Double.toString(min)).setMax(Double.toString(max));
        }
        return result;
    }

    @Override
    public String getNameAndValueAsString() {
        return getName() + " - " + this.getValueAsString();
    }

    @Override
    protected DefElement<?> getXMLDefElement() {
        return new DefNumber().setName(getName()).setLabel(getLabel()).setFormat(numberFormat)//
                .setMin(Double.toString(min)).setMax(Double.toString(max)).setStep(Double.toString(step)).setTextContent(Double.toString(value));
    }

    @Override
    public Object parseOneValue(OneElement<?> xml) {
        double v = parseNumber(xml.getTextContent().trim());

        if (v < min || v > max) {
            throw new IllegalArgumentException(getName() + " ; " + "Number (" + v + ") not in range [" + min + ", " + max + "]");
        }

        return v;
    }

    /**
     * Set the new minimal value.
     * 
     * @param min
     *            the new value.
     */
    public void setMin(double min) {
        this.min = min;
    }

    /**
     * Set the new maximal value.
     * 
     * @param max
     *            the new value.
     */
    public void setMax(double max) {
        this.max = max;
    }

    /**
     * Set the new step value.
     * 
     * @param step
     *            the new value.
     */
    public void setStep(double step) {
        this.step = step;
    }
}
