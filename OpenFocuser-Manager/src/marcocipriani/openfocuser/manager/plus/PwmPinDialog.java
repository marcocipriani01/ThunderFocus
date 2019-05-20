package marcocipriani.openfocuser.manager.plus;

import marcocipriani.openfocuser.manager.ControlPanel;

import javax.swing.*;
import javax.swing.text.DefaultFormatter;

/**
 * {@link JDialog} to ask the user for a PWM pin, its name and its value.
 *
 * @author marcocipriani01
 * @version 1.0
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class PwmPinDialog extends ControlPanel.AbstractPinDialog {

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
    private JSpinner valueSpinner;
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
    public PwmPinDialog(JFrame frame, ArduinoPin pin) {
        super(frame, pin);
        setContentPane(parent);
        setUpPinFields(pinSpinner, nameTextField);
        valueSpinner.addChangeListener(e -> pin.setValue(PinValue.ValueType.PERCENTAGE, valueSpinner.getValue()));
        valueSpinner.addMouseWheelListener(e -> {
            int rotation = e.getWheelRotation(), currentValue = (int) valueSpinner.getValue();
            if (!(rotation < 0 && currentValue == 100) && !(rotation > 0 && currentValue == 0)) {
                valueSpinner.setValue(currentValue - rotation);
            }
        });
        valueSpinner.setValue(pin.getValuePercentage());
        showUp();
    }

    private void createUIComponents() {
        pinSpinner = new JSpinner(new SpinnerNumberModel(13, 2, 99, 1));
        ((DefaultFormatter) ((JFormattedTextField) pinSpinner.getEditor().getComponent(0)).getFormatter())
                .setCommitsOnValidEdit(true);
        valueSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 100, 1));
        ((DefaultFormatter) ((JFormattedTextField) valueSpinner.getEditor().getComponent(0)).getFormatter())
                .setCommitsOnValidEdit(true);
    }
}