package co.bashscript.oscpacketrelay.gui;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;

public abstract class OSCPacketRelayAppWindow extends JFrame {
    // variables
    private File workingPath;

    public OSCPacketRelayAppWindow(String title) {
        super(title);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationByPlatform(true);
        setResizable(true);

        // Setup Menu
        JMenuBar menu_bar = new JMenuBar();

        JMenu menu_file = new JMenu("File");
        JMenuItem menu_file_open = new JMenuItem("Open...");
        menu_file.add(menu_file_open);
        menu_file.add(new JSeparator());
        JMenuItem menu_file_save = new JMenuItem("Save");
        menu_file_save.setEnabled(false);
        menu_file_save.setAccelerator(KeyStroke.getKeyStroke("control S"));
        menu_file.add(menu_file_save);
        JMenuItem menu_file_save_as = new JMenuItem("Save As...");
        menu_file.add(menu_file_save_as);
        menu_file.add(new JSeparator());
        JMenuItem menu_file_exit = new JMenuItem("Exit");
        menu_file.add(menu_file_exit);
        menu_bar.add(menu_file);

        JMenu menu_help = new JMenu("Help");
        JMenuItem menu_help_about = new JMenuItem("About");
        menu_help.add(menu_help_about);
        menu_bar.add(menu_help);
        setJMenuBar(menu_bar);

        // menu actions

        menu_help_about.addActionListener((e) -> {
            About about = new About();
            about.setTitle("About");
            about.pack();
            about.setLocationRelativeTo(OSCPacketRelayAppWindow.this);
            about.setVisible(true);
        });

        menu_file_open.addActionListener((e) -> {
            JFileChooser fileChooser = new JFileChooser();
            FileNameExtensionFilter filter = new FileNameExtensionFilter("JSON", "json");
            fileChooser.setFileFilter(filter);
            fileChooser.setDialogTitle("Specify a file to open");
            int userSelection = fileChooser.showOpenDialog(OSCPacketRelayAppWindow.this);
            if (userSelection == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                try {
                    onFileOpen(file);
                    workingPath = file;
                    menu_file_save.setEnabled(true);
                } catch (IOException | ClassNotFoundException ioException) {
                    JOptionPane.showConfirmDialog(OSCPacketRelayAppWindow.this, "Error Opening: " + ioException.getMessage(), "Error Opening", JOptionPane.OK_OPTION);
                }
            }
        });
        menu_file_save.addActionListener((e) -> {
            if(workingPath == null) {
                return;
            }

            try {
                onFileSave(workingPath);
            } catch (IOException ioException) {
                JOptionPane.showConfirmDialog(OSCPacketRelayAppWindow.this,"Error Saving: " + ioException.getMessage(), "Error Saving", JOptionPane.OK_OPTION);
            }
        });
        menu_file_save_as.addActionListener((e) -> {
            JFileChooser fileChooser = new JFileChooser();
            FileNameExtensionFilter filter = new FileNameExtensionFilter("JSON", "json");
            fileChooser.setFileFilter(filter);
            fileChooser.setDialogTitle("Specify a file to save");
            int userSelection = fileChooser.showSaveDialog(OSCPacketRelayAppWindow.this);
            if (userSelection == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                if(!file.getName().endsWith(".json")) {
                    file = new File(file.toString() + ".json");
                }
                try {
                    onFileSave(file);
                } catch (IOException ioException) {
                    JOptionPane.showConfirmDialog(OSCPacketRelayAppWindow.this,"Error Saving: " + ioException.getMessage(), "Error Saving", JOptionPane.OK_OPTION);
                }
            }
        });
        menu_file_exit.addActionListener((e) -> {
            OSCPacketRelayAppWindow.this.dispatchEvent(new WindowEvent(OSCPacketRelayAppWindow.this, WindowEvent.WINDOW_CLOSING));
        });
    }

    // abstract methods
    public abstract void onFileOpen(File file) throws IOException, ClassNotFoundException;
    public abstract void onFileSave(File path) throws IOException;
}
