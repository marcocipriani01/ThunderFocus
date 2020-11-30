package marcocipriani01.thunderfocus;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;
import marcocipriani01.thunderfocus.focuser.ThunderFocuser;
import marcocipriani01.thunderfocus.indi.INDIServerCreator;
import marcocipriani01.thunderfocus.io.ConnectionException;

import javax.swing.*;
import java.awt.*;
import java.net.*;
import java.util.Enumeration;
import java.util.Locale;

public class Main {

    public static final String APP_NAME = "ThunderFocus";
    public static final Image APP_LOGO = Toolkit.getDefaultToolkit().getImage(Main.class.getResource("/marcocipriani01/thunderfocus/res/ThunderFocus.png"));
    public static final Settings settings = Settings.load();
    public static final ThunderFocuser focuser = new ThunderFocuser();
    public static final INDIServerCreator indiServerCreator = new INDIServerCreator();

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                switch (settings.getTheme()) {
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
        indiServerCreator.stop();
        if (focuser.isConnected()) {
            try {
                focuser.disconnect();
            } catch (ConnectionException e) {
                e.printStackTrace();
            }
        }
        System.exit(code);
    }

    public static String getIP(boolean localhost) throws SocketException, IllegalStateException {
        if (localhost) {
            return "localhost";
        }
        Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
        while (interfaces.hasMoreElements()) {
            NetworkInterface networkInterface = interfaces.nextElement();
            if (networkInterface.isLoopback() || !networkInterface.isUp()) continue;
            Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
            while (addresses.hasMoreElements()) {
                InetAddress address = addresses.nextElement();
                if (address instanceof Inet6Address) continue;
                return address.getHostAddress();
            }
        }
        throw new IllegalStateException("No network interface found.");
    }

    public static void openBrowser(String url, JFrame frame) {
        Desktop desktop;
        try {
            if (Desktop.isDesktopSupported() && (desktop = Desktop.getDesktop()).isSupported(Desktop.Action.BROWSE)) {
                desktop.browse(new URI(url));
            } else if (System.getProperty("os.name", "generic").toLowerCase(Locale.ENGLISH).contains("nux")) {
                Runtime.getRuntime().exec("xdg-open " + url);
            } else {
                throw new UnsupportedOperationException("Browser support not found.");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(frame, "Errore durante l'apertura del browser!", APP_NAME, JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void openBrowser(URI uri, JFrame frame) {
        Desktop desktop;
        try {
            if (Desktop.isDesktopSupported() && (desktop = Desktop.getDesktop()).isSupported(Desktop.Action.BROWSE)) {
                desktop.browse(uri);
            } else if (System.getProperty("os.name", "generic").toLowerCase(Locale.ENGLISH).contains("nux")) {
                Runtime.getRuntime().exec("xdg-open " + uri.toString());
            } else {
                throw new UnsupportedOperationException("Browser support not found.");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(frame, "Errore durante l'apertura del browser!", APP_NAME, JOptionPane.ERROR_MESSAGE);
        }
    }
}