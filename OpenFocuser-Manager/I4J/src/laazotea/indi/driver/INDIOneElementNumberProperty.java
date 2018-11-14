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
import laazotea.indi.INDIException;

/**
 * A class representing a INDI Number Property with only one Number Element
 * (with the same name and label of the Property).
 *
 * @author S. Alonso (Zerjillo) [zerjioi at ugr.es]
 * @version 1.37, January 11, 2014
 */
public class INDIOneElementNumberProperty extends INDINumberProperty {

  private INDINumberElement element;

  /**
   * Constructs an instance of
   * <code>INDIOneElementNumberProperty</code> with a particular
   * <code>driver</code>,
   * <code>name</code>,
   * <code>label</code>,
   * <code>group</code>,
   * <code>state</code>,
   * <code>permission</code>,
   * <code>timeout</code>,
   * <code>minimum</code>,
   * <code>maximum</code>,
   * <code>step</code>,
   * <code>format</code> and a initial
   * <code>value</code> for the Element.
   *
   * @param driver The Driver to which this property is associated
   * @param name The name of the Property
   * @param label The label of the Property
   * @param group The group of the Property
   * @param state The initial state of the Property
   * @param permission The permission of the Property
   * @param timeout The timeout of the Property
   * @param minimum The minimum value for the Element
   * @param maximum The maximum value for the Element
   * @param step The step value for the Element
   * @param format the number format for the Element
   * @param value Initial value for the Element
   * @throws IllegalArgumentException
   * @see INDINumberProperty
   */
  public INDIOneElementNumberProperty(INDIDriver driver, String name, String label, String group, PropertyStates state, PropertyPermissions permission, int timeout, String minimum, String maximum, String step, String format, String value) throws IllegalArgumentException {
    super(driver, name, label, group, state, permission, timeout);

    element = new INDINumberElement(this, name, label, value, minimum, maximum, step, format);
  }

  /**
   * Constructs an instance of
   * <code>INDIOneElementNumberProperty</code> with a particular
   * <code>driver</code>,
   * <code>name</code>,
   * <code>label</code>,
   * <code>group</code>,
   * <code>state</code>,
   * <code>permission</code>,
   * <code>timeout</code>,
   * <code>minimum</code>,
   * <code>maximum</code>,
   * <code>step</code>,
   * <code>format</code> and a initial
   * <code>value</code> for the Element.
   *
   * @param driver The Driver to which this property is associated
   * @param name The name of the Property
   * @param label The label of the Property
   * @param group The group of the Property
   * @param state The initial state of the Property
   * @param permission The permission of the Property
   * @param timeout The timeout of the Property
   * @param minimum The minimum value for the Element
   * @param maximum The maximum value for the Element
   * @param step The step value for the Element
   * @param format the number format for the Element
   * @param value Initial value for the Element
   * @throws IllegalArgumentException
   * @see INDINumberProperty
   */
  public INDIOneElementNumberProperty(INDIDriver driver, String name, String label, String group, PropertyStates state, PropertyPermissions permission, int timeout, double minimum, double maximum, double step, String format, double value) throws IllegalArgumentException {
    super(driver, name, label, group, state, permission, timeout);

    element = new INDINumberElement(this, name, label, value, minimum, maximum, step, format);
  }

