package io.github.marcocipriani01.thunderfocus;

import io.github.marcocipriani01.simplesocket.SimpleServer;
import io.github.marcocipriani01.thunderfocus.board.ThunderFocuser;

import java.net.InetAddress;
import java.net.Socket;

import static io.github.marcocipriani01.thunderfocus.Main.i18n;

public class ASCOMFocuserBridge extends SimpleServer {

    public ASCOMFocuserBridge(int port) {
        super(port);
    }

    @Override
    protected void onMessage(Socket from, String msg) {
        try {
            int param = Integer.MIN_VALUE;
            String cmd;
            if (msg.contains("=")) {
                String[] split = msg.split("=");
                cmd = split[0];
                param = Integer.parseInt(split[1]);
            } else {
                cmd = msg;
            }
            switch (cmd) {
                case "ThunderFocusPing" -> {
                    if (Main.focuser.isConnected()) {
                        println("ThunderFocusPingOK");
                    } else {
                        println("ThunderFocusNotConnected");
                    }
                }
                case "Connected" -> println(from, Main.focuser.isConnected());
                case "Position" -> println(from, Main.focuser.getCurrentPos());
                case "IsMoving" -> println(from, Main.focuser.getFocuserState() == ThunderFocuser.FocuserState.MOVING);
                case "Halt" -> Main.focuser.run(ThunderFocuser.Commands.FOK1_STOP, null);
                case "DriverInfo" -> {
                    String gui = Main.getAppVersion();
                    if (gui == null) gui = "<?>";
                    println(Main.APP_NAME + " v" + gui + ", " + i18n("board") + " v" + Main.focuser.getVersion());
                }
                case "Version" -> {
                    String gui = Main.getAppVersion();
                    if (gui == null) gui = "0.0";
                    println(gui);
                }
                case "Name" -> println(Main.APP_NAME);
                case "Move" -> {
                    if (param == Integer.MIN_VALUE) return;
                    Main.focuser.run(ThunderFocuser.Commands.FOK1_ABS_MOVE, null, param);
                }
                case "MaxStep" -> println(Main.settings.getFokMaxTravel());
            }
        } catch (Exception e) {
            onError(e);
        }
    }

    @Override
    protected void onError(Exception e) {
        e.printStackTrace();
    }

    @Override
    protected boolean acceptClient(InetAddress address) {
        return true;
    }

    @Override
    protected void onNewClient(Socket client) {

    }

    @Override
    protected void onClientRemoved(Socket client) {

    }
}