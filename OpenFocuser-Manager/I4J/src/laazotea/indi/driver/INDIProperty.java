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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import laazotea.indi.Constants.PropertyPermissions;
import laazotea.indi.Constants.PropertyStates;
import laazotea.indi.INDIException;

/**
 * A class representing a INDI Property. The subclasses
 * <code>INDIBLOBProperty</code>,
 * <code>INDILightProperty</code>,
 * <code>INDINumberProperty</code>,
 * <code>INDISwitchProperty</code> and
 * <code>INDITextProperty</code> define the basic Properties that a INDI Drivers
 * may contain according to the INDI protocol.
 *
 * @author S. Alonso (Zerjillo) [zerjioi at ugr.es]
 * @version 1.35, November 11, 2013
 */
public abstract class INDIProperty implements Serializable {

  /**
   * The Driver to which this property belongs
   */
  private transient INDIDriver driver;
  /**
   * This Property name
   */
  private String name;
  /**
   * This Property label
   */
  private String label;
  /**
   * The group to which this Property might be assigned
   */
  private String group;
  /**
   * The current state of this Property
   */
  private PropertyStates state;
  /**
   * The permission of this Property
   */
  private PropertyPermissions permission;
  /**
   * The timeout of this Property
   */
  private int timeout;
  /**
   * A list of Elements for this Property
   */
  private LinkedHashMap<String, INDIElement> elements;
  /**
   * <code>true</code> if property has completely init (sent to any client).
   */
  private boolean isInit;
  /**
   * To save / retrieve properties from dis directory
   */
  private static final String PROPERTIES_DIR_NAME = "properties";
  /**
   * It marks if the property should be saved each time that it is changed.
   */
  private boolean saveable;

  /**
   * Constructs an instance of a
   * <code>INDIProperty</code>. Called by its sub-classes. If
   * <code>label</code> is
   * <code>null</code> the label will be copied from the
   * <code>name</code>. if
   * <code>group</code> is
   * <code>null</code> the group will be
   * <code>"Unsorted"</code>.
   *
   * @param driver The Driver to which this property is associated.
   * @param name The name of the Property
   * @param label The label of the Property
   * @param group The group of the Property
   * @param state The initial state of the Property
   * @param permission The permission of the Property
   * @param timeout The timeout of the Property
   * @throws IllegalArgumentException if    <code>name<code> is
   * <code>null</code> or empty.
   */
  protected INDIProperty(INDIDriver driver, String name, String label, String group, PropertyStates state, PropertyPermissions permission, int timeout) throws IllegalArgumentException {
    this.driver = driver;

    // Name
    if (name == null) {
      throw new IllegalArgumentException("No name for the Property");
    }

    name = name.trim();

    if (name.length() == 0) {
      throw new IllegalArgumentException("No name for the Property");
    }

    this.name = name;

    // Label
    if (label == null) {
      this.label = name;
    } else {
      label = label.trim();

      if (label.length() == 0) {
        this.label = name;
      } else {
        this.label = label;
      }
    }

    if (group == null) {
      group = "Unsorted";
    }

    group = group.trim();

    if (group.length() == 0) {
      group = "Unsorted";
    }

    this.group = group;

    this.state = state;

    this.permission = permission;

    if (timeout < 0) {
      this.timeout = 0;
    } else {
      this.timeout = timeout;
    }

    this.elements = new LinkedHashMap<String, INDIElement>();

    this.saveable = false;

    isInit = false;
  }

  /**
   * Set the property to be saveable. Should only be called by property
   * factories.
   *
   * @param saveable
   */
  protected void setSaveable(boolean saveable) {
    this.saveable = saveable;
  }

  /**
   * Sets the Driver of the Property.
   */
  private void setDriver(INDIDriver driver) {
    this.driver = driver;
  }

  /**
   * Gets the Driver of the Property.
   *
   * @return The Driver of the Property.
   */
  protected INDIDriver getDriver() {
    return driver;
  }

  /**
   * Sets the State of the Property.
   *
   * @param newState The new State of the Property.
   */
  public void setState(PropertyStates newState) {
    this.state = newState;
  }

  /**
   * Sets the timeout of the property.
   *
   * @param newTimeout The new timeout of the Property.
   * @throws IllegalArgumentException if <code>timeout</code> is less than 0.
   */
  public void setTimeout(int newTimeout) throws IllegalArgumentException {
    if (timeout < 0) {
      throw new IllegalArgumentException("Illegal timeout for the Property");
    }

    this.timeout = newTimeout;
  }

  /**
   * Gets the Group to which this property might be assigned.
   *
   * @return the group to which this property might be assigned.
   */
  public String getGroup() {
    return group;
  }

  /**
   * Gets the timeout for this Property.
   *
   * @return the timeout for this Property.
   */
  public int getTimeout() {
    return timeout;
  }

  /**
   * Gets the label for this Property.
   *
   * @return the label for this Property.
   */
  public String getLabel() {
    return label;
  }

  /**
   * Gets the name of this Property
   *
   * @return the name of this Property
   */
  public String getName() {
    return name;
  }

