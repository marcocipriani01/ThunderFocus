package marcocipriani01.thunderfocus;

import marcocipriani01.simplesocket.ConnectionException;
import marcocipriani01.thunderfocus.board.ThunderFocuser;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;

import static marcocipriani01.thunderfocus.Main.APP_LOGO;
import static marcocipriani01.thunderfocus.Main.i18n;

public class BacklashCalibrationWindow extends JDialog implements ThunderFocuser.Listener {

    private JButton acceptButton;
    private JButton cancelButton;
    private JButton oneStepButton;
    private JLabel counterLabel;
    private JPanel parent;
    private JButton relMovButton;
    private JSpinner relMovSpinner;
    private JLabel loading;
    private volatile int count = 0;
    private volatile int phase = 0;
    private boolean wasPowerSaveOn;

    public BacklashCalibrationWindow(Frame owner) {
        super(owner, Main.APP_NAME, true);
        setIconImage(APP_LOGO);
        Main.focuser.setExclusiveMode(this);
        setContentPane(parent);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        cancelButton.addActionListener(e -> dispose());
        parent.registerKeyboardAction(e -> dispose(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        parent.registerKeyboardAction(e -> oneStepButton.doClick(), KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        acceptButton.addActionListener(e -> {
            try {
                Main.focuser.run(ThunderFocuser.Commands.FOK1_SET_BACKLASH, this, count);
            } catch (ConnectionException ex) {
                connectionErr(ex);
            } catch (ThunderFocuser.InvalidParamException ex) {
                ex.printStackTrace();
            }
            dispose();
        });
        oneStepButton.addActionListener(e -> {
            SwingUtilities.invokeLater(() -> setControlsEnabled(false));
            try {
                Main.focuser.run(ThunderFocuser.Commands.FOK1_REL_MOVE, this, 1);
            } catch (ConnectionException ex) {
                connectionErr(ex);
            } catch (ThunderFocuser.InvalidParamException | NumberFormatException ex) {
                valueOutOfLimits(ex);
            }
        });
        relMovButton.addActionListener(e -> {
            SwingUtilities.invokeLater(() -> setControlsEnabled(false));
            try {
                Main.focuser.run(ThunderFocuser.Commands.FOK1_REL_MOVE, this, (int) relMovSpinner.getValue());
            } catch (ConnectionException ex) {
                connectionErr(ex);
            } catch (ThunderFocuser.InvalidParamException | NumberFormatException ex) {
                valueOutOfLimits(ex);
            }
        });

        Main.focuser.addListener(this);
        Main.focuser.clearRequestedPositions();
        try {
            wasPowerSaveOn = Main.focuser.isPowerSaverOn();
            Main.focuser.run(ThunderFocuser.Commands.FOK1_POWER_SAVER, this, 0);
            Main.focuser.run(ThunderFocuser.Commands.FOK1_SET_BACKLASH, this, 0);
            int target = Main.settings.getFokMaxTravel() / 4;
            if (target == Main.focuser.getCurrentPos()) {
                phase = 1;
                Main.focuser.run(ThunderFocuser.Commands.FOK1_ABS_MOVE, this, 20);
            } else {
                Main.focuser.run(ThunderFocuser.Commands.FOK1_ABS_MOVE, this, target);
            }
        } catch (ConnectionException e) {
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

    private void connectionErr(ConnectionException e) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(this, i18n("error.connection"), Main.APP_NAME, JOptionPane.ERROR_MESSAGE);
    }

    @Override
    public void dispose() {
        super.dispose();
        try {
            Main.focuser.run(ThunderFocuser.Commands.FOK1_POWER_SAVER, this, wasPowerSaveOn ? 1 : 0);
            Main.focuser.run(ThunderFocuser.Commands.FOK1_STOP, this);
        } catch (ConnectionException e) {
            connectionErr(e);
        } catch (ThunderFocuser.InvalidParamException e) {
            e.printStackTrace();
        }
        Main.focuser.setExclusiveMode(null);
        Main.focuser.removeListener(this);
    }

    private synchronized void setControlsEnabled(final boolean b) {
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
                    Main.focuser.run(ThunderFocuser.Commands.FOK1_ABS_MOVE, this, 20);
                    phase = 1;
                } catch (ConnectionException e) {
                    connectionErr(e);
                } catch (ThunderFocuser.InvalidParamException e) {
                    e.printStackTrace();
                }
            }
            case 1 -> {
                phase = 2;
                SwingUtilities.invokeLater(() -> setControlsEnabled(true));
            }
            case 2 -> {
                count += Main.focuser.getRequestedRelPos();
                SwingUtilities.invokeLater(() -> {
                    counterLabel.setText(String.valueOf(count));
                    setControlsEnabled(true);
                });
            }
        }
    }

    @Override
    public void updateParam(ThunderFocuser.Parameters p) {

    }

    @Override
    public void onCriticalError(Exception e) {

    }

    @Override
    public void updateFocuserState(ThunderFocuser.FocuserState focuserState) {

    }

    @Override
    public void updateConnSate(ThunderFocuser.ConnState connState) {

    }
}