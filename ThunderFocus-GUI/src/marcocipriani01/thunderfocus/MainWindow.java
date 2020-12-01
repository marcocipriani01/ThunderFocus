package marcocipriani01.thunderfocus;

import marcocipriani01.thunderfocus.focuser.ThunderFocuser;
import marcocipriani01.thunderfocus.indi.INDIThunderFocuserDriver;
import marcocipriani01.thunderfocus.io.ConnectionException;
import marcocipriani01.thunderfocus.io.SerialPortImpl;
import marcocipriani01.thunderfocus.focuser.ArduinoPin;
import marcocipriani01.thunderfocus.focuser.PowerBox;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.HyperlinkEvent;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.*;
import java.io.IOException;
import java.net.URISyntaxException;

import static marcocipriani01.thunderfocus.Main.APP_NAME;

public class MainWindow extends JFrame implements ChangeListener, ActionListener, KeyListener, FocusListener, ThunderFocuser.Listener, Settings.SettingsListener {

    private static final ImageIcon POWERBOX_TAB = new ImageIcon(MainWindow.class.getResource("/marcocipriani01/thunderfocus/res/powerboxtab.png"));
    private final MiniWindow miniWindow = new MiniWindow() {
        @Override
        protected void onHide() {
            MainWindow.this.setState(Frame.NORMAL);
        }
    };
    private JPanel parent;
    private JComboBox<String> serialPortComboBox;
    private JButton refreshButton;
    private JButton connectButton;
    private JSlider posSlider;
    private JButton stopButton;
    private JTextField requestedPosField;
    private JTextField currentPosField;
    private JTextField relativeMovField;
    private JButton fokOutButton;
    private JButton setRequestedPosButton;
    private JButton setZeroButton;
    private JButton fokInButton;
    private JSlider ticksPosSlider;
    private JButton miniWindowButton;
    private JLabel aboutLabel;
    private JLabel unitsLabel;
    private JLabel connStatusLabel;
    private JLabel focuserStateLabel;
    private JLabel err;
    private JLabel ok;
    private JComboBox<String> appThemeCombo;
    private JSpinner fokTicksCountSpinner;
    private JSpinner fokMaxTravelSpinner;
    private JComboBox<Settings.Units> fokUnitsCombo;
    private JSpinner fokBacklashSpinner;
    private JButton fokBacklashCalButton;
    private JSlider fokSpeedSlider;
    private JCheckBox fokPowerSaverBox;
    private JCheckBox fokReverseDirBox;
    private JPowerBoxTable powerBoxTable;
    private JButton applyPowerBoxButton;
    private JButton saveConfigButton;
    private JCheckBox enableINDIServerBox;
    private JSpinner indiPortSpinner;
    private JTextField driverNameBox;
    private JComboBox<String> localOrRemoteCombo;
    private JButton copyIndiDriverNameButton;
    private JLabel timeout;
    private JTextPane infoPane;
    private JToggleButton pinWindowButton;
    private JButton powerBoxOffButton;
    private JButton powerBoxOnButton;
    private JLabel indiStatusLabel;
    private JTabbedPane tabPane;
    private JPanel powerBoxTab;
    private JComboBox<PowerBox.AutoModes> powerBoxAutoModeBox;

