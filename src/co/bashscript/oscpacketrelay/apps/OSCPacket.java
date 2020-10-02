package co.bashscript.oscpacketrelay.apps;

import java.util.Arrays;

public class OSCPacket {
    // Constructor
    private byte[] buffer;

    public OSCPacket(byte[] buffer) {
        this.buffer = buffer;
    }

    public String getMessage() {
        byte[] b = new byte[buffer.length-8];
        System.arraycopy(buffer,0, b,0, b.length);
        return new String(b).trim();
    }

    public float getValue() {
        byte[] bytes = new byte[4];
        System.arraycopy(buffer,buffer.length-4, bytes,0,bytes.length);
        int intBits = bytes[0] << 24 | (bytes[1] & 0xFF) << 16 | (bytes[2] & 0xFF) << 8 | (bytes[3] & 0xFF);
        return Float.intBitsToFloat(intBits);
    }

    public byte[] getBuffer() {
        return buffer;
    }

    @Override
    public String toString() {
        return "Message: " + getMessage() + " Value: " + getValue();
    }
}
