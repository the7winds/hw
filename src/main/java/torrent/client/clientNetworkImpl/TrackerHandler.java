package torrent.client.clientNetworkImpl;

import torrent.tracker.ClientsInfo;
import torrent.tracker.FilesInfo;
import torrent.tracker.protocol.List;
import torrent.tracker.protocol.Sources;
import torrent.tracker.protocol.Update;
import torrent.tracker.protocol.Upload;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Collection;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import static torrent.ArgsAndConsts.port;
import static torrent.tracker.TrackerImpl.TRACKER_PORT;


class TrackerHandler {

    private Socket clientTrackerSocket;
    private DataInputStream clientTrackerDataInputStream;
    private DataOutputStream clientTrackerDataOutputStream;

    private final ClientNetworkImpl client;

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

    TrackerHandler(ClientNetworkImpl client) {
        this.client = client;
    }

    void connect(String address) throws IOException {
        clientTrackerSocket = new Socket(address, TRACKER_PORT);
        clientTrackerDataInputStream = new DataInputStream(clientTrackerSocket.getInputStream());
        clientTrackerDataOutputStream = new DataOutputStream(clientTrackerSocket.getOutputStream());
        updateTimer.schedule(updateTask, 0, TimeUnit.MINUTES.toMillis(UPDATE_PERIOD_TIME));
        Notification.connected();
    }

    synchronized Map<Integer, FilesInfo.FileInfo> execList() throws IOException {
        new List.Request().write(clientTrackerDataOutputStream);
        List.Answer answer = new List.Answer();
        answer.read(clientTrackerDataInputStream);
        return answer.getFileInfos();
    }

    synchronized Collection<ClientsInfo.ClientInfo> execSources(int id) throws IOException {
        new Sources.Request(id).write(clientTrackerDataOutputStream);
        Sources.Answer answer = new Sources.Answer();
        answer.read(clientTrackerDataInputStream);
        return answer.getClientInfoList();
    }

    synchronized int execUpload(String name, long size) throws IOException {
        new Upload.Request(name, size).write(clientTrackerDataOutputStream);
        Upload.Answer answer = new Upload.Answer();
        answer.read(clientTrackerDataInputStream);
        return answer.getId();
    }

    synchronized boolean execUpdate() throws IOException {
        new Update.Request(port, client.getAvailablePartsProvider().getAllIds()).write(clientTrackerDataOutputStream);
        Update.Answer answer = new Update.Answer();
        answer.read(clientTrackerDataInputStream);
        return answer.getStatus();
    }

    void disconnect() throws IOException {
        clientTrackerSocket.close();
    }
}
