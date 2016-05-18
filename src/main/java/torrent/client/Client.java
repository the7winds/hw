package torrent.client;

import torrent.ArgsAndConsts;
import torrent.client.clientNetworkImpl.DownloadStatus;
import torrent.client.gui.TorrentFrame;
import torrent.tracker.FilesRegister;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.Timer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by the7winds on 09.04.16.
 */
public class Client {

    private static final Client INSTANCE = new Client();

    private ClientNetwork clientNetwork;
    private TorrentFrame torrentFrame;
    private ExecutorService executor = Executors.newCachedThreadPool();

    private Client() {
    }

    public static Client getInstance() {
        return INSTANCE;
    }

    private Timer timer = new Timer(true);
    private TimerTask listDaemon = new TimerTask() {
        @Override
        public void run() {
            listHandle();
        }
    };


    public void main() throws IOException {
        torrentFrame = new TorrentFrame();
        SwingUtilities.invokeLater(() -> torrentFrame.setVisible(true));

        execute(() -> {
            try {
                clientNetwork = new torrent.client.clientNetworkImpl.ClientNetworkImpl(ArgsAndConsts.port);
                clientNetwork.connect(ArgsAndConsts.host);
                timer.schedule(listDaemon, 0, 1000);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private void listHandle() {
        try {
            Collection<FilesRegister.FileInfo> list = clientNetwork.list();
            torrentFrame.updateList(list);
        } catch (IOException e) {
            System.out.printf(e.getMessage());
        }
    }

    public void uploadHandle(File file) {
        try {
            clientNetwork.upload(file);
        } catch (IOException e) {
            System.out.printf(e.getMessage());
        }
    }

    public void downloadHandle(int id, String name, File destDir) {
        File file = destDir.toPath()
                .resolve(name)
                .toFile();
        final DownloadStatus downloadStatus = clientNetwork.download(id, file);
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                torrentFrame.updateDownloadStatus(downloadStatus);
                if (downloadStatus.isDownloaded()) {
                    cancel();
                }
            }
        }, 0, 10);
    }

    public void disconnect() {
        clientNetwork.disconnect();
        executor.shutdownNow();
    }

    public void execute(Runnable r) {
        executor.execute(r);
    }
}
