package io.github.marcocipriani01.thunderfocus.config;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import io.github.marcocipriani01.thunderfocus.board.ArduinoPin;
import io.github.marcocipriani01.thunderfocus.board.Focuser;
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
    public int backlash = 0;
    @SerializedName("Speed")
    @Expose
    public int speed = 0;
    @SerializedName("Power save")
    @Expose
    public boolean powerSaver = false;
    @SerializedName("Reverse direction")
    @Expose
    public boolean reverseDir = false;
    @Expose
    @SerializedName("Automatic mode")
    private PowerBox.AutoModes autoMode = PowerBox.AutoModes.UNAVAILABLE;
    @Expose
    @SerializedName("Latitude")
    private double latitude = 0.0;
    @Expose
    @SerializedName("Longitude")
    private double longitude = 0.0;

    public ExportableSettings(Settings s, Board b) {
        if (!b.isConnected() || !b.isReady())
            throw new IllegalStateException("Board not connected!");
        this.theme = s.theme;
        this.showIpIndiDriver = s.showIpIndiDriver;
        this.indiServer = s.indiServer;
        this.ascomBridge = s.ascomBridge;
        this.indiServerPort = s.indiServerPort;
        this.ascomBridgePort = s.ascomBridgePort;
        this.serialPort = s.serialPort;
        this.autoConnect = s.autoConnect;
        Focuser focuser = b.focuser();
        if (focuser != null) {
            this.backlash = focuser.getBacklash();
            this.speed = focuser.getSpeed();
            this.powerSaver = focuser.isPowerSaverEnabled();
            this.reverseDir = focuser.isDirInverted();
            this.relativeStepSize = s.relativeStepSize;
            this.presets = s.presets;
            this.focuserMaxTravel = s.focuserMaxTravel;
            this.focuserTicksCount = s.focuserTicksCount;
            this.focuserTicksUnit = s.focuserTicksUnit;
        }
        PowerBox powerBox = b.powerBox();
        if (powerBox != null) {
            if (powerBox.supportsAutoModes()) {
                this.autoMode = powerBox.getAutoMode();
                if (powerBox.supportsTime()) {
                    this.latitude = powerBox.getLatitude();
                    this.longitude = powerBox.getLongitude();
                }
            }
            this.powerBoxPins = s.powerBoxPins;
        }
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

    public void applyTo(Settings s, Board b) throws IllegalArgumentException, SerialPortException, IOException {
        if (!b.isConnected() || !b.isReady())
            throw new IllegalStateException("Board not connected!");
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
        if (board.hasPowerBox()) {
            PowerBox powerBox = board.powerBox();
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