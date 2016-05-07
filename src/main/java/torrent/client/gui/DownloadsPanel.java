package torrent.client.gui;

import torrent.client.clientNetworkImpl.DownloadStatus;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by the7winds on 07.05.16.
 */
public class DownloadsPanel extends JPanel {

    Map<Integer, Integer> idToRow = new HashMap<>();
    DefaultTableModel tableModel = new DefaultTableModel() {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    DownloadsPanel() {
        setLayout(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.weighty = 1;
        c.weightx = 1;
        c.fill = GridBagConstraints.BOTH;

        JTable jTable = new JTable();
        tableModel.setColumnIdentifiers(new String[] { "ID", "name", "progress" });
        jTable.setModel(tableModel);

        add(new JScrollPane(jTable), c);
    }

    public void update(DownloadStatus downloadStatus) {
        int id = downloadStatus.getId();
        if (!idToRow.containsKey(id)) {
            idToRow.put(id, tableModel.getRowCount());
            tableModel.addRow(new String[] { Integer.toString(downloadStatus.getId()), downloadStatus.getName(), null});
        }
        tableModel.setValueAt(String.format("%d/%d", downloadStatus.getCurrent(), downloadStatus.getBlocks())
                , idToRow.get(id)
                , 2);
    }
}