  /**
   * Gets the number of Elements in this Property
   *
   * @return the number of Elements in this Property.
   */
  public int getElementCount() {
    return elements.size();
  }

  /**
   * Gets the Permission of this Property.
   *
   * @return the Permission of this Property.
   */
  public PropertyPermissions getPermission() {
    return permission;
  }

  /**
   * Gets the State of this Property.
   *
   * @return the State of this Property.
   */
  public PropertyStates getState() {
    return state;
  }

  /**
   * Adds a new Element to this Property (if it there is no other Element with
   * the same name and it is already being init [not sended to clients]).
   * Drivers must not call this method directly as it is called when
   * constructing the Element.
   *
   * @param element the Element to be added.
   * @throws IllegalArgumentException
   */
  protected void addElement(INDIElement element) throws IllegalArgumentException {
    if ((this instanceof INDITextProperty) && (!(element instanceof INDITextElement))) {
      throw new IllegalArgumentException("Text Element cannot be added to Text Property");
    }

    if ((this instanceof INDISwitchProperty) && (!(element instanceof INDISwitchElement))) {
      throw new IllegalArgumentException("Switch Element cannot be added to Switch Property");
    }

    if ((this instanceof INDINumberProperty) && (!(element instanceof INDINumberElement))) {
      throw new IllegalArgumentException("Number Element cannot be added to Number Property");
    }

    if ((this instanceof INDILightProperty) && (!(element instanceof INDILightElement))) {
      throw new IllegalArgumentException("Light Element cannot be added to Light Property");
    }

    if ((this instanceof INDIBLOBProperty) && (!(element instanceof INDIBLOBElement))) {
      throw new IllegalArgumentException("BLOB Element cannot be added to BLOB Property");
    }

    if (!isInit) {  // We still can add new properties
      if (!elements.containsValue(element)) {
        elements.put(element.getName(), element);
      }
    }
  }

  /**
   * Gets a particular Element of this Property by its name.
   *
   * @param name The name of the Element to be returned
   * @return The Element of this Property with the given <code>name</code>.
   * <code>null</code> if there is no Element with that <code>name</code>.
   */
  public INDIElement getElement(String name) {
    return elements.get(name);
  }

  /**
   * Gets a
   * <code>List</code> with all the Elements of this Property.
   *
   * @return the <code>List</code> of Elements belonging to this Property.
   */
  public ArrayList<INDIElement> getElementsAsList() {
    return new ArrayList<INDIElement>(elements.values());
  }

  /**
   * Gets the names of the Elements of this Property.
   *
   * @return the names of the Elements of this Property.
   */
  public String[] getElementNames() {
    List<INDIElement> l = getElementsAsList();

    String[] names = new String[l.size()];

    for (int i = 0 ; i < l.size() ; i++) {
      names[i] = l.get(i).getName();
    }

    return names;
  }

  /**
   * Returns a String with the name of the Property, its state and its elements
   * and values.
   *
   * @return a String representation of the property and its values.
   */
  public String getNameStateAndValuesAsString() {
    String aux = getName() + " - " + getState() + "\n";
    List<INDIElement> l = getElementsAsList();

    for (int i = 0 ; i < l.size() ; i++) {
      aux += "  " + l.get(i).getNameAndValueAsString() + "\n";
    }

    return aux;
  }

  /**
   * Gets the XML code to define the property. Should not usually be called by
   * the Drivers.
   *
   * @return The XML code to define the property.
   */
  protected String getXMLPropertyDefinition() {
    return getXMLPropertyDefinition(null);
  }

  /**
   * Gets the XML code to define the property with a
   * <code>message</code>. Should not usually be called by the Drivers.
   *
   * @param message An message to be sent to the client when defining the
   * property.
   * @return The XML code to define the property.
   */
  protected String getXMLPropertyDefinition(String message) {
    String xml;

    if (message == null) {
      xml = getXMLPropertyDefinitionInit();
    } else {
      xml = getXMLPropertyDefinitionInit(message);
    }

    List<INDIElement> elem = getElementsAsList();

    for (int i = 0 ; i < elements.size() ; i++) {
      xml += elem.get(i).getXMLDefElement();
    }

    xml += getXMLPropertyDefinitionEnd();

    isInit = true; // The property now is initialized. No further changes allowed
    
    return xml;
  }

  /**
   * Gets the XML code to set the values of the property. Should not usually be
   * called by the Drivers.
   *
   * @return The XML code to set the values of the property.
   */
  protected String getXMLPropertySet() {
    return getXMLPropertySet(null);
  }

  /**
   * Gets the XML code to set the values of the property with a
   * <code>message</code>. Should not usually be called by the Drivers.
   *
   * @param message An message to be sent to the client when setting the values
   * of the property.
   * @return The XML code to set the values of the property.
   */
  protected String getXMLPropertySet(String message) {
    if (saveable) {
      try {
        saveToFile();
      } catch (IOException e) {
      }
    }

    String xml;

    if (message == null) {
      xml = getXMLPropertySetInit();
    } else {
      xml = getXMLPropertySetInit(message);
    }

    List<INDIElement> elem = getElementsAsList();

    for (int i = 0 ; i < elem.size() ; i++) {
      xml += elem.get(i).getXMLOneElement();
    }

    xml += getXMLPropertySetEnd();

    return xml;
  }

