package ru.spbau.mit;

import java.io.*;
import java.net.Socket;
import java.util.Hashtable;
import java.util.Map;

public class Client {

    private final static int LIST_TYPE = 1;
    private final static int GET_TYPE = 2;

    private Socket socket;
    private DataInputStream dataInputStream;
    private DataOutputStream dataOutputStream;

    public void connect(String host, int port) throws IOException {
        socket = new Socket(host, port);
        dataInputStream = new DataInputStream(socket.getInputStream());
        dataOutputStream = new DataOutputStream(socket.getOutputStream());
    }

    public Map<String, Boolean> executeList(String path) throws IOException {
        sendRequest(LIST_TYPE, path);
        return listAnswerHandle();
    }

    private Map<String, Boolean> listAnswerHandle() throws IOException {
        int len = dataInputStream.readInt();
        if (len == 0) {
            return null;
        } else {
            Map<String, Boolean> dirs = new Hashtable<>();
            for (int i = 0; i < len - 1; i++) {
                String file = dataInputStream.readUTF();
                boolean isDir = dataInputStream.readBoolean();
                dirs.put(file, isDir);
            }
            return dirs;
        }
    }

    public File executeGet(String path, String dest) throws IOException {
        sendRequest(GET_TYPE, path);
        return getAnswerHandle(dest);
    }

    private File getAnswerHandle(String dest) throws IOException {
        long size = dataInputStream.readLong();
        if (size == 0) {
            return null;
        } else {
            File file = new File(dest);
            file.createNewFile();
            DataOutputStream dataOutputStream = new DataOutputStream(new FileOutputStream(dest));

            byte[] buffer = new byte[10000];
            for (int i = 0, len; i < size; i += len) {
                len = (int) (size - i > buffer.length ? buffer.length : size - i);
                dataInputStream.read(buffer, 0, len);
                dataOutputStream.write(buffer, 0, len);
            }

            return file;
        }
    }

    private void sendRequest(int type, String path) throws IOException {
        dataOutputStream.writeInt(type);
        dataOutputStream.writeUTF(path);
        dataOutputStream.flush();
    }

    public void disconnect() throws IOException {
        socket.close();
    }
}
