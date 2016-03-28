package torrent.client.protocol;

import torrent.GlobalConsts;
import torrent.Sendable;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Created by the7winds on 27.03.16.
 */
public final class Get {

    private static final byte TAG = 2;

    private Get() {
        throw new UnsupportedOperationException();
    }

    public static class Request implements Sendable {

        private int id;
        private int part;

        public Request() {
        }

        public Request(int id, int part) {
            this.id = id;
            this.part = part;
        }

        @Override
        public void read(DataInputStream dataInputStream) throws IOException {
            id = dataInputStream.readInt();
            part = dataInputStream.readInt();
        }

        @Override
        public void write(DataOutputStream dataOutputStream) throws IOException {
            dataOutputStream.writeByte(TAG);
            dataOutputStream.writeInt(id);
            dataOutputStream.writeInt(part);
        }

        public int getId() {
            return id;
        }

        public int getPart() {
            return part;
        }
    }

    public static class Answer implements Sendable {

        private byte[] content;

        public Answer() {
        }

        public Answer(byte[] content) {
            this.content = content;
        }

        @Override
        public void read(DataInputStream dataInputStream) throws IOException {
            content = new byte[GlobalConsts.BLOCK_SIZE];
            dataInputStream.readFully(content);
        }

        @Override
        public void write(DataOutputStream dataOutputStream) throws IOException {
            dataOutputStream.write(content);
        }

        public byte[] getContent() {
            return content;
        }
    }
}
