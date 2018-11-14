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

import laazotea.indi.Constants;
import laazotea.indi.Constants.SwitchRules;
import laazotea.indi.Constants.SwitchStatus;
import org.w3c.dom.Element;

/**
 * A class representing a INDI Switch Element.
 *
 * @author S. Alonso (Zerjillo) [zerjioi at ugr.es]
 * @version 1.11, March 26, 2012
 */
public class INDISwitchElement extends INDIElement {

  /**
   * Current Status value for this Switch Element.
   */
  private SwitchStatus status;

  /**
   * Constructs an instance of a
   * <code>INDISwitchElement</code> with a
   * <code>name</code>, a
   * <code>label</code> and its initial
   * <code>status</code>.
   *
   * @param property The Property to which this Element belongs.
   * @param name The name of the Element.
   * @param label The label of the Element.
   * @param status The initial status of the Element
   * @throws IllegalArgumentException
   */
  public INDISwitchElement(INDISwitchProperty property, String name, String label, SwitchStatus status) throws IllegalArgumentException {
    super(property, name, label);

    this.status = status;
  }

  /**
   * Constructs an instance of a
   * <code>INDISwitchElement</code> with a
   * <code>name</code>, a
   * <code>label</code> and its initial
   * <code>status</code>. The label of the Element will be a copy of the
   * <code>name</code>.
   *
   * @param property The Property to which this Element belongs.
   * @param name The name of the Element.
   * @param status The initial state of the Element.
   * @throws IllegalArgumentException
   */
  public INDISwitchElement(INDISwitchProperty property, String name, SwitchStatus status) throws IllegalArgumentException {
    super(property, name);

    this.status = status;
  }

  @Override
  public INDISwitchProperty getProperty() {
    return (INDISwitchProperty)super.getProperty();
  }

  @Override
  public SwitchStatus getValue() {
    return status;
  }

  /**
   * Sets the Element value to a new value. This method ensures that if the
   * Switch Property rule is
   * <code>AT_MOST_ONE</code> or
   * <code>ONE_OF_MANY</code> and the new value is
   * <code>ON</code> the other Switch Elements of the property are turn to
   * <code>OFF</code>.
   *
   * @param newValue The new value.
   * @throws IllegalArgumentException If the <code>newValue</code> is not a
   * valid <code>SwitchStatus</code>.
   */
  @Override
  public void setValue(Object newValue) throws IllegalArgumentException {
    SwitchStatus ss = null;
    try {
      ss = (SwitchStatus)newValue;
    } catch (ClassCastException e) {
      throw new IllegalArgumentException("Value for a Switch Element must be a SwitchStatus");
    }

    if (ss == SwitchStatus.ON) {
      INDISwitchProperty p = getProperty();

      if ((p.getRule() == SwitchRules.AT_MOST_ONE) || (p.getRule() == SwitchRules.ONE_OF_MANY)) {  // If only one ON value is allowed in the property, set all of them to OFF
        p.resetAllSwitches();
      }
    }

    this.status = ss;
  }

  @Override
  public String getXMLOneElement() {
    String stat = Constants.getSwitchStatusAsString(status);

    String xml = "<oneSwitch name=\"" + this.getName() + "\">" + stat + "</oneSwitch>";

    return xml;
  }

  @Override
  public String getNameAndValueAsString() {
    return getName() + " - " + getValue();
  }

  @Override
  protected String getXMLDefElement() {
    String stat = Constants.getSwitchStatusAsString(status);

    String xml = "<defSwitch name=\"" + this.getName() + "\" label=\"" + getLabel() + "\">" + stat + "</defSwitch>";

    return xml;
  }

  @Override
  public Object parseOneValue(Element xml) {
    return Constants.parseSwitchStatus(xml.getTextContent().trim());
  }
}
