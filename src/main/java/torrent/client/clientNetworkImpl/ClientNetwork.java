package torrent.client.clientNetworkImpl;

import torrent.tracker.FilesInfo;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

/**
 * Created by the7winds on 09.04.16.
 */
public interface ClientNetwork {

    void connect(String trackerAddress) throws IOException;

    Collection<FilesInfo.FileInfo> list() throws IOException;

    DownloadStatus download(int id, String pathname);

    int upload(File file) throws IOException;

    void disconnect() throws IOException;
}
