package co.bashscript.oscpacketrelay.apps.control.dynamicrules;

import co.bashscript.oscpacketrelay.utils.BSMath;
import co.bashscript.oscpacketrelay.utils.BSStringUtils;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import java.awt.*;
import java.util.Map;

public class OSCDynamicValueRange extends OSCDynamicValue {

    // variables
    private float oldMin = 0f;
    private float oldMax = 100f;
    private float newMin = 0f;
    private float newMax = 100f;

    // gui elements
    private JTextField old_min_text = new JTextField("" + oldMin);
    private JTextField old_max_text = new JTextField("" + oldMax);
    private JTextField new_min_text = new JTextField("" + newMin);
    private JTextField new_max_text = new JTextField("" + newMax);

    public OSCDynamicValueRange() {
        super("range");

        old_min_text.getDocument().addDocumentListener(this);
        old_max_text.getDocument().addDocumentListener(this);
        new_min_text.getDocument().addDocumentListener(this);
        new_max_text.getDocument().addDocumentListener(this);

        old_min_text.setPreferredSize(new Dimension(50,old_min_text.getPreferredSize().height));
        old_max_text.setPreferredSize(new Dimension(50,old_min_text.getPreferredSize().height));
        new_min_text.setPreferredSize(new Dimension(50,old_min_text.getPreferredSize().height));
        new_max_text.setPreferredSize(new Dimension(50,old_min_text.getPreferredSize().height));
    }

    public OSCDynamicValueRange(Map<String, Object> map) {
        this();

        // load
        if(map.containsKey("oldMin")) {
            this.oldMin = ((Double)map.get("oldMin")).floatValue();
        }

        if(map.containsKey("oldMax")) {
            this.oldMax = ((Double)map.get("oldMax")).floatValue();
        }

        if(map.containsKey("newMin")) {
            this.newMin = ((Double)map.get("newMin")).floatValue();
        }

        if(map.containsKey("newMax")) {
            this.newMax = ((Double)map.get("newMax")).floatValue();
        }

        old_min_text.setText("" + getOldMin());
        old_max_text.setText("" + getOldMax());
        new_min_text.setText("" + getNewMin());
        new_max_text.setText("" + getNewMax());
    }

    @Override
    public float execute(float input) {
        return scaleBetween(input, getOldMin(), getOldMax(), getNewMin(), getNewMax());
    }

    @Override
    protected void buildPanel(JPanel panel, GridBagConstraints c) {
        c.gridx++;
        panel.add(new JLabel("OMin:"),c);
        c.gridx++;
        panel.add(old_min_text, c);
        c.gridx++;
        panel.add(new JLabel("OMax:"), c);
        c.gridx++;
        panel.add(old_max_text, c);
        c.gridx++;
        panel.add(new JLabel("NMin:"), c);
        c.gridx++;
        panel.add(new_min_text, c);
        c.gridx++;
        panel.add(new JLabel("NMax:"), c);
        c.gridx++;
        panel.add(new_max_text, c);
    }

    @Override
    protected void update(DocumentEvent e) {
        setOldMin(BSStringUtils.tryParseFloat(old_min_text.getText(), getOldMin()));
        setOldMax(BSStringUtils.tryParseFloat(old_max_text.getText(), getOldMax()));
        setNewMin(BSStringUtils.tryParseFloat(new_min_text.getText(), getNewMin()));
        setNewMax(BSStringUtils.tryParseFloat(new_max_text.getText(), getNewMax()));
    }

    private float scaleBetween(float unscaledNum, float minAllowed, float maxAllowed, float min, float max) {
        float normalizedValue = BSMath.InverseLerp(minAllowed, maxAllowed, unscaledNum);
        return BSMath.Lerp(min, max, normalizedValue);
    }

    // getters and setters
    public float getOldMin() { return oldMin; }
    public void setOldMin(float oldMin) { this.oldMin = oldMin; }
    public float getOldMax() { return oldMax; }
    public void setOldMax(float oldMax) { this.oldMax = oldMax; }
    public float getNewMin() { return newMin; }
    public void setNewMin(float newMin) { this.newMin = newMin; }
    public float getNewMax() { return newMax; }
    public void setNewMax(float newMax) { this.newMax = newMax; }
}
