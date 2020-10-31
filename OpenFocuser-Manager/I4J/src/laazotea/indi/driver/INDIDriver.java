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

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import laazotea.indi.Constants.PropertyPermissions;
import laazotea.indi.Constants.PropertyStates;
import laazotea.indi.Constants.SwitchRules;
import laazotea.indi.Constants.SwitchStatus;
import laazotea.indi.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * A class representing a Driver in the INDI Protocol. INDI Drivers should
 * extend this class. It is in charge of stablishing the connection to the
 * clients and parsing / formating any incoming / leaving messages.
 *
 * @author S. Alonso (Zerjillo) [zerjioi at ugr.es]
 * @version 1.34, November 6, 2013
 */
public abstract class INDIDriver implements INDIProtocolParser {

  private InputStream inputStream;
  private OutputStream outputStream;
  private PrintWriter out;
  private INDIProtocolReader reader;
  /**
   * A list of subdrivers
   */
  private ArrayList<INDIDriver> subdrivers;
  /**
   * A Switch Element for the CONNECTION property
   */
  private INDISwitchElement connectedE;
  /**
   * A Switch Element for the CONNECTION property
   */
  private INDISwitchElement disconnectedE;
  /**
   * The standard CONNECTION property (optional)
   */
  private INDISwitchProperty connectionP;
  /**
   * A list of Properties for this Driver
   */
  private LinkedHashMap<String, INDIProperty> properties;
  /**
   * To know if the driver has already been started or not.
   */
  private boolean started;

  /**
   * Constructs a INDIDriver with a particular
   * <code>inputStream<code> from which to read the incoming messages (from clients) and a
   * <code>outputStream</code> to write the messages to the clients.
   *
   * @param inputStream The stream from which to read messages.
   * @param outputStream The stream to which to write the messages.
   */
  protected INDIDriver(InputStream inputStream, OutputStream outputStream) {
    this.out = new PrintWriter(outputStream);
    this.inputStream = inputStream;
    this.outputStream = outputStream;
    this.subdrivers = new ArrayList<INDIDriver>();

    started = false;

    properties = new LinkedHashMap<String, INDIProperty>();

    if (this instanceof INDIConnectionHandler) {
      addConnectionProperty();
    }
  }

  /**
   * Registers a subdriver that may receive messages.
   *
   * @param driver The subdriver to register.
   */
  protected void registerSubdriver(INDIDriver driver) {
    subdrivers.add(driver);
  }

  /**
   * Unregister a subdriver that may not receive any other message.
   *
   * @param driver The subdriver to unregister.
   */
  protected void unregisterSubdriver(INDIDriver driver) {
    subdrivers.remove(driver);
  }

  /**
   * Gets a device by its name. Returns
   * <code>null</code> if there is no subdriver with that name.
   *
   * @param name The name of the subdriver to return
   * @return The subdriver or <code>null</code> if there is no subdrive with
   * this name.
   */
  private INDIDriver getSubdriver(String name) {
    for (int i = 0 ; i < subdrivers.size() ; i++) {
      INDIDriver d = subdrivers.get(i);

      if (d.getName().compareTo(name) == 0) {
        return d;
      }
    }

    return null;
  }

  /**
   * Gets the name of the Driver.
   *
   * @return The name of the Driver.
   */
  public abstract String getName();

  /**
   * Starts listening to inputStream. It creates a new Thread to make the
   * readings. Thus, the normal execution of the code is not stopped. This
   * method is not usually called by the Driver itself but the encapsulating
   * class (for example
   * <code>INDIDriverRunner</code>).
   *
   * @see INDIDriverRunner
   */
  public void startListening() {
    started = true;

    reader = new INDIProtocolReader(this);
    reader.start();
  }

  /**
   * Gets the started or not state of the Driver.
   *
   * @return <code>true</code> if the Driver has already started to listen to
   * messages. <code>false</code> otherwise.
   */
  public boolean isStarted() {
    return started;
  }

