package marcocipriani.openfocuser.manager.indi;

import laazotea.indi.INDIException;
import laazotea.indi.driver.INDIDriver;
import laazotea.indi.server.DefaultINDIServer;
import laazotea.indi.server.INDIClient;
import marcocipriani.openfocuser.manager.Main;
import marcocipriani.openfocuser.manager.io.ConnectionException;

import java.net.Socket;
import java.util.Arrays;

/**
 * A simple INDI Server that basically sends all messages from drivers and
 * clients and vice versa, just performing basic checks of messages integrity.
 * It allows to dynamically load / unload Java and native drivers.
 *
 * @author S. Alonso (Zerjillo) [zerjioi at ugr.es]
 * @author marcocipriani01
 * @version 2.0
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class INDIServer extends DefaultINDIServer {

    /**
     * Class constructor.
     */
    public INDIServer() {
        super();
    }

    /**
     * Class constructor.
     *
     * @param port The port to which the server will listen.
     */
    public INDIServer(int port) {
        super(port);
    }

    /**
     * Loads a Java driver from a JAR file
     *
     * @param jar The JAR file
     * @see #unloadJar
     * @see #reloadJar(String)
     */
    public void loadJar(String jar) {
        try {
            loadJavaDriversFromJAR(jar);

        } catch (INDIException e) {
            throw new ConnectionException("Error during driver loading!", ConnectionException.Type.IO);
        }
    }

    /**
     * Unloads a Java driver from its JAR file.
     *
     * @param jar The JAR file.
     * @see #loadJar
     * @see #reloadJar(String)
     */
    public void unloadJar(String jar) {
        destroyJavaDriversFromJAR(jar);
    }

    /**
     * Reloads a Java driver from its JAR file.
     *
     * @param jar the JAR file.
     * @see #loadJar(String)
     * @see #unloadJar(String)
     */
    public void reloadJar(String jar) {
        unloadJar(jar);
        try {
            Thread.sleep(100);
            while (isAlreadyLoaded(jar)) {
                Thread.sleep(100);
            }

        } catch (InterruptedException ignored) {

        }
        loadJar(jar);
    }

    /**
     * Loads a Java driver from its class.
     *
     * @param driver a Java driver.
     */
    public void loadJava(Class<? extends INDIDriver> driver) {
        try {
            loadJavaDriver(driver);

        } catch (INDIException e) {
            throw new ConnectionException("Error during driver loading!", ConnectionException.Type.IO);
        }
    }

    /**
     * Unloads a Java driver from its class.
     *
     * @param driver a Java driver.
     */
    public void unloadJava(Class<? extends INDIDriver> driver) {
        destroyJavaDriver(driver);
    }

    /**
     * Loads a native driver.
     *
     * @param path the path of the driver.
     * @see #unloadNative(String)
     * @see #reloadNative(String)
     */
    public void loadNative(String path) {
        try {
            loadNativeDriver(path);

        } catch (INDIException e) {
            throw new ConnectionException("Error during driver loading!", ConnectionException.Type.IO);
        }
    }

    /**
     * Unloads a native driver.
     *
     * @param path the path of the driver.
     * @see #loadNative
     * @see #reloadNative(String)
     */
    public void unloadNative(String path) {
        destroyNativeDriver(path);
    }

    /**
     * Reloads a native driver from its path.
     *
     * @param name the name of the driver.
     * @see #loadNative(String)
     * @see #unloadNative(String)
     */
    public void reloadNative(String name) {
        unloadNative(name);
        try {
            Thread.sleep(100);
            while (isAlreadyLoaded(name)) {
                Thread.sleep(100);
            }

        } catch (InterruptedException ignored) {

        }
        unloadNative(name);
    }

    /**
     * Starts the listening thread.
     */
    @Override
    public void startListeningToClients() {
        if (isServerRunning()) {
            throw new ConnectionException("Server already started!", ConnectionException.Type.ALREADY_STARTED);

        } else {
            super.startListeningToClients();
        }
    }

    /**
     * Gets if the server is listening for new clients to connect.
     *
     * @return {@code true} if the server is listening for new clients.
     */
    @Override
    public boolean isServerRunning() {
        return super.isServerRunning();
    }

    /**
     * Stops the server from listening new clients.
     * All connections with existing clients are also broken.
     */
    @Override
    public void stopServer() {
        if (isServerRunning()) {
            super.stopServer();

        } else {
            throw new ConnectionException("Server not started!", ConnectionException.Type.NOT_STARTED);
        }
    }

    /**
     * Connects to another server.
     *
     * @param host The host of the other server.
     * @param port The port of the other server.
     * @see #disconnect
     */
    public void connect(String host, int port) {
        try {
            loadNetworkDriver(host, port);

        } catch (INDIException e) {
            throw new ConnectionException("Unable to connect to remove server!", e, ConnectionException.Type.CONNECTION);
        }
    }

    /**
     * Disconnects from another Server.
     *
     * @param host The host of the other server.
     * @param port The port of the other server.
     * @see #connect
     */
    public void disconnect(String host, int port) {
        destroyNetworkDriver(host, port);
    }

    /**
     * Prints a message about the broken connection to the standard err.
     *
     * @param client The Client whose connection has been broken
     */
    @Override
    protected void connectionWithClientBroken(INDIClient client) {
        Main.err("Connection with client " + client.getInetAddress() + " has been broken.");
    }

    /**
     * Prints a message about the established connection to the standard err.
     *
     * @param client The Client whose connection has been established
     */
    @Override
    protected void connectionWithClientEstablished(INDIClient client) {
        Main.err("Connection with client " + client.getInetAddress() + " established.");
    }

    /**
     * Prints a message about the driver which has been disconnected.
     *
     * @param driverIdentifier the driver identifier.
     * @param deviceNames      its devices.
     */
    @Override
    protected void driverDisconnected(String driverIdentifier, String[] deviceNames) {
        Main.err("Driver " + driverIdentifier + " has been disconnected. " +
                "The following devices have disappeared: " + Arrays.toString(deviceNames)
                .replace("[", "").replace("]", ""));
    }

    /**
     * Accepts all the clients.
     *
     * @param socket a client
     * @return {@code true}
     */
    @Override
    protected boolean acceptClient(Socket socket) {
        return true;
    }
}