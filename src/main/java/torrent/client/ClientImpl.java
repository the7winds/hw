package torrent.client;

import torrent.GlobalConsts;
import torrent.client.protocol.Get;
import torrent.client.protocol.Stat;
import torrent.tracker.ClientsInfo.ClientInfo;
import torrent.tracker.protocol.List;
import torrent.tracker.protocol.Sources;
import torrent.tracker.protocol.Update;
import torrent.tracker.protocol.Upload;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.Inet4Address;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static torrent.tracker.FilesInfo.FileInfo;

/**
 * Created by the7winds on 26.03.16.
 */
public class ClientImpl implements Client{

    // connects another clients
    private final short port;
    private ServerSocket serverSocket;
    private final ExecutorService executorService = Executors.newCachedThreadPool();;

    // connects to the tracker
    private Socket clientTrackerSocket;
    private DataInputStream clientTrackerDataInputStream;
    private DataOutputStream clientTrackerDataOutputStream;

    private AvailablePartsProvider availablePartsProvider;

    // connects to another client
    private Socket clientClientSocket;
    private DataInputStream clientClientDataInputStream;
    private DataOutputStream clientClientDataOutputStream;

    private final Runnable acceptor = () -> {
        try {
            while (true) {
                Socket socket = serverSocket.accept();
                ClientHandler clientHandler = new ClientHandler(socket, availablePartsProvider);
                executorService.execute(clientHandler);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    };

    private final static int UPDATE_PERIOD_TIME = 4;
    private final Timer updateTimer = new Timer();
    private final TimerTask updateTask = new TimerTask() {
        @Override
        public void run() {
            try {
                execUpdate();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    };

    public ClientImpl(short port) throws IOException {
        this.port = port;
        availablePartsProvider = new AvailablePartsProvider();
    }

    private void start() throws IOException {
        serverSocket = new ServerSocket(port);
    }

    private void connectToTracker(String address) throws IOException {
        clientTrackerSocket = new Socket(address, GlobalConsts.TRACKER_PORT);
        clientTrackerDataInputStream = new DataInputStream(clientTrackerSocket.getInputStream());
        clientTrackerDataOutputStream = new DataOutputStream(clientTrackerSocket.getOutputStream());
    }

    private Map<Integer, FileInfo> execList() throws IOException {
        synchronized (clientTrackerSocket) {
            new List.Request().write(clientTrackerDataOutputStream);
            List.Answer answer = new List.Answer();
            answer.read(clientTrackerDataInputStream);
            return answer.getFileInfos();
        }
    }

    private Collection<ClientInfo> execSources(int id) throws IOException {
        synchronized (clientTrackerSocket) {
            new Sources.Request(id).write(clientTrackerDataOutputStream);
            Sources.Answer answer = new Sources.Answer();
            answer.read(clientTrackerDataInputStream);
            return answer.getClientInfoList();
        }
    }

    private int execUpload(String name, long size) throws IOException {
        synchronized (clientTrackerSocket) {
            new Upload.Request(name, size).write(clientTrackerDataOutputStream);
            Upload.Answer answer = new Upload.Answer();
            answer.read(clientTrackerDataInputStream);
            return answer.getId();
        }
    }

    private boolean execUpdate() throws IOException {
        synchronized (clientTrackerSocket) {
            new Update.Request(port, availablePartsProvider.getAllIds()).write(clientTrackerDataOutputStream);
            Update.Answer answer = new Update.Answer();
            answer.read(clientTrackerDataInputStream);
            return answer.getStatus();
        }
    }

    private void connectToClient(byte[] ip, short port) throws IOException {
        clientClientSocket = new Socket(Inet4Address.getByAddress(ip), port);
        clientClientDataInputStream = new DataInputStream(clientClientSocket.getInputStream());
        clientClientDataOutputStream = new DataOutputStream(clientClientSocket.getOutputStream());
    }

    private Collection<Integer> execStat(int id) throws IOException {
        synchronized (clientClientSocket) {
            new Stat.Request(id).write(clientClientDataOutputStream);
            Stat.Answer answer = new Stat.Answer();
            answer.read(clientClientDataInputStream);
            return answer.getAvailable();
        }
    }

    private byte[] execGet(int id, int part) throws IOException {
        synchronized (clientClientSocket) {
            new Get.Request(id, part).write(clientClientDataOutputStream);
            Get.Answer answer = new Get.Answer();
            answer.read(clientClientDataInputStream);
            return answer.getContent();
        }
    }

    private void execGetAndStore(int id, int part, Path dest) throws IOException {
        synchronized (clientClientSocket) {
            byte[] content = execGet(id, part);
            DataOutputStream file = new DataOutputStream(Files.newOutputStream(dest));
            file.write(content, 0, part * GlobalConsts.BLOCK_SIZE);
            availablePartsProvider.addPart(id, part, dest);
        }
    }

    private void disconnectFromClient() throws IOException {
        clientClientSocket.close();
    }

    private void disconnectFromTracker() throws IOException {
        clientTrackerSocket.close();
    }

    private void stop() throws IOException {
        serverSocket.close();
    }

    @Override
    public void connect(String trackerAddr) throws IOException {
        // connects to tracker
        connectToTracker(trackerAddr);
        // starts update task
        updateTimer.schedule(updateTask, 0, TimeUnit.MINUTES.toMillis(UPDATE_PERIOD_TIME));
        // start sid task
        start();
    }

    @Override
    public Collection<FileInfo> list() throws IOException {
        return execList().values();
    }

    @Override
    public void download(int id, Path dest) {
        try {
            FileInfo fileInfo = execList().get(id);
            if (fileInfo != null) {
                dest = dest.resolve(fileInfo.name);
                RandomAccessFile file = new RandomAccessFile(dest.toFile(), "rw");
                file.setLength(fileInfo.size);

                int amountOfBlocks = (int) (fileInfo.size / GlobalConsts.BLOCK_SIZE + (fileInfo.size % GlobalConsts.BLOCK_SIZE != 0 ? 1 : 0));
                Set<Integer> wantedBlocks = Stream.iterate(0, a -> a + 1)
                        .limit(amountOfBlocks)
                        .collect(Collectors.toSet());

                while (!wantedBlocks.isEmpty()) {
                    Collection<ClientInfo> sources = execSources(id);

                    for (ClientInfo clientInfo : sources) {
                        connectToClient(clientInfo.ip, clientInfo.port);
                        Integer part = -1;
                        for (Integer n : execStat(id)) {
                            if (wantedBlocks.contains(n)) {
                                part = n;
                                break;
                            }
                        }
                        if (part >= 0) {
                            execGetAndStore(id, part, dest);
                        }
                        disconnectFromClient();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int upload(Path file) throws IOException {
        int id = execUpload(file.getFileName().toString(), file.toFile().length());
        availablePartsProvider.addFile(id, file);
        return id;
    }

    @Override
    public void disconnect() throws IOException {
        stop();
        disconnectFromTracker();
    }
}
