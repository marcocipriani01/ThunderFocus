package marcocipriani.openfocuser.manager.plus;

import marcocipriani.openfocuser.manager.ControlPanel;

import javax.swing.*;
import javax.swing.text.DefaultFormatter;

/**
 * {@link JDialog} to ask the user for a digital pin, its name and its value.
 *
 * @author marcocipriani01
 * @version 1.0
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class DigitalPinDialog extends ControlPanel.AbstractPinDialog {

    /**
     * The panel.
     */
    private JPanel parent;
    /**
     * The name.
     */
    private JTextField nameTextField;
    /**
     * The value.
     */
    private JCheckBox stateCheckBox;
    /**
     * The label with the pin ID.
     */
    private JSpinner pinSpinner;

    /**
     * Class constructor.
     *
     * @param frame a parent window for this dialog.
     * @param pin   a pin.
     */
    public DigitalPinDialog(JFrame frame, ArduinoPin pin) {
        super(frame, pin);
        setContentPane(parent);
        setUpPinFields(pinSpinner, nameTextField);
        stateCheckBox.addActionListener(e -> pin.setValue(PinValue.ValueType.BOOLEAN, stateCheckBox.isSelected()));
        stateCheckBox.setSelected(pin.getValueBoolean());
        showUp();
    }

    private void createUIComponents() {
        pinSpinner = new JSpinner(new SpinnerNumberModel(13, 2, 99, 1));
        ((DefaultFormatter) ((JFormattedTextField) pinSpinner.getEditor().getComponent(0)).getFormatter())
                .setCommitsOnValidEdit(true);
    }
}