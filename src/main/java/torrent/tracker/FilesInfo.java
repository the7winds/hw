package torrent.tracker;

import org.ini4j.Ini;
import torrent.GlobalConsts;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by the7winds on 27.03.16.
 */
public class FilesInfo {

    private int nextId;
    private static final Path FILES_INFO = GlobalConsts.RESOURCES.resolve("filesInfo.ini");
    private final Ini INI;

    private enum Keys {
        ID,
        SIZE,
        NAME
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
    }

    public FilesInfo() throws IOException {
        INI = new Ini(FILES_INFO.toFile());
        nextId = INI.keySet().stream()
                .mapToInt(Integer::valueOf)
                .max().orElse(0);
    }

    public synchronized Map<Integer, FileInfo> getList() {
        return INI.values().stream()
                .map(section ->
                        new FileInfo(Integer.valueOf(section.get(Keys.ID.name())),
                                section.get(Keys.NAME.name()),
                                Long.valueOf(section.get(Keys.SIZE.name()))))
                .collect(Collectors.toMap(fileInfo -> fileInfo.id, Function.identity()));
    }

    /** adds record to the file */

    public synchronized int addFile(String name, long size) throws IOException {
        String id = Integer.toString(nextId);

        INI.add(id, Keys.ID.name(), Integer.toString(nextId));
        INI.add(id, Keys.NAME.name(), name);
        INI.add(id, Keys.SIZE.name(), Long.toString(size));
        INI.store();

        return nextId++;
    }
}
