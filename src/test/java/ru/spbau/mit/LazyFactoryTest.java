package ru.spbau.mit;

import org.junit.Test;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.Assert.assertTrue;

/**
 * Created by the7winds on 10.02.16.
 */
public class LazyFactoryTest {

    @Test(timeout = 2000)
    public void simpleTestLazyMonothread() {
        Lazy<String> lazy = LazyFactory.createLazyMonothread (() -> "test");

        String a = lazy.get();
        String b = lazy.get();

        assertTrue(a == b);
    }

    @Test(timeout = 2000)
    public void nullTestLazyMonothread() {
        assertTrue(LazyFactory.createLazyMonothread(() -> null).get() == null);
    }

    @Test(timeout = 2000)
    public void simpleTestLazyMultithread() {
        Lazy<String> lazy = LazyFactory.createLazyMultithread(() -> "test");
        simpleLazyMultithreadTestFunc(lazy);
    }

    @Test(timeout = 2000)
    public void simpleTestLazyLockFree() {
        Lazy<String> lazy = LazyFactory.createLazyMultithreadLockFree(() -> "test");
        simpleLazyMultithreadTestFunc(lazy);
    }

    private <T> void simpleLazyMultithreadTestFunc(Lazy<T> lazy) {
        List<Future<T>> futures = new LinkedList<>();
        int limit = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(limit);
        for (int i = 0; i < limit; ++i) {
            futures.add(executorService.submit(lazy::get));
        }

        final T res = lazy.get();
        futures.stream()
                .map(tFuture -> {
                    try {
                        return tFuture.get();
                    } catch (InterruptedException | ExecutionException e) {
                        e.printStackTrace();
                    }
                    return null;
                }).forEach(t -> assertTrue(t == res));
    }

}