  /**
   * Loads an instance of from a file or, if it cannot be loaded, constructs it
   * with a particular
   * <code>driver</code>,
   * <code>name</code>,
   * <code>label</code>,
   * <code>group</code>,
   * <code>state</code>,
   * <code>permission</code>,
   * <code>timeout</code>,
   * <code>minimum</code>,
   * <code>maximum</code>,
   * <code>step</code>,
   * <code>format</code> and a initial
   * <code>value</code> for the Element. The property will autosave its status
   * to a file every time that it is changed.
   *
   * @param driver The Driver to which this property is associated
   * @param name The name of the Property
   * @param label The label of the Property
   * @param group The group of the Property
   * @param state The initial state of the Property
   * @param permission The permission of the Property
   * @param timeout The timeout of the Property
   * @param minimum The minimum value for the Element
   * @param maximum The maximum value for the Element
   * @param step The step value for the Element
   * @param format the number format for the Element
   * @param value Initial value for the Element
   * @throws IllegalArgumentException
   * @return The loaded number property or a new constructed one if cannot be
   * loaded.
   * @see INDINumberProperty
   */
  public static INDIOneElementNumberProperty createSaveableOneElementNumberProperty(INDIDriver driver, String name, String label, String group, PropertyStates state, PropertyPermissions permission, int timeout, String minimum, String maximum, String step, String format, String value) throws IllegalArgumentException {
    INDIOneElementNumberProperty np = loadOneElementNumberProperty(driver, name);

    if (np == null) {
      np = new INDIOneElementNumberProperty(driver, name, label, group, state, permission, timeout, minimum, maximum, step, format, value);
      np.setSaveable(true);
    }

    return np;
  }

  /**
   * Loads an instance of from a file or, if it cannot be loaded, constructs it
   * with a particular
   * <code>driver</code>,
   * <code>name</code>,
   * <code>label</code>,
   * <code>group</code>,
   * <code>state</code>,
   * <code>permission</code>,
   * <code>timeout</code>,
   * <code>minimum</code>,
   * <code>maximum</code>,
   * <code>step</code>,
   * <code>format</code> and a initial
   * <code>value</code> for the Element. The property will autosave its status
   * to a file every time that it is changed.
   *
   * @param driver The Driver to which this property is associated
   * @param name The name of the Property
   * @param label The label of the Property
   * @param group The group of the Property
   * @param state The initial state of the Property
   * @param permission The permission of the Property
   * @param timeout The timeout of the Property
   * @param minimum The minimum value for the Element
   * @param maximum The maximum value for the Element
   * @param step The step value for the Element
   * @param format the number format for the Element
   * @param value Initial value for the Element
   * @throws IllegalArgumentException
   * @return The loaded number property or a new constructed one if cannot be
   * loaded.
   * @see INDINumberProperty
   */
  public static INDIOneElementNumberProperty createSaveableOneElementNumberProperty(INDIDriver driver, String name, String label, String group, PropertyStates state, PropertyPermissions permission, int timeout, double minimum, double maximum, double step, String format, double value) throws IllegalArgumentException {
    INDIOneElementNumberProperty np = loadOneElementNumberProperty(driver, name);

    if (np == null) {
      np = new INDIOneElementNumberProperty(driver, name, label, group, state, permission, timeout, minimum, maximum, step, format, value);
      np.setSaveable(true);
    }

    return np;
  }

  /**
   * Loads a One Element Number Property from a file.
   *
   * @param driver The Driver to which this property is associated
   * @param name The name of the property
   * @return The loaded number property or <code>null</code> if it could not be
   * loaded.
   */
  private static INDIOneElementNumberProperty loadOneElementNumberProperty(INDIDriver driver, String name) {
    INDIProperty prop;

    try {
      prop = INDIProperty.loadFromFile(driver, name);
    } catch (INDIException e) {  // Was not correctly loaded
      return null;
    }

    if (!(prop instanceof INDIOneElementNumberProperty)) {
      return null;
    }

    INDIOneElementNumberProperty tp = (INDIOneElementNumberProperty)prop;
    tp.setSaveable(true);
    return tp;
  }

  /**
   * Constructs an instance of
   * <code>INDIOneElementNumberProperty</code> with a particular
   * <code>driver</code>,
   * <code>name</code>,
   * <code>label</code>,
   * <code>group</code>,
   * <code>state</code>,
   * <code>permission</code>,
   * <code>minimum</code>,
   * <code>maximum</code>,
   * <code>step</code>,
   * <code>format</code> and a initial
   * <code>value</code> for the Element.
   *
   * @param driver The Driver to which this property is associated
   * @param name The name of the Property
   * @param label The label of the Property
   * @param group The group of the Property
   * @param state The initial state of the Property
   * @param permission The permission of the Property
   * @param minimum The minimum value for the Element
   * @param maximum The maximum value for the Element
   * @param step The step value for the Element
   * @param format the number format for the Element
   * @param value Initial value for the Element
   * @throws IllegalArgumentException
   * @see INDINumberProperty
   */
  public INDIOneElementNumberProperty(INDIDriver driver, String name, String label, String group, PropertyStates state, PropertyPermissions permission, String minimum, String maximum, String step, String format, String value) throws IllegalArgumentException {
    super(driver, name, label, group, state, permission);

    element = new INDINumberElement(this, name, label, value, minimum, maximum, step, format);
  }

