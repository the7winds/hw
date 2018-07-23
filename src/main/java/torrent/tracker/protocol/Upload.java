package torrent.tracker.protocol;

import torrent.Sendable;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Created by the7winds on 26.03.16.
 */
public final class Upload {

    private static final byte TAG = 2;

    private Upload() {
        throw new UnsupportedOperationException();
    }

    public static class Request implements Sendable {

        private String name;
        private long size;

        public Request() {
        }

        public Request(String name, long size) {
            this.name = name;
            this.size = size;
        }

        /** Implies that tag has been read. */

        @Override
        public void read(DataInputStream dataInputStream) throws IOException {
            name = dataInputStream.readUTF();
            size = dataInputStream.readLong();
        }

        @Override
        public void write(DataOutputStream dataOutputStream) throws IOException {
            dataOutputStream.writeByte(TAG);
            dataOutputStream.writeUTF(name);
            dataOutputStream.writeLong(size);
            dataOutputStream.flush();
        }

        public String getName() {
            return name;
        }

        public long getSize() {
            return size;
        }
    }

    public static class Answer implements Sendable {

        private int id;

        public Answer() {
        }

        public Answer(int id) {
            this.id = id;
        }

        @Override
        public void read(DataInputStream dataInputStream) throws IOException {
            id = dataInputStream.readInt();
        }

        @Override
        public void write(DataOutputStream dataOutputStream) throws IOException {
            dataOutputStream.writeInt(id);
            dataOutputStream.flush();
        }

        public int getId() {
            return id;
        }
    }
}
