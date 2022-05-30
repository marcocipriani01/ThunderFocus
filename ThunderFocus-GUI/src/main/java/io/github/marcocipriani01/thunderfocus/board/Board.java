package io.github.marcocipriani01.thunderfocus.board;

import io.github.marcocipriani01.thunderfocus.Main;
import io.github.marcocipriani01.thunderfocus.serial.SerialMessageListener;
import io.github.marcocipriani01.thunderfocus.serial.SerialPortImpl;
import jssc.SerialPortException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static io.github.marcocipriani01.thunderfocus.Main.board;
import static io.github.marcocipriani01.thunderfocus.Main.i18n;

public class Board implements SerialMessageListener {

    private final ArrayList<Listener> listeners = new ArrayList<>();
    private final SerialPortImpl serialPort = new SerialPortImpl();
    private volatile String version = null;
    private volatile PowerBox powerBox = null;
    private volatile Focuser focuser = null;
    private volatile FlatPanel flat = null;
    private volatile int timerCount = 0;
    private volatile Listener exclusiveCaller = null;
    private volatile boolean ready = false;

    public Board() {
        serialPort.addListener(this);
    }

    public String getBoardVersion() {
        if (!serialPort.isConnected())
            throw new IllegalStateException("Not connected");
        return version;
    }

    public boolean isConnected() {
        return serialPort.isConnected();
    }

    public boolean isReady() {
        return serialPort.isConnected() && ready;
    }

    public synchronized void connect(String port) throws SerialPortException {
        if (isConnected())
            throw new IllegalStateException("Already connected");
        updConnSate(ConnectionState.TIMEOUT);
        serialPort.connect(port);
        timerCount = 1;
        new Timer("SendSettingsRequestTask #" + timerCount).schedule(new PingRetryTimer(), 500);
    }

    public synchronized void disconnect() {
        disconnect(true);
    }

    public synchronized void disconnect(boolean applyPins) {
        if ((powerBox != null) && applyPins) {
            try {
                for (ArduinoPin pin : powerBox.asList()) {
                    if (pin.isOnWhenAppOpen()) {
                        pin.setValue(false);
                        Commands.POWER_BOX_SET_PIN.run(this, null, false, pin.getNumber(), pin.getValuePWM());
                    }
                }
            } catch (Exception ignored) {
            }
        }
        updConnSate(ConnectionState.TIMEOUT);
        if (focuser != null)
            updFocuserState(Focuser.FocuserState.NONE);
        ready = false;
        try {
            serialPort.disconnect();
        } catch (Exception ignored) {
        }
        updConnSate(ConnectionState.DISCONNECTED);
        focuser = null;
        powerBox = null;
        flat = null;
        version = null;
    }

    public Focuser focuser() {
        return focuser;
    }

    public FlatPanel flat() {
        return flat;
    }

    public void addListener(Listener l) {
        listeners.add(l);
    }

    public void removeListener(Listener l) {
        listeners.remove(l);
    }

    public void run(Commands cmd, Listener caller, int... params) throws IllegalArgumentException, SerialPortException, IOException {
        if (isReady() && ((exclusiveCaller == null) || (caller == exclusiveCaller)))
            cmd.run(this, caller, params);
    }

    public PowerBox powerBox() {
        return powerBox;
    }

    public boolean hasPowerBox() {
        return powerBox != null;
    }

    public boolean hasFocuser() {
        return focuser != null;
    }

    public boolean hasFlatPanel() {
        return flat != null;
    }

    public void setExclusiveMode(Listener exclusiveCaller) {
        this.exclusiveCaller = exclusiveCaller;
    }

