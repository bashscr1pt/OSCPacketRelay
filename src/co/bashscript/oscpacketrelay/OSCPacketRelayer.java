package co.bashscript.oscpacketrelay;

import co.bashscript.oscpacketrelay.gui.MainWindow;

import javax.swing.*;
import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public class OSCPacketRelayer {
    // variables
    private MainWindow window;

    // internal variables
    private Thread thread_listen;
    private Thread thread_send;
    private DatagramSocket sock = null;
    private final Deque<OSCPacket> queue = new ConcurrentLinkedDeque<>();
    private final Map<String, OSCPacket> sources = new HashMap<>();
    private final List<OSCPacketTarget> targets = new ArrayList<>();

    // constructor
    public OSCPacketRelayer(MainWindow window) {
        this.window = window;

        thread_send = new Thread(() -> {
            while(!Thread.interrupted()) {
                // while we have nothing to send, sleep
                while(queue.isEmpty()) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        break;
                    }
                }

                OSCPacket packet = queue.pop();

                List<Integer> cells_to_update = new ArrayList<>();
                synchronized (targets) {
                    for(int i=0; i<targets.size(); i++) {
                        if(targets.get(i).getSource().equals(packet.getMessage())) {
                            cells_to_update.add(i);
                            try {
                                targets.get(i).send(packet);
                            } catch (Throwable e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }

                SwingUtilities.invokeLater(() -> {
                    cells_to_update.stream().forEach(e -> window.updateTargetsCell(e));
                    window.setLabelStatus("Queue Size: " + queue.size());
                });
            }
        });
        thread_send.setName("OSC Sending Thread");
        thread_send.start();
    }

    // methods
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
                    if( e.getMessage().contains("socket closed")) { }
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
            OSCPacket[] sources = getSources();
            if(refresh_cell.get()) {
                for(int i=0; i<sources.length; i++) {
                    if(sources[i] == packet) {
                        window.updateSourcesCells(i);
                        break;
                    }
                }
            } else {
                window.updateSourcesRows();
            }

            window.setLabelStatus("Queue Size: " + queue.size());
        });
    }

    public void addTarget(OSCPacketTarget target) {
        synchronized (targets) {
            targets.add(target);
        }
    }

    public void removeTarget(int index) {
        synchronized (targets) {
            targets.remove(index);
        }
    }

    public void save(File file) throws IOException {
        ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file));
        synchronized (targets) {
            out.writeObject(targets);
        }
        out.close();
    }

    public void load(File file) throws IOException, ClassNotFoundException {
        ObjectInputStream in = new ObjectInputStream(new FileInputStream(file));

        List<OSCPacketTarget> objects = (List<OSCPacketTarget>)in.readObject();
        in.close();
        synchronized (targets) {
            targets.clear();
            targets.addAll(objects);
        }
    }

    // getters and setters
    public OSCPacketTarget getTarget(int index) { return targets.get(index); }
    public int getTargetsSize() { return targets.size(); }

    public OSCPacket[] getSources() {
        synchronized (sources) {
            List<OSCPacket> collect = sources.keySet().stream()
                    .sorted((a, b) -> a.compareTo(b))
                    .map(e -> sources.get(e))
                    .collect(Collectors.toList());
            return collect.toArray(new OSCPacket[collect.size()]);
        }
    }
}
