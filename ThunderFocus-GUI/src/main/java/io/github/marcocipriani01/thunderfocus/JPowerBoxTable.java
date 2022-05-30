package io.github.marcocipriani01.thunderfocus;

import io.github.marcocipriani01.thunderfocus.board.ArduinoPin;
import io.github.marcocipriani01.thunderfocus.board.Board;
import io.github.marcocipriani01.thunderfocus.board.PowerBox;
import jssc.SerialPortException;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.IOException;

import static io.github.marcocipriani01.thunderfocus.Main.i18n;
import static io.github.marcocipriani01.thunderfocus.Main.settings;

/**
 * JTable for viewing, editing and rendering {@link ArduinoPin} objects.
 *
 * @author marcocipriani01
 * @version 2.1
 */
public class JPowerBoxTable extends JTable {

    private static final int DEF_ROW_HEIGHT = 60;
    private static final int[] COLUMNS_WEIGHTS_PWM = {5, 3, 10, 4, 4, 4};
    private static final int[] COLUMNS_WEIGHTS = {5, 3, 4, 4, 4, 4};
    private final SliderEditorAndRenderer sliderEditorAndRenderer = new SliderEditorAndRenderer();
    private final MainWindow mainWindow;
    private boolean lockConfig = false;
    private PowerBox powerBox = null;

