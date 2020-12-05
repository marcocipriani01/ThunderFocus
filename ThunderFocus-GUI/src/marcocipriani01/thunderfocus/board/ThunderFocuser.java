package marcocipriani01.thunderfocus.board;

import marcocipriani01.thunderfocus.Main;
import marcocipriani01.thunderfocus.io.ConnectionException;
import marcocipriani01.thunderfocus.io.SerialMessageListener;
import marcocipriani01.thunderfocus.io.SerialPortImpl;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ThunderFocuser implements SerialMessageListener {

    private SerialPortImpl serialPort = new SerialPortImpl();
    private final ArrayList<Listener> listeners = new ArrayList<>();
    private String version = "?";
    private PowerBox powerBox = null;
    private int timerCount = 0;
    private Listener exclusiveCaller = null;
    private ConnState connState = ConnState.DISCONNECTED;
    private FocuserState focuserState = FocuserState.NONE;
    private boolean ready = false;
    private int requestedPos = 0;
    private int requestedRelPos = 10;
    private int currentPos = 0;
    private int currentPosTicks = 0;
    private int speed = 80;
    private int backlash = 0;
    private boolean reverseDir = false;
    private boolean powerSaver = false;

    public ThunderFocuser() {
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

    public void connect(String port) throws ConnectionException {
        updConnSate(ConnState.TIMEOUT);
        serialPort.connect(port);
        timerCount = 1;
        new Timer("SendSettingsRequestTask #" + timerCount).schedule(new SendSettingsRequestTask(), 500);
    }

    public void disconnect() {
        updConnSate(ConnState.TIMEOUT);
        updFocuserState(FocuserState.NONE);
        ready = false;
        try {
            serialPort.disconnect();
        } catch (ConnectionException e) {
            e.printStackTrace();
            serialPort = new SerialPortImpl();
        }
        updConnSate(ConnState.DISCONNECTED);
        if (powerBox != null) {
            powerBox.clear();
        }
        powerBox = null;
    }

    public void addListener(Listener l) {
        listeners.add(l);
    }

    public void removeListener(Listener l) {
        listeners.remove(l);
    }

    public ConnState getConnState() {
        return connState;
    }

    public FocuserState getFocuserState() {
        return focuserState;
    }

    public void run(Commands cmd, Listener caller, int... params) throws ConnectionException, InvalidParamException {
        if (serialPort.isConnected() && ready && ((exclusiveCaller == null) || (caller == exclusiveCaller))) {
            cmd.run(this, caller, params);
        }
    }

    public PowerBox getPowerBox() {
        return powerBox;
    }

    public boolean isPowerBox() {
        return powerBox != null;
    }

    public int getRequestedPos() {
        return requestedPos;
    }

    public int getRequestedRelPos() {
        return requestedRelPos;
    }

    public void setExclusiveMode(Listener exclusiveCaller) {
        this.exclusiveCaller = exclusiveCaller;
    }

    public void clearRequestedPositions() {
        requestedPos = currentPos;
        requestedRelPos = 0;
    }

    public int getCurrentPos() {
        return currentPos;
    }

    public int getCurrentPosTicks() {
        return currentPosTicks;
    }

    public int getSpeed() {
        return speed;
    }

    public int getBacklash() {
        return backlash;
    }

    public boolean isReverseDir() {
        return reverseDir;
    }

    public boolean isPowerSaver() {
        return powerSaver;
    }

    public int ticksToSteps(int ticks) {
        return (int) ((((double) ticks) / ((double) Main.settings.getFokTicksCount())) * ((double) Main.settings.getFokMaxTravel()));
    }

    public int stepsToTicks(int steps) {
        return (int) ((((double) steps) / ((double) Main.settings.getFokMaxTravel())) * ((double) Main.settings.getFokTicksCount()));
    }

    @Override
    public void onPortMessage(String msg) {
        char c;
        String param;
        try {
            c = msg.charAt(0);
            param = msg.substring(1);
        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
            return;
        }
        if (ready) {
            switch (c) {
                case 'L' -> // Log
                        System.out.println("Message from focuser: \"" + param + "\"");

                case 'S' -> { // Pos
                    try {
                        int newCurrentPos = Integer.parseInt(param);
                        if (newCurrentPos != currentPos) {
                            currentPos = newCurrentPos;
                            currentPosTicks = stepsToTicks(currentPos);
                            notifyListeners(null, Parameters.CURRENT_POS);
                            notifyListeners(null, Parameters.CURRENT_POS_TICKS);
                        }
                    } catch (Exception e) {
                        System.err.println("Error in position data!");
                        e.printStackTrace();
                    }
                }

                case 'M' -> // Moving
                        updFocuserState(FocuserState.MOVING);

                case 'H' -> // Hold
                        updFocuserState(FocuserState.HOLD_MOTOR);

                case 'A' -> // Arrived
                        nOnReachedPos();

                case 'P' -> // Power save
                        updFocuserState(FocuserState.POWER_SAVE);

                case 'J' -> { // Ambient
                    try {
                        String[] split = param.split(",");
                        powerBox.setTemperature(Double.parseDouble(split[0]));
                        powerBox.setHumidity(Double.parseDouble(split[1]));
                        powerBox.setDewPoint(Double.parseDouble(split[2]));
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
        } else {
            if (c == 'C') {
                try {
                    System.out.println("Focuser settings: " + param);
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
                        Pattern pattern = Pattern.compile("\\((.*?)\\)");
                        Matcher m = pattern.matcher(l[8]);
                        PowerBox digitalPins = Main.settings.getPowerBox();
                        while (m.find()) {
                            String[] rcvPin = m.group(1).split("%");
                            int number = Integer.parseInt(rcvPin[0]), value = Integer.parseInt(rcvPin[1]);
                            ArduinoPin stored = digitalPins.getPin(number);
                            this.powerBox.add(new ArduinoPin(number,
                                    stored == null ? ("Pin " + number) : stored.getName(),
                                    value, rcvPin[2].equals("1"), rcvPin[3].equals("1")));
                        }
                        powerBox.setSupportsAmbient(l[9].equals("1"));
                        if (l[10].equals("1")) {
                            powerBox.setSupportsTime(true);
                            powerBox.setLatitude(Double.parseDouble(l[11]));
                            powerBox.setLongitude(Double.parseDouble(l[12]));
                            Commands.SET_TIME_LAT_LONG.run(this, null, (int) (System.currentTimeMillis() / 1000L), 0, 0);
                        }
                    }
                    ready = true;
                    updConnSate(ConnState.CONNECTED_READY);

                } catch (Exception e) {
                    nOnCriticalError(new ConnectionException("The focuser responded with an illegal configuration.", ConnectionException.Type.PROTOCOL));
                    disconnect();
                }
            }
        }
    }

    @Override
    public void onPortError(Exception e) {
        updConnSate(ConnState.ERROR);
        e.printStackTrace();
    }

    private void nOnReachedPos() {
        for (Listener l : listeners) {
            l.onReachedPos();
        }
    }

    private void notifyListeners(Listener notMe, Parameters p) {
        for (Listener l : listeners) {
            if (l != notMe) l.updateParam(p);
        }
    }

    private void updFocuserState(FocuserState focuserState) {
        this.focuserState = focuserState;
        for (Listener l : listeners) {
            l.updateFocuserState(focuserState);
        }
    }

    private void updConnSate(ConnState connState) {
        this.connState = connState;
        for (Listener l : listeners) {
            l.updateConnSate(connState);
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
        REQUESTED_REL_POS,
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

    public enum ConnState {
        DISCONNECTED("disconnesso"),
        CONNECTED_READY("connesso"),
        TIMEOUT("timeout"),
        ERROR("errore");

        private final String label;

        ConnState(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }
    }

    public enum FocuserState {
        MOVING("movimento"),
        HOLD_MOTOR("fermo"),
        POWER_SAVE("risparmio energetico"),
        NONE("-");

        private final String label;

        FocuserState(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }
    }

    public enum Commands {
        PRINT_CONFIG('C'),
        SET_TIME_LAT_LONG('T', 3, (f, params) -> true, (f, caller, params) -> {
            if ((params[1] != 0) && (params[2] != 0)) {
                f.powerBox.setLatitude(((double) params[1]) / 1000.0);
                f.powerBox.setLongitude(((double) params[2]) / 1000.0);
                f.notifyListeners(caller, Parameters.POWERBOX_WORLD_COORD);
            }
        }),
        FOK1_REL_MOVE('R', 1, null, (f, caller, params) -> {
            f.requestedRelPos = params[0];
            f.notifyListeners(caller, Parameters.REQUESTED_REL_POS);
        }),
        FOK1_ABS_MOVE('A', 1,
                (f, params) -> (params[0] >= 0) && (params[0] <= Main.settings.getFokMaxTravel()),
                (f, caller, params) -> {
                    f.requestedPos = params[0];
                    f.notifyListeners(caller, Parameters.REQUESTED_POS);
                }),
        FOK1_STOP('S'),
        FOK1_SET_POS('Z', 1, (f, params) -> (params[0] >= 0) && (params[0] <= Main.settings.getFokMaxTravel())),
        FOK1_SET_ZERO('W'),
        FOK1_SET_SPEED('V', 1, (f, params) -> (params[0] >= 0) && (params[0] <= 100),
                (f, caller, params) -> {
                    f.speed = params[0];
                    f.notifyListeners(caller, Parameters.SPEED);
                }),
        FOK1_SET_BACKLASH('B', 1, (f, params) -> (params[0] >= 0),
                (f, caller, params) -> {
                    f.backlash = params[0];
                    f.notifyListeners(caller, Parameters.BACKLASH);
                }),
        FOK1_REVERSE_DIR('D', 1, (f, params) -> (params[0] == 0) || (params[0] == 1),
                (f, caller, params) -> {
                    f.reverseDir = (params[0] == 1);
                    f.notifyListeners(caller, Parameters.REVERSE_DIR);
                }),
        FOK1_POWER_SAVER('H', 1, (f, params) -> (params[0] == 0) || (params[0] == 1),
                (f, caller, params) -> {
                    f.powerSaver = (params[0] == 1);
                    f.notifyListeners(caller, Parameters.ENABLE_POWER_SAVE);
                }),
        POWER_BOX_SET('X', 2, (f, params) -> f.powerBox.contains(params[0]) && (params[1] >= 0) && (params[1] <= 255),
                (f, caller, params) -> {
                    f.powerBox.getPin(params[0]).setValue(params[1]);
                    f.notifyListeners(caller, Parameters.POWERBOX_PINS);
                }),
        POWER_BOX_SET_AUTO_MODE('K', 1, (f, params) -> f.powerBox.supportedAutoModes().contains(PowerBox.AutoModes.values()[params[0]]),
                (f, caller, params) -> {
                    f.powerBox.setAutoMode(params[0]);
                    f.notifyListeners(caller, Parameters.POWERBOX_AUTO_MODE);
                }),
        POWER_BOX_SET_PIN_AUTO('Y', 2, (f, params) -> f.powerBox.contains(params[0]) && ((params[1] == 0) || (params[1] == 1)),
                (f, caller, params) -> {
                    f.powerBox.getPin(params[0]).setAutoModeEn(params[1] == 1);
                    f.notifyListeners(caller, Parameters.POWERBOX_PINS);
                });

        public final char id;
        public final int paramsCount;
        private ParamValidator validator = null;

        private OnDone onDone = null;

        Commands(char id) {
            this.id = id;
            this.paramsCount = 0;
        }

        Commands(char id, int paramsCount, ParamValidator validator) {
            this.id = id;
            this.validator = validator;
            this.paramsCount = paramsCount;
        }

        Commands(char id, int paramsCount, ParamValidator validator, OnDone onDone) {
            this.id = id;
            this.validator = validator;
            this.paramsCount = paramsCount;
            this.onDone = onDone;
        }

        @SuppressWarnings("StringConcatenationInLoop")
        private void run(ThunderFocuser f, Listener caller, int... params) throws ConnectionException, InvalidParamException {
            String cmd = "$" + id;
            if (params.length != paramsCount) {
                throw new InvalidParamException("Missing/too much EasyFocuser parameters.");
            }
            if (validator != null && !validator.validate(f, params)) {
                throw new InvalidParamException("Invalid EasyFocuser parameters.");
            }
            for (int p : params) {
                cmd += p + "%";
            }
            f.serialPort.println(cmd);
            if (onDone != null) {
                onDone.onDone(f, caller, params);
            }
        }

        private interface ParamValidator {
            boolean validate(ThunderFocuser f, int[] params);

        }

        private interface OnDone {
            void onDone(ThunderFocuser f, Listener caller, int[] params);
        }
    }

    public interface Listener {
        void updateConnSate(ConnState connState);

        void updateFocuserState(FocuserState focuserState);

        void onReachedPos();

        void updateParam(Parameters p);

        void onCriticalError(Exception e);
    }

    public static class InvalidParamException extends Exception {
        public InvalidParamException(String s) {
            super(s);
        }
    }

    private class SendSettingsRequestTask extends TimerTask {
        @Override
        public void run() {
            if (!ready && serialPort.isConnected()) {
                try {
                    System.err.println("Sending focuser settings request");
                    Commands.PRINT_CONFIG.run(ThunderFocuser.this, null);
                    timerCount++;
                    if (timerCount < 5) {
                        new Timer("SendSettingsRequestTask #" + timerCount).schedule(new SendSettingsRequestTask(), 500);
                    } else {
                        disconnect();
                        nOnCriticalError(new IllegalStateException("Connection timeout, disconnecting."));
                    }
                } catch (InvalidParamException ignored) {
                } catch (ConnectionException e) {
                    disconnect();
                    nOnCriticalError(e);
                }
            }
        }
    }
}