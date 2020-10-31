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

import java.util.ArrayList;
import laazotea.indi.Constants.PropertyPermissions;
import laazotea.indi.Constants.PropertyStates;
import laazotea.indi.Constants.SwitchRules;
import laazotea.indi.Constants.SwitchStatus;
import laazotea.indi.INDIException;

/**
 * A class representing a INDI One Of Many Switch Property. It simplifies
 * dealing with Switch elements and so on.
 *
 * @author S. Alonso (Zerjillo) [zerjioi at ugr.es]
 * @version 1.34, November 8, 2013
 */
public class INDISwitchOneOfManyProperty extends INDISwitchProperty {

  /**
   * Constructs an instance of
   * <code>INDISwitchOneOfManyProperty</code> with a particular
   * <code>driver</code>,
   * <code>name</code>,
   * <code>label</code>,
   * <code>group</code>,
   * <code>state</code>,
   * <code>permission</code>,
   * <code>timeout</code>,
   * <code>elements</code> and
   * <code>selectedElement</code>.
   *
   * @param driver The Driver to which this property is associated.
   * @param name The name of the Property
   * @param label The label of the Property
   * @param group The group of the Property
   * @param state The initial state of the Property
   * @param permission The permission of the Property
   * @param timeout The timeout of the Property
   * @param elements The name of the option
   * @param selectedElement The initial status of the option
   * @see INDISwitchProperty
   */
  public INDISwitchOneOfManyProperty(INDIDriver driver, String name, String label, String group, PropertyStates state, PropertyPermissions permission, int timeout, String[] elements, int selectedElement) {
    super(driver, name, label, group, state, permission, timeout, SwitchRules.ONE_OF_MANY);

    createElements(elements, selectedElement);
  }

  /**
   * Loads an instance of
   * <code>INDISwitchOneOfManyProperty</code> from a file or, if it cannot be
   * loaded, constructs it with a particular
   * <code>driver</code>,
   * <code>name</code>,
   * <code>label</code>,
   * <code>group</code>,
   * <code>state</code>,
   * <code>permission</code>,
   * <code>timeout</code>,
   * <code>elements</code> and
   * <code>selectedElement</code>. The property will autosave its status to a
   * file every time that it is changed.
   *
   * @param driver The Driver to which this property is associated.
   * @param name The name of the Property
   * @param label The label of the Property
   * @param group The group of the Property
   * @param state The initial state of the Property
   * @param permission The permission of the Property
   * @param timeout The timeout of the Property
   * @param elements The name of the option
   * @param selectedElement The initial status of the option
   * @return The loaded switch one of many property or a new constructed one if
   * cannot be loaded.
   * @see INDISwitchProperty
   */
  public static INDISwitchOneOfManyProperty createSaveableSwitchOneOfManyProperty(INDIDriver driver, String name, String label, String group, PropertyStates state, PropertyPermissions permission, int timeout, String[] elements, int selectedElement) {
    INDISwitchOneOfManyProperty sp = loadSwitchOneOfManyProperty(driver, name);

    if (sp == null) {
      sp = new INDISwitchOneOfManyProperty(driver, name, label, group, state, permission, timeout, elements, selectedElement);
      sp.setSaveable(true);
    }

    return sp;
  }

  /**
   * Loads a Switch One of Many Property from a file.
   *
   * @param driver The Driver to which this property is associated
   * @param name The name of the property
   * @return The loaded switch one of many property or <code>null</code> if it
   * could not be loaded.
   */
  private static INDISwitchOneOfManyProperty loadSwitchOneOfManyProperty(INDIDriver driver, String name) {
    INDIProperty prop;

    try {
      prop = INDIProperty.loadFromFile(driver, name);
    } catch (INDIException e) {  // Was not correctly loaded
      return null;
    }

    if (!(prop instanceof INDISwitchOneOfManyProperty)) {
      return null;
    }

    INDISwitchOneOfManyProperty sp = (INDISwitchOneOfManyProperty)prop;
    sp.setSaveable(true);
    return sp;
  }

