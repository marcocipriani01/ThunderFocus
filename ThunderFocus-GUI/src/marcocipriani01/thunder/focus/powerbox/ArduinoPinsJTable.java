package marcocipriani01.thunder.focus.powerbox;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.EventObject;

/**
 * JTable for viewing, editing and rendering {@link ArduinoPin} objects.
 *
 * @author marcocipriani01
 * @version 1.0
 */
public class ArduinoPinsJTable extends JTable {

    private static final int DEF_ROW_HEIGHT = 45;
    private final boolean arePwmPins;
    private PinArray pins = null;
    private boolean editMode = true;

    /**
     * Class constructor. Initializes the JTable.
     *
     * @param arePwmPins {@code true} if the pins are PWM pin (therefore use a slider to select their value)
     */
    public ArduinoPinsJTable(boolean arePwmPins) {
        super();
        setModel(new ArduinoPinsTableModel());
        setRowSelectionAllowed(true);
        setCellSelectionEnabled(false);
        setColumnSelectionAllowed(false);
        JTableHeader tableHeader = getTableHeader();
        tableHeader.setResizingAllowed(false);
        tableHeader.setReorderingAllowed(false);
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
        selectionModel.addListSelectionListener(this);
        setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        TableColumn c2 = columnModel.getColumn(2);
        if (this.arePwmPins = arePwmPins) {
            SliderEditorAndRenderer editorAndRenderer = new SliderEditorAndRenderer(255, 0);
            c2.setCellEditor(editorAndRenderer);
            c2.setCellRenderer(editorAndRenderer);
        }
    }

    @Override
    public TableCellEditor getCellEditor(int row, int column) {
        return super.getCellEditor(row, column);
    }

    @Override
    public TableCellRenderer getCellRenderer(int row, int column) {
        return super.getCellRenderer(row, column);
    }

    public void refresh() {
        ((ArduinoPinsTableModel) getModel()).fireTableDataChanged();
        setTableRowsHeight();
    }

    public void fixWidths() {
        int tW = getWidth();
        columnModel.getColumn(0).setMaxWidth(Math.round(tW / 3.0f) + 1);
        DefaultTableCellRenderer centerTextRenderer = new DefaultTableCellRenderer();
        centerTextRenderer.setHorizontalAlignment(JLabel.CENTER);
        TableColumn c1 = columnModel.getColumn(1);
        c1.setCellRenderer(centerTextRenderer);
        c1.setMaxWidth(Math.round(tW / 6.0f) + 1);
        columnModel.getColumn(2).setMaxWidth(Math.round(tW / 2.0f) + 1);
    }

    public PinArray getPins() {
        return pins;
    }

    public void setPins(PinArray pins) {
        this.pins = pins;
        setTableRowsHeight();
    }

    /**
     * @return the selected pin in the table.
     */
    @SuppressWarnings("WeakerAccess")
    public ArduinoPin getSelectedPin() {
        return pins.get(super.getSelectedRow());
    }

    /**
     * @return {@code true} if the editing of the pin is enabled.
     */
    @SuppressWarnings("unused")
    public boolean isEditMode() {
        return editMode;
    }

    /**
     * Sets whether or not the editing mode should be on.
     *
     * @param editMode {@code true} to allow the name and the pin number columns to be edited.
     */
    public void setEditMode(boolean editMode) {
        this.editMode = editMode;
    }

    /**
     * Sets the height of every row in the table to the desired one.
     */
    private void setTableRowsHeight() {
        for (int row = 0; row < getRowCount(); row++) {
            setRowHeight(row, DEF_ROW_HEIGHT);
        }
    }

    /**
     * The model of this table.
     * Sets the values of the pin in the {@link PinArray} and returns up-to-date values to the table.
     *
     * @author marcocipriani01
     * @version 1.0
     */
    private class ArduinoPinsTableModel extends AbstractTableModel {
        @Override
        public String getColumnName(int col) {
            switch (col) {
                case 0 -> {
                    return "Nome";
                }
                case 1 -> {
                    return "Numero";
                }
                case 2 -> {
                    return "Valore";
                }
            }
            return "";
        }

        @Override
        public int getRowCount() {
            return (pins == null) ? 0 : pins.size();
        }

        /**
         * Adds an {@link ArduinoPin} to the {@link PinArray} and to the table.
         *
         * @param pin a pin.
         */
        void add(ArduinoPin pin) {
            if (pins == null) throw new NullPointerException("Null pins list.");
            if (pin == null) throw new NullPointerException("Null pin.");
            pins.add(pin);
            fireTableDataChanged();
            setTableRowsHeight();
        }

        /**
         * Removes an {@link ArduinoPin} from the {@link PinArray} and from the table.
         *
         * @param pin a pin.
         */
        void remove(ArduinoPin pin) {
            if (pins == null) throw new NullPointerException("Null pins list.");
            pins.remove(pin);
            fireTableDataChanged();
            setTableRowsHeight();
        }

        @Override
        public int getColumnCount() {
            return 3;
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            switch (columnIndex) {
                case 1 -> {
                    return Integer.class;
                }
                case 2 -> {
                    return arePwmPins ? Integer.class : Boolean.class;
                }
                default -> {
                    return String.class;
                }
            }
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            if (pins == null) throw new NullPointerException("Null pins list.");
            ArduinoPin pin = pins.get(rowIndex);
            switch (columnIndex) {
                case 0 -> {
                    return pin.getName();
                }
                case 1 -> {
                    return pin.getPin();
                }
                case 2 -> {
                    return arePwmPins ? pin.getValuePwm() : pin.getValueBoolean();
                }
                default -> {
                    return "";
                }
            }
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return editMode && (columnIndex != 1);
        }

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            if (pins == null) throw new NullPointerException("Null pins list.");
            ArduinoPin pin = pins.get(rowIndex);
            switch (columnIndex) {
                case 0 -> pin.setName((String) aValue);

                case 1 -> pin.setPin((int) aValue);

                case 2 -> pin.setValue(arePwmPins ? ArduinoPin.ValueType.PWM : ArduinoPin.ValueType.BOOLEAN, aValue);
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
         *
         * @param max   the maximum value.
         * @param value an initial value.
         */
        SliderEditorAndRenderer(int max, int value) {
            super();
            editorSlider = new JSlider(JSlider.HORIZONTAL, 0, max, value);
            editorSlider.setMajorTickSpacing(15);
            editorSlider.setMinorTickSpacing(5);
            editorSlider.setPaintTicks(true);
            editorSlider.setSnapToTicks(true);
            rendererSlider = new JSlider(JSlider.HORIZONTAL, 0, max, value);
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

        @Override
        public boolean isCellEditable(EventObject anEvent) {
            return true;
        }

        @Override
        public boolean stopCellEditing() {
            return super.stopCellEditing();
        }

        @Override
        protected void fireEditingStopped() {
            super.fireEditingStopped();
        }
    }
}