  /**
   * Gets the opening XML Element &lt;defXXXVector&gt; for this Property.
   *
   * @return the opening XML Element &lt;defXXXVector&gt; for this Property.
   */
  protected abstract String getXMLPropertyDefinitionInit();

  /**
   * Gets the opening XML Element &lt;defXXXVector&gt; for this Property with a
   * <code>message</code> to the client.
   *
   * @param message A message to be sent to the client.
   * @return the opening XML Element &lt;defXXXVector&gt; for this Property.
   */
  protected abstract String getXMLPropertyDefinitionInit(String message);

  /**
   * Gets the closing XML Element &lt;/defXXXVector&gt; for this Property.
   *
   * @return the closing XML Element &lt;/defXXXVector&gt; for this Property.
   */
  protected abstract String getXMLPropertyDefinitionEnd();

  /**
   * Gets the opening XML Element &lt;setXXXVector&gt; for this Property.
   *
   * @return the opening XML Element &lt;setXXXVector&gt; for this Property.
   */
  protected abstract String getXMLPropertySetInit();

  /**
   * Gets the opening XML Element &lt;setXXXVector&gt; for this Property with a
   * <code>message</code> to the client.
   *
   * @param message A message to be sent to the client.
   * @return the opening XML Element &lt;setXXXVector&gt; for this Property.
   */
  protected abstract String getXMLPropertySetInit(String message);

  /**
   * Gets the closing XML Element &lt;/setXXXVector&gt; for this Property.
   *
   * @return the closing XML Element &lt;/setXXXVector&gt; for this Property.
   */
  protected abstract String getXMLPropertySetEnd();

  /**
   * Saves the property and its elements to a file. Ideal to later restore it on
   * subsecuent executions of the driver.
   *
   * @throws IOException
   */
  private void saveToFile() throws IOException {
    File propertiesDir = new File(PROPERTIES_DIR_NAME);

    if (!propertiesDir.exists()) {
      propertiesDir.mkdir();
    }

    if (propertiesDir.exists()) {
      if (propertiesDir.isDirectory()) {
        File file = new File(propertiesDir, getPropertyNameForFile());
        FileOutputStream fos = new FileOutputStream(file);

        ObjectOutputStream oos = new ObjectOutputStream(fos);

        oos.writeObject(this);

        oos.close();
      }
    }
  }

  /**
   * Convenience method to get the name of the file for a particular property to
   * be saved / retrieved.
   *
   * @param property The property
   * @return A name for a file in which a property will be saved / restored
   */
  private String getPropertyNameForFile() {
    return removeCharacters(getDriver().getName()) + "_" + getName() + ".prop";
  }

  /**
   * Convenience method to get the name of the file for a particular property to
   * be saved / retrieved.
   *
   * @param driver The driver of the property
   * @param propertyName The name of the property
   * @return A name for a file in which a property will be saved / restored
   */
  private static String getPropertyNameForFile(INDIDriver driver, String propertyName) {
    return removeCharacters(driver.getName()) + "_" + propertyName + ".prop";
  }

  /**
   * Removes all non letters / numbers from a String.
   *
   * @param str An input string
   * @return The same <code>str</code>string without any non letter / numbers.
   */
  private static String removeCharacters(String str) {
    return str.replaceAll("[^a-zA-Z0-9]", "");
  }

  /**
   * Loads a property from a file.
   *
   * @param driver The driver which will include the property
   * @param propertyName The name of the property to load
   * @return A property loaded from a file
   * @throws INDIException If there is some problem loading it (for example if
   * the file does not exist)
   */
  protected static INDIProperty loadFromFile(INDIDriver driver, String propertyName) throws INDIException {
    File propertiesDir = new File(PROPERTIES_DIR_NAME);

    File file = new File(propertiesDir, getPropertyNameForFile(driver, propertyName));

    INDIProperty prop = null;

    try {
      FileInputStream fis = new FileInputStream(file);

      ObjectInputStream ois = new ObjectInputStream(fis);

      prop = (INDIProperty)ois.readObject();

      ois.close();
    } catch (ClassNotFoundException ex) {
      throw new INDIException("Problem when loading a property from file " + file.getName() + " - ClassNotFoundException");
    } catch (IOException ex) {
      throw new INDIException("Problem when loading a property from file " + file.getName() + " - IOException");
    }

    prop.setDriver(driver);
    return prop;
  }

  /**
   * Sets all the values in the Elements of and array of elements and values. 
   * Please note that this method does not make any assumtion about the values
   * that are going to be set, so an <code>IllegalArgumentException</code> or an
   * <code>INDIValueException</code> might be thrown.
   *
   * @param elementsAndValues The array of elements and values
   */
  public void setValues(INDIElementAndValue[] elementsAndValues) {
    for (int i = 0 ; i < elementsAndValues.length ; i++) {
      INDIElement element = elementsAndValues[i].getElement();

      element.setValue(elementsAndValues[i].getValue());
    }
  }
}
