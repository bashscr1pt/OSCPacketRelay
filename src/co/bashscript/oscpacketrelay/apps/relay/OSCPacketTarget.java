package co.bashscript.oscpacketrelay.apps.relay;

import co.bashscript.oscpacketrelay.apps.OSCPacket;
import co.bashscript.oscpacketrelay.utils.BSMath;
import co.bashscript.oscpacketrelay.utils.OSCUtils;

import java.io.IOException;
import java.io.Serializable;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class OSCPacketTarget implements Serializable {
    private static final long serialversionUID = 1L;

    // Variables
    private boolean enabled;
    private String source;
    private String ip;
    private String message;
    private int port;
    private float min;
    private float max;
    private boolean round;
    private float scale;
    private transient float value;

    // internal variables
    private transient DatagramSocket socket;

    // Constructors
    public OSCPacketTarget() throws SocketException {
        this.enabled = false;
        this.source = "";
        this.ip = "";
        this.message = "";
        this.port = 8000;
        this.min = 0;
        this.max = 100;
        this.round = false;
        this.scale = 1;
    }

    // methods
    public void send(float v) throws IOException {
        if(socket == null) {
            socket = new DatagramSocket();
        }
        if(isEnabled()) {
            OSCUtils.send(socket, InetAddress.getByName(getIp()), getPort(), getMessage(), getTranslatedValue(v));
        }
        value = getTranslatedValue(v);
    }

    public float getTranslatedValue(float value) {
        float v = scaleBetween(value, 0, 100, getMin(), getMax());

        if(isRound()) {
            v = Math.round(v);
        }
        return v * scale;
    }

    private float scaleBetween(float unscaledNum, float minAllowed, float maxAllowed, float min, float max) {
        float normalizedValue = BSMath.InverseLerp(minAllowed, maxAllowed, unscaledNum);
        return BSMath.Lerp(min, max, normalizedValue);
    }

    // getters and setters
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public String getIp() { return ip; }
    public void setIp(String ip) { this.ip = ip; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public int getPort() { return port; }
    public void setPort(int port) { this.port = port; }
    public float getMin() { return min; }
    public void setMin(float min) { this.min = min; }
    public float getMax() { return max; }
    public void setMax(float max) { this.max = max; }
    public boolean isRound() { return round; }
    public void setRound(boolean round) { this.round = round; }
    public float getScale() { return scale; }
    public void setScale(float scale) { this.scale = scale; }
    public float getValue() { return value; }
}
