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

import laazotea.indi.Constants.BLOBEnables;

/**
 * A class that represents a tuple of Device and Property names and a BLOBEnable.
 *
 * @author S. Alonso (Zerjillo) [zerjioi at ugr.es]
 * @version 1.31, April 12, 2012
 */
public class DevicePropertyBLOBEnableTuple {

  /**
   * The Device name
   */
  private String device;
  /**
   * The Property name
   */
  private String property;
  
  /**
   * The BLOB enable.
   */
  private BLOBEnables enable;

  /**
   * Constructs a new DevicePropertyBLOBEnableTuple. 
   * @param device The Device name.
   * @param property The Property name.
   * @param enable The BLOB Enable. 
   */
  protected DevicePropertyBLOBEnableTuple(String device, String property, BLOBEnables enable) {
    this.device = device;
    this.property = property;
    this.enable = enable;
  }
  
  /**
   * Constructs a new DevicePropertyBLOBEnableTuple with a BLOB Enable set to <code>null</code>.
   * @param device The Device name.
   * @param property The Property name.
   */
  protected DevicePropertyBLOBEnableTuple(String device, String property) {
    this.device = device;
    this.property = property;
    this.enable = null;
  }
  
  /**
   * Constructs a new DevicePropertyBLOBEnableTuple. 
   * @param device The Device name.
   * @param enable The BLOB Enable. 
   */
  protected DevicePropertyBLOBEnableTuple(String device, BLOBEnables enable) {
    this.device = device;
    this.property = null;
    this.enable = enable;
  }
  
  /**
   * Constructs a new DevicePropertyBLOBEnableTuple with a Property name and BLOB Enable set to <code>null</code>.
   * @param device The Device name.
   */
  protected DevicePropertyBLOBEnableTuple(String device) {
    this.device = device;
    this.property = null;
    this.enable = null;
  }

  /**
   * Gets the Device name.
   * @return the Device name.
   */
  protected String getDevice() {
    return device;
  }
  /**
   * Gets the Property name.
   * @return the Device name.
   */
  protected String getProperty() {
    return property;
  }
  
  /**
   * Gets the BLOB Enable.
   * @return the BLOB Enable.
   */
  protected BLOBEnables getBLOBEnable() {
    return enable;
  }

  /**
   * Checks if the Device has a particular name.
   * @param device The name of the Device to check.
   * @return <code>true</code> if the name of the Device coincides. <code>false</code> otherwise.
   */
  protected boolean isDevice(String device) {
    if (this.device.equals(device)) {
      return true;
    }

    return false;
  }

  /**
   * Checks if the Device has a particular name and the Property has a particular name.
   * @param device The name of the Device to check.
   * @param property The name of the Property to check.
   * @return <code>true</code> if the name of the Device coincides and the name of the Property coincides. <code>false</code> otherwise.
   */
  protected boolean isProperty(String device, String property) {
    if (this.device.equals(device)) {
      if ( (property == null) && (this.property == null) ) {
        return true;
      }
      if ( (property == null) && (this.property != null) ) {
        return false;
      }
      if ( (property != null) && (this.property == null) ) {
        return false;
      }
      if (this.property.equals(property)) {
        return true;
      }
    }

    return false;
  }
}
