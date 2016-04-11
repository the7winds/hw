package torrent.client.clientNetworkImpl;

/**
 * Created by the7winds on 09.04.16.
 */
final class Notification {

    static void added(int id) {
        System.out.printf("ADDED WITH ID=%d\n", id);
    }

    static void downloaded() {
        System.out.println("DOWNLOADED");
    }

    static void downloaded(int id, int part) {
        System.out.printf("DOWNLOADED ID=%d PART=%d\n", id, part);
    }

    public static void connected() {
        System.out.println("CONNECTED");
    }
}
