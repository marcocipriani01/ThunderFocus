package marcocipriani01.thunder.focus;

import marcocipriani01.thunder.focus.io.ConnectionException;
import marcocipriani01.thunder.focus.io.SerialMessageListener;
import marcocipriani01.thunder.focus.io.SerialPortImpl;
import marcocipriani01.thunder.focus.powerbox.ArduinoPin;
import marcocipriani01.thunder.focus.powerbox.PinArray;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EasyFocuser implements SerialMessageListener {

    private final SerialPortImpl serialPort = new SerialPortImpl();
    private final ArrayList<Listener> listeners = new ArrayList<>();
    private final PinArray digitalPins = new PinArray();
    private final PinArray pwmPins = new PinArray();
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
    private boolean isPowerBox = false;

    public EasyFocuser() {
        serialPort.addListener(this);
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

    public void disconnect() throws ConnectionException {
        updConnSate(ConnState.TIMEOUT);
        updFocuserState(FocuserState.NONE);
        ready = false;
        serialPort.disconnect();
        updConnSate(ConnState.DISCONNECTED);
        digitalPins.clear();
        pwmPins.clear();
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
        if (exclusiveCaller == null || caller == exclusiveCaller) {
            cmd.run(this, caller, params);
        }
    }

    public boolean isPowerBox() {
        return isPowerBox;
    }

    public PinArray getDigitalPins() {
        return digitalPins;
    }

    public PinArray getPwmPins() {
        return pwmPins;
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
                    if (param.length() > 0) {
                        try {
                            int newCurrentPos = Integer.parseInt(param);
                            if (newCurrentPos != currentPos) {
                                currentPos = newCurrentPos;
                                currentPosTicks = stepsToTicks(currentPos);
                                notifyListeners(null, Parameters.CURRENT_POS);
                                notifyListeners(null, Parameters.CURRENT_POS_TICKS);
                            }

                        } catch (NumberFormatException e) {
                            System.err.println("Non-integer position received: \"" + param + "\"");
                        }

                    } else {
                        System.err.println("Empty position message!");
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

                case 'E' -> // Error
                        updFocuserState(FocuserState.ERROR);

                default -> System.err.println("Unknown message received: \"" + msg + "\"");
            }
        } else {
            if (c == 'Q') {
                try {
                    System.out.println("Focuser settings: " + param);
                    String[] l = param.split(",");
                    if (l[0].equals("0")) {
                        nOnCriticalError(new ThunderFocuserException("ThunderFocus without a focuser module."));
                        disconnect();
                        return;
                    }
                    isPowerBox = l[1].equals("1");
                    currentPos = Integer.parseInt(l[2]);
                    speed = Integer.parseInt(l[3]);
                    powerSaver = Integer.parseInt(l[4]) == 1;
                    backlash = Integer.parseInt(l[5]);
                    reverseDir = Integer.parseInt(l[6]) == 1;
                    if (isPowerBox) {
                        Pattern pattern = Pattern.compile("\\((.*?)\\)");
                        if (!l[7].equals("")) {
                            Matcher m = pattern.matcher(l[7]);
                            PinArray pwmPins = Main.settings.getPwmPins();
                            while (m.find()) {
                                String[] pinDescriptor = m.group(1).split("@");
                                int pin = Integer.parseInt(pinDescriptor[0]), value = Integer.parseInt(pinDescriptor[1]);
                                ArduinoPin existing = pwmPins.getPin(pin);
                                this.pwmPins.add(new ArduinoPin(pin, existing == null ? ("Pin " + pin) : existing.getName(), value));
                            }
                        }
                        if (!l[8].equals("")) {
                            Matcher m = pattern.matcher(l[8]);
                            PinArray digitalPins = Main.settings.getDigitalPins();
                            while (m.find()) {
                                String[] pinDescriptor = m.group(1).split("@");
                                int pin = Integer.parseInt(pinDescriptor[0]), value = Integer.parseInt(pinDescriptor[1]);
                                ArduinoPin existing = digitalPins.getPin(pin);
                                this.digitalPins.add(new ArduinoPin(pin, existing == null ? ("Pin " + pin) : existing.getName(), value));
                            }
                        }
                    }
                    ready = true;
                    updConnSate(ConnState.CONNECTED);

                } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
                    nOnCriticalError(new ThunderFocuserException("The focuser responded with an illegal list of settings."));
                    try {
                        disconnect();
                    } catch (ConnectionException ce) {
                        ce.printStackTrace();
                    }
                } catch (ConnectionException e) {
                    e.printStackTrace();
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
        PWM_PINS,
        DIGITAL_PINS
    }

    public enum ConnState {
        DISCONNECTED("disconnesso"),
        CONNECTED("connesso"),
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
        ERROR("errore"),
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
        PRINT_SETTINGS('Q'),
        FOK_REL_MOVE('R', 1, null, (f, caller, params) -> {
            f.requestedRelPos = params[0];
            f.notifyListeners(caller, Parameters.REQUESTED_REL_POS);
        }),
        FOK_ABS_MOVE('A', 1,
                (f, params) -> (params[0] >= 0) && (params[0] <= Main.settings.getFokMaxTravel()),
                (f, caller, params) -> {
                    f.requestedPos = params[0];
                    f.notifyListeners(caller, Parameters.REQUESTED_POS);
                }),
        FOK_STOP('S'),
        FOK_SET_ZERO('Z'),
        FOK_SET_POS('P', 1, (f, params) -> (params[0] >= 0) && (params[0] <= Main.settings.getFokMaxTravel())),
        FOK_SET_SPEED('V', 1, (f, params) -> (params[0] >= 0) && (params[0] <= 100),
                (f, caller, params) -> {
                    f.speed = params[0];
                    f.notifyListeners(caller, Parameters.SPEED);
                }),
        FOK_SET_BACKLASH('B', 1, (f, params) -> (params[0] >= 0),
                (f, caller, params) -> {
                    f.backlash = params[0];
                    f.notifyListeners(caller, Parameters.BACKLASH);
                }),
        FOK_REVERSE_DIR('C', 1, (f, params) -> (params[0] == 0) || (params[0] == 1),
                (f, caller, params) -> {
                    f.reverseDir = (params[0] == 1);
                    f.notifyListeners(caller, Parameters.REVERSE_DIR);
                }),
        FOK_POWER_SAVER('H', 1, (f, params) -> (params[0] == 0) || (params[0] == 1),
                (f, caller, params) -> {
                    f.powerSaver = (params[0] == 1);
                    f.notifyListeners(caller, Parameters.ENABLE_POWER_SAVE);
                }),
        POWER_BOX_SET('d', 2, (f, params) -> (params[0] >= 0) && (params[1] >= 0) && (params[1] <= 255),
                (f, caller, params) -> {
                    if (f.digitalPins.contains(params[0])) {
                        f.digitalPins.getPin(params[0]).setValue(params[1]);
                        f.notifyListeners(caller, Parameters.DIGITAL_PINS);
                    } else if (f.pwmPins.contains(params[0])) {
                        f.pwmPins.getPin(params[0]).setValue(params[1]);
                        f.notifyListeners(caller, Parameters.PWM_PINS);
                    }
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
        private void run(EasyFocuser f, Listener caller, int... params) throws ConnectionException, InvalidParamException {
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
            boolean validate(EasyFocuser f, int[] params);

        }

        private interface OnDone {
            void onDone(EasyFocuser f, Listener caller, int[] params);
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

    public static class ThunderFocuserException extends RuntimeException {
        public ThunderFocuserException(String s) {
            super(s);
        }
    }

    private class SendSettingsRequestTask extends TimerTask {
        @Override
        public void run() {
            if (!ready && serialPort.isConnected()) {
                try {
                    System.err.println("Sending focuser settings request");
                    Commands.PRINT_SETTINGS.run(EasyFocuser.this, null);
                    timerCount++;
                    if (timerCount < 5) {
                        new Timer("SendSettingsRequestTask #" + timerCount).schedule(new SendSettingsRequestTask(), 500);
                    } else {
                        System.err.println("Connection timeout, disconnecting.");
                        disconnect();
                    }
                } catch (InvalidParamException ignored) {
                } catch (ConnectionException e) {
                    e.printStackTrace();
                    if (e.getType() != ConnectionException.Type.UNABLE_TO_DISCONNECT) {
                        try {
                            disconnect();
                        } catch (ConnectionException ex) {
                            ex.printStackTrace();
                        }
                    }
                    nOnCriticalError(e);
                }
            }

        }
    }
}