package marcocipriani01.thunder.focus;

import marcocipriani01.thunder.focus.io.ConnectionException;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import static marcocipriani01.thunder.focus.Main.APP_LOGO;
import static marcocipriani01.thunder.focus.Main.APP_NAME;

public abstract class MiniWindow extends JFrame implements KeyListener {

    private JPanel parent;
    private JButton left;
    private JTextField field;
    private JButton right;

    public MiniWindow() {
        super(APP_NAME);
        setIconImage(APP_LOGO);
        setContentPane(parent);
        addKeyListener(this);
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
                dispose();
            }
        });

        left.addActionListener(e -> {
            try {
                Main.focuser.run(EasyFocuser.Commands.FOK_REL_MOVE, null, -Integer.parseInt(field.getText()));
            } catch (ConnectionException ex) {
                connectionErr(ex);
            } catch (EasyFocuser.InvalidParamException | NumberFormatException ex) {
                valueOutOfLimits(ex);
            }
        });
        left.addKeyListener(this);
        left.setFocusTraversalKeysEnabled(false);
        right.addActionListener(e -> {
            try {
                Main.focuser.run(EasyFocuser.Commands.FOK_REL_MOVE, null, Integer.parseInt(field.getText()));
            } catch (ConnectionException ex) {
                connectionErr(ex);
            } catch (EasyFocuser.InvalidParamException | NumberFormatException ex) {
                valueOutOfLimits(ex);
            }
        });
        right.addKeyListener(this);
        right.setFocusTraversalKeysEnabled(false);

        setResizable(false);
        setAlwaysOnTop(true);
        pack();
        setLocation(300, 300);
    }

    private void valueOutOfLimits(Exception e) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(this, "Valore fuori dai limiti o non valido.", APP_NAME, JOptionPane.ERROR_MESSAGE);
    }

    private void connectionErr(ConnectionException e) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(this, "Errore di connessione!", APP_NAME, JOptionPane.ERROR_MESSAGE);
    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {

    }

    @Override
    public void setVisible(boolean b) {
        super.setVisible(b);
        if (b) {
            SwingUtilities.invokeLater(() -> {
                field.setText(String.valueOf(Main.focuser.getRequestedRelPos()));
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                requestFocus();
            });
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int keyCode = e.getKeyCode();
        if (keyCode == KeyEvent.VK_RIGHT) {
            right.doClick();
        } else if (keyCode == KeyEvent.VK_LEFT) {
            left.doClick();
        } else if (keyCode == KeyEvent.VK_ESCAPE) {
            dispose();
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        onHide();
    }

    protected abstract void onHide();
}
