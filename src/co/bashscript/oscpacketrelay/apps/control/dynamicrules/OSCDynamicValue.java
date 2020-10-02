package co.bashscript.oscpacketrelay.apps.control.dynamicrules;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.function.Consumer;

public abstract class OSCDynamicValue implements DocumentListener {
    // variables
    private String key;

    // gui variables
    private JButton buttonAddAbove = new JButton("A");
    private JButton buttonAddBelow = new JButton("B");
    private JButton buttonMoveUp= new JButton("U");
    private JButton buttonMoveDown = new JButton("D");
    private JButton buttonDelete = new JButton("X");

    // internal variables
    private boolean panelLoaded = false;

    public OSCDynamicValue(String key) {
        this.key = key;

        buttonAddAbove.setEnabled(false);
        buttonAddBelow.setEnabled(false);
        buttonMoveUp.setEnabled(false);
        buttonMoveDown.setEnabled(false);
    }

    public abstract float execute(float input);
    public void makePanel(JPanel panel, int index, Consumer<Integer> onDelete) {
        panelLoaded = true;
        resetButtons();
        buttonDelete.addActionListener((e) -> {
            onDelete.accept(index);
        });

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.gridx = 0;
        c.gridy = index;

        panel.add(buttonDelete, c);
        buildPanel(panel, c);
    }
    protected abstract void buildPanel(JPanel panel, GridBagConstraints c);
    protected abstract void update(DocumentEvent e);

    @Override
    public void insertUpdate(DocumentEvent e) {
        if(panelLoaded) {
            update(e);
        }
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        if(panelLoaded) {
            update(e);
        }
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
        if(panelLoaded) {
            update(e);
        }
    }

    private void resetButtons() {
        for( ActionListener al : buttonDelete.getActionListeners() ) {
            buttonDelete.removeActionListener( al );
        }
    }

    // getters and setters
    public String getKey() { return key; }
}
