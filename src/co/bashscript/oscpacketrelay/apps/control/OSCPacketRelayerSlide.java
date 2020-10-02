package co.bashscript.oscpacketrelay.apps.control;

import co.bashscript.oscpacketrelay.apps.OSCPacket;
import co.bashscript.oscpacketrelay.utils.BSStringUtils;
import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.swing.*;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class OSCPacketRelayerSlide {
    @JsonIgnore private static int PADDING = 5;
    @JsonIgnore private static int WIDTH = 100;
    @JsonIgnore private static int HEIGHT = 60;

    // Variables
    private String name = "Untitled";
    private final List<OSCPacketTarget> targets = new ArrayList<>();

    // internal variable
    private int gridX = -1;
    private int gridY = -1;
    private String thumbnail;

    // internal non stored variables
    @JsonIgnore private OSCPacketRelayerSlidePanel panel;
    @JsonIgnore private OSCPacketRelayer relayer;

    public OSCPacketRelayerSlide(OSCPacketRelayer replayer, int x, int y) {
        this.relayer = replayer;
        this.panel = getPanel();
        this.panel.update();
        setGrid(x,y);
    }

    public OSCPacketRelayerSlide(OSCPacketRelayer relayer, Map<String, Object> values) {
        this(relayer, (int) values.get("gridX"), (int) values.get("gridY"));

        if(values.containsKey("name")) {
            this.name = values.get("name").toString();
        }

        if(values.containsKey("thumbnail")) {
            if(values.get("thumbnail") != null) {
                this.thumbnail = values.get("thumbnail").toString();
            }
        }

        if(values.containsKey("targets")) {
            targets.addAll(((List<Map>) values.get("targets")).stream()
                    .map(e -> new OSCPacketTarget(e))
                    .collect(Collectors.toList()));
        }
    }

    // methods
    public void start() {
        relayer.setSlide(this);
    }
    public void stop() {
        relayer.setSlide(null);
    }

    public void addTarget(OSCPacketTarget target) {
        synchronized (targets) {
            targets.add(target);
        }
    }

    public void removeTarget(int row) {
        synchronized (targets) {
            targets.remove(row);
        }
    }


    public List<Integer> processPacket(OSCPacket packet, OSCPacketTarget.WHEN when, boolean send) {
        List<Integer> indexes = new ArrayList<>();
        synchronized (targets) {
            for(int i=0; i<targets.size(); i++) {
                if(when != null && targets.get(i).getWhen() != when) {
                    continue;
                }

                if(BSStringUtils.isNumeric(targets.get(i).getValue())) {
                    indexes.add(i);
                    targets.get(i).calculatedValue(Float.parseFloat(targets.get(i).getValue()));

                    if(send) {
                        try {
                            targets.get(i).send();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                } else if(packet != null && targets.get(i).getValue().equals(packet.getMessage())) {
                    indexes.add(i);
                    targets.get(i).calculatedValue(packet.getValue());

                    if(send) {
                        try {
                            targets.get(i).send();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }


            }
        }
        return indexes;
    }

    // Getters and Setters
    @JsonIgnore
    public OSCPacketTarget getTarget(int index) {
        if(index< 0) {
            return null;
        }
        if(index < targets.size()) {
            return targets.get(index);
        }
        return null;
    }

    public void setGrid(int x, int y) {
        setGridX(x);
        setGridY(y);
        panel.update();
    }

    @JsonIgnore
    public boolean isPlay() {
        return relayer.getCurrentSlide() == this;
    }

    @JsonIgnore
    public int getTargetsSize() {
        synchronized (targets) {
            return targets.size();
        }
    }

    // getter and setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getGridX() { return gridX; }
    public void setGridX(int gridX) { this.gridX = gridX; }
    public int getGridY() { return gridY; }
    public void setGridY(int gridY) { this.gridY = gridY; }
    @JsonIgnore
    public OSCPacketRelayerSlidePanel getPanel() {
        if(this.panel == null) {
           this.panel = new OSCPacketRelayerSlidePanel(this);
        }
        return panel;
    }
    public List<OSCPacketTarget> getTargets() { return targets; } // needed for json export
    public void setRelayer(OSCPacketRelayer relayer) { this.relayer = relayer; }
    public OSCPacketRelayer getRelayer() { return relayer; }
    public String getThumbnail() { return thumbnail; }
    public void setThumbnail(String thumbnail) { this.thumbnail = thumbnail; }
}
