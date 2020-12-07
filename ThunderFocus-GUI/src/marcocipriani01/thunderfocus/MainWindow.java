package marcocipriani01.thunderfocus;

import marcocipriani01.simplesocket.ConnectionException;
import marcocipriani01.thunderfocus.board.ArduinoPin;
import marcocipriani01.thunderfocus.board.PowerBox;
import marcocipriani01.thunderfocus.board.ThunderFocuser;
import marcocipriani01.thunderfocus.indi.INDIThunderFocuserDriver;
import marcocipriani01.thunderfocus.io.SerialPortImpl;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.plot.dial.*;
import org.jfree.chart.renderer.xy.XYSplineRenderer;
import org.jfree.chart.ui.GradientPaintTransformType;
import org.jfree.chart.ui.StandardGradientPaintTransformer;
import org.jfree.data.general.DefaultValueDataset;
import org.jfree.data.general.ValueDataset;
import org.jfree.data.time.Second;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.HyperlinkEvent;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.*;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Date;

import static marcocipriani01.thunderfocus.Main.APP_NAME;
import static marcocipriani01.thunderfocus.Settings.ExternalControl.*;

public class MainWindow extends JFrame implements
        ChangeListener, ActionListener, KeyListener, FocusListener,
        ThunderFocuser.Listener, Settings.SettingsListener {

    private static final ImageIcon POWERBOX_TAB =
            new ImageIcon(MainWindow.class.getResource("/marcocipriani01/thunderfocus/res/powerboxtab.png"));
    private static final ImageIcon AMBIENT_TAB =
            new ImageIcon(MainWindow.class.getResource("/marcocipriani01/thunderfocus/res/ambienttab.png"));
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
    private ChartPanel tempChartPanel;
    private ChartPanel humChartPanel;
    private ChartPanel dewPointChartPanel;
    private JPanel ambientTab;
    private JPanel powerBoxConfigPanel;
    private JSpinner powerBoxLatSpinner;
    private JSpinner powerBoxLongSpinner;
    private JTextField sunElevationField;
    private JLabel powerBoxAutoModeLabel;
    private JLabel sunElevationLabel;
    private ChartPanel timeSensorsChart;
    private JButton cleanGraphButton;
    private JLabel ascomStatusLabel;
    private JLabel versionLabel;
    private JRadioButton indiServerRadio;
    private JRadioButton ascomBridgeRadio;
    private JRadioButton disableExtControlRadio;
    private JSpinner ascomPortSpinner;
    private JScrollPane configScrollPane;
    private DefaultValueDataset tempDataset;
    private DefaultValueDataset humidityDataset;
    private DefaultValueDataset dewPointDataset;
    private TimeSeries tempSeries;
    private TimeSeries dewPointSeries;
    private TimeSeries humiditySeries;

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
        removeTab(powerBoxTab);
        removeTab(ambientTab);
        tabPane.addChangeListener(this);
        configScrollPane.getVerticalScrollBar().setUnitIncrement(10);

        String version = Main.getAppVersion();
        if (version == null) {
            versionLabel.setText("Versione sconosciuta!");
        } else {
            versionLabel.setText("v" + version + " ");
        }
        aboutLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        aboutLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                Main.openBrowser("https://marcocipriani01.github.io", MainWindow.this);
            }
        });
        infoPane.setCaretPosition(0);
        setKeyListeners(parent, setRequestedPosButton, setZeroButton, fokInButton, fokOutButton, miniWindowButton,
                stopButton, requestedPosField, aboutLabel, currentPosField, ticksPosSlider, posSlider,
                relativeMovField, pinWindowButton);
        setButtonListeners(connectButton, refreshButton, setRequestedPosButton, fokBacklashCalButton,
                setZeroButton, fokInButton, fokOutButton, miniWindowButton, stopButton,
                applyPowerBoxButton, copyIndiDriverNameButton, saveConfigButton,
                powerBoxOnButton, powerBoxOffButton, cleanGraphButton);
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
        Settings.ExternalControl externalControl = Main.settings.getExternalControl();
        ascomBridgeRadio.setSelected(externalControl == ASCOM);
        disableExtControlRadio.setSelected(externalControl == NONE);
        indiServerRadio.setSelected(externalControl == INDI);

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
                    JOptionPane.showMessageDialog(MainWindow.this,
                            "Errore durante l'apertura del browser!", APP_NAME, JOptionPane.ERROR_MESSAGE);
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
        setBounds(350, 150, 750, 770);
        setVisible(true);
    }

    public JFreeChart createStandardDialChart(String title, ValueDataset dataset,
                                              double lowerBound, double upperBound) {
        DialPlot dialplot = new DialPlot();
        dialplot.setDataset(dataset);
        dialplot.setDialFrame(new StandardDialFrame());
        dialplot.setBackground(new DialBackground());
        DialTextAnnotation dialtextannotation = new DialTextAnnotation(title);
        dialtextannotation.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
        dialtextannotation.setRadius(0.55D);
        dialplot.addLayer(dialtextannotation);
        DialValueIndicator dialvalueindicator = new DialValueIndicator(0);
        dialvalueindicator.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
        dialplot.addLayer(dialvalueindicator);
        StandardDialScale standarddialscale = new StandardDialScale(lowerBound, upperBound, -140D, -260D, 10D, 9);
        standarddialscale.setTickRadius(0.88D);
        standarddialscale.setTickLabelOffset(0.15);
        standarddialscale.setTickLabelFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        dialplot.addScale(0, standarddialscale);
        dialplot.addPointer(new DialPointer.Pin());
        DialCap dialcap = new DialCap();
        dialplot.setCap(dialcap);
        return new JFreeChart(dialplot);
    }

    public JFreeChart createTemperatureDialChart(String title,
                                                 ValueDataset dataset, Color baseColor, Color pointerColor) {
        JFreeChart jfreechart = createStandardDialChart(title, dataset, -20D, 50D);
        DialPlot dialplot = (DialPlot) jfreechart.getPlot();
        StandardDialRange range1 = new StandardDialRange(-20D, -5D, Color.RED);
        range1.setInnerRadius(0.5D);
        dialplot.addLayer(range1);
        StandardDialRange range2 = new StandardDialRange(-5, 5D, Color.ORANGE);
        range2.setInnerRadius(0.5D);
        dialplot.addLayer(range2);
        StandardDialRange range3 = new StandardDialRange(0D, 38D, Color.GREEN);
        range3.setInnerRadius(0.5D);
        dialplot.addLayer(range3);
        StandardDialRange range4 = new StandardDialRange(38D, 50D, Color.RED);
        range4.setInnerRadius(0.5D);
        dialplot.addLayer(range4);
        GradientPaint gradientpaint = new GradientPaint(new Point(), new Color(255, 255, 255), new Point(), baseColor);
        DialBackground dialbackground = new DialBackground(gradientpaint);
        dialbackground.setGradientPaintTransformer(
                new StandardGradientPaintTransformer(GradientPaintTransformType.VERTICAL));
        dialplot.setBackground(dialbackground);
        dialplot.removePointer(0);
        DialPointer.Pointer pointer = new DialPointer.Pointer();
        pointer.setFillPaint(pointerColor);
        dialplot.addPointer(pointer);
        return jfreechart;
    }

    public JFreeChart createHumidityDialChart(String title, ValueDataset dataset, Color baseColor, Color pointerColor) {
        JFreeChart jfreechart = createStandardDialChart(title, dataset, 0D, 100D);
        DialPlot dialplot = (DialPlot) jfreechart.getPlot();
        StandardDialRange range0 = new StandardDialRange(0D, 80D, Color.GREEN);
        range0.setInnerRadius(0.5D);
        dialplot.addLayer(range0);
        StandardDialRange range1 = new StandardDialRange(80D, 90D, Color.ORANGE);
        range1.setInnerRadius(0.5D);
        dialplot.addLayer(range1);
        StandardDialRange range2 = new StandardDialRange(90D, 100D, Color.RED);
        range2.setInnerRadius(0.5D);
        dialplot.addLayer(range2);
        GradientPaint gradientpaint = new GradientPaint(new Point(), new Color(255, 255, 255), new Point(), baseColor);
        DialBackground dialbackground = new DialBackground(gradientpaint);
        dialbackground.setGradientPaintTransformer(
                new StandardGradientPaintTransformer(GradientPaintTransformType.VERTICAL));
        dialplot.setBackground(dialbackground);
        dialplot.removePointer(0);
        DialPointer.Pointer pointer = new DialPointer.Pointer();
        pointer.setFillPaint(pointerColor);
        dialplot.addPointer(pointer);
        return jfreechart;
    }

    private void createUIComponents() {
        indiPortSpinner = new JSpinner(new SpinnerNumberModel(
                Main.settings.getIndiServerPort(), 1024, 99999, 1));
        ascomPortSpinner = new JSpinner(new SpinnerNumberModel(
                Main.settings.getAscomBridgePort(), 1024, 99999, 1));
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
        powerBoxLatSpinner = new JSpinner(new SpinnerNumberModel(0.0, -180.0, 180.0, 0.001));
        powerBoxLongSpinner = new JSpinner(new SpinnerNumberModel(0.0, -180.0, 180.0, 0.001));

        tempDataset = new DefaultValueDataset(-20D);
        tempChartPanel = new ChartPanel(createTemperatureDialChart("Temperatura", tempDataset,
                new Color(255, 82, 82), new Color(41, 182, 246)));
        tempChartPanel.setPreferredSize(new Dimension(240, 220));
        humidityDataset = new DefaultValueDataset(0D);
        humChartPanel = new ChartPanel(createHumidityDialChart("Umidità", humidityDataset,
                new Color(38, 166, 154), new Color(255, 145, 0)));
        humChartPanel.setPreferredSize(new Dimension(240, 220));
        dewPointDataset = new DefaultValueDataset(-20D);
        dewPointChartPanel = new ChartPanel(createTemperatureDialChart("P.to di rugiada",
                dewPointDataset, new Color(68, 138, 255), new Color(244, 67, 54)));
        dewPointChartPanel.setPreferredSize(new Dimension(240, 220));

        TimeSeriesCollection tempGraphDataset = new TimeSeriesCollection();
        tempSeries = new TimeSeries("Temperatura");
        tempGraphDataset.addSeries(tempSeries);
        dewPointSeries = new TimeSeries("P.to di rugiada");
        tempGraphDataset.addSeries(dewPointSeries);
        TimeSeriesCollection humGraphDataset = new TimeSeriesCollection();
        humiditySeries = new TimeSeries("Umidità");
        humGraphDataset.addSeries(humiditySeries);
        XYPlot plot = new XYPlot();
        plot.setDataset(0, tempGraphDataset);
        plot.setDataset(1, humGraphDataset);
        XYSplineRenderer tempRend = new XYSplineRenderer();
        tempRend.setSeriesShapesVisible(0, false);
        tempRend.setSeriesShapesVisible(1, false);
        plot.setRenderer(0, tempRend);
        XYSplineRenderer humRend = new XYSplineRenderer();
        humRend.setSeriesShapesVisible(0, false);
        plot.setRenderer(1, humRend);
        plot.setRangeAxis(0, new NumberAxis("Temp. e p.to di rugiada (°C)"));
        NumberAxis humAxis = new NumberAxis("Umidità (%)");
        humAxis.setRange(0.0, 100.0);
        plot.setRangeAxis(1, humAxis);
        plot.setDomainAxis(new DateAxis());
        plot.mapDatasetToRangeAxis(0, 0);
        plot.mapDatasetToRangeAxis(1, 1);
        timeSensorsChart = new ChartPanel(new JFreeChart(plot));
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
        if (Main.isAscomRunning() && (Main.OPERATING_SYSTEM == Main.OperatingSystem.WINDOWS)) {
            try {
                setState(Frame.ICONIFIED);
                SystemTray tray = SystemTray.getSystemTray();
                TrayIcon trayIcon = new TrayIcon(Main.APP_LOGO, APP_NAME);
                trayIcon.setImageAutoSize(true);
                trayIcon.setToolTip(APP_NAME);
                trayIcon.addActionListener(e -> {
                    tray.remove(trayIcon);
                    setState(Frame.NORMAL);
                    toFront();
                    requestFocus();
                });
                tray.add(trayIcon);
                trayIcon.displayMessage(APP_NAME, "Ponte ASCOM in background.", TrayIcon.MessageType.INFO);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            setState(Frame.NORMAL);
            toFront();
            requestFocus();
            if (JOptionPane.showConfirmDialog(this,
                    "Uscire dall'applicazione?\nQuesta operazione terminerà il server\nINDI e il ponte ASCOM.",
                    APP_NAME, JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
                Main.focuser.removeListener(this);
                dispose();
                Main.exit(0);
            }
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
            if (Main.focuser.isConnected()) {
                Main.focuser.disconnect();
            } else if (serialPortComboBox.getSelectedItem() != null) {
                String port = (String) serialPortComboBox.getSelectedItem();
                Main.settings.setSerialPort(port, this);
                try {
                    Main.focuser.connect(port);
                } catch (ConnectionException ex) {
                    connectionErr(ex);
                }
            } else {
                JOptionPane.showMessageDialog(this, "Nessuna porta disponibile o selezionata.",
                        APP_NAME, JOptionPane.ERROR_MESSAGE);
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
                Main.focuser.run(ThunderFocuser.Commands.FOK1_REL_MOVE, this,
                        Integer.parseInt(relativeMovField.getText()));
            } catch (ConnectionException ex) {
                connectionErr(ex);
            } catch (ThunderFocuser.InvalidParamException | NumberFormatException ex) {
                valueOutOfLimits(ex);
            }

        } else if (source == fokInButton) {
            try {
                Main.focuser.run(ThunderFocuser.Commands.FOK1_REL_MOVE, this,
                        -Integer.parseInt(relativeMovField.getText()));
            } catch (NumberFormatException ignored) {
            } catch (ConnectionException | ThunderFocuser.InvalidParamException connectionException) {
                connectionException.printStackTrace();
            }

        } else if (source == setRequestedPosButton || source == requestedPosField) {
            try {
                Main.focuser.run(ThunderFocuser.Commands.FOK1_ABS_MOVE, this,
                        Integer.parseInt(requestedPosField.getText()));
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
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(
                    new StringSelection(driverNameBox.getText()), null);

        } else if (source == fokBacklashCalButton) {
            if (JOptionPane.showConfirmDialog(this,
                    "Procedere? Attenzione: il focheggiatore verrà mosso, assicurarsi che " +
                            "questo non interrompa eventuali sessioni osservative. " +
                            "Inoltre, è molto importante che i limiti del focheggiatore siano ben impostati.",
                    APP_NAME, JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.OK_OPTION) {
                new BacklashCalibrationWindow(this);
            }

        } else if (source == saveConfigButton) {
            Main.settings.setTheme(appThemeCombo.getSelectedIndex(), this);
            if (indiServerRadio.isSelected()) {
                Main.settings.setExternalControl(INDI, this);
            } else if (ascomBridgeRadio.isSelected()) {
                Main.settings.setExternalControl(ASCOM, this);
            } else {
                Main.settings.setExternalControl(NONE, this);
            }
            Main.settings.setShowRemoteIndi(localOrRemoteCombo.getSelectedIndex() == 1, this);
            int oldIndiPort = Main.settings.getIndiServerPort();
            Main.settings.setIndiServerPort((int) indiPortSpinner.getValue(), this);
            int oldAscomPort = Main.settings.getAscomBridgePort();
            Main.settings.setAscomBridgePort((int) ascomPortSpinner.getValue(), this);
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
                    if (Main.focuser.isPowerBox() && Main.focuser.getPowerBox().supportsAmbient()) {
                        Main.focuser.run(ThunderFocuser.Commands.SET_TIME_LAT_LONG, this, 0,
                                (int) (((double) powerBoxLatSpinner.getValue()) * 1000),
                                (int) (((double) powerBoxLongSpinner.getValue()) * 1000));
                    }
                } catch (ConnectionException ex) {
                    connectionErr(ex);
                } catch (ThunderFocuser.InvalidParamException | NumberFormatException ex) {
                    valueOutOfLimits(ex);
                }
            }
            startOrStopINDI(Main.settings.getIndiServerPort() != oldIndiPort);
            if (Main.focuser.isConnected()) {
                startOrStopASCOM(Main.settings.getAscomBridgePort() != oldAscomPort);
            }
            try {
                Main.settings.save();
            } catch (IOException ioException) {
                ioException.printStackTrace();
                JOptionPane.showMessageDialog(this, "Impossibile salvare la configurazione!",
                        APP_NAME, JOptionPane.ERROR_MESSAGE);
            }

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
                    if (!p.isAutoModeEn()) {
                        p.setValue(255);
                        Main.focuser.run(ThunderFocuser.Commands.POWER_BOX_SET, this, p.getNumber(), p.getValuePwm());
                    }
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
                    if (!p.isAutoModeEn()) {
                        p.setValue(0);
                        Main.focuser.run(ThunderFocuser.Commands.POWER_BOX_SET, this, p.getNumber(), p.getValuePwm());
                    }
                }
                powerBoxTable.refresh();
            } catch (ConnectionException ex) {
                connectionErr(ex);
            } catch (ThunderFocuser.InvalidParamException ex) {
                ex.printStackTrace();
            }

        } else if (source == cleanGraphButton) {
            tempSeries.clear();
            dewPointSeries.clear();
            humiditySeries.clear();
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
        if (Main.settings.getExternalControl() == INDI) {
            Main.indiServerCreator.start(Main.settings.getIndiServerPort(), forceRestart);
            indiStatusLabel.setText("Server avviato");
        } else {
            Main.indiServerCreator.stop();
            indiStatusLabel.setText("Server non attivo");
        }
    }

    private void startOrStopASCOM(boolean forceRestart) {
        try {
            if (Main.settings.getExternalControl() == ASCOM) {
                if (Main.isAscomRunning()) {
                    if (forceRestart) {
                        Main.ascomFocuserBridge.close();
                        Main.ascomFocuserBridge = new ASCOMFocuserBridge(Main.settings.getAscomBridgePort());
                        Main.ascomFocuserBridge.connect();
                    }
                } else {
                    Main.ascomFocuserBridge = new ASCOMFocuserBridge(Main.settings.getAscomBridgePort());
                    Main.ascomFocuserBridge.connect();
                }
                ascomStatusLabel.setText("Ponte attivo");
            } else {
                Main.ascomFocuserBridge.close();
                ascomStatusLabel.setText("Ponte non attivo");
            }
        } catch (ConnectionException e) {
            onCriticalError(e);
            ascomStatusLabel.setText("Errore!");
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
                Main.focuser.run(ThunderFocuser.Commands.FOK1_ABS_MOVE,
                        this, Main.focuser.ticksToSteps(ticksPosSlider.getValue()));
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
                case REQUESTED_REL_POS -> relativeMovField.setText(
                        String.valueOf(Math.abs(Main.focuser.getRequestedRelPos())));
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
                case POWERBOX_AUTO_MODE -> powerBoxAutoModeBox.setSelectedItem(Main.focuser.getPowerBox().getAutoMode());
                case POWERBOX_AMBIENT_DATA -> {
                    PowerBox powerBox = Main.focuser.getPowerBox();
                    double temperature = powerBox.getTemperature();
                    Second instant = new Second(new Date());
                    if (temperature == PowerBox.ABSOLUTE_ZERO) {
                        tempDataset.setValue(-20D);
                    } else {
                        tempDataset.setValue(temperature);
                        tempSeries.add(instant, temperature);
                    }
                    double humidity = powerBox.getHumidity();
                    if (humidity == PowerBox.INVALID_HUMIDITY) {
                        humidityDataset.setValue(0D);
                    } else {
                        humidityDataset.setValue(humidity);
                        humiditySeries.add(instant, humidity);
                    }
                    double dewPoint = powerBox.getDewPoint();
                    if (dewPoint == PowerBox.ABSOLUTE_ZERO) {
                        dewPointDataset.setValue(-20D);
                    } else {
                        dewPointDataset.setValue(dewPoint);
                        dewPointSeries.add(instant, dewPoint);
                    }
                }
                case POWERBOX_SUN_ELEV -> sunElevationField.setText(Main.focuser.getPowerBox().getSunElev() + "°");
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
                        tabPane.insertTab("Power box", POWERBOX_TAB, powerBoxTab, "", 1);
                        PowerBox powerBox = Main.focuser.getPowerBox();
                        powerBoxTable.setPowerBox(powerBox);
                        boolean supportsAutoModes = powerBox.supportsAutoModes();
                        powerBoxAutoModeLabel.setVisible(supportsAutoModes);
                        powerBoxAutoModeBox.setVisible(supportsAutoModes);
                        if (supportsAutoModes) {
                            powerBoxAutoModeBox.setModel(new DefaultComboBoxModel<>(powerBox.supportedAutoModesArray()));
                            powerBoxAutoModeBox.setSelectedItem(Main.focuser.getPowerBox().getAutoMode());
                        }
                        if (powerBox.supportsAmbient()) {
                            tabPane.insertTab("Sensori", AMBIENT_TAB, ambientTab, "", 2);
                            powerBoxLatSpinner.setValue(powerBox.getLatitude());
                            powerBoxLongSpinner.setValue(powerBox.getLongitude());
                            powerBoxConfigPanel.setVisible(true);
                        }
                        boolean supportsTime = powerBox.supportsTime();
                        sunElevationLabel.setVisible(supportsTime);
                        sunElevationField.setVisible(supportsTime);
                        sunElevationField.setText("?");
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
                    startOrStopASCOM(false);
                    break;
                }
                case DISCONNECTED: {
                    if (Main.isAscomRunning()) {
                        try {
                            Main.ascomFocuserBridge.close();
                            ascomStatusLabel.setText("Ponte non attivo");
                        } catch (ConnectionException ex) {
                            onCriticalError(ex);
                            ascomStatusLabel.setText("Errore!");
                        }
                    }
                    powerBoxConfigPanel.setVisible(false);
                    removeTab(ambientTab);
                    removeTab(powerBoxTab);
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

    private void removeTab(Component tab) {
        int i = tabPane.indexOfComponent(tab);
        if (i != -1) {
            tabPane.removeTabAt(i);
        }
    }

    @Override
    public void onCriticalError(Exception e) {
        SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(MainWindow.this,
                "Errore inaspettato!", APP_NAME, JOptionPane.ERROR_MESSAGE));
    }

    @Override
    public void updateSetting(Settings.Value what, int value) {
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
    public void updateSetting(Settings.Value what, String value) {
        if (what == Settings.Value.SERIAL_PORT) {
            SwingUtilities.invokeLater(() -> serialPortComboBox.setSelectedItem(value));
        }
    }

    @Override
    public void updateSetting(Settings.Value what, boolean value) {
        if (what == Settings.Value.SHOW_REMOTE_INDI) SwingUtilities.invokeLater(() ->
                localOrRemoteCombo.setSelectedItem(value ? 1 : 0));
    }

    @Override
    public void updateSetting(Settings.Units value) {
        SwingUtilities.invokeLater(() -> {
            fokUnitsCombo.setSelectedItem(value);
            updateUnitsLabel();
        });
    }

    @Override
    public void updateSetting(PowerBox value) {

    }

    @Override
    public void updateSetting(Settings.ExternalControl value) {
        ascomBridgeRadio.setSelected(value == ASCOM);
        disableExtControlRadio.setSelected(value == NONE);
        indiServerRadio.setSelected(value == INDI);
        indiStatusLabel.setText(Main.indiServerCreator.isRunning() ? "Server avviato" : "Server non attivo");
    }
}