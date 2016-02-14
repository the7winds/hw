package ru.spbau.mit;

import org.junit.Test;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.function.Supplier;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by the7winds on 10.02.16.
 */
public class LazyFactoryTest {

    // Monothread tests

    @Test
    public void simpleTestLazyMonothread() {
        Lazy<String> lazy = LazyFactory.createLazyMonothread (() -> "test");

        String a = lazy.get();
        String b = lazy.get();

        assertEquals("test", a);
        assertEquals("test", b);
        assertTrue(a == b);
    }

    @Test
    public void nullTestLazyMonothread() {
        assertTrue(LazyFactory.createLazyMonothread(() -> null).get() == null);
    }

    @Test
    public void countTestMonothread() {
        Supplier<String> f = new Supplier<String>() {
            private boolean called = false;
            @Override
            public String get() {
                if (!called) {
                    called = true;
                    return "ok";
                } else {
                    return "failed";
                }
            }
        };

        Lazy<String> lazy = LazyFactory.createLazyMonothread(f);

        String a = lazy.get();
        String b = lazy.get();

        assertEquals("ok", a);
        assertTrue(a == b);
    }

    @Test
    public void LazinessTestLazyMonothread() {
        Wrapper<Boolean> asked = new Wrapper<>(false);

        Lazy<String> lazy = LazyFactory.createLazyMonothread (() -> {
            assertTrue(asked.x);
            return "test";
        });

        asked.x = true;
        String a = lazy.get();
        String b = lazy.get();

        assertEquals("test", a);
        assertEquals("test", b);
        assertTrue(a == b);
    }

    // Multithread tests

    private class Wrapper<T> {
        public volatile T x;

        public Wrapper(T a) {
            x = a;
        }
    }

    @Test
    public void countAndDataRaceAndLazinessTestMultiThread() throws InterruptedException {
        Wrapper<Boolean> asked = new Wrapper<>(false);
        Wrapper<Integer> counter = new Wrapper<>(0);
        String res = "test";

        Supplier<String> f = () -> {
            assertTrue(asked.x);
            counter.x++;
            return res;
        };

        Lazy<String> lazy = LazyFactory.createLazyMultithread(f);
        dataRaceAndLazinessTestLazyMultithreadFunc(lazy, res, asked);

        assertEquals(1, (int) counter.x);
    }

    @Test
    public void countAndDataRaceAndLazinessTestMultithreadLockFree() throws InterruptedException {
        Wrapper<Boolean> asked = new Wrapper<>(false);
        Map<String, Integer> counter = new HashMap<>();
        String res = "test";

        Supplier<String> f = () -> {
            assertTrue(asked.x);

            String key = Thread.currentThread().getName();
            counter.put(key, counter.containsKey(key) ? counter.get(key) + 1 : 0);

            return res;
        };

        Lazy<String> lazyLockFree = LazyFactory.createLazyMultithreadLockFree(f);
        dataRaceAndLazinessTestLazyMultithreadFunc(lazyLockFree, res, asked);
        counter.entrySet().stream()
                .forEach(e -> assertTrue(e.getValue() < 1));
    }

    private <T> void dataRaceAndLazinessTestLazyMultithreadFunc(Lazy<T> lazy, T res, Wrapper<Boolean> asked) throws InterruptedException {
        List<Future<T>> futures = new LinkedList<>();
        int limit = 3000;
        ExecutorService executorService = Executors.newFixedThreadPool(limit);
        CountDownLatch countDownLatch = new CountDownLatch(limit);

        for (int i = 0; i < limit; i++) {
            futures.add(executorService.submit(() -> {
                countDownLatch.countDown();
                countDownLatch.await();
                asked.x = true;
                return lazy.get();
            }));
        }

        countDownLatch.await();

        futures.stream()
                .map(tFuture -> {
                    try {
                        return tFuture.get();
                    } catch (InterruptedException | ExecutionException e) {
                        e.printStackTrace();
                    }
                    return null;
                }).forEach(t -> {
            assertEquals(res, t);
            assertTrue(t == res);
        });
    }

}