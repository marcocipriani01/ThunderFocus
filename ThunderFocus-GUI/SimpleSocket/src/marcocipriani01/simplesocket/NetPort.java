package marcocipriani01.simplesocket;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.Socket;

/**
 * Network client or server base class.
 *
 * @author marcocipriani01
 * @version 1.0
 * @param <MessageType> the message type.
 * @see StringNetPort
 */
public abstract class NetPort<MessageType> {

    /**
     * Port.
     */
    protected final int port;
    /**
     * Connection state.
     */
    protected volatile boolean connected = false;

    /**
     * Class constructor. Initializes the port without attempting a connection.
     *
     * @param port the port.
     */
    public NetPort(int port) {
        this.port = port;
    }

    /**
     * Starts the connection to the client/server.
     */
    public final void connect() throws ConnectionException {
        if (connected) {
            throw new ConnectionException("Already connected!", ConnectionException.Type.ALREADY_CONNECTED);
        }
        new Thread(() -> {
            try {
                connect0();
            } catch (IOException e) {
                onError(new ConnectionException("Cannot connect the socket!", e, ConnectionException.Type.CONNECTION));
            }
        }, "Connection thread").start();
    }

    /**
     * Starts the connection to the client / server.
     */
    protected abstract void connect0() throws IOException;

    protected abstract void onError(Exception e);

    /**
     * Sends a message to the server or clients.
     *
     * @param msg the message to send.
     */
    public abstract void print(MessageType msg) throws ConnectionException;

    /**
     * Invoked when a new message arrives from the server / client.
     */
    protected abstract void onMessage(Socket from, MessageType msg);

    /**
     * Invoke it to start reading.
     */
    protected abstract void read(Socket from, BufferedReader in) throws IOException;

    /**
     * @return the current TCP port.
     */
    public int getPort() {
        return port;
    }

    /**
     * Closes the connection.
     */
    public final void close() throws ConnectionException {
        ensureConnection();
        Thread thread = new Thread(() -> {
            try {
                close0();
                connected = false;
            } catch (IOException e) {
                onError(new ConnectionException("Cannot disconnect the socket!", e, ConnectionException.Type.UNABLE_TO_DISCONNECT));
            }
        }, "NetPort connection thread");
        thread.start();
    }

    /**
     * Closes the connection.
     */
    protected abstract void close0() throws IOException;

    /**
     * Returns the connection state of the current client.
     *
     * @return the connection state, connected or not.
     */
    public final boolean isConnected() {
        return connected;
    }

    protected final void ensureConnection() throws ConnectionException {
        if (!connected) {
            throw new ConnectionException("Not connected!", ConnectionException.Type.NOT_CONNECTED);
        }
    }
}