  /**
   * Constructs an instance of
   * <code>INDISwitchOneOfManyProperty</code> with a particular
   * <code>driver</code>,
   * <code>name</code>,
   * <code>label</code>,
   * <code>group</code>,
   * <code>state</code>,
   * <code>permission</code>,
   * <code>timeout</code> and
   * <code>elements</code>.
   *
   * @param driver The Driver to which this property is associated.
   * @param name The name of the Property
   * @param label The label of the Property
   * @param group The group of the Property
   * @param state The initial state of the Property
   * @param permission The permission of the Property
   * @param timeout The timeout of the Property
   * @param elements The name of the option
   * @see INDISwitchProperty
   */
  public INDISwitchOneOfManyProperty(INDIDriver driver, String name, String label, String group, PropertyStates state, PropertyPermissions permission, int timeout, String[] elements) {
    super(driver, name, label, group, state, permission, timeout, SwitchRules.ONE_OF_MANY);

    createElements(elements, 0);
  }

  /**
   * Loads an instance of
   * <code>INDISwitchOneOfManyProperty</code> from a file or, if it cannot be
   * loaded, constructs it with a particular
   * <code>driver</code>,
   * <code>name</code>,
   * <code>label</code>,
   * <code>group</code>,
   * <code>state</code>,
   * <code>permission</code>,
   * <code>timeout</code> and
   * <code>elements</code>. The property will autosave its status to a file
   * every time that it is changed.
   *
   * @param driver The Driver to which this property is associated.
   * @param name The name of the Property
   * @param label The label of the Property
   * @param group The group of the Property
   * @param state The initial state of the Property
   * @param permission The permission of the Property
   * @param timeout The timeout of the Property
   * @param elements The name of the option
   * @return The loaded switch one of many property or a new constructed one if
   * cannot be loaded.
   * @see INDISwitchProperty
   */
  public static INDISwitchOneOfManyProperty createSaveableSwitchOneOfManyProperty(INDIDriver driver, String name, String label, String group, PropertyStates state, PropertyPermissions permission, int timeout, String[] elements) {
    INDISwitchOneOfManyProperty sp = loadSwitchOneOfManyProperty(driver, name);

    if (sp == null) {
      sp = new INDISwitchOneOfManyProperty(driver, name, label, group, state, permission, timeout, elements);
      sp.setSaveable(true);
    }

    return sp;
  }

  /**
   * Constructs an instance of
   * <code>INDISwitchOneOfManyProperty</code> with a particular
   * <code>driver</code>,
   * <code>name</code>,
   * <code>label</code>,
   * <code>group</code>,
   * <code>state</code>,
   * <code>permission</code>,
   * <code>elements</code> and
   * <code>selectedElement</code>.
   *
   * @param driver The Driver to which this property is associated.
   * @param name The name of the Property
   * @param label The label of the Property
   * @param group The group of the Property
   * @param state The initial state of the Property
   * @param permission The permission of the Property
   * @param elements The name of the option
   * @param selectedElement The initial status of the option
   * @see INDISwitchProperty
   */
  public INDISwitchOneOfManyProperty(INDIDriver driver, String name, String label, String group, PropertyStates state, PropertyPermissions permission, String[] elements, int selectedElement) {
    super(driver, name, label, group, state, permission, 0, SwitchRules.ONE_OF_MANY);

    createElements(elements, selectedElement);
  }

