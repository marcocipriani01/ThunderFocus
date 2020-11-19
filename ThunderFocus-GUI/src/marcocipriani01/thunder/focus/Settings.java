package marcocipriani01.thunder.focus;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import marcocipriani01.thunder.focus.powerbox.PinArray;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;

/**
 * Stores all the app's settings.
 *
 * @author marcocipriani01
 * @version 1.0
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class Settings {

    /**
     * An instance of {@link Gson} to save all the settings.
     */
    private static final Gson serializer = new GsonBuilder()
            .setPrettyPrinting().serializeNulls().excludeFieldsWithoutExposeAnnotation().create();

    @SerializedName("Theme")
    @Expose
    public int theme = 0;
    @SerializedName("Serial port")
    @Expose
    public String serialPort = "";
    @SerializedName("Enable INDI")
    @Expose
    public boolean enableIndi = false;
    @SerializedName("INDI port")
    @Expose
    public int indiServerPort = 7626;
    @SerializedName("Show IP for INDI server")
    @Expose
    public boolean showRemoteIndi = false;
    @SerializedName("Digital pins")
    @Expose
    public PinArray digitalPins = new PinArray();
    @SerializedName("PWM pins")
    @Expose
    public PinArray pwmPins = new PinArray();
    @SerializedName("Focuser ticks count")
    @Expose
    public int fokTicksCount = 70;
    @SerializedName("Focuser ticks unit")
    @Expose
    public Units fokTicksUnit = Units.TICKS;
    @SerializedName("Focuser max travel")
    @Expose
    public int fokMaxTravel = 32767;

    /**
     * Class constructor.
     */
    public Settings() {

    }

    private static Path getPath() throws IOException, IllegalStateException {
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
        return Paths.get(folder + File.separator + Main.APP_NAME + ".json");
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

    public void save() throws IOException {
        Files.write(getPath(), serializer.toJson(this).getBytes());
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
}