    public MainWindow() {
        super(APP_NAME);
        setIconImage(Main.APP_LOGO);
        setContentPane(parent);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
                askClose();
            }
        });
        parent.registerKeyboardAction(e -> askClose(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        tabPane.removeTabAt(1);
        tabPane.addChangeListener(this);

        aboutLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        aboutLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                Main.openBrowser("https://marcocipriani01.github.io", MainWindow.this);
            }
        });
        infoPane.setCaretPosition(0);
        setKeyListeners(parent, connectButton, refreshButton, setRequestedPosButton, fokBacklashCalButton,
                setZeroButton, fokInButton, fokOutButton, miniWindowButton, stopButton, requestedPosField,
                aboutLabel, currentPosField, ticksPosSlider, posSlider, relativeMovField, pinWindowButton,
                powerBoxOnButton, powerBoxOffButton);
        setButtonListeners(connectButton, refreshButton, setRequestedPosButton, fokBacklashCalButton,
                setZeroButton, fokInButton, fokOutButton, miniWindowButton, stopButton,
                applyPowerBoxButton, copyIndiDriverNameButton, saveConfigButton,
                powerBoxOnButton, powerBoxOffButton);
        pinWindowButton.addActionListener(this);
        requestedPosField.addActionListener(this);
        relativeMovField.addActionListener(this);
        updateSlidersLimit();
        posSlider.addFocusListener(this);
        ticksPosSlider.addFocusListener(this);
        updateUnitsLabel();
        localOrRemoteCombo.setSelectedIndex(Main.settings.getShowRemoteIndi() ? 1 : 0);
        localOrRemoteCombo.addItemListener(e -> refreshDriverName());
        refreshDriverName();
        appThemeCombo.setSelectedIndex(Main.settings.getTheme());
        enableINDIServerBox.setSelected(Main.settings.isIndiEnabled());

        parent.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                super.mouseEntered(e);
                parent.requestFocus();
            }
        });
        infoPane.addHyperlinkListener(e -> {
            if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                try {
                    Main.openBrowser(e.getURL().toURI(), this);
                } catch (URISyntaxException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(MainWindow.this, "Errore durante l'apertura del browser!", APP_NAME, JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        boolean selectPort = false;
        String serialPort = Main.settings.getSerialPort();
        for (String p : SerialPortImpl.scanSerialPorts()) {
            serialPortComboBox.addItem(p);
            if (p.equals(serialPort)) {
                selectPort = true;
            }
        }
        if (selectPort) serialPortComboBox.setSelectedItem(serialPort);

        Main.focuser.addListener(this);
        Main.settings.addListener(this);
        startOrStopINDI(false);

        setResizable(false);
        setBounds(350, 150, 750, 750);
        setVisible(true);
    }

    private void createUIComponents() {
        indiPortSpinner = new JSpinner(new SpinnerNumberModel(
                Main.settings.getIndiServerPort(), 1024, 9999, 1));
        fokTicksCountSpinner = new JSpinner(new SpinnerNumberModel(
                Main.settings.getFokTicksCount(), 10, 2147483647, 1));
        fokUnitsCombo = new JComboBox<>(Settings.Units.values());
        fokUnitsCombo.setSelectedItem(Main.settings.getFokTicksUnit());
        fokMaxTravelSpinner = new JSpinner(new SpinnerNumberModel(
                Main.settings.getFokMaxTravel(), 1, 2147483647, 1));
        fokBacklashSpinner = new JSpinner(new SpinnerNumberModel(
                Main.focuser.getBacklash(), 0, 200, 1));
        powerBoxTable = new JPowerBoxTable();
        powerBoxAutoModeBox = new JComboBox<>();
    }

    private void refreshDriverName() {
        try {
            driverNameBox.setText(INDIThunderFocuserDriver.DRIVER_NAME + "@" +
                    Main.getIP(localOrRemoteCombo.getSelectedIndex() == 0) + ":" + Main.settings.getIndiServerPort());
        } catch (Exception e) {
            e.printStackTrace();
            driverNameBox.setText("Errore nel rilevamento dell'IP!");
        }
    }

    private void askClose() {
        MainWindow.this.setState(Frame.NORMAL);
        toFront();
        requestFocus();
        if (JOptionPane.showConfirmDialog(this, "Uscire dall'applicazione?", APP_NAME,
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
            Main.focuser.removeListener(this);
            dispose();
            Main.exit(0);
        }
    }

    private void setKeyListeners(Component... components) {
        for (Component c : components) {
            c.addKeyListener(this);
            c.setFocusable(true);
            c.setFocusTraversalKeysEnabled(false);
        }
    }

    private void setButtonListeners(JButton... buttons) {
        for (JButton b : buttons) {
            b.addActionListener(this);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
        if (source == refreshButton) {
            String selectedItem = (String) serialPortComboBox.getSelectedItem();
            serialPortComboBox.removeAllItems();
            for (String p : SerialPortImpl.scanSerialPorts()) {
                serialPortComboBox.addItem(p);
            }
            serialPortComboBox.setSelectedItem(selectedItem);
            serialPortComboBox.showPopup();

        } else if (source == connectButton) {
            try {
                if (Main.focuser.isConnected()) {
                    Main.focuser.disconnect();
                } else if (serialPortComboBox.getSelectedItem() != null) {
                    String port = (String) serialPortComboBox.getSelectedItem();
                    Main.settings.setSerialPort(port, this);
                    Main.focuser.connect(port);
                } else {
                    JOptionPane.showMessageDialog(this, "Nessuna porta disponibile o selezionata.", APP_NAME, JOptionPane.ERROR_MESSAGE);
                }
            } catch (ConnectionException ex) {
                connectionErr(ex);
            }

        } else if (source == pinWindowButton) {
            setAlwaysOnTop(pinWindowButton.isSelected());

        } else if (source == stopButton) {
            try {
                Main.focuser.run(ThunderFocuser.Commands.FOK1_STOP, this);
            } catch (ConnectionException ex) {
                connectionErr(ex);
            } catch (ThunderFocuser.InvalidParamException ex) {
                ex.printStackTrace();
            }

        } else if (source == fokOutButton || source == relativeMovField) {
            try {
                Main.focuser.run(ThunderFocuser.Commands.FOK1_REL_MOVE, this, Integer.parseInt(relativeMovField.getText()));
            } catch (ConnectionException ex) {
                connectionErr(ex);
            } catch (ThunderFocuser.InvalidParamException | NumberFormatException ex) {
                valueOutOfLimits(ex);
            }

        } else if (source == fokInButton) {
            try {
                Main.focuser.run(ThunderFocuser.Commands.FOK1_REL_MOVE, this, -Integer.parseInt(relativeMovField.getText()));
            } catch (NumberFormatException ignored) {
            } catch (ConnectionException | ThunderFocuser.InvalidParamException connectionException) {
                connectionException.printStackTrace();
            }

        } else if (source == setRequestedPosButton || source == requestedPosField) {
            try {
                Main.focuser.run(ThunderFocuser.Commands.FOK1_ABS_MOVE, this, Integer.parseInt(requestedPosField.getText()));
            } catch (ConnectionException ex) {
                connectionErr(ex);
            } catch (ThunderFocuser.InvalidParamException | NumberFormatException ex) {
                valueOutOfLimits(ex);
            }

        } else if (source == setZeroButton) {
            try {
                Main.focuser.run(ThunderFocuser.Commands.FOK1_SET_ZERO, this);
            } catch (ConnectionException ex) {
                connectionErr(ex);
            } catch (ThunderFocuser.InvalidParamException ex) {
                ex.printStackTrace();
            }

        } else if (source == miniWindowButton) {
            miniWindow.setVisible(true);
            setState(Frame.ICONIFIED);

        } else if (source == copyIndiDriverNameButton) {
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(driverNameBox.getText()), null);

        } else if (source == fokBacklashCalButton) {
            if (JOptionPane.showConfirmDialog(this, "Procedere? Attenzione: il focheggiatore verrà mosso, assicurarsi che questo non interrompa eventuali sessioni osservative. " +
                            "Inoltre, è molto importante che i limiti del focheggiatore siano ben impostati.",
                    APP_NAME, JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.OK_OPTION) {
                new BacklashCalibrationWindow(this);
            }

        } else if (source == saveConfigButton) {
            Main.settings.setTheme(appThemeCombo.getSelectedIndex(), this);
            Main.settings.setIndiEnabled(enableINDIServerBox.isSelected(), this);
            Main.settings.setShowRemoteIndi(localOrRemoteCombo.getSelectedIndex() == 1, this);
            int oldIndiPort = Main.settings.getIndiServerPort();
            Main.settings.setIndiServerPort((int) indiPortSpinner.getValue(), this);
            if (Main.focuser.isConnected() && Main.focuser.isReady()) {
                Main.settings.setFokTicksCount((int) fokTicksCountSpinner.getValue(), this);
                Main.settings.setFokTicksUnit(Settings.Units.values()[fokUnitsCombo.getSelectedIndex()], this);
                updateUnitsLabel();
                Main.settings.setFokMaxTravel((int) fokMaxTravelSpinner.getValue(), this);
                posSlider.removeChangeListener(this);
                ticksPosSlider.removeChangeListener(this);
                updateSlidersLimit();
                posSlider.addChangeListener(this);
                ticksPosSlider.addChangeListener(this);
                try {
                    Main.focuser.run(ThunderFocuser.Commands.FOK1_SET_BACKLASH, this, (int) fokBacklashSpinner.getValue());
                    Main.focuser.run(ThunderFocuser.Commands.FOK1_SET_SPEED, this, fokSpeedSlider.getValue());
                    Main.focuser.run(ThunderFocuser.Commands.FOK1_REVERSE_DIR, this, fokReverseDirBox.isSelected() ? 1 : 0);
                    Main.focuser.run(ThunderFocuser.Commands.FOK1_POWER_SAVER, this, fokPowerSaverBox.isSelected() ? 1 : 0);
                } catch (ConnectionException ex) {
                    connectionErr(ex);
                } catch (ThunderFocuser.InvalidParamException | NumberFormatException ex) {
                    valueOutOfLimits(ex);
                }
            }
            startOrStopINDI(Main.settings.getIndiServerPort() != oldIndiPort);
            try {
                Main.settings.save();
            } catch (IOException ioException) {
                ioException.printStackTrace();
                JOptionPane.showMessageDialog(this, "Impossibile salvare la configurazione!", APP_NAME, JOptionPane.ERROR_MESSAGE);
            }
            tabPane.setSelectedIndex(0);

        } else if (source == applyPowerBoxButton) {
            try {
                PowerBox powerBox = powerBoxTable.getPowerBox();
                Main.settings.setPowerBox(new PowerBox(powerBox), this);
                try {
                    Main.settings.save();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                PowerBox.AutoModes autoMode = (PowerBox.AutoModes) powerBoxAutoModeBox.getSelectedItem();
                if (autoMode != null) {
                    Main.focuser.run(ThunderFocuser.Commands.POWER_BOX_SET_AUTO_MODE, this, autoMode.ordinal());
                }
                for (ArduinoPin p : powerBox.asList()) {
                    int number = p.getNumber();
                    if (p.isAutoModeEn()) {
                        Main.focuser.run(ThunderFocuser.Commands.POWER_BOX_SET_PIN_AUTO, this, number, 1);
                    } else {
                        Main.focuser.run(ThunderFocuser.Commands.POWER_BOX_SET, this, number, p.getValuePwm());
                    }
                }
            } catch (ConnectionException ex) {
                connectionErr(ex);
            } catch (ThunderFocuser.InvalidParamException | NumberFormatException ex) {
                valueOutOfLimits(ex);
            }

        } else if (source == powerBoxOnButton) {
            try {
                for (ArduinoPin p : Main.focuser.getPowerBox().asList()) {
                    p.setValue(255);
                    Main.focuser.run(ThunderFocuser.Commands.POWER_BOX_SET, this, p.getNumber(), p.getValuePwm());
                }
                powerBoxTable.refresh();
            } catch (ConnectionException ex) {
                connectionErr(ex);
            } catch (ThunderFocuser.InvalidParamException ex) {
                ex.printStackTrace();
            }

        } else if (source == powerBoxOffButton) {
            try {
                for (ArduinoPin p : Main.focuser.getPowerBox().asList()) {
                    p.setValue(0);
                    Main.focuser.run(ThunderFocuser.Commands.POWER_BOX_SET, this, p.getNumber(), p.getValuePwm());
                }
                powerBoxTable.refresh();
            } catch (ConnectionException ex) {
                connectionErr(ex);
            } catch (ThunderFocuser.InvalidParamException ex) {
                ex.printStackTrace();
            }
        }
    }

    private void valueOutOfLimits(Exception e) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(this, "Valore fuori dai limiti o non valido.", APP_NAME, JOptionPane.ERROR_MESSAGE);
    }

    private void connectionErr(ConnectionException e) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(this, "Errore di connessione!", APP_NAME, JOptionPane.ERROR_MESSAGE);
    }

    private void updateSlidersLimit() {
        int fokMaxTravel = Main.settings.getFokMaxTravel();
        posSlider.setMaximum(fokMaxTravel);
        posSlider.setMinorTickSpacing(fokMaxTravel / 70);
        posSlider.setLabelTable(null);
        posSlider.setMajorTickSpacing(fokMaxTravel / 4);
        int fokTicksCount = Main.settings.getFokTicksCount();
        ticksPosSlider.setMaximum(fokTicksCount);
        ticksPosSlider.setMinorTickSpacing(fokTicksCount / 70);
        ticksPosSlider.setLabelTable(null);
        ticksPosSlider.setMajorTickSpacing(fokTicksCount / 7);
    }

    private void enableComponents(boolean connected) {
        if (!connected) {
            miniWindow.dispose();
        }
        refreshButton.setEnabled(!connected);
        serialPortComboBox.setEnabled(!connected);
        posSlider.setEnabled(connected);
        stopButton.setEnabled(connected);
        fokOutButton.setEnabled(connected);
        fokInButton.setEnabled(connected);
        setRequestedPosButton.setEnabled(connected);
        setZeroButton.setEnabled(connected);
        relativeMovField.setEnabled(connected);
        requestedPosField.setEnabled(connected);
        ticksPosSlider.setEnabled(connected);
        miniWindowButton.setEnabled(connected);
        fokTicksCountSpinner.setEnabled(connected);
        fokUnitsCombo.setEnabled(connected);
        fokMaxTravelSpinner.setEnabled(connected);
        fokBacklashSpinner.setEnabled(connected);
        fokBacklashCalButton.setEnabled(connected);
        fokSpeedSlider.setEnabled(connected);
        fokReverseDirBox.setEnabled(connected);
        fokPowerSaverBox.setEnabled(connected);
        boolean pbEn = connected && Main.focuser.isPowerBox();
        powerBoxOnButton.setEnabled(pbEn);
        powerBoxOffButton.setEnabled(pbEn);
        powerBoxAutoModeBox.setEnabled(pbEn);
        applyPowerBoxButton.setEnabled(pbEn);
    }

    private void startOrStopINDI(boolean forceRestart) {
        if (Main.settings.isIndiEnabled()) {
            Main.indiServerCreator.start(Main.settings.getIndiServerPort(), forceRestart);
            indiStatusLabel.setText("Server avviato");
        } else {
            Main.indiServerCreator.stop();
            indiStatusLabel.setText("Server non attivo");
        }
    }

    @Override
    public void stateChanged(ChangeEvent event) {
        Object source = event.getSource();
        if (source == tabPane) {
            if (tabPane.getSelectedComponent() == powerBoxTab) powerBoxTable.fixWidths();
            return;
        }
        try {
            if (source == posSlider) {
                Main.focuser.run(ThunderFocuser.Commands.FOK1_ABS_MOVE, this, posSlider.getValue());
            } else if (source == ticksPosSlider) {
                Main.focuser.run(ThunderFocuser.Commands.FOK1_ABS_MOVE, this, Main.focuser.ticksToSteps(ticksPosSlider.getValue()));
            }
        } catch (ConnectionException ex) {
            connectionErr(ex);
        } catch (ThunderFocuser.InvalidParamException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {

    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (Main.focuser.isConnected() && Main.focuser.isReady()) {
            int keyCode = e.getKeyCode();
            if (keyCode == KeyEvent.VK_RIGHT) {
                fokOutButton.doClick();
            } else if (keyCode == KeyEvent.VK_LEFT) {
                fokInButton.doClick();
            } else if (keyCode == KeyEvent.VK_DOWN) {
                stopButton.doClick();
            }
        }
    }

    @Override
    public void focusGained(FocusEvent e) {

    }

    @Override
    public void focusLost(FocusEvent e) {
        Object source = e.getSource();
        if (source == posSlider) {
            updatePosSlider();

        } else if (source == ticksPosSlider) {
            updateTicksPosSlider();
        }
    }

    private void updatePosSlider() {
        posSlider.removeChangeListener(MainWindow.this);
        posSlider.setValue(Main.focuser.getCurrentPos());
        posSlider.addChangeListener(MainWindow.this);
    }

    private void updateTicksPosSlider() {
        ticksPosSlider.removeChangeListener(MainWindow.this);
        ticksPosSlider.setValue(Main.focuser.getCurrentPosTicks());
        ticksPosSlider.addChangeListener(MainWindow.this);
    }

    private void updateUnitsLabel() {
        unitsLabel.setText(Main.settings.getFokTicksUnit().toString() + ":");
    }

    @Override
    public void onReachedPos() {

    }

    @Override
    public void updateParam(ThunderFocuser.Parameters p) {
        SwingUtilities.invokeLater(() -> {
            switch (p) {
                case REQUESTED_POS -> requestedPosField.setText(String.valueOf(Main.focuser.getRequestedPos()));
                case REQUESTED_REL_POS -> relativeMovField.setText(String.valueOf(Math.abs(Main.focuser.getRequestedRelPos())));
                case CURRENT_POS -> {
                    currentPosField.setText(String.valueOf(Main.focuser.getCurrentPos()));
                    if (!posSlider.hasFocus()) {
                        updatePosSlider();
                    }
                }
                case CURRENT_POS_TICKS -> {
                    if (!ticksPosSlider.hasFocus()) {
                        updateTicksPosSlider();
                    }
                }
                case SPEED -> {
                    if (!fokSpeedSlider.hasFocus()) {
                        fokSpeedSlider.setValue(Main.focuser.getSpeed());
                    }
                }
                case BACKLASH -> fokBacklashSpinner.setValue(Main.focuser.getBacklash());
                case REVERSE_DIR -> fokReverseDirBox.setSelected(Main.focuser.isReverseDir());
                case ENABLE_POWER_SAVE -> fokPowerSaverBox.setSelected(Main.focuser.isPowerSaver());
                case POWERBOX_PINS -> powerBoxTable.refresh();
            }
        });
    }

    @Override
    public void updateFocuserState(ThunderFocuser.FocuserState focuserState) {
        SwingUtilities.invokeLater(() -> focuserStateLabel.setText(focuserState.getLabel()));
    }

    @Override
    public void updateConnSate(ThunderFocuser.ConnState connState) {
        SwingUtilities.invokeLater(() -> {
            connStatusLabel.setText(connState.getLabel());
            switch (connState) {
                case CONNECTED_READY: {
                    ok.setVisible(true);
                    timeout.setVisible(false);
                    err.setVisible(false);
                    if (Main.focuser.isPowerBox()) {
                        if (tabPane.getTabComponentAt(1) != powerBoxTab) {
                            tabPane.insertTab("Power box", POWERBOX_TAB, powerBoxTab, "", 1);
                        }
                        PowerBox powerBox = Main.focuser.getPowerBox();
                        powerBoxTable.setPowerBox(powerBox);
                        boolean supportsAutoModes = powerBox.supportsAutoModes();
                        powerBoxAutoModeBox.setEnabled(supportsAutoModes);
                        if (supportsAutoModes) {
                            powerBoxAutoModeBox.setModel(new DefaultComboBoxModel<>(powerBox.supportedAutoModesArray()));
                        }
                    }
                    fokSpeedSlider.setValue(Main.focuser.getSpeed());
                    fokReverseDirBox.setSelected(Main.focuser.isReverseDir());
                    fokPowerSaverBox.setSelected(Main.focuser.isPowerSaver());
                    int currentPos = Main.focuser.getCurrentPos();
                    posSlider.removeChangeListener(MainWindow.this);
                    posSlider.setValue(currentPos);
                    posSlider.addChangeListener(MainWindow.this);
                    ticksPosSlider.removeChangeListener(MainWindow.this);
                    ticksPosSlider.setValue(Main.focuser.stepsToTicks(currentPos));
                    ticksPosSlider.addChangeListener(MainWindow.this);
                    String currentPosStr = String.valueOf(currentPos);
                    currentPosField.setText(currentPosStr);
                    requestedPosField.setText(currentPosStr);
                    relativeMovField.setText(String.valueOf(Main.focuser.getRequestedRelPos()));
                    enableComponents(true);
                    break;
                }
                case DISCONNECTED: {
                    enableComponents(false);
                    powerBoxTable.setPowerBox(null);
                }
                case ERROR: {
                    ok.setVisible(false);
                    timeout.setVisible(false);
                    err.setVisible(true);
                    break;
                }
                case TIMEOUT: {
                    ok.setVisible(false);
                    timeout.setVisible(true);
                    err.setVisible(false);
                    break;
                }
            }
        });
    }

    @Override
    public void onCriticalError(Exception e) {
        SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(MainWindow.this, "Errore inaspettato!", APP_NAME, JOptionPane.ERROR_MESSAGE));
    }

    @Override
    public void update(Settings.Value what, int value) {
        SwingUtilities.invokeLater(() -> {
            switch (what) {
                case THEME -> appThemeCombo.setSelectedIndex(value);
                case INDI_PORT -> indiPortSpinner.setValue(value);
                case FOK_TICKS_COUNT -> {
                    fokTicksCountSpinner.setValue(value);
                    updateSlidersLimit();
                }
                case FOK_MAX_TRAVEL -> {
                    fokMaxTravelSpinner.setValue(value);
                    updateSlidersLimit();
                }
            }
        });
    }

    @Override
    public void update(Settings.Value what, String value) {
        if (what == Settings.Value.SERIAL_PORT) {
            SwingUtilities.invokeLater(() -> serialPortComboBox.setSelectedItem(value));
        }
    }

    @Override
    public void update(Settings.Value what, Settings.Units value) {
        if (what == Settings.Value.FOK_TICKS_UNIT) {
            SwingUtilities.invokeLater(() -> {
                fokUnitsCombo.setSelectedItem(value);
                updateUnitsLabel();
            });
        }
    }

    @Override
    public void update(Settings.Value what, boolean value) {
        SwingUtilities.invokeLater(() -> {
            switch (what) {
                case IS_INDI_ENABLED -> {
                    enableINDIServerBox.setSelected(value);
                    indiStatusLabel.setText(Main.indiServerCreator.isRunning() ? "Server avviato" : "Server non attivo");
                }
                case SHOW_REMOTE_INDI -> localOrRemoteCombo.setSelectedItem(value ? 1 : 0);
            }
        });
    }

    @Override
    public void update(Settings.Value what, PowerBox value) {

    }
}