  /**
   * Loads an instance of
   * <code>INDISwitchOneOfManyProperty</code> from a file or, if it cannot be
   * loaded, constructs it with a particular
   * <code>driver</code>,
   * <code>name</code>,
   * <code>label</code>,
   * <code>group</code>,
   * <code>state</code>,
   * <code>permission</code>,
   * <code>elements</code> and
   * <code>selectedElement</code>. The property will autosave its status to a
   * file every time that it is changed.
   *
   * @param driver The Driver to which this property is associated.
   * @param name The name of the Property
   * @param label The label of the Property
   * @param group The group of the Property
   * @param state The initial state of the Property
   * @param permission The permission of the Property
   * @param elements The name of the option
   * @param selectedElement The initial status of the option
   * @return The loaded switch one of many property or a new constructed one if
   * cannot be loaded.
   * @see INDISwitchProperty
   */
  public static INDISwitchOneOfManyProperty createSaveableSwitchOneOfManyProperty(INDIDriver driver, String name, String label, String group, PropertyStates state, PropertyPermissions permission, String[] elements, int selectedElement) {
    INDISwitchOneOfManyProperty sp = loadSwitchOneOfManyProperty(driver, name);

    if (sp == null) {
      sp = new INDISwitchOneOfManyProperty(driver, name, label, group, state, permission, elements, selectedElement);
      sp.setSaveable(true);
    }

    return sp;
  }

  /**
   * Constructs an instance of
   * <code>INDISwitchOneOfManyProperty</code> with a particular
   * <code>driver</code>,
   * <code>name</code>,
   * <code>label</code>,
   * <code>group</code>,
   * <code>state</code>,
   * <code>permission</code> and
   * <code>elements</code>.
   *
   * @param driver The Driver to which this property is associated.
   * @param name The name of the Property
   * @param label The label of the Property
   * @param group The group of the Property
   * @param state The initial state of the Property
   * @param permission The permission of the Property
   * @param elements The name of the option
   * @see INDISwitchProperty
   */
  public INDISwitchOneOfManyProperty(INDIDriver driver, String name, String label, String group, PropertyStates state, PropertyPermissions permission, String[] elements) {
    super(driver, name, label, group, state, permission, 0, SwitchRules.ONE_OF_MANY);

    createElements(elements, 0);
  }

  /**
   * Loads an instance of
   * <code>INDISwitchOneOfManyProperty</code> from a file or, if it cannot be
   * loaded, constructs it with a particular
   * <code>driver</code>,
   * <code>name</code>,
   * <code>label</code>,
   * <code>group</code>,
   * <code>state</code>,
   * <code>permission</code> and
   * <code>elements</code>. The property will autosave its status to a file
   * every time that it is changed.
   *
   * @param driver The Driver to which this property is associated.
   * @param name The name of the Property
   * @param label The label of the Property
   * @param group The group of the Property
   * @param state The initial state of the Property
   * @param permission The permission of the Property
   * @param elements The name of the option
   * @return The loaded switch one of many property or a new constructed one if
   * cannot be loaded.
   * @see INDISwitchProperty
   */
  public static INDISwitchOneOfManyProperty createSaveableSwitchOneOfManyProperty(INDIDriver driver, String name, String label, String group, PropertyStates state, PropertyPermissions permission, String[] elements) {
    INDISwitchOneOfManyProperty sp = loadSwitchOneOfManyProperty(driver, name);

    if (sp == null) {
      sp = new INDISwitchOneOfManyProperty(driver, name, label, group, state, permission, elements);
      sp.setSaveable(true);
    }

    return sp;
  }

  /**
   * Constructs an instance of
   * <code>INDISwitchOneOfManyProperty</code> with a particular
   * <code>driver</code>,
   * <code>name</code>,
   * <code>label</code>,
   * <code>state</code>,
   * <code>permission</code>,
   * <code>elements</code> and
   * <code>selectedElement</code>.
   *
   * @param driver The Driver to which this property is associated.
   * @param name The name of the Property
   * @param label The label of the Property
   * @param state The initial state of the Property
   * @param permission The permission of the Property
   * @param elements The name of the option
   * @param selectedElement The initial status of the option
   * @see INDISwitchProperty
   */
  public INDISwitchOneOfManyProperty(INDIDriver driver, String name, String label, PropertyStates state, PropertyPermissions permission, String[] elements, int selectedElement) {
    super(driver, name, label, null, state, permission, 0, SwitchRules.ONE_OF_MANY);

    createElements(elements, selectedElement);
  }

