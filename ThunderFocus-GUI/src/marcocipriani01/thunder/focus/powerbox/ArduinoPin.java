package marcocipriani01.thunder.focus.powerbox;

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
     * @param pin      the id of this pin (its number on the Arduino board).
     * @param name     a name for this pin.
     * @param pwmValue an initial value. Integer, 0→255
     * @see #setValue(int)
     */
    public ArduinoPin(int pin, String name, int pwmValue) {
        this(pin, name);
        this.value = constrain(pwmValue);
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
    }

    private static int constrain(int n) {
        return (n >= 255 ? 255 : (Math.max(n, 0)));
    }

    /**
     * @return The value of the pin, integer, 0→255
     * @see ValueType#PWM
     */
    public int getValuePwm() {
        return value;
    }

    /**
     * @return The value of the pin, {@code true} or {@code false}
     * @see ValueType#BOOLEAN
     */
    public boolean getValueBoolean() {
        return value > 100;
    }

    /**
     * @return The value of the pin in percentage, {@link Constants.SwitchStatus#ON} or {@link Constants.SwitchStatus#OFF}
     * @see ValueType#INDI
     */
    public Constants.SwitchStatus getValueIndi() {
        return value > 100 ? Constants.SwitchStatus.ON : Constants.SwitchStatus.OFF;
    }

    /**
     * Sets a new value to this pin.
     *
     * @param type  the type of value.
     * @param value the initial value.
     * @see ValueType
     */
    public void setValue(ValueType type, Object value) {
        Objects.requireNonNull(value, "Null value!");
        switch (Objects.requireNonNull(type)) {
            case PWM -> {
                if (!(value instanceof Integer)) {
                    throw new IllegalArgumentException("Invalid value!");
                }
                this.value = constrain((int) value);
            }

            case INDI -> {
                if (!(value instanceof Constants.SwitchStatus)) {
                    throw new IllegalArgumentException("Invalid value!");
                }
                this.value = value == Constants.SwitchStatus.ON ? 255 : 0;
            }

            case BOOLEAN -> {
                if (!(value instanceof Boolean)) {
                    throw new IllegalArgumentException("Invalid value!");
                }
                this.value = ((boolean) value) ? 255 : 0;
            }

            default -> throw new UnsupportedOperationException("Unsupported type of pin value!");
        }
    }

    /**
     * Sets a new value to this pin.
     *
     * @param pwmValue a new value for this pin, 0→255.
     */
    public void setValue(int pwmValue) {
        value = constrain(pwmValue);
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
        return "Pin " + pin + " is \"" + name + "\", value: " + (value == 255 ? "high" : (value == 0 ? "low" : ((int) Math.round(value / 2.55) + "%")));
    }

    /**
     * All the possible value types for Arduino pins.
     *
     * @author marcocipriani01
     * @version 1.0
     */
    public enum ValueType {
        /**
         * Integer, 0→255
         */
        PWM,
        /**
         * {@link Constants.SwitchStatus#ON} or {@link Constants.SwitchStatus#OFF}
         */
        INDI,
        BOOLEAN
    }
}