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
import laazotea.indi.Constants.SwitchRules;
import laazotea.indi.Constants.SwitchStatus;
import laazotea.indi.INDIException;

/**
 * A class representing a INDI One or None Switch Property (aka a simple
 * button). It simplifies dealing with Switch elements and so on.
 *
 * @author S. Alonso (Zerjillo) [zerjioi at ugr.es]
 * @version 1.34, January 11, 2014
 */
public class INDISwitchOneOrNoneProperty extends INDISwitchProperty {

  /**
   * The only Switch Element of the Property
   */
  private INDISwitchElement option;

  /**
   * Constructs an instance of
   * <code>INDISwitchOneOrNoneProperty</code> with a particular
   * <code>driver</code>,
   * <code>name</code>,
   * <code>label</code>,
   * <code>group</code>,
   * <code>state</code>,
   * <code>permission</code>,
   * <code>timeout</code>,
   * <code>option</code> and
   * <code>initialStatus</code>.
   *
   * @param driver The Driver to which this property is associated.
   * @param name The name of the Property
   * @param label The label of the Property
   * @param group The group of the Property
   * @param state The initial state of the Property
   * @param permission The permission of the Property
   * @param timeout The timeout of the Property
   * @param option The name of the option
   * @param initialStatus The initial status of the option
   * @see INDISwitchProperty
   */
  public INDISwitchOneOrNoneProperty(INDIDriver driver, String name, String label, String group, PropertyStates state, PropertyPermissions permission, int timeout, String option, SwitchStatus initialStatus) {
    super(driver, name, label, group, state, permission, timeout, SwitchRules.AT_MOST_ONE);

    createElement(name, option, initialStatus);
  }

  /**
   * Loads an instance of
   * <code>INDISwitchOneOrNoneProperty</code> from a file or, if it cannot be
   * loaded, constructs it with a particular
   * <code>driver</code>,
   * <code>name</code>,
   * <code>label</code>,
   * <code>group</code>,
   * <code>state</code>,
   * <code>permission</code>,
   * <code>timeout</code>,
   * <code>option</code> and
   * <code>initialStatus</code>. The property will autosave its status to a file
   * every time that it is changed.
   *
   * @param driver The Driver to which this property is associated.
   * @param name The name of the Property
   * @param label The label of the Property
   * @param group The group of the Property
   * @param state The initial state of the Property
   * @param permission The permission of the Property
   * @param timeout The timeout of the Property
   * @param option The name of the option
   * @param initialStatus The initial status of the option
   * @throws IllegalArgumentException
   * @return The loaded switch one or none property or a new constructed one if
   * cannot be loaded.
   * @see INDISwitchProperty
   */
  public static INDISwitchOneOrNoneProperty createSaveableSwitchOneOrNoneProperty(INDIDriver driver, String name, String label, String group, PropertyStates state, PropertyPermissions permission, int timeout, String option, SwitchStatus initialStatus) throws IllegalArgumentException {
    INDISwitchOneOrNoneProperty sp = loadSwitchOneOrNoneProperty(driver, name);

    if (sp == null) {
      sp = new INDISwitchOneOrNoneProperty(driver, name, label, group, state, permission, timeout, option, initialStatus);
      sp.setSaveable(true);
    }

    return sp;
  }

  /**
   * Loads a Switch One or None Property from a file.
   *
   * @param driver The Driver to which this property is associated
   * @param name The name of the property
   * @return The loaded switch one or none property or <code>null</code> if it
   * could not be loaded.
   */
  private static INDISwitchOneOrNoneProperty loadSwitchOneOrNoneProperty(INDIDriver driver, String name) {
    INDIProperty prop;

    try {
      prop = INDIProperty.loadFromFile(driver, name);
    } catch (INDIException e) {  // Was not correctly loaded
      return null;
    }

    if (!(prop instanceof INDISwitchOneOrNoneProperty)) {
      return null;
    }

    INDISwitchOneOrNoneProperty sp = (INDISwitchOneOrNoneProperty)prop;
    sp.setSaveable(true);
    return sp;
  }

