package torrent.client.clientNetworkImpl;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by the7winds on 11.04.16.
 */
public class DownloadStatus {

    private AtomicBoolean downloaded = new AtomicBoolean(false);

    void downloaded() {
        downloaded.set(true);
    }

    public boolean isDownloaded() {
        return downloaded.get();
    }
}
