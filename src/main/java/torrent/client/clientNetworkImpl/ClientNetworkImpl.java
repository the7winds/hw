package torrent.client.clientNetworkImpl;

import java.io.*;
import java.util.Collection;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static torrent.tracker.FilesInfo.FileInfo;


public class ClientNetworkImpl implements ClientNetwork {

    private final TrackerHandler trackerHandler;
    private final ClientsHandler clientsHandler;
    private final AvailablePartsProvider availablePartsProvider;
    private Executor downloads;

    public ClientNetworkImpl(short port) throws IOException {
        availablePartsProvider = new AvailablePartsProvider();
        trackerHandler = new TrackerHandler(this);
        clientsHandler = new ClientsHandler(this, port);
    }

    @Override
    public void connect(String trackerAddress) throws IOException {
        trackerHandler.connect(trackerAddress);
        clientsHandler.start();
        downloads = Executors.newCachedThreadPool();
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
                downloads.execute(new DownloadHandler(this, status, fileInfo, pathname));
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
    }

    TrackerHandler getTrackerHandler() {
        return trackerHandler;
    }

    Executor getDownloads() {
        return downloads;
    }

    AvailablePartsProvider getAvailablePartsProvider() {
        return availablePartsProvider;
    }
}
