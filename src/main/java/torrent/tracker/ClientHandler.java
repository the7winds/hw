package torrent.tracker;

import torrent.tracker.protocol.List;
import torrent.tracker.protocol.Sources;
import torrent.tracker.protocol.Update;
import torrent.tracker.protocol.Upload;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;


/**
 * Created by the7winds on 27.03.16.
 */

/** Handler which works with one client */

class ClientHandler implements Runnable {

    private final Socket socket;
    private final DataInputStream dataInputStream;
    private final DataOutputStream dataOutputStream;
    private final FilesRegister filesRegister;
    private final ClientsInfo clientsInfo;

    /** in minutes */
    private static final int WAITING_UPDATE_TIMEOUT = 5;
    private final Timer waitingUpdateTimeout = new Timer();
    private final Map<InetSocketAddress, TimerTask> addressToTask = new HashMap<>();


    ClientHandler(Socket socket, FilesRegister filesRegister, ClientsInfo clientsInfo) throws IOException {
        this.socket = socket;
        this.filesRegister = filesRegister;
        this.clientsInfo = clientsInfo;

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
                        Logger.getGlobal().finest(String.format("%s: LIST REQUEST GET\n", socket.toString()));
                        List.Request listRequest = new List.Request();
                        listRequest.read(dataInputStream);
                        handleRequest();
                        Logger.getGlobal().finest(String.format("%s: LIST REQUEST DONE\n", socket.toString()));
                        break;
                    case 2:
                        Logger.getGlobal().finest(String.format("%s: UPLOAD REQUEST\n", socket.toString()));
                        Upload.Request uploadRequest = new Upload.Request();
                        uploadRequest.read(dataInputStream);
                        handleRequest(uploadRequest);
                        Logger.getGlobal().finest(String.format("%s: UPLOAD REQUEST DONE\n", socket.toString()));
                        break;
                    case 3:
                        Logger.getGlobal().finest(String.format("%s: SOURCES REQUEST\n", socket.toString()));
                        Sources.Request sourcesRequest = new Sources.Request();
                        sourcesRequest.read(dataInputStream);
                        handleRequest(sourcesRequest);
                        Logger.getGlobal().finest(String.format("%s: SOURCES REQUEST DONE\n", socket.toString()));
                        break;
                    case 4:
                        Logger.getGlobal().finest(String.format("%s: UPDATE REQUEST\n", socket.toString()));
                        Update.Request updateRequest = new Update.Request();
                        updateRequest.read(dataInputStream);
                        handleRequest(updateRequest);
                        Logger.getGlobal().finest(String.format("%s: UPDATE REQUEST DONE\n", socket.toString()));
                        break;
                    default:
                        throw new UnsupportedOperationException("Unsupported tag was received");
                }
            }
        } catch (IOException e) {
            Logger.getGlobal().info(e.getMessage());
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
        final short port = request.getPort();
        final byte[] ip = socket.getInetAddress().getAddress();

        final InetSocketAddress address = new InetSocketAddress(Inet4Address.getByAddress(ip), port);

        if (addressToTask.get(address) != null) {
            addressToTask.get(address).cancel();
            waitingUpdateTimeout.purge();
        }

        TimerTask removeFromTrackerTask = new TimerTask() {
            @Override
            public void run() {
                Logger.getGlobal().finest("client removed from tracker list");
                clientsInfo.removeClient(address);
            }
        };

        addressToTask.put(address, removeFromTrackerTask);

        clientsInfo.addClient(address, request.getFilesIds());
        waitingUpdateTimeout.schedule(removeFromTrackerTask, TimeUnit.MINUTES.toMillis(WAITING_UPDATE_TIMEOUT));

        new Update.Answer(true).write(dataOutputStream);
    }
}
