package ru.spbau.mit;

import org.junit.Test;

import java.io.*;
import java.util.Hashtable;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;


public class ClientServerTest {
    private String host = "localhost";
    private Map<String, Boolean> answer = new Hashtable<>();

    public ClientServerTest() {
        answer.put("3", true);
        answer.put("1", true);
        answer.put("t.txt", false);
    }

    private int getNewPort() {
        return 8000 + (new Random().nextInt()) % 100;
    }

    @Test
    public void simpleConnectionTest() throws IOException, InterruptedException {
        Server server = new Server();
        Client client = new Client();

        int port = getNewPort();
        server.start(port);
        client.connect(host, port);

        client.disconnect();
        server.stop();
    }

    @Test
    public void simpleListRequest() throws IOException, InterruptedException {

        Server server = new Server();
        Client client = new Client();

        int port = getNewPort();
        server.start(port);
        client.connect(host, port);

        assertEquals(answer, client.executeList("./src/test/resources"));

        client.disconnect();
        server.stop();
    }

    @Test
    public void simpleNullListRequest() throws IOException, InterruptedException {
        Server server = new Server();
        Client client = new Client();

        int port = getNewPort();
        server.start(port);
        client.connect(host, port);

        assertNull(client.executeList("./src/test/not_exists"));

        client.disconnect();
        server.stop();
    }

    @Test
    public void simpleGetRequest() throws IOException, InterruptedException {
        Server server = new Server();
        Client client = new Client();

        int port = getNewPort();
        server.start(port);
        client.connect(host, port);

        String p1 = "./src/test/resources/t.txt";
        String p2 = "./src/test/resources/1/t.txt";
        client.executeGet(p1, p2);

        client.disconnect();
        server.stop();

        BufferedReader br1 = new BufferedReader(new InputStreamReader(new FileInputStream(p1)));
        BufferedReader br2 = new BufferedReader(new InputStreamReader(new FileInputStream(p2)));
        assertEquals(br1.readLine(), br2.readLine());

        File file = new File(p2);
        file.delete();
    }

    @Test
    public void simpleNullGetRequest() throws IOException, InterruptedException {
        Server server = new Server();
        Client client = new Client();

        int port = getNewPort();
        server.start(port);
        client.connect(host, port);

        String p1 = "./src/test/resources/not_exists.txt";
        String p2 = "./src/test/resources/1/t.txt";

        assertNull(client.executeGet(p1, p2));

        client.disconnect();
        server.stop();
    }

    @Test
    public void manyClientsListRequest() throws IOException, InterruptedException {
        Server server = new Server();

        int port = getNewPort();
        server.start(port);

        int limit = 20;
        ExecutorService executorService = Executors.newFixedThreadPool(limit);
        CountDownLatch countDownLatch = new CountDownLatch(limit);
        CountDownLatch countDownLatch2 = new CountDownLatch(limit);

        for (int i = 0; i < limit; i++) {
            executorService.submit((Runnable) () -> {
                Client client = new Client();
                countDownLatch.countDown();
                try {
                    countDownLatch.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                try {
                    client.connect(host, port);
                    assertEquals(answer, client.executeList("./src/test/resources"));
                    client.disconnect();
                    countDownLatch2.countDown();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }

        countDownLatch2.await();
        server.stop();
    }
}
