package co.bashscript.oscpacketrelay.utils;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class OSCUtils {

    public static void send(DatagramSocket socket, InetAddress ip, int port, String key, float value) throws IOException {
        byte[] message = key.getBytes();

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
        int intBits =  Float.floatToIntBits(value);
        byte[] value_bytes = new byte[] {(byte) (intBits >> 24), (byte) (intBits >> 16), (byte) (intBits >> 8), (byte) (intBits) };
        System.arraycopy(value_bytes,0,buffer,buffer.length-4,value_bytes.length);

        DatagramPacket sendPacket = new DatagramPacket(buffer, buffer.length, ip, port);
        socket.send(sendPacket);
    }
}
