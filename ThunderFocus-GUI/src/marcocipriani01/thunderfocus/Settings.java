package marcocipriani01.thunderfocus;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import marcocipriani01.thunderfocus.focuser.PowerBox;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Locale;

/**
 * Stores all the app's settings.
 *
 * @author marcocipriani01
 * @version 1.0
 */
public class Settings {

    /**
     * An instance of {@link Gson} to save all the settings.
     */
    private static final Gson serializer = new GsonBuilder()
            .setPrettyPrinting().serializeNulls().excludeFieldsWithoutExposeAnnotation().create();
    private static Path path = null;
    private final ArrayList<SettingsListener> listeners = new ArrayList<>();

    @SerializedName("Theme")
    @Expose
    private int theme = 0;
    @SerializedName("Serial port")
    @Expose
    private String serialPort = "";
    @SerializedName("Enable INDI")
    @Expose
    private boolean indiEnabled = false;
    @SerializedName("INDI port")
    @Expose
    private int indiServerPort = 7626;
    @SerializedName("Show IP for INDI server")
    @Expose
    private boolean showRemoteIndi = false;
    @SerializedName("Focuser ticks count")
    @Expose
    private int fokTicksCount = 70;
    @SerializedName("Focuser ticks unit")
    @Expose
    private Units fokTicksUnit = Units.TICKS;
    @SerializedName("Focuser max travel")
    @Expose
    private int fokMaxTravel = 32767;
    @SerializedName("Powerbox data")
    @Expose
    private PowerBox powerBox = new PowerBox();

    /**
     * Class constructor.
     */
    public Settings() {

    }

    private static Path getPath() throws IOException, IllegalStateException {
        if (path != null) return path;
        String folder = System.getProperty("user.home"),
                os = System.getProperty("os.name", "generic").toLowerCase(Locale.ENGLISH);
        if (os.contains("win")) {
            folder += (folder.endsWith("\\") ? "" : "\\") + "AppData\\Roaming\\" + Main.APP_NAME + "\\";
        } else if (os.contains("nux")) {
            folder += (folder.endsWith("/") ? "" : "/") + ".config/";
        } else if ((os.contains("mac")) || (os.contains("darwin"))) {
            folder += (folder.endsWith("/") ? "" : "/") + "Library/Preferences/" + Main.APP_NAME + "/";
        }
        File f = new File(folder);
        if (f.exists()) {
            if (f.isFile()) {
                throw new IllegalStateException("Unable to create config folder, already exists and is a file");
            }
        } else {
            if (!f.mkdirs()) {
                throw new IOException("Unable to create config folder.");
            }
        }
        return (path = Paths.get(folder + File.separator + Main.APP_NAME + ".json"));
    }

    public static Settings load() {
        Settings s;
        try {
            s = serializer.fromJson(new String(Files.readAllBytes(getPath())), Settings.class);
        } catch (NoSuchFileException e) {
            return new Settings();
        } catch (Exception e) {
            e.printStackTrace();
            return new Settings();
        }
        // Normalize invalid values
        if (s.indiServerPort <= 1024) {
            s.indiServerPort = 7625;
        }
        return s;
    }

    public void addListener(SettingsListener listener) {
        listeners.add(listener);
    }

    public void removeListener(SettingsListener listener) {
        listeners.remove(listener);
    }

    public int getTheme() {
        return theme;
    }

    public void setTheme(int theme, SettingsListener caller) {
        this.theme = theme;
        update(Value.THEME, caller, theme);
    }

    public String getSerialPort() {
        return serialPort;
    }

    public void setSerialPort(String serialPort, SettingsListener caller) {
        this.serialPort = serialPort;
        update(Value.SERIAL_PORT, caller, serialPort);
    }

    public boolean isIndiEnabled() {
        return indiEnabled;
    }

    public void setIndiEnabled(boolean indiEnabled, SettingsListener caller) {
        this.indiEnabled = indiEnabled;
        update(Value.IS_INDI_ENABLED, caller, indiEnabled);
    }

    public int getIndiServerPort() {
        return indiServerPort;
    }

    public void setIndiServerPort(int indiServerPort, SettingsListener caller) {
        this.indiServerPort = indiServerPort;
        update(Value.INDI_PORT, caller, indiServerPort);
    }

    public boolean getShowRemoteIndi() {
        return showRemoteIndi;
    }

    public void setShowRemoteIndi(boolean showRemoteIndi, SettingsListener caller) {
        this.showRemoteIndi = showRemoteIndi;
        update(Value.SHOW_REMOTE_INDI, caller, showRemoteIndi);
    }

    public PowerBox getPowerBox() {
        return powerBox;
    }

    public void setDigitalPins(PowerBox digitalPins, SettingsListener caller) {
        this.powerBox = digitalPins;
        update(Value.POWERBOX_PINS, caller, digitalPins);
    }

    public int getFokTicksCount() {
        return fokTicksCount;
    }

    public void setFokTicksCount(int fokTicksCount, SettingsListener caller) {
        this.fokTicksCount = fokTicksCount;
        update(Value.FOK_TICKS_COUNT, caller, fokTicksCount);
    }

    public Units getFokTicksUnit() {
        return fokTicksUnit;
    }

    public void setFokTicksUnit(Units fokTicksUnit, SettingsListener caller) {
        this.fokTicksUnit = fokTicksUnit;
        update(Value.FOK_TICKS_UNIT, caller, fokTicksUnit);
    }

    public int getFokMaxTravel() {
        return fokMaxTravel;
    }

    public void setFokMaxTravel(int fokMaxTravel, SettingsListener caller) {
        this.fokMaxTravel = fokMaxTravel;
        update(Value.FOK_MAX_TRAVEL, caller, fokMaxTravel);
    }

    public void save() throws IOException {
        Files.write(getPath(), serializer.toJson(this).getBytes());
    }

    void update(Value what, SettingsListener notMe, int value) {
        for (SettingsListener l : listeners) {
            if (l != notMe) l.update(what, value);
        }
    }

    void update(Value what, SettingsListener notMe, String value) {
        for (SettingsListener l : listeners) {
            if (l != notMe) l.update(what, value);
        }
    }

    void update(Value what, SettingsListener notMe, Units value) {
        for (SettingsListener l : listeners) {
            if (l != notMe) l.update(what, value);
        }
    }

    void update(Value what, SettingsListener notMe, boolean value) {
        for (SettingsListener l : listeners) {
            if (l != notMe) l.update(what, value);
        }
    }

    void update(Value what, SettingsListener notMe, PowerBox value) {
        for (SettingsListener l : listeners) {
            if (l != notMe) l.update(what, value);
        }
    }

    public enum Value {
        THEME, SERIAL_PORT,
        IS_INDI_ENABLED, SHOW_REMOTE_INDI, INDI_PORT,
        FOK_TICKS_COUNT, FOK_TICKS_UNIT, FOK_MAX_TRAVEL,
        POWERBOX_PINS
    }

    public enum Units {
        TICKS("Tacche"),
        uMETERS("Micrometri"),
        MILLIMETERS("Millimetri"),
        TENTH_OF_MILLIMETERS("Decimi di mm"),
        CENTIMETERS("Centimetri"),
        DEGREES("Gradi");

        private final String name;

        Units(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    public interface SettingsListener {
        void update(Value what, int value);

        void update(Value what, String value);

        void update(Value what, Units value);

        void update(Value what, boolean value);

        void update(Value what, PowerBox value);
    }
}