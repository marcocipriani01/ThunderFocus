package marcocipriani01.thunderfocus;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import marcocipriani01.thunderfocus.board.PowerBox;

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
    @SerializedName("External control")
    @Expose
    private ExternalControl externalControl = ExternalControl.NONE;
    @SerializedName("INDI port")
    @Expose
    private int indiServerPort = 7626;
    @SerializedName("ASCOM port")
    @Expose
    private int ascomBridgePort = 5001;
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
        String folder = System.getProperty("user.home");
        switch (Main.OPERATING_SYSTEM) {
            case WINDOWS -> folder += (folder.endsWith("\\") ? "" : "\\") + "AppData\\Roaming\\" + Main.APP_NAME + "\\";
            case LINUX -> folder += (folder.endsWith("/") ? "" : "/") + ".config/";
            case MACOS -> folder += (folder.endsWith("/") ? "" : "/") + "Library/Preferences/" + Main.APP_NAME + "/";
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

    public ExternalControl getExternalControl() {
        return externalControl;
    }

    public void setExternalControl(ExternalControl externalControl, SettingsListener caller) {
        this.externalControl = externalControl;
        for (SettingsListener l : listeners) {
            if (l != caller) l.updateSetting(externalControl);
        }
    }

    public int getIndiServerPort() {
        return indiServerPort;
    }

    public void setIndiServerPort(int indiServerPort, SettingsListener caller) {
        this.indiServerPort = indiServerPort;
        update(Value.INDI_PORT, caller, indiServerPort);
    }

    public int getAscomBridgePort() {
        return ascomBridgePort;
    }

    public void setAscomBridgePort(int ascomBridgePort, SettingsListener caller) {
        this.ascomBridgePort = ascomBridgePort;
        update(Value.ASCOM_PORT, caller, ascomBridgePort);
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

    public void setPowerBox(PowerBox powerBox, SettingsListener caller) {
        this.powerBox = powerBox;
        for (SettingsListener l : listeners) {
            if (l != caller) l.updateSetting(powerBox);
        }
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
        for (SettingsListener l : listeners) {
            if (l != caller) l.updateSetting(fokTicksUnit);
        }
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
            if (l != notMe) l.updateSetting(what, value);
        }
    }

    void update(Value what, SettingsListener notMe, String value) {
        for (SettingsListener l : listeners) {
            if (l != notMe) l.updateSetting(what, value);
        }
    }

    void update(Value what, SettingsListener notMe, boolean value) {
        for (SettingsListener l : listeners) {
            if (l != notMe) l.updateSetting(what, value);
        }
    }

    public enum Value {
        THEME, SERIAL_PORT, SHOW_REMOTE_INDI, INDI_PORT,
        FOK_TICKS_COUNT, FOK_TICKS_UNIT, FOK_MAX_TRAVEL, ASCOM_PORT
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

    public enum ExternalControl {
        NONE, ASCOM, INDI
    }

    public interface SettingsListener {
        void updateSetting(Value what, int value);

        void updateSetting(Value what, String value);

        void updateSetting(Value what, boolean value);

        void updateSetting(Units value);

        void updateSetting(PowerBox value);

        void updateSetting(ExternalControl value);
    }
}