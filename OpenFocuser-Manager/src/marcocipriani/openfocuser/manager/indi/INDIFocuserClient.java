package marcocipriani.openfocuser.manager.indi;

import laazotea.indi.client.*;

import java.io.IOException;
import java.util.Date;

public class INDIFocuserClient implements INDIServerConnectionListener, INDIPropertyListener, INDIDeviceListener {

    /**
     * The connection to the INDI server.
     */
    private INDIServerConnection connection;
    private INDIDevice device;

    public INDIFocuserClient(String host, int port) throws IOException {
        if (!isConnected()) {
            connection = new INDIServerConnection(host, port);
            connection.addINDIServerConnectionListener(this);
            connection.connect();
            connection.askForDevices();

        } else {
            throw new IllegalStateException("Already connected!");
        }
    }

    /**
     * @return the current state of this connection manager (connected or not).
     */
    public boolean isConnected() {
        return (connection != null) && (connection.isConnected());
    }

    @Override
    public void newProperty(INDIDevice device, INDIProperty property) {
        if (device.getName().equals(INDIArduinoDriver.DRIVER_NAME)) {
            property.addINDIPropertyListener(this);
        }
    }

    @Override
    public void removeProperty(INDIDevice device, INDIProperty property) {
        if (device.getName().equals(INDIArduinoDriver.DRIVER_NAME)) {
            property.removeINDIPropertyListener(this);
        }
    }

    @Override
    public void messageChanged(INDIDevice device) {

    }

    @Override
    public void propertyChanged(INDIProperty property) {

    }

    @Override
    public void newDevice(INDIServerConnection connection, INDIDevice device) {
        if (device.getName().equals(INDIArduinoDriver.DRIVER_NAME)) {
            this.device = device;
            device.addINDIDeviceListener(this);
        }
    }

    @Override
    public void removeDevice(INDIServerConnection connection, INDIDevice device) {
        if (device.getName().equals(INDIArduinoDriver.DRIVER_NAME)) {
            device.removeINDIDeviceListener(this);
        }
    }

    @Override
    public void connectionLost(INDIServerConnection connection) {
        device.removeINDIDeviceListener(this);
        connection = null;
    }

    @Override
    public void newMessage(INDIServerConnection connection, Date timestamp, String message) {

    }
}