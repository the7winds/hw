package torrent.client;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import torrent.GlobalConsts;
import torrent.tracker.FilesInfo.FileInfo;
import torrent.tracker.Tracker;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;

import static org.junit.Assert.assertEquals;
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
        cleanFiles();
        Path filesInfoNotEmpty = TEST_RESOURCES.resolve("inifiles").resolve("filesInfoNotEmpty.ini");
        Path filesInfo = GlobalConsts.RESOURCES.resolve("filesInfo.ini");
        filesInfo.toFile().delete();
        Files.copy(filesInfoNotEmpty, filesInfo);
    }

    @After
    public void cleanFiles() throws IOException {
        File filesInfo = GlobalConsts.RESOURCES.resolve("filesInfo.ini").toFile();
        filesInfo.delete();
        filesInfo.createNewFile();

        File availableParts = GlobalConsts.RESOURCES.resolve("availableParts.ini").toFile();
        availableParts.delete();
        availableParts.createNewFile();

        for (File file : TEST_RESOURCES.resolve("downloads").toFile().listFiles()) {
            file.delete();
        }
    }

    @Test
    public void list() throws Exception {
        Tracker tracker = new Tracker();
        Client client = new ClientImpl(port0);

        tracker.start();
        client.connect(host);

        Collection<FileInfo> files = client.list();

        client.disconnect();
        tracker.stop();

        assertTrue(files.contains(new FileInfo(0, "0.txt", 0)));
        assertTrue(files.contains(new FileInfo(1, "1.txt", 10)));
        assertTrue(files.contains(new FileInfo(2, "1.txt", 10)));
    }

    @Test
    public void upload() throws Exception {
        Tracker tracker = new Tracker();
        Client client = new ClientImpl(port0);

        tracker.start();
        client.connect(host);

        File file = TEST_RESOURCES.resolve("2.txt").toFile();
        int id = client.upload(file);
        Collection<FileInfo> list = client.list();

        client.disconnect();
        tracker.stop();

        assertTrue(list.contains(new FileInfo(id, "2.txt", file.length())));
    }

    @Test
    public void download() throws Exception {
        Tracker tracker = new Tracker();
        Client loader = new ClientImpl(port0);
        Client downloader = new ClientImpl(port1);

        tracker.start();
        loader.connect(host);
        downloader.connect(host);

        File file = TEST_RESOURCES.resolve("2.txt").toFile();

        int id = loader.upload(file);
        downloader.download(id, TEST_RESOURCES.resolve("downloads"));

        downloader.disconnect();
        loader.disconnect();
        tracker.stop();

        File downloaded = TEST_RESOURCES.resolve("downloads").resolve("2.txt").toFile();

        assertTrue(com.google.common.io.Files.equal(file, downloaded));
    }

    @Test
    public void downloadBig() throws Exception {
        Tracker tracker = new Tracker();
        Client loader = new ClientImpl(port0);
        Client downloader = new ClientImpl(port1);

        tracker.start();
        loader.connect(host);
        downloader.connect(host);

        Path dest = TEST_RESOURCES.resolve("downloads");
        File file = dest.resolve("3.txt").toFile();

        int id = loader.upload(file);
        downloader.download(id, dest);

        downloader.disconnect();
        loader.disconnect();
        tracker.stop();

        File fileClone = TEST_RESOURCES.resolve("downloads").resolve("3.txt").toFile();

        assertTrue(com.google.common.io.Files.equal(file, fileClone));
    }
}