package marcocipriani.openfocuser.manager.io;

import marcocipriani.openfocuser.manager.Main;

/**
 * Serial port multiplexer/duplicator.
 *
 * @author marcocipriani01
 * @version 1.2
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public abstract class SerialPortMultiplexer {

    /**
     * The mocked serial port.
     */
    protected SerialPortImpl mockedSerialPort;
    protected Forwarder mockedSerialPortListener;
    /**
     * The real serial port.
     */
    protected SerialPortImpl realSerialPort;
    protected Forwarder realSerialPortLister;

    /**
     * Class constructor.
     *
     * @param realSerialPort a real serial port to duplicate.
     */
    public SerialPortMultiplexer(SerialPortImpl realSerialPort) {

    }

    /**
     * @return an OS-compatible serial port multiplexer, or {@code null} if there isn't any.
     */
    public static SerialPortMultiplexer getSystemCompatibleMultiplexer(SerialPortImpl realSerialPort) {
        if (Main.COMPUTER_OS.isUnix) {
            return new UnixSerialPortMultiplexer(realSerialPort);

        } else if (Main.COMPUTER_OS == Main.OperatingSystem.Windows) {
            //TODO: implement serial port multiplexing in Windows
            throw new UnsupportedOperationException("Serial port multiplexing is not currently supported in Windows!");
            //return new WindowsSerialPortMultiplexer(realSerialPort);

        } else {
            throw new UnsupportedOperationException("Unsupported OS!");
        }
    }

    /**
     * Stops everything.
     */
    public void stop() {
        realSerialPort.removeListener(realSerialPortLister);
        mockedSerialPort.removeListener(mockedSerialPortListener);
        mockedSerialPort.disconnect();
    }

    /**
     * @return the port to be used for another connected.
     */
    public abstract String getMockedPort();

    /**
     * Sends the messages that receives from the real serial port to the mocked one and vice-versa.
     *
     * @author marcocipriani01
     * @version 1.0
     */
    protected class Forwarder implements SerialMessageListener {

        private SerialPortImpl forwardTo;

        /**
         * Class constructor.
         */
        Forwarder(SerialPortImpl forwardTo) {
            this.forwardTo = forwardTo;
        }

        /**
         * Called when a new message is received from the serial port.
         *
         * @param msg the received message.
         */
        @Override
        public void onPortMessage(String msg) {
            forwardTo.print(msg);
        }

        /**
         * Called when an error occurred while communicating with the serial port.
         *
         * @param e the {@code Exception}.
         */
        @Override
        public void onPortError(Exception e) {
            e.printStackTrace();
        }
    }
}