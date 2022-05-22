package io.github.marcocipriani01.thunderfocus.ascom;

import io.github.marcocipriani01.thunderfocus.Main;
import io.github.marcocipriani01.thunderfocus.board.Focuser;
import io.github.marcocipriani01.thunderfocus.board.Board;

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
                    if (Main.board.isConnected()) {
                        println("ThunderFocusPingOK");
                    } else {
                        println("ThunderFocusNotConnected");
                    }
                }

                case "Connected" -> println(from, String.valueOf(Main.board.isConnected()));

                case "Position" -> println(from, String.valueOf(Main.board.focuser().getPos()));

                case "IsMoving" -> println(from,
                        String.valueOf(Main.board.focuser().getState() == Focuser.FocuserState.MOVING));

                case "Halt" -> Main.board.run(Board.Commands.FOCUSER_STOP, null);

                case "DriverInfo" -> {
                    String gui = Main.getAppVersion();
                    if (gui == null) gui = "<?>";
                    println(Main.APP_NAME + " v" + gui + ", " + i18n("board") + " v" + Main.board.getVersion());
                }

                case "Version" -> {
                    String gui = Main.getAppVersion();
                    if (gui == null) gui = "0.0";
                    println(gui);
                }

                case "Name" -> println(Main.APP_NAME);

                case "Move" -> {
                    if (param == Integer.MIN_VALUE) return;
                    Main.board.run(Board.Commands.FOCUSER_ABS_MOVE, null, param);
                }

                case "MaxStep" -> println(String.valueOf(Main.settings.getFocuserMaxTravel()));
            }
        } catch (Exception e) {
            onError(e);
        }
    }

    @Override
    protected boolean acceptClient(InetAddress address) {
        return true;
    }

    @Override
    protected void onError(Exception e) {
        e.printStackTrace();
    }

    @Override
    protected void onNewClient(Socket client) {

    }

    @Override
    protected void onClientRemoved(Socket client) {

    }
}