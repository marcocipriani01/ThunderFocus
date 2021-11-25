package io.github.marcocipriani01.thunderfocus;

import io.github.marcocipriani01.thunderfocus.board.ThunderFocuser;
import jssc.SerialPortException;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.IOException;

import static io.github.marcocipriani01.thunderfocus.Main.*;

public class BacklashCalibrationWindow extends JDialog implements ThunderFocuser.Listener {

    private JButton acceptButton;
    private JButton cancelButton;
    private JButton oneStepButton;
    private JLabel counterLabel;
    private JPanel parent;
    private JButton relMovButton;
    private JSpinner relMovSpinner;
    private JLabel loading;
    private boolean wasPowerSaveOn;
    private volatile int phase = 0;
    private int count = 0;
    private int lastMoveSteps = 0;

    public BacklashCalibrationWindow(Frame owner) {
        super(owner, Main.APP_NAME, true);
        setIconImage(APP_LOGO);
        focuser.setExclusiveMode(this);
        setContentPane(parent);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        cancelButton.addActionListener(e -> dispose());
        parent.registerKeyboardAction(e -> dispose(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        parent.registerKeyboardAction(e -> oneStepButton.doClick(), KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        acceptButton.addActionListener(e -> {
            try {
                focuser.run(ThunderFocuser.Commands.FOCUSER_SET_BACKLASH, this, count);
            } catch (IOException | SerialPortException ex) {
                connectionErr(ex);
            } catch (ThunderFocuser.InvalidParamException ex) {
                ex.printStackTrace();
            }
            dispose();
        });
        oneStepButton.addActionListener(e -> {
            setControlsEnabled(false);
            lastMoveSteps = 1;
            try {
                focuser.run(ThunderFocuser.Commands.FOCUSER_REL_MOVE, this, 1);
            } catch (IOException | SerialPortException ex) {
                connectionErr(ex);
            } catch (ThunderFocuser.InvalidParamException | NumberFormatException ex) {
                valueOutOfLimits(ex);
            }
        });
        relMovButton.addActionListener(e -> {
            setControlsEnabled(false);
            lastMoveSteps = (int) relMovSpinner.getValue();
            try {
                focuser.run(ThunderFocuser.Commands.FOCUSER_REL_MOVE, this, lastMoveSteps);
            } catch (IOException | SerialPortException ex) {
                connectionErr(ex);
            } catch (ThunderFocuser.InvalidParamException | NumberFormatException ex) {
                valueOutOfLimits(ex);
            }
        });

        focuser.addListener(this);
        focuser.clearRequestedPositions();
        try {
            wasPowerSaveOn = focuser.isPowerSaverOn();
            focuser.run(ThunderFocuser.Commands.FOCUSER_POWER_SAVER, this, 0);
            focuser.run(ThunderFocuser.Commands.FOCUSER_SET_BACKLASH, this, 0);
            int maxTravel = settings.getFocuserMaxTravel();
            int target = maxTravel / 2;
            if (target == focuser.getCurrentPos()) {
                phase = 1;
                focuser.run(ThunderFocuser.Commands.FOCUSER_ABS_MOVE, this, maxTravel / 4);
            } else {
                focuser.run(ThunderFocuser.Commands.FOCUSER_ABS_MOVE, this, target);
            }
        } catch (IOException | SerialPortException e) {
            connectionErr(e);
            dispose();
        } catch (ThunderFocuser.InvalidParamException e) {
            e.printStackTrace();
        }
        setBounds(450, 250, 420, 520);
        setResizable(false);
        setVisible(true);
    }

    private void createUIComponents() {
        relMovSpinner = new JSpinner(new SpinnerNumberModel(5, -500, 500, 1));
    }

    private void valueOutOfLimits(Exception e) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(this, i18n("error.invalid"), Main.APP_NAME, JOptionPane.ERROR_MESSAGE);
    }

    private void connectionErr(Exception e) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(this, i18n("error.connection"), Main.APP_NAME, JOptionPane.ERROR_MESSAGE);
    }

    @Override
    public void dispose() {
        super.dispose();
        try {
            focuser.run(ThunderFocuser.Commands.FOCUSER_POWER_SAVER, this, wasPowerSaveOn ? 1 : 0);
            focuser.run(ThunderFocuser.Commands.FOCUSER_STOP, this);
        } catch (IOException | SerialPortException e) {
            connectionErr(e);
        } catch (ThunderFocuser.InvalidParamException e) {
            e.printStackTrace();
        }
        focuser.setExclusiveMode(null);
        focuser.removeListener(this);
    }

    private void setControlsEnabled(final boolean b) {
        loading.setVisible(!b);
        counterLabel.setVisible(b);
        acceptButton.setEnabled(b);
        oneStepButton.setEnabled(b);
        relMovSpinner.setEnabled(b);
        relMovButton.setEnabled(b);
    }

    @Override
    public synchronized void onReachedPos() {
        switch (phase) {
            case 0 -> {
                try {
                    focuser.run(ThunderFocuser.Commands.FOCUSER_ABS_MOVE, this, settings.getFocuserMaxTravel() / 4);
                    phase = 1;
                } catch (IOException | SerialPortException e) {
                    connectionErr(e);
                } catch (ThunderFocuser.InvalidParamException e) {
                    e.printStackTrace();
                }
            }
            case 1 -> {
                phase = 2;
                SwingUtilities.invokeLater(() -> setControlsEnabled(true));
            }
            case 2 -> SwingUtilities.invokeLater(() -> {
                count += lastMoveSteps;
                counterLabel.setText(String.valueOf(count));
                setControlsEnabled(true);
            });
        }
    }
}