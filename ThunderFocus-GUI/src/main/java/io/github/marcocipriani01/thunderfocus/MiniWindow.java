package io.github.marcocipriani01.thunderfocus;

import io.github.marcocipriani01.thunderfocus.board.Board;
import jssc.SerialPortException;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;

import static io.github.marcocipriani01.thunderfocus.Main.*;

public class MiniWindow extends JFrame implements KeyListener {

    private JFrame mainWindow = null;
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
                Main.board.run(Board.Commands.FOCUSER_REL_MOVE, null, -Integer.parseInt(field.getText()));
            } catch (IOException | SerialPortException ex) {
                connectionErr(ex);
            } catch (IllegalArgumentException ex) {
                valueOutOfLimits(ex);
            }
        });
        left.addKeyListener(this);
        left.setFocusTraversalKeysEnabled(false);
        right.addActionListener(e -> {
            try {
                Main.board.run(Board.Commands.FOCUSER_REL_MOVE, null, Integer.parseInt(field.getText()));
            } catch (IOException | SerialPortException ex) {
                connectionErr(ex);
            } catch (IllegalArgumentException ex) {
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
        JOptionPane.showMessageDialog(this, i18n("error.invalid"), APP_NAME, JOptionPane.ERROR_MESSAGE);
    }

    private void connectionErr(Exception e) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(this, i18n("error.connection"), APP_NAME, JOptionPane.ERROR_MESSAGE);
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
        if (b) SwingUtilities.invokeLater(() -> {
            field.setText(String.valueOf(settings.relativeStepSize));
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            requestFocus();
        });
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
        if (mainWindow != null) mainWindow.setState(Frame.NORMAL);
    }

    public void setMainWindow(JFrame mainWindow) {
        this.mainWindow = mainWindow;
    }
}