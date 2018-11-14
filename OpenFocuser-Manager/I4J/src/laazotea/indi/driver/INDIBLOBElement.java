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

import laazotea.indi.INDIBLOBValue;
import org.w3c.dom.Element;

/**
 * A class representing a INDI BLOB Element.
 *
 * @author S. Alonso (Zerjillo) [zerjioi at ugr.es]
 * @version 1.11, March 26, 2012
 */
public class INDIBLOBElement extends INDIElement {

  /**
   * The current value of the BLOB Element
   */
  private INDIBLOBValue value;

  /**
   * Constructs an instance of a
   * <code>INDIBLOBElement</code> with a
   * <code>name</code> and a
   * <code>label</code>.
   *
   * @param property The Property to which this Element belongs.
   * @param name The name of the Element.
   * @param label The label of the Element.
   * @throws IllegalArgumentException
   */
  public INDIBLOBElement(INDIBLOBProperty property, String name, String label) throws IllegalArgumentException {
    super(property, name, label);

    value = new INDIBLOBValue(new byte[0], "");
  }

  /**
   * Constructs an instance of a
   * <code>INDIBLOBElement</code> with a
   * <code>name</code>.
   *
   * @param property The Property to which this Element belongs.
   * @param name The name of the Element.
   * @throws IllegalArgumentException
   */
  public INDIBLOBElement(INDIBLOBProperty property, String name) throws IllegalArgumentException {
    super(property, name);

    value = new INDIBLOBValue(new byte[0], "");
  }

  @Override
  public INDIBLOBProperty getProperty() {
    return (INDIBLOBProperty)super.getProperty();
  }

  @Override
  public INDIBLOBValue getValue() {
    return value;
  }

  @Override
  public void setValue(Object newValue) throws IllegalArgumentException {
    INDIBLOBValue b = null;
    try {
      b = (INDIBLOBValue)newValue;
    } catch (ClassCastException e) {
      throw new IllegalArgumentException("Value for a BLOB Element must be a INDIBLOBValue");
    }

    this.value = b;
  }

  @Override
  public String getXMLOneElement() {
    int size = value.getSize();

    String data = value.getBase64BLOBData();

    String xml = "<oneBLOB name=\"" + this.getName() + "\" size=\"" + size + "\" format=\"" + value.getFormat() + "\">" + data + "</oneBLOB>";

    return xml;
  }

  @Override
  public String getNameAndValueAsString() {
    return getName() + " - BLOB format: " + this.getValue().getFormat() + " - BLOB Size: " + this.getValue().getSize();
  }

  @Override
  protected String getXMLDefElement() {
    String xml = "<defBLOB name=\"" + this.getName() + "\" label=\"" + getLabel() + "\" />";

    return xml;
  }

  @Override
  public Object parseOneValue(Element xml) {
    return new INDIBLOBValue(xml);
  }
}
