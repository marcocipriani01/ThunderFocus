package marcocipriani01.thunder.focus;

import org.indilib.i4j.INDIException;
import org.indilib.i4j.protocol.api.INDIConnection;
import org.indilib.i4j.server.INDIServerAccessImpl;
import org.indilib.i4j.server.api.INDIClientInterface;
import org.indilib.i4j.server.api.INDIDeviceInterface;
import org.indilib.i4j.server.api.INDIServerEventHandler;
import org.indilib.i4j.server.api.INDIServerInterface;

public class INDIServerCreator extends INDIServerAccessImpl implements INDIServerEventHandler {

    @Override
    public INDIServerInterface createOrGet(String host, Integer port) {
        INDIServerInterface old = get();
        INDIServerInterface server = super.createOrGet(host, port);
        if (server != old) {
            driverAndListener(server);
        }
        return server;
    }

    public void start(int port, boolean forceRestart) {
        if (forceRestart) {
            if (isRunning()) {
                get().stopServer();
            }
            driverAndListener(super.createOrGet(null, port));
        } else {
            createOrGet(null, port);
        }
    }

    private void driverAndListener(INDIServerInterface server) {
        server.addEventHandler(this);
        try {
            server.loadJavaDriver(INDIThunderFocuserDriver.class);
        } catch (INDIException e) {
            e.printStackTrace();
        }
    }

    public boolean isRunning() {
        INDIServerInterface server = get();
        return server != null && server.isServerRunning();
    }

    public void stop() {
        if (isRunning()) {
            get().stopServer();
        }
    }

    @Override
    public boolean acceptClient(INDIConnection clientSocket) {
        return true;
    }

    @Override
    public void connectionWithClientBroken(INDIClientInterface client) {
        System.out.println("Client " + client.getInetAddress() + "disconnected.");
    }

    @Override
    public void connectionWithClientEstablished(INDIClientInterface client) {
        System.out.println("Client " + client.getInetAddress() + "connected.");
    }

    @Override
    public void driverDisconnected(INDIDeviceInterface device) {
        System.out.println("Driver " + device.getDeviceIdentifier() + "disconnected.");
    }
}