  /**
   * Constructs an instance of
   * <code>INDISwitchOneOrNoneProperty</code> with a particular
   * <code>driver</code>,
   * <code>name</code>,
   * <code>label</code>,
   * <code>group</code>,
   * <code>state</code>,
   * <code>permission</code>,
   * <code>option</code> and
   * <code>initialStatus</code>.
   *
   * @param driver The Driver to which this property is associated.
   * @param name The name of the Property
   * @param label The label of the Property
   * @param group The group of the Property
   * @param state The initial state of the Property
   * @param permission The permission of the Property
   * @param option The name of the option
   * @param initialStatus The initial status of the option
   * @see INDISwitchProperty
   */
  public INDISwitchOneOrNoneProperty(INDIDriver driver, String name, String label, String group, PropertyStates state, PropertyPermissions permission, String option, SwitchStatus initialStatus) {
    super(driver, name, label, group, state, permission, 0, SwitchRules.AT_MOST_ONE);

    createElement(name, option, initialStatus);
  }

  /**
   * Loads an instance of
   * <code>INDISwitchOneOrNoneProperty</code> from a file or, if it cannot be
   * loaded, constructs it with a particular
   * <code>driver</code>,
   * <code>name</code>,
   * <code>label</code>,
   * <code>group</code>,
   * <code>state</code>,
   * <code>permission</code>,
   * <code>option</code> and
   * <code>initialStatus</code>. The property will autosave its status to a file
   * every time that it is changed.
   *
   * @param driver The Driver to which this property is associated.
   * @param name The name of the Property
   * @param label The label of the Property
   * @param group The group of the Property
   * @param state The initial state of the Property
   * @param permission The permission of the Property
   * @param option The name of the option
   * @param initialStatus The initial status of the option
   * @throws IllegalArgumentException
   * @return The loaded switch one or none property or a new constructed one if
   * cannot be loaded.
   * @see INDISwitchProperty
   */
  public static INDISwitchOneOrNoneProperty createSaveableSwitchOneOrNoneProperty(INDIDriver driver, String name, String label, String group, PropertyStates state, PropertyPermissions permission, String option, SwitchStatus initialStatus) throws IllegalArgumentException {
    INDISwitchOneOrNoneProperty sp = loadSwitchOneOrNoneProperty(driver, name);

    if (sp == null) {
      sp = new INDISwitchOneOrNoneProperty(driver, name, label, group, state, permission, option, initialStatus);
      sp.setSaveable(true);
    }

    return sp;
  }

  /**
   * Constructs an instance of
   * <code>INDISwitchOneOrNoneProperty</code> with a particular
   * <code>driver</code>,
   * <code>name</code>,
   * <code>label</code>,
   * <code>state</code>,
   * <code>permission</code>,
   * <code>option</code> and
   * <code>initialStatus</code>.
   *
   * @param driver The Driver to which this property is associated.
   * @param name The name of the Property
   * @param label The label of the Property
   * @param state The initial state of the Property
   * @param permission The permission of the Property
   * @param option The name of the option
   * @param initialStatus The initial status of the option
   * @see INDISwitchProperty
   */
  public INDISwitchOneOrNoneProperty(INDIDriver driver, String name, String label, PropertyStates state, PropertyPermissions permission, String option, SwitchStatus initialStatus) {
    super(driver, name, label, null, state, permission, 0, SwitchRules.AT_MOST_ONE);

    createElement(name, option, initialStatus);
  }

  /**
   * Loads an instance of
   * <code>INDISwitchOneOrNoneProperty</code> from a file or, if it cannot be
   * loaded, constructs it with a particular
   * <code>driver</code>,
   * <code>name</code>,
   * <code>label</code>,
   * <code>state</code>,
   * <code>permission</code>,
   * <code>option</code> and
   * <code>initialStatus</code>. The property will autosave its status to a file
   * every time that it is changed.
   *
   * @param driver The Driver to which this property is associated.
   * @param name The name of the Property
   * @param label The label of the Property
   * @param state The initial state of the Property
   * @param permission The permission of the Property
   * @param option The name of the option
   * @param initialStatus The initial status of the option
   * @throws IllegalArgumentException
   * @return The loaded switch one or none property or a new constructed one if
   * cannot be loaded.
   * @see INDISwitchProperty
   */
  public static INDISwitchOneOrNoneProperty createSaveableSwitchOneOrNoneProperty(INDIDriver driver, String name, String label, PropertyStates state, PropertyPermissions permission, String option, SwitchStatus initialStatus) throws IllegalArgumentException {
    INDISwitchOneOrNoneProperty sp = loadSwitchOneOrNoneProperty(driver, name);

    if (sp == null) {
      sp = new INDISwitchOneOrNoneProperty(driver, name, label, state, permission, option, initialStatus);
      sp.setSaveable(true);
    }

    return sp;
  }

