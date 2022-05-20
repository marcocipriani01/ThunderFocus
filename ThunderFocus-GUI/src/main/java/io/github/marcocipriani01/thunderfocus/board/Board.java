package io.github.marcocipriani01.thunderfocus.board;

import io.github.marcocipriani01.thunderfocus.Main;
import io.github.marcocipriani01.thunderfocus.io.SerialMessageListener;
import io.github.marcocipriani01.thunderfocus.io.SerialPortImpl;
import jssc.SerialPortException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static io.github.marcocipriani01.thunderfocus.Main.i18n;

public class Board implements SerialMessageListener {

    private final ArrayList<Listener> listeners = new ArrayList<>();
    private SerialPortImpl serialPort = new SerialPortImpl();
    private String version = "<?>";
    private PowerBox powerBox = null;
    private Focuser focuser = null;
    private FlatPanel flat = null;
    private volatile int timerCount = 0;
    private Listener exclusiveCaller = null;
    private volatile boolean ready = false;

    public Board() {
        serialPort.addListener(this);
    }

    public String getVersion() {
        return version;
    }

    public boolean isConnected() {
        return serialPort.isConnected();
    }

    public boolean isReady() {
        return ready;
    }

    public synchronized void connect(String port) throws SerialPortException {
        updConnSate(ConnectionState.TIMEOUT);
        serialPort.connect(port);
        timerCount = 1;
        new Timer("SendSettingsRequestTask #" + timerCount).schedule(new PingRetryTimer(), 500);
    }

