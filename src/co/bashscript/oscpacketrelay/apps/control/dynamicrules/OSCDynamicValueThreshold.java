package co.bashscript.oscpacketrelay.apps.control.dynamicrules;

import co.bashscript.oscpacketrelay.utils.BSStringUtils;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import java.awt.*;
import java.util.Map;

public class OSCDynamicValueThreshold extends OSCDynamicValue {

    // variables
    private float defaultValue = 0f;
    private float threshold = 1f;

    // gui variables
    private JTextField textThreshold = new JTextField("" + threshold);
    private JTextField textDefault = new JTextField("" + defaultValue);

    public OSCDynamicValueThreshold() {
        super("threshold");

        textThreshold.getDocument().addDocumentListener(this);
        textDefault.getDocument().addDocumentListener(this);
        textThreshold.setPreferredSize(new Dimension(50, textThreshold.getPreferredSize().height));
        textDefault.setPreferredSize(new Dimension(50, textDefault.getPreferredSize().height));
    }

    public OSCDynamicValueThreshold(Map<String, Object> map) {
        this();

        if(map.containsKey("threshold")) {
            this.threshold = ((Double)map.get("threshold")).floatValue();
        }

        if(map.containsKey("defaultValue")) {
            this.defaultValue = ((Double)map.get("defaultValue")).floatValue();
        }

        textThreshold.setText("" + getThreshold());
        textDefault.setText("" + getDefaultValue());
    }

    @Override
    public float execute(float input) {
        if(input < threshold) {
            return defaultValue;
        }
        return input;
    }

    @Override
    protected void buildPanel(JPanel panel, GridBagConstraints c) {

        c.gridx++;
        panel.add(new JLabel("Threshold: "), c);
        c.gridx++;
        panel.add(textThreshold, c);

        c.gridx++;
        panel.add(new JLabel("Default: "), c);
        c.gridx++;
        panel.add(textDefault, c);
    }

    @Override
    protected void update(DocumentEvent e) {
        setThreshold(BSStringUtils.tryParseFloat(textThreshold.getText(),getThreshold()));
        setDefaultValue(BSStringUtils.tryParseFloat(textDefault.getText(),getDefaultValue()));
    }

    // getters and setters
    public float getThreshold() { return threshold; }
    public void setThreshold(float threshold) { this.threshold = threshold; }
    public float getDefaultValue() { return defaultValue; }
    public void setDefaultValue(float defaultValue) { this.defaultValue = defaultValue; }
}
