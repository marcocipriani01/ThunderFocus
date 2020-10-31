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
import laazotea.indi.Constants.PropertyPermissions;
import laazotea.indi.Constants.PropertyStates;
import laazotea.indi.INDIDateFormat;
import laazotea.indi.INDIException;

/**
 * A class representing a INDI Light Property.
 *
 * @author S. Alonso (Zerjillo) [zerjioi at ugr.es]
 * @version 1.34, November 7, 2013
 */
public class INDILightProperty extends INDIProperty {

  /**
   * Constructs an instance of
   * <code>INDILightProperty</code> with a particular
   * <code>driver</code>,
   * <code>name</code>,
   * <code>label</code>,
   * <code>group</code> and
   * <code>state</code>.
   *
   * @param driver The Driver to which this property is associated.
   * @param name The name of the Property
   * @param label The label of the Property
   * @param group The group of the Property
   * @param state The initial state of the Property
   * @throws IllegalArgumentException
   * @see INDIProperty
   */
  public INDILightProperty(INDIDriver driver, String name, String label, String group, PropertyStates state) throws IllegalArgumentException {
    super(driver, name, label, group, state, PropertyPermissions.RO, 0);
  }

  /**
   * Loads an instance of
   * <code>INDINumberProperty</code> from a file or, if it cannot be loaded,
   * constructs it with a particular
   * <code>driver</code>,
   * <code>name</code>,
   * <code>label</code>,
   * <code>group</code> and
   * <code>state</code>. The property will autosave its status to a file every
   * time that it is changed.
   *
   * @param driver The Driver to which this property is associated.
   * @param name The name of the Property
   * @param label The label of the Property
   * @param group The group of the Property
   * @param state The initial state of the Property
   * @throws IllegalArgumentException
   * @return The loaded light property or a new constructed one if cannot be
   * loaded.
   * @see INDIProperty
   */
  public static INDILightProperty createSaveableLightProperty(INDIDriver driver, String name, String label, String group, PropertyStates state) throws IllegalArgumentException {
    INDILightProperty lp = loadLightProperty(driver, name);

    if (lp == null) {
      lp = new INDILightProperty(driver, name, label, group, state);
      lp.setSaveable(true);
    }

    return lp;
  }

  /**
   * Loads a Light Property from a file.
   *
   * @param driver The Driver to which this property is associated
   * @param name The name of the property
   * @return The loaded light property or <code>null</code> if it could not be
   * loaded.
   */
  private static INDILightProperty loadLightProperty(INDIDriver driver, String name) {
    INDIProperty prop;

    try {
      prop = INDIProperty.loadFromFile(driver, name);
    } catch (INDIException e) {  // Was not correctly loaded
      return null;
    }

    if (!(prop instanceof INDILightProperty)) {
      return null;
    }

    INDILightProperty lp = (INDILightProperty)prop;
    lp.setSaveable(true);
    return lp;
  }

  /**
   * Constructs an instance of
   * <code>INDILightProperty</code> with a particular
   * <code>driver</code>,
   * <code>name</code>,
   * <code>label</code> and
   * <code>state</code>. The group will be the default one.
   *
   * @param driver The Driver to which this property is associated.
   * @param name The name of the Property
   * @param label The label of the Property
   * @param state The initial state of the Property
   * @throws IllegalArgumentException
   * @see INDIProperty
   */
  public INDILightProperty(INDIDriver driver, String name, String label, PropertyStates state) throws IllegalArgumentException {
    super(driver, name, label, null, state, PropertyPermissions.RO, 0);
  }

  /**
   * Loads an instance of
   * <code>INDINumberProperty</code> from a file or, if it cannot be loaded,
   * constructs it with a particular
   * <code>driver</code>,
   * <code>name</code>,
   * <code>label</code> and
   * <code>state</code>. The property will autosave its status to a file every
   * time that it is changed.
   *
   * @param driver The Driver to which this property is associated.
   * @param name The name of the Property
   * @param label The label of the Property
   * @param state The initial state of the Property
   * @throws IllegalArgumentException
   * @return The loaded light property or a new constructed one if cannot be
   * loaded.
   * @see INDIProperty
   */
  public static INDILightProperty createSaveableLightProperty(INDIDriver driver, String name, String label, PropertyStates state) throws IllegalArgumentException {
    INDILightProperty lp = loadLightProperty(driver, name);

    if (lp == null) {
      lp = new INDILightProperty(driver, name, label, state);
      lp.setSaveable(true);
    }

    return lp;
  }

