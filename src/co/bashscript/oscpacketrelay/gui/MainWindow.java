package co.bashscript.oscpacketrelay.gui;

import co.bashscript.oscpacketrelay.OSCPacket;
import co.bashscript.oscpacketrelay.OSCPacketTarget;
import co.bashscript.oscpacketrelay.OSCPacketRelayer;
import co.bashscript.oscpacketrelay.utils.BSValidators;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.net.SocketException;

public class MainWindow extends JFrame {
    public static final String version = "1.0.1";

    // GUI Components
    private JPanel panel;
    private JTextField textPort;
    private JButton buttonStart;
    private JButton buttonAddTarget;
    private JButton buttonRemoveTarget;
    private JTable tableTargets;
    private JTable tableSources;
    private JLabel labelStatus;

    // Internal GUI Variables
    private DefaultTableModel tableSourcesModel;
    private DefaultTableModel tableTargetsModel;
    private File workingPath;

    // Internal Variables
    private OSCPacketRelayer relayer;


    // Constructors
    public MainWindow() {
        super("OSCPacketRelay - v" + version);
        relayer = new OSCPacketRelayer(this);
        init();
    }

    private void init() {
        setContentPane(panel);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationByPlatform(true);
        setResizable(true);

        // Setup Menu
        JMenuBar menu_bar = new JMenuBar();

        JMenu menu_file = new JMenu("File");
        JMenuItem menu_file_open = new JMenuItem("Open...");
        menu_file.add(menu_file_open);
        menu_file.add(new JSeparator());
        JMenuItem menu_file_save = new JMenuItem("Save");
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
            about.setLocationRelativeTo(MainWindow.this);
            about.setVisible(true);
        });

        menu_file_open.addActionListener((e) -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Specify a file to open");
            int userSelection = fileChooser.showOpenDialog(MainWindow.this);
            if (userSelection == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                try {
                    relayer.load(file);
                    workingPath = file;
                    menu_file_save.setEnabled(true);
                    tableTargetsModel.fireTableDataChanged();
                } catch (IOException | ClassNotFoundException ioException) {
                    JOptionPane.showConfirmDialog(MainWindow.this,
                            "Error Opening: " + ioException.getMessage(),
                            "Error Opening",
                            JOptionPane.OK_OPTION);
                }
            }
        });
        menu_file_save.setEnabled(false);
        menu_file_save.addActionListener((e) -> {
            if(workingPath == null) {
                return;
            }
            try {
                relayer.save(workingPath);
            } catch (IOException ioException) {
                JOptionPane.showConfirmDialog(MainWindow.this,
                        "Error Saving: " + ioException.getMessage(),
                        "Error Saving",
                        JOptionPane.OK_OPTION);
            }
        });
        menu_file_save_as.addActionListener((e) -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Specify a file to save");
            int userSelection = fileChooser.showSaveDialog(MainWindow.this);
            if (userSelection == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                try {
                    relayer.save(file);
                    workingPath = file;
                    menu_file_save.setEnabled(true);
                } catch (IOException ioException) {
                    JOptionPane.showConfirmDialog(MainWindow.this,
                            "Error Saving: " + ioException.getMessage(),
                            "Error Saving",
                            JOptionPane.OK_OPTION);
                }
            }
        });
        menu_file_exit.addActionListener((e) -> {
            MainWindow.this.dispatchEvent(new WindowEvent(MainWindow.this, WindowEvent.WINDOW_CLOSING));
        });


        // setup buttons
        buttonAddTarget.addActionListener((e) -> {

            try {
                OSCPacketTarget target = new OSCPacketTarget();
                relayer.addTarget(target);
                tableTargetsModel.fireTableDataChanged();
            } catch (SocketException socketException) {
                socketException.printStackTrace();
            }
        });

        buttonRemoveTarget.addActionListener((e) -> {
            int row = tableTargets.getSelectedRow();
            if(row == -1) {
                return;
            }

            if (JOptionPane.showConfirmDialog(MainWindow.this,
                    "Are you sure want to remove this row?",
                    "Remove Target",
                    JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                relayer.removeTarget(row);
                tableTargetsModel.fireTableDataChanged();
            } else {
            }
        });

        // setup table
        tableSourcesModel = new DefaultTableModel(new Object[] { "Message", "Value" }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if(columnIndex == 0) {
                    return String.class;
                } else {
                    return Float.class;
                }
            }

            @Override
            public Object getValueAt(int row, int column) {
                OSCPacket[] sources = relayer.getSources();
                if(row >= sources.length) {
                    return "";
                }

                if(column == 0) {
                    return sources[row].getMessage();
                } else {
                    return sources[row].getValue();
                }
            }

            @Override
            public int getRowCount() {
                OSCPacket[] sources = relayer.getSources();
                if(sources == null) {
                    return 0;
                }

                return sources.length;
            }
        };
        tableSources.setModel(tableSourcesModel);
        tableSources.setCellSelectionEnabled(true);
        tableSources.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        tableTargetsModel = new DefaultTableModel(new Object[] { "Enabled", "Source", "Message", "Target", "Port", "Min", "Max", "Sending" }, 0) {
            @Override
            public Class getColumnClass(int columnIndex) {
                switch(columnIndex) {
                    case 0: return Boolean.class;
                    case 1: return String.class;
                    case 2: return String.class;
                    case 3: return String.class;
                    case 4: return Integer.class;
                    case 5: return Float.class;
                    case 6: return Float.class;
                    case 7: return Float.class;
                    default: return String.class;
                }
            }

            @Override
            public Object getValueAt(int row, int column) {
                OSCPacketTarget target = relayer.getTarget(row);

                switch(column) {
                    case 0: return target.isEnabled();
                    case 1: return target.getSource();
                    case 2: return target.getMessage();
                    case 3: return target.getIp();
                    case 4: return target.getPort();
                    case 5: return target.getMin();
                    case 6: return target.getMax();
                    case 7: return target.getValue();
                    default: return String.class;
                }
            }

            @Override
            public void setValueAt(Object aValue, int row, int column) {
                OSCPacketTarget target = relayer.getTarget(row);

                switch(column) {
                    case 0:
                        target.setEnabled((boolean) aValue);
                        break;
                    case 1:
                        target.setSource((String) aValue);
                        break;
                    case 2:
                        target.setMessage((String) aValue);
                        break;
                    case 3:
                        String ip = (String) aValue;
                        if(BSValidators.isValidInet4Address(ip)) {
                            target.setIp(ip);
                        }
                        break;
                    case 4:
                        int port = (int) aValue;
                        if(BSValidators.isValidPort(port)) {
                            target.setPort(port);
                        }
                        break;
                    case 5:
                        target.setMin((float) aValue);
                        break;
                    case 6:
                        target.setMax((float) aValue);
                        break;
                    case 7: break;
                }
            }

            @Override
            public boolean isCellEditable(int row, int column) {
                OSCPacketTarget target = relayer.getTarget(row);
                switch(column) {
                    case 0: return true;
                    case 1: return (target.isEnabled())?false:true;
                    case 2: return (target.isEnabled())?false:true;
                    case 3: return (target.isEnabled())?false:true;
                    case 4: return (target.isEnabled())?false:true;
                    case 5: return (target.isEnabled())?false:true;
                    case 6: return (target.isEnabled())?false:true;
                    case 7: return false;
                }
                return false;
            }

            @Override
            public int getRowCount() {
                return relayer.getTargetsSize();
            }
        };
        tableTargets.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tableTargets.setCellSelectionEnabled(true);
        tableTargets.setModel(tableTargetsModel);

        // control button
        buttonStart.addActionListener((e) -> {
            if(textPort.isEnabled()) {
                if(textPort.getText().trim().equals("")) {
                    labelStatus.setText("Invalid port, unable to start");
                    return;
                }
                int port = 0;
                try {
                    port = Integer.parseInt(textPort.getText());
                } catch (NumberFormatException ex) {
                    labelStatus.setText("Invalid port, unable to start");
                    return;
                }

                if(!BSValidators.isValidPort(port)) {
                    labelStatus.setText("Invalid port, unable to start");
                    return;
                }

                buttonStart.setText("Stop");
                textPort.setEnabled(false);
                relayer.start(port);
            } else {
                buttonStart.setText("Start");
                textPort.setEnabled(true);
                relayer.stop();
            }
        });

        labelStatus.setText("");
    }

    public void updateSourcesRows() {
        tableSourcesModel.fireTableDataChanged();
    }
    public void updateSourcesCells(int row) {
        tableSourcesModel.fireTableCellUpdated(row, 1);
    }
    public void setLabelStatus(String text) {
        labelStatus.setText(text);
    }
    public void updateTargetsCell(int row) {
        tableTargetsModel.fireTableCellUpdated(row, 7);
    }

}
