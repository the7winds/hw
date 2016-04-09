package torrent.tracker;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;



/**
 * Created by the7winds on 26.03.16.
 */

/** Server */

public class TrackerImpl {

    public static final int BLOCK_SIZE = 10 * (1 << 20); // 10 MB
    public static final short TRACKER_PORT = 8081;

    private FilesInfo filesInfo;
    private ClientsInfo clientsInfo;

    private ServerSocket serverSocket;
    private ExecutorService executorService;
    private Runnable acceptor = () -> {
        try {
            while (true) {
                Socket socket = serverSocket.accept();
                TorrentHandler torrentHandler = new TorrentHandler(socket, filesInfo, clientsInfo);
                Notifications.accepted(socket);
                executorService.execute(torrentHandler);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    };

    public TrackerImpl() throws IOException {
        serverSocket = new ServerSocket(TRACKER_PORT);
        filesInfo = new FilesInfo();
        clientsInfo = new ClientsInfo();
    }

    /** starts tracker */

    public void start() {
        executorService = Executors.newCachedThreadPool();
        executorService.submit(acceptor);
    }

    /** stops tracker */

    public void stop() {
        try {
            serverSocket.close();
        } catch (SocketException ignored) {

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
