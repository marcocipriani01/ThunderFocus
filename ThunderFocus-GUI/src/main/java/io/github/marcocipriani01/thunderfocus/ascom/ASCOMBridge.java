package io.github.marcocipriani01.thunderfocus.ascom;

import io.github.marcocipriani01.thunderfocus.Main;
import io.github.marcocipriani01.thunderfocus.board.*;

import java.net.InetAddress;
import java.net.Socket;

import static io.github.marcocipriani01.thunderfocus.Main.board;
import static io.github.marcocipriani01.thunderfocus.Main.settings;

@SuppressWarnings("unused")
public class ASCOMBridge extends SimpleServer {

    private Runnable onClientListChange = null;

    public ASCOMBridge(int port) {
        super(port);
    }

    public ASCOMBridge(int port, Runnable onClientListChang) {
        super(port);
        this.onClientListChange = onClientListChang;
    }

    public void setOnClientListChange(Runnable onClientListChange) {
        this.onClientListChange = onClientListChange;
    }

    @Override
    protected void onMessage(Socket from, String msg) {
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
                println(from, String.valueOf(board.isConnected()));
                return;
            }
            if (!board.isConnected()) {
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

                    case "MaxStep" -> println(String.valueOf(Main.settings.getFocuserMaxTravel()));

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

                    case "HasAmbientSensors" -> println(from, String.valueOf(powerBox.supportsAmbient()));

                    case "MaxSwitch" -> {
                        int max = 0;
                        for (ArduinoPin pin : powerBox.asList()) {
                            int n = pin.getNumber();
                            if (n > max) max = n;
                        }
                        println(from, String.valueOf(max + 1));
                    }

                    case "GetSwitchName", "GetSwitchDescription" -> {
                        int pinNumber = Integer.parseInt(params[0]);
                        if (powerBox.contains(pinNumber))
                            println(from, powerBox.get(pinNumber).getName());
                        else
                            println(from, "<Unavailable>");
                    }

                    case "SetSwitchName" -> {
                        int pinNumber = Integer.parseInt(params[0]);
                        if (powerBox.contains(pinNumber)) {
                            ArduinoPin pin = powerBox.get(pinNumber);
                            pin.setName(params[1]);
                            PowerBox.clonePins(powerBox, settings.powerBoxPins);
                            Main.settings.save();
                        }
                    }

                    case "CanWrite" -> {
                        int pinNumber = Integer.parseInt(params[0]);
                        if (powerBox.contains(pinNumber)) {
                            ArduinoPin pin = powerBox.get(pinNumber);
                            println(from, String.valueOf((!pin.isAutoModeEn()) && (!pin.isOnWhenAppOpen())));
                        } else {
                            println(from, "false");
                        }
                    }

                    case "GetSwitch" -> {
                        int pinNumber = Integer.parseInt(params[0]);
                        if (powerBox.contains(pinNumber)) {
                            ArduinoPin pin = powerBox.get(pinNumber);
                            println(from, pin.isPWMEnabled() ? "PWM" : String.valueOf(pin.getValueBoolean()));
                        } else {
                            println(from, "Unavailable");
                        }
                    }

                    case "SetSwitch" -> {
                        int pinNumber = Integer.parseInt(params[0]);
                        if (powerBox.contains(pinNumber)) {
                            ArduinoPin pin = powerBox.get(pinNumber);
                            if (pin.isAutoModeEn() || pin.isOnWhenAppOpen()) {
                                println(from, "ReadOnly");
                            } else {
                                Main.board.run(Board.Commands.POWER_BOX_SET, null, pinNumber, params[1].contains("true") ? 255 : 0);
                                println(from, "OK");
                            }
                        } else {
                            println(from, "Unavailable");
                        }
                    }

                    case "MaxSwitchValue" -> {
                        int pinNumber = Integer.parseInt(params[0]);
                        if (powerBox.contains(pinNumber)) {
                            ArduinoPin pin = powerBox.get(pinNumber);
                            println(from, pin.isPWMEnabled() ? "255" : "1");
                        } else {
                            println(from, "Unavailable");
                        }
                    }

                    case "GetSwitchValue" -> {
                        int pinNumber = Integer.parseInt(params[0]);
                        if (powerBox.contains(pinNumber)) {
                            ArduinoPin pin = powerBox.get(pinNumber);
                            println(from, pin.isPWMEnabled() ? String.valueOf(pin.getValuePWM()) : String.valueOf(pin.getValueBoolean()));
                        } else {
                            println(from, "Unavailable");
                        }
                    }

                    case "SetSwitchValue" -> {
                        int pinNumber = Integer.parseInt(params[0]);
                        if (powerBox.contains(pinNumber)) {
                            ArduinoPin pin = powerBox.get(pinNumber);
                            if (pin.isAutoModeEn() || pin.isOnWhenAppOpen()) {
                                println(from, "ReadOnly");
                            } else {
                                Main.board.run(Board.Commands.POWER_BOX_SET, null, pinNumber, Integer.parseInt(params[1]));
                                println(from, "OK");
                            }
                        } else {
                            println(from, "Unavailable");
                        }
                    }

                    case "GetTemperature" -> println(from, String.valueOf(powerBox.getTemperature()));

                    case "GetHumidity" -> println(from, String.valueOf(powerBox.getHumidity()));

                    case "DewPoint" -> println(from, String.valueOf(powerBox.getDewPoint()));

                    default -> exit = false;
                }
            }
            if (exit)
                return;
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
            onError(e);
        }
    }

    @Override
    protected boolean acceptClient(InetAddress address) {
        return address.isLinkLocalAddress();
    }

    @Override
    protected void onError(Exception e) {
        e.printStackTrace();
    }

    @Override
    protected void onNewClient(Socket client) {
        if (onClientListChange != null) onClientListChange.run();
    }

    @Override
    protected void onClientLost(Socket client) {
        if (onClientListChange != null) onClientListChange.run();
    }
}