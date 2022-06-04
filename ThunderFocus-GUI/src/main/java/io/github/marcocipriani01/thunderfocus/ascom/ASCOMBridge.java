package io.github.marcocipriani01.thunderfocus.ascom;

import io.github.marcocipriani01.thunderfocus.Main;
import io.github.marcocipriani01.thunderfocus.board.*;

import java.net.InetAddress;
import java.net.Socket;

import static io.github.marcocipriani01.thunderfocus.Main.board;
import static io.github.marcocipriani01.thunderfocus.Main.settings;

public final class ASCOMBridge extends SimpleServer {

    private final Runnable onClientListChange;

    public ASCOMBridge(int port, Runnable onClientListChang) {
        super(port);
        this.onClientListChange = onClientListChang;
    }

    @Override
    protected synchronized void onMessage(Socket from, String msg) {
        try {
            String cmd;
            String[] params = {};
            if (msg.contains("=")) {
                cmd = msg.substring(0, msg.indexOf('='));
                params = msg.substring(msg.indexOf('=') + 1).split(",");
            } else {
                cmd = msg;
            }
            if (cmd.equals("Connected")) {
                println(from, String.valueOf(board.isReady()));
                return;
            }
            if (!board.isReady()) {
                println(from, "Disconnected");
                return;
            }
            boolean exit = true;
            final Focuser focuser = board.focuser();
            if (focuser == null) {
                if (cmd.equals("HasFocuser")) println(from, "false");
                else exit = false;
            } else {
                switch (cmd) {
                    case "HasFocuser" -> println(from, "true");

                    case "Position" -> println(from, String.valueOf(focuser.getPos()));

                    case "IsMoving" -> println(from,
                            String.valueOf(focuser.getState() == Focuser.FocuserState.MOVING));

                    case "Halt" -> board.run(Board.Commands.FOCUSER_STOP, null);

                    case "Move" -> board.run(Board.Commands.FOCUSER_ABS_MOVE, null, Integer.parseInt(params[0]));

                    case "MaxStep" -> println(String.valueOf(settings.getFocuserMaxTravel()));

                    default -> exit = false;
                }
            }
            if (exit) return;
            exit = true;
            PowerBox powerBox = board.powerBox();
            if (powerBox == null) {
                if (cmd.equals("HasPowerBox") || cmd.equals("HasAmbientSensors")) println(from, "false");
                else exit = false;
            } else {
                switch (cmd) {
                    case "HasPowerBox" -> println(from, "true");

                    case "HasAmbientSensors" -> println(from, String.valueOf(powerBox.hasAmbientSensors()));

                    case "MaxSwitch" -> println(from, String.valueOf(powerBox.maxPinNumber() + 1));

                    case "GetSwitchNames" -> {
                        int max = powerBox.maxPinNumber();
                        StringBuilder sb = new StringBuilder();
                        for (int i = 0; i <= max; i++) {
                            ArduinoPin pin = powerBox.get(i);
                            sb.append((pin == null) ? "Unavailable" : pin.getName());
                            if (i < max) sb.append(";");
                        }
                        println(from, sb.toString());
                    }

                    case "GetSwitchDescriptions" -> {
                        int max = powerBox.maxPinNumber();
                        StringBuilder sb = new StringBuilder();
                        for (int i = 0; i <= max; i++) {
                            ArduinoPin pin = powerBox.get(i);
                            if (pin == null) {
                                sb.append("This switch doesn't exist on the powerbox.");
                            } else {
                                sb.append(pin.getName());
                                if (pin.isPWMEnabled()) sb.append(" (PWM)");
                                if (pin.isAutoModeEn() || pin.isOnWhenAppOpen()) sb.append(", automatic mode enabled.");
                            }
                            if (i < max) sb.append(";");
                        }
                        println(from, sb.toString());
                    }

                    case "CanWrite" -> {
                        int max = powerBox.maxPinNumber();
                        StringBuilder sb = new StringBuilder();
                        for (int i = 0; i <= max; i++) {
                            ArduinoPin pin = powerBox.get(i);
                            sb.append((pin != null) && pin.canWrite());
                            if (i < max) sb.append(";");
                        }
                        println(from, sb.toString());
                    }

                    case "GetSwitches" -> {
                        int max = powerBox.maxPinNumber();
                        StringBuilder sb = new StringBuilder();
                        for (int i = 0; i <= max; i++) {
                            ArduinoPin pin = powerBox.get(i);
                            sb.append((pin == null) ? 0 : (pin.isPWMEnabled() ? pin.getValuePWM() :
                                    (pin.getValueBoolean() ? 1 : 0)));
                            if (i < max) sb.append(";");
                        }
                        println(from, sb.toString());
                    }

                    case "GetSwitch" -> {
                        ArduinoPin pin = powerBox.get(Integer.parseInt(params[0]));
                        if (pin == null) {
                            println(from, "0");
                        } else {
                            int value = pin.getValuePWM();
                            println(from, pin.isPWMEnabled() ? String.valueOf(value) : ((value > 100) ? "1" : "0"));
                        }
                    }

                    case "MaxSwitchValues" -> {
                        int max = powerBox.maxPinNumber();
                        StringBuilder sb = new StringBuilder();
                        for (int i = 0; i <= max; i++) {
                            ArduinoPin pin = powerBox.get(i);
                            sb.append((pin == null) ? 1 : (pin.isPWMEnabled() ? 255 : 1));
                            if (i < max) sb.append(";");
                        }
                        println(from, sb.toString());
                    }

                    case "SetSwitch" -> {
                        int pinNumber = Integer.parseInt(params[0]);
                        ArduinoPin pin = powerBox.get(pinNumber);
                        if (pin == null) {
                            println(from, "Unavailable");
                        } else {
                            if (pin.isAutoModeEn() || pin.isOnWhenAppOpen()) {
                                println(from, "ReadOnly");
                            } else {
                                int val = Integer.parseInt(params[1]);
                                if (pin.isDigitalPin() && (val == 1)) val = 255;
                                Main.board.run(Board.Commands.POWER_BOX_SET_PIN, null, pinNumber, val);
                                println(from, "OK");
                            }
                        }
                    }

                    case "GetTemperature" -> println(from, String.valueOf(powerBox.getTemperature()));

                    case "GetHumidity" -> println(from, String.valueOf(powerBox.getHumidity()));

                    case "DewPoint" -> println(from, String.valueOf(powerBox.getDewPoint()));

                    default -> exit = false;
                }
            }
            if (exit) return;
            final FlatPanel flat = board.flat();
            if (flat == null) {
                if (cmd.equals("HasFlat")) println(from, "false");
            } else {
                switch (cmd) {
                    case "HasFlat" -> println(from, "true");

                    case "HasServo" -> println(from, String.valueOf(flat.hasServo()));

                    case "CoverState" -> {
                        if (!flat.hasServo())
                            println(from, "NotPresent");
                        switch (flat.getCoverStatus()) {
                            case CLOSED -> println(from, "Closed");
                            case OPEN -> println(from, "Open");
                            case NEITHER_OPEN_NOR_CLOSED -> println(from, "Moving");
                            case HALT -> println(from, "Error");
                        }
                    }

                    case "OpenCover" -> {
                        if (flat.hasServo()) {
                            board.run(Board.Commands.FLAT_SET_COVER, null, 1);
                            println(from, "OK");
                        } else {
                            println(from, "Error");
                        }
                    }

                    case "CloseCover" -> {
                        if (flat.hasServo()) {
                            board.run(Board.Commands.FLAT_SET_COVER, null, 0);
                            println(from, "OK");
                        } else {
                            println(from, "Error");
                        }
                    }

                    case "HaltCover" -> {
                        if (flat.hasServo()) {
                            board.run(Board.Commands.FLAT_HALT, null);
                            println(from, "OK");
                        } else {
                            println(from, "Error");
                        }
                    }

                    case "CalibratorState" -> println(from, flat.getLightStatus() ? "Ready" : "Off");

                    case "Brightness" -> println(from, String.valueOf(flat.getBrightness()));

                    case "CalibratorOn" -> {
                        board.run(Board.Commands.FLAT_SET_BRIGHTNESS, null, Integer.parseInt(params[0]));
                        board.run(Board.Commands.FLAT_SET_LIGHT, null, 1);
                    }

                    case "CalibratorOff" -> {
                        board.run(Board.Commands.FLAT_SET_LIGHT, null, 0);
                        board.run(Board.Commands.FLAT_SET_BRIGHTNESS, null, 0);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected boolean acceptClient(InetAddress address) {
        return address.isLinkLocalAddress();
    }

    @Override
    protected void onNewClient(Socket client) {
        onClientListChange.run();
    }

    @Override
    protected void onClientLost(Socket client) {
        onClientListChange.run();
    }
}