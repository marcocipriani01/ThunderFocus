/*
 *  This file is part of INDI for Java Driver.
 * 
 *  INDI for Java Driver is free software: you can redistribute it
 *  and/or modify it under the terms of the GNU General Public License 
 *  as published by the Free Software Foundation, either version 3 of 
 *  the License, or (at your option) any later version.
 * 
 *  INDI for Java Driver is distributed in the hope that it will be
 *  useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 *  of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with INDI for Java Driver.  If not, see 
 *  <http://www.gnu.org/licenses/>.
 */
package laazotea.indi.driver;

import java.util.Formatter;
import java.util.Locale;
import laazotea.indi.INDISexagesimalFormatter;
import org.w3c.dom.Element;

/**
 * A class representing a INDI Number Element.
 *
 * @author S. Alonso (Zerjillo) [zerjioi at ugr.es]
 * @version 1.34, November 8, 2013
 */
public class INDINumberElement extends INDIElement {

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
   * Constructs an instance of a
   * <code>INDINumberElement</code> with a
   * <code>name</code>, a
   * <code>label</code>, a initial
   * <code>value</code>, a
   * <code>minimum</code>, a
   * <code>maximum</code>, a
   * <code>step</code> and a
   * <code>numberFormat</code>.
   *
   * @param property The Property to which this Element belongs.
   * @param name The name of the Element.
   * @param label The label of the Element.
   * @param value The initial value of the Element.
   * @param minimum The minimum of the Element.
   * @param maximum The maximum of the Element.
   * @param step The step of the Element.
   * @param numberFormat The number format of the element.
   * @throws IllegalArgumentException if any of the <code>value</code>,
   * <code>minimum</code>, <code>maximum</code> or <code>step</code> are not
   * well formatted or if the initial Value is not in [minimum, maximum].
   */
  public INDINumberElement(INDINumberProperty property, String name, String label, String value, String minimum, String maximum, String step, String numberFormat) throws IllegalArgumentException {
    super(property, name, label);

    setNumberFormat(numberFormat);

    setMin(minimum);

    setMax(maximum);

    setStep(step);

    setValueAsString(value);
  }

  /**
   * Constructs an instance of a
   * <code>INDINumberElement</code> with a
   * <code>name</code>, a
   * <code>label</code>, a initial
   * <code>value</code>, a
   * <code>minimum</code>, a
   * <code>maximum</code>, a
   * <code>step</code> and a
   * <code>numberFormat</code>.
   *
   * @param property The Property to which this Element belongs.
   * @param name The name of the Element.
   * @param label The label of the Element.
   * @param value The initial value of the Element.
   * @param minimum The minimum of the Element.
   * @param maximum The maximum of the Element.
   * @param step The step of the Element.
   * @param numberFormat The number format of the element.
   * @throws IllegalArgumentException if the initial Value is not in [minimum,
   * maximum].
   */
  public INDINumberElement(INDINumberProperty property, String name, String label, double value, double minimum, double maximum, double step, String numberFormat) throws IllegalArgumentException {
    super(property, name, label);

    setNumberFormat(numberFormat);

    this.min = minimum;

    this.max = maximum;

    this.step = step;

    setValueAsdouble(value);
  }

  /**
   * Constructs an instance of a
   * <code>INDINumberElement</code> with a
   * <code>name</code>, a initial
   * <code>value</code>, a
   * <code>minimum</code>, a
   * <code>maximum</code>, a
   * <code>step</code> and a
   * <code>numberFormat</code>. The label of the Element will be a copy of the
   * <code>name</code>.
   *
   * @param property The Property to which this Element belongs.
   * @param name The name of the Element.
   * @param value The initial value of the Element.
   * @param minimum The minimum of the Element.
   * @param maximum The maximum of the Element.
   * @param step The step of the Element.
   * @param numberFormat The number format of the element.
   * @throws IllegalArgumentException if any of the <code>value</code>,
   * <code>minimum</code>, <code>maximum</code> or <code>step</code> are not
   * well formatted or if the initial Value is not in [minimum, maximum].
   */
  public INDINumberElement(INDINumberProperty property, String name, String value, String minimum, String maximum, String step, String numberFormat) throws IllegalArgumentException {
    super(property, name);

    setNumberFormat(numberFormat);

    setMin(minimum);

    setMax(maximum);

    setStep(step);

    setValueAsString(value);
  }

  /**
   * Constructs an instance of a
   * <code>INDINumberElement</code> with a
   * <code>name</code>, a initial
   * <code>value</code>, a
   * <code>minimum</code>, a
   * <code>maximum</code>, a
   * <code>step</code> and a
   * <code>numberFormat</code>. The label of the Element will be a copy of the
   * <code>name</code>.
   *
   * @param property The Property to which this Element belongs.
   * @param name The name of the Element.
   * @param value The initial value of the Element.
   * @param minimum The minimum of the Element.
   * @param maximum The maximum of the Element.
   * @param step The step of the Element.
   * @param numberFormat The number format of the element.
   * @throws IllegalArgumentException if the initial Value is not in [minimum,
   * maximum].
   */
  public INDINumberElement(INDINumberProperty property, String name, double value, double minimum, double maximum, double step, String numberFormat) throws IllegalArgumentException {
    super(property, name);

    setNumberFormat(numberFormat);

    this.min = minimum;

    this.max = maximum;

    this.step = step;

    setValueAsdouble(value);
  }

