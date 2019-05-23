package marcocipriani.openfocuser.manager.io;

import marcocipriani.openfocuser.manager.Main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Serial port multiplexer/duplicator for Linux/Unix that uses socat (via {@link SocatRunner}).
 *
 * @author marcocipriani01
 * @version 1.1
 */
@SuppressWarnings("unused")
public class UnixSerialPortMultiplexer extends SerialPortMultiplexer {

    /**
     * Socat.
     */
    private SocatRunner socat;

    /**
     * Class constructor.
     *
     * @param realSerialPort a real serial port to duplicate.
     */
    public UnixSerialPortMultiplexer(SerialPortImpl realSerialPort) {
        super(realSerialPort);
        socat = new SocatRunner();
        new Thread(socat).start();
        // Wait for process to start
        try {
            long startTime = System.currentTimeMillis();
            while (socat.isNotReady()) {
                Thread.sleep(50);
                if (System.currentTimeMillis() - startTime >= 1000) {
                    throw new ConnectionException("Unable to start socat!", ConnectionException.Type.TIMEOUT);
                }
            }

        } catch (InterruptedException e) {
            if (Main.isVerboseMode()) {
                e.printStackTrace();
            }
        }
        mockedSerialPort = new SerialPortImpl(socat.getPort1());
        this.realSerialPort = realSerialPort;
        mockedSerialPortListener = new Forwarder(this.realSerialPort);
        mockedSerialPort.addListener(mockedSerialPortListener);
        realSerialPortLister = new Forwarder(mockedSerialPort);
        this.realSerialPort.addListener(realSerialPortLister);
    }

    /**
     * Stops everything.
     */
    @Override
    public void stop() {
        super.stop();
        socat.stop();
    }

    /**
     * @return the port to be used for another connected.
     */
    @Override
    public String getMockedPort() {
        return socat.getPort2();
    }

    /**
     * Utility that starts socat to create two virtual raw PTYs (pseudoterminals) or virtual serial ports.
     *
     * @author marcocipriani01
     * @version 1.0
     */
    private static class SocatRunner implements Runnable {

        /**
         * The socat process.
         */
        private Process process;
        /**
         * Port 1.
         */
        private String port1;
        /**
         * Port 2.
         */
        private String port2;

        @Override
        public void run() {
            try {
                if (!System.getProperty("os.name").toLowerCase().equals("linux")) {
                    UnsupportedOperationException e = new UnsupportedOperationException("Only Linux is supported by socat!");
                    Main.err(e.getMessage());
                    throw e;
                }
                ProcessBuilder processBuilder = new ProcessBuilder("socat",
                        "-d", "-d", "pty,raw,echo=0", "pty,raw,echo=0");
                processBuilder.redirectErrorStream(true);
                process = processBuilder.start();
                BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String line;
                while ((line = in.readLine()) != null) {
                    if (line.contains("] ")) {
                        line = line.substring(line.indexOf("] ") + 2);
                        Main.err("socat says: " + line);
                        if (line.startsWith("N PTY is ")) {
                            line = line.replace("N PTY is ", "");
                            if (port1 == null) {
                                port1 = line;

                            } else if (port2 == null) {
                                port2 = line;
                                break;
                            }
                        }
                    }
                }
                process.waitFor();
                in.close();

            } catch (SecurityException | UnsupportedOperationException | InterruptedException | IOException e) {
                Main.err("Error occurred while launching socat: " + e.getMessage(), e, false);
                Main.exit(Main.ExitCodes.SOCAT_ERROR);
            }
        }

        /**
         * @return {@code true} if both the ports have been initialized.
         */
        boolean isNotReady() {
            return (port1 == null) || (port2 == null);
        }

        /**
         * Stops the process.
         */
        void stop() {
            process.destroy();
            Main.err("socat stopped");
        }

        /**
         * @return the socat process.
         */
        Process getProcess() {
            return process;
        }

        /**
         * @return the first port.
         */
        String getPort1() {
            return port1;
        }

        /**
         * @return the second port.
         */
        String getPort2() {
            return port2;
        }
    }
}