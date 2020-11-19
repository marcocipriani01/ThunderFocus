package marcocipriani01.thunder.focus;

import marcocipriani01.thunder.focus.io.ConnectionException;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;

public class BacklashCalibrationWindow extends JDialog implements EasyFocuser.Listener {

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
        Main.focuser.setExclusiveMode(this);
        setContentPane(parent);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        cancelButton.addActionListener(e -> dispose());
        parent.registerKeyboardAction(e -> dispose(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        parent.registerKeyboardAction(e -> oneStepButton.doClick(), KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        acceptButton.addActionListener(e -> {
            try {
                Main.focuser.run(EasyFocuser.Commands.FOK_SET_BACKLASH, this, count);
            } catch (ConnectionException ex) {
                connectionErr(ex);
            } catch (EasyFocuser.InvalidParamException ex) {
                ex.printStackTrace();
            }
            dispose();
        });
        oneStepButton.addActionListener(e -> {
            try {
                Main.focuser.run(EasyFocuser.Commands.FOK_REL_MOVE, this, 1);
                setControlsEnabled(false);
            } catch (ConnectionException ex) {
                connectionErr(ex);
            } catch (EasyFocuser.InvalidParamException | NumberFormatException ex) {
                valueOutOfLimits(ex);
            }
        });
        relMovButton.addActionListener(e -> {
            try {
                Main.focuser.run(EasyFocuser.Commands.FOK_REL_MOVE, this, (int) relMovSpinner.getValue());
                setControlsEnabled(false);
            } catch (ConnectionException ex) {
                connectionErr(ex);
            } catch (EasyFocuser.InvalidParamException | NumberFormatException ex) {
                valueOutOfLimits(ex);
            }
        });
        setBounds(450, 250, 420, 510);
        setResizable(false);

        Main.focuser.addListener(this);
        Main.focuser.clearRequestedPositions();
        setVisible(true);
        try {
            Main.focuser.run(EasyFocuser.Commands.FOK_SET_BACKLASH, this, 0);
            Main.focuser.run(EasyFocuser.Commands.FOK_ABS_MOVE, this, Main.settings.fokMaxTravel / 2);
        } catch (ConnectionException e) {
            connectionErr(e);
            dispose();
        } catch (EasyFocuser.InvalidParamException e) {
            e.printStackTrace();
        }
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
    public void onReady() {

    }

    @Override
    public void onReachedPos() {
        setControlsEnabled(true);
        if (initialMoveDone) {
            count += Main.focuser.getRequestedRelPos();
            counterLabel.setText(String.valueOf(count));
        } else {
            try {
                Main.focuser.run(EasyFocuser.Commands.FOK_ABS_MOVE, this, 20);
                initialMoveDone = true;
            } catch (ConnectionException e) {
                connectionErr(e);
                e.printStackTrace();
            } catch (EasyFocuser.InvalidParamException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void updateParam(EasyFocuser.Parameters p) {

    }

    @Override
    public void onCriticalError(Exception e) {

    }

    @Override
    public void updateFocuserState(EasyFocuser.FocuserState focuserState) {

    }

    @Override
    public void updateConnSate(EasyFocuser.ConnState connState) {

    }

    private void createUIComponents() {
        relMovSpinner = new JSpinner(new SpinnerNumberModel(5, -500, 500, 1));
    }
}
