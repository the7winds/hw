package torrent.tracker;

import java.net.Socket;

/**
 * Created by the7winds on 09.04.16.
 */
final class Notifications {

    static void start() {
        System.out.println("TRACKER STARTED");
    }

    static void accepted(Socket socket) {
        System.out.printf("ACCEPTED NEW CLIENT = %s\n", socket.getInetAddress().toString());
    }

    static void listGet(Socket socket) {
        System.out.printf("%s: LIST REQUEST GET\n", socket.toString());
    }

    static void listDone(Socket socket) {
        System.out.printf("%s: LIST REQUEST DONE\n", socket.toString());
    }

    static void uploadGet(Socket socket) {
        System.out.printf("%s: UPLOAD REQUEST\n", socket.toString());
    }

    static void uploadDone(Socket socket) {
        System.out.printf("%s: UPLOAD REQUEST DONE\n", socket.toString());
    }

    static void sourcesGet(Socket socket) {
        System.out.printf("%s: SOURCES REQUEST\n", socket.toString());
    }

    static void sourcesDone(Socket socket) {
        System.out.printf("%s: SOURCES REQUEST DONE\n", socket.toString());
    }

    static void updateGet(Socket socket) {
        System.out.printf("%s: UPDATE REQUEST\n", socket.toString());
    }

    static void updateDone(Socket socket) {
        System.out.printf("%s: UPDATE REQUEST DONE\n", socket.toString());
    }

    static void removeClient(byte[] ip, short port) {
        System.out.printf("REMOVE CLIENT: IP=%d:%d:%d:%d PORT=%d\n", ip[0], ip[1], ip[2], ip[3], port);
    }
}
