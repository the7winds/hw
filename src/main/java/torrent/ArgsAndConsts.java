package torrent;

import org.kohsuke.args4j.Option;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by the7winds on 27.03.16.
 */
public final class ArgsAndConsts {
    public static final int BLOCK_SIZE = 10 * (1 << 20); // 10 MB
    public static final short TRACKER_PORT = 8081;

    @Option(name="--mode")
    public static String mode;

    @Option(name="--ini")
    public static Path RESOURCES = Paths.get("./src/main/resources/");

    @Option(name="--port")
    public static short port = 8082;

    @Option(name="--host")
    public static String host = "localhost";
}
