package torrent.client;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import torrent.ArgsAndConsts;
import torrent.client.clientNetworkImpl.ClientNetworkImpl;
import torrent.client.clientNetworkImpl.DownloadStatus;
import torrent.tracker.FilesRegister.FileInfo;
import torrent.tracker.TrackerImpl;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.logging.Logger;

import static org.junit.Assert.assertTrue;

/**
 * Created by the7winds on 28.03.16.
 */
public class ClientTest {

    private static final Path TEST_RESOURCES = Paths.get("src/test/resources");
    private static final String host = "localhost";
    private final static short port0 = 8082;
    private final static short port1 = 8083;

    @Before
    public void before() throws IOException {
        Logger.getGlobal().setFilter(record -> true);
        cleanFiles();
    }

    @After
    public void cleanFiles() throws IOException {
        ArgsAndConsts.RESOURCES.resolve("files register").toFile().delete();
        ArgsAndConsts.RESOURCES.resolve("available parts register").toFile().delete();
        TEST_RESOURCES.resolve("download.txt").toFile().delete();
    }

    @Test
    public void listAndUpload() throws Exception {
        TrackerImpl trackerImpl = new TrackerImpl();
        ClientNetwork client = new ClientNetworkImpl(port0);

        trackerImpl.start();
        client.connect(host);

        File file = TEST_RESOURCES.resolve("2.txt").toFile();
        int id = client.upload(file);
        Collection<FileInfo> list = client.list();

        client.disconnect();
        trackerImpl.stop();

        assertTrue(list.contains(new FileInfo(id, "2.txt", file.length())));
    }

    @Test
    public void download() throws Exception {
        TrackerImpl trackerImpl = new TrackerImpl();
        ClientNetwork loader = new ClientNetworkImpl(port0);
        ClientNetwork downloader = new ClientNetworkImpl(port1);

        trackerImpl.start();
        loader.connect(host);
        downloader.connect(host);

        File file = TEST_RESOURCES.resolve("2.txt").toFile();
        Path dest = TEST_RESOURCES.resolve("download.txt");

        int id = loader.upload(file);
        DownloadStatus status = downloader.download(id, dest.toString());

        while (!status.isDownloaded());

        downloader.disconnect();
        loader.disconnect();
        trackerImpl.stop();

        assertTrue(com.google.common.io.Files.equal(file, dest.toFile()));
    }

    @Test
    public void downloadBig() throws Exception {
        TrackerImpl trackerImpl = new TrackerImpl();
        ClientNetwork loader = new ClientNetworkImpl(port0);
        ClientNetwork downloader = new ClientNetworkImpl(port1);

        trackerImpl.start();
        loader.connect(host);
        downloader.connect(host);

        Path dest = TEST_RESOURCES.resolve("download.txt");
        File file = TEST_RESOURCES.resolve("3.txt").toFile();

        int id = loader.upload(file);
        DownloadStatus downloadStatus = downloader.download(id, dest.toString());

        while (!downloadStatus.isDownloaded());

        downloader.disconnect();
        loader.disconnect();
        trackerImpl.stop();

        assertTrue(com.google.common.io.Files.equal(file, dest.toFile()));
    }
}