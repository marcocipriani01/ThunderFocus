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
 * A class representing a INDI Text Property.
 *
 * @author S. Alonso (Zerjillo) [zerjioi at ugr.es]
 * @version 1.34, November 7, 2013
 */
public class INDITextProperty extends INDIProperty {

  /**
   * Constructs an instance of
   * <code>INDITextProperty</code> with a particular
   * <code>driver</code>,
   * <code>name</code>,
   * <code>label</code>,
   * <code>group</code>,
   * <code>state</code>,
   * <code>permission</code> and
   * <code>timeout</code>.
   *
   * @param driver The Driver to which this property is associated
   * @param name The name of the Property
   * @param label The label of the Property
   * @param group The group of the Property
   * @param state The initial state of the Property
   * @param permission The permission of the Property
   * @param timeout The timeout of the Property
   * @throws IllegalArgumentException
   * @see INDIProperty
   */
  public INDITextProperty(INDIDriver driver, String name, String label, String group, PropertyStates state, PropertyPermissions permission, int timeout) throws IllegalArgumentException {
    super(driver, name, label, group, state, permission, timeout);
  }

  /**
   * Loads an instance of from a file or, if it cannot be loaded, constructs it
   * with a particular
   * <code>driver</code>,
   * <code>name</code>,
   * <code>label</code>,
   * <code>group</code>,
   * <code>state</code>,
   * <code>permission</code> and
   * <code>timeout</code>. The property will autosave its status to a file every
   * time that it is changed.
   *
   * @param driver The Driver to which this property is associated
   * @param name The name of the Property
   * @param label The label of the Property
   * @param group The group of the Property
   * @param state The initial state of the Property
   * @param permission The permission of the Property
   * @param timeout The timeout of the Property
   * @throws IllegalArgumentException
   * @return The loaded text property or a new constructed one if cannot be
   * loaded.
   * @see INDIProperty
   */
  public static INDITextProperty createSaveableTextProperty(INDIDriver driver, String name, String label, String group, PropertyStates state, PropertyPermissions permission, int timeout) throws IllegalArgumentException {
    INDITextProperty tp = loadTextProperty(driver, name);

    if (tp == null) {
      tp = new INDITextProperty(driver, name, label, group, state, permission, timeout);
      tp.setSaveable(true);
    }

    return tp;
  }

  /**
   * Loads a Text Property from a file.
   *
   * @param driver The Driver to which this property is associated
   * @param name The name of the property
   * @return The loaded text property or <code>null</code> if it could not be
   * loaded.
   */
  private static INDITextProperty loadTextProperty(INDIDriver driver, String name) {
    INDIProperty prop;

    try {
      prop = INDIProperty.loadFromFile(driver, name);
    } catch (INDIException e) {  // Was not correctly loaded
      return null;
    }

    if (!(prop instanceof INDITextProperty)) {
      return null;
    }

    INDITextProperty tp = (INDITextProperty)prop;
    tp.setSaveable(true);
    return tp;
  }

  /**
   * Constructs an instance of
   * <code>INDITextProperty</code> with a particular
   * <code>driver</code>,
   * <code>name</code>,
   * <code>label</code>,
   * <code>group</code>,
   * <code>state</code>,
   * <code>permission</code> and a 0 timeout.
   *
   * @param driver The Driver to which this property is associated.
   * @param name The name of the Property
   * @param label The label of the Property
   * @param group The group of the Property
   * @param state The initial state of the Property
   * @param permission The permission of the Property
   * @throws IllegalArgumentException
   * @see INDIProperty
   */
  public INDITextProperty(INDIDriver driver, String name, String label, String group, PropertyStates state, PropertyPermissions permission) throws IllegalArgumentException {
    super(driver, name, label, group, state, permission, 0);
  }

  /**
   * Loads an instance of from a file or, if it cannot be loaded, constructs it
   * with a particular
   * <code>driver</code>,
   * <code>name</code>,
   * <code>label</code>,
   * <code>group</code>,
   * <code>state</code>,
   * <code>permission</code>. The property will autosave its status to a file
   * every time that it is changed.
   *
   * @param driver The Driver to which this property is associated
   * @param name The name of the Property
   * @param label The label of the Property
   * @param group The group of the Property
   * @param state The initial state of the Property
   * @param permission The permission of the Property
   * @throws IllegalArgumentException
   * @return The loaded text property or a new constructed one if canot be
   * loaded.
   * @see INDIProperty
   */
  public static INDITextProperty createSaveableTextProperty(INDIDriver driver, String name, String label, String group, PropertyStates state, PropertyPermissions permission) throws IllegalArgumentException {
    INDITextProperty tp = loadTextProperty(driver, name);

    if (tp == null) {
      tp = new INDITextProperty(driver, name, label, group, state, permission);
      tp.setSaveable(true);
    }

    return tp;
  }

  /**
   * Constructs an instance of
   * <code>INDITextProperty</code> with a particular
   * <code>driver</code>,
   * <code>name</code>,
   * <code>label</code>,
   * <code>state</code>,
   * <code>permission</code> and a 0 timeout and default group.
   *
   * @param driver The Driver to which this property is associated.
   * @param name The name of the Property
   * @param label The label of the Property
   * @param state The initial state of the Property
   * @param permission The permission of the Property
   * @throws IllegalArgumentException
   * @see INDIProperty
   */
  public INDITextProperty(INDIDriver driver, String name, String label, PropertyStates state, PropertyPermissions permission) throws IllegalArgumentException {
    super(driver, name, label, null, state, permission, 0);
  }

