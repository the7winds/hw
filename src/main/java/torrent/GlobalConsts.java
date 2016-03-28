package torrent;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by the7winds on 27.03.16.
 */
public class GlobalConsts {
    public static final int BLOCK_SIZE = 10 * (1 << 20); // 10 MB
    public static final short TRACKER_PORT = 8081;
    public static final Path RESOURCES = Paths.get("./src/main/resources/");
}
