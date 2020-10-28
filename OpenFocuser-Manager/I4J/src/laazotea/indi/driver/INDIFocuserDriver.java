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
import java.util.Date;
import laazotea.indi.Constants.PropertyPermissions;
import laazotea.indi.Constants.PropertyStates;
import laazotea.indi.Constants.SwitchStatus;
import laazotea.indi.INDIException;

/**
 * A class representing a Focuser Driver in the INDI Protocol. INDI Focuser
 * Drivers should extend this class. It is in charge of handling the following
 * properties for Focusers:
 * <ul>
 * <li>FOCUS_SPEED -> FOCUS_SPEED_VALUE (number)</li>
 * <li>ABS_FOCUS_POSITION -> FOCUS_ABSOLUTE_POSITION (number)</li>
 * <li>stop_focusing (single switch)</li>
 * </ul>
 *
 * It is <strong>VERY IMPORTANT</strong> that any subclasses use
 * <code>super.processNewSwitchValue(property, timestamp, elementsAndValues);</code>
 * and
 * <code>super.processNewNumberValue(property, timestamp, elementsAndValues);</code>
 * at the beginning of
 * <code>processNewSwitchValue</code> and
 * <code>processNewNumberValue</code> to handle the generic focuser properties
 * correctly.
 *
 * @author S. Alonso (Zerjillo) [zerjioi at ugr.es]
 * @version 1.38, July 22, 2014
 */
public abstract class INDIFocuserDriver extends INDIDriver {

  /**
   * The last position to which the focuser has been sent (but it may have not
   * yet reached).
   */
  private int desiredAbsPosition;
  /**
   * The
   * <code>FOCUS_SPEED</code> property.
   */
  private INDINumberProperty focusSpeedP;
  /**
   * The
   * <code>FOCUS_SPEED_VALUE</code> element.
   */
  private INDINumberElement focusSpeedValueE;
  /**
   * The
   * <code>ABS_FOCUS_POSITION</code> property.
   */
  private INDINumberProperty absFocusPositionP;
  /**
   * The
   * <code>FOCUS_ABSOLUTE_POSITION</code> element.
   */
  private INDINumberElement focusAbsolutePositionE;
  /**
   * The
   * <code>stop_focusing</code> property (not standard, but very useful)
   */
  private INDISwitchOneOrNoneProperty stopFocusingP;

  /**
   * Constructs a INDIFocuserDriver with a particular
   * <code>inputStream<code> from which to read the incoming messages (from clients) and a
   * <code>outputStream</code> to write the messages to the clients.
   *
   * @param inputStream The stream from which to read messages
   * @param outputStream The stream to which to write the messages
   */
  public INDIFocuserDriver(InputStream inputStream, OutputStream outputStream) {
    super(inputStream, outputStream);
  }

  /**
   * Initializes the standard properties. MUST BE CALLED BY SUBDRIVERS.
   */
  protected void initializeStandardProperties() {
    absFocusPositionP = new INDINumberProperty(this, "ABS_FOCUS_POSITION", "Absolute", "Control", PropertyStates.IDLE, PropertyPermissions.RW);
    focusAbsolutePositionE = new INDINumberElement(absFocusPositionP, "FOCUS_ABSOLUTE_POSITION", "Focus Position", getInitialAbsPos() + "", getMinimumAbsPos() + "", getMaximumAbsPos() + "", "1", "%.0f");
    desiredAbsPosition = getInitialAbsPos();

    addProperty(absFocusPositionP);
  }

  /**
   * Gets the maximum speed of the focuser. Note that 0 is the minimum speed for
   * any focuser. Must be overloaded if the driver uses the
   * <code>FOCUS_SPEED</code> property.
   *
   * @return The maximum speed of the focuser
   */
  protected int getMaximumSpeed() {
    return 0;
  }

  /**
   * Called when the
   * <code>FOCUS_SPEED</code> property has been changed. Must be overloaded if
   * the driver uses the
   * <code>FOCUS_SPEED</code> property.
   */
  public void speedHasBeenChanged() {
  }

  /**
   * Returns the maximum value that the
   * <code>FOCUS_ABSOLUTE_POSITION</code> element can have.
   *
   * @return The maximum value
   */
  public abstract int getMaximumAbsPos();

  /**
   * Returns the minimum value that the
   * <code>FOCUS_ABSOLUTE_POSITION</code> element can have.
   *
   * @return The minimum value
   */
  public abstract int getMinimumAbsPos();

  /**
   * Returns the initial value that the
   * <code>FOCUS_ABSOLUTE_POSITION</code> element shuld have.
   *
   * @return The initial position
   */
  public abstract int getInitialAbsPos();

  /**
   * Called when the
   * <code>ABS_FOCUS_POSITION</code> property has been changed.
   */
  public abstract void absolutePositionHasBeenChanged();

  /**
   * Called when the
   * <code>stop_focusing</code> property has been changed. Must be overloaded if
   * the driver uses the
   * <code>stop_focusing</code> property.
   */
  public void stopHasBeenRequested() {
  }

  /**
   * Shows the standard
   * <code>FOCUS_SPEED</code> property. Must be called by drivers that want to
   * use this property.
   */
  protected void showSpeedProperty() {
    if (focusSpeedP == null) {
      focusSpeedP = INDINumberProperty.createSaveableNumberProperty(this, "FOCUS_SPEED", "Focus Speed", "Configuration", PropertyStates.IDLE, PropertyPermissions.RW);
      focusSpeedValueE = focusSpeedP.getElement("FOCUS_SPEED_VALUE");
      if (focusSpeedValueE == null) {
        focusSpeedValueE = new INDINumberElement(focusSpeedP, "FOCUS_SPEED_VALUE", "Focus Speed", getMaximumSpeed() + "", "0", "" + getMaximumSpeed(), "1", "%.0f");
      }
    }

    addProperty(focusSpeedP);
  }

