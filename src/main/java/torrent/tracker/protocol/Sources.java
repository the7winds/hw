package torrent.tracker.protocol;

import torrent.Sendable;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
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
            dataOutputStream.flush();
        }

        public int getId() {
            return id;
        }
    }

    public static class Answer implements Sendable {

        private Collection<InetSocketAddress> sources;

        public Answer() {
        }

        public Answer(Collection<InetSocketAddress> sources) {
            this.sources = sources;
        }

        @Override
        public void read(DataInputStream dataInputStream) throws IOException {
            sources = new LinkedList<>();
            int size = dataInputStream.readInt();
            for (int i = 0; i < size; ++i) {
                byte[] ip = new byte[4];
                dataInputStream.readFully(ip);
                short port = dataInputStream.readShort();
                sources.add(new InetSocketAddress(InetAddress.getByAddress(ip), port));
            }
        }

        @Override
        public void write(DataOutputStream dataOutputStream) throws IOException {
            dataOutputStream.writeInt(sources.size());
            for (InetSocketAddress clientInfo : sources) {
                dataOutputStream.write(clientInfo.getAddress().getAddress(), 0, 4);
                dataOutputStream.writeShort(clientInfo.getPort());
            }
            dataOutputStream.flush();
        }

        public Collection<InetSocketAddress> getSources() {
            return sources;
        }
    }
}
