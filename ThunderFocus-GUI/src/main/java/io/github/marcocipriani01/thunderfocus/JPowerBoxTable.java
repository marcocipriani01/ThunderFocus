package io.github.marcocipriani01.thunderfocus;

import io.github.marcocipriani01.simplesocket.ConnectionException;
import io.github.marcocipriani01.thunderfocus.board.ArduinoPin;
import io.github.marcocipriani01.thunderfocus.board.PowerBox;
import io.github.marcocipriani01.thunderfocus.board.ThunderFocuser;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.io.IOException;

import static io.github.marcocipriani01.thunderfocus.Main.i18n;

/**
 * JTable for viewing, editing and rendering {@link ArduinoPin} objects.
 *
 * @author marcocipriani01
 * @version 2.1
 */
public class JPowerBoxTable extends JTable {

    private static final int DEF_ROW_HEIGHT = 60;
    private static final int[] COLUMNS_WEIGHTS = {5, 3, 10, 4, 4};
    private final SliderEditorAndRenderer sliderEditorAndRenderer = new SliderEditorAndRenderer();
    private final MainWindow mainWindow;
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
    }

    public void refresh() {
        ((PowerBoxTableModel) dataModel).fireTableDataChanged();
        for (int row = 0; row < getRowCount(); row++) {
            setRowHeight(row, DEF_ROW_HEIGHT);
        }
    }

    public void fixWidths() {
        if (powerBox == null) return;
        int tW = getWidth(), count = getColumnCount(), weightsSum = 0;
        TableColumn[] columns = new TableColumn[count];
        for (int i = 0; i < count; i++) {
            columns[i] = columnModel.getColumn(i);
            weightsSum += COLUMNS_WEIGHTS[i];
        }
        DefaultTableCellRenderer centerTextRenderer = new DefaultTableCellRenderer();
        centerTextRenderer.setHorizontalAlignment(JLabel.CENTER);
        columns[1].setCellRenderer(centerTextRenderer);
        for (int i = 0; i < count; i++) {
            columns[i].setMaxWidth(Math.round(tW * COLUMNS_WEIGHTS[i] / ((float) weightsSum)));
        }
    }

    public void setPowerBox(PowerBox powerBox) {
        this.powerBox = powerBox;
        ((PowerBoxTableModel) dataModel).fireTableStructureChanged();
        for (int row = 0; row < getRowCount(); row++) {
            setRowHeight(row, DEF_ROW_HEIGHT);
        }
    }

    @Override
    public TableCellRenderer getCellRenderer(int row, int column) {
        if (column != 2) return super.getCellRenderer(row, column);
        if (powerBox.getIndex(row).isPwm()) {
            return sliderEditorAndRenderer;
        }
        return getDefaultRenderer(Boolean.class);
    }

    @Override
    public TableCellEditor getCellEditor(int row, int column) {
        if (column != 2) return super.getCellEditor(row, column);
        if (powerBox.getIndex(row).isPwm()) return sliderEditorAndRenderer;
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
                    return i18n("on.when.app.open");
                }
                case 4 -> {
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
            return (powerBox == null) ? 0 : (powerBox.supportsAutoModes() ? 5 : 4);
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            if (powerBox == null) throw new NullPointerException("Null pins list.");
            ArduinoPin pin = powerBox.getIndex(rowIndex);
            switch (columnIndex) {
                case 0 -> {
                    return pin.getName();
                }
                case 1 -> {
                    return pin.getNumber();
                }
                case 2 -> {
                    if (pin.isPwm()) {
                        return pin.getValuePwm();
                    } else {
                        return pin.getValueBoolean();
                    }
                }
                case 3 -> {
                    return powerBox.getIndex(rowIndex).isOnWhenAppOpen();
                }
                case 4 -> {
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
                case 3, 4 -> {
                    return Boolean.class;
                }
                default -> {
                    return String.class;
                }
            }
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            if (columnIndex == 0) return true;
            ArduinoPin pin = powerBox.getIndex(rowIndex);
            if (pin.isOnWhenAppOpen()) return (columnIndex == 3);
            if (pin.isAutoModeEn()) return (columnIndex == 4);
            return (columnIndex != 1);
        }

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            if (powerBox == null) throw new NullPointerException("Null pins list.");
            ArduinoPin pin = powerBox.getIndex(rowIndex);
            switch (columnIndex) {
                case 0 -> {
                    pin.setName((String) aValue);
                    Main.settings.setPowerBox(new PowerBox(powerBox));
                    try {
                        Main.settings.save();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }

                case 2 -> {
                    if (pin.isPwm()) {
                        pin.setValue((int) aValue);
                    } else {
                        pin.setValue((boolean) aValue);
                    }
                    try {
                        Main.focuser.run(ThunderFocuser.Commands.POWER_BOX_SET, mainWindow, pin.getNumber(), pin.getValuePwm());
                    } catch (ConnectionException ex) {
                        mainWindow.connectionErr(ex);
                    } catch (ThunderFocuser.InvalidParamException | NumberFormatException ex) {
                        mainWindow.valueOutOfLimits(ex);
                    }
                }

                case 3 -> {
                    boolean b = (boolean) aValue;
                    pin.setOnWhenAppOpen(b);
                    if (b) {
                        pin.setValue(true);
                        fireTableCellUpdated(rowIndex, 2);
                        try {
                            Main.focuser.run(ThunderFocuser.Commands.POWER_BOX_SET, mainWindow, pin.getNumber(), pin.getValuePwm());
                        } catch (ConnectionException ex) {
                            mainWindow.connectionErr(ex);
                        } catch (ThunderFocuser.InvalidParamException | NumberFormatException ex) {
                            mainWindow.valueOutOfLimits(ex);
                        }
                    }
                    Main.settings.setPowerBox(new PowerBox(powerBox));
                    try {
                        Main.settings.save();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }

                case 4 -> {
                    boolean b = (boolean) aValue;
                    pin.setAutoModeEn(b);
                    try {
                        Main.focuser.run(ThunderFocuser.Commands.POWER_BOX_SET_PIN_AUTO, mainWindow, pin.getNumber(), b ? 1 : 0);
                    } catch (ConnectionException ex) {
                        mainWindow.connectionErr(ex);
                    } catch (ThunderFocuser.InvalidParamException | NumberFormatException ex) {
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