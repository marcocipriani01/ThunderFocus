package marcocipriani.openfocuser.manager;

import marcocipriani.openfocuser.manager.indi.INDIArduinoDriver;
import marcocipriani.openfocuser.manager.indi.INDIServer;
import marcocipriani.openfocuser.manager.plus.ServerMiniWindow;
import marcocipriani.openfocuser.manager.plus.Settings;
import org.apache.commons.cli.*;

import javax.swing.*;
import java.io.File;

/**
 * The main class of the application. Interprets the input commands and
 * runs the server, the driver or the control panel.
 *
 * @author marcocipriani01
 * @version 2.1
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class Main {

    /**
     * Command line options for the parser.
     */
    private static final Options cliOptions = new Options();
    /**
     * Verbose/debug mode.
     */
    private static boolean verboseMode = false;
    /**
     * Global settings
     */
    private static Settings settings;
    /**
     * The current running server.
     */
    private static INDIServer server;
    /**
     * Do not show the control panel.
     */
    private static boolean showGUI = false;

    static {
        /*Option settingsOption = new Option("s", "settings", true,
                "The directory where OpenFocuser-Manager will save and retrieve its settings (normally, automatically set by the application runner).");
        settingsOption.setRequired(true);
        cliOptions.addOption(settingsOption);*/
        cliOptions.addOption("c", "control-panel", false,
                "Shows the control panel.");
        cliOptions.addOption("d", "driver", false,
                "Driver-only mode (no server, stdin/stdout)");
        cliOptions.addOption("p", "indi-port", true,
                "Stand-alone server mode, CLI. If port=0, fetch the last used port from the settings.");
        cliOptions.addOption("a", "serial-port", true,
                "Specifies a serial port and connects to it if possible. Otherwise it will be stored to settings only.");
        cliOptions.addOption("v", "verbose", false, "Verbose mode.");
    }

    /**
     * Main. Configures the L&F and starts the application.
     *
     * @param args command line arguments.
     */
    public static void main(String[] args) {
        CommandLineParser parser = new DefaultParser();
        boolean autoConnectSerial = false;
        try {
            CommandLine line = parser.parse(cliOptions, args);

            verboseMode = line.hasOption('v');

            if (verboseMode) {
                System.err.println("Loading data...");
            }
            String userHome = System.getProperty("user.home");
            userHome += (userHome.endsWith(File.separator) ? "" : File.separator) + ".config" + File.separator + AppInfo.APP_NAME;
            err("Configuration file folder: " + userHome);
            File cfgFolder = new File(userHome);
            if (cfgFolder.exists()) {
                if (cfgFolder.isFile()) {
                    exit(ExitCodes.CFG_FOLDER_ERROR);
                }

            } else {
                if (!cfgFolder.mkdirs()) {
                    exit(ExitCodes.CFG_FOLDER_ERROR);
                }
            }
            settings = Settings.load(cfgFolder);

            boolean controlPanel = showGUI = line.hasOption('c'),
                    serverMode = line.hasOption('p'),
                    driverOnly = line.hasOption('d');

            if (line.hasOption('a')) {
                settings.setUsbPort(line.getOptionValue('a'));
                autoConnectSerial = true;
            }

            int serverPort = settings.getIndiPort();
            if (serverMode) {
                try {
                    int gp = Integer.valueOf(line.getOptionValue('p'));
                    if (gp != 0) {
                        serverPort = gp;
                        settings.setIndiPort(serverPort);
                        settings.save();
                    }

                } catch (NumberFormatException e) {
                    exit(ExitCodes.PARSING_ERROR);
                }
            }

            if (serverMode && (!controlPanel && !driverOnly)) {
                Main.info("Welcome to OpenFocuser-Manager CLI server!", false);
                runServer(serverPort);
                Main.info("Ctrl-C to stop");

            } else if (driverOnly && (!controlPanel && !serverMode)) {
                new INDIArduinoDriver(System.in, System.out, autoConnectSerial);

            } else if (!serverMode && !driverOnly) {
                showGUI = true;
                SwingUtilities.invokeLater(() -> {
                    try {
                        err("Setting up L&F...");
                        for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                            if ("Nimbus".equals(info.getName())) {
                                UIManager.setLookAndFeel(info.getClassName());
                                break;
                            }
                        }

                    } catch (UnsupportedLookAndFeelException | IllegalAccessException | InstantiationException | ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                    new ControlPanel() {
                        @Override
                        protected void onRunServer(int port) {
                            runServer(port);
                            if (Main.isGUIEnabled()) {
                                SwingUtilities.invokeLater(ServerMiniWindow::new);
                            }
                        }
                    };
                });

            } else {
                exit(ExitCodes.INVALID_OPTIONS);
            }

        } catch (ParseException e) {
            exit(ExitCodes.PARSING_ERROR);
        }
    }

    /**
     * Runs the server with the INDI driver.
     */
    private static void runServer(int port) {
        server = new INDIServer(port);
        server.loadJava(INDIArduinoDriver.class);
    }

    /**
     * @return {@code true} if the user enabled the verbose/debug mode.
     */
    public static boolean isVerboseMode() {
        return verboseMode;
    }

    /**
     * @return {@code true} if the GUI is enabled.
     */
    public static boolean isGUIEnabled() {
        return showGUI;
    }

    /**
     * @return the current settings.
     */
    public static Settings getSettings() {
        return settings;
    }

    /**
     * @return the current running server.
     */
    public static INDIServer getServer() {
        return server;
    }

    /**
     * Closes the app.
     *
     * @param code an exit code.
     */
    public static void exit(int code) {
        exit(ExitCodes.fromCode(code));
    }

    /**
     * Closes the app.
     *
     * @param code an exit code.
     */
    public static void exit(ExitCodes code) {
        if (server != null && server.isServerRunning()) {
            server.stopServer();
        }
        String message = code.getMessage();
        if (message != null) {
            err(message, true);
        }
        if ((code == ExitCodes.INVALID_OPTIONS) || (code == ExitCodes.PARSING_ERROR)) {
            new HelpFormatter().printHelp("openfocuser [-v] -c/-d/-p=xxxx [-a=/dev/ttyUSBx]",
                    "Command line options description:", cliOptions,
                    "Licensed under the Apache License Version 2.0\nAn application by marcocipriani01");
        }
        System.exit(code.getCode());
    }

    /**
     * Closes the app.
     */
    public static void exit() {
        exit(0);
    }

    /**
     * Reports an error to the user.
     *
     * @param msg        a message that will be printed to {@link System#err} if verbose mode is on
     * @param e          the exception you want to print the stacktrace of.
     * @param showDialog set to true to show a visual dialog with the same message.
     */
    public static void err(String msg, Exception e, boolean showDialog) {
        if (verboseMode) {
            System.err.println(msg);
            e.printStackTrace();
        }
        if (showGUI && showDialog) {
            JOptionPane.showMessageDialog(null, msg, AppInfo.APP_NAME, JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Reports an error to the user.
     *
     * @param msg          a message that will be printed to {@link System#err} if verbose mode is on
     * @param e            the exception you want to print the stacktrace of.
     * @param dialogParent parent window for the dialog.
     */
    public static void err(String msg, Exception e, JFrame dialogParent) {
        if (verboseMode) {
            System.err.println(msg);
            e.printStackTrace();
        }
        if (showGUI) {
            JOptionPane.showMessageDialog(dialogParent, msg, AppInfo.APP_NAME, JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Reports an error to the user.
     *
     * @param msg        a message that will be printed to {@link System#err} if verbose mode is on
     * @param showDialog set to true to show a visual dialog with the same message.
     */
    public static void err(String msg, boolean showDialog) {
        if (verboseMode) {
            System.err.println(msg);
        }
        if (showGUI && showDialog) {
            JOptionPane.showMessageDialog(null, msg, AppInfo.APP_NAME, JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Reports an error to the user.
     *
     * @param msg          a message that will be printed to {@link System#err} if verbose mode is on
     * @param dialogParent parent window for the dialog.
     */
    public static void err(String msg, JFrame dialogParent) {
        if (verboseMode) {
            System.err.println(msg);
        }
        if (showGUI) {
            JOptionPane.showMessageDialog(dialogParent, msg, AppInfo.APP_NAME, JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Reports an error to the user.
     *
     * @param msg a message that will be printed to {@link System#err} if verbose mode is on
     */
    public static void err(String msg) {
        err(msg, false);
    }

    /**
     * Reports an info to the user.
     *
     * @param msg        a message that will be printed to {@link System#err} ({@link System#out} may have been used for the INDI driver).
     * @param showDialog set to true to show a visual dialog with the same message.
     */
    public static void info(String msg, boolean showDialog) {
        System.err.println(msg);
        if (showGUI && showDialog) {
            JOptionPane.showMessageDialog(null, msg, AppInfo.APP_NAME, JOptionPane.INFORMATION_MESSAGE);
        }
    }

    /**
     * Reports an info to the user.
     *
     * @param msg          a message that will be printed to {@link System#err} ({@link System#out} may have been used for the INDI driver).
     * @param dialogParent parent window for the dialog.
     */
    public static void info(String msg, JFrame dialogParent) {
        System.err.println(msg);
        if (showGUI) {
            JOptionPane.showMessageDialog(dialogParent, msg, AppInfo.APP_NAME, JOptionPane.INFORMATION_MESSAGE);
        }
    }

    /**
     * Reports an info to the user.
     *
     * @param msg a message that will be printed to {@link System#err} ({@link System#out} may have been used for the INDI driver).
     */
    public static void info(String msg) {
        System.err.println(msg);
    }

    /**
     * A list of common exit codes.
     *
     * @author marcocipriani01
     * @version 0.1
     */
    public enum ExitCodes {
        OK(0),
        CFG_FOLDER_ERROR(8, "Config folder could not be initialized!"),
        PARSING_ERROR(9, "Unable to parse parameters!"),
        INVALID_OPTIONS(10, "Invalid options!"),
        SOCAT_ERROR(11, "socat error!");

        /**
         * The exit code.
         */
        private int code;
        /**
         * A message.
         */
        private String message;

        /**
         * Enum constructor.
         *
         * @param code integer, exit code.
         */
        ExitCodes(int code) {
            this(code, null);
        }

        /**
         * Enum constructor.
         *
         * @param code    integer, exit code.
         * @param message a message.
         */
        ExitCodes(int code, String message) {
            this.code = code;
            this.message = message;
        }

        /**
         * Returns the {@link ExitCodes} object associated to the given exit code.
         *
         * @param code an exit code.
         * @return the {@link ExitCodes} object associated to the given exit code. {@code null} if nothing matches the given code.
         */
        public static ExitCodes fromCode(int code) {
            for (ExitCodes c : values()) {
                if (c.code == code) {
                    return c;
                }
            }
            return null;
        }

        /**
         * @return a message to be printed in case of error. Can be null.
         */
        public String getMessage() {
            return message;
        }

        /**
         * @return the stored exit code.
         */
        public int getCode() {
            return code;
        }
    }
}