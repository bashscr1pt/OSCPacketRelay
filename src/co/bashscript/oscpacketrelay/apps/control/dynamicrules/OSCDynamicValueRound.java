package co.bashscript.oscpacketrelay.apps.control.dynamicrules;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import java.awt.*;
import java.util.Map;

public class OSCDynamicValueRound extends OSCDynamicValue {

    // variables
    private boolean enabled;

    // gui variables
    private JCheckBox checkbox = new JCheckBox();

    public OSCDynamicValueRound() {
        super("round");

        checkbox.addItemListener((e) -> {
            update(null);
        });
    }

    public OSCDynamicValueRound(Map<String, Object> map) {
        this();

        if(map.containsKey("enabled")) {
            this.enabled = (boolean) map.get("enabled");
        }

        checkbox.setSelected(enabled);
    }

    @Override
    public float execute(float input) {
        if(enabled) {
            return Math.round(input);
        } else {
            return input;
        }
    }

    @Override
    protected void buildPanel(JPanel panel, GridBagConstraints c) {


        c.gridx++;
        panel.add(new JLabel("Round: "), c);
        c.gridx++;
        panel.add(checkbox, c);
    }

    @Override
    protected void update(DocumentEvent e) {
        setEnabled(checkbox.isSelected());
    }

    // getters and setters
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
}