  /**
   * Loads an instance of
   * <code>INDISwitchOneOfManyProperty</code> from a file or, if it cannot be
   * loaded, constructs it with a particular
   * <code>driver</code>,
   * <code>name</code>,
   * <code>label</code>,
   * <code>state</code>,
   * <code>permission</code>,
   * <code>elements</code> and
   * <code>selectedElement</code>. The property will autosave its status to a
   * file every time that it is changed.
   *
   * @param driver The Driver to which this property is associated.
   * @param name The name of the Property
   * @param label The label of the Property
   * @param state The initial state of the Property
   * @param permission The permission of the Property
   * @param elements The name of the option
   * @param selectedElement The initial status of the option
   * @return The loaded switch one of many property or a new constructed one if
   * cannot be loaded.
   * @see INDISwitchProperty
   */
  public static INDISwitchOneOfManyProperty createSaveableSwitchOneOfManyProperty(INDIDriver driver, String name, String label, PropertyStates state, PropertyPermissions permission, String[] elements, int selectedElement) {
    INDISwitchOneOfManyProperty sp = loadSwitchOneOfManyProperty(driver, name);

    if (sp == null) {
      sp = new INDISwitchOneOfManyProperty(driver, name, label, state, permission, elements, selectedElement);
      sp.setSaveable(true);
    }

    return sp;
  }

  /**
   * Constructs an instance of
   * <code>INDISwitchOneOfManyProperty</code> with a particular
   * <code>driver</code>,
   * <code>name</code>,
   * <code>label</code>,
   * <code>state</code>,
   * <code>permission</code> and
   * <code>elements</code>.
   *
   * @param driver The Driver to which this property is associated.
   * @param name The name of the Property
   * @param label The label of the Property
   * @param state The initial state of the Property
   * @param permission The permission of the Property
   * @param elements The name of the option
   * @see INDISwitchProperty
   */
  public INDISwitchOneOfManyProperty(INDIDriver driver, String name, String label, PropertyStates state, PropertyPermissions permission, String[] elements) {
    super(driver, name, label, null, state, permission, 0, SwitchRules.ONE_OF_MANY);

    createElements(elements, 0);
  }

  /**
   * Loads an instance of
   * <code>INDISwitchOneOfManyProperty</code> from a file or, if it cannot be
   * loaded, constructs it with a particular
   * <code>driver</code>,
   * <code>name</code>,
   * <code>label</code>,
   * <code>state</code>,
   * <code>permission</code> and
   * <code>elements</code>. The property will autosave its status to a file
   * every time that it is changed.
   *
   * @param driver The Driver to which this property is associated.
   * @param name The name of the Property
   * @param label The label of the Property
   * @param state The initial state of the Property
   * @param permission The permission of the Property
   * @param elements The name of the option
   * @return The loaded switch one of many property or a new constructed one if
   * cannot be loaded.
   * @see INDISwitchProperty
   */
  public static INDISwitchOneOfManyProperty createSaveableSwitchOneOfManyProperty(INDIDriver driver, String name, String label, PropertyStates state, PropertyPermissions permission, String[] elements) {
    INDISwitchOneOfManyProperty sp = loadSwitchOneOfManyProperty(driver, name);

    if (sp == null) {
      sp = new INDISwitchOneOfManyProperty(driver, name, label, state, permission, elements);
      sp.setSaveable(true);
    }

    return sp;
  }