    @Override
    public final synchronized void onSerialMessage(String msg) {
        if (msg.length() == 0)
            return;
        char c = msg.charAt(0);
        String param = msg.substring(1);
        if (ready) {
            try {
                switch (c) {
                    case '>' -> // Log
                            System.out.println("Message from focuser: \"" + param + "\"");

                    case 'S' -> { // Pos
                        int newCurrentPos = Integer.parseInt(param);
                        if (newCurrentPos != focuser.pos) {
                            focuser.pos = newCurrentPos;
                            notifyListeners(null, Parameters.CURRENT_POS);
                            notifyListeners(null, Parameters.CURRENT_POS_TICKS);
                        }
                    }

                    case 'M' -> // Moving
                            updFocuserState(Focuser.FocuserState.MOVING);

                    case 'H' -> // Hold
                            updFocuserState(Focuser.FocuserState.HOLD_MOTOR);

                    case 'A' -> { // Arrived
                        for (Listener l : listeners) {
                            l.onReachedPos();
                        }
                    }

                    case 'P' -> // Power save
                            updFocuserState(Focuser.FocuserState.POWER_SAVE);

                    case 'J' -> { // Ambient
                        String[] split = param.split(",");
                        powerBox.temperature = Double.parseDouble(split[0]);
                        powerBox.humidity = Double.parseDouble(split[1]);
                        powerBox.dewPoint = Double.parseDouble(split[2]);
                        notifyListeners(null, Parameters.POWERBOX_AMBIENT_DATA);
                    }

                    case 'Y' -> { // Pins
                        String[] split = param.split(",");
                        powerBox.autoMode = PowerBox.AutoModes.values()[Integer.parseInt(split[0])];
                        Pattern pattern = Pattern.compile("\\((.*?)\\)");
                        Matcher m = pattern.matcher(split[1]);
                        while (m.find()) {
                            String[] rcvPin = m.group(1).split("%");
                            int number = Integer.parseInt(rcvPin[0]), value = Integer.parseInt(rcvPin[1]);
                            this.powerBox.get(number).setValue(value);
                        }
                        notifyListeners(null, Parameters.POWERBOX_AUTO_MODE);
                        notifyListeners(null, Parameters.POWERBOX_PINS);
                    }

                    case 'T' -> { // Sun elevation
                        powerBox.sunElev = Double.parseDouble(param);
                        notifyListeners(null, Parameters.POWERBOX_SUN_ELEV);
                    }

                    case 'W' -> // Time sync
                            Commands.SET_TIME_LAT_LONG.run(this, null, (int) (System.currentTimeMillis() / 1000L), 0, 0);

                    case 'E' -> { // Flat panel
                        flat.coverStatus = FlatPanel.CoverStatus.values()[Integer.parseInt(param)];
                        notifyListeners(null, Parameters.FLAT_COVER_STATUS);
                    }

                    default -> System.err.println("Unknown message received: \"" + msg + "\"");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (c == 'C') {
            try {
                System.out.println("Focuser settings: " + param);
                String[] devs = param.split(";");
                version = devs[0];
                for (int i = 1; i < devs.length; i++) {
                    String dev = devs[i];
                    char type = dev.charAt(0);
                    String[] config = dev.substring(dev.indexOf("[") + 1, dev.indexOf("]")).split(",");
                    switch (type) {
                        case 'F' -> // Focuser
                                focuser = new Focuser(
                                        Integer.parseInt(config[0]),
                                        Integer.parseInt(config[1]),
                                        Integer.parseInt(config[2]),
                                        config[3].equals("1"),
                                        config[4].equals("1"));

                        case 'D' -> { // Powerbox
                            boolean ambient = config[0].equals("1"),
                                    rtc = config[1].equals("1");
                            String pins;
                            if (rtc) {
                                powerBox = new PowerBox(ambient,
                                        Double.parseDouble(config[2]), Double.parseDouble(config[3]),
                                        PowerBox.AutoModes.values()[Integer.parseInt(config[4])]);
                                pins = config[5];
                                Commands.SET_TIME_LAT_LONG.run(this, null, (int) (System.currentTimeMillis() / 1000L), 0, 0);
                            } else {
                                powerBox = new PowerBox(ambient, PowerBox.AutoModes.values()[Integer.parseInt(config[2])]);
                                pins = config[3];
                            }
                            Pattern pattern = Pattern.compile("\\((.*?)\\)");
                            Matcher m = pattern.matcher(pins);
                            while (m.find()) {
                                String[] rcvPin = m.group(1).split("%");
                                int number = Integer.parseInt(rcvPin[0]),
                                        value = Integer.parseInt(rcvPin[1]);
                                boolean isPwm = rcvPin[2].equals("1");
                                ArduinoPin stored = PowerBox.getPinFrom(Main.settings.powerBoxPins, number);
                                if (stored == null) {
                                    if (isPwm)
                                        this.powerBox.add(new ArduinoPin(number, i18n("output.pin.default.name") + " " + number,
                                                value, rcvPin[3].equals("1"), rcvPin[4].equals("1"), false));
                                    else
                                        this.powerBox.add(new ArduinoPin(number, i18n("output.pin.default.name") + " " + number,
                                                value, rcvPin[3].equals("1"), false));
                                } else if (stored.isOnWhenAppOpen()) {
                                    if (isPwm)
                                        this.powerBox.add(new ArduinoPin(number, stored.getName(),
                                                255, rcvPin[3].equals("1"), rcvPin[4].equals("1"), true));
                                    else
                                        this.powerBox.add(new ArduinoPin(number, stored.getName(),
                                                255, rcvPin[3].equals("1"), true));
                                    Commands.POWER_BOX_SET_PIN.run(this, null, number, 255);
                                } else {
                                    if (isPwm)
                                        this.powerBox.add(new ArduinoPin(number, stored.getName(),
                                                value, rcvPin[3].equals("1"), rcvPin[4].equals("1"), false));
                                    else
                                        this.powerBox.add(new ArduinoPin(number, stored.getName(),
                                                value, rcvPin[3].equals("1"), false));
                                }
                            }
                        }

                        case 'P' -> { // Flat panel
                            boolean lightStatus = config[0].equals("1");
                            int brightness = Integer.parseInt(config[1]);
                            if (config[2].equals("1")) {
                                this.flat = new FlatPanel(lightStatus, brightness,
                                        Integer.parseInt(config[3]), Integer.parseInt(config[4]), Integer.parseInt(config[5]),
                                        FlatPanel.CoverStatus.values()[Integer.parseInt(config[6])]);
                            } else {
                                this.flat = new FlatPanel(lightStatus, brightness);
                            }
                        }
                    }
                }
                ready = true;
                updConnSate(ConnectionState.CONNECTED_READY);
            } catch (Exception e) {
                nOnCriticalError(e);
                disconnect();
            }
        }
    }

    @Override
    public final void onSerialError(Exception e) {
        updConnSate(ConnectionState.ERROR);
        e.printStackTrace();
    }

    private void notifyListeners(Listener notMe, Parameters p) {
        for (Listener l : listeners) {
            if (l != notMe) l.updateParam(p);
        }
    }

    private void updFocuserState(Focuser.FocuserState focuserState) {
        this.focuser.state = focuserState;
        for (Listener l : listeners) {
            l.updateFocuserState(focuserState);
        }
    }

    private void updConnSate(ConnectionState connectionState) {
        for (Listener l : listeners) {
            l.updateConnectionState(connectionState);
        }
    }

    private void nOnCriticalError(Exception e) {
        e.printStackTrace();
        for (Listener l : listeners) {
            l.onCriticalError(e);
        }
    }

    public enum Parameters {
        REQUESTED_POS,
        CURRENT_POS,
        CURRENT_POS_TICKS,
        SPEED,
        BACKLASH,
        REVERSE_DIR,
        ENABLE_POWER_SAVE,
        POWERBOX_PINS,
        POWERBOX_AUTO_MODE,
        POWERBOX_AMBIENT_DATA,
        POWERBOX_SUN_ELEV,
        FLAT_COVER_STATUS,
        FLAT_LIGHT_STATUS,
        FLAT_BRIGHTNESS
    }

    public enum ConnectionState {
        DISCONNECTED(i18n("disconnected")),
        CONNECTED_READY(i18n("connected")),
        TIMEOUT(i18n("timeout")),
        ERROR(i18n("error.lowercase"));

        private final String label;

        ConnectionState(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }
    }

    public enum Commands {
        PRINT_CONFIG('C'),
        SET_TIME_LAT_LONG('T', 3, (b, params) -> (b.hasPowerBox() && b.powerBox.supportsTime()), (b, caller, params) -> {
            if ((params[1] != 0) && (params[2] != 0)) {
                b.powerBox.latitude = ((double) params[1]) / 1000.0;
                b.powerBox.longitude = ((double) params[2]) / 1000.0;
            }
        }),
        FOCUSER_REL_MOVE('R', 1, (b, params) -> (b.hasFocuser() &&
                ((b.focuser.pos + params[0]) >= 0) && ((b.focuser.pos + params[0]) <= Main.settings.getFocuserMaxTravel()))),
        FOCUSER_ABS_MOVE('A', 1,
                (b, params) -> (board.hasFocuser() && (params[0] >= 0) && (params[0] <= Main.settings.getFocuserMaxTravel())),
                (b, caller, params) -> {
                    b.focuser.targetPos = params[0];
                    b.notifyListeners(caller, Parameters.REQUESTED_POS);
                }),
        FOCUSER_STOP('S', 0, (b, params) -> b.hasFocuser()),
        FOCUSER_SET_POS('P', 1, (b, params) -> (b.hasFocuser() && (params[0] >= 0) && (params[0] <= Main.settings.getFocuserMaxTravel()))),
        FOCUSER_SET_ZERO('W', 0, (b, params) -> b.hasFocuser()),
        FOCUSER_SET_SPEED('V', 1, (b, params) -> (b.hasFocuser() && (params[0] >= 0) && (params[0] <= 100)),
                (b, caller, params) -> {
                    b.focuser.speed = params[0];
                    b.notifyListeners(caller, Parameters.SPEED);
                }),
        FOCUSER_SET_BACKLASH('B', 1, (b, params) -> (b.hasFocuser() && (params[0] >= 0)),
                (b, caller, params) -> {
                    b.focuser.backlash = params[0];
                    b.notifyListeners(caller, Parameters.BACKLASH);
                }),
        FOCUSER_REVERSE_DIR('D', 1, (b, params) -> (b.hasFocuser() && ((params[0] == 0) || (params[0] == 1))),
                (b, caller, params) -> {
                    b.focuser.invertDir = (params[0] == 1);
                    b.notifyListeners(caller, Parameters.REVERSE_DIR);
                }),
        FOCUSER_POWER_SAVER('H', 1, (b, params) -> (b.hasFocuser() && ((params[0] == 0) || (params[0] == 1))),
                (b, caller, params) -> {
                    b.focuser.powerSaver = (params[0] == 1);
                    b.notifyListeners(caller, Parameters.ENABLE_POWER_SAVE);
                }),
        POWER_BOX_SET_PIN('X', 2, (b, params) -> {
            if (b.powerBox == null) return false;
            ArduinoPin pin = b.powerBox.get(params[0]);
            return ((pin != null) && (params[1] >= 0) && (params[1] <= 255) && (!pin.isAutoModeEn()));
        }, (b, caller, params) -> {
            b.powerBox.get(params[0]).setValue(params[1]);
            b.notifyListeners(caller, Parameters.POWERBOX_PINS);
        }),
        POWER_BOX_SET_AUTO_MODE('K', 1, (b, params) -> (b.hasPowerBox() && b.powerBox.supportedAutoModes().contains(PowerBox.AutoModes.values()[params[0]])),
                (b, caller, params) -> {
                    b.powerBox.autoMode = PowerBox.AutoModes.values()[params[0]];
                    b.notifyListeners(caller, Parameters.POWERBOX_AUTO_MODE);
                }),
        POWER_BOX_EN_PIN_PWM('J', 2, (b, params) -> (b.hasPowerBox() && b.powerBox.contains(params[0]) && ((params[1] == 0) || (params[1] == 1))),
                (b, caller, params) -> {
                    b.powerBox.get(params[0]).setPWMEnabled(params[1] == 1);
                    b.notifyListeners(caller, Parameters.POWERBOX_PINS);
                }),
        POWER_BOX_SET_PIN_AUTO('Y', 2, (b, params) -> (b.hasPowerBox() && b.powerBox.contains(params[0]) && ((params[1] == 0) || (params[1] == 1))),
                (b, caller, params) -> {
                    b.powerBox.get(params[0]).setAutoModeEn(params[1] == 1);
                    b.notifyListeners(caller, Parameters.POWERBOX_PINS);
                }),
        FLAT_SET_BRIGHTNESS('Z', 1, (b, params) -> (b.hasFlatPanel() && (params[0] >= 0) && (params[0] <= 255)),
                (b, caller, params) -> {
                    b.flat.brightness = params[0];
                    b.notifyListeners(caller, Parameters.FLAT_BRIGHTNESS);
                }),
        FLAT_SET_LIGHT('L', 1, (b, params) -> (b.hasFlatPanel() && ((params[0] == 0) || (params[0] == 1))),
                (b, caller, params) -> {
                    b.flat.lightStatus = (params[0] == 1);
                    b.notifyListeners(caller, Parameters.FLAT_LIGHT_STATUS);
                }),
        FLAT_SET_COVER('Q', 1, (b, params) -> (b.hasFlatPanel() && b.flat.hasServo() && ((params[0] == 0) || (params[0] == 1))), null),
        FLAT_SET_CONFIG('F', 3, (b, params) -> (b.hasFlatPanel() && b.flat.hasServo() &&
                (params[0] >= 170) && (params[0] <= 290) && (params[1] >= -15) && (params[1] <= 15) && (params[2] >= 0) && (params[2] <= 10)), null),
        FLAT_HALT('M', 0, (b, params) -> (b.hasFlatPanel() && b.flat.hasServo()), null);

        public final char id;
        public final int paramsCount;
        private ParamValidator validator = null;

        private WhenDone whenDone = null;

        Commands(char id) {
            this.id = id;
            this.paramsCount = 0;
        }

        Commands(char id, int paramsCount, ParamValidator validator) {
            this.id = id;
            this.validator = validator;
            this.paramsCount = paramsCount;
        }

        Commands(char id, int paramsCount, ParamValidator validator, WhenDone whenDone) {
            this.id = id;
            this.validator = validator;
            this.paramsCount = paramsCount;
            this.whenDone = whenDone;
        }

        private void run(Board b, Listener caller, int... params)
                throws IllegalArgumentException, SerialPortException, IOException {
            run(b, caller, true, params);
        }

        private void run(Board b, Listener caller, boolean applyPinsOnDisconnect, int... params)
                throws IllegalArgumentException, SerialPortException, IOException {
            StringBuilder cmd = new StringBuilder("$").append(id);
            if (params.length != paramsCount)
                throw new IllegalArgumentException("Missing/too much parameters.");
            if ((validator != null) && !validator.validate(b, params))
                throw new IllegalArgumentException("Invalid parameters.");
            for (int p : params) {
                cmd.append(p).append("%");
            }
            try {
                b.serialPort.println(cmd.toString());
                if (whenDone != null) whenDone.action(b, caller, params);
            } catch (SerialPortException | IOException e) {
                b.disconnect(applyPinsOnDisconnect);
                throw e;
            }
        }

        private interface ParamValidator {
            @SuppressWarnings("BooleanMethodIsAlwaysInverted")
            boolean validate(Board b, int[] params);
        }

        private interface WhenDone {
            void action(Board b, Listener caller, int[] params);
        }
    }

    public interface Listener {
        default void updateConnectionState(ConnectionState connectionState) {
        }

        default void updateFocuserState(Focuser.FocuserState focuserState) {
        }

        default void onReachedPos() {
        }

        default void updateParam(Parameters p) {
        }

        default void onCriticalError(Exception e) {
        }
    }

    private class PingRetryTimer extends TimerTask {
        @Override
        public synchronized void run() {
            if (isConnected() && (!ready)) {
                try {
                    System.err.println("Sending focuser settings request");
                    Commands.PRINT_CONFIG.run(Board.this, null);
                    timerCount++;
                    if (timerCount < 5) {
                        new Timer("SendSettingsRequestTask #" + timerCount).schedule(new PingRetryTimer(), 500);
                    } else {
                        disconnect();
                        nOnCriticalError(new IllegalStateException("Connection timeout, disconnecting."));
                    }
                } catch (IllegalArgumentException ignored) {
                } catch (IOException | SerialPortException e) {
                    disconnect();
                    nOnCriticalError(e);
                }
            }
        }
    }
}