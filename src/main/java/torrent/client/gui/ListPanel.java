package torrent.client.gui;

import torrent.client.Client;
import torrent.tracker.FilesRegister;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Collection;
import java.util.Collections;

/**
 * Created by the7winds on 06.05.16.
 */
class ListPanel extends JPanel {

    private Collection<FilesRegister.FileInfo> old = Collections.emptyList();
    private DefaultTableModel tableModel = new DefaultTableModel() {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    ListPanel() {
        setLayout(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.anchor = GridBagConstraints.PAGE_START;
        c.weighty = 1;
        c.weightx = 1;
        c.gridwidth = 2;
        c.gridx = 0;
        c.gridy = 0;

        final JTable jTable = new JTable();
        tableModel.setColumnIdentifiers(new String[] {"ID", "name", "size"});
        jTable.setModel(tableModel);
        jTable.setDragEnabled(false);
        jTable.getColumnModel().getColumn(0).setWidth(40);
        jTable.getColumnModel().getColumn(1);
        jTable.getColumnModel().getColumn(2).setWidth(100);
        jTable.enableInputMethods(false);

        add(new JScrollPane(jTable), c);

        JButton upload = new JButton();
        upload.setAction(new AbstractAction("upload") {
            @Override
            public void actionPerformed(ActionEvent e) {
                final JFileChooser fileChooser = new JFileChooser();
                fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

                if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                    Client.getInstance().execute(() -> Client.getInstance().uploadHandle(fileChooser.getSelectedFile()));
                }
            }
        });

        c.weightx = 0.5;
        c.weighty = 0.01;
        c.gridwidth = 1;
        c.anchor = GridBagConstraints.PAGE_END;
        c.gridy = 1;
        add(upload, c);

        JButton download = new JButton();
        download.setAction(new AbstractAction("download") {
            @Override
            public void actionPerformed(ActionEvent e) {
                final JFileChooser fileChooser = new JFileChooser();
                fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

                if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                    int id = Integer.valueOf((String) tableModel.getValueAt(jTable.getSelectedRow(), 0));
                    String name = (String) tableModel.getValueAt(jTable.getSelectedRow(), 1);
                    Client.getInstance().execute(() -> Client.getInstance().downloadHandle(id, name, fileChooser.getSelectedFile()));
                }
            }
        });

        c.gridx = 1;
        add(download, c);
    }

    void update(Collection<FilesRegister.FileInfo> files) {
        for (FilesRegister.FileInfo file : files) {
            if (!old.contains(file)) {
                addNode(file);
            }
        }
        old = files;
    }

    private void addNode(final FilesRegister.FileInfo fileInfo) {
        tableModel.addRow(new String[] {
                Integer.toString(fileInfo.id)
                , fileInfo.name
                , Long.toString(fileInfo.size) });
    }
}