  /**
   * Shows the NON standard
   * <code>stop_focusing</code> property. Must be called by drivers that want to
   * use this property.
   */
  protected void showStopFocusingProperty() {
    if (stopFocusingP == null) {
      stopFocusingP = new INDISwitchOneOrNoneProperty(this, "stop_focusing", "Stop", "Control", PropertyStates.IDLE, PropertyPermissions.RW, "Stop Focusing", SwitchStatus.OFF);
    }

    addProperty(stopFocusingP);
  }

  /**
   * Hides the
   * <code>FOCUS_SPEED</code> property.
   */
  protected void hideSpeedProperty() {
    removeProperty(focusSpeedP);
  }

  /**
   * Hides the
   * <code>stop_focusing</code> property.
   */
  protected void hideStopFocusingProperty() {
    removeProperty(stopFocusingP);
  }

  /**
   * Gets the value of the
   * <code>FOCUS_SPEED_VALUE</code> element.
   *
   * @return The current speed value
   */
  protected int getCurrentSpeed() {
    if (focusSpeedValueE != null) {
      int speed = focusSpeedValueE.getValue().intValue();

      return speed;
    }

    return -1;
  }

  /**
   * Gets the desired absolute position (which may not have been reached by the
   * focuser).
   *
   * @return The desired absolute position
   */
  protected int getDesiredAbsPosition() {
    return desiredAbsPosition;
  }

  /**
   * Must be called by drivers when the final position for the focuser has been
   * reached.
   */
  protected void finalPositionReached() {
    absFocusPositionP.setState(PropertyStates.OK);

    try {
      updateProperty(absFocusPositionP);
    } catch (INDIException e) {
    }
  }

  /**
   * Must be called by drivers when a new speed has been set.
   */
  protected void desiredSpeedSet() {
    focusSpeedP.setState(PropertyStates.OK);

    try {
      updateProperty(focusSpeedP);
    } catch (INDIException e) {
    }
  }

  /**
   * Must be called by drivers when the focuser stops (only when it has been
   * asked to stop)
   */
  protected void stopped() {
    stopFocusingP.setState(PropertyStates.OK);

    try {
      updateProperty(stopFocusingP);
    } catch (INDIException e) {
    }
  }

  /**
   * Should be called by the drivers when the focuser its moving. It can be
   * called with any frequency, but a less than one second is preferred to
   * notify the clients of the movement of the focuser.
   *
   * @param currentPos The current position of the focuser.
   */
  protected void positionChanged(int currentPos) {
    focusAbsolutePositionE.setValue("" + currentPos);

    try {
      updateProperty(absFocusPositionP);
    } catch (INDIException e) {
    }
  }
  
  /**
   * Should be called by the drivers when the focuser speed changes (if for
   * example the device has a potentiometer to control the speed). 
   *
   * @param currentSpeed The current speed of the focuser.
   */
  protected void speedChanged(int currentSpeed) {
    focusSpeedValueE.setValue("" + currentSpeed);

    try {
      updateProperty(focusSpeedP);
    } catch (INDIException e) {
    }
  }

  @Override
  public void processNewSwitchValue(INDISwitchProperty property, Date timestamp, INDISwitchElementAndValue[] elementsAndValues) {
    if (property == stopFocusingP) {
      stopFocusingP.setState(PropertyStates.BUSY);
      stopFocusingP.setStatus(SwitchStatus.OFF);
      try {
        updateProperty(stopFocusingP);
      } catch (INDIException e) {
      }

      stopHasBeenRequested();
    }
  }

  @Override
  public void processNewNumberValue(INDINumberProperty property, Date timestamp, INDINumberElementAndValue[] elementsAndValues) {
    if (property == absFocusPositionP) {
      int newVal = elementsAndValues[0].getValue().intValue();

      if ((newVal >= getMinimumAbsPos()) && (newVal <= getMaximumAbsPos())) {
        if (focusAbsolutePositionE.getValue().intValue() != newVal) {
          absFocusPositionP.setState(PropertyStates.BUSY);

          desiredAbsPosition = newVal;

          try {
            updateProperty(absFocusPositionP);
          } catch (INDIException e) {
          }

          absolutePositionHasBeenChanged();
        } else {
          absFocusPositionP.setState(PropertyStates.OK);
          try {
            updateProperty(absFocusPositionP);
          } catch (INDIException e) {
          }
        }
      }
    }

    if (property == focusSpeedP) {
      int newVal = elementsAndValues[0].getValue().intValue();

      if ((newVal >= 0) && (newVal <= getMaximumSpeed())) {
        if (focusSpeedValueE.getValue().intValue() != newVal) {
          focusSpeedP.setState(PropertyStates.BUSY);

          focusSpeedValueE.setValue("" + newVal);

          try {
            updateProperty(focusSpeedP);
          } catch (INDIException e) {
          }

          speedHasBeenChanged();
        } else {
          focusSpeedP.setState(PropertyStates.OK);

          try {
            updateProperty(focusSpeedP);
          } catch (INDIException e) {
          }
        }
      }
    }
  }
}
