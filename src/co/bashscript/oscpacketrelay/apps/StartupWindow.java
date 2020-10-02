package co.bashscript.oscpacketrelay.apps;

import co.bashscript.oscpacketrelay.OSCPacketRelayMain;
import co.bashscript.oscpacketrelay.apps.control.ControlWindow;
import co.bashscript.oscpacketrelay.apps.relay.RelayWindow;

import javax.swing.*;
import java.awt.event.WindowEvent;

public class StartupWindow extends JFrame {
    private JButton buttonRelay;
    private JButton buttonControl;
    private JPanel contentPanel;

    public StartupWindow() {
        super("OSCPacketRelay - v" + OSCPacketRelayMain.VERSION);
        init();
    }

    private void init() {
        setContentPane(contentPanel);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(300,100);
        setLocationByPlatform(true);
        setResizable(false);

        buttonRelay.addActionListener((e) -> {
            RelayWindow window = new RelayWindow();
            window.setVisible(true);
            StartupWindow.this.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
            StartupWindow.this.dispatchEvent(new WindowEvent(StartupWindow.this, WindowEvent.WINDOW_CLOSING));
        });

        buttonControl.addActionListener((e) -> {
            ControlWindow window = new ControlWindow();
            window.setVisible(true);
            StartupWindow.this.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
            StartupWindow.this.dispatchEvent(new WindowEvent(StartupWindow.this, WindowEvent.WINDOW_CLOSING));
        });
    }
}