  @Override
  public void finishReader() {
    System.err.println("DRIVER " + getName() + " finishing");

    if (reader != null) {
      reader.setStop(true);
    }
  }

  /**
   * Gets the output
   * <code>PrintWriter</code>.
   *
   * @return The output <code>PrintWriter</code>.
   */
  protected PrintWriter getOut() {
    return out;
  }

  /**
   * Adds a
   * <code>INDISwitchProperty</code> called "CONNECTION" with two Elements
   * called "CONNECT" and "DISCONNECT". The DISCONNECT Element is ON, while the
   * CONNECT Element is OFF. It is a Read / write property with "one of many"
   * rule (thus, one option is always selected).
   */
  private void addConnectionProperty() {
    connectionP = new INDISwitchProperty(this, "CONNECTION", "Connection", "Main Control", PropertyStates.IDLE, PropertyPermissions.RW, 100, SwitchRules.ONE_OF_MANY);
    connectedE = new INDISwitchElement(connectionP, "CONNECT", "Connect", SwitchStatus.OFF);
    disconnectedE = new INDISwitchElement(connectionP, "DISCONNECT", "Disconnect", SwitchStatus.ON);

    addProperty(connectionP);
  }

  /**
   * Sets the CONNECTION Property to connected or disconnected and sends the
   * changes to the clients. If the property does not exist nothing happens.
   *
   * @param connected <code>true</code> if the CONNECT Element must be selected.
   * <code>false</code> if the DISCONNECT Element must be selected.
   */
  private void setConnectionProperty(boolean connected) {
    setConnectionProperty(connected, null);
  }

  /**
   * Sets the CONNECTION Property to connected or disconnected and sends the
   * changes to the clients. If the property does not exist nothing happens. If
   * the connection is already stablished ignore petition.
   *
   * @param connected
   * @param message An optional message (can be <code>null</code>)
   * <code>true</code> if the CONNECT Element must be selected.
   * <code>false</code> if the DISCONNECT Element must be selected.
   */
  private void setConnectionProperty(boolean connected, String message) {
    if (connectionP == null) {
      return;
    }

    connectionP.setState(PropertyStates.OK);

    if (connected) {
      connectedE.setValue(SwitchStatus.ON);
    } else {
      disconnectedE.setValue(SwitchStatus.ON);
    }

    if (message == null) {
      try {
        updateProperty(connectionP);
      } catch (INDIException e) { // Ignore, there must be no errors here        
      }
    } else {
      try {
        connectionP.setState(PropertyStates.ALERT);
        updateProperty(connectionP, message);
      } catch (INDIException e) { // Ignore, there must be no errors here        
      }
    }
  }

  /**
   * Checks if the CONNECTION Property is set to
   * <code>SwitchStatus.ON</code>.
   *
   * @return <code>true</code> if the CONNECTION Property is set to
   * <code>SwitchStatus.ON</code>. <code>false</code> otherwise.
   */
  protected boolean isConnected() {
    if (connectedE.getValue() == SwitchStatus.ON) {
      return true;
    }

    return false;
  }

  /**
   * Parses the XML messages. Should not be called by particular Drivers.
   *
   * @param doc the messages to be parsed.
   */
  @Override
  public void parseXML(Document doc) {
    Element el = doc.getDocumentElement();

    if (el.getNodeName().compareTo("INDI") != 0) {
      return;
    }

    NodeList nodes = el.getChildNodes();

    for (int i = 0 ; i < nodes.getLength() ; i++) {
      Node n = nodes.item(i);

      if (n instanceof Element) {
        Element child = (Element)n;

        parseXMLElement(child);
      }
    }
  }