  /**
   * Constructs an instance of
   * <code>INDIOneElementNumberProperty</code> with a particular
   * <code>driver</code>,
   * <code>name</code>,
   * <code>label</code>,
   * <code>group</code>,
   * <code>state</code>,
   * <code>permission</code>,
   * <code>minimum</code>,
   * <code>maximum</code>,
   * <code>step</code>,
   * <code>format</code> and a initial
   * <code>value</code> for the Element.
   *
   * @param driver The Driver to which this property is associated
   * @param name The name of the Property
   * @param label The label of the Property
   * @param group The group of the Property
   * @param state The initial state of the Property
   * @param permission The permission of the Property
   * @param minimum The minimum value for the Element
   * @param maximum The maximum value for the Element
   * @param step The step value for the Element
   * @param format the number format for the Element
   * @param value Initial value for the Element
   * @throws IllegalArgumentException
   * @see INDINumberProperty
   */
  public INDIOneElementNumberProperty(INDIDriver driver, String name, String label, String group, PropertyStates state, PropertyPermissions permission, double minimum, double maximum, double step, String format, double value) throws IllegalArgumentException {
    super(driver, name, label, group, state, permission);

    element = new INDINumberElement(this, name, label, value, minimum, maximum, step, format);
  }

  /**
   * Loads an instance of from a file or, if it cannot be loaded, constructs it
   * with a particular
   * <code>driver</code>,
   * <code>name</code>,
   * <code>label</code>,
   * <code>group</code>,
   * <code>state</code>,
   * <code>permission</code>,
   * <code>minimum</code>,
   * <code>maximum</code>,
   * <code>step</code>,
   * <code>format</code> and a initial
   * <code>value</code> for the Element. The property will autosave its status
   * to a file every time that it is changed.
   *
   * @param driver The Driver to which this property is associated
   * @param name The name of the Property
   * @param label The label of the Property
   * @param group The group of the Property
   * @param state The initial state of the Property
   * @param permission The permission of the Property
   * @param minimum The minimum value for the Element
   * @param maximum The maximum value for the Element
   * @param step The step value for the Element
   * @param format the number format for the Element
   * @param value Initial value for the Element
   * @throws IllegalArgumentException
   * @return The loaded number property or a new constructed one if cannot be
   * loaded.
   * @see INDINumberProperty
   */
  public static INDIOneElementNumberProperty createSaveableOneElementNumberProperty(INDIDriver driver, String name, String label, String group, PropertyStates state, PropertyPermissions permission, String minimum, String maximum, String step, String format, String value) throws IllegalArgumentException {
    INDIOneElementNumberProperty np = loadOneElementNumberProperty(driver, name);

    if (np == null) {
      np = new INDIOneElementNumberProperty(driver, name, label, group, state, permission, minimum, maximum, step, format, value);
      np.setSaveable(true);
    }

    return np;
  }

