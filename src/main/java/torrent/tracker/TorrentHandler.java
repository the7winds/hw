package torrent.tracker;

import torrent.tracker.protocol.List;
import torrent.tracker.protocol.Sources;
import torrent.tracker.protocol.Update;
import torrent.tracker.protocol.Upload;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Hashtable;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

;

/**
 * Created by the7winds on 27.03.16.
 */

/** Handler which works with one client */

public class TorrentHandler implements Runnable {

    private final static String TAG = "TORRENT_TMP_FILE";
    private final Socket socket;
    private final Path tmp;
    private final DataInputStream dataInputStream;
    private final DataOutputStream dataOutputStream;
    private final FilesInfo filesInfo;
    private final ClientsInfo clientsInfo;

    /** in minutes */
    private final long WAITING_UPDATE_TIMEOUT = 5;
    private Timer waitingUpdateTimeout;
    private Map<byte[], Map<Short, RemoveFromTrackerTask>> adresesToTask;
    private class RemoveFromTrackerTask extends TimerTask {

        private byte[] ip;
        private short port;

        public RemoveFromTrackerTask(byte[] ip, short port) {
            this.ip = ip;
            this.port = port;
        }

        @Override
        public void run() {
            clientsInfo.removeClient(ip, port);
        }
    }


    public TorrentHandler(Socket socket, FilesInfo filesInfo, ClientsInfo clientsInfo) throws IOException {
        this.socket = socket;
        this.filesInfo = filesInfo;
        this.clientsInfo = clientsInfo;
        waitingUpdateTimeout = new Timer();
        tmp = Files.createTempFile(TAG, null);
        dataInputStream = new DataInputStream(socket.getInputStream());
        dataOutputStream = new DataOutputStream(socket.getOutputStream());
    }

    @Override
    public void run() {
        try {
            while (true) {
                byte requestTag = dataInputStream.readByte();
                switch (requestTag) {
                    case 1:
                        List.Request listRequest = new List.Request();
                        listRequest.read(dataInputStream);
                        handleRequest(listRequest);
                        break;
                    case 2:
                        Upload.Request uploadRequest = new Upload.Request();
                        uploadRequest.read(dataInputStream);
                        handleRequest(uploadRequest);
                        break;
                    case 3:
                        Sources.Request sourcesRequest = new Sources.Request();
                        sourcesRequest.read(dataInputStream);
                        handleRequest(sourcesRequest);
                        break;
                    case 4:
                        Update.Request updateRequest = new Update.Request();
                        updateRequest.read(dataInputStream);
                        handleRequest(updateRequest);
                        break;
                    default:
                        throw new UnsupportedOperationException("Unsupported tag was received");
                }
            }
        } catch (IOException  e) {
            e.printStackTrace();
        }
    }

    private void handleRequest(List.Request request) throws IOException {
        new List.Answer(filesInfo.getList()).write(dataOutputStream);
    }

    private void handleRequest(Upload.Request request) throws IOException {
        new Upload.Answer(filesInfo.addFile(request.getName(), request.getSize())).write(dataOutputStream);
    }

    private void handleRequest(Sources.Request request) throws IOException {
        new Sources.Answer(clientsInfo.getSources(request.getId())).write(dataOutputStream);
    }

    private void handleRequest(Update.Request request) {
        byte[] ip = request.getIp();
        short port = request.getPort();

        if (adresesToTask.get(ip) != null) {
            if (adresesToTask.get(ip).get(port) != null) {
                adresesToTask.get(ip).get(port).cancel();
                waitingUpdateTimeout.purge();
            }
        }

        RemoveFromTrackerTask removeFromTrackerTask = new RemoveFromTrackerTask(ip, port);
        adresesToTask.putIfAbsent(ip, new Hashtable<>());
        adresesToTask.get(ip).put(port, removeFromTrackerTask);

        clientsInfo.addClient(ip, port, request.getIds());
        waitingUpdateTimeout.schedule(removeFromTrackerTask, TimeUnit.MINUTES.toMillis(WAITING_UPDATE_TIMEOUT));
    }
}