  /**
   * Parses a particular XML Element.
   *
   * @param xml The XML element to be parsed.
   */
  private void parseXMLElement(Element xml) {
    INDIDriver subd = getSubdriver(xml);

    if (subd != null) {
      subd.parseXMLElement(xml);
    } else {
      String name = xml.getNodeName();

      if (name.equals("getProperties")) {
        processGetProperties(xml);
      } else if (name.equals("newTextVector")) {
        processNewTextVector(xml);
      } else if (name.equals("newSwitchVector")) {
        processNewSwitchVector(xml);
      } else if (name.equals("newNumberVector")) {
        processNewNumberVector(xml);
      } else if (name.equals("newBLOBVector")) {
        processNewBLOBVector(xml);
      }
    }
  }

  /**
   * Parses a &lt;newTextVector&gt; XML message.
   *
   * @param xml The &lt;newTextVector&gt; XML message to be parsed.
   */
  private void processNewTextVector(Element xml) {
    INDIProperty prop = processNewXXXVector(xml);

    if (prop == null) {
      return;
    }

    if (!(prop instanceof INDITextProperty)) {
      return;
    }

    INDIElementAndValue[] evs = processINDIElements(prop, xml);

    Date timestamp = INDIDateFormat.parseTimestamp(xml.getAttribute("timestamp"));

    INDITextElementAndValue[] newEvs = new INDITextElementAndValue[evs.length];

    for (int i = 0 ; i < newEvs.length ; i++) {
      newEvs[i] = (INDITextElementAndValue)evs[i];
    }

    processNewTextValue((INDITextProperty)prop, timestamp, newEvs);
  }

  /**
   * Called when a new Text Vector message has been received from a Client. Must
   * be implemented in Drivers to take care of the new values sent by clients.
   * It will be called with correct Properties and Elements. Any incorrect Text
   * Message received will be discarded and this method will not be called.
   *
   * @param property The Text Property asked to change.
   * @param timestamp The timestamp of the received message
   * @param elementsAndValues An array of pairs of Text Elements and its
   * requested values to be parsed.
   */
  public abstract void processNewTextValue(INDITextProperty property, Date timestamp, INDITextElementAndValue[] elementsAndValues);

  /**
   * Parses a &lt;newSwitchVector&gt; XML message. If the switch is the standard
   * CONNECTION property it will analyze the message and call the
   * <code>driverConnect</code> and
   * <code>driverDisconnect</code> methods from
   * <code>INDIConnectionHandler</code>.
   *
   * @param xml The &lt;newSwitchVector&gt; XML message to be parsed.
   * @see INDIConnectionHandler
   */
  private void processNewSwitchVector(Element xml) {
    INDIProperty prop = processNewXXXVector(xml);

    if (prop == null) {
      return;
    }

    if (!(prop instanceof INDISwitchProperty)) {
      return;
    }

    INDIElementAndValue[] evs = processINDIElements(prop, xml);

    Date timestamp = INDIDateFormat.parseTimestamp(xml.getAttribute("timestamp"));

    INDISwitchElementAndValue[] newEvs = new INDISwitchElementAndValue[evs.length];

    for (int i = 0 ; i < newEvs.length ; i++) {
      newEvs[i] = (INDISwitchElementAndValue)evs[i];
    }

    if ((this instanceof INDIConnectionHandler) && (prop == connectionP)) { // If it is the CONNECTION property
      handleConnectionProperty(newEvs, timestamp);
    } else {  // if it is any other property
      processNewSwitchValue((INDISwitchProperty)prop, timestamp, newEvs);
    }
  }

  /**
   * Handles the connection property. Called from
   * <code>processNewSwitchVector</code>.
   *
   * @param newEvs The new Elements and Values
   * @param timestamp The timestamp of the received CONNECTION message.
   */
  private synchronized void handleConnectionProperty(INDISwitchElementAndValue[] newEvs, Date timestamp) {
    for (int i = 0 ; i < newEvs.length ; i++) {
      INDISwitchElement el = newEvs[i].getElement();
      SwitchStatus s = newEvs[i].getValue();

      if (el == connectedE) {
        if (s == SwitchStatus.ON) {
          if (connectedE.getValue() != SwitchStatus.ON) {
            try {
              ((INDIConnectionHandler)this).driverConnect(timestamp);

              setConnectionProperty(true);
            } catch (INDIException e) {
              setConnectionProperty(false, e.getMessage());
            }
          } else {
            setConnectionProperty(true);
          }
        }
      } else if (el == disconnectedE) {
        if (s == SwitchStatus.ON) {
          if (disconnectedE.getValue() != SwitchStatus.ON) {
            try {
              ((INDIConnectionHandler)this).driverDisconnect(timestamp);

              setConnectionProperty(false);
            } catch (INDIException e) {
              setConnectionProperty(true, e.getMessage());
            }
          } else {
            setConnectionProperty(false);
          }
        }
      }
    }
  }