    public synchronized void disconnect() {
        if (powerBox != null) {
            try {
                for (ArduinoPin pin : powerBox.asList()) {
                    if (pin.isOnWhenAppOpen()) {
                        pin.setValue(false);
                        Commands.POWER_BOX_SET.run(this, null, pin.getNumber(), pin.getValuePwm());
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        updConnSate(ConnectionState.TIMEOUT);
        updFocuserState(Focuser.FocuserState.NONE);
        ready = false;
        try {
            serialPort.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
            serialPort = new SerialPortImpl();
            serialPort.addListener(this);
        }
        updConnSate(ConnectionState.DISCONNECTED);
        focuser = null;
        powerBox = null;
        flat = null;
    }

    public void addListener(Listener l) {
        listeners.add(l);
    }

    public void removeListener(Listener l) {
        listeners.remove(l);
    }

    public void run(Commands cmd, Listener caller, int... params) throws IllegalArgumentException, SerialPortException, IOException {
        if (serialPort.isConnected() && ready && ((exclusiveCaller == null) || (caller == exclusiveCaller)))
            cmd.run(this, caller, params);
    }

    public PowerBox getPowerBox() {
        return powerBox;
    }

    public boolean isPowerBox() {
        return powerBox != null;
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
            switch (c) {
                case 'L' -> // Log
                        System.out.println("Message from focuser: \"" + param + "\"");

                case 'S' -> { // Pos
                    try {
                        int newCurrentPos = Integer.parseInt(param);
                        if (newCurrentPos != focuser.pos) {
                            focuser.pos = newCurrentPos;
                            notifyListeners(null, Parameters.CURRENT_POS);
                            notifyListeners(null, Parameters.CURRENT_POS_TICKS);
                        }
                    } catch (Exception e) {
                        System.err.println("Error in position data!");
                        e.printStackTrace();
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
                    try {
                        String[] split = param.split(",");
                        powerBox.temperature = Double.parseDouble(split[0]);
                        powerBox.humidity = Double.parseDouble(split[1]);
                        powerBox.dewPoint = Double.parseDouble(split[2]);
                        notifyListeners(null, Parameters.POWERBOX_AMBIENT_DATA);
                    } catch (Exception e) {
                        System.err.println("Error in ambient data!");
                        e.printStackTrace();
                    }
                }

                case 'Y' -> { // Pins
                    try {
                        String[] split = param.split(",");
                        powerBox.setAutoMode(Integer.parseInt(split[0]));
                        Pattern pattern = Pattern.compile("\\((.*?)\\)");
                        Matcher m = pattern.matcher(split[1]);
                        while (m.find()) {
                            String[] rcvPin = m.group(1).split("%");
                            int number = Integer.parseInt(rcvPin[0]), value = Integer.parseInt(rcvPin[1]);
                            this.powerBox.getPin(number).setValue(value);
                        }
                        notifyListeners(null, Parameters.POWERBOX_AUTO_MODE);
                        notifyListeners(null, Parameters.POWERBOX_PINS);
                    } catch (Exception e) {
                        System.err.println("Error in pins data!");
                        e.printStackTrace();
                    }
                }

                case 'T' -> { // Sun elevation
                    try {
                        powerBox.setSunElev(Double.parseDouble(param));
                        notifyListeners(null, Parameters.POWERBOX_SUN_ELEV);
                    } catch (Exception e) {
                        System.err.println("Error in Sun elevation data!");
                        e.printStackTrace();
                    }
                }

                default -> System.err.println("Unknown message received: \"" + msg + "\"");
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
                        case 'F' -> { // Focuser
                            focuser = new Focuser(
                                    Integer.parseInt(config[0]),
                                    Integer.parseInt(config[1]),
                                    Integer.parseInt(config[2]),
                                    Boolean.parseBoolean(config[3]),
                                    Boolean.parseBoolean(config[4]));
                        }

                        case 'D' -> { // Powerbox
                            boolean ambient = Boolean.parseBoolean(config[0]),
                                    rtc = Boolean.parseBoolean(config[1]);
                            String pins;
                            if (rtc) {
                                powerBox = new PowerBox(ambient,
                                        Integer.parseInt(config[2]), Integer.parseInt(config[3]),
                                        PowerBox.AutoModes.values()[Integer.parseInt(config[4])]);
                                pins = config[5];
                            } else {
                                powerBox = new PowerBox(ambient, PowerBox.AutoModes.values()[Integer.parseInt(config[2])]);
                                pins = config[3];
                            }
                            Pattern pattern = Pattern.compile("\\((.*?)\\)");
                            Matcher m = pattern.matcher(pins);
                            while (m.find()) {
                                String[] rcvPin = m.group(1).split("%");
                                int number = Integer.parseInt(rcvPin[0]), value = Integer.parseInt(rcvPin[1]);
                                ArduinoPin stored = PowerBox.getPin(Main.settings.powerBoxPins, number);
                                if (stored == null) {
                                    this.powerBox.add(new ArduinoPin(number, i18n("output.pin.default.name") + " " + number,
                                            value, rcvPin[2].equals("1"), rcvPin[3].equals("1"), false));
                                } else if (stored.isOnWhenAppOpen()) {
                                    serialPort.println("$X" + number + "%255%");
                                    this.powerBox.add(new ArduinoPin(number, stored.getName(),
                                            255, rcvPin[2].equals("1"), rcvPin[3].equals("1"), true));
                                } else {
                                    this.powerBox.add(new ArduinoPin(number, stored.getName(),
                                            value, rcvPin[2].equals("1"), rcvPin[3].equals("1"), false));
                                }
                            }
                        }
                    }
                }


                String[] l = param.split(",");
                version = l[0].charAt(0) + "." + l[0].charAt(1);
                currentPos = Integer.parseInt(l[1]);
                speed = Integer.parseInt(l[2]);
                powerSaver = l[3].equals("1");
                backlash = Integer.parseInt(l[4]);
                reverseDir = l[5].equals("1");
                if (l[6].equals("1")) {
                    powerBox = new PowerBox();
                    powerBox.setAutoMode(Integer.parseInt(l[7]));

                    powerBox.setSupportsAmbient(l[9].equals("1"));
                    if (l[10].equals("1")) {
                        powerBox.setSupportsTime(true);
                        powerBox.setLatitude(Double.parseDouble(l[11]));
                        powerBox.setLongitude(Double.parseDouble(l[12]));
                        Commands.SET_TIME_LAT_LONG.run(this, null, (int) (System.currentTimeMillis() / 1000L), 0, 0);
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
        POWERBOX_WORLD_COORD,
        POWERBOX_SUN_ELEV
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
        SET_TIME_LAT_LONG('T', 3, (b, params) -> true, (b, caller, params) -> {
            if ((params[1] != 0) && (params[2] != 0)) {
                b.powerBox.setLatitude(((double) params[1]) / 1000.0);
                b.powerBox.setLongitude(((double) params[2]) / 1000.0);
                b.notifyListeners(caller, Parameters.POWERBOX_WORLD_COORD);
            }
        }),
        FOCUSER_REL_MOVE('R', 1, null),
        FOCUSER_ABS_MOVE('A', 1,
                (b, params) -> (params[0] >= 0) && (params[0] <= Main.settings.getFocuserMaxTravel()),
                (b, caller, params) -> {
                    b.focuser.targetPos = params[0];
                    b.notifyListeners(caller, Parameters.REQUESTED_POS);
                }),
        FOCUSER_STOP('S'),
        FOCUSER_SET_POS('P', 1, (b, params) -> (params[0] >= 0) && (params[0] <= Main.settings.getFocuserMaxTravel())),
        FOCUSER_SET_ZERO('W'),
        FOCUSER_SET_SPEED('V', 1, (b, params) -> (params[0] >= 0) && (params[0] <= 100),
                (b, caller, params) -> {
                    b.focuser.speed = params[0];
                    b.notifyListeners(caller, Parameters.SPEED);
                }),
        FOCUSER_SET_BACKLASH('B', 1, (b, params) -> (params[0] >= 0),
                (b, caller, params) -> {
                    b.focuser.backlash = params[0];
                    b.notifyListeners(caller, Parameters.BACKLASH);
                }),
        FOCUSER_REVERSE_DIR('D', 1, (b, params) -> (params[0] == 0) || (params[0] == 1),
                (b, caller, params) -> {
                    b.focuser.invertDir = (params[0] == 1);
                    b.notifyListeners(caller, Parameters.REVERSE_DIR);
                }),
        FOCUSER_POWER_SAVER('H', 1, (b, params) -> (params[0] == 0) || (params[0] == 1),
                (b, caller, params) -> {
                    b.focuser.powerSaver = (params[0] == 1);
                    b.notifyListeners(caller, Parameters.ENABLE_POWER_SAVE);
                }),
        POWER_BOX_SET('X', 2, (b, params) -> b.powerBox.contains(params[0]) && (params[1] >= 0) && (params[1] <= 255),
                (b, caller, params) -> {
                    b.powerBox.getPin(params[0]).setValue(params[1]);
                    b.notifyListeners(caller, Parameters.POWERBOX_PINS);
                }),
        POWER_BOX_SET_AUTO_MODE('K', 1, (b, params) -> b.powerBox.supportedAutoModes().contains(PowerBox.AutoModes.values()[params[0]]),
                (b, caller, params) -> {
                    b.powerBox.setAutoMode(params[0]);
                    b.notifyListeners(caller, Parameters.POWERBOX_AUTO_MODE);
                }),
        POWER_BOX_SET_PIN_AUTO('Y', 2, (b, params) -> b.powerBox.contains(params[0]) && ((params[1] == 0) || (params[1] == 1)),
                (b, caller, params) -> {
                    b.powerBox.getPin(params[0]).setAutoModeEn(params[1] == 1);
                    b.notifyListeners(caller, Parameters.POWERBOX_PINS);
                });

        public final char id;
        public final int paramsCount;
        private ParamValidator validator = null;

        private WhenDone whenDone = null;

        Commands(char id) {
            this.id = id;
            this.paramsCount = 0;
        }

        @SuppressWarnings("SameParameterValue")
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
            StringBuilder cmd = new StringBuilder("$").append(id);
            if (params.length != paramsCount)
                throw new IllegalArgumentException("Missing/too much parameters.");
            if (validator != null && !validator.validate(b, params))
                throw new IllegalArgumentException("Invalid parameters.");
            for (int p : params) {
                cmd.append(p).append("%");
            }
            b.serialPort.println(cmd.toString());
            if (whenDone != null) {
                whenDone.action(b, caller, params);
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
            if (!ready && serialPort.isConnected()) {
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