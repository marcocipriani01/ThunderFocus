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

import static marcocipriani01.thunderfocus.Main.i18n;

/**
 * Stores all the app's settings.
 *
 * @author marcocipriani01
 * @version 1.1
 */
@SuppressWarnings({"SameParameterValue", "unused"})
public class Settings {

    /**
     * An instance of {@link Gson} to save all the settings.
     */
    private static final Gson serializer = new GsonBuilder()
            .setPrettyPrinting().serializeNulls().excludeFieldsWithoutExposeAnnotation().create();
    private static Path filePath = null;
    private static String folder = null;
    private final ArrayList<SettingsListener> listeners = new ArrayList<>();

    @SerializedName("AppTheme")
    @Expose
    private Theme theme = Theme.LIGHT;
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
    @SerializedName("INDI connection mode")
    @Expose
    private INDIConnectionMode indiConnectionMode = INDIConnectionMode.LOCAL;
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
    @SerializedName("Auto connect")
    @Expose
    private boolean autoConnect = false;

    /**
     * Class constructor.
     */
    public Settings() {

    }

    public static String getSettingsFolder() throws IOException {
        if (Settings.folder != null) return Settings.folder;
        String folder = System.getProperty("user.home");
        switch (Main.OPERATING_SYSTEM) {
            case WINDOWS -> folder += (folder.endsWith("\\") ? "" : "\\") + "AppData\\Roaming\\" + Main.APP_NAME + "\\";
            case LINUX -> folder += (folder.endsWith("/") ? "" : "/") + ".config/";
            case MACOS -> folder += (folder.endsWith("/") ? "" : "/") + "Library/Preferences/" + Main.APP_NAME + "/";
            case OTHER -> folder += (folder.endsWith(File.separator) ? "" : File.separator);
        }
        File f = new File(folder);
        if (f.exists()) {
            if (f.isFile()) throw new IOException("Unable to create the config folder, already exists and is a file");
        } else {
            if (!f.mkdirs()) throw new IOException("Unable to create the config folder.");
        }
        return (Settings.folder = folder);
    }

    private static Path getSettingsFilePath() throws IOException {
        if (filePath != null) return filePath;
        return (filePath = Paths.get(getSettingsFolder() + Main.APP_NAME + ".json"));
    }

    public static Settings load() {
        Settings s;
        try {
            s = serializer.fromJson(new String(Files.readAllBytes(getSettingsFilePath())), Settings.class);
        } catch (NoSuchFileException e) {
            return new Settings();
        } catch (Exception e) {
            e.printStackTrace();
            return new Settings();
        }
        // Normalize invalid values
        if (s.indiServerPort <= 1024) s.indiServerPort = 7625;
        if (s.ascomBridgePort <= 1024) s.ascomBridgePort = 5001;
        return s;
    }

    public void save() throws IOException {
        Files.write(getSettingsFilePath(), serializer.toJson(this).getBytes());
    }

    public boolean getAutoConnect() {
        return autoConnect;
    }

    public void setAutoConnect(boolean autoConnect, SettingsListener caller) {
        this.autoConnect = autoConnect;
        update(Value.AUTO_CONNECT, caller, this.autoConnect);
    }

    public void addListener(SettingsListener listener) {
        listeners.add(listener);
    }

    public void removeListener(SettingsListener listener) {
        listeners.remove(listener);
    }

    public Theme getTheme() {
        return theme;
    }

    public void setTheme(Theme theme, SettingsListener caller) {
        this.theme = theme;
        update(caller, theme);
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

    public INDIConnectionMode getIndiConnectionMode() {
        return indiConnectionMode;
    }

    public void setIndiConnectionMode(INDIConnectionMode indiConnectionMode, SettingsListener caller) {
        this.indiConnectionMode = indiConnectionMode;
        update(caller, indiConnectionMode);
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

    private void update(Value what, SettingsListener notMe, int value) {
        for (SettingsListener l : listeners) {
            if (l != notMe) l.updateSetting(what, value);
        }
    }

    private void update(SettingsListener notMe, Theme value) {
        for (SettingsListener l : listeners) {
            if (l != notMe) l.updateSetting(value);
        }
    }

    private void update(SettingsListener notMe, INDIConnectionMode value) {
        for (SettingsListener l : listeners) {
            if (l != notMe) l.updateSetting(value);
        }
    }

    private void update(Value what, SettingsListener notMe, String value) {
        for (SettingsListener l : listeners) {
            if (l != notMe) l.updateSetting(what, value);
        }
    }

    private void update(Value what, SettingsListener notMe, boolean value) {
        for (SettingsListener l : listeners) {
            if (l != notMe) l.updateSetting(what, value);
        }
    }

    public enum Theme {
        LIGHT(i18n("light")),
        DARK(i18n("dark")),
        SYSTEM(i18n("system.theme"));

        private final String name;

        Theme(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    public enum INDIConnectionMode {
        LOCAL(i18n("local")),
        REMOTE(i18n("remote"));

        private final String name;

        INDIConnectionMode(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    public enum Value {
        SERIAL_PORT, INDI_PORT, FOK_TICKS_COUNT, FOK_MAX_TRAVEL, AUTO_CONNECT, ASCOM_PORT
    }

    public enum Units {
        TICKS(i18n("ticks")),
        uMETERS(i18n("micrometers")),
        MILLIMETERS(i18n("millimeters")),
        TENTHS_OF_MILLIMETERS("tenths.of.millimeter"),
        CENTIMETERS(i18n("centimeters")),
        DEGREES(i18n("degrees"));

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
        default void updateSetting(Value what, int value) {}

        default void updateSetting(Value what, String value) {}

        default void updateSetting(Value what, boolean value) {}

        default void updateSetting(Theme value) {}

        default void updateSetting(INDIConnectionMode value) {}

        default void updateSetting(Units value) {}

        default void updateSetting(PowerBox value) {}

        default void updateSetting(ExternalControl value) {}
    }
}