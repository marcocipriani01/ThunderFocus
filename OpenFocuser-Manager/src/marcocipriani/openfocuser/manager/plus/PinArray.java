package marcocipriani.openfocuser.manager.plus;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;

/**
 * Represents a list of Arduino pins.
 *
 * @author marcocipriani01
 * @version 1.0
 * @see PinValue
 * @see ArduinoPin
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class PinArray {

    /**
     * The list.
     */
    @SerializedName("List")
    @Expose
    private ArrayList<ArduinoPin> list;

    /**
     * Class constructor. Initializes an empty list.
     */
    public PinArray() {
        list = new ArrayList<>();
    }

    /**
     * Looks for duplicated pins in a list.
     *
     * @param pins lists of pins
     * @return {@code true} if no duplicates are found.
     * @throws IndexOutOfBoundsException if a pin is outside the allowed bounds (2 ≤ pin ≤ 99)
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean checkPins(ArduinoPin[]... pins) {
        LinkedHashSet<Integer> checker = new LinkedHashSet<>();
        int size = 0;
        for (ArduinoPin[] a : pins) {
            size += a.length;
            for (ArduinoPin p : a) {
                int n = p.getPin();
                if ((n < 2) || (n > 99)) {
                    throw new IndexOutOfBoundsException("Invalid pin: " + p + "\" is outside the allowed bounds (2 ≤ pin ≤ 99)!");
                }
                checker.add(n);
            }
        }
        return checker.size() == size;
    }

    /**
     * @return the size of the list.
     */
    public int size() {
        return list.size();
    }

    /**
     * @param pin a pin to look for.
     * @return {@code true} if this list contains the given pin.
     */
    public boolean contains(int pin) {
        for (ArduinoPin ap : list) {
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
        for (ArduinoPin ap : list) {
            if (ap.getPin() == param) {
                return true;
            }
        }
        return false;
    }

    /**
     * @param pin a pin to look for.
     * @return the index of the first occurrence of the specified element in this list, or -1 if this list does not contain the element.
     */
    public int indexOf(ArduinoPin pin) {
        return list.indexOf(pin);
    }

    /**
     * @return an array containing all the pins of this list.
     */
    public ArduinoPin[] toArray() {
        Object[] array = list.toArray();
        return Arrays.copyOf(array, array.length, ArduinoPin[].class);
    }

    /**
     * Adds a pin to the list.
     *
     * @param pin the pin to be added.
     */
    public void add(ArduinoPin pin) {
        if (pin == null) {
            throw new NullPointerException("Null pin!");
        }
        if (contains(pin)) {
            throw new IllegalStateException("Pin already in list!");
        }
        list.add(pin);
    }

    /**
     * Removes the given pin from the list.
     *
     * @param pin a pin to remove.
     */
    public void remove(ArduinoPin pin) {
        if (pin == null) {
            throw new NullPointerException("Null pin!");
        }
        list.remove(pin);
    }

    /**
     * Clears the list.
     */
    public void clear() {
        list.clear();
    }
}