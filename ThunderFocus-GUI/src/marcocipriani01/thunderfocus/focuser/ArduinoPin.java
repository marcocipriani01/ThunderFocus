package marcocipriani01.thunderfocus.focuser;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import org.indilib.i4j.Constants;

import java.util.Objects;

/**
 * Represents an Arduino pin, with an id (its number on the Arduino board), a name and a value.
 *
 * @author marcocipriani01
 * @version 2.0
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class ArduinoPin {

    @Expose
    @SerializedName("Pin")
    private int pin = 0;
    @SerializedName("Name")
    @Expose
    private String name = "Pin 0";
    private int value = 0;
    private boolean isPwm = false;
    private boolean autoModeEn = false;

    /**
     * Class constructor. For Gson only!
     */
    public ArduinoPin() {

    }

    /**
     * Class constructor. Value = 0.
     *
     * @param pin  the id of this pin (its number on the Arduino board).
     * @param name a name for this pin.
     */
    public ArduinoPin(int pin, String name) {
        this.pin = pin;
        this.name = name;
    }

    /**
     * Class constructor.
     *
     * @param pin   the id of this pin (its number on the Arduino board).
     * @param name  a name for this pin.
     * @param value an initial value. Integer, 0→255
     * @see #setValue(int)
     */
    public ArduinoPin(int pin, String name, int value, boolean isPwm, boolean autoModeEn) {
        this.pin = pin;
        this.name = name;
        this.value = constrain(value);
        this.isPwm = isPwm;
        this.autoModeEn = autoModeEn;
    }

    /**
     * Copy constructor.
     *
     * @param other another pin to copy.
     */
    public ArduinoPin(ArduinoPin other) {
        value = other.value;
        name = other.name;
        pin = other.pin;
        autoModeEn = other.autoModeEn;
    }

    private static int constrain(int n) {
        return (n >= 255 ? 255 : (Math.max(n, 0)));
    }

    public boolean isPwm() {
        return isPwm;
    }

    public void setPwm(boolean pwm) {
        isPwm = pwm;
    }

    public boolean isAutoModeEn() {
        return autoModeEn;
    }

    public void setAutoModeEn(boolean autoModeEn) {
        this.autoModeEn = autoModeEn;
    }

    /**
     * @return The value of the pin, integer, 0→255
     */
    public int getValuePwm() {
        return value;
    }

    /**
     * @return The value of the pin, {@code true} or {@code false}
     */
    public boolean getValueBoolean() {
        return value > 100;
    }

    /**
     * @return The value of the pin, {@link Constants.SwitchStatus#ON} or {@link Constants.SwitchStatus#OFF}
     */
    public Constants.SwitchStatus getValueIndi() {
        return value > 100 ? Constants.SwitchStatus.ON : Constants.SwitchStatus.OFF;
    }

    /**
     * Sets a new value to this pin.
     *
     * @param value a new value for this pin, 0→255.
     */
    public void setValue(int value) {
        this.value = isPwm ? constrain(value) : (value > 100 ? 255 : 0);
    }

    public void setValue(boolean value) {
        this.value = value ? 255 : 0;
    }

    public void setValue(Constants.SwitchStatus value) {
        this.value = value == Constants.SwitchStatus.ON ? 255 : 0;
    }

    /**
     * @return the pin id.
     */
    public int getPin() {
        return pin;
    }

    /**
     * @param pin a new pin id.
     */
    public void setPin(int pin) {
        this.pin = pin;
    }

    /**
     * @return the name of this pin.
     */
    public String getName() {
        return name;
    }

    /**
     * @param name a new name for this pin.
     */
    public void setName(String name) {
        this.name = Objects.requireNonNull(name, "Null name!");
    }

    @Override
    public String toString() {
        return "Pin " + pin + " is \"" + name + "\", value: " + (isPwm ? value : (value > 100 ? "HIGH" : "LOW"));
    }
}