  /**
   * Constructs an instance of
   * <code>INDISwitchOneOfManyProperty</code> with a particular
   * <code>driver</code>,
   * <code>name</code>,
   * <code>state</code>,
   * <code>permission</code>,
   * <code>elements</code> and
   * <code>selectedElement</code>.
   *
   * @param driver The Driver to which this property is associated.
   * @param name The name of the Property
   * @param state The initial state of the Property
   * @param permission The permission of the Property
   * @param elements The name of the option
   * @param selectedElement The initial status of the option
   * @see INDISwitchProperty
   */
  public INDISwitchOneOfManyProperty(INDIDriver driver, String name, PropertyStates state, PropertyPermissions permission, String[] elements, int selectedElement) {
    super(driver, name, null, null, state, permission, 0, SwitchRules.ONE_OF_MANY);

    createElements(elements, selectedElement);
  }

  /**
   * Loads an instance of
   * <code>INDISwitchOneOfManyProperty</code> from a file or, if it cannot be
   * loaded, constructs it with a particular
   * <code>driver</code>,
   * <code>name</code>,
   * <code>state</code>,
   * <code>permission</code>,
   * <code>elements</code> and
   * <code>selectedElement</code>. The property will autosave its status to a
   * file every time that it is changed.
   *
   * @param driver The Driver to which this property is associated.
   * @param name The name of the Property
   * @param state The initial state of the Property
   * @param permission The permission of the Property
   * @param elements The name of the option
   * @param selectedElement The initial status of the option
   * @return The loaded switch one of many property or a new constructed one if
   * cannot be loaded.
   * @see INDISwitchProperty
   */
  public static INDISwitchOneOfManyProperty createSaveableSwitchOneOfManyProperty(INDIDriver driver, String name, PropertyStates state, PropertyPermissions permission, String[] elements, int selectedElement) {
    INDISwitchOneOfManyProperty sp = loadSwitchOneOfManyProperty(driver, name);

    if (sp == null) {
      sp = new INDISwitchOneOfManyProperty(driver, name, state, permission, elements, selectedElement);
      sp.setSaveable(true);
    }

    return sp;
  }

  /**
   * Constructs an instance of
   * <code>INDISwitchOneOfManyProperty</code> with a particular
   * <code>driver</code>,
   * <code>name</code>,
   * <code>state</code>,
   * <code>permission</code> and
   * <code>elements</code>.
   *
   * @param driver The Driver to which this property is associated.
   * @param name The name of the Property
   * @param state The initial state of the Property
   * @param permission The permission of the Property
   * @param elements The name of the option
   * @see INDISwitchProperty
   */
  public INDISwitchOneOfManyProperty(INDIDriver driver, String name, PropertyStates state, PropertyPermissions permission, String[] elements) {
    super(driver, name, null, null, state, permission, 0, SwitchRules.ONE_OF_MANY);

    createElements(elements, 0);
  }

  /**
   * Loads an instance of
   * <code>INDISwitchOneOfManyProperty</code> from a file or, if it cannot be
   * loaded, constructs it with a particular
   * <code>driver</code>,
   * <code>name</code>,
   * <code>state</code>,
   * <code>permission</code> and
   * <code>elements</code>. The property will autosave its status to a file
   * every time that it is changed.
   *
   * @param driver The Driver to which this property is associated.
   * @param name The name of the Property
   * @param state The initial state of the Property
   * @param permission The permission of the Property
   * @param elements The name of the option
   * @return The loaded switch one of many property or a new constructed one if
   * cannot be loaded
   * @see INDISwitchProperty
   */
  public static INDISwitchOneOfManyProperty createSaveableSwitchOneOfManyProperty(INDIDriver driver, String name, PropertyStates state, PropertyPermissions permission, String[] elements) {
    INDISwitchOneOfManyProperty sp = loadSwitchOneOfManyProperty(driver, name);

    if (sp == null) {
      sp = new INDISwitchOneOfManyProperty(driver, name, state, permission, elements);
      sp.setSaveable(true);
    }

    return sp;
  }

