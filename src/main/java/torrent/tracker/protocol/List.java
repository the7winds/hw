package torrent.tracker.protocol;

import torrent.Sendable;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static torrent.tracker.FilesRegister.FileInfo;

/**
 * Created by the7winds on 26.03.16.
 */
public final class List {

    private static final byte TAG = 1;

    private List() {
        throw new UnsupportedOperationException();
    }

    public static class Request implements Sendable {

        /** Implies that tag has been read. */

        @Override
        public void read(DataInputStream dataInputStream) throws IOException {
        }

        @Override
        public void write(DataOutputStream dataOutputStream) throws IOException {
            dataOutputStream.writeByte(TAG);
            dataOutputStream.flush();
        }
    }

    public static class Answer implements Sendable {

        private Map<Integer, FileInfo> files;

        public Answer() {
        }

        public Answer(Map<Integer, FileInfo> files) {
            this.files = files;
        }

        @Override
        public void read(DataInputStream dataInputStream) throws IOException {
            files = new HashMap<>();
            int count = dataInputStream.readInt();
            for (int i = 0; i < count; ++i) {
                int id = dataInputStream.readInt();
                String name = dataInputStream.readUTF();
                long size = dataInputStream.readLong();
                files.put(id, new FileInfo(id, name, size));
            }
        }

        @Override
        public void write(DataOutputStream dataOutputStream) throws IOException {
            synchronized (files) {
                dataOutputStream.writeInt(files.size());
                for (FileInfo fileInfo : files.values()) {
                    dataOutputStream.writeInt(fileInfo.id);
                    dataOutputStream.writeUTF(fileInfo.name);
                    dataOutputStream.writeLong(fileInfo.size);
                }
                dataOutputStream.flush();
            }
        }

        public Map<Integer, FileInfo> getFiles() {
            return files;
        }
    }
}