  /**
   * Loads an instance of from a file or, if it cannot be loaded, constructs it
   * with a particular
   * <code>driver</code>,
   * <code>name</code>,
   * <code>label</code>,
   * <code>state</code>,
   * <code>permission</code>. The property will autosave its status to a file
   * every time that it is changed.
   *
   * @param driver The Driver to which this property is associated
   * @param name The name of the Property
   * @param label The label of the Property
   * @param state The initial state of the Property
   * @param permission The permission of the Property
   * @throws IllegalArgumentException
   * @return The loaded text property or a new constructed one if canot be
   * loaded.
   * @see INDIProperty
   */
  public static INDITextProperty createSaveableTextProperty(INDIDriver driver, String name, String label, PropertyStates state, PropertyPermissions permission) throws IllegalArgumentException {
    INDITextProperty tp = loadTextProperty(driver, name);

    if (tp == null) {
      tp = new INDITextProperty(driver, name, label, state, permission);
      tp.setSaveable(true);
    }

    return tp;
  }

  /**
   * Constructs an instance of
   * <code>INDITextProperty</code> with a particular
   * <code>driver</code>,
   * <code>name</code>,
   * <code>state</code>,
   * <code>permission</code> and a 0 timeout, a default group and a label equal
   * to its
   * <code>name</code>
   *
   * @param driver The Driver to which this property is associated.
   * @param name The name of the Property
   * @param state The initial state of the Property
   * @param permission The permission of the Property
   * @throws IllegalArgumentException
   * @see INDIProperty
   */
  public INDITextProperty(INDIDriver driver, String name, PropertyStates state, PropertyPermissions permission) throws IllegalArgumentException {
    super(driver, name, null, null, state, permission, 0);
  }

  /**
   * Loads an instance of from a file or, if it cannot be loaded, constructs it
   * with a particular
   * <code>driver</code>,
   * <code>name</code>,
   * <code>state</code>,
   * <code>permission</code>. The property will autosave its status to a file
   * every time that it is changed.
   *
   * @param driver The Driver to which this property is associated
   * @param name The name of the Property
   * @param state The initial state of the Property
   * @param permission The permission of the Property
   * @throws IllegalArgumentException
   * @return The loaded text property or a new constructed one if canot be
   * loaded.
   * @see INDIProperty
   */
  public static INDITextProperty createSaveableTextProperty(INDIDriver driver, String name, PropertyStates state, PropertyPermissions permission) throws IllegalArgumentException {
    INDITextProperty tp = loadTextProperty(driver, name);

    if (tp == null) {
      tp = new INDITextProperty(driver, name, state, permission);
      tp.setSaveable(true);
    }

    return tp;
  }

  @Override
  public INDITextElement getElement(String name) {
    return (INDITextElement)super.getElement(name);
  }

  @Override
  protected String getXMLPropertyDefinitionInit() {
    String xml = "<defTextVector device=\"" + getDriver().getName() + "\" name=\"" + getName() + "\" label=\"" + getLabel() + "\" group=\"" + getGroup() + "\" state=\"" + Constants.getPropertyStateAsString(getState()) + "\" perm=\"" + Constants.getPropertyPermissionAsString(getPermission()) + "\" timeout=\"" + getTimeout() + "\" timestamp=\"" + INDIDateFormat.getCurrentTimestamp() + "\">";

    return xml;
  }

  @Override
  protected String getXMLPropertyDefinitionInit(String message) {
    String xml = "<defTextVector device=\"" + getDriver().getName() + "\" name=\"" + getName() + "\" label=\"" + getLabel() + "\" group=\"" + getGroup() + "\" state=\"" + Constants.getPropertyStateAsString(getState()) + "\" perm=\"" + Constants.getPropertyPermissionAsString(getPermission()) + "\" timeout=\"" + getTimeout() + "\" timestamp=\"" + INDIDateFormat.getCurrentTimestamp() + "\" message=\"" + message + "\">";

    return xml;
  }

  @Override
  protected String getXMLPropertyDefinitionEnd() {
    String xml = "</defTextVector>";

    return xml;
  }

  @Override
  protected String getXMLPropertySetInit() {
    String xml = "<setTextVector device=\"" + getDriver().getName() + "\" name=\"" + getName() + "\" state=\"" + Constants.getPropertyStateAsString(getState()) + "\" timeout=\"" + getTimeout() + "\" timestamp=\"" + INDIDateFormat.getCurrentTimestamp() + "\">";

    return xml;
  }

  @Override
  protected String getXMLPropertySetInit(String message) {
    String xml = "<setTextVector device=\"" + getDriver().getName() + "\" name=\"" + getName() + "\" state=\"" + Constants.getPropertyStateAsString(getState()) + "\" timeout=\"" + getTimeout() + "\" timestamp=\"" + INDIDateFormat.getCurrentTimestamp() + "\" message=\"" + message + "\">";

    return xml;
  }

  @Override
  protected String getXMLPropertySetEnd() {
    String xml = "</setTextVector>";

    return xml;
  }
}
