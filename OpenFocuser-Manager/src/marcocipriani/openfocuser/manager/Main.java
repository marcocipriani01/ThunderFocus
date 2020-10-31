package marcocipriani.openfocuser.manager;

import marcocipriani.openfocuser.manager.indi.INDIArduinoDriver;
import marcocipriani.openfocuser.manager.indi.INDIServer;
import marcocipriani.openfocuser.manager.io.ConnectionException;
import org.apache.commons.cli.*;

import javax.swing.*;
import java.io.File;

import static marcocipriani.openfocuser.manager.Utils.getStringResource;

/**
 * The main class of the application. Interprets the input commands and
 * runs the server, the driver or the control panel.
 *
 * @author marcocipriani01
 * @version 2.2
 */
public class Main {

    /**
     * Command line options for the parser.
     */
    private static final Options cLIOptions = new Options();
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
    private static INDIServer server = null;
    private static ExecutionMode executionMode;

    static {
        cLIOptions.addOption("c", "control-panel", false, getStringResource("control_panel_param"));
        cLIOptions.addOption("d", "driver", false, getStringResource("driver_only_param"));
        cLIOptions.addOption("p", "indi-port", true, getStringResource("server_mode_param"));
        cLIOptions.addOption("a", "serial-port", true, getStringResource("serial_port_param"));
        cLIOptions.addOption("v", "verbose", false, getStringResource("verbose_mode"));
    }

    /**
     * Main. Configures the Look And Feel and starts the application.
     *
     * @param args command line arguments.
     */
    public static void main(String[] args) {
        CommandLineParser parser = new DefaultParser();
        boolean autoConnectSerial = false;
        try {
            CommandLine line = parser.parse(cLIOptions, args);
            verboseMode = line.hasOption('v');
            String userHome = System.getProperty("user.home");
            userHome += (userHome.endsWith(File.separator) ? "" : File.separator) + ".config" + File.separator + AppInfo.APP_NAME;
            Utils.info("Configuration file folder: " + userHome);
            File cfgFolder = new File(userHome);
            if (cfgFolder.exists()) {
                if (cfgFolder.isFile()) {
                    Utils.exit(Utils.ExitCodes.CFG_FOLDER_ERROR);
                }

            } else {
                if (!cfgFolder.mkdirs()) {
                    Utils.exit(Utils.ExitCodes.CFG_FOLDER_ERROR);
                }
            }
            settings = Settings.load(cfgFolder);

            boolean controlPanel = line.hasOption('c'),
                    serverMode = line.hasOption('p'),
                    driverOnly = line.hasOption('d');

            if (line.hasOption('a')) {
                settings.serialPort = line.getOptionValue('a');
                autoConnectSerial = true;
            }

            if (serverMode) {
                try {
                    int sp = Integer.parseInt(line.getOptionValue('p'));
                    if (sp != 0) {
                        settings.localServerIndiPort = sp;
                        settings.save();
                    }

                } catch (NumberFormatException e) {
                    Utils.exit(Utils.ExitCodes.PARSING_ERROR);
                }
            }

            if (serverMode && (!controlPanel && !driverOnly)) {
                Utils.info("Welcome to OpenFocuser-Manager CLI server!");
                executionMode = ExecutionMode.CLI_SERVER;
                runServer(settings.localServerIndiPort);
                Utils.info("Ctrl-C to stop");

            } else if (driverOnly && (!controlPanel && !serverMode)) {
                executionMode = ExecutionMode.DRIVER;
                new INDIArduinoDriver(System.in, System.out, autoConnectSerial);

            } else if (!serverMode && !driverOnly) {
                executionMode = ExecutionMode.GUI;
                SwingUtilities.invokeLater(() -> {
                    try {
                        for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                            if ("Nimbus".equals(info.getName())) {
                                UIManager.setLookAndFeel(info.getClassName());
                                break;
                            }
                        }

                    } catch (UnsupportedLookAndFeelException | IllegalAccessException | InstantiationException | ClassNotFoundException e) {
                        Utils.err(e);
                    }
                    new ControlPanel();
                });

            } else {
                Utils.exit(Utils.ExitCodes.INVALID_OPTIONS);
            }

        } catch (ParseException e) {
            Utils.exit(Utils.ExitCodes.PARSING_ERROR);
        }
    }

    public static ExecutionMode getExecutionMode() {
        return executionMode;
    }

    /**
     * @return the CLI options parser for this application.
     */
    public static Options getCLIOptions() {
        return cLIOptions;
    }

    /**
     * Runs the server with the INDI driver.
     */
    public static void runServer(int port) {
        if (server == null) {
            server = new INDIServer(port);
            server.loadJava(INDIArduinoDriver.class);

        } else {
            throw new ConnectionException("Server already started!", ConnectionException.Type.ALREADY_STARTED);
        }
    }

    public static void stopServer() {
        if (server != null) {
            server.stopServer();
            server = null;

        } else {
            throw new ConnectionException("Server not started!", ConnectionException.Type.NOT_STARTED);
        }
    }

    /**
     * @return the current running server.
     */
    public static boolean isServerRunning() {
        return server != null && server.isServerRunning();
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
        return executionMode == ExecutionMode.GUI;
    }

    /**
     * @return the current settings.
     */
    public static Settings getSettings() {
        return settings;
    }

    public enum ExecutionMode {
        CLI_SERVER, GUI, DRIVER
    }
}