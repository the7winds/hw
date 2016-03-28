package torrent.client;

import torrent.tracker.FilesInfo.FileInfo;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;

/**
 * Created by the7winds on 27.03.16.
 */
public interface Client {

    void connect(String tracker) throws IOException;

    Collection<FileInfo> list() throws IOException;

    void download(int id, Path dest) throws IOException;

    int upload(File file) throws IOException;

    void disconnect() throws IOException;
}
