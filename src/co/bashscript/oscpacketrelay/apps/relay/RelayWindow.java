package co.bashscript.oscpacketrelay.apps.relay;

import co.bashscript.oscpacketrelay.*;
import co.bashscript.oscpacketrelay.apps.OSCPacket;
import co.bashscript.oscpacketrelay.gui.OSCPacketRelayAppWindow;
import co.bashscript.oscpacketrelay.utils.BSValidators;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.io.File;
import java.io.IOException;
import java.net.SocketException;

public class RelayWindow extends OSCPacketRelayAppWindow {

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

    // Internal Variables
    private OSCPacketRelayer relayer = new OSCPacketRelayer(this);

    // Constructors
    public RelayWindow() {
        super( "OSCPacketRelay - Relayer - v" + OSCPacketRelayMain.VERSION);
        init();
    }

    private void init() {
        setContentPane(panel);
        setSize(800, 600);

        // setup buttons
        buttonAddTarget.addActionListener((e) -> {

            try {
                OSCPacketTarget target = new OSCPacketTarget();
                relayer.addTarget(target);
                fireTargetsUpdated(-1, target);
            } catch (SocketException socketException) {
                socketException.printStackTrace();
            }
        });

        buttonRemoveTarget.addActionListener((e) -> {
            int row = tableTargets.getSelectedRow();
            if(row == -1) {
                return;
            }

            if (JOptionPane.showConfirmDialog(RelayWindow.this,
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

        tableTargetsModel = new DefaultTableModel(new Object[] {
                "Enabled",
                "Source",
                "Message",
                "Target",
                "Port",
                "Min",
                "Max",
                "Round",
                "Scale",
                "Sending" }, 0) {
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
                    case 7: return Boolean.class;
                    case 8: return Float.class;
                    case 9: return Float.class;
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
                    case 7: return target.isRound();
                    case 8: return target.getScale();
                    case 9: return target.getValue();
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
                    case 7:
                        target.setRound((boolean) aValue);
                        break;
                    case 8:
                        target.setScale((float) aValue);
                        break;
                    case 9: break;
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
                    case 5: return true;
                    case 6: return true;
                    case 7: return true;
                    case 8: return true;
                    case 9: return false;
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

    @Override
    public void onFileOpen(File file) throws IOException, ClassNotFoundException {
        relayer.load(file);
        tableTargetsModel.fireTableDataChanged();
    }

    @Override
    public void onFileSave(File path) throws IOException {
        relayer.save(path);
    }

    public void fireSourceUpdated(int index, OSCPacket source, OSCPacket packet) {
        if(index == -1) {
            tableSourcesModel.fireTableDataChanged();
        } else {
            tableSourcesModel.fireTableCellUpdated(index, 1);
        }
    }

    public void fireTargetsUpdated(int index, OSCPacketTarget target) {
        if(index == -1) {
            tableTargetsModel.fireTableDataChanged();
        } else {
            tableTargetsModel.fireTableCellUpdated(index, 7);
        }
    }

    public void fireStatus(String text) {
        labelStatus.setText(text);
    }
}