  /**
   * Called when a new Switch Vector message has been received from a Client.
   * Must be implemented in Drivers to take care of the new values sent by
   * clients. It will be called with correct Properties and Elements. Any
   * incorrect Switch Message received will be discarded and this method will
   * not be called.
   *
   * @param property The Switch Property asked to change.
   * @param timestamp The timestamp of the received message
   * @param elementsAndValues An array of pairs of Switch Elements and its
   * requested values to be parsed.
   */
  public abstract void processNewSwitchValue(INDISwitchProperty property, Date timestamp, INDISwitchElementAndValue[] elementsAndValues);

  /**
   * Parses a &lt;newNumberVector&gt; XML message.
   *
   * @param xml The &lt;newNumberVector&gt; XML message to be parsed.
   */
  private void processNewNumberVector(Element xml) {
    INDIProperty prop = processNewXXXVector(xml);

    if (prop == null) {
      return;
    }

    if (!(prop instanceof INDINumberProperty)) {
      return;
    }

    INDIElementAndValue[] evs = processINDIElements(prop, xml);

    Date timestamp = INDIDateFormat.parseTimestamp(xml.getAttribute("timestamp"));

    INDINumberElementAndValue[] newEvs = new INDINumberElementAndValue[evs.length];

    for (int i = 0 ; i < newEvs.length ; i++) {
      newEvs[i] = (INDINumberElementAndValue)evs[i];
    }

    processNewNumberValue((INDINumberProperty)prop, timestamp, newEvs);
  }

  /**
   * Called when a new Number Vector message has been received from a Client.
   * Must be implemented in Drivers to take care of the new values sent by
   * clients. It will be called with correct Properties and Elements. Any
   * incorrect Number Message received will be discarded and this method will
   * not be called.
   *
   * @param property The Number Property asked to change.
   * @param timestamp The timestamp of the received message
   * @param elementsAndValues An array of pairs of Number Elements and its
   * requested values to be parsed.
   */
  public abstract void processNewNumberValue(INDINumberProperty property, Date timestamp, INDINumberElementAndValue[] elementsAndValues);

  /**
   * Parses a &lt;newBLOBVector&gt; XML message.
   *
   * @param xml The &lt;newBLOBVector&gt; XML message to be parsed.
   */
  private void processNewBLOBVector(Element xml) {
    INDIProperty prop = processNewXXXVector(xml);

    if (prop == null) {
      return;
    }

    if (!(prop instanceof INDIBLOBProperty)) {
      return;
    }

    INDIElementAndValue[] evs = processINDIElements(prop, xml);

    Date timestamp = INDIDateFormat.parseTimestamp(xml.getAttribute("timestamp"));

    INDIBLOBElementAndValue[] newEvs = new INDIBLOBElementAndValue[evs.length];

    for (int i = 0 ; i < newEvs.length ; i++) {
      newEvs[i] = (INDIBLOBElementAndValue)evs[i];
    }

    processNewBLOBValue((INDIBLOBProperty)prop, timestamp, newEvs);
  }

  /**
   * Called when a new BLOB Vector message has been received from a Client. Must
   * be implemented in Drivers to take care of the new values sent by clients.
   * It will be called with correct Properties and Elements. Any incorrect BLOB
   * Message received will be discarded and this method will not be called.
   *
   * @param property The BLOB Property asked to change.
   * @param timestamp The timestamp of the received message
   * @param elementsAndValues An array of pairs of BLOB Elements and its
   * requested values to be parsed.
   */
  public abstract void processNewBLOBValue(INDIBLOBProperty property, Date timestamp, INDIBLOBElementAndValue[] elementsAndValues);

