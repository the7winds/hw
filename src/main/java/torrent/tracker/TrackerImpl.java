package torrent.tracker;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import static torrent.ArgsAndConsts.TRACKER_PORT;


/**
 * Created by the7winds on 26.03.16.
 */

/** Server */

public class TrackerImpl {

    // stores information about uploaded files
    private FilesRegister filesRegister;
    // stores information about who contains some parts
    private ClientsInfo clientsInfo;

    // server fields
    private ServerSocket serverSocket;
    private List<Socket> acceptedSockets;

    private ExecutorService executorService;

    // accepts new connections
    private final Runnable acceptor = () -> {
        try {
            while (!serverSocket.isClosed()) {
                Socket socket = serverSocket.accept();
                acceptedSockets.add(socket);
                ClientHandler clientHandler = new ClientHandler(socket, filesRegister, clientsInfo);
                Logger.getGlobal().info(String.format("%s: ACCEPTED", socket.getInetAddress().toString()));
                executorService.execute(clientHandler);
            }
        } catch (IOException e) {
            Logger.getGlobal().info(e.getMessage());
        }
    };

    public TrackerImpl() throws IOException {
        serverSocket = new ServerSocket(TRACKER_PORT);
        filesRegister = new FilesRegister();
        clientsInfo = new ClientsInfo();
    }

    /** starts tracker */

    public void start() {
        Logger.getGlobal().info("START TRACKER");
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
