package torrent.tracker;

import torrent.ArgsAndConsts;

import java.io.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by the7winds on 27.03.16.
 */
public class FilesRegister {

    private static final String registerName = "files register";

    private int nextId = 0;
    private final Map<Integer, FileInfo> register = Collections.synchronizedMap(new HashMap<>());

    public FilesRegister() throws IOException {
        File registerFile = ArgsAndConsts.RESOURCES.resolve(registerName).toFile();
        try (DataInputStream dataInputStream = new DataInputStream(new FileInputStream(registerFile))) {
            nextId = dataInputStream.readInt();
            for (int n = dataInputStream.readInt(), i = 0; i < n; ++i) {
                int id = dataInputStream.readInt();
                String name = dataInputStream.readUTF();
                long size = dataInputStream.readLong();
                register.put(id, new FileInfo(id, name, size));
            }
        } catch (FileNotFoundException ignored) {
        }
    }

    Map<Integer, FileInfo> getRegister() {
        return register;
    }

    /** adds record to the file */

    synchronized int addFile(String name, long size) throws IOException {
        register.put(nextId, new FileInfo(nextId, name, size));
        return nextId++;
    }

    void store() throws IOException {
        File registerFile = ArgsAndConsts.RESOURCES.resolve(registerName).toFile();
        registerFile.createNewFile();
        try (DataOutputStream dataOutputStream = new DataOutputStream(new FileOutputStream(registerFile))) {
            dataOutputStream.writeInt(nextId);
            dataOutputStream.writeInt(register.size());
            for (FileInfo entry : register.values()) {
                dataOutputStream.writeInt(entry.id);
                dataOutputStream.writeUTF(entry.name);
                dataOutputStream.writeLong(entry.size);
            }
        }
    }

    /** file descriptor */

    public static class FileInfo {
        public int id;
        public String name;
        public long size;

        public FileInfo(int id, String name, long size) {
            this.id = id;
            this.name = name;
            this.size = size;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof FileInfo) {
                FileInfo fileInfo = (FileInfo) obj;
                return fileInfo.id == id && fileInfo.name.equals(name) && fileInfo.size == size;
            }
            return false;
        }
    }
}
