package marcocipriani01.thunder.focus;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;
import marcocipriani01.thunder.focus.io.ConnectionException;
import org.indilib.i4j.server.INDIServerAccessImpl;
import org.indilib.i4j.server.api.INDIServerInterface;

import javax.swing.*;
import java.awt.*;

public class Main {

    public static final String APP_NAME = "ThunderFocus";
    public static final Image APP_LOGO = Toolkit.getDefaultToolkit().getImage(Main.class.getResource("/marcocipriani01/thunder/focus/res/ThunderFocus.png"));
    public static final Settings settings = Settings.load();
    public static final EasyFocuser focuser = new EasyFocuser();
    public static final INDIServerAccessImpl indiServerAccess = new INDIServerAccessImpl();

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                switch (settings.theme) {
                    case 0 -> FlatLightLaf.install();
                    case 1 -> FlatDarkLaf.install();
                    case 2 -> UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                }
            } catch (UnsupportedLookAndFeelException | IllegalAccessException | InstantiationException | ClassNotFoundException e) {
                e.printStackTrace();
            }
            new MainWindow();
        });
    }

    public static void exit(int code) {
        INDIServerInterface server = indiServerAccess.get();
        if (server != null && server.isServerRunning()) {
            server.stopServer();
        }
        if (focuser.isConnected()) {
            try {
                focuser.disconnect();
            } catch (ConnectionException e) {
                e.printStackTrace();
            }
        }
        System.exit(code);
    }
}