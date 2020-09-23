package co.bashscript.oscpacketrelay;

import co.bashscript.oscpacketrelay.gui.MainWindow;

import javax.swing.*;

public class OSCPacketRelayMain {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }
        catch (Exception e) {
            System.err.println("Unable to set look and feel, using default");
        }

        MainWindow frame = new MainWindow();
        frame.setVisible(true);
    }
}
