package io.github.marcocipriani01.thunderfocus;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import io.github.marcocipriani01.thunderfocus.board.PowerBox;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;

import static io.github.marcocipriani01.thunderfocus.Main.i18n;

/**
 * Stores all the app's settings.
 *
 * @author marcocipriani01
 * @version 1.2
 */
public class Settings {

    /**
     * An instance of {@link Gson} to save all the settings.
     */
    private static final Gson serializer = new GsonBuilder()
            .setPrettyPrinting().serializeNulls().excludeFieldsWithoutExposeAnnotation().create();
    private static Path filePath = null;
    private static String folder = null;
    private final ArrayList<SettingsListener> listeners = new ArrayList<>();
    @SerializedName("Relative step size")
    @Expose
    public int relativeStepSize = 10;
    @SerializedName("Focuser presets")
    @Expose
    public LinkedHashMap<Integer, String> presets = new LinkedHashMap<>();
    @SerializedName("AppTheme")
    @Expose
    public Theme theme = Theme.LIGHT;
    @SerializedName("Show IP in INDI driver")
    @Expose
    public boolean showIpIndiDriver = false;
    @SerializedName("External control")
    @Expose
    public ExternalControl externalControl = ExternalControl.NONE;
    @SerializedName("INDI port")
    @Expose
    public int indiServerPort = 7626;
    @SerializedName("ASCOM port")
    @Expose
    public int ascomBridgePort = 5001;
    @SerializedName("Focuser ticks count")
    @Expose
    public int fokTicksCount = 70;
    @SerializedName("Focuser ticks unit")
    @Expose
    public Units fokTicksUnit = Units.TICKS;
    @SerializedName("Auto connect")
    @Expose
    public boolean autoConnect = false;
    @SerializedName("Serial port")
    @Expose
    private String serialPort = "";
    @SerializedName("Focuser max travel")
    @Expose
    private int fokMaxTravel = 32767;
    @SerializedName("Powerbox data")
    @Expose
    private PowerBox powerBox = new PowerBox();

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
        if (s.indiServerPort <= 1024 || s.indiServerPort >= 65535) s.indiServerPort = 7625;
        if (s.ascomBridgePort <= 1024 || s.ascomBridgePort >= 65535) s.ascomBridgePort = 5001;
        if (s.fokTicksCount < 10 || s.fokTicksCount >= 2147483647) s.fokTicksCount = 70;
        if (s.fokMaxTravel < 1 || s.fokMaxTravel >= 2147483647) s.fokMaxTravel = 32767;
        if (s.theme == null) s.theme = Theme.LIGHT;
        if (s.externalControl == null) s.externalControl = ExternalControl.NONE;
        if (s.fokTicksUnit == null) s.fokTicksUnit = Units.TICKS;
        if (s.powerBox == null) s.powerBox = new PowerBox();
        if (s.relativeStepSize <= 0) s.relativeStepSize = 10;
        return s;
    }

    public void save() throws IOException {
        Files.write(getSettingsFilePath(), serializer.toJson(this).getBytes());
    }

    public void addListener(SettingsListener listener) {
        listeners.add(listener);
    }

    public String getSerialPort() {
        return serialPort;
    }

    public void setSerialPort(String serialPort, SettingsListener caller) {
        this.serialPort = serialPort;
        for (SettingsListener l : listeners) {
            if (l != caller) l.updateSerialPort(serialPort);
        }
    }

    public PowerBox getPowerBox() {
        return powerBox;
    }

    public void setPowerBox(PowerBox powerBox) {
        this.powerBox = powerBox;
    }

    public int getFokMaxTravel() {
        return fokMaxTravel;
    }

    public void setFokMaxTravel(int fokMaxTravel, SettingsListener caller) {
        this.fokMaxTravel = fokMaxTravel;
        for (SettingsListener l : listeners) {
            if (l != caller) l.updateFocuserMaxTravel(fokMaxTravel);
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

    public enum Units {
        TICKS(i18n("ticks")),
        uMETERS(i18n("micrometers")),
        MILLIMETERS(i18n("millimeters")),
        TENTHS_OF_MILLIMETERS(i18n("tenths.of.millimeter")),
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
        void updateFocuserMaxTravel(int value);

        void updateSerialPort(String value);
    }
}