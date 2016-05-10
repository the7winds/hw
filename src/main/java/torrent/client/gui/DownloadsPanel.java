package torrent.client.gui;

import javafx.util.Pair;
import torrent.client.clientNetworkImpl.DownloadStatus;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by the7winds on 07.05.16.
 */
class DownloadsPanel extends JPanel {

    private Map<Integer, Integer> idToRow = new HashMap<>();
    private DefaultTableModel tableModel = new DefaultTableModel() {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable jTable = new JTable();

    DownloadsPanel() {
        setLayout(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.weighty = 1;
        c.weightx = 1;
        c.fill = GridBagConstraints.BOTH;

        tableModel.setColumnIdentifiers(new String[] { "ID", "name", "progress" });
        jTable.setModel(tableModel);

        add(new JScrollPane(jTable), c);
    }

    void update(DownloadStatus downloadStatus) {
        int id = downloadStatus.getId();
        if (!idToRow.containsKey(id)) {
            idToRow.put(id, tableModel.getRowCount());
            tableModel.addRow(new String[] { Integer.toString(downloadStatus.getId()), downloadStatus.getName(), null});
            tableModel.setValueAt(new Pair<>(downloadStatus.getCurrent(), downloadStatus.getBlocks())
                    , idToRow.get(id)
                    , 2);
            jTable.getColumn("progress").setCellRenderer(new JProgressBarRender());
        }
        tableModel.setValueAt(new Pair<>(downloadStatus.getCurrent(), downloadStatus.getBlocks())
                , idToRow.get(id)
                , 2);
    }

    static class JProgressBarRender extends JProgressBar implements TableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            if (column != 2) {
                DefaultTableCellRenderer defaultTableCellRenderer = new DefaultTableCellRenderer();
                return defaultTableCellRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            } else {
                Pair<Integer, Integer> pair = (Pair<Integer, Integer>) value;
                setValue(pair.getKey());
                setMaximum(pair.getValue());
                return this;
            }
        }
    }
}
