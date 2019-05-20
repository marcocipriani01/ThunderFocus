package marcocipriani.openfocuser.manager.fwconf;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;

/**
 * AVR Hex firmware uploader/updater utility. Windows and Linux.
 *
 * @author marcocipriani01
 * @version 1.0
 */
@SuppressWarnings("WeakerAccess")
public class Updater {

    private ArrayList<Board> boards;
    private ArrayList<Firmware> firmwares;
    private String avrdude;
    private String avrdudeConfig;
    private String tmpDir;
    private AvrdudeListener listener;

    /**
     * Class constructor.
     * Loads the available firmwares and boards, chooses the right AVR executable depending on the OS,
     * creates the temp folder and copies in it the avrdude config file.
     *
     * @param listener avrdude listener.
     * @throws IOException                   if resources in the app jar are not found.
     * @throws IllegalStateException         if the system temp folder can't be initialized.
     * @throws UnsupportedOperationException if the current OS doesn't support avrdude
     */
    public Updater(AvrdudeListener listener) throws IOException {
        InputStream fwStream = getClass().getResourceAsStream("/marcocipriani/openfocuser/manager/fwconf/firmwares");
        BufferedReader fwReader = new BufferedReader(new InputStreamReader(fwStream));
        String fwLine;
        String fwName = null, hex = null, version = null;
        firmwares = new ArrayList<>();
        while ((fwLine = fwReader.readLine()) != null) {
            if (fwLine.startsWith("[")) {
                fwName = fwLine.substring(1, fwLine.indexOf(']'));

            } else if (fwLine.startsWith("hex=")) {
                hex = fwLine.substring(fwLine.indexOf('\"') + 1, fwLine.lastIndexOf('\"'));

            } else if (fwLine.startsWith("version")) {
                version = fwLine.substring(fwLine.indexOf('\"') + 1, fwLine.lastIndexOf('\"'));
            }
            if (fwName != null && hex != null && version != null) {
                firmwares.add(new Firmware(fwName, hex, version));
                fwName = hex = version = null;
            }
        }

        InputStream stream = getClass().getResourceAsStream("/marcocipriani/openfocuser/manager/fwconf/boards");
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        String bLine;
        String bName = null, avrdudeCmd = null, hexSuffix = null;
        boards = new ArrayList<>();
        while ((bLine = reader.readLine()) != null) {
            if (bLine.startsWith("[")) {
                bName = bLine.substring(1, bLine.indexOf(']'));

            } else if (bLine.startsWith("cmd=")) {
                avrdudeCmd = bLine.substring(bLine.indexOf('\"') + 1, bLine.lastIndexOf('\"'));

            } else if (bLine.startsWith("hex-suffix")) {
                hexSuffix = bLine.substring(bLine.indexOf('\"') + 1, bLine.lastIndexOf('\"'));
            }
            if (bName != null && avrdudeCmd != null && hexSuffix != null) {
                boards.add(new Board(bName, avrdudeCmd, hexSuffix));
                bName = avrdudeCmd = hexSuffix = null;
            }
        }

        tmpDir = System.getProperty("java.io.tmpdir");
        tmpDir = tmpDir + (tmpDir.endsWith(File.separator) ? "" : File.separator) + "OpenFocuser";
        File tmpDirFile = new File(tmpDir);
        if (!tmpDirFile.exists()) {
            tmpDirFile.mkdir();

        } else if (!tmpDirFile.isDirectory()) {
            throw new IllegalStateException("Invalid temp folder!");
        }

        InputStream cStream = getClass().getResourceAsStream("/marcocipriani/openfocuser/manager/fwconf/avrdude.conf");
        File cFile = new File(tmpDir + File.separator + "avrdude.conf");
        Files.copy(cStream, cFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        avrdudeConfig = cFile.getAbsolutePath();

        String os = System.getProperty("os.name").toLowerCase();
        if (os.equals("linux")) {
            avrdude = "avrdude";

        } else if (os.contains("win")) {
            InputStream pStream = getClass().getResourceAsStream("/bin/avrdude.exe");
            File pFile = new File(tmpDir + File.separator + "avrdude.exe");
            Files.copy(pStream, pFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            InputStream dllStream = getClass().getResourceAsStream("/bin/libusb0.dll");
            File dllFile = new File(tmpDir + File.separator + "libusb0.dll");
            Files.copy(dllStream, dllFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            avrdude = pFile.getAbsolutePath();

        } else {
            throw new UnsupportedOperationException("Unsupported OS!");
        }

        this.listener = listener;
    }

    /**
     * @return a list of available boards.
     * @see Board
     */
    public ArrayList<Board> getBoards() {
        return boards;
    }

    /**
     * @return a list of available firmwares.
     * @see Firmware
     */
    public ArrayList<Firmware> getFirmwares() {
        return firmwares;
    }

    /**
     * Uploads the given firmware to the chosen board at the selected port. The whole process runs in another thread.
     *
     * @param board    the board type.
     * @param firmware the firmware to upload.
     * @param port     the port of the board.
     */
    public void updateFirmware(Firmware firmware, Board board, String port) {
        new Thread(() -> {
            try {
                String hex = firmware.hex + board.hexSuffix;
                InputStream hStream = getClass().getResourceAsStream("/hex/" + hex);
                File hFile = new File(tmpDir + File.separator + hex);
                Files.copy(hStream, hFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

                String[] options = board.avrdudeCmd.replace("{port}", port).replace("{avrdude.conf}", avrdudeConfig)
                        .replace("{hex}", hFile.getAbsolutePath()).split(" ");
                String[] cmd = new String[options.length + 1];
                cmd[0] = avrdude;
                System.arraycopy(options, 0, cmd, 1, options.length);
                ProcessBuilder processBuilder = new ProcessBuilder(cmd);
                processBuilder.redirectErrorStream(true);
                Process process = processBuilder.start();
                BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String line;
                while ((line = in.readLine()) != null) {
                    listener.print(line);
                }
                try {
                    process.waitFor();

                } catch (InterruptedException ignored) {

                }
                in.close();

            } catch (IOException e) {
                listener.print(e.getMessage());
                e.printStackTrace();
            }
            listener.onFinished();
        }, "avrdude runner").start();
    }

    /**
     * Avrdude listener.
     *
     * @author marcocipriani01
     * @version 1.0
     */
    public interface AvrdudeListener {

        /**
         * Prints the selected log.
         *
         * @param log a log from Avrdude
         */
        void print(String log);

        /**
         * Called when avrdude finishes its task.
         */
        void onFinished();
    }

    /**
     * Represents a firmware.
     *
     * @author marcocipriani01
     * @version 1.0
     */
    public class Firmware {

        public final String name;
        public final String hex;
        public final String version;

        Firmware(String name, String hex, String version) {
            this.name = name;
            this.hex = hex;
            this.version = version;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    /**
     * Represents a target AVR board.
     *
     * @author marcocipriani01
     * @version 1.0
     */
    public class Board {

        public final String name;
        public final String avrdudeCmd;
        public final String hexSuffix;

        Board(String name, String avrdudeCmd, String hexSuffix) {
            this.name = name;
            this.avrdudeCmd = avrdudeCmd;
            this.hexSuffix = hexSuffix;
        }

        @Override
        public String toString() {
            return name;
        }
    }
}