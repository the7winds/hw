package torrent.client;

import torrent.client.protocol.Get;
import torrent.client.protocol.Stat;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

;

/**
 * Created by the7winds on 27.03.16.
 */
public class ClientHandler implements Runnable {

    private final Socket socket;
    private final DataInputStream dataInputStream;
    private final DataOutputStream dataOutputStream;
    private final AvailablePartsProvider partsProvider;

    public ClientHandler(Socket socket, AvailablePartsProvider partsInfo) throws IOException {
        this.socket = socket;
        this.partsProvider = partsInfo;
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
                        Stat.Request statRequest = new Stat.Request();
                        statRequest.read(dataInputStream);
                        handleRequest(statRequest);
                        break;
                    case 2:
                        Get.Request getRequest = new Get.Request();
                        getRequest.read(dataInputStream);
                        handleRequest(getRequest);
                        break;
                    default:
                        throw new UnsupportedOperationException();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleRequest(Stat.Request request) throws IOException {
        new Stat.Answer(partsProvider.getAvailableById(request.getId())).write(dataOutputStream);
    }

    private void handleRequest(Get.Request request) throws IOException {
        new Get.Answer(partsProvider.getPart(request.getId(), request.getPart())).write(dataOutputStream);
    }
}
