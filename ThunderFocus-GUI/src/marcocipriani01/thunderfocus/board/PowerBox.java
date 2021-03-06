package marcocipriani01.thunderfocus.board;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.Arrays;

import static marcocipriani01.thunderfocus.Main.i18n;

/**
 * Represents a powerbox.
 *
 * @author marcocipriani01
 * @version 2.0
 * @see ArduinoPin
 */
@SuppressWarnings("unused")
public class PowerBox {

    public static final double ABSOLUTE_ZERO = -273.0;
    public static final double INVALID_HUMIDITY = -1.0;

    @Expose
    @SerializedName("List")
    private final ArrayList<ArduinoPin> pins = new ArrayList<>();
    private AutoModes autoMode = AutoModes.NIGHT_ASTRONOMICAL;
    private boolean supportsTime = false;
    private boolean supportsAmbient = false;
    private double temperature = ABSOLUTE_ZERO;
    private double humidity = INVALID_HUMIDITY;
    private double dewPoint = ABSOLUTE_ZERO;
    private double latitude = 0.0;
    private double longitude = 0.0;
    private double sunElev = Double.MIN_VALUE;

    /**
     * Class constructor. Initializes an empty list.
     */
    public PowerBox() {

    }

    /**
     * Copy constructor.
     */
    public PowerBox(PowerBox pb) {
        for (ArduinoPin ap : pb.pins) {
            pins.add(new ArduinoPin(ap));
        }
        this.autoMode = pb.autoMode;
        this.supportsTime = pb.supportsTime;
        this.supportsAmbient = pb.supportsAmbient;
        this.temperature = pb.temperature;
        this.humidity = pb.humidity;
        this.dewPoint = pb.dewPoint;
        this.latitude = pb.latitude;
        this.longitude = pb.longitude;
        this.sunElev = pb.sunElev;
    }

    public double getSunElev() {
        return sunElev;
    }

    void setSunElev(double sunElev) {
        this.sunElev = sunElev;
    }

    public double getLatitude() {
        return latitude;
    }

    void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getTemperature() {
        return temperature;
    }

    void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    public double getHumidity() {
        return humidity;
    }

    void setHumidity(double humidity) {
        this.humidity = humidity;
    }

    public double getDewPoint() {
        return dewPoint;
    }

    void setDewPoint(double dewPoint) {
        this.dewPoint = dewPoint;
    }

    public ArrayList<ArduinoPin> asList() {
        return pins;
    }

    public ArrayList<ArduinoPin> listOnlyPwm() {
        ArrayList<ArduinoPin> list = new ArrayList<>();
        for (ArduinoPin ap : pins) {
            if (ap.isPwm()) list.add(ap);
        }
        return list;
    }

    public ArrayList<ArduinoPin> listOnlyDigital() {
        ArrayList<ArduinoPin> list = new ArrayList<>();
        for (ArduinoPin ap : pins) {
            if (!ap.isPwm()) list.add(ap);
        }
        return list;
    }

    public AutoModes getAutoMode() {
        return autoMode;
    }

    void setAutoMode(AutoModes autoMode) {
        this.autoMode = autoMode;
    }

    void setAutoMode(int index) {
        this.autoMode = AutoModes.values()[index];
    }

    public boolean supportsTime() {
        return supportsTime;
    }

    @SuppressWarnings("SameParameterValue")
    void setSupportsTime(boolean supportsTime) {
        this.supportsTime = supportsTime;
    }

    public boolean supportsAmbient() {
        return supportsAmbient;
    }

    void setSupportsAmbient(boolean supportsAmbient) {
        this.supportsAmbient = supportsAmbient;
    }

    public int size() {
        return pins.size();
    }

    public int countDigitalPins() {
        int count = 0;
        for (ArduinoPin ap : pins) {
            if (!ap.isPwm()) count++;
        }
        return count;
    }

    public int countPwmPins() {
        int count = 0;
        for (ArduinoPin ap : pins) {
            if (ap.isPwm()) count++;
        }
        return count;
    }

    public ArduinoPin getIndex(int index) {
        return pins.get(index);
    }

    void clear() {
        pins.clear();
    }

    /**
     * @param pin a pin to look for.
     * @return {@code true} if this list contains the given pin.
     */
    public boolean contains(int pin) {
        for (ArduinoPin ap : pins) {
            if (ap.getNumber() == pin) {
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
        int param = pin.getNumber();
        for (ArduinoPin ap : pins) {
            if (ap.getNumber() == param) {
                return true;
            }
        }
        return false;
    }

    public ArduinoPin getPin(int pin) {
        for (ArduinoPin ap : pins) {
            if (ap.getNumber() == pin) {
                return ap;
            }
        }
        return null;
    }

    void remove(ArduinoPin pin) {
        for (int i = 0; i < pins.size(); i++) {
            if (pins.get(i).getNumber() == pin.getNumber()) {
                pins.remove(i);
                return;
            }
        }
    }

    /**
     * Adds a pin to the list.
     *
     * @param pin the pin to be added.
     */
    void add(ArduinoPin pin) {
        if (pin == null) throw new NullPointerException("Null pin!");
        if (pins.contains(pin)) throw new IllegalArgumentException("Pin already in list!");
        pins.add(pin);
    }

    public AutoModes[] supportedAutoModesArray() {
        Object[] array = supportedAutoModes().toArray();
        return Arrays.copyOf(array, array.length, AutoModes[].class);
    }

    public ArrayList<AutoModes> supportedAutoModes() {
        ArrayList<AutoModes> list = new ArrayList<>();
        if (supportsTime) {
            list.add(AutoModes.NIGHT_ASTRONOMICAL);
            list.add(AutoModes.NIGHT_CIVIL);
            list.add(AutoModes.DAYTIME);
        }
        if (supportsAmbient) {
            list.add(AutoModes.DEW_POINT_DIFF1);
            list.add(AutoModes.DEW_POINT_DIFF2);
            list.add(AutoModes.DEW_POINT_DIFF3);
            list.add(AutoModes.DEW_POINT_DIFF5);
            list.add(AutoModes.DEW_POINT_DIFF7);
            list.add(AutoModes.HUMIDITY_90);
            list.add(AutoModes.HUMIDITY_80);
            list.add(AutoModes.HUMIDITY_70);
            list.add(AutoModes.TEMP_FREEZE);
        }
        return list;
    }

    public boolean supportsAutoModes() {
        return supportsAmbient || supportsTime;
    }

    public enum AutoModes {
        NIGHT_ASTRONOMICAL(i18n("night.astronomical")),
        NIGHT_CIVIL(i18n("night.civil")),
        DAYTIME(i18n("day")),
        DEW_POINT_DIFF1(i18n("dew.point.delta") + "1"),
        DEW_POINT_DIFF2(i18n("dew.point.delta") + "2"),
        DEW_POINT_DIFF3(i18n("dew.point.delta") + "3"),
        DEW_POINT_DIFF5(i18n("dew.point.delta") + "5"),
        DEW_POINT_DIFF7(i18n("dew.point.delta") + "7"),
        HUMIDITY_90(i18n("humidity") + " >90%"),
        HUMIDITY_80(i18n("humidity") + " >80%"),
        HUMIDITY_70(i18n("humidity") + " >70%"),
        TEMP_FREEZE(i18n("cold")),
        UNAVAILABLE(i18n("not.available"));

        private final String label;

        AutoModes(String label) {
            this.label = label;
        }

        @Override
        public String toString() {
            return label;
        }
    }
}