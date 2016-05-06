package torrent.client.clientNetworkImpl;

import torrent.client.ClientNetwork;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static torrent.tracker.FilesRegister.FileInfo;


public class ClientNetworkImpl implements ClientNetwork {

    private final TrackerHandler trackerHandler;
    private final ClientsHandler clientsHandler;
    private final AvailablePartsProvider availablePartsProvider;
    private Executor downloadsExecutor;

    public ClientNetworkImpl(short port) throws IOException {
        availablePartsProvider = new AvailablePartsProvider();
        trackerHandler = new TrackerHandler(this);
        clientsHandler = new ClientsHandler(this, port);
    }

    @Override
    public void connect(String trackerAddress) throws IOException {
        trackerHandler.connect(trackerAddress);
        clientsHandler.start();
        downloadsExecutor = Executors.newCachedThreadPool();
    }

    @Override
    public Collection<FileInfo> list() throws IOException {
        return trackerHandler.execList().values();
    }

    @Override
    public DownloadStatus download(int id, String pathname) {
        try {
            FileInfo fileInfo = trackerHandler.execList().get(id);
            if (fileInfo != null) {
                DownloadStatus status = new DownloadStatus();
                downloadsExecutor.execute(new DownloadHandler(this, status, fileInfo, pathname));
                return status;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public int upload(File file) throws IOException {
        int id = trackerHandler.execUpload(file.getName(), file.length());
        availablePartsProvider.addFile(id, file);
        trackerHandler.execUpdate();
        Notification.added(id);
        return id;
    }

    @Override
    public void disconnect() throws IOException {
        trackerHandler.disconnect();
        clientsHandler.stop();
        availablePartsProvider.store();
    }

    TrackerHandler getTrackerHandler() {
        return trackerHandler;
    }

    Executor getDownloadsExecutor() {
        return downloadsExecutor;
    }

    AvailablePartsProvider getAvailablePartsProvider() {
        return availablePartsProvider;
    }
}
