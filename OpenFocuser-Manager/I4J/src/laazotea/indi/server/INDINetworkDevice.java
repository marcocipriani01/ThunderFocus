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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import laazotea.indi.INDIException;

/**
 * A class that represent a Network Device (another INDI server).
 *
 * @author S. Alonso (Zerjillo) [zerjioi at ugr.es]
 * @version 1.32, January 19, 2013
 */
public class INDINetworkDevice extends INDIDevice {

  /**
   * The socket to connect for the INDI Server
   */
  private Socket socket;
  /**
   * The host to connect for the INDI Server
   */
  private String host;
  /**
   * The port to connect for the INDI Server
   */
  private int port;
  /**
   * A list of names of the Device (it may be more than one)
   */
  private ArrayList<String> names;

  /**
   * Constructs a new Network Device and connects to it.
   * @param server The server which listens to this Device.
   * @param host The host to connect for the Device.
   * @param port The port to connect for the Device.
   * @throws INDIException if there is any problem with the connection.
   */
  protected INDINetworkDevice(AbstractINDIServer server, String host, int port) throws INDIException {
    super(server);

    names = new ArrayList<String>();

    this.host = host;
    this.port = port;

    try {
      socket = new Socket();

      socket.connect(new InetSocketAddress(host, port), 20000);
    } catch (IOException e) {
      throw new INDIException("Problem connecting to " + host + ":" + port);
    }
  }

  /**
   * Gets a String with the host and port of the connection.
   * @return A String with the host and port of the connection.
   */
  private String getNetworkName() {
    return host + ":" + port;
  }

  /**
   * Deals with a possible new Device name, adding it if it is new.
   *
   * @param possibleNewName The new possible new name.
   */
  @Override
  protected void dealWithPossibleNewDeviceName(String possibleNewName) {
    if (!names.contains(possibleNewName)) {
      names.add(possibleNewName);
    }
  }

  /**
   * Checks if the Device has a particular name.
   *
   * @param name The name to check.
   * @return
   * <code>true</code> if the Device respond to
   * <code>name</code>.
   * <code>false</code> otherwise.
   */
  @Override
  protected boolean hasName(String name) {
    if (names.contains(name)) {
      return true;
    }

    return false;
  }

  @Override
  public void closeConnections() {
    try {
      socket.close();
    } catch (IOException e) {
    }
  }

  @Override
  public InputStream getInputStream() {
    try {
      return socket.getInputStream();
    } catch (IOException e) {
    }

    return null;
  }

  @Override
  public OutputStream getOutputStream() {
    try {
      return socket.getOutputStream();
    } catch (IOException e) {
    }

    return null;
  }

  @Override
  public String getDeviceIdentifier() {
    return getNetworkName();
  }

  @Override
  public boolean isDevice(String deviceIdentifier) {
    return getDeviceIdentifier().equals(deviceIdentifier);
  }

  @Override
  protected String[] getNames() {
    String[] ns = new String[names.size()];

    for (int i = 0; i < ns.length; i++) {
      ns[i] = names.get(i);
    }

    return ns;
  }

  /**
   * Gets a String representation of the Device.
   *
   * @return A String representation of the Device.
   */
  @Override
  public String toString() {
    return "Network Device: " + this.getNetworkName() + " - " + printArray(getNames());
  }
  
  private String printArray(String[] arr) {
    String res = "[";
    
    for (int i = 0 ; i < arr.length ; i++) {
      if (i != arr.length - 1) {
        res += ", ";
      }
      res += arr[i];
    }
    
    res += "]";
    
    return res;
  }

  @Override
  public void isBeingDestroyed() {
  }
}
