package torrent.client.clientNetworkImpl;

import torrent.client.protocol.Get;
import torrent.client.protocol.Stat;
import torrent.tracker.FilesRegister;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static torrent.ArgsAndConsts.BLOCK_SIZE;

/**
 * Created by the7winds on 11.04.16.
 */
class DownloadHandler implements Runnable {

    private final ClientNetworkImpl client;
    private final FilesRegister.FileInfo fileInfo;
    private final File file;
    private final Set<Integer> wantedBlocks;
    private final DownloadStatus status;

    DownloadHandler(ClientNetworkImpl client, DownloadStatus status, FilesRegister.FileInfo fileInfo, File file) {
        this.client = client;
        this.fileInfo = fileInfo;
        this.status = status;
        this.file = file;
        wantedBlocks = Collections.synchronizedSet(getWantedBlocks());
    }

    @Override
    public void run() {
        try {
            file.createNewFile();

            try (RandomAccessFile rFile = new RandomAccessFile(file, "rw")) {
                rFile.setLength(fileInfo.size);
            }

            int blocks = wantedBlocks.size();
            while (!wantedBlocks.isEmpty()) {
                Collection<InetSocketAddress> sources = client.getTrackerHandler().execSources(fileInfo.id);
                for (InetSocketAddress clientInfo : sources) {
                    client.getDownloadsExecutor().execute(new DownloadTask(clientInfo, fileInfo.id, wantedBlocks));
                }

                synchronized (this) {
                    try {
                        Thread.currentThread().sleep(TimeUnit.SECONDS.toMillis(1));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                status.update(blocks - wantedBlocks.size());
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

    private class DownloadTask implements Runnable {

        private final int id;
        private final Set<Integer> wantedBlocks;
        private final InetSocketAddress sourceAddress;

        private DataOutputStream dataOutputStream;
        private DataInputStream dataInputStream;

        DownloadTask(InetSocketAddress sourceAddress, int id, Set<Integer> wantedBlocks) throws IOException {
            this.id = id;
            this.wantedBlocks = wantedBlocks;
            this.sourceAddress = sourceAddress;
        }

        @Override
        public void run() {
            Socket source = null;
            try {
                source = new Socket(sourceAddress.getHostName(), sourceAddress.getPort());
                dataInputStream = new DataInputStream(source.getInputStream());
                dataOutputStream = new DataOutputStream(source.getOutputStream());
                Collection<Integer> availableBlocks = execStat(id);
                synchronized (wantedBlocks) {
                    Iterator<Integer> iterator = wantedBlocks.iterator();
                    while (iterator.hasNext()) {
                        Integer part = iterator.next();
                        if (availableBlocks.contains(part)) {
                            execGetAndStore(id, part, file);
                            Notification.downloaded(id, part);
                            iterator.remove();
                        }
                    }
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
