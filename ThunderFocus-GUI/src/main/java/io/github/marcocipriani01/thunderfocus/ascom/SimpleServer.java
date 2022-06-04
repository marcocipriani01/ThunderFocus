package io.github.marcocipriani01.thunderfocus.ascom;

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
 * @version 1.1
 */
public abstract class SimpleServer {

    /**
     * List of client sockets.
     */
    protected final HashMap<Socket, PrintWriter> clients = new HashMap<>();
    /**
     * Port.
     */
    protected final int port;
    protected volatile ServerSocket serverSocket;
    /**
     * Connection state.
     */
    protected volatile boolean connected = false;

    /**
     * Class constructor. Initializes the client without attempting a connection.
     *
     * @param port the port of the new server.
     */
    public SimpleServer(int port) {
        this.port = port;
    }

    /**
     * Starts the connection to the client/server.
     */
    public synchronized void start() throws IOException {
        if (connected) throw new IllegalStateException("Already connected.");
        serverSocket = new ServerSocket(port);
        connected = true;
        new Thread(() -> {
            while (connected) {
                try {
                    Socket socket = serverSocket.accept();
                    if (!acceptClient(socket.getInetAddress())) {
                        socket.close();
                        continue;
                    }
                    PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                    clients.put(socket, out);
                    new Thread(() -> {
                        try {
                            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                            String inputLine;
                            while ((inputLine = in.readLine()) != null) {
                                onMessage(socket, inputLine);
                            }
                            closeClient(socket);
                        } catch (Exception e) {
                            closeClient(socket);
                        }
                    }, "Reader thread for " + socket).start();
                    onNewClient(socket);
                } catch (Exception ignored) {
                }
            }
        }, "Client listener thread").start();
    }

    private synchronized void closeClient(Socket socket) {
        try {
            socket.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        clients.remove(socket);
        onClientLost(socket);
    }

    /**
     * Closes the connection.
     */
    public synchronized void stop() throws IOException {
        if (!connected) throw new IllegalStateException("Not connected!");
        for (Socket s : clients.keySet()) {
            s.close();
        }
        serverSocket.close();
        clients.clear();
        connected = false;
    }

    /**
     * Sends a message to all the clients.
     *
     * @param msg the message to send.
     */
    public synchronized void println(final String msg) {
        if (!connected) throw new IllegalStateException("Not connected!");
        for (Socket s : clients.keySet()) {
            try {
                clients.get(s).println(msg);
            } catch (Exception ex) {
                closeClient(s);
            }
        }
    }

    /**
     * Sends a message to a client.
     *
     * @param msg the message to send.
     */
    public synchronized void println(final Socket client, final String msg) {
        if (!connected) throw new IllegalStateException("Not connected!");
        try {
            clients.get(client).println(msg);
        } catch (Exception ex) {
            closeClient(client);
        }
    }

    public final int getClientsCount() {
        return clients.size();
    }

    /**
     * @return the current TCP port.
     */
    public final int getPort() {
        return port;
    }

    /**
     * Returns the connection state of the current client.
     *
     * @return the connection state, connected or not.
     */
    public final boolean isConnected() {
        return connected;
    }

    /**
     * Invoked when a new message arrives from the clients.
     */
    protected abstract void onMessage(Socket from, String msg);

    protected abstract boolean acceptClient(InetAddress address);

    protected abstract void onNewClient(Socket client);

    protected abstract void onClientLost(Socket client);
}