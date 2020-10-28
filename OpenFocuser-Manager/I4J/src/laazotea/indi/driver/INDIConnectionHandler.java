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

import java.util.Date;
import laazotea.indi.INDIException;

/**
 * An interface for those drivers that wish to have a standard CONNECTION
 * property. Note that any INDIDriver implementing this interface will
 * automatically include the connection property. No code will be necessary in
 * the Driver code to include or manage it.
 *
 * @author S. Alonso (Zerjillo) [zerjioi at ugr.es]
 * @version 1.11, March 26, 2012
 */
public interface INDIConnectionHandler {

  /**
   * The method that will handle the connection.
   *
   * @param timestamp when the connection message has been received.
   */
  public void driverConnect(Date timestamp) throws INDIException;

  /**
   * The method that will handle the disconnection.
   *
   * @param timestamp when the disconnection message has been received.
   */
  public void driverDisconnect(Date timestamp) throws INDIException;
}