  /**
   * Returns an array of Elements and its corresponded requested values from a
   * XML message.
   *
   * @param property The property from which to parse the Elements.
   * @param xml The XML message
   * @return An array of Elements and its corresponding requested values
   */
  private INDIElementAndValue[] processINDIElements(INDIProperty property, Element xml) {

    String oneType;
    if (property instanceof INDITextProperty) {
      oneType = "oneText";
    } else if (property instanceof INDIBLOBProperty) {
      oneType = "oneBLOB";
    } else if (property instanceof INDINumberProperty) {
      oneType = "oneNumber";
    } else if (property instanceof INDISwitchProperty) {
      oneType = "oneSwitch";
    } else {
      return new INDIElementAndValue[0];
    }

    ArrayList<INDIElementAndValue> list = new ArrayList<INDIElementAndValue>();

    NodeList nodes = xml.getChildNodes();

    for (int i = 0 ; i < nodes.getLength() ; i++) {
      Node n = nodes.item(i);

      if (n instanceof Element) {
        Element child = (Element)n;

        String name = child.getNodeName();

        if (name.equals(oneType)) {
          INDIElementAndValue ev = processOneXXX(property, child);

          if (ev != null) {
            list.add(ev);
          }
        }
      }
    }

    return list.toArray(new INDIElementAndValue[0]);
  }

  /**
   * Processes a XML &lt;oneXXX&gt; message for a property.
   *
   * @param property The property from which to parse the Element.
   * @param xml The &lt;oneXXX&gt; XML message
   * @return A Element and its corresponding requested value
   */
  private INDIElementAndValue processOneXXX(INDIProperty property, Element xml) {
    if (!xml.hasAttribute("name")) {
      return null;
    }

    String elName = xml.getAttribute("name");

    INDIElement el = property.getElement(elName);

    if (el == null) {
      return null;
    }

    Object value;

    try {
      value = el.parseOneValue(xml);
    } catch (IllegalArgumentException e) {
      return null;
    }

    if (el instanceof INDITextElement) {
      return new INDITextElementAndValue((INDITextElement)el, (String)value);
    } else if (el instanceof INDISwitchElement) {
      return new INDISwitchElementAndValue((INDISwitchElement)el, (SwitchStatus)value);
    } else if (el instanceof INDINumberElement) {
      return new INDINumberElementAndValue((INDINumberElement)el, (Double)value);
    } else if (el instanceof INDIBLOBElement) {
      return new INDIBLOBElementAndValue((INDIBLOBElement)el, (INDIBLOBValue)value);
    }

    return null;
  }

  /**
   * Returns the subdriver to which a xml message is sent (if any).
   * <code>null</code> if it is not directed to any subdriver.
   *
   * @param xml The XML message
   * @return The subdriver to which the message is directed.
   */
  private INDIDriver getSubdriver(Element xml) {
    if (!xml.hasAttribute("device")) {
      return null;
    }

    String deviceName = xml.getAttribute("device").trim();

    return getSubdriver(deviceName);
  }

  /**
   * Processes a &lt;newXXXVector&gt; message.
   *
   * @param xml The XML message
   * @return The INDI Property to which the <code>xml</code> message refers.
   */
  private INDIProperty processNewXXXVector(Element xml) {
    if ((!xml.hasAttribute("device")) || (!xml.hasAttribute("name"))) {
      return null;
    }

    String devName = xml.getAttribute("device");
    String propName = xml.getAttribute("name");

    if (devName.compareTo(getName()) != 0) {  // If the message is not for this device
      return null;
    }

    INDIProperty prop = getProperty(propName);

    return prop;
  }

