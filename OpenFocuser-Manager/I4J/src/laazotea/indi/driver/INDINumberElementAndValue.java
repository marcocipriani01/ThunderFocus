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

/**
 * A class representing a pair of a
 * <code>INDINumberElement</code> and a
 * <code>Double</code>.
 *
 * @author S. Alonso (Zerjillo) [zerjioi at ugr.es]
 * @version 1.34, November 8, 2013
 */
public class INDINumberElementAndValue implements INDIElementAndValue {

  /**
   * The Number element
   */
  private final INDINumberElement element;
  /**
   * The Number value
   */
  private final Double value;

  /**
   * Constructs an instance of a
   * <code>INDINumberElementAndValue</code>. This class should not usually be
   * instantiated by specific Drivers.
   *
   * @param element The Number Element
   * @param value The number
   */
  public INDINumberElementAndValue(INDINumberElement element, Double value) {
    this.element = element;
    this.value = value;
  }

  @Override
  public INDINumberElement getElement() {
    return element;
  }

  @Override
  public Double getValue() {
    return value;
  }
}