  /**
   * Constructs an instance of
   * <code>INDILightProperty</code> with a particular
   * <code>driver</code>,
   * <code>name</code>,
   * <code>label</code> and
   * <code>state</code>. The group will be the default one and the label equal
   * to its
   * <code>name</code>.
   *
   * @param driver The Driver to which this property is associated.
   * @param name The name of the Property
   * @param state The initial state of the Property
   * @throws IllegalArgumentException
   * @see INDIProperty
   */
  public INDILightProperty(INDIDriver driver, String name, PropertyStates state) throws IllegalArgumentException {
    super(driver, name, null, null, state, PropertyPermissions.RO, 0);
  }

  /**
   * Loads an instance of
   * <code>INDINumberProperty</code> from a file or, if it cannot be loaded,
   * constructs it with a particular
   * <code>driver</code>,
   * <code>name</code> and
   * <code>state</code>. The property will autosave its status to a file every
   * time that it is changed.
   *
   * @param driver The Driver to which this property is associated.
   * @param name The name of the Property
   * @param state The initial state of the Property
   * @throws IllegalArgumentException
   * @return The loaded light property or a new constructed one if cannot be
   * loaded.
   * @see INDIProperty
   */
  public static INDILightProperty createSaveableLightProperty(INDIDriver driver, String name, PropertyStates state) throws IllegalArgumentException {
    INDILightProperty lp = loadLightProperty(driver, name);

    if (lp == null) {
      lp = new INDILightProperty(driver, name, state);
      lp.setSaveable(true);
    }

    return lp;
  }

  @Override
  public INDILightElement getElement(String name) {
    return (INDILightElement)super.getElement(name);
  }

  @Override
  protected String getXMLPropertyDefinitionInit() {
    String xml = "<defLightVector device=\"" + getDriver().getName() + "\" name=\"" + getName() + "\" label=\"" + getLabel() + "\" group=\"" + getGroup() + "\" state=\"" + Constants.getPropertyStateAsString(getState()) + "\" timestamp=\"" + INDIDateFormat.getCurrentTimestamp() + "\">";

    return xml;
  }

  @Override
  protected String getXMLPropertyDefinitionInit(String message) {
    String xml = "<defLightVector device=\"" + getDriver().getName() + "\" name=\"" + getName() + "\" label=\"" + getLabel() + "\" group=\"" + getGroup() + "\" state=\"" + Constants.getPropertyStateAsString(getState()) + "\" timestamp=\"" + INDIDateFormat.getCurrentTimestamp() + "\" message=\"" + message + "\">";

    return xml;
  }

  @Override
  protected String getXMLPropertyDefinitionEnd() {
    String xml = "</defLightVector>";

    return xml;
  }

  @Override
  protected String getXMLPropertySetInit() {
    String xml = "<setLightVector device=\"" + getDriver().getName() + "\" name=\"" + getName() + "\" state=\"" + Constants.getPropertyStateAsString(getState()) + "\" timestamp=\"" + INDIDateFormat.getCurrentTimestamp() + "\">";

    return xml;
  }

  @Override
  protected String getXMLPropertySetInit(String message) {
    String xml = "<setLightVector device=\"" + getDriver().getName() + "\" name=\"" + getName() + "\" state=\"" + Constants.getPropertyStateAsString(getState()) + "\" timestamp=\"" + INDIDateFormat.getCurrentTimestamp() + "\" message=\"" + message + "\">";

    return xml;
  }

  @Override
  protected String getXMLPropertySetEnd() {
    String xml = "</setLightVector>";

    return xml;
  }
}
