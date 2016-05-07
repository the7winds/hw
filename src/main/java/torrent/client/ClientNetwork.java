package torrent.client;

import torrent.client.clientNetworkImpl.DownloadStatus;
import torrent.tracker.FilesRegister;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

/**
 * Created by the7winds on 09.04.16.
 */
public interface ClientNetwork {

    void connect(String trackerAddress) throws IOException;

    Collection<FilesRegister.FileInfo> list() throws IOException;

    DownloadStatus download(int id, File file);

    int upload(File file) throws IOException;

    void disconnect();
}
