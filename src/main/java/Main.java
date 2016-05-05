import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import torrent.ArgsAndConsts;
import torrent.client.Client;
import torrent.tracker.Tracker;

import java.io.IOException;

/**
 * Created by the7winds on 09.04.16.
 */
public class Main {

    private static final String CLIENT_MODE = "client";
    private static final String TRACKER_MODE = "tracker";
    private static final String ERROR_MESSAGE = "wrong mode";

    public static void main(String[] args) throws CmdLineException, IOException, InterruptedException {
        final CmdLineParser parser = new CmdLineParser(new ArgsAndConsts());
        parser.parseArgument(args);

        switch (ArgsAndConsts.mode) {
            case CLIENT_MODE:
                Client.getInstance().main();
                break;
            case TRACKER_MODE:
                Tracker.getInstance().main();
                break;
            default:
                throw new UnsupportedOperationException(ERROR_MESSAGE);
        }
    }
}
