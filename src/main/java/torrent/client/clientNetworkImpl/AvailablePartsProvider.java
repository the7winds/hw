package torrent.client.clientNetworkImpl;

import torrent.ArgsAndConsts;

import java.io.*;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

import static torrent.ArgsAndConsts.BLOCK_SIZE;

/**
 * Created by the7winds on 27.03.16.
 */
class AvailablePartsProvider {

    private final static String registerFilename = "available parts register";
    private final Map<Integer, AvailablePartsInfo> register = new ConcurrentHashMap<>();

    AvailablePartsProvider() throws IOException {
        try {
            DataInput registerDataInput = new DataInputStream(new FileInputStream(registerFilename));
            while (true) {
                int id = registerDataInput.readInt();
                String name = registerDataInput.readUTF();
                String pathname = registerDataInput.readUTF();
                Set<Integer> availableParts = new TreeSet<>();
                for (int i = 0, n = registerDataInput.readInt(); i < n; ++i) {
                    availableParts.add(registerDataInput.readInt());
                }
                register.put(id, new AvailablePartsInfo(id, name, new File(pathname), availableParts));
            }
        } catch (FileNotFoundException ignored) {}
    }

    synchronized Collection<Integer> getAllIds() {
        return register.keySet();
    }

    synchronized Collection<Integer> getAvailableById(int id) {
        return register.get(id).availableParts;
    }

    synchronized byte[] getPart(int id, int part) throws IOException {
        RandomAccessFile rFile = new RandomAccessFile(register.get(id).file.getPath(), "rw");
        rFile.seek(part * BLOCK_SIZE);
        byte[] content = new byte[BLOCK_SIZE];

        try {
            rFile.readFully(content);
        } catch (EOFException ignored) {}

        return content;
    }

    synchronized void addPart(int id, int part) throws IOException {
        register.get(id).availableParts.add(part);
    }

    synchronized void addPart(int id, int part, File dest) throws IOException {
        register.getOrDefault(id, new AvailablePartsInfo(id, dest.getName(), dest)).availableParts.add(part);
    }

    synchronized void addFile(int id, File file) throws IOException {
        register.put(id, new AvailablePartsInfo(id, file.getName(), file));
        for (int part = 0; part < file.length() / ArgsAndConsts.BLOCK_SIZE
                + (file.length() % ArgsAndConsts.BLOCK_SIZE == 0 ? 0 : 1); ++part) {
            addPart(id, part);
        }
    }

    static class AvailablePartsInfo {
        final int id;
        final String name;
        final File file;
        final Set<Integer> availableParts;

        public AvailablePartsInfo(int id, String name, File file) {
            this.id = id;
            this.name = name;
            this.file = file;
            availableParts = new TreeSet<>();
        }

        public AvailablePartsInfo(int id, String name, File file, Set<Integer> availableParts) {
            this.id = id;
            this.name = name;
            this.file = file;
            this.availableParts = availableParts;
        }
    }

    void store() throws IOException {
        File registerFile = ArgsAndConsts.RESOURCES.resolve(registerFilename).toFile();
        registerFile.createNewFile();
        try (DataOutputStream dataOutputStream = new DataOutputStream(new FileOutputStream(registerFile))) {
            for (AvailablePartsInfo entry : register.values()) {
                dataOutputStream.writeInt(entry.id);
                dataOutputStream.writeUTF(entry.file.getName());
                dataOutputStream.writeUTF(entry.file.getPath());

                dataOutputStream.writeInt(entry.availableParts.size());
                for (Integer part : entry.availableParts) {
                    dataOutputStream.writeInt(part);
                }
            }
        }
    }
}