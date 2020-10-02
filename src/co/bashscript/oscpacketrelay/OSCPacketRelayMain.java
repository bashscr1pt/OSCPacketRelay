package co.bashscript.oscpacketrelay;

import co.bashscript.oscpacketrelay.apps.StartupWindow;

import javax.swing.*;

public class OSCPacketRelayMain {
    public static String VERSION = "1.1.0";

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }
        catch (Exception e) {
            System.err.println("Unable to set look and feel, using default");
        }

        StartupWindow frame = new StartupWindow();
        frame.setVisible(true);
    }
}
