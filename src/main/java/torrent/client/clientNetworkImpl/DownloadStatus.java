package torrent.client.clientNetworkImpl;

import torrent.tracker.FilesRegister;

import java.util.concurrent.atomic.AtomicBoolean;

import static torrent.ArgsAndConsts.BLOCK_SIZE;

/**
 * Created by the7winds on 11.04.16.
 */
public class DownloadStatus {

    private AtomicBoolean downloaded = new AtomicBoolean(false);
    private final FilesRegister.FileInfo fileInfo;
    private final int blocks;
    private volatile int current;

    DownloadStatus(FilesRegister.FileInfo fileInfo) {
        this.fileInfo = fileInfo;
        this.blocks = (int) (fileInfo.size / BLOCK_SIZE + (fileInfo.size % BLOCK_SIZE == 0 ? 0 : 1));
        this.current = 0;
    }

    void update(int current) {
        this.current = current;
    }

    void downloaded() {
        downloaded.set(true);
    }

    public boolean isDownloaded() {
        return downloaded.get();
    }

    public int getId() {
        return fileInfo.id;
    }

    public int getCurrent() {
        return current;
    }

    public String getName() {
        return fileInfo.name;
    }

    public int getBlocks() {
        return blocks;
    }
}
