package marcocipriani.openfocuser.manager.plus;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import laazotea.indi.Constants;

import java.util.Objects;

/**
 * Represents an Arduino pin, with an id (its number on the Arduino board), a name and a value.
 *
 * @author marcocipriani01
 * @version 1.0
 * @see PinValue
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class ArduinoPin {

    /**
     * The pin id (its number on the Arduino board).
     */
    @Expose
    @SerializedName("Pin")
    private int pin = -1;
    /**
     * The pin name.
     */
    @SerializedName("Name")
    @Expose
    private String name = "A pin";
    /**
     * The pin value.
     */
    @SerializedName("Value")
    @Expose
    private PinValue value = new PinValue();

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
        if (name != null) {
            this.name = name;
        }
    }

    /**
     * Class constructor.
     *
     * @param pin   the id of this pin (its number on the Arduino board).
     * @param name  a name for this pin.
     * @param value an initial value.
     * @see #setValue(PinValue.ValueType, Object)
     */
    public ArduinoPin(int pin, String name, PinValue value) {
        this(pin, name);
        this.value = Objects.requireNonNull(value, "Null pin value!");
    }

    /**
     * Class constructor.
     *
     * @param pin      the id of this pin (its number on the Arduino board).
     * @param name     a name for this pin.
     * @param pwmValue an initial value. Integer, 0→255
     * @see #setValue(int)
     */
    public ArduinoPin(int pin, String name, int pwmValue) {
        this(pin, name);
        this.value = new PinValue(pwmValue);
    }

    /**
     * @return Integer, 0→255
     * @see PinValue.ValueType#PWM
     */
    public int getValuePwm() {
        return value.getValuePwm();
    }

    /**
     * @return {@code true} or {@code false}
     * @see PinValue.ValueType#BOOLEAN
     */
    public boolean getValueBoolean() {
        return value.getValueBoolean();
    }

    /**
     * @return Integer, 0→100%
     * @see PinValue.ValueType#PERCENTAGE
     */
    public int getValuePercentage() {
        return value.getValuePercentage();
    }

    /**
     * @return {@link Constants.SwitchStatus#ON} or {@link Constants.SwitchStatus#OFF}
     * @see PinValue.ValueType#INDI
     */
    public Constants.SwitchStatus getValueIndi() {
        return value.getValueIndi();
    }

    /**
     * Sets a new value to this pin.
     *
     * @param type  the type of value.
     * @param value the initial value.
     * @see PinValue.ValueType
     */
    public void setValue(PinValue.ValueType type, Object value) {
        this.value.setValue(type, value);
    }

    /**
     * Sets a new value to this pin.
     *
     * @param pwmValue a new value for this pin, 0→255.
     */
    public void setValue(int pwmValue) {
        value.setValue(pwmValue);
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
     * @return the stored value of the pin.
     */
    public PinValue getPinValueObj() {
        return value;
    }

    /**
     * @param value the new value.
     */
    public void setPinValueObj(PinValue value) {
        this.value = Objects.requireNonNull(value);
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
        return "Pin " + pin + " is \"" + name + "\", value: " + value.toString();
    }
}