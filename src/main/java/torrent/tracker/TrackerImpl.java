package torrent.tracker;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


/**
 * Created by the7winds on 26.03.16.
 */

/** Server */

public class TrackerImpl {

    public static final int BLOCK_SIZE = 10 * (1 << 20); // 10 MB
    public static final short TRACKER_PORT = 8081;

    private FilesRegister filesRegister;
    private ClientsInfo clientsInfo;

    private ServerSocket serverSocket;
    private List<Socket> acceptedSockets;

    private ExecutorService executorService;

    private final Runnable acceptor = () -> {
        try {
            while (!serverSocket.isClosed()) {
                Socket socket = serverSocket.accept();
                acceptedSockets.add(socket);
                TorrentHandler torrentHandler = new TorrentHandler(socket, filesRegister, clientsInfo);
                Notifications.accepted(socket);
                executorService.execute(torrentHandler);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    };

    public TrackerImpl() throws IOException {
        serverSocket = new ServerSocket(TRACKER_PORT);
        filesRegister = new FilesRegister();
        clientsInfo = new ClientsInfo();
    }

    /** starts tracker */

    public void start() {
        acceptedSockets = new LinkedList<>();
        executorService = Executors.newCachedThreadPool();
        executorService.submit(acceptor);
    }

    /** stops tracker */

    public void stop() throws IOException, InterruptedException {
        for (Socket socket : acceptedSockets) {
            socket.close();
        }
        serverSocket.close();
        executorService.shutdown();
        executorService.awaitTermination(1, TimeUnit.MINUTES);
        filesRegister.store();
    }
}