    /**
     * Class constructor. Initializes the JTable.
     */
    public JPowerBoxTable(MainWindow mainWindow) {
        super();
        this.mainWindow = mainWindow;
        setModel(new PowerBoxTableModel());
        setRowSelectionAllowed(true);
        setCellSelectionEnabled(false);
        setColumnSelectionAllowed(false);
        JTableHeader tableHeader = getTableHeader();
        tableHeader.setResizingAllowed(false);
        tableHeader.setReorderingAllowed(false);
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
        setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                scaleColumns();
            }
        });
    }

    public void setConfigLock(boolean b) {
        lockConfig = b;
        refresh();
    }

    public void refresh() {
        ((PowerBoxTableModel) dataModel).fireTableDataChanged();
        scaleRows();
    }

    public void scaleColumns() {
        if (powerBox == null) return;
        int tW = getWidth(), count = dataModel.getColumnCount(), weightsSum = 0;
        //if (count == 0) return;
        TableColumn[] columns = new TableColumn[count];
        int[] weights = (powerBox.hasPWMPins()) ? COLUMNS_WEIGHTS_PWM : COLUMNS_WEIGHTS;
        for (int i = 0; i < count; i++) {
            columns[i] = columnModel.getColumn(i);
            weightsSum += weights[i];
        }
        DefaultTableCellRenderer centerTextRenderer = new DefaultTableCellRenderer();
        centerTextRenderer.setHorizontalAlignment(JLabel.CENTER);
        columns[1].setCellRenderer(centerTextRenderer);
        for (int i = 0; i < count; i++) {
            columns[i].setMaxWidth(Math.round(tW * weights[i] / ((float) weightsSum)));
        }
    }

    public void scaleRows() {
        for (int row = 0; row < dataModel.getRowCount(); row++) {
            setRowHeight(row, DEF_ROW_HEIGHT);
        }
    }

    public void setPowerBox(PowerBox powerBox) {
        this.powerBox = powerBox;
        ((PowerBoxTableModel) dataModel).fireTableStructureChanged();
        scaleRows();
    }

    @Override
    public TableCellRenderer getCellRenderer(int row, int column) {
        if (column != 2) return super.getCellRenderer(row, column);
        if (powerBox.getIndex(row).isPWMEnabled()) {
            return sliderEditorAndRenderer;
        }
        return getDefaultRenderer(Boolean.class);
    }

    @Override
    public TableCellEditor getCellEditor(int row, int column) {
        if (column != 2) return super.getCellEditor(row, column);
        if (powerBox.getIndex(row).isPWMEnabled()) return sliderEditorAndRenderer;
        return getDefaultEditor(Boolean.class);
    }

    /**
     * The model of this table.
     * Sets the values of the pin in the {@link PowerBox} and returns up-to-date values to the table.
     *
     * @author marcocipriani01
     * @version 2.0
     */
    private class PowerBoxTableModel extends AbstractTableModel {

        @Override
        public String getColumnName(int col) {
            switch (col) {
                case 0 -> {
                    return i18n("name");
                }
                case 1 -> {
                    return i18n("number");
                }
                case 2 -> {
                    return i18n("value");
                }
                case 3 -> {
                    return i18n("pwm");
                }
                case 4 -> {
                    return i18n("on.when.app.open");
                }
                case 5 -> {
                    return i18n("auto");
                }
            }
            return "";
        }

        @Override
        public int getRowCount() {
            return (powerBox == null) ? 0 : powerBox.size();
        }

        @Override
        public int getColumnCount() {
            return (powerBox == null) ? 0 : (powerBox.supportsAutoModes() ? 6 : 5);
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            if (powerBox == null) throw new NullPointerException("Null powerbox.");
            ArduinoPin pin = powerBox.getIndex(rowIndex);
            switch (columnIndex) {
                case 0 -> {
                    return pin.getName();
                }
                case 1 -> {
                    return pin.getNumber();
                }
                case 2 -> {
                    return pin.isPWMEnabled() ? pin.getValuePWM() : pin.getValueBoolean();
                }
                case 3 -> {
                    return pin.isPWMEnabled();
                }
                case 4 -> {
                    return powerBox.getIndex(rowIndex).isOnWhenAppOpen();
                }
                case 5 -> {
                    return powerBox.getIndex(rowIndex).isAutoModeEn();
                }
                default -> {
                    return "";
                }
            }
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            switch (columnIndex) {
                case 1 -> {
                    return Integer.class;
                }
                case 2 -> {
                    return Object.class;
                }
                case 3, 4, 5 -> {
                    return Boolean.class;
                }
                default -> {
                    return String.class;
                }
            }
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            ArduinoPin pin = powerBox.getIndex(rowIndex);
            if (lockConfig) return (columnIndex == 2) && (!pin.isOnWhenAppOpen()) && (!pin.isAutoModeEn());
            if (columnIndex == 0) return true;
            if (columnIndex == 3) return pin.isPWM();
            if (pin.isOnWhenAppOpen()) return (columnIndex == 4);
            if (pin.isAutoModeEn()) return (columnIndex == 5);
            return (columnIndex != 1);
        }

        @Override
        public void setValueAt(Object val, int rowIndex, int columnIndex) {
            if (powerBox == null) throw new NullPointerException("Null pins list.");
            ArduinoPin pin = powerBox.getIndex(rowIndex);
            switch (columnIndex) {
                case 0 -> {
                    pin.setName((String) val);
                    PowerBox.clonePins(powerBox, settings.powerBoxPins);
                    try {
                        Main.settings.save();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }

                case 2 -> {
                    try {
                        Main.board.run(Board.Commands.POWER_BOX_SET_PIN, mainWindow, pin.getNumber(),
                                pin.isPWMEnabled() ? ArduinoPin.constrain((int) val) : (((boolean) val) ? 255 : 0));
                    } catch (IOException | SerialPortException ex) {
                        mainWindow.connectionErr(ex);
                    } catch (IllegalArgumentException ex) {
                        mainWindow.valueOutOfLimits(ex);
                    }
                }

                case 3 -> {
                    boolean enablePWM = (boolean) val;
                    pin.setPWMEnabled(enablePWM);
                    try {
                        Main.board.run(Board.Commands.POWER_BOX_EN_PIN_PWM, mainWindow, pin.getNumber(), enablePWM ? 1 : 0);
                        Main.board.run(Board.Commands.POWER_BOX_SET_PIN, mainWindow, pin.getNumber(), pin.getValuePWM());
                    } catch (IOException | SerialPortException ex) {
                        mainWindow.connectionErr(ex);
                    } catch (IllegalArgumentException ex) {
                        mainWindow.valueOutOfLimits(ex);
                    }
                    ((PowerBoxTableModel) dataModel).fireTableStructureChanged();
                    scaleRows();
                    scaleColumns();
                }

                case 4 -> {
                    boolean b = (boolean) val;
                    pin.setOnWhenAppOpen(b);
                    if (b) {
                        pin.setValue(true);
                        fireTableCellUpdated(rowIndex, 2);
                        try {
                            Main.board.run(Board.Commands.POWER_BOX_SET_PIN, mainWindow, pin.getNumber(), 255);
                        } catch (IOException | SerialPortException ex) {
                            mainWindow.connectionErr(ex);
                        } catch (IllegalArgumentException ex) {
                            mainWindow.valueOutOfLimits(ex);
                        }
                    }
                    PowerBox.clonePins(powerBox, settings.powerBoxPins);
                    try {
                        Main.settings.save();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }

                case 5 -> {
                    boolean b = (boolean) val;
                    pin.setAutoModeEn(b);
                    try {
                        Main.board.run(Board.Commands.POWER_BOX_SET_PIN_AUTO, mainWindow, pin.getNumber(), b ? 1 : 0);
                    } catch (IOException | SerialPortException ex) {
                        mainWindow.connectionErr(ex);
                    } catch (IllegalArgumentException ex) {
                        mainWindow.valueOutOfLimits(ex);
                    }
                }
            }
            fireTableCellUpdated(rowIndex, columnIndex);
        }
    }

    /**
     * A slider editor and renderer for the pin value column in the table.
     *
     * @author marcocipriani01
     * @version 1.0
     */
    private class SliderEditorAndRenderer extends AbstractCellEditor implements TableCellEditor, TableCellRenderer {

        private final JSlider editorSlider;
        private final JSlider rendererSlider;
        private final JPanel rendererPanel;

        /**
         * Creates the slider.
         */
        SliderEditorAndRenderer() {
            super();
            editorSlider = new JSlider(JSlider.HORIZONTAL, 0, 255, 0);
            editorSlider.setMajorTickSpacing(15);
            editorSlider.setMinorTickSpacing(5);
            editorSlider.setPaintTicks(true);
            editorSlider.setSnapToTicks(true);
            rendererSlider = new JSlider(JSlider.HORIZONTAL, 0, 255, 0);
            rendererPanel = new JPanel();
            rendererPanel.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));
            rendererPanel.add(rendererSlider);
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            if (isSelected) {
                editorSlider.setForeground(getSelectionForeground());
                editorSlider.setBackground(getSelectionBackground());
            } else {
                editorSlider.setForeground(getForeground());
                editorSlider.setBackground(getBackground());
            }
            editorSlider.setValue((Integer) value);
            return editorSlider;
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            if (isSelected) {
                rendererPanel.setForeground(getSelectionForeground());
                rendererPanel.setBackground(getSelectionBackground());
            } else {
                rendererPanel.setForeground(getForeground());
                rendererPanel.setBackground(getBackground());
            }
            rendererSlider.setSize(new Dimension(table.getColumnModel().getColumn(column).getWidth(), table.getRowHeight()));
            rendererSlider.setValue((Integer) value);
            return rendererPanel;
        }

        @Override
        public Object getCellEditorValue() {
            return editorSlider.getValue();
        }
    }
}