  /**
   * Loads an instance of from a file or, if it cannot be loaded, constructs it
   * with a particular
   * <code>driver</code>,
   * <code>name</code>,
   * <code>label</code>,
   * <code>group</code>,
   * <code>state</code>,
   * <code>permission</code>,
   * <code>minimum</code>,
   * <code>maximum</code>,
   * <code>step</code>,
   * <code>format</code> and a initial
   * <code>value</code> for the Element. The property will autosave its status
   * to a file every time that it is changed.
   *
   * @param driver The Driver to which this property is associated
   * @param name The name of the Property
   * @param label The label of the Property
   * @param group The group of the Property
   * @param state The initial state of the Property
   * @param permission The permission of the Property
   * @param minimum The minimum value for the Element
   * @param maximum The maximum value for the Element
   * @param step The step value for the Element
   * @param format the number format for the Element
   * @param value Initial value for the Element
   * @throws IllegalArgumentException
   * @return The loaded number property or a new constructed one if cannot be
   * loaded.
   * @see INDINumberProperty
   */
  public static INDIOneElementNumberProperty createSaveableOneElementNumberProperty(INDIDriver driver, String name, String label, String group, PropertyStates state, PropertyPermissions permission, double minimum, double maximum, double step, String format, double value) throws IllegalArgumentException {
    INDIOneElementNumberProperty np = loadOneElementNumberProperty(driver, name);

    if (np == null) {
      np = new INDIOneElementNumberProperty(driver, name, label, group, state, permission, minimum, maximum, step, format, value);
      np.setSaveable(true);
    }

    return np;
  }

  /**
   * Constructs an instance of
   * <code>INDIOneElementNumberProperty</code> with a particular
   * <code>driver</code>,
   * <code>name</code>,
   * <code>label</code>,
   * <code>state</code>,
   * <code>permission</code>,
   * <code>minimum</code>,
   * <code>maximum</code>,
   * <code>step</code>,
   * <code>format</code> and a initial
   * <code>value</code> for the Element.
   *
   * @param driver The Driver to which this property is associated
   * @param name The name of the Property
   * @param label The label of the Property
   * @param state The initial state of the Property
   * @param permission The permission of the Property
   * @param minimum The minimum value for the Element
   * @param maximum The maximum value for the Element
   * @param step The step value for the Element
   * @param format the number format for the Element
   * @param value Initial value for the Element
   * @throws IllegalArgumentException
   * @see INDINumberProperty
   */
  public INDIOneElementNumberProperty(INDIDriver driver, String name, String label, PropertyStates state, PropertyPermissions permission, String minimum, String maximum, String step, String format, String value) throws IllegalArgumentException {
    super(driver, name, label, state, permission);

    element = new INDINumberElement(this, name, label, value, minimum, maximum, step, format);
  }

  /**
   * Constructs an instance of
   * <code>INDIOneElementNumberProperty</code> with a particular
   * <code>driver</code>,
   * <code>name</code>,
   * <code>label</code>,
   * <code>state</code>,
   * <code>permission</code>,
   * <code>minimum</code>,
   * <code>maximum</code>,
   * <code>step</code>,
   * <code>format</code> and a initial
   * <code>value</code> for the Element.
   *
   * @param driver The Driver to which this property is associated
   * @param name The name of the Property
   * @param label The label of the Property
   * @param state The initial state of the Property
   * @param permission The permission of the Property
   * @param minimum The minimum value for the Element
   * @param maximum The maximum value for the Element
   * @param step The step value for the Element
   * @param format the number format for the Element
   * @param value Initial value for the Element
   * @throws IllegalArgumentException
   * @see INDINumberProperty
   */
  public INDIOneElementNumberProperty(INDIDriver driver, String name, String label, PropertyStates state, PropertyPermissions permission, double minimum, double maximum, double step, String format, double value) throws IllegalArgumentException {
    super(driver, name, label, state, permission);

    element = new INDINumberElement(this, name, label, value, minimum, maximum, step, format);
  }

