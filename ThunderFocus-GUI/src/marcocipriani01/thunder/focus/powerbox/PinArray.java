package marcocipriani01.thunder.focus.powerbox;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Represents a list of Arduino pins.
 *
 * @author marcocipriani01
 * @version 1.2
 * @see ArduinoPin
 */
public class PinArray extends ArrayList<ArduinoPin> {

    /**
     * Class constructor. Initializes an empty list.
     */
    public PinArray() {

    }

    /**
     * Class constructor. Initializes an empty list.
     */
    public PinArray(PinArray pa) {
        for (ArduinoPin ap : pa) {
            add(new ArduinoPin(ap));
        }
    }

    /**
     * @param pin a pin to look for.
     * @return {@code true} if this list contains the given pin.
     */
    public boolean contains(int pin) {
        for (ArduinoPin ap : this) {
            if (ap.getPin() == pin) {
                return true;
            }
        }
        return false;
    }

    /**
     * @param pin a pin to look for.
     * @return {@code true} if this list contains the given pin.
     */
    public boolean contains(ArduinoPin pin) {
        if (pin == null) {
            throw new NullPointerException("Null pin!");
        }
        int param = pin.getPin();
        for (ArduinoPin ap : this) {
            if (ap.getPin() == param) {
                return true;
            }
        }
        return false;
    }

    public ArduinoPin getPin(int pin) {
        for (ArduinoPin ap : this) {
            if (ap.getPin() == pin) {
                return ap;
            }
        }
        return null;
    }

    /**
     * @return an array containing all the pins of this list.
     */
    @Override
    public ArduinoPin[] toArray() {
        Object[] array = super.toArray();
        return Arrays.copyOf(array, array.length, ArduinoPin[].class);
    }

    public void remove(ArduinoPin pin) {
        for (int i = 0; i < size(); i++) {
            if (get(i).getPin() == pin.getPin()) {
                remove(i);
                return;
            }
        }
    }

    /**
     * Adds a pin to the list.
     *
     * @param pin the pin to be added.
     */
    @Override
    public boolean add(ArduinoPin pin) {
        if (pin == null) {
            throw new NullPointerException("Null pin!");
        }
        if (contains(pin)) {
            throw new IllegalArgumentException("Pin already in list!");
        }
        super.add(pin);
        return true;
    }
}