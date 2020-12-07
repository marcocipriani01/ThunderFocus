package marcocipriani01.simplesocket;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.Socket;

/**
 * Implementation of the {@link NetPort} class to send and receive strings.
 *
 * @author marcocipriani01
 * @version 1.0
 * @see NetPort
 * @see SimpleServer
 * @see SimpleClient
 */
public abstract class StringNetPort extends NetPort<String> {

    /**
     * Class constructor. Initializes the port without attempting a connection.
     *
     * @param port the port.
     */
    public StringNetPort(int port) {
        super(port);
    }

    /**
     * Sends a message to all the clients.
     *
     * @param msg the message to send.
     */
    public abstract void println(String msg) throws ConnectionException;

    /**
     * Sends a message to all the clients.
     *
     * @param msg the message to send.
     */
    public abstract void println(int msg) throws ConnectionException;

    /**
     * Sends a message to all the clients.
     *
     * @param msg the message to send.
     */
    @Override
    public abstract void print(String msg) throws ConnectionException;

    /**
     * Sends a message to all the clients.
     *
     * @param msg the message to send.
     */
    public abstract void print(int msg) throws ConnectionException;

    /**
     * Sends a message to all the clients.
     *
     * @param msg the message to send.
     */
    public abstract void print(boolean msg) throws ConnectionException;

    /**
     * Sends a message to all the clients.
     *
     * @param msg the message to send.
     */
    public abstract void println(boolean msg) throws ConnectionException;

    protected void startReading(Socket from, BufferedReader in) {
        new Thread(() -> {
            try {
                read(from, in);
            } catch (IOException e) {
                onError(new ConnectionException("Reading error!", e, ConnectionException.Type.INPUT));
            }
        }, "Reading thread").start();
    }

    /**
     * Invoke it to start reading.
     */
    @Override
    protected void read(Socket from, BufferedReader in) throws IOException {
        String inputLine;
        while ((inputLine = in.readLine()) != null) {
            onMessage(from, inputLine);
        }
    }
}