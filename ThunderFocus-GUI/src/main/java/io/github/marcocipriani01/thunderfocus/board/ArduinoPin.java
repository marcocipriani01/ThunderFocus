package io.github.marcocipriani01.thunderfocus.board;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import org.indilib.i4j.Constants;

import java.util.Objects;

/**
 * Represents an Arduino pin, with an id (the number on the Arduino board), a name and a value.
 *
 * @author marcocipriani01
 * @version 2.0
 */
public class ArduinoPin {

    @Expose
    @SerializedName("Number")
    private int number = 0;
    @SerializedName("Name")
    @Expose
    private String name = "Pin 0";
    private int value = 0;
    private boolean isPWM = false;
    private boolean enablePWM = false;
    private boolean autoModeEn = false;
    @SerializedName("ON when app is open")
    @Expose
    private boolean onWhenAppOpen = false;

    /**
     * Class constructor. For Gson only!
     */
    @SuppressWarnings("unused")
    public ArduinoPin() {

    }
    /**
     * Class constructor.
     *
     * @param number the id of this pin (the number on the Arduino board).
     * @param name   a name for this pin.
     * @param value  an initial value. Integer, 0→255
     * @see #setValue(int)
     */
    public ArduinoPin(int number, String name, int value, boolean autoModeEn, boolean onWhenAppOpen) {
        this.number = number;
        this.name = name;
        this.value = constrain(value);
        this.isPWM = this.enablePWM = false;
        this.autoModeEn = autoModeEn;
        this.onWhenAppOpen = onWhenAppOpen;
    }

    /**
     * Class constructor.
     *
     * @param number the id of this pin (the number on the Arduino board).
     * @param name   a name for this pin.
     * @param value  an initial value. Integer, 0→255
     * @see #setValue(int)
     */
    public ArduinoPin(int number, String name, int value, boolean enablePWM, boolean autoModeEn, boolean onWhenAppOpen) {
        this.number = number;
        this.name = name;
        this.value = constrain(value);
        this.isPWM = true;
        this.enablePWM = enablePWM;
        this.autoModeEn = autoModeEn;
        this.onWhenAppOpen = onWhenAppOpen;
    }

    /**
     * Copy constructor.
     *
     * @param other another pin to copy.
     */
    public ArduinoPin(ArduinoPin other) {
        value = other.value;
        name = other.name;
        number = other.number;
        autoModeEn = other.autoModeEn;
        isPWM = other.isPWM;
        enablePWM = other.enablePWM;
        onWhenAppOpen = other.onWhenAppOpen;
    }

    public static int constrain(int n) {
        return (n >= 255 ? 255 : (Math.max(n, 0)));
    }

    public boolean isPWMEnabled() {
        return isPWM && enablePWM;
    }

    public boolean isDigitalPin() {
        return (!isPWM) || (!enablePWM);
    }

    public void setPWMEnabled(boolean enablePWM) {
        if (!isPWM)
            throw new IllegalStateException("This pin is not a PWM pin!");
        this.enablePWM = enablePWM;
        if (!enablePWM)
            value = (value > 100) ? 255 : 0;
    }

    public boolean isOnWhenAppOpen() {
        return onWhenAppOpen;
    }

    public void setOnWhenAppOpen(boolean onWhenAppOpen) {
        this.onWhenAppOpen = onWhenAppOpen;
    }

    public boolean isPWM() {
        return isPWM;
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
    public int getValuePWM() {
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
    public Constants.SwitchStatus getValueINDI() {
        return value > 100 ? Constants.SwitchStatus.ON : Constants.SwitchStatus.OFF;
    }

    /**
     * Sets a new value to this pin.
     *
     * @param value a new value for this pin, 0→255.
     */
    public void setValue(int value) {
        this.value = isPWMEnabled() ? constrain(value) : (value > 100 ? 255 : 0);
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
    public int getNumber() {
        return number;
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
        return "Pin " + number + " is \"" + name + "\", value: " + (enablePWM ? value : (value > 100 ? "HIGH" : "LOW"));
    }
}