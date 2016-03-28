package torrent.client;

import org.ini4j.Ini;
import torrent.GlobalConsts;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.stream.Collectors;

import static torrent.GlobalConsts.BLOCK_SIZE;

/**
 * Created by the7winds on 27.03.16.
 */
class AvailablePartsProvider {

    private static final Path AVAILABLE_PARTS_INFO = GlobalConsts.RESOURCES.resolve("availableParts.ini");
    private final Ini INI;

    private enum Keys {
        ID,
        PART_NUM,
        PART_PATH,
    }

    AvailablePartsProvider() throws IOException {
        INI = new Ini(AVAILABLE_PARTS_INFO.toFile());
    }

    synchronized Collection<Integer> getAllIds() {
        return INI.values().stream()
                .map(section -> Integer.valueOf(section.get(Keys.ID.name())))
                .distinct()
                .collect(Collectors.toList());
    }

    synchronized Collection<Integer> getAvailableById(int id) {
        return INI.values().stream()
                .filter(section -> Integer.valueOf(section.get(Keys.ID.name())) == id)
                .map(section -> Integer.valueOf(section.get(Keys.PART_NUM.name())))
                .collect(Collectors.toList());
    }

    synchronized byte[] getPart(int id, int part) throws IOException {
        byte[] content = new byte[BLOCK_SIZE];
        Path path = Paths.get(INI.get(getSectionName(id, part), Keys.PART_PATH.name()));
        DataInputStream partInputStream = new DataInputStream(Files.newInputStream(path));
        partInputStream.skipBytes(part * BLOCK_SIZE);

        try {
            partInputStream.readFully(content);
        } catch (EOFException ignored) {
        }

        return content;
    }

    private String getSectionName(int id, int part) {
        return id + ":" + part;
    }

    synchronized void addPart(int id, int part, File file) throws IOException {
        String sectionName = getSectionName(id, part);
        INI.add(sectionName, Keys.ID.name(), Integer.toString(id));
        INI.add(sectionName, Keys.PART_NUM.name(), Integer.toString(part));
        INI.add(sectionName, Keys.PART_PATH.name(), file.getAbsolutePath());
        INI.store();
    }

    synchronized void addFile(int id, File file) throws IOException {
        for (int n = (int) (file.length() / BLOCK_SIZE +
                (file.length() % BLOCK_SIZE != 0 ? 1 : 0)), i = 0; i < n; ++i) {
            addPart(id, i, file);
        }
    }
}