  @Override
  public INDINumberProperty getProperty() {
    return (INDINumberProperty)super.getProperty();
  }

  /**
   * Set the number format for this Number Element.
   *
   * @param newNumberFormat The new number format.
   * @throws IllegalArgumentException if the number format is not correct.
   */
  private void setNumberFormat(String newNumberFormat) throws IllegalArgumentException {
    newNumberFormat = newNumberFormat.trim();

    if (!newNumberFormat.startsWith("%")) {
      throw new IllegalArgumentException("Number format not starting with %\n");
    }

    if ((!newNumberFormat.endsWith("f")) && (!newNumberFormat.endsWith("e")) && (!newNumberFormat.endsWith("E")) && (!newNumberFormat.endsWith("g")) && (!newNumberFormat.endsWith("G")) && (!newNumberFormat.endsWith("m"))) {
      throw new IllegalArgumentException("Number format not recognized%\n");
    }

    if (newNumberFormat.endsWith("m")) {
      sFormatter = new INDISexagesimalFormatter(newNumberFormat);
    }

    this.numberFormat = newNumberFormat;
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
   * Gets the maximum for this Number Element formated as a String according to
   * the number format.
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
   * Gets the minimum for this Number Element formated as a String according to
   * the number format.
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
   * Gets the step for this Number Element formated as a String according to the
   * number format.
   *
   * @return The step for this Number Element formatted as a String.
   */
  public String getStepAsString() {
    return getNumberAsString(getStep());
  }

  /**
   * Gets the value of this Number Element formated as a String according to the
   * number format.
   *
   * @return The value of this Number Element formatted as a String.
   */
  public String getValueAsString() {
    return getNumberAsString((Double)getValue());
  }

  /**
   * Returns a number formatted according to the Number Format of this Number
   * Element.
   *
   * @param number the number to be formatted.
   * @return the number formatted according to the Number Format of this Number
   * Element.
   */
  private String getNumberAsString(double number) {
    String aux;

    if (numberFormat.endsWith("m")) {
      aux = sFormatter.format(number);
    } else {
      Formatter formatter = new Formatter(Locale.US);
      aux = formatter.format(getNumberFormat(), number).toString();
    }

    return aux;
  }

  @Override
  public Double getValue() {
    return value;
  }

  @Override
  public void setValue(Object newValue) throws IllegalArgumentException {
    if (newValue instanceof String) {
      setValueAsString((String)newValue);
    } else if (newValue instanceof Double) {
      setValueAsdouble(((Double)newValue).doubleValue());
    } else {
      throw new IllegalArgumentException("Value for a Number Element must be a String or a Double");
    }
  }

  /**
   * Sets the maximum of this Number Element
   *
   * @param maxS A String with the maximum value of this Number Element
   */
  private void setMax(String maxS) {
    max = parseNumber(maxS);
  }

  /**
   * Sets the minimum of this Number Element
   *
   * @param maxS A String with the minimum value of this Number Element
   */
  private void setMin(String minS) {
    min = parseNumber(minS);
  }

  /**
   * Sets the step of this Number Element
   *
   * @param maxS A String with the step value of this Number Element
   */
  private void setStep(String stepS) {
    step = parseNumber(stepS);
  }

  /**
   * A convenience method to set the value represented by a String.
   * @param valueS The value
   * @throws IllegalArgumentException if it is not a value within the limits.
   */
  private void setValueAsString(String valueS) throws IllegalArgumentException {
    value = parseNumber(valueS);

    if ((value < min) || (value > max)) {
      throw new IllegalArgumentException(getName() + " ; " + "Number (" + valueS + ") not in range [" + min + ", " + max + "]");
    }
  }

  /**
   * A covenience method to set the value represented by a double
   * @param value The value
   * @throws IllegalArgumentException if it is not a value within the limits.
   */
  private void setValueAsdouble(double value) throws IllegalArgumentException {
    this.value = value;

    if ((value < min) || (value > max)) {
      throw new IllegalArgumentException(getName() + " ; " + "Number (" + value + ") not in range [" + min + ", " + max + "]");
    }
  }

  /**
   * Parses a number according to the Number Format of this Number Element.
   *
   * @param number The number to be parsed.
   * @return the parsed number
   * @throw IllegalArgumentException if the <code>number</code> is not correctly
   * formatted.
   */
  private double parseNumber(String number) throws IllegalArgumentException {
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
  public String getXMLOneElement() {
    String xml = "<oneNumber name=\"" + this.getName() + "\">" + value + "</oneNumber>";

    return xml;
  }

  @Override
  public String getNameAndValueAsString() {
    return getName() + " - " + this.getValueAsString();
  }

  @Override
  protected String getXMLDefElement() {
    String xml = "<defNumber name=\"" + this.getName() + "\" label=\"" + getLabel() + "\" format=\"" + numberFormat + "\" min=\"" + min + "\" max=\"" + max + "\" step=\"" + step + "\">" + value + "</defNumber>";

    return xml;
  }

  @Override
  public Object parseOneValue(Element xml) {
    double v = parseNumber(xml.getTextContent().trim());

    if ((v < min) || (v > max)) {
      throw new IllegalArgumentException(getName() + " ; " + "Number (" + v + ") not in range [" + min + ", " + max + "]");
    }

    return v;
  }
}
