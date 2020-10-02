package co.bashscript.oscpacketrelay.apps.control.dynamicrules.gui;

import co.bashscript.oscpacketrelay.apps.control.ControlWindow;
import co.bashscript.oscpacketrelay.apps.control.OSCPacketTarget;
import co.bashscript.oscpacketrelay.apps.control.dynamicrules.*;
import co.bashscript.oscpacketrelay.gui.OSCPacketRelayAppWindow;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;

public class OSCDynamicValueRuleWindow extends JDialog implements ActionListener {
    private JButton buttonScale;
    private JButton buttonRange;
    private JButton buttonThreshold;
    private JButton buttonRound;
    private JPanel panel;

    // variables
    private ControlWindow window;
    private OSCPacketTarget target;


    public OSCDynamicValueRuleWindow(ControlWindow window, OSCPacketTarget target) {
        super(window, true);
        this.window = window;
        this.target = target;

        setContentPane(panel);
        pack();
        setTitle("Which Dynamic Rule do you want?");
        setResizable(false);
        setLocationRelativeTo(window);

        // hooks
        buttonScale.addActionListener(this);
        buttonRange.addActionListener(this);
        buttonThreshold.addActionListener(this);
        buttonRound.addActionListener(this);
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        String text = ((JButton) e.getSource()).getText();
        if(text.toLowerCase().equals("range")) {
            target.addDynamicValue(new OSCDynamicValueRange());
        } else if(text.toLowerCase().equals("threshold")) {
            target.addDynamicValue(new OSCDynamicValueThreshold());
        } else if(text.toLowerCase().equals("round")) {
            target.addDynamicValue(new OSCDynamicValueRound());
        } else if(text.toLowerCase().equals("scale")) {
            target.addDynamicValue(new OSCDynamicValueScale());
        }


        window.fireSlideTargetDynamicValue(target);

        this.dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
    }
}
