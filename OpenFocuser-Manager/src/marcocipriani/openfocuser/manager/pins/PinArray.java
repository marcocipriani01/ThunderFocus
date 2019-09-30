package marcocipriani.openfocuser.manager.pins;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;

/**
 * Represents a list of Arduino pins.
 *
 * @author marcocipriani01
 * @version 1.2
 * @see ArduinoPin
 */
@SuppressWarnings("unused")
public class PinArray extends ArrayList<ArduinoPin> {

    /**
     * Class constructor. Initializes an empty list.
     */
    public PinArray() {

    }

    /**
     * Looks for duplicated pins.
     *
     * @param inCommonWith add other pin arrays to the check.
     * @return {@code true} if no duplicates are found.
     * @throws IndexOutOfBoundsException if a pin is outside the allowed bounds (2 ≤ pin ≤ 99)
     */
    public boolean hasDuplicates(PinArray... inCommonWith) {
        LinkedHashSet<Integer> checker = new LinkedHashSet<>();
        int sizeSum = 0;
        for (PinArray array : inCommonWith) {
            for (ArduinoPin pin : array) {
                checkPin(checker, pin);
            }
            sizeSum += array.size();
        }
        for (ArduinoPin pin : this) {
            checkPin(checker, pin);
        }
        return checker.size() != (sizeSum + size());
    }

    private void checkPin(LinkedHashSet<Integer> checker, ArduinoPin pin) {
        int n = pin.getPin();
        if ((n < 2) || (n > 99)) {
            throw new IndexOutOfBoundsException("Invalid pin: " + pin + "\" is outside the allowed bounds (2 ≤ pin ≤ 99)!");
        }
        checker.add(n);
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

    /**
     * @return an array containing all the pins of this list.
     */
    @Override
    public ArduinoPin[] toArray() {
        Object[] array = super.toArray();
        return Arrays.copyOf(array, array.length, ArduinoPin[].class);
    }

    public ArduinoPin remove(ArduinoPin pin) {
        for (int i = 0; i < size(); i++) {
            if (get(i).getPin() == pin.getPin()) {
                return remove(i);
            }
        }
        return null;
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
            throw new IllegalStateException("Pin already in list!");
        }
        super.add(pin);
        return true;
    }
}