  /**
   * Processes a &lt;getProperties&gt; message.
   *
   * @param xml The XML message
   */
  private void processGetProperties(Element xml) {
    if (!xml.hasAttribute("version")) {
      printMessage("getProperties: no version specified\n");

      return;
    }

    if (xml.hasAttribute("device")) {
      String deviceName = xml.getAttribute("device").trim();

      if (deviceName.compareTo(deviceName) != 0) {  // not asking for this driver
        return;
      }
    }

    if (xml.hasAttribute("name")) {
      String propertyName = xml.getAttribute("name");
      INDIProperty p = getProperty(propertyName);

      if (p != null) {
        sendDefXXXVectorMessage(p, null);
      }
    } else {  // Send all of them
      sendAllProperties();
    }
  }

  /**
   * Sends all the properties to the clients.
   */
  public void sendAllProperties() {
    ArrayList<INDIProperty> props = getPropertiesAsList();

    for (int i = 0 ; i < props.size() ; i++) {
      sendDefXXXVectorMessage(props.get(i), null);
    }

    propertiesRequested();
  }

  /**
   * This method is called when all the properties are requested. A driver with
   * subdrivers may override it to be notified about this kind of requests (and
   * ask the subdriver to also send the properties).
   */
  protected void propertiesRequested() {
  }

  /**
   * Adds a new Property to the Device. A message about it will be send to the
   * clients. Drivers must call this method if they want to define a new
   * Property.
   *
   * @param property The Property to be added.
   */
  protected void addProperty(INDIProperty property) {
    addProperty(property, null);
  }

  /**
   * Adds a new Property to the Device with a
   * <code>message</code> to the client. A message about it will be send to the
   * clients. Drivers must call this method if they want to define a new
   * Property.
   *
   * @param property The Property to be added.
   * @param message The message to be sended to the clients with the definition
   * message.
   */
  protected void addProperty(INDIProperty property, String message) {
    if (!properties.containsValue(property)) {
      properties.put(property.getName(), property);

      sendDefXXXVectorMessage(property, message);
    }
  }

  /**
   * Notifies the clients about the property and its values. Drivres must call
   * this method when the values of the Elements of the property are updated in
   * order to notify the clients.
   *
   * @param property The Property whose values have change and about which the
   * clients must be notified.
   */
  protected void updateProperty(INDIProperty property) throws INDIException {
    updateProperty(property, null);
  }

  /**
   * Notifies the clients about the property and its values with an additional
   * <code>message</code>. Drivres must call this method when the values of the
   * Elements of the property are updated in order to notify the clients.
   *
   * @param property The Property whose values have change and about which the
   * clients must be notified.
   * @param message The message to be sended to the clients with the udpate
   * message.
   */
  protected void updateProperty(INDIProperty property, String message) throws INDIException {
    if (properties.containsValue(property)) {
      if (property instanceof INDISwitchProperty) {
        INDISwitchProperty sp = (INDISwitchProperty)property;

        if (!sp.checkCorrectValues()) {
          throw new INDIException("Switch (" + property.getName() + ") value not value (not following its rule).");
        }
      }

      String msg = property.getXMLPropertySet(message);

      sendXML(msg);
    } else {
      throw new INDIException("The Property is not from this driver. Maybe you forgot to add it?");
    }
  }

  /**
   * Notifies the clients about a new property with a
   * <code>message</code>. The
   * <code>message</code> can be
   * <code>null</code> if there is nothing to special to say.
   *
   * @param property The property that will be notified.
   * @param message
   */
  private void sendDefXXXVectorMessage(INDIProperty property, String message) {
    String msg = property.getXMLPropertyDefinition(message);

    sendXML(msg);
  }

  /**
   * Sends a XML message to the clients.
   *
   * @param XML The message to be sended.
   */
  private void sendXML(String XML) {
    /*
     * if (XML.length() < 500) { printMessage(XML); }
     */
    out.print(XML);
    out.flush();
  }

