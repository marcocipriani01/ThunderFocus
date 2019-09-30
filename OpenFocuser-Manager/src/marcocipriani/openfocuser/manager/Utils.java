package marcocipriani.openfocuser.manager;

import org.apache.commons.cli.HelpFormatter;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * General logging utils and OS detection.
 *
 * @author marcocipriani01
 * @version 1.0
 */
public class Utils {

    /**
     * The current operating system.
     *
     * @see <a href="https://stackoverflow.com/a/18417382">How do I programmatically determine operating system in Java?</a>
     */
    public static final OperatingSystem COMPUTER_OS;

    /**
     * Resources bundle for strings.
     */
    private static final ResourceBundle STRINGS = ResourceBundle.getBundle("marcocipriani/openfocuser/manager/res/strings", Locale.getDefault());

    static {
        String os = System.getProperty("os.name", "generic").toLowerCase(Locale.ENGLISH);
        if ((os.contains("mac")) || (os.contains("darwin"))) {
            COMPUTER_OS = Utils.OperatingSystem.MacOS;

        } else if (os.contains("win")) {
            COMPUTER_OS = Utils.OperatingSystem.Windows;

        } else if (os.contains("nux")) {
            COMPUTER_OS = Utils.OperatingSystem.Linux;

        } else {
            COMPUTER_OS = Utils.OperatingSystem.Other;
        }
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
        if (Main.isServerRunning()) {
            Main.stopServer();
        }
        String message = code.getMessage();
        if (message != null) {
            err(message, (JFrame) null);
        }
        if ((code == ExitCodes.INVALID_OPTIONS) || (code == ExitCodes.PARSING_ERROR)) {
            new HelpFormatter().printHelp("openfocuser [-v] -c/-d/-p=xxxx [-a=/dev/ttyUSBx]",
                    "Command line options description:", Main.getCLIOptions(),
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
     * @param msg a message that will be printed to {@link System#err} if verbose mode is on
     * @param e   the exception you want to print the stacktrace of.
     */
    public static void err(String msg, Exception e) {
        System.err.println(msg);
        if (Main.isVerboseMode()) {
            e.printStackTrace();
        }
    }

    /**
     * Reports an error to the user.
     *
     * @param e the exception you want to print the stacktrace of.
     */
    public static void err(Exception e) {
        err(e.getMessage(), e);
    }

    /**
     * Reports an error to the user.
     *
     * @param msg          a message that will be printed to {@link System#err} if verbose mode is on
     * @param e            the exception you want to print the stacktrace of.
     * @param dialogParent parent window for the dialog.
     */
    public static void err(String msg, Exception e, JFrame dialogParent) {
        err(msg, e);
        if (Main.isGUIEnabled()) {
            JOptionPane.showMessageDialog(dialogParent, msg, AppInfo.APP_NAME, JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Reports an error to the user.
     *
     * @param msg          a message that will be printed to {@link System#err} if verbose mode is on
     * @param dialogParent parent window for the dialog.
     */
    public static void err(String msg, JFrame dialogParent) {
        System.err.println(msg);
        if (Main.isGUIEnabled()) {
            JOptionPane.showMessageDialog(dialogParent, msg, AppInfo.APP_NAME, JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Reports an error to the user.
     *
     * @param msg a message that will be printed to {@link System#err} if verbose mode is on
     */
    public static void err(String msg) {
        System.err.println(msg);
    }

    /**
     * Reports an info to the user.
     *
     * @param msg          a message that will be printed to {@link System#err} ({@link System#out} may have been used for the INDI driver).
     * @param dialogParent parent window for the dialog.
     */
    public static void info(String msg, JFrame dialogParent) {
        info(msg);
        if (Main.isGUIEnabled()) {
            JOptionPane.showMessageDialog(dialogParent, msg, AppInfo.APP_NAME, JOptionPane.INFORMATION_MESSAGE);
        }
    }

    /**
     * Reports an info to the user.
     *
     * @param msg a message that will be printed to {@link System#err} if in driver mode or {@link System#out} if otherwise.
     */
    public static void info(String msg) {
        if (Main.getExecutionMode() == Main.ExecutionMode.DRIVER) {
            System.err.println(msg);

        } else {
            System.out.println(msg);
        }
    }

    /**
     * Retrieves a string from the application's resource bundle.
     *
     * @param key the key of the string to retrieve.
     * @return the string taken from the application's resource bundle.
     */
    public static String getStringResource(String key) {
        return STRINGS.getString(key);
    }

    /**
     * Opens the web browser.
     *
     * @param url          an URL to open.
     * @param dialogParent a parent for all the error dialogs. Can be {@code null}
     */
    public static void launchBrowser(String url, JFrame dialogParent) {
        Desktop desktop;
        if (Desktop.isDesktopSupported() && (desktop = Desktop.getDesktop()).isSupported(Desktop.Action.BROWSE)) {
            try {
                desktop.browse(new URI(url));
            } catch (Exception e) {
                Utils.err("Error while starting the web browser!", e, dialogParent);
            }

        } else if (COMPUTER_OS == OperatingSystem.Linux) {
            try {
                Runtime.getRuntime().exec("xdg-open " + url);

            } catch (IOException e) {
                Utils.err("Error while starting the web browser!", e, dialogParent);
            }

        } else {
            Utils.err("Error while starting the web browser!", dialogParent);
        }
    }

    /**
     * @return {@code true} if the application is running in debug mode.
     */
    public static boolean isDebugMode() {
        return java.lang.management.ManagementFactory.getRuntimeMXBean().getInputArguments().toString().contains("-agentlib:jdwp");
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
        INVALID_OPTIONS(10, "Invalid options!");

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

    /**
     * Enum that lists all the possible operating systems.
     *
     * @author marcocipriani01
     * @version 1.0
     */
    public enum OperatingSystem {
        Windows(false), MacOS(true), Linux(true), Other(false);

        public final boolean isUnix;

        OperatingSystem(boolean isUnix) {
            this.isUnix = isUnix;
        }
    }
}