package marcocipriani.openfocuser.manager.pins;

import marcocipriani.openfocuser.manager.ControlPanel;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.text.ParseException;
import java.util.EventObject;

/**
 * JTable for viewing, editing and rendering {@link ArduinoPin} objects.
 *
 * @author marcocipriani01
 * @version 1.0
 */
public class ArduinoPinsJTable extends JTable {

    private static final int DEF_ROW_HEIGHT = 35;
    private final boolean arePwmPins;
    private final JButton removePinButton;
    private final ControlPanel controlPanel;
    private boolean editMode = true;
    private PinArray pins;

    /**
     * Class constructor. Initializes the JTable.
     *
     * @param pins            The pins to manage and edit
     * @param arePwmPins      {@code true} if the pins are PWM pin (therefore use a slider to select their value)
     * @param addPinButton    a {@link JButton} that handles the addition of a new pin.
     * @param removePinButton a {@link JButton} that handles the removal of a new pin.
     * @param controlPanel    a {@link ControlPanel} object.
     */
    public ArduinoPinsJTable(PinArray pins, boolean arePwmPins, JButton addPinButton, JButton removePinButton, ControlPanel controlPanel) {
        super();
        this.pins = pins;
        this.arePwmPins = arePwmPins;
        this.removePinButton = removePinButton;
        this.controlPanel = controlPanel;

        setModel(new ArduinoPinsTableModel());
        setRowSelectionAllowed(true);
        setCellSelectionEnabled(false);
        setColumnSelectionAllowed(false);
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        addPinButton.addActionListener(e -> ((ArduinoPinsTableModel) ArduinoPinsJTable.this.getModel()).add(controlPanel.askNewPin()));
        removePinButton.addActionListener(e -> ((ArduinoPinsTableModel) ArduinoPinsJTable.this.getModel()).remove(getSelectedPin()));
        selectionModel.addListSelectionListener(this);

        TableColumnModel columnModel = getColumnModel();
        TableColumn column1 = columnModel.getColumn(1);
        column1.setCellEditor(new SpinnerEditor(2, 12));
        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(JLabel.LEFT);
        column1.setCellRenderer(rightRenderer);
        TableColumn column2 = columnModel.getColumn(2);
        if (arePwmPins) {
            SliderEditorAndRenderer editorAndRenderer = new SliderEditorAndRenderer(0, 100, 0);
            column2.setCellEditor(editorAndRenderer);
            column2.setCellRenderer(editorAndRenderer);

        } else {
            column2.setPreferredWidth(20);
        }
        setTableRowsHeight();
    }

