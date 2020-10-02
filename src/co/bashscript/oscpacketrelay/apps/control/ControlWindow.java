package co.bashscript.oscpacketrelay.apps.control;

import co.bashscript.oscpacketrelay.*;
import co.bashscript.oscpacketrelay.apps.OSCPacket;
import co.bashscript.oscpacketrelay.apps.control.dynamicrules.gui.OSCDynamicValueRuleWindow;
import co.bashscript.oscpacketrelay.gui.OSCPacketRelayAppWindow;
import co.bashscript.oscpacketrelay.utils.BSStringUtils;
import co.bashscript.oscpacketrelay.utils.BSValidators;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.*;
import java.util.*;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class ControlWindow extends OSCPacketRelayAppWindow {
    // GUI Variables
    private JPanel contentPanel;
    private JPanel panelSlides;
    private JButton buttonAddSlide;
    private JTextField textPort;
    private JButton buttonStart;
    private JTable tableSources;
    private JPanel panelSlideValues;
    private JTextField textSlideTitle;
    private JTextField textSlideGridX;
    private JTextField textSlideGridY;
    private JTable tableSlideTargets;
    private JPanel panelSlideDynamicValuesArea;
    private JButton buttonSlideTargetRemove;
    private JButton buttonSlideTargetAdd;
    private JButton buttonSlideGridUpdate;
    private JSplitPane splitPaneSlideValues;
    private JButton buttonSlideDelete;
    private JLabel labelSlideTargetDynamicInput;
    private JLabel labelSlideTargetDynamicOutput;
    private JPanel panelSlideDynamicValues;
    private JButton buttonSlideDynamicValueAdd;

    // Internal Variables
    private final List<OSCPacketRelayerSlide> grid = new ArrayList<>();
    private final OSCPacketRelayer relayer = new OSCPacketRelayer(this);
    private OSCPacketRelayerSlide selectedSlide = null;
    private OSCPacketTarget selectedTarget = null;

    private DefaultTableModel tableSourcesModel;
    private DefaultTableModel tableSlideTargetsModel;
    private Consumer<Integer> dynamicValueOnDelete;

    public ControlWindow() {
        super("OSCPacketRelay - Control - v" + OSCPacketRelayMain.VERSION);
        init();
    }

    private void init() {
        setContentPane(contentPanel);
        setSize(800, 600);
        ((JScrollPane)panelSlides.getParent().getParent()).getVerticalScrollBar().setUnitIncrement(16);
        ((JScrollPane)panelSlides.getParent().getParent()).getHorizontalScrollBar().setUnitIncrement(16);

        panelSlides.setLayout(null);
        panelSlideValues.setVisible(false);
        panelSlideDynamicValuesArea.setVisible(false);

        // tables
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
                OSCPacket[] sources = relayer.getSourcesArray();
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
                OSCPacket[] sources = relayer.getSourcesArray();
                if(sources == null) {
                    return 0;
                }

                return sources.length;
            }
        };
        tableSources.setModel(tableSourcesModel);
        tableSources.setCellSelectionEnabled(true);
        tableSources.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        tableSlideTargetsModel = new DefaultTableModel(new Object[] {
               "Enabled", "When", "IP", "Port", "Message", "Value", "Output" }, 0) {

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                switch(columnIndex) {
                    case 0: return Boolean.class;
                    case 1: return String.class;
                    case 2: return String.class;
                    case 3: return Integer.class;
                    case 4: return String.class;
                    case 5: return String.class;
                    case 6: return Float.class;
                }
                return String.class;
            }

            @Override
            public Object getValueAt(int row, int column) {
                if(selectedSlide == null) {
                    return "";
                }

                OSCPacketTarget target = selectedSlide.getTarget(row);

                switch (column) {
                    case 0: return target.isEnabled();
                    case 1: return target.getWhen().toString();
                    case 2: return target.getIp();
                    case 3: return target.getPort();
                    case 4: return target.getMessage();
                    case 5: return target.getValue();
                    case 6: return target.getValueOutput();
                }
                return null;
            }

            @Override
            public int getRowCount() {
                if(selectedSlide == null) {
                    return 0;
                }
                return selectedSlide.getTargetsSize();
            }

            @Override
            public boolean isCellEditable(int row, int column) {
                if(selectedSlide == null) {
                    return false;
                }

                OSCPacketTarget target = selectedSlide.getTarget(row);
                switch(column) {
                    case 0: return true;
                    case 1: return true;
                    case 2: return (target.isEnabled())?false:true;
                    case 3: return (target.isEnabled())?false:true;
                    case 4: return (target.isEnabled())?false:true;
                    case 5: return (target.isEnabled())?false:true;
                    case 6: return false;
                }
                return false;
            }

            @Override
            public void setValueAt(Object aValue, int row, int column) {
                if(selectedSlide == null) {
                    return;
                }

                OSCPacketTarget target = selectedSlide.getTarget(row);

                switch(column) {
                    case 0:
                        target.setEnabled((boolean) aValue);
                        break;
                    case 1:
                        target.setWhen(OSCPacketTarget.WHEN.valueOf((String) aValue));
                        break;
                    case 2:
                        String ip = (String) aValue;
                        if(BSValidators.isValidInet4Address(ip)) {
                            target.setIp(ip);
                        }
                        break;
                    case 3:
                        int port = (int) aValue;
                        if(BSValidators.isValidPort(port)) {
                            target.setPort(port);
                        }
                        break;
                    case 4:
                        target.setMessage((String) aValue);
                        break;
                    case 5:
                        target.setValue((String) aValue);
                        break;
                    case 6:
                        break;
                }
            }
        };
        tableSlideTargets.setModel(tableSlideTargetsModel);
        tableSlideTargets.setCellSelectionEnabled(true);
        tableSlideTargets.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JComboBox<String> comboBox_table_slide_when = new JComboBox<>();
        Arrays.stream(OSCPacketTarget.WHEN.values()).forEach(e-> {
            comboBox_table_slide_when.addItem(e.toString());
        });
        tableSlideTargets.getColumnModel().getColumn(1).setCellEditor(new DefaultCellEditor(comboBox_table_slide_when));
        tableSlideTargets.getSelectionModel().addListSelectionListener((e) -> {
            if(e.getValueIsAdjusting()) {
                return;
            }

            if(selectedSlide == null) {
                return;
            }

            int row = tableSlideTargets.getSelectedRow();
            if(row == -1) {
                return;
            }

            selectedTarget = getSelectedSlide().getTarget(row);
            labelSlideTargetDynamicInput.setText("Input: " + selectedTarget.getValueInput());
            labelSlideTargetDynamicOutput.setText("Output: " + selectedTarget.getValueOutput());
            panelSlideDynamicValuesArea.setVisible(true);
            updateDynamicValuesPanel();
        });

        // buttons
        buttonAddSlide.addActionListener((e) -> {
            Point point = gridFindFreePoint();
            OSCPacketRelayerSlide slide = new OSCPacketRelayerSlide(relayer, point.x, point.y);
            synchronized (grid) {
                grid.add(slide);
            }
            panelSlides.add(slide.getPanel());
            panelSlides.repaint();

            addSlideHooks(slide);
            update();
        });
        buttonStart.addActionListener((e) -> {
            if(textPort.isEnabled()) {
                if(textPort.getText().trim().equals("")) {
//                    labelStatus.setText("Invalid port, unable to start");
                    return;
                }
                int port = 0;
                try {
                    port = Integer.parseInt(textPort.getText());
                } catch (NumberFormatException ex) {
//                    labelStatus.setText("Invalid port, unable to start");
                    return;
                }

                if(!BSValidators.isValidPort(port)) {
//                    labelStatus.setText("Invalid port, unable to start");
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
        buttonSlideTargetAdd.addActionListener((e) -> {
            if(selectedSlide == null) {
                return;
            }

            OSCPacketTarget target = new OSCPacketTarget();
            selectedSlide.addTarget(target);
            fireSlideTargetsUpdated(-1);
        });

        buttonSlideTargetRemove.addActionListener((e) -> {
            if(selectedSlide == null) {
                return;
            }

            int row = tableSlideTargets.getSelectedRow();
            if(row == -1) {
                return;
            }

            if (JOptionPane.showConfirmDialog(ControlWindow.this,
                    "Are you sure want to remove this row?",
                    "Remove Target",
                    JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                selectedSlide.removeTarget(row);
                tableSlideTargetsModel.fireTableDataChanged();
            }
        });

        // hooks
        textSlideTitle.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent e) {
                update();
            }
            public void removeUpdate(DocumentEvent e) {
                update();
            }
            public void insertUpdate(DocumentEvent e) {
                update();
            }

            private void update() {
                if(selectedSlide == null) {
                    return;
                }

                selectedSlide.setName(textSlideTitle.getText());
                selectedSlide.getPanel().update();
            }
        });
        buttonSlideGridUpdate.addActionListener((e) -> {
            if(selectedSlide == null) {
                return;
            }
            if(!(BSStringUtils.tryParseInt(textSlideGridX.getText()) || BSStringUtils.tryParseInt(textSlideGridY.getText()))) {
                JOptionPane.showMessageDialog(ControlWindow.this,
                        "Error: Grid x and y must be integers", "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            // we need to see if there is a slide existing at the new destination
            int x = Integer.parseInt(textSlideGridX.getText());
            int y = Integer.parseInt(textSlideGridY.getText());

            if(selectedSlide.getGridX() == x && selectedSlide.getGridY() == y) {
                // do nothin'
            }

            OSCPacketRelayerSlide target = getSlideAt(x, y);

            if(target != null) {
                // we are trying to put a slide ontop of another
                if (JOptionPane.showConfirmDialog(ControlWindow.this,
                        "You have specified a grid values of an existing slide.  Do you want to swap positions?",
                        "Swap Slide with " + target.getName(),
                        JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                    target.setGrid(selectedSlide.getGridX(), selectedSlide.getGridY());
                    selectedSlide.setGrid(x,y);
                    update();
                } else {
                    // do nothin'
                }
            } else {
                selectedSlide.setGrid(x,y);
                update();
            }

        });
        buttonSlideDelete.addActionListener((e) -> {
            if(selectedSlide == null) {
                return;
            }

            if (JOptionPane.showConfirmDialog(ControlWindow.this,
                    "Are you sure you want to remove this slide?",
                    "Delete Slide " + selectedSlide.getName(),
                    JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                grid.remove(selectedSlide);
                panelSlides.remove(selectedSlide.getPanel());
                selectedSlide = null;
                selectedTarget = null;
                panelSlideValues.setVisible(false);
                update();
            } else {
                // do nothin'
            }
        });
        buttonSlideDynamicValueAdd.addActionListener((e) -> {
            if(selectedTarget == null) {
                return;
            }
            OSCDynamicValueRuleWindow window = new OSCDynamicValueRuleWindow(this, selectedTarget);
            window.setVisible(true);
        });


        dynamicValueOnDelete = (index) -> {
            if (JOptionPane.showConfirmDialog(ControlWindow.this,
                    "Are you sure want to remove this dynamic rule?",
                    "Remove Dynamic Rule",
                    JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                selectedTarget.removeDynamicValue(index);
                updateDynamicValuesPanel();
            }
        };
    }

    private void updateDynamicValuesPanel() {
        if(selectedTarget == null) {
            return;
        }

        panelSlideDynamicValues.removeAll();
        selectedTarget.buildDynamicValuesPanel(panelSlideDynamicValues, dynamicValueOnDelete);
        panelSlideDynamicValues.getParent().revalidate();
        panelSlideDynamicValues.repaint();
    }


    private void update() {
        int max_x = 0;
        int max_y = 0;
        for(OSCPacketRelayerSlide slide : grid) {
            max_x = Math.max(max_x, slide.getPanel().getBounds().x + slide.getPanel().getBounds().width);
            max_y = Math.max(max_y, slide.getPanel().getBounds().y + slide.getPanel().getBounds().height);
        }
        panelSlides.setPreferredSize(new Dimension(max_x, max_y));
        panelSlides.repaint();
        panelSlides.getParent().revalidate();
    }

    private void addSlideHooks(OSCPacketRelayerSlide slide) {
        Consumer<Object> click = (o) -> {
            OSCPacketRelayerSlide previous_slide = selectedSlide;
            selectedSlide = slide;
            slide.getPanel().update();

            // load the settings
            textSlideTitle.setText(selectedSlide.getName());
            textSlideGridX.setText("" + selectedSlide.getGridX());
            textSlideGridY.setText("" + selectedSlide.getGridY());
            panelSlideValues.setVisible(true);
            panelSlideDynamicValuesArea.setVisible(false);
            tableSlideTargets.clearSelection();
            splitPaneSlideValues.setDividerLocation(200);
            tableSlideTargetsModel.fireTableDataChanged();


            // unselect the previous slide
            if(previous_slide != null) {
                previous_slide.getPanel().update();
            }
        };
        // add hooks
        slide.getPanel().addMouseListener(new MouseListener() {
            @Override public void mouseClicked(MouseEvent e) {
                click.accept(null);
            }
            @Override public void mousePressed(MouseEvent e) { }
            @Override public void mouseReleased(MouseEvent e) { }
            @Override public void mouseEntered(MouseEvent e) { }
            @Override public void mouseExited(MouseEvent e) { }
        });
        slide.getPanel().addMouseMotionListener(new MouseMotionListener() {
            @Override
            public void mouseDragged(MouseEvent e) {
                click.accept(null);
            }

            @Override
            public void mouseMoved(MouseEvent e) {

            }
        });
    }
    private Point gridFindFreePoint() {
        int max_rows = getGridMaxRows();
        int max_columns = getGridMaxColumns();

        if(max_rows == -1) {
            return new Point(0,0);
        } else {
            for(int y=0; y<max_rows; y++) {
                // we will never have a black column
                for(int x=0; x<max_columns; x++) {
                    if(getSlideAt(x,y) == null) {
                        return new Point(x,y);
                    }
                }
            }
        }

        if(max_columns<max_rows) {
            return new Point(max_columns,0);
        }
        return new Point(0, max_rows);
    }

    private OSCPacketRelayerSlide getSlideAt(int x, int y) {
        Optional<OSCPacketRelayerSlide> optional = grid.stream().filter(e -> e.getGridX() == x && e.getGridY() == y).findAny();
        if(optional.isPresent()) {
            return optional.get();
        }
        return null;
    }

    private int getGridMaxColumns() {
        int max = -1;
        for(OSCPacketRelayerSlide slide : grid) {
            max = Math.max(max, slide.getGridX());
        }
        return max + 1;
    }

    private int getGridMaxRows() {
        int max = -1;
        for(OSCPacketRelayerSlide slide : grid) {
            max = Math.max(max, slide.getGridY());
        }
        return max + 1;
    }

    @Override
    public void onFileOpen(File file) throws IOException, ClassNotFoundException {

        ObjectMapper mapper = new ObjectMapper();
        Map o = mapper.readValue(file, Map.class);

        // reset everything
        relayer.setSlide(null);
        selectedSlide = null;
        panelSlides.removeAll();

        List<OSCPacketRelayerSlide> slides = ((ArrayList<Map>) o.get("slides")).stream()
                .map(e -> new OSCPacketRelayerSlide(relayer, e))
                .collect(Collectors.toList());
        synchronized (grid) {
            grid.clear();
            grid.addAll(slides);
            grid.stream().forEach(e -> {
                e.setRelayer(relayer);
                panelSlides.add(e.getPanel());
                e.getPanel().update();
                addSlideHooks(e);
            });
        }
        update();

    }

    @Override
    public void onFileSave(File file) throws IOException {
        Map<String, Object> to_save = new HashMap<>();
        to_save.put("slides", grid);

        ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(file, to_save);
    }

    public void fireSourceUpdated(int index, OSCPacket source, OSCPacket packet) {
        if(index == -1) {
            tableSourcesModel.fireTableDataChanged();
        } else {
            tableSourcesModel.fireTableCellUpdated(index, 1);
        }
    }

    public void fireSlideTargetsUpdated(Integer index) {
        if(index == -1) {
            tableSlideTargetsModel.fireTableDataChanged();
        } else {
            tableSlideTargetsModel.fireTableCellUpdated(index, 6);
        }

        if(selectedTarget != null && selectedTarget == selectedSlide.getTarget(index)) {
            labelSlideTargetDynamicInput.setText("Input: " + selectedSlide.getTarget(index).getValueInput());
            labelSlideTargetDynamicOutput.setText("Output: " + selectedSlide.getTarget(index).getValueOutput());
        }
    }

    public void fireSlideTargetDynamicValue(OSCPacketTarget target) {
        if(selectedTarget == target) {
            updateDynamicValuesPanel();
        }
    }
    public void fireStatus(String text) {

    }

    // getters and setters
    public List<OSCPacketRelayerSlide> getSlides() {
        synchronized (grid) {
            return ImmutableList.copyOf(grid);
        }
    }
    public OSCPacketRelayerSlide getSelectedSlide() { return selectedSlide; }
    public OSCPacketTarget getSelectedTarget() { return selectedTarget; }

    public static void main(String[] args) {

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }
        catch (Exception e) {
            System.err.println("Unable to set look and feel, using default");
        }

        ControlWindow window = new ControlWindow();
        window.setVisible(true);
    }


}
