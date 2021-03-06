package co.bashscript.oscpacketrelay;

import co.bashscript.oscpacketrelay.utils.BSMath;

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
    public void send(OSCPacket packet) throws IOException {
        if(socket == null) {
            socket = new DatagramSocket();
        }
        if(isEnabled()) {
            byte[] message = getMessage().getBytes();

            int extra = 0;
            if(message.length % 4 == 0) {
                extra = 1;
            }

            int number_of_bytes = ((int) Math.ceil((message.length+8 + extra) / 4.0)) * 4;

            // the message part
            byte[] buffer = new byte[number_of_bytes];
            System.arraycopy(message,0,buffer,0,message.length);

            // the middle bits
            message = ",f".getBytes();
            System.arraycopy(message,0,buffer,buffer.length-8,message.length);

            // value part
            int intBits =  Float.floatToIntBits(getTranslatedValue(packet.getValue()));
            byte[] value_bytes = new byte[] {(byte) (intBits >> 24), (byte) (intBits >> 16), (byte) (intBits >> 8), (byte) (intBits) };
            System.arraycopy(value_bytes,0,buffer,buffer.length-4,value_bytes.length);

            DatagramPacket sendPacket = new DatagramPacket(buffer, buffer.length, InetAddress.getByName(ip), port);
            socket.send(sendPacket);
        }
        value = getTranslatedValue(packet.getValue());
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
