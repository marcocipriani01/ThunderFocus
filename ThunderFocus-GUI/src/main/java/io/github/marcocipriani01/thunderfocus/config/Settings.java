package io.github.marcocipriani01.thunderfocus.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import io.github.marcocipriani01.thunderfocus.Main;
import io.github.marcocipriani01.thunderfocus.board.ArduinoPin;

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
 * @version 1.3
 */
public class Settings {

    /**
     * An instance of {@link Gson} to save all the settings.
     */
    protected static final Gson serializer = new GsonBuilder()
            .setPrettyPrinting().serializeNulls().excludeFieldsWithoutExposeAnnotation().create();
    protected static Path filePath = null;
    protected static String folder = null;
    protected final ArrayList<SettingsListener> listeners = new ArrayList<>();
    @SerializedName("Relative step size")
    @Expose
    public int relativeStepSize = 10;
    @SerializedName("Focuser presets")
    @Expose
    public LinkedHashMap<Integer, String> presets = new LinkedHashMap<>();
    @SerializedName("AppTheme")
    @Expose
    public Theme theme = Theme.SYSTEM;
    @SerializedName("Show IP in INDI driver")
    @Expose
    public boolean showIpIndiDriver = (Main.OPERATING_SYSTEM != Main.OperatingSystem.LINUX);
    @SerializedName("INDI server")
    @Expose
    public boolean indiServer = (Main.OPERATING_SYSTEM != Main.OperatingSystem.WINDOWS);
    @SerializedName("ASCOM bridge")
    @Expose
    public boolean ascomBridge = (Main.OPERATING_SYSTEM == Main.OperatingSystem.WINDOWS);
    @SerializedName("INDI port")
    @Expose
    public int indiServerPort = (Main.OPERATING_SYSTEM == Main.OperatingSystem.WINDOWS) ? 7624 : 7626;
    @SerializedName("ASCOM port")
    @Expose
    public int ascomBridgePort = 5001;
    @SerializedName("Focuser ticks count")
    @Expose
    public int focuserTicksCount = 70;
    @SerializedName("Focuser ticks unit")
    @Expose
    public Units focuserTicksUnit = Units.TICKS;
    @SerializedName("Auto connect")
    @Expose
    public boolean autoConnect = true;
    @Expose
    @SerializedName("Powerbox pins")
    public ArrayList<ArduinoPin> powerBoxPins = new ArrayList<>();
    @SerializedName("Serial port")
    @Expose
    protected String serialPort = "";
    @SerializedName("Focuser max travel")
    @Expose
    protected int focuserMaxTravel = 32767;

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

    protected static Path getSettingsFilePath() throws IOException {
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
        normalize(s);
        return s;
    }

    protected static void normalize(Settings s) {
        // Normalize invalid values
        if (s.indiServerPort <= 1024 || s.indiServerPort >= 65535) s.indiServerPort = 7625;
        if (s.ascomBridgePort <= 1024 || s.ascomBridgePort >= 65535) s.ascomBridgePort = 5001;
        if (s.focuserTicksCount < 10 || s.focuserTicksCount >= 2147483647) s.focuserTicksCount = 70;
        if (s.focuserMaxTravel < 1 || s.focuserMaxTravel >= 2147483647) s.focuserMaxTravel = 32767;
        if (s.theme == null) s.theme = Theme.LIGHT;
        if (s.focuserTicksUnit == null) s.focuserTicksUnit = Units.TICKS;
        if (s.powerBoxPins == null) s.powerBoxPins = new ArrayList<>();
        if (s.relativeStepSize <= 0) s.relativeStepSize = 10;
    }

    protected void save(Path path) throws IOException {
        Files.write(path, serializer.toJson(this).getBytes());
    }

    public void save() throws IOException {
        save(getSettingsFilePath());
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

    public int getFocuserMaxTravel() {
        return focuserMaxTravel;
    }

    public void setFokMaxTravel(int fokMaxTravel, SettingsListener caller) {
        this.focuserMaxTravel = fokMaxTravel;
        for (SettingsListener l : listeners) {
            if (l != caller) l.updateFocuserMaxTravel(fokMaxTravel);
        }
    }

    public enum Theme {
        SYSTEM(i18n("system.theme")),
        LIGHT(i18n("light")),
        DARK(i18n("dark")),
        Arc_Dark("Arc Dark", "Arc Dark.json"),
        Atom_One_Dark("Atom One Dark", "Atom One Dark.json"),
        Atom_One_Light("Atom One Light", "Atom One Light.json"),
        Dracula("Dracula", "Dracula.json"),
        GitHub_Dark("GitHub Dark", "GitHub Dark.json"),
        GitHub("GitHub", "GitHub.json"),
        Light_Owl("Light Owl", "Light Owl.json"),
        Material_Darker("Material Darker", "Material Darker.json"),
        Material_Deep_Ocean("Material Deep Ocean", "Material Deep Ocean.json"),
        Material_Lighter("Material Lighter", "Material Lighter.json"),
        Material_Oceanic("Material Oceanic", "Material Oceanic.json"),
        Material_Palenight("Material Palenight", "Material Palenight.json"),
        Monokai_Pro("Monokai Pro", "Monokai Pro.json"),
        Moonlight("Moonlight", "Moonlight.json"),
        Night_Owl("Night Owl", "Night Owl.json"),
        Solarized_Dark("Solarized Dark", "Solarized Dark.json"),
        Solarized_Light("Solarized Light", "Solarized Light.json");

        private final String name;
        private final String fileName;

        Theme(String name) {
            this.name = name;
            this.fileName = null;
        }

        Theme(String name, String fileName) {
            this.name = name;
            this.fileName = fileName;
        }

        public String getFileName() {
            return fileName;
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

    public interface SettingsListener {
        void updateFocuserMaxTravel(int value);

        void updateSerialPort(String value);
    }
}