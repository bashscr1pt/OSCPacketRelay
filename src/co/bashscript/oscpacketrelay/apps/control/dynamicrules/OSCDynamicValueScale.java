package co.bashscript.oscpacketrelay.apps.control.dynamicrules;

import co.bashscript.oscpacketrelay.utils.BSStringUtils;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.util.Map;

public class OSCDynamicValueScale extends OSCDynamicValue  {

    // variables
    private float scale = 1f;

    // gui variables
    private JTextField text = new JTextField("" + scale);

    public OSCDynamicValueScale() {
        super("scale");

        text.getDocument().addDocumentListener(this);
        text.setPreferredSize(new Dimension(50,text.getPreferredSize().height));
    }

    public OSCDynamicValueScale(Map<String, Object> map) {
        this();

        if(map.containsKey("scale")) {
            this.scale = ((Double)map.get("scale")).floatValue();
        }

        text.setText("" + getScale());
    }

    @Override
    public float execute(float input) {
        return input * scale;
    }

    @Override
    protected void buildPanel(JPanel panel, GridBagConstraints c) {


        c.gridx++;
        panel.add(new JLabel("Scale: "),c);
        c.gridx++;
        panel.add(text,c);
    }

    @Override
    protected void update(DocumentEvent e) {
        setScale(BSStringUtils.tryParseFloat(text.getText(),getScale()));
    }

    // getters and setters
    public float getScale() { return scale; }
    public void setScale(float scale) { this.scale = scale; }

}