  /**
   * Loads an instance of from a file or, if it cannot be loaded, constructs it
   * with a particular
   * <code>driver</code>,
   * <code>name</code>,
   * <code>label</code>,
   * <code>state</code>,
   * <code>permission</code>,
   * <code>minimum</code>,
   * <code>maximum</code>,
   * <code>step</code>,
   * <code>format</code> and a initial
   * <code>value</code> for the Element. The property will autosave its status
   * to a file every time that it is changed.
   *
   * @param driver The Driver to which this property is associated
   * @param name The name of the Property
   * @param label The label of the Property
   * @param state The initial state of the Property
   * @param permission The permission of the Property
   * @param minimum The minimum value for the Element
   * @param maximum The maximum value for the Element
   * @param step The step value for the Element
   * @param format the number format for the Element
   * @param value Initial value for the Element
   * @throws IllegalArgumentException
   * @return The loaded number property or a new constructed one if cannot be
   * loaded.
   * @see INDINumberProperty
   */
  public static INDIOneElementNumberProperty createSaveableOneElementNumberProperty(INDIDriver driver, String name, String label, PropertyStates state, PropertyPermissions permission, String minimum, String maximum, String step, String format, String value) throws IllegalArgumentException {
    INDIOneElementNumberProperty np = loadOneElementNumberProperty(driver, name);

    if (np == null) {
      np = new INDIOneElementNumberProperty(driver, name, label, state, permission, minimum, maximum, step, format, value);
      np.setSaveable(true);
    }

    return np;
  }

  /**
   * Loads an instance of from a file or, if it cannot be loaded, constructs it
   * with a particular
   * <code>driver</code>,
   * <code>name</code>,
   * <code>label</code>,
   * <code>state</code>,
   * <code>permission</code>,
   * <code>minimum</code>,
   * <code>maximum</code>,
   * <code>step</code>,
   * <code>format</code> and a initial
   * <code>value</code> for the Element. The property will autosave its status
   * to a file every time that it is changed.
   *
   * @param driver The Driver to which this property is associated
   * @param name The name of the Property
   * @param label The label of the Property
   * @param state The initial state of the Property
   * @param permission The permission of the Property
   * @param minimum The minimum value for the Element
   * @param maximum The maximum value for the Element
   * @param step The step value for the Element
   * @param format the number format for the Element
   * @param value Initial value for the Element
   * @throws IllegalArgumentException
   * @return The loaded number property or a new constructed one if cannot be
   * loaded.
   * @see INDINumberProperty
   */
  public static INDIOneElementNumberProperty createSaveableOneElementNumberProperty(INDIDriver driver, String name, String label, PropertyStates state, PropertyPermissions permission, double minimum, double maximum, double step, String format, double value) throws IllegalArgumentException {
    INDIOneElementNumberProperty np = loadOneElementNumberProperty(driver, name);

    if (np == null) {
      np = new INDIOneElementNumberProperty(driver, name, label, state, permission, minimum, maximum, step, format, value);
      np.setSaveable(true);
    }

    return np;
  }

  /**
   * Constructs an instance of
   * <code>INDIOneElementNumberProperty</code> with a particular
   * <code>driver</code>,
   * <code>name</code>,
   * <code>state</code>,
   * <code>permission</code>,
   * <code>minimum</code>,
   * <code>maximum</code>,
   * <code>step</code>,
   * <code>format</code> and a initial
   * <code>value</code> for the Element.
   *
   * @param driver The Driver to which this property is associated
   * @param name The name of the Property
   * @param state The initial state of the Property
   * @param permission The permission of the Property
   * @param minimum The minimum value for the Element
   * @param maximum The maximum value for the Element
   * @param step The step value for the Element
   * @param format the number format for the Element
   * @param value Initial value for the Element
   * @throws IllegalArgumentException
   * @see INDINumberProperty
   */
  public INDIOneElementNumberProperty(INDIDriver driver, String name, PropertyStates state, PropertyPermissions permission, String minimum, String maximum, String step, String format, String value) throws IllegalArgumentException {
    super(driver, name, state, permission);

    element = new INDINumberElement(this, name, value, minimum, maximum, step, format);
  }

