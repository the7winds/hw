package torrent.tracker.protocol;

import torrent.Sendable;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;

/**
 * Created by the7winds on 27.03.16.
 */
public final class Update {

    private final static byte TAG = 4;

    private Update() {
        throw new UnsupportedOperationException();
    }

    public static class Request implements Sendable {

        private short port;
        private Collection<Integer> filesIds;

        public Request() {
        }

        public Request(short port, Collection<Integer> filesIds) {
            this.port = port;
            this.filesIds = filesIds;
        }

        @Override
        public void read(DataInputStream dataInputStream) throws IOException {
            filesIds = new LinkedList<>();
            port = dataInputStream.readShort();
            int count = dataInputStream.readInt();
            for (int i = 0; i < count; ++i) {
                filesIds.add(dataInputStream.readInt());
            }
        }

        @Override
        public void write(DataOutputStream dataOutputStream) throws IOException {
            dataOutputStream.writeByte(TAG);
            dataOutputStream.writeShort(port);
            dataOutputStream.writeInt(filesIds.size());
            for (int id : filesIds) {
                dataOutputStream.writeInt(id);
            }
            dataOutputStream.flush();
        }

        public short getPort() {
            return port;
        }

        public Collection<Integer> getFilesIds() {
            return filesIds;
        }
    }

    public static class Answer implements Sendable {

        private boolean status;

        public Answer() {
        }

        public Answer(boolean status) {
            this.status = status;
        }

        @Override
        public void read(DataInputStream dataInputStream) throws IOException {
            status = dataInputStream.readBoolean();
        }

        @Override
        public void write(DataOutputStream dataOutputStream) throws IOException {
            dataOutputStream.writeBoolean(status);
            dataOutputStream.flush();
        }

        public boolean getStatus() {
            return status;
        }
    }
}
