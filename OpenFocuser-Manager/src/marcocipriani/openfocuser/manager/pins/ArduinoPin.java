package marcocipriani.openfocuser.manager.pins;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import laazotea.indi.Constants;

import java.util.Objects;

/**
 * Represents an Arduino pin, with an id (its number on the Arduino board), a name and a value.
 *
 * @author marcocipriani01
 * @version 2.0
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
    @SerializedName("PWM value")
    @Expose
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
        this.value = constrain(pwmValue, 255);
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

    /**
     * Converts a percentage to a PWM value.
     *
     * @param percentage the input percentage, 0→100%
     * @return the output PWM value.
     * @see ValueType#PERCENTAGE
     * @see ValueType#PWM
     */
    public static int percentageToPwm(int percentage) {
        return constrain((int) Math.round(percentage * 2.55), 255);
    }

    /**
     * Converts a PWM value to a percentage.
     *
     * @param pwm the input pwm value, 0→255
     * @return the output percentage.
     * @see ValueType#PERCENTAGE
     * @see ValueType#PWM
     */
    public static int pwmToPercentage(int pwm) {
        return constrain((int) Math.round(pwm / 2.55), 100);
    }

    /**
     * @param n   an input number
     * @param max a maximum value
     * @return {@code min} if {@code n < min}, {@code max} if {@code n > max}, otherwise simply {@code n}.
     */
    public static int constrain(int n, int max) {
        return (n >= max ? max : (Math.max(n, 0)));
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
        return value >= 255;
    }

    /**
     * @return The value of the pin in percentage, integer, 0→100%
     * @see ValueType#PERCENTAGE
     */
    public int getValuePercentage() {
        return pwmToPercentage(value);
    }

    /**
     * @return The value of the pin in percentage, {@link Constants.SwitchStatus#ON} or {@link Constants.SwitchStatus#OFF}
     * @see ValueType#INDI
     */
    public Constants.SwitchStatus getValueIndi() {
        return value == 255 ? Constants.SwitchStatus.ON : Constants.SwitchStatus.OFF;
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
            case PERCENTAGE: {
                if (!(value instanceof Integer)) {
                    throw new IllegalArgumentException("Invalid value!");
                }
                this.value = percentageToPwm((int) value);
                break;
            }

            case PWM: {
                if (!(value instanceof Integer)) {
                    throw new IllegalArgumentException("Invalid value!");
                }
                this.value = constrain((int) value, 255);
                break;
            }

            case INDI: {
                if (!(value instanceof Constants.SwitchStatus)) {
                    throw new IllegalArgumentException("Invalid value!");
                }
                this.value = value == Constants.SwitchStatus.ON ? 255 : 0;
                break;
            }

            case BOOLEAN: {
                if (!(value instanceof Boolean)) {
                    throw new IllegalArgumentException("Invalid value!");
                }
                this.value = ((boolean) value) ? 255 : 0;
                break;
            }

            default: {
                throw new UnsupportedOperationException("Unsupported type of pin value!");
            }
        }
    }

    /**
     * Sets a new value to this pin.
     *
     * @param pwmValue a new value for this pin, 0→255.
     */
    public void setValue(int pwmValue) {
        value = constrain(pwmValue, 255);
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
         * Integer, 0→100%
         */
        PERCENTAGE,
        /**
         * Integer, 0→255
         */
        PWM,
        /**
         * {@link laazotea.indi.Constants.SwitchStatus#ON} or {@link laazotea.indi.Constants.SwitchStatus#OFF}
         */
        INDI,
        /**
         * {@code true} or {@code false}
         */
        BOOLEAN
    }
}