  /**
   *
   * @param property The Property to be added.
   */
  /**
   * Removes a Property from the Device. A XML message about it will be send to
   * the clients. Drivers must call this method if they want to remove a
   * Property.
   *
   * @param property The property to be removed
   */
  protected void removeProperty(INDIProperty property) {
    removeProperty(property, null);
  }

  /**
   * Removes a Property from the Device with a
   * <code>message</code>. A XML message about it will be send to the clients.
   * Drivers must call this method if they want to remove a Property.
   *
   * @param property The property to be removed
   * @param message A message that will be included in the XML message to the
   * client.
   */
  protected void removeProperty(INDIProperty property, String message) {
    if (properties.containsValue(property)) {
      properties.remove(property.getName());

      sendDelPropertyMessage(property, message);
    }
  }

  /**
   * Removes the Device from the clients with a
   * <code>message</code>. A XML message about it will be send to the clients.
   * Drivers must call this method if they want to remove the entire device from
   * the clients. It should be used if the Driver is ending.
   *
   * @param message The message to be sended to the clients. It can be
   * <code>null</code> if there is nothing special to say.
   */
  protected void removeDevice(String message) {
    sendDelPropertyMessage(message);
  }

  /**
   * Sends a mesage to the client to remove the entire device.
   *
   * @param message A optional message (can be <code>null</code>).
   */
  private void sendDelPropertyMessage(String message) {
    String mm = "";

    if (message != null) {
      mm = " message=\"" + message + "\"";
    }

    String msg = "<delProperty device=\"" + this.getName() + "\" timestamp=\"" + INDIDateFormat.getCurrentTimestamp() + "\"" + mm + " />";

    sendXML(msg);
  }

  /**
   * Sends a message to the client to remove a Property with a
   * <code>message</code>.
   *
   * @param property The property that is being removed.
   * @param message The optional message (can be <code>null</code>).
   */
  private void sendDelPropertyMessage(INDIProperty property, String message) {
    String mm = "";

    if (message != null) {
      mm = " message=\"" + message + "\"";
    }

    String msg = "<delProperty device=\"" + this.getName() + "\" name=\"" + property.getName() + "\" timestamp=\"" + INDIDateFormat.getCurrentTimestamp() + "\"" + mm + " />";

    sendXML(msg);
  }

  /**
   * Gets a Property of the Driver given its name.
   *
   * @param propertyName The name of the Property to be retrieved.
   * @return The Property with <code>propertyName</code> name. <code>null</code>
   * if there is no property with that name.
   */
  protected INDIProperty getProperty(String propertyName) {
    return properties.get(propertyName);
  }

  /**
   * Gets the default Connection property (if the driver implements
   * INDIConnectionHandler)
   *
   * @return The standard Connection property if this driver implements
   * INDIConnectionHandler. <code>null</code> otherwise.
   */
  protected INDISwitchProperty getConnectionProperty() {
    if (this instanceof INDIConnectionHandler) {
      return connectionP;
    }

    return null;
  }

  /**
   * Gets a list of all the Properties in the Driver.
   *
   * @return A List of all the Properties in the Driver.
   */
  public ArrayList<INDIProperty> getPropertiesAsList() {
    return new ArrayList<INDIProperty>(properties.values());
  }

  /**
   * Write message to the INDI console (System.err)
   *
   * @param message The message to be printed.
   */
  public void printMessage(String message) {
    System.err.println(message);
    System.err.flush();
  }

  @Override
  public InputStream getInputStream() {
    return inputStream;
  }

  /**
   * Gets the
   * <code>OutputStream</code> of the driver (useful for subdrivers).
   *
   * @return The <code>OutputStream</code> of the driver.
   */
  public OutputStream getOutputStream() {
    return outputStream;
  }

  /**
   * A method that should be implemented when the driver is being destroyed to
   * stop threads, kill sub-drivers, etc. By default it calls
   * <code>removeDevice</code>.
   *
   * @see #removeDevice
   */
  public void isBeingDestroyed() {
    finishReader();
    removeDevice("Removing " + getName());
  }
}
