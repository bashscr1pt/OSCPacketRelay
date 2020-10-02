package co.bashscript.oscpacketrelay.apps.control;

import co.bashscript.oscpacketrelay.apps.OSCPacket;

import javax.swing.*;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public class OSCPacketRelayer {

    // variables
    private ControlWindow window;

    // internal variables
    private OSCPacketTarget.WHEN currentWhen;
    private OSCPacketRelayerSlide currentSlide;
    private Thread thread_listen;
    private DatagramSocket sock = null;
    private final Deque<OSCPacket> queue = new ConcurrentLinkedDeque<>();
    private final Map<String, OSCPacket> sources = new HashMap<>();

    // constructor
    public OSCPacketRelayer(ControlWindow window) {
        this.window = window;

        Thread thread_send = new Thread(() -> {
            while(!Thread.interrupted()) {

                // while we have nothing to send, sleep
                OSCPacket packet = null;
                if(queue.isEmpty()) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                else {
                    packet = queue.pop();
                }

                // playing slide
                if(currentSlide != null) {
                    currentSlide.processPacket(packet, currentWhen, true);
                    currentWhen = OSCPacketTarget.WHEN.ALWAYS;
                }

                // selected slide
                for (OSCPacketRelayerSlide slide : window.getSlides()) {
                    if(slide == window.getSelectedSlide()) {
                        window.getSelectedSlide().processPacket(packet, null,false).stream()
                                .forEach(index -> {
                                    SwingUtilities.invokeLater(() -> {
                                        window.fireSlideTargetsUpdated(index);
                                    });
                                });
                    } else {
                        slide.processPacket(packet, null,false);
                    }
                }
            }
        });
        thread_send.setName("OSC Sending Thread");
        thread_send.start();
    }

    public synchronized void setSlide(OSCPacketRelayerSlide slide) {
        OSCPacketRelayerSlide previous_slide = this.currentSlide;
        this.currentSlide = slide;
        currentWhen = OSCPacketTarget.WHEN.START;

        if(previous_slide != null) {
            previous_slide.getPanel().update();
        }
    }

    public void start(int port) {
        if(thread_listen != null) {
            return;
        }

        thread_listen = new Thread(() -> {
            try {
                sock = new DatagramSocket(new InetSocketAddress(port));
            }
            catch(IOException e) {
                System.err.println("IOException " + e);
            }
            while(Thread.currentThread() == thread_listen && !Thread.interrupted())
            {
                try {
                    byte[] buffer = new byte[4098];
                    DatagramPacket reply = new DatagramPacket(buffer, buffer.length);
                    sock.receive(reply);

                    byte[] data = new byte[reply.getLength()];
                    if(data.length > 0) {
                        System.arraycopy(reply.getData(), 0, data, 0, reply.getLength());
                        OSCPacket packet = new OSCPacket(data);
                        store(packet);
                    }
                }
                catch(IOException e) {
                    if(e.getMessage().contains("socket closed")) { }
                    else {
                        System.err.println("IOException " + e);
                    }
                }
            }
        });
        thread_listen.setName("OSCPacketRelayer - Incoming Queue");
        thread_listen.start();
    }

    public void stop() {
        if(thread_listen == null) {
            return;
        }
        thread_listen.interrupt();
        thread_listen = null;

        if(sock != null) {
            sock.close();
        }
    }

    private void store(OSCPacket packet) {
        AtomicBoolean refresh_cell = new AtomicBoolean(false);
        if(sources.containsKey(packet.getMessage())) {
            refresh_cell.set(true);
        }
        synchronized (sources) {
            sources.put(packet.getMessage(), packet);
        }

        // don't wanna overload queue
        while(queue.size() >= 100) {
            queue.pop();
        }
        queue.add(packet);

        // Update GUI's
        SwingUtilities.invokeLater(() -> {
            OSCPacket[] sources = getSourcesArray();
            for(int i=0; i<sources.length; i++) {
                if(sources[i].getMessage().equals(packet.getMessage())) {
                    window.fireSourceUpdated(refresh_cell.get()?i:-1, sources[i], packet);
                    break;
                }
            }

            window.fireStatus("Queue Size" + queue.size());
        });
    }

    // getter and setters
    public OSCPacket[] getSourcesArray() {
        synchronized (sources) {
            List<OSCPacket> collect = sources.keySet().stream()
                    .sorted((a, b) -> a.compareTo(b))
                    .map(e -> sources.get(e))
                    .collect(Collectors.toList());
            return collect.toArray(new OSCPacket[collect.size()]);
        }
    }
    public synchronized OSCPacketRelayerSlide getCurrentSlide() { return currentSlide; }
    public ControlWindow getWindow() { return window; }
}
