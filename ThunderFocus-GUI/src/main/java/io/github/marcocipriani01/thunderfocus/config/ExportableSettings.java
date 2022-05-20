package io.github.marcocipriani01.thunderfocus.config;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import io.github.marcocipriani01.thunderfocus.board.ArduinoPin;
import io.github.marcocipriani01.thunderfocus.board.PowerBox;
import io.github.marcocipriani01.thunderfocus.board.Board;
import jssc.SerialPortException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static io.github.marcocipriani01.thunderfocus.Main.board;

/**
 * @author marcocipriani01
 * @version 1.0
 */
public class ExportableSettings extends Settings {

    @SerializedName("Backlash")
    @Expose
    public int backlash;
    @SerializedName("Speed")
    @Expose
    public int speed;
    @SerializedName("Power save")
    @Expose
    public boolean powerSaver;
    @SerializedName("Reverse direction")
    @Expose
    public boolean reverseDir;
    @Expose
    @SerializedName("Automatic mode")
    private PowerBox.AutoModes autoMode;
    @Expose
    @SerializedName("Latitude")
    private double latitude;
    @Expose
    @SerializedName("Longitude")
    private double longitude;

    public ExportableSettings(Settings s, Board f) {
        if (!f.isConnected() || !f.isReady())
            throw new IllegalStateException("Focuser not connected!");
        this.backlash = f.getBacklash();
        this.speed = f.getSpeed();
        this.powerSaver = f.isPowerSaverOn();
        this.reverseDir = f.isReverseDir();
        if (f.isPowerBox()) {
            PowerBox powerBox = f.getPowerBox();
            if (powerBox.supportsAutoModes())
                this.autoMode = powerBox.getAutoMode();
            if (powerBox.supportsAmbient()) {
                this.latitude = powerBox.getLatitude();
                this.longitude = powerBox.getLongitude();
            }
        }
        this.relativeStepSize = s.relativeStepSize;
        this.presets = s.presets;
        this.theme = s.theme;
        this.showIpIndiDriver = s.showIpIndiDriver;
        this.indiServer = s.indiServer;
        this.ascomBridge = s.ascomBridge;
        this.indiServerPort = s.indiServerPort;
        this.ascomBridgePort = s.ascomBridgePort;
        this.focuserMaxTravel = s.focuserMaxTravel;
        this.focuserTicksCount = s.focuserTicksCount;
        this.focuserTicksUnit = s.focuserTicksUnit;
        this.serialPort = s.serialPort;
        this.autoConnect = s.autoConnect;
        this.powerBoxPins = s.powerBoxPins;
    }

    public static ExportableSettings load(Path path) throws IOException {
        ExportableSettings s;
        s = serializer.fromJson(new String(Files.readAllBytes(path)), ExportableSettings.class);
        normalize(s);
        if (s.speed > 100)
            s.speed = 100;
        else if (s.speed < 0)
            s.speed = 0;
        if (s.backlash < 0)
            s.backlash = 0;
        return s;
    }

    @Override
    public void save(Path path) throws IOException {
        super.save(path);
    }

    public void applyTo(Settings s, Board f) throws IllegalArgumentException, SerialPortException, IOException {
        if (!f.isConnected() || !f.isReady())
            throw new IllegalStateException("Focuser not connected!");
        s.relativeStepSize = this.relativeStepSize;
        s.presets = this.presets;
        s.theme = this.theme;
        s.showIpIndiDriver = this.showIpIndiDriver;
        s.indiServer = this.indiServer;
        s.ascomBridge = this.ascomBridge;
        s.indiServerPort = this.indiServerPort;
        s.ascomBridgePort = this.ascomBridgePort;
        s.setFokMaxTravel(this.focuserMaxTravel, null);
        s.focuserTicksCount = this.focuserTicksCount;
        s.focuserTicksUnit = this.focuserTicksUnit;
        s.autoConnect = this.autoConnect;
        s.powerBoxPins = this.powerBoxPins;
        s.save();
        board.run(Board.Commands.FOCUSER_SET_BACKLASH, null, this.backlash);
        board.run(Board.Commands.FOCUSER_SET_SPEED, null, this.speed);
        board.run(Board.Commands.FOCUSER_REVERSE_DIR, null, this.reverseDir ? 1 : 0);
        board.run(Board.Commands.FOCUSER_POWER_SAVER, null, this.powerSaver ? 1 : 0);
        if (board.isPowerBox()) {
            PowerBox powerBox = board.getPowerBox();
            for (ArduinoPin p : this.powerBoxPins) {
                if (powerBox.contains(p)) {
                    if (p.isOnWhenAppOpen()) {
                        board.run(Board.Commands.POWER_BOX_SET_PIN_AUTO, null, p.getNumber(), 0);
                        powerBox.setOnWhenAppOpen(p, true);
                        board.run(Board.Commands.POWER_BOX_SET, null, p.getNumber(), 255);
                    } else {
                        powerBox.setOnWhenAppOpen(p, false);
                    }
                }
            }
            if (powerBox.supportsAutoModes())
                board.run(Board.Commands.POWER_BOX_SET_AUTO_MODE, null, this.autoMode.ordinal());
            if (powerBox.supportsAmbient())
                board.run(Board.Commands.SET_TIME_LAT_LONG, null, 0,
                        (int) ((this.latitude) * 1000), (int) ((this.longitude) * 1000));
        }
    }
}