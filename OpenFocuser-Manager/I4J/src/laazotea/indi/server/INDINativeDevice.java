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
import laazotea.indi.INDIException;

/**
 * A class that represent a Native Device (created with the usual INDI library).
 * @author S. Alonso (Zerjillo) [zerjioi at ugr.es]
 * @version 1.32, January 13, 2013
 */
public class INDINativeDevice extends INDIDevice {

  /**
   * The path of the Driver (that will be launched).
   */
  private String driverPath;
  /**
   * The process that will be launched to start the Driver.
   */
  private Process process;

    /**
   * The name of the device. May be null if it has not been discovered through a <code>defXXXVector</code> message.
   */
  private String name;
  
  /**
   * Constructs a new Native Device and launches it as a external process.
   * @param server The server which listens to this Device.
   * @param driverPath The path of of the Driver.
   * @throws INDIException If there is any problem launching the external process of the driver.
   */
  protected INDINativeDevice(AbstractINDIServer server, String driverPath) throws INDIException {
    super(server);
    
    name = null;
    
    this.driverPath = driverPath;

    try {
      process = Runtime.getRuntime().exec(driverPath);
    } catch (IOException e) {
      throw new INDIException("Problem executing " + driverPath);
    }
  }

  /**
   * Gets the path of the Driver.
   * @return The path of the Driver.
   */
  public String getDriverPath() {
    return driverPath;
  }

      /**
   * Deals with a possible new Device name. If the Device already has a name, the new name is discarded.
   * @param possibleNewName The new possible new name.
   */
  @Override
  protected void dealWithPossibleNewDeviceName(String possibleNewName) {
    if (name == null) {
      name = possibleNewName; 
    }
  }
  
  /**
   * Checks if the Device has a particular name.
   * @param name The name to check.
   * @return <code>true</code> if the Device respond to <code>name</code>. <code>false</code> otherwise.
   */
  @Override
  protected boolean hasName(String name) {
    if (this.name == null) {
      return false;
    }

    if (this.name.equals(name)) {
      return true; 
    }
    
    return false;
  }
  
  @Override
  public void closeConnections() {
    process.destroy();
  }

  @Override
  public InputStream getInputStream() {
    return process.getInputStream();
  }

  @Override
  public OutputStream getOutputStream() {
    return process.getOutputStream();
  }

  @Override
  public String getDeviceIdentifier() {
    return driverPath;
  }
  
  @Override
  public boolean isDevice(String deviceIdentifier) {
    return getDeviceIdentifier().equals(deviceIdentifier);
  }  
  
  @Override
  protected String[] getNames() {
    return new String[] {name};
  }
  
  /**
   * Gets a String representation of the Device.
   *
   * @return A String representation of the Device.
   */
  @Override
  public String toString() {
    return "Native Device: " + driverPath;
  }

  @Override
  public void isBeingDestroyed() {
  }
}
