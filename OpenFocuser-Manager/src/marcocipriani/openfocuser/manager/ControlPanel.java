package marcocipriani.openfocuser.manager;

import marcocipriani.openfocuser.manager.io.ConnectionException;
import marcocipriani.openfocuser.manager.io.ScpUploader;
import marcocipriani.openfocuser.manager.io.SerialPortImpl;
import marcocipriani.openfocuser.manager.pins.ArduinoPin;
import marcocipriani.openfocuser.manager.pins.ArduinoPinsJTable;
import marcocipriani.openfocuser.manager.updater.Updater;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.UncheckedIOException;

import static marcocipriani.openfocuser.manager.AppInfo.APP_LOGO;

/**
 * The app's control panel.
 *
 * @author marcocipriani01
 * @version 2.0
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class ControlPanel extends JFrame {

    private JPanel parent;
    private JSpinner localIndiPortField;
    private JButton addDigitalPinButton;
    private JButton removeDigitalPinButton;
    private JButton addPwmPinButton;
    private JButton removePwmPinButton;
    private JButton saveConfigButton;
    private JButton sendConfigButton;
    private JComboBox<Updater.Board> boardComboBox;
    private JComboBox<Updater.Firmware> fwComboBox;
    private JLabel fwVersionLabel;
    private JButton updateFwButton;
    private JTextArea avrdudeLogs;
    private JComboBox<String> serialPortComboBox;
    private JButton refreshPortsButton;
    private JScrollPane logsScrollPane;
    private JLabel managerVersionLabel;
    private JButton updateManagerButton;
    private JLabel titleLabel;
    private JButton websiteButton;
    private JButton githubButton;
    private JButton issueReportButton;
    private JLabel codenameLabel;
    private JButton startServerButton;
    private JButton stopServerButton;
    private JPanel startStopServerPanel;
    private ArduinoPinsJTable digitalPinsJTable;
    private ArduinoPinsJTable pwmPinsJTable;
    private JTabbedPane mainTabbedPane;

    private Updater fwUpdater;
    private AppInfo appInfo;
    private Settings settings;

    /**
     * Class constructor.
     */
    public ControlPanel() {
        super(AppInfo.APP_NAME + " control panel");
        setIconImage(APP_LOGO);
        setContentPane(parent);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        codenameLabel.setText(AppInfo.CODENAME);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
                int operation = JOptionPane.showConfirmDialog(ControlPanel.this, "Save and exit, exit or cancel?", AppInfo.APP_NAME,
                        JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
                if (operation == JOptionPane.YES_OPTION) {
                    saveConfig();
                    Utils.exit(Utils.ExitCodes.OK);

                } else if (operation == JOptionPane.NO_OPTION) {
                    Utils.exit(Utils.ExitCodes.OK);
                }
            }
        });

        try {
            fwUpdater = new Updater(new Updater.AvrdudeListener() {
                JScrollBar scrollBar = logsScrollPane.getVerticalScrollBar();

                @Override
                public void print(String log) {
                    avrdudeLogs.append("\n" + log);
                    scrollBar.setValue(scrollBar.getMaximum()); //TODO: it doesn't seem to work!
                }

                @Override
                public void onFinished() {
                    updateFwButton.setEnabled(true);
                }
            });
            for (Updater.Firmware firmware : fwUpdater.getFirmwares()) {
                fwComboBox.addItem(firmware);
            }
            fwComboBox.addActionListener(e -> updateFwVersionLabel());
            updateFwVersionLabel();
            for (Updater.Board board : fwUpdater.getBoards()) {
                boardComboBox.addItem(board);
            }
            for (String p : SerialPortImpl.scanSerialPorts()) {
                serialPortComboBox.addItem(p);
            }
            serialPortComboBox.setSelectedItem(settings.serialPort);
            refreshPortsButton.addActionListener(this::actionPerformed);
            updateFwButton.addActionListener(this::actionPerformed);

        } catch (IOException | UnsupportedOperationException | IllegalStateException e) {
            Utils.err("Firmware updater tool not available!", e, this);
            boardComboBox.setEnabled(false);
            fwComboBox.setEnabled(false);
            updateFwButton.setEnabled(false);
            refreshPortsButton.setEnabled(false);
            serialPortComboBox.setEnabled(false);
        }

        try {
            appInfo = new AppInfo();
            managerVersionLabel.setText(appInfo.getCurrentVersion());
            updateManagerButton.addActionListener(this::actionPerformed);

        } catch (IOException | IllegalStateException | NullPointerException e) {
            if (!Utils.isDebugMode()) {
                Utils.err("Unable to retrieve information about the current version!", e, this);
            }
            updateManagerButton.setEnabled(false);
        }

        websiteButton.addActionListener(e -> Utils.launchBrowser(AppInfo.WEBSITE, this));
        issueReportButton.addActionListener(e -> Utils.launchBrowser(AppInfo.ISSUE_REPORT, this));
        githubButton.addActionListener(e -> Utils.launchBrowser(AppInfo.GITHUB_REPO, this));

        saveConfigButton.addActionListener(e -> saveConfig());
        sendConfigButton.addActionListener(this::actionPerformed);

        startServerButton.addActionListener(this::actionPerformed);
        stopServerButton.addActionListener(this::actionPerformed);

        pack();
        setMinimumSize(getPreferredSize());
        setBounds(300, 200, 750, 550);
        setVisible(true);
    }

    /**
     * Sets up the user interface.
     */
    private void createUIComponents() {
        settings = Main.getSettings();

        localIndiPortField = new JSpinner(new SpinnerNumberModel(settings.localServerIndiPort, 10, 99999, 1));
        JSpinner.NumberEditor editor1 = new JSpinner.NumberEditor(localIndiPortField, "#");
        editor1.getTextField().setHorizontalAlignment(JTextField.LEFT);
        localIndiPortField.setEditor(editor1);

        digitalPinsJTable = new ArduinoPinsJTable(settings.digitalPins, false,
                addDigitalPinButton = new JButton(), removeDigitalPinButton = new JButton(), this);
        pwmPinsJTable = new ArduinoPinsJTable(settings.pwmPins, true,
                addPwmPinButton = new JButton(), removePwmPinButton = new JButton(), this);
    }

    /**
     * Saves all the configuration to the settings.
     *
     * @return {@code true} if everything was OK and the settings were been saved.
     */
    private boolean saveConfig() {
        Utils.info("Saving settings...");
        Object sp = serialPortComboBox.getSelectedItem();
        if (sp != null) {
            settings.serialPort = (String) sp;
        }
        int port = (int) localIndiPortField.getValue();
        if (port < 50) {
            Utils.err("Invalid local INDI server port!", this);
            return false;

        } else {
            settings.localServerIndiPort = port;
        }
        if (settings.digitalPins.hasDuplicates(settings.pwmPins)) {
            Utils.err("Duplicated pins found, please fix this in order to continue.", this);
            return false;
        }
        try {
            settings.save();

        } catch (UncheckedIOException e) {
            Utils.err(e);
            return false;
        }
        return true;
    }

    private void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
        if (source == updateFwButton) {
            Updater.Firmware selectedFw = (Updater.Firmware) fwComboBox.getSelectedItem();
            if (selectedFw != null) {
                Updater.Board selectedBoard = (Updater.Board) boardComboBox.getSelectedItem();
                if (selectedBoard != null) {
                    Object port = serialPortComboBox.getSelectedItem();
                    if (port != null) {
                        updateFwButton.setEnabled(false);
                        fwUpdater.updateFirmware(selectedFw, selectedBoard, (String) port);

                    } else {
                        Utils.err("Please select a port!", this);
                    }

                } else {
                    Utils.err("The selected board isn't valid!", this);
                }

            } else {
                Utils.err("The selected firmware isn't valid!", this);
            }

        } else if (source == refreshPortsButton) {
            boolean popupVisible = serialPortComboBox.isPopupVisible();
            String selectedItem = (String) serialPortComboBox.getSelectedItem();
            serialPortComboBox.removeAllItems();
            for (String p : SerialPortImpl.scanSerialPorts()) {
                serialPortComboBox.addItem(p);
            }
            if (popupVisible) {
                SwingUtilities.invokeLater(() -> serialPortComboBox.showPopup());
            }
            serialPortComboBox.setSelectedItem(selectedItem);

        } else if (source == updateManagerButton) {
            try {
                if (appInfo.checkForUpdates()) {
                    if (JOptionPane.showConfirmDialog(this,
                            "A newer version (" + appInfo.getLatestVersion() + ") is available! Download it?",
                            AppInfo.APP_NAME, JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
                        Utils.launchBrowser(appInfo.getLatestVersionLink(), ControlPanel.this);
                    }

                } else {
                    Utils.info("No updates found!", ControlPanel.this);
                }

            } catch (IOException | IllegalStateException | NumberFormatException ex) {
                Utils.err("Unable to retrieve information about the latest version!", ex, ControlPanel.this);
            }

        } else if (source == sendConfigButton) {
            if (saveConfig()) {
                String host = JOptionPane.showInputDialog(this, "Remote SSH server IP/address:", "SCP", JOptionPane.QUESTION_MESSAGE);
                if (host != null) {
                    String user = JOptionPane.showInputDialog(this, "Remote username:", "SCP", JOptionPane.QUESTION_MESSAGE);
                    if (user != null) {
                        try {
                            ScpUploader.send(settings.getFile(), user, host, "/home/" + user + "/.config/OpenFocuser-Manager/Settings.json",
                                    new ScpUploader.UserInfoProvider(this));
                            Utils.info("Settings uploaded to remote host!", this);

                        } catch (ConnectionException ex) {
                            Utils.err(ex.getMessage(), ex, this);
                        }
                    }
                }
            }

        } else if (source == startServerButton) {
            if (saveConfig()) {
                updateComponentsServerOrClientRunning(true);
                Main.runServer(settings.localServerIndiPort);
            }

        } else if (source == stopServerButton) {
            Main.stopServer();
            updateComponentsServerOrClientRunning(false);
        }
    }

    private void updateComponentsServerOrClientRunning(boolean b) {
        digitalPinsJTable.setEditMode(!b);
        pwmPinsJTable.setEditMode(!b);
        removeDigitalPinButton.setEnabled(!b);
        addDigitalPinButton.setEnabled(!b);
        removePwmPinButton.setEnabled(!b);
        addPwmPinButton.setEnabled(!b);
        localIndiPortField.setEnabled(!b);
        startServerButton.setEnabled(!b);
        stopServerButton.setEnabled(b);
    }

    /**
     * Updates the label that shows the selected firmware version.
     */
    private void updateFwVersionLabel() {
        Updater.Firmware selectedFw = (Updater.Firmware) fwComboBox.getSelectedItem();
        if (selectedFw != null) {
            fwVersionLabel.setText(selectedFw.version);
        }
    }

    /**
     * Shows a dialog to the user asking for a new pin's number.
     *
     * @return an {@link ArduinoPin} object representing the given pin, or {@code null}.
     */
    public ArduinoPin askNewPin() {
        boolean check = true;
        int pin = -1;
        do {
            try {
                String input = JOptionPane.showInputDialog(this, "New pin",
                        "Control panel", JOptionPane.QUESTION_MESSAGE);
                if (input == null) {
                    return null;
                }
                pin = Integer.parseInt(input);
                if ((pin < 2) || (pin > 12)) {
                    Utils.err("Invalid pin: " + pin + " is outside the allowed bounds (2 ≤ pin ≤ 12)!", this);

                } else if (settings.digitalPins.contains(pin) || settings.pwmPins.contains(pin)) {
                    Utils.err("Pin " + pin + " is already defined!", this);

                } else {
                    check = false;
                }

            } catch (NumberFormatException e) {
                Utils.err("Invalid pin! Must be a number.", this);
            }
        } while (check);
        return new ArduinoPin(pin, "Pin " + pin);
    }
}