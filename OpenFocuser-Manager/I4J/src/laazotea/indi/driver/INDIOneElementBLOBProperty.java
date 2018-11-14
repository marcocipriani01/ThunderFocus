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

import laazotea.indi.Constants.PropertyPermissions;
import laazotea.indi.Constants.PropertyStates;
import laazotea.indi.INDIBLOBValue;
import laazotea.indi.INDIException;

/**
 * A class representing a INDI BLOB Property with only one BLOB Element (with
 * the same name and label of the Property).
 *
 * @author S. Alonso (Zerjillo) [zerjioi at ugr.es]
 * @version 1.36, November 23, 2013
 */
public class INDIOneElementBLOBProperty extends INDIBLOBProperty {

  /**
   * The BLOB Element
   */
  private INDIBLOBElement element;

  /**
   * Constructs an instance of
   * <code>INDIOneElementBLOBProperty</code> with a particular
   * <code>driver</code>,
   * <code>name</code>,
   * <code>label</code>,
   * <code>group</code>,
   * <code>state</code>,
   * <code>permission</code>,
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
   * @see INDIBLOBProperty
   */
  public INDIOneElementBLOBProperty(INDIDriver driver, String name, String label, String group, PropertyStates state, PropertyPermissions permission, int timeout) throws IllegalArgumentException {
    super(driver, name, label, group, state, permission, timeout);

    element = new INDIBLOBElement(this, name, label);
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
   * @see INDIBLOBProperty
   */
  public static INDIOneElementBLOBProperty createSaveableOneElementBLOBProperty(INDIDriver driver, String name, String label, String group, PropertyStates state, PropertyPermissions permission, int timeout) throws IllegalArgumentException {
    INDIOneElementBLOBProperty bp = loadOneElementBLOBProperty(driver, name);

    if (bp == null) {
      bp = new INDIOneElementBLOBProperty(driver, name, label, group, state, permission, timeout);
      bp.setSaveable(true);
    }

    return bp;
  }

  /**
   * Loads a One Element BLOB Property from a file.
   *
   * @param driver The Driver to which this property is associated
   * @param name The name of the property
   * @return The loaded BLOB property or <code>null</code> if it could not be
   * loaded.
   */
  private static INDIOneElementBLOBProperty loadOneElementBLOBProperty(INDIDriver driver, String name) {
    INDIProperty prop;

    try {
      prop = INDIProperty.loadFromFile(driver, name);
    } catch (INDIException e) {  // Was not correctly loaded
      return null;
    }

    if (!(prop instanceof INDIOneElementBLOBProperty)) {
      return null;
    }

    INDIOneElementBLOBProperty tp = (INDIOneElementBLOBProperty)prop;
    tp.setSaveable(true);
    return tp;
  }

  /**
   * Constructs an instance of
   * <code>INDIOneElementBLOBProperty</code> with a particular
   * <code>driver</code>,
   * <code>name</code>,
   * <code>label</code>,
   * <code>group</code>,
   * <code>state</code>,
   * <code>permission</code>.
   *
   * @param driver The Driver to which this property is associated
   * @param name The name of the Property
   * @param label The label of the Property
   * @param group The group of the Property
   * @param state The initial state of the Property
   * @param permission The permission of the Property
   * @throws IllegalArgumentException
   * @see INDIBLOBProperty
   */
  public INDIOneElementBLOBProperty(INDIDriver driver, String name, String label, String group, PropertyStates state, PropertyPermissions permission) throws IllegalArgumentException {
    super(driver, name, label, group, state, permission);

    element = new INDIBLOBElement(this, name, label);
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
   * @return The loaded text property or a new constructed one if cannot be
   * loaded.
   * @see INDIBLOBProperty
   */
  public static INDIOneElementBLOBProperty createSaveableOneElementBLOBProperty(INDIDriver driver, String name, String label, String group, PropertyStates state, PropertyPermissions permission) throws IllegalArgumentException {
    INDIOneElementBLOBProperty bp = loadOneElementBLOBProperty(driver, name);

    if (bp == null) {
      bp = new INDIOneElementBLOBProperty(driver, name, label, group, state, permission);
      bp.setSaveable(true);
    }

    return bp;
  }

  /**
   * Constructs an instance of
   * <code>INDIOneElementBLOBProperty</code> with a particular
   * <code>driver</code>,
   * <code>name</code>,
   * <code>label</code>,
   * <code>state</code>,
   * <code>permission</code>.
   *
   * @param driver The Driver to which this property is associated
   * @param name The name of the Property
   * @param label The label of the Property
   * @param state The initial state of the Property
   * @param permission The permission of the Property
   * @throws IllegalArgumentException
   * @see INDIBLOBProperty
   */
  public INDIOneElementBLOBProperty(INDIDriver driver, String name, String label, PropertyStates state, PropertyPermissions permission) throws IllegalArgumentException {
    super(driver, name, label, state, permission);

    element = new INDIBLOBElement(this, name, label);
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
   * @return The loaded text property or a new constructed one if cannot be
   * loaded.
   * @see INDIBLOBProperty
   */
  public static INDIOneElementBLOBProperty createSaveableOneElementBLOBProperty(INDIDriver driver, String name, String label, PropertyStates state, PropertyPermissions permission) throws IllegalArgumentException {
    INDIOneElementBLOBProperty bp = loadOneElementBLOBProperty(driver, name);

    if (bp == null) {
      bp = new INDIOneElementBLOBProperty(driver, name, label, state, permission);
      bp.setSaveable(true);
    }

    return bp;
  }

  /**
   * Constructs an instance of
   * <code>INDIOneElementBLOBProperty</code> with a particular
   * <code>driver</code>,
   * <code>name</code>,
   * <code>state</code>,
   * <code>permission</code>.
   *
   * @param driver The Driver to which this property is associated
   * @param name The name of the Property
   * @param state The initial state of the Property
   * @param permission The permission of the Property
   * @throws IllegalArgumentException
   * @see INDIBLOBProperty
   */
  public INDIOneElementBLOBProperty(INDIDriver driver, String name, PropertyStates state, PropertyPermissions permission) throws IllegalArgumentException {
    super(driver, name, state, permission);

    element = new INDIBLOBElement(this, name);
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
   * @return The loaded text property or a new constructed one if cannot be
   * loaded.
   * @see INDIBLOBProperty
   */
  public static INDIOneElementBLOBProperty createSaveableOneElementBLOBProperty(INDIDriver driver, String name, PropertyStates state, PropertyPermissions permission) throws IllegalArgumentException {
    INDIOneElementBLOBProperty bp = loadOneElementBLOBProperty(driver, name);

    if (bp == null) {
      bp = new INDIOneElementBLOBProperty(driver, name, state, permission);
      bp.setSaveable(true);
    }

    return bp;
  }

  /**
   * Gets the value of the Element.
   *
   * @return The Value of the Element.
   * @see INDIBLOBElement#getValue()
   */
  public INDIBLOBValue getValue() {
    return element.getValue();
  }

  /**
   * Sets the value of the Element.
   *
   * @param newValue The new value for the Element
   * @throws IllegalArgumentException
   * @see INDIBLOBElement#setValue(Object newValue)
   */
  public void setValue(Object newValue) throws IllegalArgumentException {
    element.setValue(newValue);
  }
}
