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
import laazotea.indi.Constants.LightStates;
import org.w3c.dom.Element;

/**
 * A class representing a INDI Light Element.
 *
 * @author S. Alonso (Zerjillo) [zerjioi at ugr.es]
 * @version 1.11, March 26, 2012
 */
public class INDILightElement extends INDIElement {

  /**
   * Current State value for this Light Element.
   */
  private LightStates state;

  /**
   * Constructs an instance of a
   * <code>INDILightElement</code> with a
   * <code>name</code>, a
   * <code>label</code> and its initial
   * <code>state</code>.
   *
   * @param property The Property to which this Element belongs.
   * @param name The name of the Element.
   * @param label The label of the Element.
   * @param state The initial state of the Element.
   */
  public INDILightElement(INDILightProperty property, String name, String label, LightStates state) {
    super(property, name, label);

    this.state = state;
  }

  /**
   * Constructs an instance of a
   * <code>INDILightElement</code> with a
   * <code>name</code> and its initial
   * <code>state</code>. The label of the Element will be a copy of the
   * <code>name</code>.
   *
   * @param property The Property to which this Element belongs.
   * @param name The name of the Element.
   * @param state The initial state of the Element.
   * @throws IllegalArgumentException
   */
  public INDILightElement(INDILightProperty property, String name, LightStates state) {
    super(property, name);

    this.state = state;
  }

  @Override
  public INDILightProperty getProperty() {
    return (INDILightProperty)super.getProperty();
  }

  @Override
  public LightStates getValue() {
    return state;
  }

  @Override
  public void setValue(Object newValue) throws IllegalArgumentException {
    LightStates ns = null;
    try {
      ns = (LightStates)newValue;
    } catch (ClassCastException e) {
      throw new IllegalArgumentException("Value for a Light Element must be a INDILightElement.LightStates");
    }

    this.state = ns;
  }

  @Override
  public String getXMLOneElement() {
    String v = Constants.getLightStateAsString(state);

    String xml = "<oneLight name=\"" + this.getName() + "\">" + v + "</oneLight>";

    return xml;
  }

  @Override
  public String getNameAndValueAsString() {
    return getName() + " - " + getValue();
  }

  @Override
  protected String getXMLDefElement() {
    String v = Constants.getLightStateAsString(state);

    String xml = "<defLight name=\"" + this.getName() + "\" label=\"" + getLabel() + "\">" + v + "</defLight>";

    return xml;
  }

  @Override
  public Object parseOneValue(Element xml) {
    return Constants.parseLightState(xml.getTextContent().trim());
  }
}
