package io.github.marcocipriani01.thunderfocus;

import io.github.marcocipriani01.thunderfocus.board.ThunderFocuser;
import jssc.SerialPortException;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import static io.github.marcocipriani01.thunderfocus.Main.APP_LOGO;
import static io.github.marcocipriani01.thunderfocus.Main.i18n;

public class PositionCalibrationWindow extends JDialog implements ActionListener {

    private JPanel parent;
    private JTextField stepsField;
    private JTextField unitsField;
    private JButton zeroButton;
    private JButton stepsButton;
    private JButton unitsButton;
    private JLabel unitsLabel;

    public PositionCalibrationWindow(Frame owner) {
        super(owner, Main.APP_NAME, true);
        setIconImage(APP_LOGO);
        setContentPane(parent);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        zeroButton.addActionListener(this);
        stepsField.addActionListener(this);
        stepsButton.addActionListener(this);
        unitsField.addActionListener(this);
        unitsButton.addActionListener(this);
        unitsLabel.setText(Main.settings.focuserTicksUnit.toString().toLowerCase());
        pack();
        setLocation(450, 350);
        setResizable(false);
        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
        try {
            if (source == stepsButton || source == stepsField) {
                Main.focuser.run(ThunderFocuser.Commands.FOCUSER_SET_POS, null, Integer.parseInt(stepsField.getText()));
                dispose();
            } else if (source == unitsButton || source == unitsField) {
                Main.focuser.run(ThunderFocuser.Commands.FOCUSER_SET_POS, null,
                        Main.focuser.ticksToSteps(Integer.parseInt(unitsField.getText())));
                dispose();
            } else if (source == zeroButton) {
                Main.focuser.run(ThunderFocuser.Commands.FOCUSER_SET_ZERO, null);
                dispose();
            }
        } catch (IOException | SerialPortException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, i18n("error.connection"), Main.APP_NAME, JOptionPane.ERROR_MESSAGE);
        } catch (ThunderFocuser.InvalidParamException | NumberFormatException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, i18n("error.invalid"), Main.APP_NAME, JOptionPane.ERROR_MESSAGE);
        }
    }
}