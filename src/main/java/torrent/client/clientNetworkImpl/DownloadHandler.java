package torrent.client.clientNetworkImpl;

import torrent.client.protocol.Get;
import torrent.client.protocol.Stat;
import torrent.tracker.ClientsInfo;
import torrent.tracker.FilesRegister;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static torrent.tracker.TrackerImpl.BLOCK_SIZE;

/**
 * Created by the7winds on 11.04.16.
 */
class DownloadHandler implements Runnable {

    private final ClientNetworkImpl client;
    private final FilesRegister.FileInfo fileInfo;
    private final File file;
    private final Set<Integer> wantedBlocks;
    private final DownloadStatus status;
    private Set<Integer> downloadedBlocks;

    DownloadHandler(ClientNetworkImpl client, DownloadStatus status, FilesRegister.FileInfo fileInfo, String pathname) {
        this.client = client;
        this.fileInfo = fileInfo;
        this.status = status;
        file = new File(pathname);
        wantedBlocks = getWantedBlocks();
        downloadedBlocks = new HashSet<>();
    }

    @Override
    public void run() {
        try {
            file.createNewFile();

            try (RandomAccessFile rFile = new RandomAccessFile(file, "rw")) {
                rFile.setLength(fileInfo.size);
            }

            while (!wantedBlocks.isEmpty()) {
                Collection<ClientsInfo.ClientInfo> sources = client.getTrackerHandler().execSources(fileInfo.id);
                for (ClientsInfo.ClientInfo clientInfo : sources) {
                    for (int block : wantedBlocks) {
                        client.getDownloads().execute(new DownloadTask(clientInfo, fileInfo.id, block));
                    }
                }

                wantedBlocks.removeAll(downloadedBlocks);

                synchronized (this) {
                    try {
                        Thread.currentThread().sleep(TimeUnit.SECONDS.toMillis(1));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            status.downloaded();
            Notification.downloaded();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Set<Integer> getWantedBlocks() {
        int amountOfBlocks = (int) (fileInfo.size / BLOCK_SIZE + (fileInfo.size % BLOCK_SIZE != 0 ? 1 : 0));
        return Stream.iterate(0, a -> a + 1)
                .limit(amountOfBlocks)
                .collect(Collectors.toSet());
    }

    class DownloadTask implements Runnable {

        private final int id;
        private final int block;
        private final Socket source;
        private final DataOutputStream dataOutputStream;
        private final DataInputStream dataInputStream;

        DownloadTask(ClientsInfo.ClientInfo clientInfo, int id, int block) throws IOException {
            this.id = id;
            this.block = block;
            source = new Socket(InetAddress.getByAddress(clientInfo.ip), clientInfo.port);
            dataOutputStream = new DataOutputStream(source.getOutputStream());
            dataInputStream = new DataInputStream(source.getInputStream());
        }

        @Override
        public void run() {
            try {
                Collection<Integer> blocks = execStat(id);
                if (!downloadedBlocks.contains(block) && blocks.contains(block)) {
                    execGetAndStore(id, block, file);
                    downloadedBlocks.add(block);
                    Notification.downloaded(id, block);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    source.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        private Collection<Integer> execStat(int id) throws IOException {
            new Stat.Request(id).write(dataOutputStream);
            Stat.Answer answer = new Stat.Answer();
            answer.read(dataInputStream);
            return answer.getAvailable();
        }

        private byte[] execGet(int id, int part) throws IOException {
            new Get.Request(id, part).write(dataOutputStream);
            Get.Answer answer = new Get.Answer();
            answer.read(dataInputStream);
            return answer.getContent();
        }

        private void execGetAndStore(int id, int part, File dest) throws IOException {
            byte[] content = execGet(id, part);
            int dataLen = ((dest.length() / BLOCK_SIZE) == part ? (int) (dest.length() % BLOCK_SIZE) : BLOCK_SIZE);
            try (RandomAccessFile rFile = new RandomAccessFile(dest, "rw")) {
                rFile.seek(part * BLOCK_SIZE);
                rFile.write(content, 0, dataLen);
                client.getAvailablePartsProvider().addPart(id, part, dest);
                client.getTrackerHandler().execUpdate();
            }
        }
    }
}
