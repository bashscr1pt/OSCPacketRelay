package co.bashscript.oscpacketrelay.apps.control;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;

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

        if(parent.getThumbnail() != null) {
            try {
                byte[] bytes = Base64.getDecoder().decode(parent.getThumbnail());
                InputStream in = new ByteArrayInputStream(bytes);
                Image bImageFromConvert = ImageIO.read(in);
                g.drawImage(bImageFromConvert, 0, 10, null);
            } catch (IOException e) {
            }
        }
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
