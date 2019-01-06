/*
 *  This file is part of INDI for Java Server.
 * 
 *  INDI for Java Server is free software: you can redistribute it
 *  and/or modify it under the terms of the GNU General Public License 
 *  as published by the Free Software Foundation, either version 3 of 
 *  the License, or (at your option) any later version.
 * 
 *  INDI for Java Server is distributed in the hope that it will be
 *  useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 *  of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with INDI for Java Server.  If not, see 
 *  <http://www.gnu.org/licenses/>.
 */
package laazotea.indi.server;

import java.net.Socket;
import java.util.ArrayList;
import org.w3c.dom.Element;

/**
 * A class representing a default implementation of an INDI Server. It
 * implements all the abstract methods of
 * <code>AbstractINDIServer</code> to allow any client that connects to access
 * any device that the Server loads. New Servers that do not implement specific
 * restrictions may extend this class instead of the AbstractINDIServer.
 *
 * @author S. Alonso (Zerjillo) [zerjioi at ugr.es]
 * @version 1.31, April 12, 2012
 * @see AbstractINDIServer
 */
public class DefaultINDIServer extends AbstractINDIServer {

  /**
   * Constructs the server.
   */
  public DefaultINDIServer() {
    super();
  }

  /**
   * Constructs the server with a particular port.
   *
   * @param port The port to which the server will listen.
   */
  public DefaultINDIServer(int port) {
    super(port);
  }

  /**
   * Sends the
   * <code>defXXXVector</code> message to the appropriate Clients.
   *
   * @param device The Device sending the message.
   * @param xml The message
   */
  @Override
  protected void notifyDeviceListenersDefXXXVector(INDIDevice device, Element xml) {
    String deviceName = xml.getAttribute("device").trim();
    String propertyName = xml.getAttribute("name").trim();

    ArrayList<INDIDeviceListener> list = getClientsListeningToProperty(deviceName, propertyName);

    for (int i = 0 ; i < list.size() ; i++) {
      INDIDeviceListener c = list.get(i);

      c.sendXMLMessage(xml);
    }
  }

  /**
   * Sends the
   * <code>setXXXVector</code> message to the appropriate Clients.
   *
   * @param device The Device sending the message.
   * @param xml The message
   */
  @Override
  protected void notifyDeviceListenersSetXXXVector(INDIDevice device, Element xml) {
    String deviceName = xml.getAttribute("device").trim();
    String propertyName = xml.getAttribute("name").trim();

    String messageType = xml.getTagName();
    boolean isBLOB = false;
    
    if (messageType.equals("setBLOBVector")) {
      isBLOB = true;
    }
     
    ArrayList<INDIDeviceListener> list = getClientsListeningToPropertyUpdates(deviceName, propertyName, isBLOB);

    for (int i = 0 ; i < list.size() ; i++) {
      INDIDeviceListener c = list.get(i);

      c.sendXMLMessage(xml);
    }
  }

  /**
   * Sends the
   * <code>message</code> message to the appropriate Clients..
   *
   * @param device The Device sending the message.
   * @param xml The message
   */
  @Override
  protected void notifyDeviceListenersMessage(INDIDevice device, Element xml) {
    String deviceName = xml.getAttribute("device").trim();

    if (deviceName.length() == 0) {
      sendXMLMessageToAllClients(xml);
    } else {
      ArrayList<INDIDeviceListener> list = getClientsListeningToDevice(deviceName);

      for (int i = 0 ; i < list.size() ; i++) {
        INDIDeviceListener c = list.get(i);

        c.sendXMLMessage(xml);
      }
    }
  }

  /**
   * Sends the
   * <code>delProperty</code> message to the appropriate Clients.
   *
   * @param device The Device sending the message.
   * @param xml The message
   */
  @Override
  protected void notifyDeviceListenersDelProperty(INDIDevice device, Element xml) {
    String deviceName = xml.getAttribute("device").trim();
   
    ArrayList<INDIDeviceListener> list = getClientsListeningToDevice(deviceName);
          
    for (int i = 0 ; i < list.size() ; i++) {
      INDIDeviceListener c = list.get(i);

      c.sendXMLMessage(xml);
    }
  }

  /**
   * Sends the
   * <code>getProperties</code> message to the appropriate Devices.
   *
   * @param client The Client sending the message.
   * @param xml The message
   */
  @Override
  protected void notifyClientListenersGetProperties(INDIDeviceListener client, Element xml) {
//    System.err.println("CLIENT ASKED FOR PROPERTIES");

    String device = xml.getAttribute("device").trim();

    INDIDevice d = this.getDevice(device);

    if (d == null) {
      sendXMLMessageToAllDevices(xml);
    } else {
      d.sendXMLMessage(xml);
    }
  }

  /**
   * Sends the
   * <code>newXXXVector</code> message to the appropriate Devices.
   *
   * @param client The Client sending the message.
   * @param xml The message
   */
  @Override
  protected void notifyClientListenersNewXXXVector(INDIClient client, Element xml) {
    String device = xml.getAttribute("device").trim();
    INDIDevice d = this.getDevice(device);

    if (d != null) {
      d.sendXMLMessage(xml);
    }
  }

  /**
   * Does nothing since the enableBLOB message is not usually useful for
   * Devices. The control of when to send the BLOB values is automatically done
   * by the Server.
   *
   * @param client The Client sending the message.
   * @param xml The message
   */
  @Override
  protected void notifyClientListenersEnableBLOB(INDIClient client, Element xml) {
    /*
     * String device = xml.getAttribute("device").trim(); INDIDevice d =
     * this.getDevice(device);
     *
     * if (d != null) { d.sendXMLMessage(xml);
    }
     */
  }

  /**
   * Accepts all Clients.
   *
   * @param socket
   * @return
   * <code>true</code>
   */
  @Override
  protected boolean acceptClient(Socket socket) {
    return true;
  }

  /**
   * Does nothing.
   *
   * @param client
   */
  @Override
  protected void connectionWithClientBroken(INDIClient client) {
  }

  /**
   * Does nothing.
   *
   * @param client
   */
  @Override
  protected void connectionWithClientEstablished(INDIClient client) {
  }

  /**
   * Does nothing
   * 
   * @param driverIdentifier
   * @param deviceNames 
   */
  @Override
  protected void driverDisconnected(String driverIdentifier, String[] deviceNames) {
  }
}
