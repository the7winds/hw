package torrent.tracker;

import java.io.IOException;

/**
 * Created by the7winds on 09.04.16.
 */
public class Tracker {

    private static final Tracker INSTANCE = new Tracker();

    private static final String QUIT_CMD = "q";

    private TrackerImpl tracker;

    private Tracker() {}

    public static Tracker getInstance() {
        return INSTANCE;
    }

    public void main() throws IOException, InterruptedException {
        Notifications.start();

        tracker = new TrackerImpl();
        tracker.start();

        while (true) {
            String cmd = System.console().readLine();
            if (cmd != null && cmd.equals(QUIT_CMD)) {
                break;
            }
        }

        tracker.stop();
    }
}
