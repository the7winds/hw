package torrent.client.protocol;

import torrent.Sendable;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;

/**
 * Created by the7winds on 27.03.16.
 */
public final class Stat {

    private static final byte TAG = 1;

    private Stat() {
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
            dataOutputStream.flush();
        }

        public int getId() {
            return id;
        }
    }

    public static class Answer implements Sendable {

        private Collection<Integer> available;

        public Answer() {
        }

        public Answer(Collection<Integer> available) {
            this.available = available;
        }

        @Override
        public void read(DataInputStream dataInputStream) throws IOException {
            available = new LinkedList<>();
            int count = dataInputStream.readInt();
            for (int i = 0; i < count; ++i) {
                int n = dataInputStream.readInt();
                available.add(n);
            }
        }

        @Override
        public void write(DataOutputStream dataOutputStream) throws IOException {
            dataOutputStream.writeInt(available.size());
            for (int n : available) {
                dataOutputStream.writeInt(n);
            }
            dataOutputStream.flush();
        }

        public Collection<Integer> getAvailable() {
            return available;
        }
    }
}
