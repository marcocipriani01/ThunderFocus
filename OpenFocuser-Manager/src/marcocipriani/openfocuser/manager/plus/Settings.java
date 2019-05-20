package marcocipriani.openfocuser.manager.plus;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.*;

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
    public static final Gson serializer = new GsonBuilder()
            .setPrettyPrinting().serializeNulls().excludeFieldsWithoutExposeAnnotation().create();

    /**
     * Last used serial port.
     */
    @SerializedName("USB port")
    @Expose
    private String usbPort = "";
    /**
     * INDI server port.
     */
    @SerializedName("INDI server port")
    @Expose
    private int indiPort = 7625;
    /**
     * List of digital pins.
     */
    @SerializedName("Digital pins")
    @Expose
    private PinArray digitalPins = new PinArray();
    /**
     * List of PWM pins.
     */
    @SerializedName("PWM pins")
    @Expose
    private PinArray pwmPins = new PinArray();
    /**
     * The file where to store settings.
     */
    private File file;

    /**
     * Class constructor.
     */
    public Settings() {

    }

    /**
     * Class constructor.
     *
     * @param file a file where to save the settings.
     */
    public Settings(File file) {
        setFile(file);
    }

    /**
     * Loads the app's settings from the disk.
     *
     * @param file the folder where the config file is, or the file itself.
     * @return the loaded settings or a new {@link Settings} object in case of error.
     */
    public static Settings load(File file) {
        if (file.isDirectory()) {
            String path = file.getAbsolutePath();
            file = new File(path + (path.endsWith(File.separator) ? "" : File.separator) + "Settings.json");
        }
        StringBuilder json = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                json.append(line);
            }

        } catch (IOException e) {
            return new Settings(file);
        }
        Settings s;
        try {
            s = serializer.fromJson(json.toString(), Settings.class);

        } catch (Exception e) {
            return new Settings(file);
        }
        // Normalize invalid values
        for (ArduinoPin pin : s.digitalPins.toArray()) {
            pin.setValue(pin.getValueBoolean() ? 255 : 0);
        }
        for (ArduinoPin pin : s.pwmPins.toArray()) {
            pin.setValue(pin.getValuePwm());
        }
        if (s.indiPort <= 50) {
            s.indiPort = 7625;
        }
        // Save again
        s.setFile(file);
        s.save();
        return s;
    }

    /**
     * @return the serial port to use.
     */
    public String getUsbPort() {
        return usbPort;
    }

    /**
     * @param usbPort a new serial port.
     */
    public void setUsbPort(String usbPort) {
        this.usbPort = usbPort;
    }

    /**
     * @return the INDI port to use.
     */
    public int getIndiPort() {
        return indiPort;
    }

    /**
     * @param indiPort a new port for the INDI server.
     */
    public void setIndiPort(int indiPort) {
        this.indiPort = indiPort;
    }

    /**
     * @return the list of the current stored digital pins.
     */
    public PinArray getDigitalPins() {
        return digitalPins;
    }

    /**
     * @return the list of the current stored PWM pins.
     */
    public PinArray getPwmPins() {
        return pwmPins;
    }

    /**
     * @return the current output file.
     */
    public File getFile() {
        return file;
    }

    /**
     * @param file an output file for the settings.
     */
    public void setFile(File file) {
        if (file.isDirectory()) {
            String path = file.getAbsolutePath();
            this.file = new File(path + (path.endsWith(File.separator) ? "" : File.separator) + "Settings.json");

        } else {
            this.file = file;
        }
    }

    /**
     * Saves the app's settings.
     *
     * @see #setFile(File)
     * @see #getFile()
     */
    public void save() {
        if (file == null) {
            throw new IllegalStateException("Output file not set!");
        }
        if (file.isDirectory()) {
            String path = file.getAbsolutePath();
            file = new File(path + (path.endsWith(File.separator) ? "" : File.separator) + "Settings.json");
        }
        try (BufferedWriter out = new BufferedWriter(new FileWriter(file))) {
            out.write(serializer.toJson(this));

        } catch (IOException e) {
            throw new UncheckedIOException("Unable to write in the output file!", e);
        }
    }
}