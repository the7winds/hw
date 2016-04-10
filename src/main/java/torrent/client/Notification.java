package torrent.client;

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

    public static void connected() {
        System.out.println("CONNECTED");
    }
}
