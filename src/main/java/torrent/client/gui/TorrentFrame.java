package torrent.client.gui;

import torrent.client.Client;
import torrent.client.clientNetworkImpl.DownloadStatus;
import torrent.tracker.FilesRegister;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Collection;

/**
 * Created by the7winds on 06.05.16.
 */
public class TorrentFrame extends JFrame {

    private ListPanel listPanel;
    private DownloadsPanel downloadsPanel;

    public TorrentFrame() {

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(getMaximumSize());

        JTabbedPane tabbedPane = new JTabbedPane();

        listPanel = new ListPanel();
        tabbedPane.add("available torrents", listPanel);

        downloadsPanel = new DownloadsPanel();
        tabbedPane.add("downloads", downloadsPanel);

        add(tabbedPane);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
                Client.getInstance().disconnect();
            }
        });
    }

    public void updateList(Collection<FilesRegister.FileInfo> list) {
        listPanel.update(list);
    }

    public void updateDownloadStatus(DownloadStatus downloadStatus) {
        downloadsPanel.update(downloadStatus);
    }
}
