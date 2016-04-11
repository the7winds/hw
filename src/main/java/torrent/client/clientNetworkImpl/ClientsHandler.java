package torrent.client.clientNetworkImpl;

import torrent.client.protocol.Get;
import torrent.client.protocol.Stat;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collections;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


class ClientsHandler {

    private final short port;
    private ServerSocket serverSocket;
    private final ExecutorService executorService = Executors.newCachedThreadPool();
    private ClientNetworkImpl client;

    private java.util.List<Socket> acceptedSockets;

    private final Runnable acceptor = () -> {
        try {
            while (!serverSocket.isClosed()) {
                Socket socket = serverSocket.accept();
                acceptedSockets.add(socket);
                ClientHandler clientHandler = new ClientHandler(client, socket);
                executorService.execute(clientHandler);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    };

    ClientsHandler(ClientNetworkImpl client, short port) {
        this.client = client;
        this.port = port;
    }

    void start() throws IOException {
        acceptedSockets = Collections.synchronizedList(new LinkedList<>());
        serverSocket = new ServerSocket(port);
        executorService.execute(acceptor);
    }

    void stop() throws IOException {
        for (Socket socket : acceptedSockets) {
            socket.close();
        }
        serverSocket.close();
    }

    private static class ClientHandler implements Runnable {

        private final Socket socket;
        private final DataInputStream dataInputStream;
        private final DataOutputStream dataOutputStream;
        private final ClientNetworkImpl client;

        ClientHandler(ClientNetworkImpl client, Socket socket) throws IOException {
            this.socket = socket;
            this.client = client;
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
            new Stat.Answer(client.getAvailablePartsProvider()
                    .getAvailableById(request.getId())).write(dataOutputStream);
        }

        private void handleRequest(Get.Request request) throws IOException {
            new Get.Answer(client.getAvailablePartsProvider()
                    .getPart(request.getId(), request.getPart())).write(dataOutputStream);
        }
    }
}
