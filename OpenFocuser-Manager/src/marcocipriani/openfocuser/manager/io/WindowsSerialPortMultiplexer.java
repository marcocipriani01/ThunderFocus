package marcocipriani.openfocuser.manager.io;

/**
 * Serial port multiplexer/duplicator for Windows, using JNI.
 *
 * @author marcocipriani01
 * @version 1.1
 */
@SuppressWarnings("unused")
public class WindowsSerialPortMultiplexer extends SerialPortMultiplexer {

    /**
     * Port 2.
     */
    private String port2;

    /**
     * Class constructor.
     *
     * @param realSerialPort a real serial port to duplicate.
     */
    public WindowsSerialPortMultiplexer(SerialPortImpl realSerialPort) {
        super(realSerialPort);
        String[] ports = createVirtualPorts0();
        if (ports.length != 2) {
            //TODO(marcocipriani01): handle exception
            throw new IllegalStateException();
        }
        mockedSerialPort = new SerialPortImpl(ports[0]);
        port2 = ports[1];
        this.realSerialPort = realSerialPort;
        mockedSerialPortListener = new Forwarder(this.realSerialPort);
        mockedSerialPort.addListener(mockedSerialPortListener);
        realSerialPortLister = new Forwarder(mockedSerialPort);
        this.realSerialPort.addListener(realSerialPortLister);
    }

    private native String[] createVirtualPorts0();

    /**
     * Stops everything.
     */
    @Override
    public void stop() {
        super.stop();
        stop0();
    }

    private native void stop0();

    /**
     * @return the port to be used for another connected.
     */
    @Override
    public String getMockedPort() {
        return port2;
    }
}