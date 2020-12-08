package marcocipriani01.simplesocket;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * marcocipriani01.simplesocket.Client Socket for a local LAN marcocipriani01.simplesocket.Server Socket with event handling.
 *
 * @author marcocipriani01
 * @version 1.0
 */
public abstract class SimpleClient extends StringNetPort {

    /**
     * The server IP.
     */
    private final String ip;
    private Socket socket;
    /**
     * Output.
     */
    private PrintWriter out;

    /**
     * Class constructor. Initializes the client without attempting a connection.
     *
     * @param address the IP address of your marcocipriani01.simplesocket.Server Socket.
     * @param port    the port of your server.
     */
    public SimpleClient(String address, int port) {
        super(port);
        ip = address;
    }

    /**
     * Starts the socket and the connection.
     */
    @Override
    protected void connect0() throws IOException {
        socket = new Socket(ip, port);
        out = new PrintWriter(socket.getOutputStream(), true);
        startReading(socket, new BufferedReader(new InputStreamReader(socket.getInputStream())));
        connected = true;
    }

    /**
     * @return the server ip.
     */
    public String getIp() {
        return ip;
    }

    /**
     * Invoked when a new message arrives from the server.
     */
    @Override
    public abstract void onMessage(Socket from, String msg);

    /**
     * Sends a message to the server.
     *
     * @param msg the message you want to send.
     * @throws ConnectionException if the client is not connected.
     */
    @Override
    public void print(String msg) throws ConnectionException {
        ensureConnection();
        new Thread(() -> out.println(msg), "Socket write").start();
    }

    @Override
    public void println(String msg) throws ConnectionException {
        ensureConnection();
        new Thread(() -> out.println(msg), "Socket write").start();
    }

    @Override
    public void println(int msg) throws ConnectionException {
        ensureConnection();
        new Thread(() -> out.println(msg), "Socket write").start();
    }

    @Override
    public void print(int msg) throws ConnectionException {
        ensureConnection();
        new Thread(() -> out.print(msg), "Socket write").start();
    }

    @Override
    public void println(boolean msg) throws ConnectionException {
        ensureConnection();
        new Thread(() -> out.println(msg), "Socket write").start();
    }

    @Override
    public void print(boolean msg) throws ConnectionException {
        ensureConnection();
        new Thread(() -> out.print(msg), "Socket write").start();
    }

    /**
     * Closes the connection.
     */
    @Override
    protected void close0() throws IOException {
        socket.close();
    }
}