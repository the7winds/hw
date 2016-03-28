package torrent.tracker.protocol;

import torrent.Sendable;
import torrent.tracker.ClientsInfo.ClientInfo;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;

/**
 * Created by the7winds on 26.03.16.
 */
public final class Sources {

    private static final byte TAG = 3;

    private Sources() {
        throw new UnsupportedOperationException();
    }

    public static class Request implements Sendable {

        private int id;

        public Request() {
        }

        public Request(int id) {
            this.id = id;
        }

        @Override
        public void read(DataInputStream dataInputStream) throws IOException {
            id = dataInputStream.readInt();
        }

        @Override
        public void write(DataOutputStream dataOutputStream) throws IOException {
            dataOutputStream.writeByte(TAG);
            dataOutputStream.writeInt(id);
        }

        public int getId() {
            return id;
        }
    }

    public static class Answer implements Sendable {

        private Collection<ClientInfo> clientInfoList;

        public Answer() {
        }

        public Answer(Collection<ClientInfo> clientInfoList) {
            this.clientInfoList = clientInfoList;
        }

        @Override
        public void read(DataInputStream dataInputStream) throws IOException {
            clientInfoList = new LinkedList<>();
            int count = dataInputStream.readInt();
            for (int i = 0; i < count; ++i) {
                byte[] ip = new byte[4];
                dataInputStream.readFully(ip);
                short port = dataInputStream.readShort();
                clientInfoList.add(new ClientInfo(ip, port));
            }
        }

        @Override
        public void write(DataOutputStream dataOutputStream) throws IOException {
            dataOutputStream.writeByte(TAG);
            dataOutputStream.writeInt(clientInfoList.size());
            for (ClientInfo clientInfo : clientInfoList) {
                dataOutputStream.write(clientInfo.ip);
                dataOutputStream.writeShort(clientInfo.port);
            }
        }

        public Collection<ClientInfo> getClientInfoList() {
            return clientInfoList;
        }
    }
}
