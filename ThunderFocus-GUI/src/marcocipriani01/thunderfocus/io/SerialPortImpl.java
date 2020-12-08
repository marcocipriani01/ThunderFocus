package marcocipriani01.thunderfocus.io;

import jssc.*;
import marcocipriani01.simplesocket.ConnectionException;
import marcocipriani01.thunderfocus.Main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Locale;

/**
 * Simple serial port manager with listeners.
 * Provides a simple way to connect your board, to send and to receive data and to get a list containing all the available ports.
 * For each error, this class will use the {@link ConnectionException} class to give you a better explanation of the error
 * (see {@link ConnectionException#getType()}, {@link ConnectionException#getCause()} and {@link ConnectionException#getMessage()}).
 *
 * @author marcocipriani01
 * @version 1.2
 * @see <a href="https://github.com/scream3r/java-simple-serial-connector">jSSC on GitHub</a>
 */
@SuppressWarnings("unused")
public class SerialPortImpl implements SerialPortEventListener {

    /**
     * List of all the listeners.
     */
    protected ArrayList<SerialMessageListener> listeners = new ArrayList<>();
    /**
     * An instance of the {@link SerialPort} class.
     */
    protected SerialPort serialPort;
    private String buf = "";

    /**
     * Class constructor.
     */
    public SerialPortImpl() {

    }

    /**
     * Class constructor. Initializes the serial port and starts a connection.
     *
     * @param port the port of your board.
     */
    public SerialPortImpl(String port) throws
            ConnectionException {
        connect(port);
    }

    /**
     * Class constructor. Initializes the serial port and starts a connection.
     *
     * @param port the port of your board.
     * @param rate the baud rate.
     */
    public SerialPortImpl(String port, int rate) throws ConnectionException {
        connect(port, rate);
    }

