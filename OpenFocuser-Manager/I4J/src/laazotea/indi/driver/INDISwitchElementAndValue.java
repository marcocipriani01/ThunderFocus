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

import laazotea.indi.Constants.SwitchStatus;

/**
 * A class representing a pair of a
 * <code>INDISwitchElement</code> and a
 * <code>SwitchStatus</code>.
 *
 * @author S. Alonso (Zerjillo) [zerjioi at ugr.es]
 * @version 1.34, November 8, 2013
 */
public class INDISwitchElementAndValue implements INDIElementAndValue {

  /**
   * The Switch element
   */
  private final INDISwitchElement element;
  /**
   * The Switch status
   */
  private final SwitchStatus status;

  /**
   * Constructs an instance of a
   * <code>INDISwitchElementAndValue</code>. This class should not usually be
   * instantiated by specific Drivers.
   *
   * @param element The Switch Element
   * @param status The Switch Status
   */
  public INDISwitchElementAndValue(INDISwitchElement element, SwitchStatus status) {
    this.element = element;
    this.status = status;
  }

  @Override
  public INDISwitchElement getElement() {
    return element;
  }

  @Override
  public SwitchStatus getValue() {
    return status;
  }
}
