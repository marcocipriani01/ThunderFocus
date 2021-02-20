package marcocipriani01.thunderfocus;

import marcocipriani01.simplesocket.ConnectionException;
import marcocipriani01.thunderfocus.board.ArduinoPin;
import marcocipriani01.thunderfocus.board.PowerBox;
import marcocipriani01.thunderfocus.board.ThunderFocuser;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.io.IOException;

import static marcocipriani01.thunderfocus.Main.i18n;

/**
 * JTable for viewing, editing and rendering {@link ArduinoPin} objects.
 *
 * @author marcocipriani01
 * @version 2.0
 */
public class JPowerBoxTable extends JTable {

    private static final int DEF_ROW_HEIGHT = 45;
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
        //selectionModel.addListSelectionListener(this);
    }

    public void refresh() {
        ((PowerBoxTableModel) getModel()).fireTableDataChanged();
        setTableRowsHeight();
    }

    public void fixWidths() {
        if (powerBox == null) return;
        int tW = getWidth();
        TableColumn c0 = columnModel.getColumn(0);
        TableColumn c1 = columnModel.getColumn(1);
        TableColumn c2 = columnModel.getColumn(2);
        if (powerBox.supportsAutoModes()) {
            c0.setMaxWidth(Math.round(tW / 3.4773523480f) + 1);
            DefaultTableCellRenderer centerTextRenderer = new DefaultTableCellRenderer();
            centerTextRenderer.setHorizontalAlignment(JLabel.CENTER);
            c1.setCellRenderer(centerTextRenderer);
            c1.setMaxWidth(Math.round(tW / 6.4773523480f) + 1);
            c2.setMaxWidth(Math.round(tW / 2.4773523480f) + 1);
            columnModel.getColumn(3).setMaxWidth(Math.round(tW / 6.4773523480f) + 1);
        } else {
            c0.setMaxWidth(Math.round(tW / 3.0f) + 1);
            DefaultTableCellRenderer centerTextRenderer = new DefaultTableCellRenderer();
            centerTextRenderer.setHorizontalAlignment(JLabel.CENTER);
            c1.setCellRenderer(centerTextRenderer);
            c1.setMaxWidth(Math.round(tW / 6.0f) + 1);
            c2.setMaxWidth(Math.round(tW / 2.0f) + 1);
        }
    }

    public PowerBox getPowerBox() {
        return powerBox;
    }

    public void setPowerBox(PowerBox powerBox) {
        this.powerBox = powerBox;
        ((PowerBoxTableModel) dataModel).fireTableStructureChanged();
        setTableRowsHeight();
    }

    /**
     * Sets the height of every row in the table to the desired one.
     */
    private void setTableRowsHeight() {
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
        if (powerBox.getIndex(row).isPwm()) {
            return sliderEditorAndRenderer;
        }
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
            return (powerBox == null) ? 0 : (powerBox.supportsAutoModes() ? 4 : 3);
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
                case 3 -> {
                    return Boolean.class;
                }
                default -> {
                    return String.class;
                }
            }
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            if (powerBox.getIndex(rowIndex).isAutoModeEn()) return (columnIndex != 1 && columnIndex != 2);
            return (columnIndex != 1);
        }

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            if (powerBox == null) throw new NullPointerException("Null pins list.");
            ArduinoPin pin = powerBox.getIndex(rowIndex);
            switch (columnIndex) {
                case 0 -> {
                    pin.setName((String) aValue);
                    Main.settings.setPowerBox(new PowerBox(powerBox), mainWindow);
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
                rendererSlider.setForeground(getSelectionForeground());
                rendererSlider.setBackground(getSelectionBackground());
            } else {
                rendererSlider.setForeground(getForeground());
                rendererSlider.setBackground(getBackground());
            }
            rendererSlider.setSize(new Dimension(table.getColumnModel().getColumn(column).getWidth(), table.getRowHeight()));
            rendererSlider.setValue((Integer) value);
            return rendererSlider;
        }

        @Override
        public Object getCellEditorValue() {
            return editorSlider.getValue();
        }
    }
}