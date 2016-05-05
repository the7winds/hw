package torrent.tracker;

import torrent.tracker.protocol.List;
import torrent.tracker.protocol.Sources;
import torrent.tracker.protocol.Update;
import torrent.tracker.protocol.Upload;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;



/**
 * Created by the7winds on 27.03.16.
 */

/** Handler which works with one client */

public class TorrentHandler implements Runnable {

    private final Socket socket;
    private final DataInputStream dataInputStream;
    private final DataOutputStream dataOutputStream;
    private final FilesRegister filesRegister;
    private final ClientsInfo clientsInfo;

    /** in minutes */
    private final long WAITING_UPDATE_TIMEOUT = 5;
    private Timer waitingUpdateTimeout;
    private Map<InetSocketAddress, RemoveFromTrackerTask> addressToTask = new HashMap<>();
    private class RemoveFromTrackerTask extends TimerTask {

        private byte[] ip;
        private short port;

        RemoveFromTrackerTask(byte[] ip, short port) {
            this.ip = ip;
            this.port = port;
        }

        @Override
        public void run() {
            try {
                Notifications.removeClient(ip, port);
                clientsInfo.removeClient(ip, port);
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
        }
    }


    TorrentHandler(Socket socket, FilesRegister filesRegister, ClientsInfo clientsInfo) throws IOException {
        this.socket = socket;
        this.filesRegister = filesRegister;
        this.clientsInfo = clientsInfo;
        waitingUpdateTimeout = new Timer();
        dataInputStream = new DataInputStream(socket.getInputStream());
        dataOutputStream = new DataOutputStream(socket.getOutputStream());
    }

    @Override
    public void run() {
        try {
            while (!socket.isClosed()) {
                byte requestTag = dataInputStream.readByte();
                switch (requestTag) {
                    case 1:
                        Notifications.listGet(socket);
                        List.Request listRequest = new List.Request();
                        listRequest.read(dataInputStream);
                        handleRequest();
                        Notifications.listDone(socket);
                        break;
                    case 2:
                        Notifications.uploadGet(socket);
                        Upload.Request uploadRequest = new Upload.Request();
                        uploadRequest.read(dataInputStream);
                        handleRequest(uploadRequest);
                        Notifications.uploadDone(socket);
                        break;
                    case 3:
                        Notifications.sourcesGet(socket);
                        Sources.Request sourcesRequest = new Sources.Request();
                        sourcesRequest.read(dataInputStream);
                        handleRequest(sourcesRequest);
                        Notifications.sourcesDone(socket);
                        break;
                    case 4:
                        Notifications.updateGet(socket);
                        Update.Request updateRequest = new Update.Request();
                        updateRequest.read(dataInputStream);
                        handleRequest(updateRequest);
                        Notifications.updateDone(socket);
                        break;
                    default:
                        throw new UnsupportedOperationException("Unsupported tag was received");
                }
            }
        } catch (IOException  e) {
            e.printStackTrace();
        }
    }

    private void handleRequest() throws IOException {
        new List.Answer(filesRegister.getRegister()).write(dataOutputStream);
    }

    private void handleRequest(Upload.Request request) throws IOException {
        new Upload.Answer(filesRegister.addFile(request.getName(), request.getSize())).write(dataOutputStream);
    }

    private void handleRequest(Sources.Request request) throws IOException {
        new Sources.Answer(clientsInfo.getSources(request.getId())).write(dataOutputStream);
    }

    private void handleRequest(Update.Request request) throws IOException {
        short port = request.getPort();
        byte[] ip = socket.getInetAddress().getAddress();

        InetSocketAddress address = new InetSocketAddress(Inet4Address.getByAddress(ip), port);

        if (addressToTask.get(address) != null) {
            addressToTask.get(address).cancel();
            waitingUpdateTimeout.purge();
        }

        RemoveFromTrackerTask removeFromTrackerTask = new RemoveFromTrackerTask(ip, port);
        addressToTask.put(address, removeFromTrackerTask);

        clientsInfo.addClient(ip, port, request.getIds());
        waitingUpdateTimeout.schedule(removeFromTrackerTask, TimeUnit.MINUTES.toMillis(WAITING_UPDATE_TIMEOUT));

        new Update.Answer(true).write(dataOutputStream);
    }
}
