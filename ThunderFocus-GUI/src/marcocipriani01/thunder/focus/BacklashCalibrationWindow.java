package marcocipriani01.thunder.focus;

import marcocipriani01.thunder.focus.io.ConnectionException;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;

import static marcocipriani01.thunder.focus.Main.APP_LOGO;

public class BacklashCalibrationWindow extends JDialog implements ThuderFocuser.Listener {

    private JButton acceptButton;
    private JButton cancelButton;
    private JButton oneStepButton;
    private JLabel counterLabel;
    private JPanel parent;
    private JButton relMovButton;
    private JSpinner relMovSpinner;
    private JLabel loading;
    private int count;
    private boolean initialMoveDone = false;

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
                Main.focuser.run(ThuderFocuser.Commands.FOK1_SET_BACKLASH, this, count);
            } catch (ConnectionException ex) {
                connectionErr(ex);
            } catch (ThuderFocuser.InvalidParamException ex) {
                ex.printStackTrace();
            }
            dispose();
        });
        oneStepButton.addActionListener(e -> {
            try {
                Main.focuser.run(ThuderFocuser.Commands.FOK1_REL_MOVE, this, 1);
                setControlsEnabled(false);
            } catch (ConnectionException ex) {
                connectionErr(ex);
            } catch (ThuderFocuser.InvalidParamException | NumberFormatException ex) {
                valueOutOfLimits(ex);
            }
        });
        relMovButton.addActionListener(e -> {
            try {
                Main.focuser.run(ThuderFocuser.Commands.FOK1_REL_MOVE, this, (int) relMovSpinner.getValue());
                setControlsEnabled(false);
            } catch (ConnectionException ex) {
                connectionErr(ex);
            } catch (ThuderFocuser.InvalidParamException | NumberFormatException ex) {
                valueOutOfLimits(ex);
            }
        });

        Main.focuser.addListener(this);
        Main.focuser.clearRequestedPositions();
        try {
            Main.focuser.run(ThuderFocuser.Commands.FOK1_SET_BACKLASH, this, 0);
            Main.focuser.run(ThuderFocuser.Commands.FOK1_ABS_MOVE, this, Main.settings.getFokMaxTravel() / 2);
        } catch (ConnectionException e) {
            connectionErr(e);
            dispose();
        } catch (ThuderFocuser.InvalidParamException e) {
            e.printStackTrace();
        }
        setBounds(450, 250, 420, 510);
        setResizable(false);
        setVisible(true);
    }

    private void valueOutOfLimits(Exception e) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(this, "Valore fuori dai limiti o non valido.", Main.APP_NAME, JOptionPane.ERROR_MESSAGE);
    }

    private void connectionErr(ConnectionException e) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(this, "Errore di connessione!", Main.APP_NAME, JOptionPane.ERROR_MESSAGE);
    }

    @Override
    public void dispose() {
        super.dispose();
        try {
            Main.focuser.run(ThuderFocuser.Commands.FOK1_STOP, this);
        } catch (ConnectionException e) {
            connectionErr(e);
            dispose();
        } catch (ThuderFocuser.InvalidParamException e) {
            e.printStackTrace();
        }
        Main.focuser.setExclusiveMode(null);
        Main.focuser.removeListener(this);
    }

    private void setControlsEnabled(boolean b) {
        loading.setVisible(!b);
        counterLabel.setVisible(b);
        acceptButton.setEnabled(b);
        oneStepButton.setEnabled(b);
        relMovSpinner.setEnabled(b);
        relMovButton.setEnabled(b);
    }

    @Override
    public void onReachedPos() {
        setControlsEnabled(true);
        if (initialMoveDone) {
            count += Main.focuser.getRequestedRelPos();
            counterLabel.setText(String.valueOf(count));
        } else {
            try {
                Main.focuser.run(ThuderFocuser.Commands.FOK1_ABS_MOVE, this, 20);
                initialMoveDone = true;
            } catch (ConnectionException e) {
                connectionErr(e);
                e.printStackTrace();
            } catch (ThuderFocuser.InvalidParamException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void updateParam(ThuderFocuser.Parameters p) {

    }

    @Override
    public void onCriticalError(Exception e) {

    }

    @Override
    public void updateFocuserState(ThuderFocuser.FocuserState focuserState) {

    }

    @Override
    public void updateConnSate(ThuderFocuser.ConnState connState) {

    }

    private void createUIComponents() {
        relMovSpinner = new JSpinner(new SpinnerNumberModel(5, -500, 500, 1));
    }
}
