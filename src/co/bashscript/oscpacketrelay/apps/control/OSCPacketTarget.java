package co.bashscript.oscpacketrelay.apps.control;

import co.bashscript.oscpacketrelay.apps.control.dynamicrules.*;
import co.bashscript.oscpacketrelay.apps.control.dynamicrules.gui.OSCDynamicValueRuleWindow;
import co.bashscript.oscpacketrelay.utils.BSValidators;
import co.bashscript.oscpacketrelay.utils.OSCUtils;
import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.naming.ldap.Control;
import javax.swing.*;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class OSCPacketTarget {
    public enum WHEN { START, ALWAYS }

    // Variables
    private boolean enabled;
    private WHEN when;
    private String ip;
    private int port;
    private String message;
    private String value;
    private List<OSCDynamicValue> dynamicValues = new ArrayList<>();

    // internal variables
    @JsonIgnore private DatagramSocket socket;
    @JsonIgnore private float valueInput;
    @JsonIgnore private float valueOutput;

    // Constructors
    public OSCPacketTarget() {
        this.enabled = false;
        this.when = WHEN.ALWAYS;
        this.ip = "";
        this.port = 8000;
        this.message = "";
        this.value = "";
    }

    public OSCPacketTarget(Map<String, Object> map) {
        this();

        if(map.containsKey("enabled")) {
            this.enabled = (boolean) map.get("enabled");
        }

        if(map.containsKey("when")) {
            this.when = WHEN.valueOf(map.get("when").toString());
        }

        if(map.containsKey("ip")) {
            this.ip = (String) map.get("ip");
        }

        if(map.containsKey("port")) {
            this.port = (int) map.get("port");
        }

        if(map.containsKey("message")) {
            this.message = (String) map.get("message");
        }

        if(map.containsKey("value")) {
            this.value = (String) map.get("value");
        }

        // need to process rules
        if(map.containsKey("dynamicValues")) {
            dynamicValues.addAll(((List<Map>) map.get("dynamicValues")).stream()
                    .map(e -> {
                        if(e.get("key").equals("scale")) {
                            return new OSCDynamicValueScale(e);
                        } else if(e.get("key").equals("threshold")) {
                            return new OSCDynamicValueThreshold(e);
                        } else if(e.get("key").equals("threshold")) {
                            return new OSCDynamicValueThreshold(e);
                        } else if(e.get("key").equals("round")) {
                            return new OSCDynamicValueRound(e);
                        } else if(e.get("key").equals("range")) {
                            return new OSCDynamicValueRange(e);
                        }
                        return null;
                    })
                    .filter(e -> e != null)
                    .collect(Collectors.toList()));
        }
    }

    // methods
    public void send() throws IOException {
        if(socket == null) {
            socket = new DatagramSocket();
        }
        if(isEnabled()) {
            OSCUtils.send(socket, InetAddress.getByName(getIp()), getPort(), getMessage(), getValueOutput());
        }
    }

    // getters and setters
    @JsonIgnore
    public float getValueOutput() {
        return valueOutput;
    }

    @JsonIgnore
    public float getValueInput() {
        return valueInput;
    }

    public float calculatedValue(float input) {
        this.valueInput = input;
        synchronized (dynamicValues) {
            for(int i=0; i<dynamicValues.size(); i++) {
                input = dynamicValues.get(i).execute(input);
            }
        }
        this.valueOutput = input;
        return input;
    }

    public void buildDynamicValuesPanel(JPanel panel, Consumer<Integer> onDelete) {
        synchronized (dynamicValues) {
            for(int i=0; i<dynamicValues.size(); i++) {
                dynamicValues.get(i).makePanel(panel, i, onDelete);
            }
        }
    }

    public void addDynamicValue(OSCDynamicValue value) {
        synchronized (dynamicValues) {
            dynamicValues.add(value);
        }
    }

    public void removeDynamicValue(int index) {
        synchronized (dynamicValues) {
            dynamicValues.remove(index);
        }
    }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) {
        if(!enabled) {
            this.enabled = false;
            return;
        }

        if(BSValidators.isValidInet4Address(ip) &&
                BSValidators.isValidPort(port) &&
                message.trim().startsWith("/") &&
                !value.trim().equals("")
        ) {
            this.enabled = true;
        }
    }
    public List<OSCDynamicValue> getDynamicValues() { return dynamicValues; }
    public String getIp() { return ip; }
    public void setIp(String ip) { this.ip = ip; }
    public int getPort() { return port; }
    public void setPort(int port) { this.port = port; }
    public WHEN getWhen() { return when; }
    public void setWhen(WHEN when) { this.when = when; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }
}
