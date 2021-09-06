package io.github.marcocipriani01.simplesocket;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

/**
 * Server Socket for a local LAN server with event handling.
 *
 * @author marcocipriani01
 * @version 1.0
 */
public abstract class SimpleServer extends StringNetPort {

    /**
     * List of client sockets.
     */
    protected final HashMap<Socket, PrintWriter> clients = new HashMap<>();
    protected ServerSocket serverSocket;

    /**
     * Class constructor. Initializes the client without attempting a connection.
     *
     * @param port the port of the new server.
     */
    public SimpleServer(int port) {
        super(port);
    }

    /**
     * Starts the socket and the connection.
     */
    @Override
    protected void connect0() throws IOException {
        serverSocket = new ServerSocket(port);
        connected = true;
        while (connected) {
            try {
                Socket socket = serverSocket.accept();
                if (!acceptClient(socket.getInetAddress())) {
                    socket.close();
                    continue;
                }
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                clients.put(socket, out);
                startReading(socket, new BufferedReader(new InputStreamReader(socket.getInputStream())));
                onNewClient(socket);
            } catch (Exception ignored) {
            }
        }
    }

    /**
     * Sends a message to all the clients.
     *
     * @param msg the message to send.
     */
    @Override
    public void println(String msg) throws ConnectionException {
        ensureConnection();
        new Thread(() -> {
            for (Socket s : clients.keySet()) {
                clients.get(s).println(msg);
            }
        }, "Socket write").start();
    }

    @Override
    public void println(int msg) throws ConnectionException {
        ensureConnection();
        new Thread(() -> {
            for (Socket s : clients.keySet()) {
                clients.get(s).println(msg);
            }
        }, "Socket write").start();
    }

    @Override
    public void print(int msg) throws ConnectionException {
        ensureConnection();
        new Thread(() -> {
            for (Socket s : clients.keySet()) {
                clients.get(s).print(msg);
            }
        }, "Socket write").start();
    }

    @Override
    public void println(boolean msg) throws ConnectionException {
        ensureConnection();
        new Thread(() -> {
            for (Socket s : clients.keySet()) {
                clients.get(s).println(msg);
            }
        }, "Socket write").start();
    }

    @Override
    public void print(boolean msg) throws ConnectionException {
        ensureConnection();
        new Thread(() -> {
            for (Socket s : clients.keySet()) {
                clients.get(s).print(msg);
            }
        }, "Socket write").start();
    }

    /**
     * Sends a message to all the clients.
     *
     * @param msg the message to send.
     */
    @Override
    public void print(String msg) throws ConnectionException {
        ensureConnection();
        new Thread(() -> {
            for (Socket s : clients.keySet()) {
                clients.get(s).print(msg);
            }
        }, "Socket write").start();
    }

    /**
     * Sends a message to a client.
     *
     * @param msg the message to send.
     */
    public void println(Socket client, String msg) throws ConnectionException {
        ensureConnection();
        new Thread(() -> clients.get(client).println(msg), "Socket write").start();
    }

    /**
     * Sends a message to a client.
     *
     * @param msg the message to send.
     */
    public void print(Socket client, String msg) throws ConnectionException {
        ensureConnection();
        new Thread(() -> clients.get(client).print(msg), "Socket write").start();
    }

    public void println(Socket client, int msg) throws ConnectionException {
        ensureConnection();
        new Thread(() -> clients.get(client).println(msg), "Socket write").start();
    }

    public void print(Socket client, int msg) throws ConnectionException {
        ensureConnection();
        new Thread(() -> clients.get(client).print(msg), "Socket write").start();
    }

    public void println(Socket client, boolean msg) throws ConnectionException {
        ensureConnection();
        new Thread(() -> clients.get(client).println(msg), "Socket write").start();
    }

    public void print(Socket client, boolean msg) throws ConnectionException {
        ensureConnection();
        new Thread(() -> clients.get(client).print(msg), "Socket write").start();
    }

    @Override
    protected void startReading(Socket from, BufferedReader in) {
        new Thread(() -> read(from, in), "Reading thread").start();
    }

    @Override
    protected void read(Socket from, BufferedReader in) {
        try {
            super.read(from, in);
        } catch (IOException e) {
            try {
                from.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            clients.remove(from);
            onClientRemoved(from);
        }
    }

    /**
     * Closes the connection.
     */
    @Override
    protected void close0() throws IOException {
        for (Socket s : clients.keySet()) {
            s.close();
        }
        serverSocket.close();
        clients.clear();
    }

    /**
     * Invoked when a new message arrives from the clients.
     */
    @Override
    protected abstract void onMessage(Socket from, String msg);

    protected abstract boolean acceptClient(InetAddress address);

    protected abstract void onNewClient(Socket client);

    protected abstract void onClientRemoved(Socket client);

    public int getClientsCount() {
        return clients.size();
    }
}