    /**
     * Called when the user changes the selection in the table.
     * Turns on and off the removal button if a row is selected.
     */
    @Override
    public void valueChanged(ListSelectionEvent e) {
        super.valueChanged(e);
        removePinButton.setEnabled(getSelectedRowCount() == 1);
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
                case 0: {
                    return "Name";
                }

                case 1: {
                    return "Number";
                }

                case 2: {
                    return "Default value";
                }
            }
            return "";
        }

        @Override
        public int getRowCount() {
            return pins.size();
        }

        /**
         * Adds an {@link ArduinoPin} to the {@link PinArray} and to the table.
         *
         * @param pin a pin.
         */
        void add(ArduinoPin pin) {
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
                case 1: {
                    return Integer.class;
                }

                case 2: {
                    return arePwmPins ? Integer.class : Boolean.class;
                }

                case 0:
                default: {
                    return String.class;
                }
            }
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            ArduinoPin pin = pins.get(rowIndex);
            switch (columnIndex) {
                case 0: {
                    return pin.getName();
                }

                case 1: {
                    return pin.getPin();
                }

                case 2: {
                    return arePwmPins ? pin.getValuePercentage() : pin.getValueBoolean();
                }

                default: {
                    return "";
                }
            }
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return editMode;
        }

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            ArduinoPin pin = pins.get(rowIndex);
            switch (columnIndex) {
                case 0: {
                    pin.setName((String) aValue);
                    break;
                }

                case 1: {
                    pin.setPin((int) aValue);
                    break;
                }

                case 2: {
                    if (arePwmPins) {
                        pin.setValue(ArduinoPin.ValueType.PERCENTAGE, aValue);

                    } else {
                        pin.setValue(ArduinoPin.ValueType.BOOLEAN, aValue);
                    }
                    break;
                }
            }
            fireTableCellUpdated(rowIndex, columnIndex);
        }
    }

    /**
     * A spinner editor for the pin number column in the table.
     *
     * @author marcocipriani01
     * @version 1.0
     */
    private class SpinnerEditor extends DefaultCellEditor {

        private JSpinner spinner;
        private JSpinner.DefaultEditor editor;
        private JTextField textField;
        private boolean valueSet;
        private int min;
        private int max;

        /**
         * Creates the spinner.
         *
         * @param min the minimum value the spinner can assume.
         * @param max the maximum.
         */
        SpinnerEditor(int min, int max) {
            super(new JTextField());
            this.min = min;
            this.max = max;
            spinner = new JSpinner(new SpinnerNumberModel((max - min) / 2, min, max, 1));
            editor = ((JSpinner.DefaultEditor) spinner.getEditor());
            editor.getTextField().setHorizontalAlignment(JTextField.LEFT);
            textField = editor.getTextField();
            textField.addFocusListener(new FocusListener() {
                @Override
                public void focusGained(FocusEvent fe) {
                    SwingUtilities.invokeLater(() -> {
                        if (valueSet) {
                            textField.setCaretPosition(1);
                        }
                    });
                }

                @Override
                public void focusLost(FocusEvent fe) {

                }
            });
            textField.addActionListener(ae -> stopCellEditing());
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            if (!valueSet) {
                spinner.setValue(value);
            }
            SwingUtilities.invokeLater(() -> textField.requestFocus());
            if (isSelected) {
                spinner.setForeground(getSelectionForeground());
                spinner.setBackground(getSelectionBackground());

            } else {
                spinner.setForeground(getForeground());
                spinner.setBackground(getBackground());
            }
            return spinner;
        }

        @Override
        public boolean isCellEditable(EventObject anEvent) {
            if (anEvent instanceof KeyEvent) {
                KeyEvent ke = (KeyEvent) anEvent;
                textField.setText(String.valueOf(ke.getKeyChar()));
                valueSet = true;

            } else {
                valueSet = false;
            }
            return editMode;
        }

        @Override
        public Object getCellEditorValue() {
            return spinner.getValue();
        }

        @Override
        public boolean stopCellEditing() {
            try {
                editor.commitEdit();
                spinner.commitEdit();

            } catch (ParseException e) {
                JOptionPane.showMessageDialog(controlPanel, "Pin value must be withing the range " + min + "-" + max + "!",
                        controlPanel.getTitle(), JOptionPane.ERROR_MESSAGE);
            }
            return super.stopCellEditing();
        }
    }

    /**
     * A slider editor and renderer for the pin value column in the table.
     *
     * @author marcocipriani01
     * @version 1.0
     */
    private class SliderEditorAndRenderer extends AbstractCellEditor implements TableCellEditor, TableCellRenderer {

        private JSlider editorSlider;
        private JSlider rendererSlider;

        /**
         * Creates the slider.
         *
         * @param min   the minimum value the slider can assume.
         * @param max   the maximum.
         * @param value an initial value.
         */
        SliderEditorAndRenderer(int min, int max, int value) {
            super();
            editorSlider = new JSlider(JSlider.HORIZONTAL, min, max, value);
            editorSlider.setMajorTickSpacing(25);
            editorSlider.setMinorTickSpacing(5);
            editorSlider.setPaintTicks(true);
            rendererSlider = new JSlider(JSlider.HORIZONTAL, min, max, value);
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