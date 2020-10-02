package co.bashscript.oscpacketrelay.apps.control;

import javax.swing.*;
import java.awt.*;

public class OSCPacketRelayerSlidePanel extends JPanel{

    private static int PADDING = 5;
    private static int WIDTH = 100;
    private static int HEIGHT = 95;

    // gui variables
    private JLabel labelTitle = new JLabel("TITLE", SwingConstants.CENTER);
    private JButton buttonPlay = new JButton("PLAY");

    // variables
    private OSCPacketRelayerSlide parent;

    public OSCPacketRelayerSlidePanel(OSCPacketRelayerSlide parent) {
        this.parent = parent;

        // setup the panel
        setLayout(null);
        setBackground(Color.red);
        setCursor(new Cursor(Cursor.HAND_CURSOR));

        labelTitle.setBounds(0,0,WIDTH,10);
        labelTitle.setOpaque(true);
        buttonPlay.setBounds(0,HEIGHT-25,WIDTH,25);
        buttonPlay.setOpaque(true);
        add(buttonPlay);
        add(labelTitle);

        // action handelers
        buttonPlay.addActionListener((e) -> {
            if(parent.isPlay()) {
                parent.stop();
            } else {
                parent.start();
            }
            update();
        });
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
    }

    public void update() {
        labelTitle.setText(parent.getName());
        setBounds((WIDTH + PADDING) * parent.getGridX(),(HEIGHT + PADDING) * parent.getGridY(),WIDTH,HEIGHT);

        if(parent.isPlay()) {
            buttonPlay.setText("PLAYING!");
            if(parent.getRelayer().getWindow().getSelectedSlide() == parent) {
                labelTitle.setForeground(Color.black);
                labelTitle.setBackground(Color.cyan);
            } else {
                labelTitle.setForeground(Color.black);
                labelTitle.setBackground(Color.green.darker());
            }
        } else {
            buttonPlay.setText("Play");
            if(parent.getRelayer().getWindow().getSelectedSlide() == parent) {
                labelTitle.setForeground(Color.black);
                labelTitle.setBackground(Color.cyan);
            } else {
                labelTitle.setForeground(Color.white);
                labelTitle.setBackground(Color.black);
            }
        }
    }

}
