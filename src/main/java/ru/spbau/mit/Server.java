package ru.spbau.mit;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class Server {

    private ServerSocket serverSocket;
    private ExecutorService executorService;

    private final Runnable connectionsAcceptor = new Runnable() {
        @Override
        public void run() {
            try {
                while (!serverSocket.isClosed()) {
                    try {
                        Socket socket = serverSocket.accept();
                        executorService.execute(new SocketHandler(socket));
                    } catch (SocketTimeoutException ignored) {}
                }
            } catch (IOException ignored) {}
        }
    };

    private static class SocketHandler implements Runnable {
        private static final int TIMEOUT = 100;
        private final Socket socket;
        private final DataInputStream dataInputStream;
        private final DataOutputStream dataOutputStream;

        private SocketHandler(Socket socket) throws IOException {
            this.socket = socket;
            this.socket.setSoTimeout(TIMEOUT);
            dataInputStream = new DataInputStream(socket.getInputStream());
            dataOutputStream = new DataOutputStream(socket.getOutputStream());
        }

        @Override
        public void run() {
            try {
                while (!socket.isClosed()) {
                    try {
                        int type = dataInputStream.readInt();
                        String path = dataInputStream.readUTF();
                        if (type == 1) {
                            handleList(path);
                        } else if (type == 2) {
                            handleGet(path);
                        }
                    } catch (SocketTimeoutException ignored) {
                    }
                }
            } catch (IOException ignored) {
            }
        }

        private void handleList(String path) throws IOException {
            File directory = new File(path);

            if (directory.exists() && directory.isDirectory()) {
                File[] files = directory.listFiles();
                dataOutputStream.writeInt(files.length);
                for (File file : files) {
                    dataOutputStream.writeUTF(file.getName());
                    dataOutputStream.writeBoolean(file.isDirectory());
                }
            } else {
                dataOutputStream.write(0);
            }

            dataOutputStream.flush();
        }

        private void handleGet(String path) throws IOException {
            File file = new File(path);

            if (file.exists() && !file.isDirectory()) {
                dataOutputStream.writeLong(file.length());

                DataInputStream dataInputStream = new DataInputStream(new FileInputStream(path));
                byte[] buffer = new byte[100000];
                int len;

                while ((len = dataInputStream.read(buffer)) != -1) {
                    dataOutputStream.write(buffer, 0, len);
                }
            } else {
                dataOutputStream.writeLong(0);
            }

            dataOutputStream.flush();
        }
    }

    public void start(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        executorService = Executors.newCachedThreadPool();
        executorService.execute(connectionsAcceptor);
    }

    public void stop() throws IOException, InterruptedException {
        serverSocket.close();
    }
}
