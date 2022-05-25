package io.github.marcocipriani01.thunderfocus.ascom;

import io.github.marcocipriani01.thunderfocus.Main;
import io.github.marcocipriani01.thunderfocus.board.*;

import java.net.InetAddress;
import java.net.Socket;

import static io.github.marcocipriani01.thunderfocus.Main.board;
import static io.github.marcocipriani01.thunderfocus.Main.settings;

public class ASCOMFocuserBridge extends SimpleServer {

    public ASCOMFocuserBridge(int port) {
        super(port);
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
            switch (cmd) {
                case "HasFocuser" -> println(from, String.valueOf(board.hasFocuser()));

                case "HasFlat" -> println(from, String.valueOf(board.hasFlatPanel()));

                case "HasPowerBox" -> println(from, String.valueOf(board.hasPowerBox()));

                case "HasAmbientSensors" -> {
                    PowerBox powerBox = board.powerBox();
                    println(from, String.valueOf((powerBox != null) && powerBox.supportsAmbient()));
                }
            }
            if (board.hasFocuser()) {
                final Focuser focuser = board.focuser();
                switch (cmd) {
                    case "Position" -> println(from, String.valueOf(focuser.getPos()));

                    case "IsMoving" -> println(from,
                            String.valueOf(focuser.getState() == Focuser.FocuserState.MOVING));

                    case "Halt" -> board.run(Board.Commands.FOCUSER_STOP, null);

                    case "Move" -> board.run(Board.Commands.FOCUSER_ABS_MOVE, null, Integer.parseInt(params[0]));

                    case "MaxStep" -> println(String.valueOf(Main.settings.getFocuserMaxTravel()));
                }
            }
            if (board.hasPowerBox()) {
                PowerBox powerBox = board.powerBox();
                switch (cmd) {
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
                            println(from, "NonExistent");
                        }
                    }

                    case "SetSwitch" -> {
                        int pinNumber = Integer.parseInt(params[0]);
                        if (powerBox.contains(pinNumber)) {
                            ArduinoPin pin = powerBox.get(pinNumber);
                            if (pin.isAutoModeEn() || pin.isOnWhenAppOpen()) {
                                println(from, "CannotWrite");
                            } else {
                                Main.board.run(Board.Commands.POWER_BOX_SET, null, pinNumber, params[1].equals("True") ? 255 : 0);
                                println(from, "OK");
                            }
                        } else {
                            println(from, "NonExistent");
                        }
                    }

                    case "MaxSwitchValue" -> {
                        int pinNumber = Integer.parseInt(params[0]);
                        if (powerBox.contains(pinNumber)) {
                            ArduinoPin pin = powerBox.get(pinNumber);
                            println(from, pin.isPWMEnabled() ? "255.0" : "1.0");
                        } else {
                            println(from, "NonExistent");
                        }
                    }

                    case "GetSwitchValue" -> {
                        int pinNumber = Integer.parseInt(params[0]);
                        if (powerBox.contains(pinNumber)) {
                            ArduinoPin pin = powerBox.get(pinNumber);
                            println(from, pin.isPWMEnabled() ? String.valueOf(pin.getValuePWM()) : "Boolean");
                        } else {
                            println(from, "NonExistent");
                        }
                    }

                    case "SetSwitchValue" -> {
                        int pinNumber = Integer.parseInt(params[0]);
                        if (powerBox.contains(pinNumber)) {
                            ArduinoPin pin = powerBox.get(pinNumber);
                            if (pin.isAutoModeEn() || pin.isOnWhenAppOpen()) {
                                println(from, "CannotWrite");
                            } else if (pin.isPWMEnabled()) {
                                Main.board.run(Board.Commands.POWER_BOX_SET, null, pinNumber, Integer.parseInt(params[1]));
                                println(from, "OK");
                            } else {
                                println(from, "Boolean");
                            }
                        } else {
                            println(from, "NonExistent");
                        }
                    }
                }
            }
            if (board.hasFlatPanel()) {
                final FlatPanel flat = board.flat();
                switch (cmd) {
                    case "HasServo" -> println(from, String.valueOf(flat.hasServo()));

                    case "CoverState" -> {
                        if (!flat.hasServo())
                            println(from, "NotPresent");
                        switch (flat.getCoverStatus()) {
                            case CLOSED -> println(from, "Closed");
                            case OPEN -> println(from, "Open");
                            case NEITHER_OPEN_NOR_CLOSED -> println(from, "Moving");
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

                    case "CalibratorState" -> println(from, String.valueOf(flat.getLightStatus()));

                    case "Brightness" -> println(from, String.valueOf(flat.getBrightness()));

                    case "CalibratorOn" -> {
                        board.run(Board.Commands.FLAT_SET_LIGHT, null, 1);
                        board.run(Board.Commands.FLAT_SET_BRIGHTNESS, null, Integer.parseInt(params[0]));
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
        return address.isLoopbackAddress();
    }

    @Override
    protected void onError(Exception e) {
        e.printStackTrace();
    }

    @Override
    protected void onNewClient(Socket client) {

    }

    @Override
    protected void onClientLost(Socket client) {

    }
}