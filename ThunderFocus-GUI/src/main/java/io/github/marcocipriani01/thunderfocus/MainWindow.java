package io.github.marcocipriani01.thunderfocus;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatIntelliJLaf;
import com.formdev.flatlaf.IntelliJTheme;
import io.github.marcocipriani01.thunderfocus.ascom.ASCOMBridge;
import io.github.marcocipriani01.thunderfocus.board.*;
import io.github.marcocipriani01.thunderfocus.config.ExportableSettings;
import io.github.marcocipriani01.thunderfocus.config.Settings;
import io.github.marcocipriani01.thunderfocus.indi.INDIThunderFocusDriver;
import io.github.marcocipriani01.thunderfocus.io.SerialPortImpl;
import jssc.SerialPortException;
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
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Objects;
import java.util.stream.IntStream;

import static io.github.marcocipriani01.thunderfocus.Main.*;

public class MainWindow extends JFrame implements
        ChangeListener, ActionListener, KeyListener, FocusListener,
        Board.Listener, Settings.SettingsListener, ItemListener {

    private static final ImageIcon POWERBOX_TAB =
            new ImageIcon(Objects.requireNonNull(MainWindow.class.getResource("/io/github/marcocipriani01/thunderfocus/res/power_box_tab.png")));
    private static final ImageIcon AMBIENT_TAB =
            new ImageIcon(Objects.requireNonNull(MainWindow.class.getResource("/io/github/marcocipriani01/thunderfocus/res/ambient_tab.png")));
    private static final ImageIcon FLAT_PANEL_TAB =
            new ImageIcon(Objects.requireNonNull(MainWindow.class.getResource("/io/github/marcocipriani01/thunderfocus/res/flat_tab.png")));
    private final MiniWindow miniWindow = new MiniWindow();
    private final PresetsTableModel presetsTableModel;
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
    private JComboBox<Settings.Theme> appThemeCombo;
    private JSpinner fokTicksCountSpinner;
    private JSpinner fokMaxTravelSpinner;
    private JComboBox<Settings.Units> fokUnitsCombo;
    private JSpinner fokBacklashSpinner;
    private JButton fokBacklashCalButton;
    private JSlider fokSpeedSlider;
    private JCheckBox fokPowerSaverBox;
    private JCheckBox fokReverseDirBox;
    private JPowerBoxTable powerBoxTable;
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
    @SuppressWarnings("unused")
    private ChartPanel timeSensorsChart;
    private JButton cleanGraphButton;
    private JLabel ascomStatusLabel;
    private JLabel versionLabel;
    private JCheckBox indiServerCheckBox;
    private JCheckBox ascomBridgeCheckBox;
    private JSpinner ascomPortSpinner;
    private JScrollPane configScrollPane;
    private JCheckBox autoConnectBox;
    private JTable presetsTable;
    private JButton addPresetButton;
    private JButton deletePresetButton;
    private JButton goToPresetButton;
    private JButton exportButton;
    private JButton importButton;
    private JLabel appNameLabel;
    private JRadioButton closeCoverRadio;
    private JRadioButton lightOnRadio;
    private JRadioButton lightOffRadio;
    private JRadioButton openCoverRadio;
    private JLabel coverStatusLabel;
    private JSlider coverSpeedSlider;
    private JSpinner openAngleSpinner;
    private JSpinner closedAngleSpinner;
    private JSlider brightnessSlider;
    private JButton saveServoConfig;
    private JPanel flatPanelTab;
    private JLabel powerboxLockLabel;
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
        removeTab(flatPanelTab);
        tabPane.addChangeListener(this);
        configScrollPane.getVerticalScrollBar().setUnitIncrement(10);

        String version = Main.getAppVersion();
        if (version == null) {
            versionLabel.setText(i18n("version.unknown"));
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
        miniWindow.setMainWindow(this);
        setKeyListeners(parent, setRequestedPosButton, setZeroButton, fokInButton, fokOutButton, miniWindowButton,
                stopButton, aboutLabel, pinWindowButton);
        setButtonListeners(connectButton, refreshButton, setRequestedPosButton, fokBacklashCalButton,
                setZeroButton, fokInButton, fokOutButton, miniWindowButton, stopButton, copyIndiDriverNameButton,
                saveConfigButton, powerBoxOnButton, powerBoxOffButton, cleanGraphButton, addPresetButton,
                deletePresetButton, goToPresetButton, importButton, exportButton,
                saveServoConfig, lightOnRadio, lightOffRadio, openCoverRadio, closeCoverRadio);
        pinWindowButton.addActionListener(this);
        requestedPosField.addActionListener(this);
        relativeMovField.setText(String.valueOf(settings.relativeStepSize));
        relativeMovField.addActionListener(this);
        updateSlidersLimit();
        updateUnitsLabel();
        localOrRemoteCombo.setSelectedItem(i18n(settings.showIpIndiDriver ? "remote" : "local"));
        localOrRemoteCombo.addItemListener(e -> refreshDriverName());
        refreshDriverName();
        appThemeCombo.setSelectedItem(settings.theme);
        ascomBridgeCheckBox.setSelected(settings.ascomBridge);
        indiServerCheckBox.setSelected(settings.indiServer);
        brightnessSlider.addChangeListener(this);

        parent.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                super.mouseEntered(e);
                parent.requestFocus();
            }
        });
        infoPane.addHyperlinkListener(e -> {
            if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) Main.openBrowser(e, this);
        });

        JTableHeader tableHeader = presetsTable.getTableHeader();
        tableHeader.setResizingAllowed(false);
        tableHeader.setReorderingAllowed(false);
        presetsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        presetsTable.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
        DefaultTableCellRenderer presetTableRenderer = new DefaultTableCellRenderer();
        presetTableRenderer.setHorizontalAlignment(JLabel.LEFT);
        presetsTableModel = new PresetsTableModel();
        presetsTable.setModel(presetsTableModel);
        presetsTable.getColumnModel().getColumn(0).setCellRenderer(presetTableRenderer);
        presetsTable.setRowHeight(30);

        boolean autoConnect = settings.autoConnect;
        autoConnectBox.setSelected(autoConnect);
        boolean selectPort = false;
        String serialPort = settings.getSerialPort();
        for (String p : SerialPortImpl.scanSerialPorts()) {
            serialPortComboBox.addItem(p);
            if (p.equals(serialPort)) selectPort = true;
        }
        if (selectPort) {
            serialPortComboBox.setSelectedItem(serialPort);
            if (autoConnect && (!board.isConnected())) {
                try {
                    board.connect(serialPort);
                } catch (SerialPortException ex) {
                    ex.printStackTrace();
                }
            }
        }

        board.addListener(this);
        settings.addListener(this);
        startOrStopINDI(false);

        setResizable(true);
        setBounds(350, 150, 790, 800);
        setMinimumSize(new Dimension(770, 700));
        setVisible(true);
    }

    public JFreeChart createStandardDialChart(
            String title, ValueDataset dataset, double lowerBound, double upperBound) {
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
        StandardDialScale standarddialscale = new StandardDialScale(lowerBound, upperBound,
                -140D, -260D, 10D, 9);
        standarddialscale.setTickRadius(0.88D);
        standarddialscale.setTickLabelOffset(0.15);
        standarddialscale.setTickLabelFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        dialplot.addScale(0, standarddialscale);
        dialplot.addPointer(new DialPointer.Pin());
        DialCap dialcap = new DialCap();
        dialplot.setCap(dialcap);
        return new JFreeChart(dialplot);
    }

    private JFreeChart createTemperatureDialChart(
            String title, ValueDataset dataset, Color baseColor, Color pointerColor) {
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
        setupJFreeChart(baseColor, pointerColor, dialplot);
        return jfreechart;
    }

    private JFreeChart createHumidityDialChart(
            String title, ValueDataset dataset, Color baseColor, Color pointerColor) {
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
        setupJFreeChart(baseColor, pointerColor, dialplot);
        return jfreechart;
    }

    private void setupJFreeChart(Color baseColor, Color pointerColor, DialPlot dialPlot) {
        GradientPaint gradientPaint = new GradientPaint(new Point(), new Color(255, 255, 255), new Point(), baseColor);
        DialBackground dialBackground = new DialBackground(gradientPaint);
        dialBackground.setGradientPaintTransformer(
                new StandardGradientPaintTransformer(GradientPaintTransformType.VERTICAL));
        dialPlot.setBackground(dialBackground);
        dialPlot.removePointer(0);
        DialPointer.Pointer pointer = new DialPointer.Pointer();
        pointer.setFillPaint(pointerColor);
        dialPlot.addPointer(pointer);
    }

    private void createUIComponents() {
        appThemeCombo = new JComboBox<>(Settings.Theme.values());
        localOrRemoteCombo = new JComboBox<>(new String[]{i18n("local"), i18n("remote")});
        indiPortSpinner = new JSpinner(new SpinnerNumberModel(
                settings.indiServerPort, 1024, 65535, 1));
        indiPortSpinner.setEditor(new JSpinner.NumberEditor(indiPortSpinner, "#"));
        ascomPortSpinner = new JSpinner(new SpinnerNumberModel(
                settings.ascomBridgePort, 1024, 65535, 1));
        ascomPortSpinner.setEditor(new JSpinner.NumberEditor(ascomPortSpinner, "#"));
        fokTicksCountSpinner = new JSpinner(new SpinnerNumberModel(
                settings.focuserTicksCount, 10, 2147483647, 1));
        fokTicksCountSpinner.setEditor(new JSpinner.NumberEditor(fokTicksCountSpinner, "#"));
        fokUnitsCombo = new JComboBox<>(Settings.Units.values());
        fokUnitsCombo.setSelectedItem(settings.focuserTicksUnit);
        fokMaxTravelSpinner = new JSpinner(new SpinnerNumberModel(
                settings.getFocuserMaxTravel(), 1, 2147483647, 1));
        fokMaxTravelSpinner.setEditor(new JSpinner.NumberEditor(fokMaxTravelSpinner, "#"));
        fokBacklashSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 1000, 1));
        fokBacklashSpinner.setEditor(new JSpinner.NumberEditor(fokBacklashSpinner, "#"));
        powerBoxTable = new JPowerBoxTable(this);
        powerBoxAutoModeBox = new JComboBox<>();
        powerBoxAutoModeBox.addItemListener(this);
        powerBoxLatSpinner = new JSpinner(new SpinnerNumberModel(0.0, -180.0, 180.0, 0.001));
        powerBoxLongSpinner = new JSpinner(new SpinnerNumberModel(0.0, -180.0, 180.0, 0.001));

        tempDataset = new DefaultValueDataset(-20D);
        tempChartPanel = new ChartPanel(createTemperatureDialChart(i18n("temperature"), tempDataset,
                new Color(255, 82, 82), new Color(41, 182, 246)));
        tempChartPanel.setPreferredSize(new Dimension(240, 220));
        humidityDataset = new DefaultValueDataset(0D);
        humChartPanel = new ChartPanel(createHumidityDialChart(i18n("humidity"), humidityDataset,
                new Color(38, 166, 154), new Color(255, 145, 0)));
        humChartPanel.setPreferredSize(new Dimension(240, 220));
        dewPointDataset = new DefaultValueDataset(-20D);
        dewPointChartPanel = new ChartPanel(createTemperatureDialChart(i18n("dew.point"),
                dewPointDataset, new Color(68, 138, 255), new Color(244, 67, 54)));
        dewPointChartPanel.setPreferredSize(new Dimension(240, 220));

        TimeSeriesCollection tempGraphDataset = new TimeSeriesCollection();
        tempSeries = new TimeSeries(i18n("temperature"));
        tempGraphDataset.addSeries(tempSeries);
        dewPointSeries = new TimeSeries(i18n("dew.point"));
        tempGraphDataset.addSeries(dewPointSeries);
        TimeSeriesCollection humGraphDataset = new TimeSeriesCollection();
        humiditySeries = new TimeSeries(i18n("humidity"));
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
        plot.setRangeAxis(0, new NumberAxis(i18n("temp.dew.point")));
        NumberAxis humAxis = new NumberAxis(i18n("humidity.percentage"));
        humAxis.setRange(0.0, 100.0);
        plot.setRangeAxis(1, humAxis);
        plot.setDomainAxis(new DateAxis());
        plot.mapDatasetToRangeAxis(0, 0);
        plot.mapDatasetToRangeAxis(1, 1);
        timeSensorsChart = new ChartPanel(new JFreeChart(plot));

        openAngleSpinner = new JSpinner(new SpinnerNumberModel(250, 170, 290, 1));
        closedAngleSpinner = new JSpinner(new SpinnerNumberModel(0, -15, 15, 1));
    }

    private void refreshDriverName() {
        try {
            driverNameBox.setText(INDIThunderFocusDriver.DRIVER_NAME + "@" +
                    Main.getIP(localOrRemoteCombo.getSelectedIndex() == 0) + ":" + settings.indiServerPort);
        } catch (Exception e) {
            e.printStackTrace();
            driverNameBox.setText(i18n("ip.error"));
        }
    }

    private void askClose() {
        if ((Main.OPERATING_SYSTEM == Main.OperatingSystem.WINDOWS) &&
                Main.isAscomRunning() && (Main.ascomBridge.getClientsCount() > 0)) {
            try {
                setVisible(false);
                SystemTray tray = SystemTray.getSystemTray();
                TrayIcon trayIcon = new TrayIcon(Main.APP_LOGO, APP_NAME);
                trayIcon.setImageAutoSize(true);
                trayIcon.setToolTip(APP_NAME);
                trayIcon.addMouseListener(new MouseListener() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        tray.remove(trayIcon);
                        setVisible(true);
                        setState(Frame.NORMAL);
                        toFront();
                        requestFocus();
                    }

                    @Override
                    public void mousePressed(MouseEvent e) {

                    }

                    @Override
                    public void mouseReleased(MouseEvent e) {

                    }

                    @Override
                    public void mouseEntered(MouseEvent e) {

                    }

                    @Override
                    public void mouseExited(MouseEvent e) {

                    }
                });
                tray.add(trayIcon);
                trayIcon.displayMessage(APP_NAME, i18n("ascom.background"), TrayIcon.MessageType.INFO);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            setState(Frame.NORMAL);
            toFront();
            requestFocus();
            String msg = i18n("exit.app");
            if (Main.isAscomRunning() || Main.indiServerCreator.isRunning()) {
                msg += i18n("exit.app.warning");
            }
            if (JOptionPane.showConfirmDialog(this, msg,
                    APP_NAME, JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
                board.removeListener(this);
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

    private void setButtonListeners(AbstractButton... buttons) {
        for (AbstractButton b : buttons) {
            b.addActionListener(this);
        }
    }

    private void relativeFocuserMove(boolean invert) {
        try {
            int stepSize = Math.abs(Integer.parseInt(relativeMovField.getText().trim()));
            relativeMovField.setText(String.valueOf(stepSize));
            settings.relativeStepSize = stepSize;
            if (invert) stepSize *= -1;
            board.run(Board.Commands.FOCUSER_REL_MOVE, this, stepSize);
        } catch (IOException | SerialPortException ex) {
            connectionErr(ex);
        } catch (IllegalArgumentException ex) {
            valueOutOfLimits(ex);
        }
        try {
            settings.save();
        } catch (IOException e) {
            e.printStackTrace();
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
            if (board.isConnected()) {
                board.disconnect();
            } else if (serialPortComboBox.getSelectedItem() != null) {
                String port = (String) serialPortComboBox.getSelectedItem();
                settings.setSerialPort(port, this);
                try {
                    board.connect(port);
                } catch (SerialPortException ex) {
                    connectionErr(ex);
                }
                try {
                    settings.save();
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            } else {
                JOptionPane.showMessageDialog(this, i18n("serial.port.not.selected"),
                        APP_NAME, JOptionPane.ERROR_MESSAGE);
            }

        } else if (source == pinWindowButton) {
            setAlwaysOnTop(pinWindowButton.isSelected());

        } else if (source == stopButton) {
            try {
                board.run(Board.Commands.FOCUSER_STOP, this);
            } catch (IOException | SerialPortException ex) {
                connectionErr(ex);
            } catch (IllegalArgumentException ex) {
                ex.printStackTrace();
            }

        } else if (source == fokOutButton || source == relativeMovField) {
            relativeFocuserMove(false);

        } else if (source == fokInButton) {
            relativeFocuserMove(true);

        } else if (source == setRequestedPosButton || source == requestedPosField) {
            try {
                board.run(Board.Commands.FOCUSER_ABS_MOVE, this,
                        Integer.parseInt(requestedPosField.getText().trim()));
            } catch (IOException | SerialPortException ex) {
                connectionErr(ex);
            } catch (IllegalArgumentException ex) {
                valueOutOfLimits(ex);
            }

        } else if (source == setZeroButton) {
            new PositionCalibrationWindow(this);

        } else if (source == miniWindowButton) {
            miniWindow.setVisible(true);
            setState(Frame.ICONIFIED);

        } else if (source == copyIndiDriverNameButton) {
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(
                    new StringSelection(driverNameBox.getText()), null);

        } else if (source == fokBacklashCalButton) {
            if (JOptionPane.showConfirmDialog(this, i18n("backlash.cal.warning"),
                    APP_NAME, JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.OK_OPTION) {
                new BacklashCalibrationWindow(this);
            }

        } else if (source == saveConfigButton) {
            Settings.Theme newTheme = Objects.requireNonNull((Settings.Theme) appThemeCombo.getSelectedItem());
            if (settings.theme != newTheme) {
                settings.theme = newTheme;
                try {
                    switch (newTheme) {
                        case LIGHT -> FlatIntelliJLaf.setup();
                        case DARK -> FlatDarkLaf.setup();
                        default -> IntelliJTheme.setup(Main.class.getResourceAsStream(
                                "/io/github/marcocipriani01/thunderfocus/themes/" + Objects.requireNonNull(settings.theme.getFileName())));
                    }
                    SwingUtilities.updateComponentTreeUI(this);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                appNameLabel.setFont(new Font(appNameLabel.getFont().getName(), Font.BOLD, 24));
                aboutLabel.setFont(new Font(aboutLabel.getFont().getName(), Font.BOLD, 13));
            }
            settings.indiServer = indiServerCheckBox.isSelected();
            settings.ascomBridge = ascomBridgeCheckBox.isSelected();
            settings.autoConnect = autoConnectBox.isSelected();
            settings.showIpIndiDriver = Objects.requireNonNull(localOrRemoteCombo.getSelectedItem())
                    .toString().equals(i18n("remote"));
            int oldIndiPort = settings.indiServerPort;
            settings.indiServerPort = (int) indiPortSpinner.getValue();
            int oldAscomPort = settings.ascomBridgePort;
            settings.ascomBridgePort = (int) ascomPortSpinner.getValue();
            if (board.isConnected() && board.isReady()) {
                Focuser focuser = board.focuser();
                if (focuser != null) {
                    settings.focuserTicksCount = (int) fokTicksCountSpinner.getValue();
                    settings.focuserTicksUnit = Settings.Units.values()[fokUnitsCombo.getSelectedIndex()];
                    updateUnitsLabel();
                    settings.setFokMaxTravel((int) fokMaxTravelSpinner.getValue(), this);
                    updateSlidersLimit();
                }
                try {
                    if (focuser != null) {
                        board.run(Board.Commands.FOCUSER_SET_BACKLASH, this, (int) fokBacklashSpinner.getValue());
                        board.run(Board.Commands.FOCUSER_SET_SPEED, this, fokSpeedSlider.getValue());
                        board.run(Board.Commands.FOCUSER_REVERSE_DIR, this, fokReverseDirBox.isSelected() ? 1 : 0);
                    }
                    PowerBox powerBox = board.powerBox();
                    if ((powerBox != null) && powerBox.supportsAmbient()) {
                        board.run(Board.Commands.SET_TIME_LAT_LONG, this, 0,
                                (int) (((double) powerBoxLatSpinner.getValue()) * 1000),
                                (int) (((double) powerBoxLongSpinner.getValue()) * 1000));
                    }
                } catch (IOException | SerialPortException ex) {
                    connectionErr(ex);
                } catch (IllegalArgumentException ex) {
                    valueOutOfLimits(ex);
                }
            }
            startOrStopINDI(settings.indiServerPort != oldIndiPort);
            if (board.isConnected()) startOrStopASCOM(settings.ascomBridgePort != oldAscomPort);
            try {
                settings.save();
            } catch (IOException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, i18n("error.saving"), APP_NAME, JOptionPane.ERROR_MESSAGE);
            }

        } else if (source == powerBoxOnButton) {
            try {
                for (ArduinoPin p : board.powerBox().asList()) {
                    if ((!p.isAutoModeEn()) && (!p.isOnWhenAppOpen())) {
                        p.setValue(255);
                        board.run(Board.Commands.POWER_BOX_SET, this, p.getNumber(), p.getValuePWM());
                    }
                }
                powerBoxTable.refresh();
            } catch (IOException | SerialPortException ex) {
                connectionErr(ex);
            } catch (IllegalArgumentException ex) {
                ex.printStackTrace();
            }

        } else if (source == powerBoxOffButton) {
            try {
                for (ArduinoPin p : board.powerBox().asList()) {
                    if ((!p.isAutoModeEn()) && (!p.isOnWhenAppOpen())) {
                        p.setValue(0);
                        board.run(Board.Commands.POWER_BOX_SET, this, p.getNumber(), p.getValuePWM());
                    }
                }
                powerBoxTable.refresh();
            } catch (IOException | SerialPortException ex) {
                connectionErr(ex);
            } catch (IllegalArgumentException ex) {
                ex.printStackTrace();
            }

        } else if (source == cleanGraphButton) {
            tempSeries.clear();
            dewPointSeries.clear();
            humiditySeries.clear();

        } else if (source == addPresetButton) {
            int pos = board.focuser().getPos();
            settings.presets.put(pos, i18n("description"));
            Integer[] positions = settings.presets.keySet().toArray(new Integer[0]);
            int index = IntStream.range(0, positions.length).filter(i -> positions[i] == pos).findFirst().orElse(-1);
            if (index != -1)
                ((PresetsTableModel) presetsTable.getModel()).fireTableRowsInserted(index, index);
            try {
                settings.save();
            } catch (IOException ex) {
                ex.printStackTrace();
            }

        } else if (source == deletePresetButton) {
            int selectedRow = presetsTable.getSelectedRow();
            if (selectedRow != -1) {
                Integer[] positions = settings.presets.keySet().toArray(new Integer[0]);
                int pos = positions[selectedRow];
                int index = IntStream.range(0, positions.length).filter(i -> positions[i] == pos).findFirst().orElse(-1);
                settings.presets.remove(pos);
                if (index != -1)
                    ((PresetsTableModel) presetsTable.getModel()).fireTableRowsDeleted(index, index);
                try {
                    settings.save();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }

        } else if (source == goToPresetButton) {
            int selectedRow = presetsTable.getSelectedRow();
            if (selectedRow != -1) {
                try {
                    board.run(Board.Commands.FOCUSER_ABS_MOVE, this,
                            settings.presets.keySet().toArray(new Integer[0])[selectedRow]);
                } catch (IOException | SerialPortException ex) {
                    connectionErr(ex);
                } catch (IllegalArgumentException ex) {
                    valueOutOfLimits(ex);
                }
            }

        } else if (source == exportButton) {
            JFileChooser chooser = new JFileChooser();
            chooser.setCurrentDirectory(new File(System.getProperty("user.home")));
            chooser.setFileFilter(new FileNameExtensionFilter(
                    "Settings files (*.thunder)", "thunder"));
            if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                try {
                    File file = chooser.getSelectedFile();
                    if (!file.getName().endsWith(".thunder"))
                        file = new File(file.getAbsolutePath() + ".thunder");
                    new ExportableSettings(settings, board).save(file.toPath());
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(this, i18n("error.saving"), APP_NAME, JOptionPane.ERROR_MESSAGE);
                }
            }

        } else if (source == importButton) {
            JFileChooser chooser = new JFileChooser();
            chooser.setCurrentDirectory(new File(System.getProperty("user.home")));
            chooser.setFileFilter(new FileNameExtensionFilter(
                    "Settings files (*.thunder)", "thunder"));
            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                try {
                    //TODO: apply settings
                    int oldIndiPort = settings.indiServerPort;
                    int oldAscomPort = settings.ascomBridgePort;
                    ExportableSettings es = ExportableSettings.load(chooser.getSelectedFile().toPath());
                    es.applyTo(settings, board);
                    appThemeCombo.setSelectedItem(es.theme);
                    relativeMovField.setText(String.valueOf(es.relativeStepSize));
                    localOrRemoteCombo.setSelectedItem(i18n(es.showIpIndiDriver ? "remote" : "local"));
                    ascomBridgeCheckBox.setSelected(es.ascomBridge);
                    indiServerCheckBox.setSelected(es.indiServer);
                    autoConnectBox.setSelected(es.autoConnect);
                    indiPortSpinner.setValue(es.indiServerPort);
                    ascomPortSpinner.setValue(es.ascomBridgePort);
                    fokTicksCountSpinner.setValue(es.focuserTicksCount);
                    fokUnitsCombo.setSelectedItem(es.focuserTicksUnit);
                    fokBacklashSpinner.setValue(es.backlash);
                    fokSpeedSlider.setValue(es.speed);
                    fokReverseDirBox.setSelected(es.reverseDir);
                    fokPowerSaverBox.setSelected(es.powerSaver);
                    updateUnitsLabel();
                    updateSlidersLimit();
                    startOrStopINDI(settings.indiServerPort != oldIndiPort);
                    startOrStopASCOM(settings.ascomBridgePort != oldAscomPort);
                    presetsTableModel.fireTableDataChanged();
                    powerBoxTable.refresh();
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(this, i18n("error.saving"), APP_NAME, JOptionPane.ERROR_MESSAGE);
                }
            }
        } else if ((source == lightOnRadio) || (source == lightOffRadio)) {
            try {
                board.run(Board.Commands.FLAT_SET_LIGHT, this, lightOnRadio.isSelected() ? 1 : 0);
            } catch (IOException | SerialPortException ex) {
                connectionErr(ex);
            } catch (IllegalArgumentException ex) {
                valueOutOfLimits(ex);
            }

        } else if ((source == openCoverRadio) || (source == closeCoverRadio)) {
            try {
                board.run(Board.Commands.FLAT_SET_COVER, this, openCoverRadio.isSelected() ? 1 : 0);
            } catch (IOException | SerialPortException ex) {
                connectionErr(ex);
            } catch (IllegalArgumentException ex) {
                valueOutOfLimits(ex);
            }

        } else if (source == saveServoConfig) {
            try {
                board.run(Board.Commands.FLAT_SET_CONFIG, this, (int) openAngleSpinner.getValue(),
                        (int) closedAngleSpinner.getValue(), coverSpeedSlider.getValue());
            } catch (IOException | SerialPortException ex) {
                connectionErr(ex);
            } catch (IllegalArgumentException ex) {
                valueOutOfLimits(ex);
            }
        }
    }

    public void valueOutOfLimits(Exception e) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(this, i18n("error.invalid"), APP_NAME, JOptionPane.ERROR_MESSAGE);
    }

    public void connectionErr(Exception e) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(this, i18n("error.connection"), APP_NAME, JOptionPane.ERROR_MESSAGE);
    }

    private void updateSlidersLimit() {
        posSlider.removeChangeListener(this);
        int fokMaxTravel = settings.getFocuserMaxTravel();
        posSlider.setMaximum(fokMaxTravel);
        posSlider.setMinorTickSpacing(fokMaxTravel / 70);
        posSlider.setLabelTable(null);
        posSlider.setMajorTickSpacing(fokMaxTravel / 4);
        posSlider.addChangeListener(this);
        ticksPosSlider.removeChangeListener(this);
        int fokTicksCount = settings.focuserTicksCount;
        ticksPosSlider.setMaximum(fokTicksCount);
        ticksPosSlider.setMinorTickSpacing(fokTicksCount / 70);
        ticksPosSlider.setLabelTable(null);
        ticksPosSlider.setMajorTickSpacing(fokTicksCount / 7);
        ticksPosSlider.addChangeListener(this);
    }

    @SuppressWarnings("DuplicatedCode")
    private void enableComponents(boolean connected) {
        if (connected) {
            connectButton.setIcon(new ImageIcon(Objects.requireNonNull(
                    getClass().getResource("/io/github/marcocipriani01/thunderfocus/res/disconnect.png"))));
        } else {
            connectButton.setIcon(new ImageIcon(
                    Objects.requireNonNull(getClass().getResource("/io/github/marcocipriani01/thunderfocus/res/connect.png"))));
            miniWindow.dispose();
        }
        refreshButton.setEnabled(!connected);
        serialPortComboBox.setEnabled(!connected);
        importButton.setEnabled(connected);
        exportButton.setEnabled(connected);

        boolean focuserEn = connected && board.hasFocuser();
        posSlider.setEnabled(focuserEn);
        stopButton.setEnabled(focuserEn);
        fokOutButton.setEnabled(focuserEn);
        fokInButton.setEnabled(focuserEn);
        setRequestedPosButton.setEnabled(focuserEn);
        setZeroButton.setEnabled(focuserEn);
        relativeMovField.setEnabled(focuserEn);
        requestedPosField.setEnabled(focuserEn);
        ticksPosSlider.setEnabled(focuserEn);
        miniWindowButton.setEnabled(focuserEn);
        fokTicksCountSpinner.setEnabled(focuserEn);
        fokUnitsCombo.setEnabled(focuserEn);
        fokMaxTravelSpinner.setEnabled(focuserEn);
        fokBacklashSpinner.setEnabled(focuserEn);
        fokBacklashCalButton.setEnabled(focuserEn);
        fokSpeedSlider.setEnabled(focuserEn);
        fokReverseDirBox.setEnabled(focuserEn);
        fokPowerSaverBox.setEnabled(focuserEn);
        presetsTable.setEnabled(focuserEn);
        addPresetButton.setEnabled(focuserEn);
        deletePresetButton.setEnabled(focuserEn);
        goToPresetButton.setEnabled(focuserEn);

        boolean powerBoxEn = connected && board.hasPowerBox();
        powerBoxOnButton.setEnabled(powerBoxEn);
        powerBoxOffButton.setEnabled(powerBoxEn);
        powerBoxAutoModeBox.setEnabled(powerBoxEn);

        boolean flat = connected && board.hasFlatPanel();
        lightOnRadio.setEnabled(flat);
        lightOffRadio.setEnabled(flat);
        brightnessSlider.setEnabled(flat);
        boolean hasServo = flat && board.flat().hasServo();
        closeCoverRadio.setEnabled(hasServo);
        openCoverRadio.setEnabled(hasServo);
        coverSpeedSlider.setEnabled(hasServo);
        openAngleSpinner.setEnabled(hasServo);
        closedAngleSpinner.setEnabled(hasServo);
        saveServoConfig.setEnabled(hasServo);
    }

    private void startOrStopINDI(boolean forceRestart) {
        if (settings.indiServer) {
            Main.indiServerCreator.start(settings.indiServerPort, forceRestart);
            indiStatusLabel.setText(i18n("server.active"));
        } else {
            Main.indiServerCreator.stop();
            indiStatusLabel.setText(i18n("server.inactive"));
        }
    }

    private void startOrStopASCOM(boolean forceRestart) {
        try {
            if (settings.ascomBridge) {
                if (Main.isAscomRunning()) {
                    if (forceRestart) {
                        Main.ascomBridge.close();
                        Main.ascomBridge = new ASCOMBridge(settings.ascomBridgePort, this::onASCOMClientListChange);
                        Main.ascomBridge.start();
                    }
                } else {
                    Main.ascomBridge = new ASCOMBridge(settings.ascomBridgePort, this::onASCOMClientListChange);
                    Main.ascomBridge.start();
                }
                ascomStatusLabel.setText(i18n("bridge.active"));
            } else {
                if (Main.isAscomRunning()) Main.ascomBridge.close();
                ascomStatusLabel.setText(i18n("bridge.inactive"));
            }
        } catch (IOException e) {
            onCriticalError(e);
            ascomStatusLabel.setText(i18n("error"));
        }
    }

    private void onASCOMClientListChange() {
        int clientsCount = ascomBridge.getClientsCount();
        boolean lock = isAscomRunning() && (clientsCount > 0);
        powerBoxTable.setConfigLock(lock);
        powerboxLockLabel.setVisible(lock);
        ascomStatusLabel.setText((clientsCount > 0) ?
                (i18n("bridge.active") + " (" + clientsCount + ")") : i18n("bridge.active"));
    }

    @Override
    public void stateChanged(ChangeEvent event) {
        Object source = event.getSource();
        if (source == tabPane) {
            if (tabPane.getSelectedComponent() == powerBoxTab) {
                powerBoxTable.scaleColumns();
                powerBoxTable.scaleRows();
            }
            return;
        }
        try {
            if (source == posSlider) {
                board.run(Board.Commands.FOCUSER_ABS_MOVE, this, posSlider.getValue());
            } else if (source == ticksPosSlider) {
                board.run(Board.Commands.FOCUSER_ABS_MOVE,
                        this, Focuser.ticksToSteps(ticksPosSlider.getValue()));
            } else if (source == brightnessSlider) {
                board.run(Board.Commands.FLAT_SET_BRIGHTNESS, this, brightnessSlider.getValue());
            }
        } catch (IOException | SerialPortException ex) {
            connectionErr(ex);
        } catch (IllegalArgumentException ex) {
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
        if (board.isConnected() && board.isReady()) {
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
        posSlider.setValue(board.focuser().getPos());
        posSlider.addChangeListener(MainWindow.this);
    }

    private void updateTicksPosSlider() {
        ticksPosSlider.removeChangeListener(MainWindow.this);
        ticksPosSlider.setValue(board.focuser().getPosTicks());
        ticksPosSlider.addChangeListener(MainWindow.this);
    }

    private void updateUnitsLabel() {
        unitsLabel.setText(settings.focuserTicksUnit.toString() + ":");
    }

    @Override
    public void updateParam(Board.Parameters p) {
        SwingUtilities.invokeLater(() -> {
            switch (p) {
                case REQUESTED_POS -> requestedPosField.setText(String.valueOf(board.focuser().getTargetPos()));
                case CURRENT_POS -> {
                    currentPosField.setText(String.valueOf(board.focuser().getPos()));
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
                        fokSpeedSlider.setValue(board.focuser().getSpeed());
                    }
                }
                case BACKLASH -> fokBacklashSpinner.setValue(board.focuser().getBacklash());
                case REVERSE_DIR -> fokReverseDirBox.setSelected(board.focuser().isDirInverted());
                case ENABLE_POWER_SAVE -> fokPowerSaverBox.setSelected(board.focuser().isPowerSaverEnabled());
                case POWERBOX_PINS -> powerBoxTable.refresh();
                case POWERBOX_AUTO_MODE -> {
                    powerBoxAutoModeBox.removeItemListener(this);
                    powerBoxAutoModeBox.setSelectedItem(board.powerBox().getAutoMode());
                    powerBoxAutoModeBox.addItemListener(this);
                }
                case POWERBOX_AMBIENT_DATA -> {
                    PowerBox powerBox = board.powerBox();
                    double temperature = powerBox.getTemperature();
                    Second instant = new Second(new Date());
                    if (temperature == PowerBox.ABSOLUTE_ZERO) {
                        tempDataset.setValue(-20D);
                    } else {
                        tempDataset.setValue(temperature);
                        tempSeries.addOrUpdate(instant, temperature);
                    }
                    double humidity = powerBox.getHumidity();
                    if (humidity == PowerBox.INVALID_HUMIDITY) {
                        humidityDataset.setValue(0D);
                    } else {
                        humidityDataset.setValue(humidity);
                        humiditySeries.addOrUpdate(instant, humidity);
                    }
                    double dewPoint = powerBox.getDewPoint();
                    if (dewPoint == PowerBox.ABSOLUTE_ZERO) {
                        dewPointDataset.setValue(-20D);
                    } else {
                        dewPointDataset.setValue(dewPoint);
                        dewPointSeries.addOrUpdate(instant, dewPoint);
                    }
                }
                case POWERBOX_SUN_ELEV -> sunElevationField.setText(board.powerBox().getSunElev() + "°");
                case FLAT_COVER_STATUS -> updateFlatPanelComponents(board.flat());
                case FLAT_LIGHT_STATUS -> {
                    lightOnRadio.removeActionListener(this);
                    lightOffRadio.removeActionListener(this);
                    boolean lightStatus = board.flat().getLightStatus();
                    lightOnRadio.setSelected(lightStatus);
                    lightOffRadio.setSelected(!lightStatus);
                    lightOnRadio.addActionListener(this);
                    lightOffRadio.addActionListener(this);
                }
                case FLAT_BRIGHTNESS -> {
                    brightnessSlider.removeChangeListener(this);
                    brightnessSlider.setValue(board.flat().getBrightness());
                    brightnessSlider.addChangeListener(this);
                }
            }
        });
    }

    private void updateFlatPanelComponents(FlatPanel flat) {
        FlatPanel.CoverStatus coverStatus = flat.getCoverStatus();
        openCoverRadio.removeActionListener(this);
        closeCoverRadio.removeActionListener(this);
        openCoverRadio.setSelected(coverStatus == FlatPanel.CoverStatus.OPEN);
        closeCoverRadio.setSelected(coverStatus == FlatPanel.CoverStatus.CLOSED);
        openCoverRadio.addActionListener(this);
        closeCoverRadio.addActionListener(this);
        boolean enabledRadios = (coverStatus != FlatPanel.CoverStatus.NEITHER_OPEN_NOR_CLOSED);
        openCoverRadio.setEnabled(enabledRadios);
        closeCoverRadio.setEnabled(enabledRadios);
        coverStatusLabel.setText(coverStatus.getLabel());
    }

    @Override
    public void updateFocuserState(Focuser.FocuserState focuserState) {
        SwingUtilities.invokeLater(() -> focuserStateLabel.setText(focuserState.getLabel()));
    }

    @Override
    public void updateConnectionState(Board.ConnectionState connectionState) {
        SwingUtilities.invokeLater(() -> {
            connStatusLabel.setText(connectionState.getLabel());
            switch (connectionState) {
                case CONNECTED_READY: {
                    ok.setVisible(true);
                    timeout.setVisible(false);
                    err.setVisible(false);
                    Focuser focuser = board.focuser();
                    if (focuser != null) {
                        fokSpeedSlider.setValue(focuser.getSpeed());
                        fokReverseDirBox.setSelected(focuser.isDirInverted());
                        fokPowerSaverBox.setSelected(focuser.isPowerSaverEnabled());
                        int currentPos = focuser.getPos();
                        posSlider.removeChangeListener(MainWindow.this);
                        posSlider.setValue(currentPos);
                        posSlider.addChangeListener(MainWindow.this);
                        ticksPosSlider.removeChangeListener(MainWindow.this);
                        ticksPosSlider.setValue(Focuser.stepsToTicks(currentPos));
                        ticksPosSlider.addChangeListener(MainWindow.this);
                        String currentPosStr = String.valueOf(currentPos);
                        currentPosField.setText(currentPosStr);
                        requestedPosField.setText(currentPosStr);
                        requestedPosField.setText(currentPosStr);
                        fokBacklashSpinner.setValue(focuser.getBacklash());
                    }
                    PowerBox powerBox = board.powerBox();
                    int tabInsertIndex = 2;
                    if (powerBox != null) {
                        tabPane.insertTab(i18n("powerbox.tab"), POWERBOX_TAB, powerBoxTab, "", tabInsertIndex);
                        tabInsertIndex++;
                        powerBoxTable.setPowerBox(powerBox);
                        boolean supportsAutoModes = powerBox.supportsAutoModes();
                        powerBoxAutoModeLabel.setVisible(supportsAutoModes);
                        powerBoxAutoModeBox.setVisible(supportsAutoModes);
                        if (supportsAutoModes) {
                            powerBoxAutoModeBox.removeItemListener(this);
                            powerBoxAutoModeBox.setModel(new DefaultComboBoxModel<>(powerBox.supportedAutoModesArray()));
                            powerBoxAutoModeBox.setSelectedItem(board.powerBox().getAutoMode());
                            powerBoxAutoModeBox.addItemListener(this);
                        }
                        if (powerBox.supportsAmbient()) {
                            tabPane.insertTab(i18n("sensors.tab"), AMBIENT_TAB, ambientTab, "", tabInsertIndex);
                            tabInsertIndex++;
                        }
                        boolean supportsTime = powerBox.supportsTime();
                        sunElevationLabel.setVisible(supportsTime);
                        sunElevationField.setVisible(supportsTime);
                        sunElevationField.setText("?");
                        powerBoxConfigPanel.setVisible(supportsTime);
                        if (supportsTime) {
                            powerBoxLatSpinner.setValue(powerBox.getLatitude());
                            powerBoxLongSpinner.setValue(powerBox.getLongitude());
                        }
                    }
                    FlatPanel flat = board.flat();
                    if (flat != null) {
                        tabPane.insertTab(i18n("flat.panel"), FLAT_PANEL_TAB, flatPanelTab, "", tabInsertIndex);
                        boolean lightStatus = flat.getLightStatus();
                        lightOnRadio.removeActionListener(this);
                        lightOffRadio.removeActionListener(this);
                        lightOnRadio.setSelected(lightStatus);
                        lightOffRadio.setSelected(!lightStatus);
                        lightOnRadio.addActionListener(this);
                        lightOffRadio.addActionListener(this);
                        brightnessSlider.removeChangeListener(this);
                        brightnessSlider.setValue(flat.getBrightness());
                        brightnessSlider.addChangeListener(this);
                        if (flat.hasServo()) {
                            updateFlatPanelComponents(flat);
                            openAngleSpinner.removeChangeListener(this);
                            closedAngleSpinner.removeChangeListener(this);
                            coverSpeedSlider.removeChangeListener(this);
                            openAngleSpinner.setValue(flat.getOpenServoVal());
                            closedAngleSpinner.setValue(flat.getClosedServoVal());
                            coverSpeedSlider.setValue(flat.getServoSpeed());
                            openAngleSpinner.addChangeListener(this);
                            closedAngleSpinner.addChangeListener(this);
                            coverSpeedSlider.addChangeListener(this);
                        }
                    }
                    enableComponents(true);
                    startOrStopASCOM(false);
                    tabPane.setSelectedIndex(0);
                    break;
                }
                case DISCONNECTED: {
                    if (Main.isAscomRunning()) {
                        try {
                            Main.ascomBridge.close();
                            ascomStatusLabel.setText(i18n("bridge.inactive"));
                        } catch (IOException ex) {
                            onCriticalError(ex);
                            ascomStatusLabel.setText(i18n("error"));
                        }
                    }
                    powerBoxConfigPanel.setVisible(false);
                    removeTab(ambientTab);
                    removeTab(powerBoxTab);
                    enableComponents(false);
                    powerBoxTable.setPowerBox(null);
                    tabPane.setSelectedIndex(0);
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
        e.printStackTrace();
        SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(
                this, i18n("error.unexpected") + " " + e.getMessage(), APP_NAME, JOptionPane.ERROR_MESSAGE));
    }

    @Override
    public void updateFocuserMaxTravel(int value) {
        SwingUtilities.invokeLater(() -> {
            fokMaxTravelSpinner.setValue(value);
            updateSlidersLimit();
        });
    }

    @Override
    public void updateSerialPort(String value) {
        SwingUtilities.invokeLater(() -> serialPortComboBox.setSelectedItem(value));
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        try {
            PowerBox.AutoModes autoMode = (PowerBox.AutoModes) powerBoxAutoModeBox.getSelectedItem();
            if (autoMode != null)
                board.run(Board.Commands.POWER_BOX_SET_AUTO_MODE, this, autoMode.ordinal());
        } catch (IOException | SerialPortException ex) {
            connectionErr(ex);
        } catch (IllegalArgumentException ex) {
            valueOutOfLimits(ex);
        }
    }

    private static class PresetsTableModel extends AbstractTableModel {

        @Override
        public String getColumnName(int col) {
            return i18n((col == 0) ? "position" : "description");
        }

        @Override
        public int getRowCount() {
            return settings.presets.size();
        }

        @Override
        public int getColumnCount() {
            return 2;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            if (columnIndex == 0)
                return settings.presets.keySet().toArray()[rowIndex];
            else
                return settings.presets.values().toArray()[rowIndex];
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            return (columnIndex == 0) ? Integer.class : String.class;
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return columnIndex == 1;
        }

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            if (columnIndex == 1) {
                settings.presets.put((Integer) settings.presets.keySet().toArray()[rowIndex], (String) aValue);
                fireTableCellUpdated(rowIndex, columnIndex);
                try {
                    settings.save();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }
}