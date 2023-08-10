package io.github.marcocipriani01.thunderfocus.serial;

import io.github.marcocipriani01.thunderfocus.Main;
import jssc.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * Simple serial port manager with listeners.
 *
 * @author marcocipriani01
 * @version 1.3
 * @see <a href="https://github.com/scream3r/java-simple-serial-connector">jSSC on GitHub</a>
 */
@SuppressWarnings("unused")
public class SerialPortImpl implements SerialPortEventListener {

    /**
     * List of all the listeners.
     */
    protected final ArrayList<SerialMessageListener> listeners = new ArrayList<>();
    /**
     * An instance of the {@link SerialPort} class.
     */
    protected volatile SerialPort serialPort = null;
    private String buf = "";

    /**
     * Class constructor.
     */
    public SerialPortImpl() {

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
     * Connects a board to this object.
     *
     * @param port the port.
     * @param rate the baud rate.
     */
    public void connect(String port, int rate) throws SerialPortException {
        if (isConnected()) throw new IllegalStateException("Already connected.");
        serialPort = new SerialPort(port);
        serialPort.openPort();
        serialPort.setParams(rate, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE, true, true);
        serialPort.setEventsMask(SerialPort.MASK_RXCHAR);
        serialPort.addEventListener(this);
    }

    /**
     * Connects a board to this object (at the default rate of 115200).
     *
     * @param port the port.
     */
    public void connect(String port) throws SerialPortException {
        connect(port, SerialPort.BAUDRATE_115200);
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
     * Disconnects from the Serial Port and clears the listeners list.
     *
     * @see #connect
     */
    public void disconnect() throws IOException {
        if (serialPort == null) return;
        try {
            serialPort.closePort();
            serialPort = null;
        } catch (SerialPortException e) {
            serialPort = null;
            throw new IOException("Could not disconnect!", e);
        }
    }

    /**
     * Prints a {@code String} to the connected board.
     *
     * @param message the message you want to send.
     */
    public void print(String message) throws SerialPortException, IOException {
        if (!isConnected())
            throw new IllegalStateException("Not connected.");
        if (!serialPort.writeBytes(message.getBytes()))
            throw new IOException("Could not write serial message.");
    }

    /**
     * Prints a {@code String} to the connected board.
     *
     * @param message the message you want to send.
     */
    public void println(String message) throws SerialPortException, IOException {
        print(message + "\n");
    }

    /**
     * Returns the name of the serial port currently being used.
     *
     * @return the serial port's name.
     */
    public String getSerialPortName() {
        return serialPort.getPortName();
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
                        for (SerialMessageListener l : listeners) {
                            l.onSerialMessage(split[i]);
                        }
                    }
                }
                buf = b ? "" : split[split.length - 1];
            }
        } catch (SerialPortException e) {
            for (SerialMessageListener l : listeners) {
                l.onSerialError(e);
            }
        }
    }
}