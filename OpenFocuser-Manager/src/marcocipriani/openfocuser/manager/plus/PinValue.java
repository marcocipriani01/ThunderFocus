package marcocipriani.openfocuser.manager.plus;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import laazotea.indi.Constants;

import java.util.Objects;

/**
 * Represents the value of an Arduino pin.
 *
 * @author marcocipriani01
 * @version 1.0
 * @see ArduinoPin
 */
@SuppressWarnings({"unused", "WeakerAccess", "SameParameterValue"})
public class PinValue {

    /**
     * The value (PWM).
     */
    @Expose
    @SerializedName("PWM")
    private int value = 0;

    /**
     * Class constructor. Value = 0.
     */
    public PinValue() {

    }

    /**
     * Class constructor.
     *
     * @param pwmValue the initial value, Integer 0→255.
     */
    public PinValue(int pwmValue) {
        setValue(pwmValue);
    }

    /**
     * Class constructor.
     *
     * @param type  the type of value.
     * @param value the initial value.
     */
    public PinValue(ValueType type, Object value) {
        setValue(type, value);
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
        return constrain((int) Math.round(percentage * 2.55), 0, 255);
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
        return constrain((int) Math.round(pwm / 2.55), 0, 100);
    }

    /**
     * @param n   an input number
     * @param min a minimum value
     * @param max a maximum value
     * @return {@code min} if {@code n < min}, {@code max} if {@code n > max}, otherwise simply {@code n}.
     */
    public static int constrain(int n, int min, int max) {
        return (n >= max ? max : (n <= min ? min : n));
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean equals(PinValue obj) {
        return obj.getValuePwm() == value;
    }

    /**
     * @return Integer, 0→255
     * @see ValueType#PWM
     */
    public int getValuePwm() {
        return value;
    }

    /**
     * @return {@code true} or {@code false}
     * @see ValueType#BOOLEAN
     */
    public boolean getValueBoolean() {
        return value >= 255;
    }

    /**
     * @return Integer, 0→100%
     * @see ValueType#PERCENTAGE
     */
    public int getValuePercentage() {
        return pwmToPercentage(value);
    }

    /**
     * @return {@link laazotea.indi.Constants.SwitchStatus#ON} or {@link laazotea.indi.Constants.SwitchStatus#OFF}
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
                this.value = constrain((int) value, 0, 255);
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
        this.value = constrain(pwmValue, 0, 255);
    }

    /**
     * @return a String representation of this pin value.
     */
    @Override
    public String toString() {
        return (value == 255 ? "high" : (value == 0 ? "low" : ((int) Math.round(value / 2.55) + "%")));
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