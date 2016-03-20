package ru.spbau.mit;

import org.junit.Rule;
import org.junit.Test;

import java.io.*;
import java.util.Hashtable;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;


public class ClientServerTest {
    private final String host = "localhost";
    private final int port = 2000 + new Random().nextInt() % 1000;
    private final Map<String, Boolean> answer = new Hashtable<>();

    @Rule
    public ThreadRule threadRule = new ThreadRule();

    public ClientServerTest() {
        answer.put("3", true);
        answer.put("1", true);
        answer.put("t.txt", false);
    }

    @Test
    public void simpleListRequest() throws IOException, InterruptedException {
        Server server = new Server();
        Client client = new Client();

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

        server.start(port);

        int limit = 20;
        ExecutorService executorService = Executors.newFixedThreadPool(limit);
        CountDownLatch countDownLatch = new CountDownLatch(limit);

        for (int i = 0; i < limit; i++) {
            executorService.submit((Runnable) () -> {
                threadRule.register(Thread.currentThread());
                Client client = new Client();

                try {
                    countDownLatch.countDown();
                    countDownLatch.await();

                    client.connect(host, port);
                    if (!answer.equals(client.executeList("./src/test/resources"))) {
                        throw new RuntimeException();
                    }
                } catch (InterruptedException | IOException e) {
                    throw new RuntimeException(e);
                } finally {
                    try {
                        client.disconnect();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
        }

        executorService.shutdown();
        executorService.awaitTermination(500, TimeUnit.MILLISECONDS);
        server.stop();
    }
}