    /**
     * Serial ports discovery.
     *
     * @return an array containing all the available and not busy ports.
     */
    public static String[] scanSerialPorts() {
        if (Main.OPERATING_SYSTEM == Main.OperatingSystem.MACOS) {
            try {
                Process process = new ProcessBuilder("ls", "-1", "/dev/tty.*").start();
                BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String line;
                ArrayList<String> devs = new ArrayList<>();
                while ((line = in.readLine()) != null) {
                    devs.add(line);
                }
                try {
                    process.waitFor();
                } catch (InterruptedException ignored) {
                }
                return devs.toArray(new String[0]);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return SerialPortList.getPortNames();
    }

    /**
     * Returns the actual state of the board: connected or disconnected.
     *
     * @return {@code true} if the board is connected, {@code false} if otherwise.
     */
    public boolean isConnected() {
        return (serialPort != null) && serialPort.isOpened();
    }

    /**
     * Adds a listener to the list.
     *
     * @param listener a listener to add.
     */
    public void addListener(SerialMessageListener listener) {
        if (listeners.contains(listener)) {
            throw new IllegalArgumentException("Listener already in the list!");
        }
        listeners.add(listener);
    }

    /**
     * Removes a listener from the list.
     *
     * @param listener a listener to remove.
     */
    public void removeListener(SerialMessageListener listener) {
        listeners.remove(listener);
    }

    /**
     * Connects an board to this object.
     *
     * @param port the port.
     * @param rate the baud rate.
     */
    public void connect(String port, int rate) throws ConnectionException {
        if (isConnected()) {
            throw new ConnectionException(ConnectionException.Type.ALREADY_CONNECTED);
        }
        serialPort = new SerialPort(port);
        try {
            serialPort.openPort();
            serialPort.setParams(rate, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE, false, false);
            serialPort.setEventsMask(SerialPort.MASK_RXCHAR);
            serialPort.addEventListener(this);

        } catch (SerialPortException e) {
            ConnectionException.Type type;
            switch (e.getExceptionType()) {
                case SerialPortException.TYPE_PORT_BUSY, SerialPortException.TYPE_PORT_ALREADY_OPENED -> type = ConnectionException.Type.PORT_BUSY;
                case SerialPortException.TYPE_PORT_NOT_FOUND -> type = ConnectionException.Type.PORT_NOT_FOUND;
                default -> type = ConnectionException.Type.UNKNOWN;
            }
            throw new ConnectionException("An error occurred during connection!", e, type);
        }
    }

    /**
     * Connects a board to this object (at the default rate of 115200).
     *
     * @param port the port.
     */
    public void connect(String port) throws ConnectionException {
        connect(port, SerialPort.BAUDRATE_115200);
    }

    /**
     * Disconnects from the Serial Port and clears the listeners list.
     *
     * @see #connect
     */
    public void disconnect() throws ConnectionException {
        try {
            if (!serialPort.closePort()) {
                throw new ConnectionException("Something went wrong during the disconnection!", ConnectionException.Type.UNABLE_TO_DISCONNECT);
            }

        } catch (SerialPortException e) {
            throw new ConnectionException("Something went wrong during the disconnection!", e, ConnectionException.Type.UNABLE_TO_DISCONNECT);
        }
    }

    /**
     * Sends an error event to the listeners.
     *
     * @param e the exception to notify.
     */
    protected void notifyError(Exception e) {
        for (SerialMessageListener l : listeners) {
            l.onPortError(e);
        }
    }

    /**
     * Prints a {@code String} to the connected board.
     *
     * @param message the message you want to send.
     */
    public void print(String message) throws ConnectionException {
        if (isConnected()) {
            try {
                if (!serialPort.writeBytes(message.getBytes())) {
                    notifyError(new ConnectionException("An error occurred while sending the message!", ConnectionException.Type.OUTPUT));
                }
            } catch (SerialPortException e) {
                ConnectionException.Type type;
                switch (e.getExceptionType()) {
                    case SerialPortException.TYPE_PORT_BUSY -> type = ConnectionException.Type.BUSY;
                    case SerialPortException.TYPE_PORT_NOT_OPENED -> type = ConnectionException.Type.NOT_CONNECTED;
                    default -> type = ConnectionException.Type.UNKNOWN;
                }
                throw new ConnectionException("An error occurred during data transfer!", e, type);
            }
        } else {
            throw new ConnectionException(ConnectionException.Type.NOT_CONNECTED);
        }
    }

    /**
     * Prints an {@code int} to the board.
     *
     * @param number the message you want to send.
     */
    public void print(int number) throws ConnectionException {
        print(String.valueOf(number));
    }

    /**
     * Prints a character to the board.
     *
     * @param c the char you want to send.
     */
    public void print(char c) throws ConnectionException {
        print(String.valueOf(c));
    }

    /**
     * Prints a {@code double} to the board.
     *
     * @param d the number you want to send.
     */
    public void print(double d) throws ConnectionException {
        print(String.valueOf(d));
    }

    /**
     * Prints a {@code String} to the connected board.
     *
     * @param message the message you want to send.
     */
    public void println(String message) throws ConnectionException {
        print(message + "\n");
    }

    /**
     * Prints an {@code int} to the board.
     *
     * @param number the message you want to send.
     */
    public void println(int number) throws ConnectionException {
        println(String.valueOf(number));
    }

    /**
     * Prints a character to the board.
     *
     * @param c the char you want to send.
     */
    public void println(char c) throws ConnectionException {
        println(String.valueOf(c));
    }

    /**
     * Prints a {@code double} to the board.
     *
     * @param d the number you want to send.
     */
    public void println(double d) throws ConnectionException {
        println(String.valueOf(d));
    }

    /**
     * Returns the name of the serial port currently being used..
     *
     * @return the serial port's name.
     */
    public String getSerialPortName() {
        return serialPort.getPortName();
    }

    /**
     * @return a String representation of this object.
     */
    @Override
    public String toString() {
        return "SerialPort[" + (isConnected() ? (serialPort.getPortName()) : "false") + "]";
    }

    /**
     * Serial event. Receives data from the connected board.
     *
     * @param portEvent the port event.
     */
    @Override
    public void serialEvent(SerialPortEvent portEvent) {
        try {
            String in = serialPort.readString();
            if (in != null && !in.equals("")) {
                String[] split = (buf + in.replace("\r", "")).split("\n", 0);
                boolean b = in.endsWith("\n");
                for (int i = 0; i < (b ? split.length : (split.length - 1)); i++) {
                    split[i] = split[i].trim();
                    if (split[i] != null && !split[i].equals("")) {
                        notifyListener(split[i]);
                    }
                }
                buf = b ? "" : split[split.length - 1];
            }

        } catch (SerialPortException e) {
            notifyError(new ConnectionException("An error occurred while receiving data from the serial port!",
                    e, ConnectionException.Type.INPUT));
        }
    }

    /**
     * Sends a message to all the registered serial message listeners.
     *
     * @param msg the message.
     */
    protected void notifyListener(String msg) {
        for (SerialMessageListener l : listeners) {
            l.onPortMessage(msg);
        }
    }
}