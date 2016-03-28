package torrent.tracker;

import torrent.GlobalConsts;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

;

/**
 * Created by the7winds on 26.03.16.
 */

/** Server */

public class Tracker {

    private FilesInfo filesInfo;
    private ClientsInfo clientsInfo;

    private ServerSocket serverSocket;
    private ExecutorService executorService;
    private Runnable acceptor = () -> {
        try {
            while (true) {
                Socket socket = serverSocket.accept();
                TorrentHandler torrentHandler = new TorrentHandler(socket, filesInfo, clientsInfo);
                executorService.execute(torrentHandler);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    };

    public Tracker() throws IOException {
        serverSocket = new ServerSocket(GlobalConsts.TRACKER_PORT);
        filesInfo = new FilesInfo();
        clientsInfo = new ClientsInfo();
    }

    /** starts tracker */

    public void start() {
        executorService = Executors.newCachedThreadPool();
        executorService.submit(acceptor);
    }

    /** stops tracker */

    public void stop() throws IOException {
        serverSocket.close();
    }
}
