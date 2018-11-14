package marcocipriani.openfocuser.manager.plus;

import marcocipriani.openfocuser.manager.AppInfo;
import marcocipriani.openfocuser.manager.ControlPanel;
import marcocipriani.openfocuser.manager.Main;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * Mini window shown in server mode to stop the application (if GUI is enabled).
 *
 * @author marcocipriani01
 * @version 1.1
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class ServerMiniWindow extends JFrame {

    /**
     * The parent component.
     */
    private JPanel parent;
    /**
     * Click to exit.
     */
    private JButton exitButton;
    private JLabel titleLabel;

    /**
     * Class constructor.
     */
    public ServerMiniWindow() {
        super(AppInfo.APP_NAME);
        setIconImage(ControlPanel.APP_LOGO);
        setContentPane(parent);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent windowEvent) {
                exit();
            }
        });
        exitButton.addActionListener(e -> exit());
        setBounds(250, 250, 280, 200);
        setResizable(false);
        setVisible(true);
    }

    private void exit() {
        if (JOptionPane.showConfirmDialog(ServerMiniWindow.this, "Are you sure?",
                AppInfo.APP_NAME, JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            Main.exit();
        }
    }
}