  /**
   * Creates de Switch Elements of the property. Each Element name and label
   * will be the same. The
   * <code>defaultOption</code> element will be selected.
   *
   * @param options The names of the Switch Elements
   * @param defaultOption The number of the selected element
   */
  private void createElements(String[] options, int defaultOption) {
    if (defaultOption >= options.length) {
      defaultOption = 0;
    }

    for (int i = 0 ; i < options.length ; i++) {
      SwitchStatus ss = SwitchStatus.OFF;

      if (i == defaultOption) {
        ss = SwitchStatus.ON;
      }

      INDISwitchElement element = new INDISwitchElement(this, options[i], ss);
    }
  }

  /**
   * Gets the name of the selected element.
   *
   * @return The name of the selected eleent
   */
  public String getSelectedValue() {
    INDISwitchElement e = getSelectedElement();

    return e.getName();
  }

  /**
   * Gets the selected element.
   *
   * @return The selected element
   */
  private INDISwitchElement getSelectedElement() {
    ArrayList<INDIElement> list = getElementsAsList();

    for (int i = 0 ; i < list.size() ; i++) {
      INDISwitchElement e = (INDISwitchElement)list.get(i);

      if (e.getValue() == SwitchStatus.ON) {
        return e;
      }
    }

    return null; // Should never happen
  }

  /**
   * Gets the index of the selected element.
   *
   * @return The index of the selected element
   */
  public int getSelectedIndex() {
    ArrayList<INDIElement> list = getElementsAsList();

    for (int i = 0 ; i < list.size() ; i++) {
      INDISwitchElement e = (INDISwitchElement)list.get(i);

      if (e.getValue() == SwitchStatus.ON) {
        return i;
      }
    }

    return -1; // Should never happen
  }

  /**
   * Sets the selected Element to the one with a particular
   * <code>index</code>.
   *
   * @param index The index of the Element that is being selected
   */
  public void setSelectedIndex(int index) {
    if ((index < 0) || (index >= this.getElementCount())) {
      return;
    }

    ArrayList<INDIElement> list = getElementsAsList();

    INDISwitchElement e = (INDISwitchElement)list.get(index);

    setOnlyOneSwitchOn(e);
  }

  /**
   * Gets the index of the element that should be selected according to some
   * Elements and Values pairs. This method DOES NOT change the selected index
   * nor returns the really selected element index.
   *
   * @param ev The pairs of elements and values
   * @return The index of the element that would be selected according to the
   * pairs of elements and values.
   */
  public int getSelectedIndex(INDISwitchElementAndValue[] ev) {
    for (int i = 0 ; i < ev.length ; i++) {
      if (ev[i].getValue() == SwitchStatus.ON) {
        ArrayList<INDIElement> list = getElementsAsList();

        for (int h = 0 ; h < list.size() ; h++) {
          if (list.get(h) == ev[i].getElement()) {
            return h;
          }
        }
      }
    }

    return -1;
  }

  /**
   * Gets the element that should be selected according to some Elements and
   * Values pairs. This method DOES NOT change the selected index nor returns
   * the really selected element.
   *
   * @param ev The pairs of elements and values
   * @return The element that would be selected according to the pairs of
   * elements and values.
   */
  public String getSelectedValue(INDISwitchElementAndValue[] ev) {
    for (int i = 0 ; i < ev.length ; i++) {
      if (ev[i].getValue() == SwitchStatus.ON) {
        ArrayList<INDIElement> list = getElementsAsList();

        for (int h = 0 ; h < list.size() ; h++) {
          if (list.get(h) == ev[i].getElement()) {
            return list.get(h).getName();
          }
        }
      }
    }

    return null;
  }

  /**
   * Sets the selected Element to the one specified in an array of elements and
   * values.
   *
   * @param ev The pairs of elements and values
   */
  public void setSelectedIndex(INDISwitchElementAndValue[] ev) {
    int selected = getSelectedIndex(ev);

    setSelectedIndex(selected);
  }
}