  /**
   * Constructs an instance of
   * <code>INDIOneElementNumberProperty</code> with a particular
   * <code>driver</code>,
   * <code>name</code>,
   * <code>state</code>,
   * <code>permission</code>,
   * <code>minimum</code>,
   * <code>maximum</code>,
   * <code>step</code>,
   * <code>format</code> and a initial
   * <code>value</code> for the Element.
   *
   * @param driver The Driver to which this property is associated
   * @param name The name of the Property
   * @param state The initial state of the Property
   * @param permission The permission of the Property
   * @param minimum The minimum value for the Element
   * @param maximum The maximum value for the Element
   * @param step The step value for the Element
   * @param format the number format for the Element
   * @param value Initial value for the Element
   * @throws IllegalArgumentException
   * @see INDINumberProperty
   */
  public INDIOneElementNumberProperty(INDIDriver driver, String name, PropertyStates state, PropertyPermissions permission, double minimum, double maximum, double step, String format, double value) throws IllegalArgumentException {
    super(driver, name, state, permission);

    element = new INDINumberElement(this, name, value, minimum, maximum, step, format);
  }

  /**
   * Loads an instance of from a file or, if it cannot be loaded, constructs it
   * with a particular
   * <code>driver</code>,
   * <code>name</code>,
   * <code>state</code>,
   * <code>permission</code>,
   * <code>minimum</code>,
   * <code>maximum</code>,
   * <code>step</code>,
   * <code>format</code> and a initial
   * <code>value</code> for the Element. The property will autosave its status
   * to a file every time that it is changed.
   *
   * @param driver The Driver to which this property is associated
   * @param name The name of the Property
   * @param state The initial state of the Property
   * @param permission The permission of the Property
   * @param minimum The minimum value for the Element
   * @param maximum The maximum value for the Element
   * @param step The step value for the Element
   * @param format the number format for the Element
   * @param value Initial value for the Element
   * @throws IllegalArgumentException
   * @return The loaded number property or a new constructed one if cannot be
   * loaded.
   * @see INDINumberProperty
   */
  public static INDIOneElementNumberProperty createSaveableOneElementNumberProperty(INDIDriver driver, String name, PropertyStates state, PropertyPermissions permission, String minimum, String maximum, String step, String format, String value) throws IllegalArgumentException {
    INDIOneElementNumberProperty np = loadOneElementNumberProperty(driver, name);

    if (np == null) {
      np = new INDIOneElementNumberProperty(driver, name, state, permission, minimum, maximum, step, format, value);
      np.setSaveable(true);
    }

    return np;
  }

  /**
   * Loads an instance of from a file or, if it cannot be loaded, constructs it
   * with a particular
   * <code>driver</code>,
   * <code>name</code>,
   * <code>state</code>,
   * <code>permission</code>,
   * <code>minimum</code>,
   * <code>maximum</code>,
   * <code>step</code>,
   * <code>format</code> and a initial
   * <code>value</code> for the Element. The property will autosave its status
   * to a file every time that it is changed.
   *
   * @param driver The Driver to which this property is associated
   * @param name The name of the Property
   * @param state The initial state of the Property
   * @param permission The permission of the Property
   * @param minimum The minimum value for the Element
   * @param maximum The maximum value for the Element
   * @param step The step value for the Element
   * @param format the number format for the Element
   * @param value Initial value for the Element
   * @throws IllegalArgumentException
   * @return The loaded number property or a new constructed one if cannot be
   * loaded.
   * @see INDINumberProperty
   */
  public static INDIOneElementNumberProperty createSaveableOneElementNumberProperty(INDIDriver driver, String name, PropertyStates state, PropertyPermissions permission, double minimum, double maximum, double step, String format, double value) throws IllegalArgumentException {
    INDIOneElementNumberProperty np = loadOneElementNumberProperty(driver, name);

    if (np == null) {
      np = new INDIOneElementNumberProperty(driver, name, state, permission, minimum, maximum, step, format, value);
      np.setSaveable(true);
    }

    return np;
  }

  /**
   * Gets the value of the Element.
   *
   * @return The Value of the Element.
   * @see INDINumberElement#getValue()
   */
  public Double getValue() {
    return element.getValue();
  }

  /**
   * Sets the value of the Element.
   *
   * @param newValue The new value for the Element
   * @throws IllegalArgumentException
   * @see INDINumberElement#setValue(Object newValue)
   */
  public void setValue(Object newValue) throws IllegalArgumentException {
    element.setValue(newValue);
  }
}