  /**
   * Constructs an instance of
   * <code>INDISwitchOneOrNoneProperty</code> with a particular
   * <code>driver</code>,
   * <code>name</code>,
   * <code>state</code>,
   * <code>permission</code>,
   * <code>option</code> and
   * <code>initialStatus</code>.
   *
   * @param driver The Driver to which this property is associated.
   * @param name The name of the Property
   * @param state The initial state of the Property
   * @param permission The permission of the Property
   * @param option The name of the option
   * @param initialStatus The initial status of the option
   * @see INDISwitchProperty
   */
  public INDISwitchOneOrNoneProperty(INDIDriver driver, String name, PropertyStates state, PropertyPermissions permission, String option, SwitchStatus initialStatus) {
    super(driver, name, null, null, state, permission, 0, SwitchRules.AT_MOST_ONE);

    createElement(name, option, initialStatus);
  }

  /**
   * Loads an instance of
   * <code>INDISwitchOneOrNoneProperty</code> from a file or, if it cannot be
   * loaded, constructs it with a particular
   * <code>driver</code>,
   * <code>name</code>,
   * <code>state</code>,
   * <code>permission</code>,
   * <code>option</code> and
   * <code>initialStatus</code>. The property will autosave its status to a file
   * every time that it is changed.
   *
   * @param driver The Driver to which this property is associated.
   * @param name The name of the Property
   * @param state The initial state of the Property
   * @param permission The permission of the Property
   * @param option The name of the option
   * @param initialStatus The initial status of the option
   * @throws IllegalArgumentException
   * @return The loaded switch one or none property or a new constructed one if
   * cannot be loaded.
   * @see INDISwitchProperty
   */
  public static INDISwitchOneOrNoneProperty createSaveableSwitchOneOrNoneProperty(INDIDriver driver, String name, PropertyStates state, PropertyPermissions permission, String option, SwitchStatus initialStatus) throws IllegalArgumentException {
    INDISwitchOneOrNoneProperty sp = loadSwitchOneOrNoneProperty(driver, name);

    if (sp == null) {
      sp = new INDISwitchOneOrNoneProperty(driver, name, state, permission, option, initialStatus);
      sp.setSaveable(true);
    }

    return sp;
  }

  /**
   * Creates the Element of the Property.
   *
   * @param name The name of the Element
   * @param option The label of the Element
   * @param initialStatus The initial status of the Element
   */
  private void createElement(String name, String option, SwitchStatus initialStatus) {
    this.option = new INDISwitchElement(this, name, option, initialStatus);
  }

  /**
   * Sets the status of the Element of the Property.
   *
   * @param newStatus The new status
   */
  public void setStatus(SwitchStatus newStatus) {
    option.setValue(newStatus);
  }

  /**
   * Sets the status of the Element of the property according to a pair of
   * ev and values.
   *
   * @param ev The pairs of ev and values (only one except some
   * error / strange behaviour).
   */
  public void setStatus(INDISwitchElementAndValue[] ev) {
    for (int i = 0 ; i < ev.length ; i++) {
      if (ev[i].getElement() == option) {
        option.setValue(ev[i].getValue());
      }
    }
  }

  /**
   * Gets the status of the Element of the Property.
   *
   * @return The status of the Element of the Property
   */
  public SwitchStatus getStatus() {
    return option.getValue();
  }

  /**
   * Gets the status of the Element of the Property that would be set according
   * to some pairs of ev and values. This method DOES NOT change the
   * status of the Element NOR it gives the actual status of it.
   *
   * @param ev The pairs of ev and values (only one except some
   * error / strange behaviour).
   * @return The status of the Element of the Property that would be set
   */
  public SwitchStatus getStatus(INDISwitchElementAndValue[] ev) {
    for (int i = 0 ; i < ev.length ; i++) {
      if (ev[i].getElement() == option) {
        return ev[i].getValue();
      }
    }

    return SwitchStatus.OFF;
  }
}
