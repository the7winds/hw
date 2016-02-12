package ru.spbau.mit;

import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import java.util.function.Supplier;

/**
 * Created by the7winds on 09.02.16.
 */

public class LazyFactory {

    public static <T> Lazy<T> createLazyMonothread(Supplier<T> f) {
        return new Lazy<T>() {
            private boolean called = false;
            private T res;

            @Override
            public T get() {
                if (!called) {
                    called = true;
                    res = f.get();
                }
                return res;
            }
        };
    }

    public static <T> Lazy<T> createLazyMultithread(Supplier<T> f) {
        return new Lazy<T>() {
            private volatile boolean called = false;
            private volatile T res;

            @Override
            public T get() {
                if (!called) {
                    synchronized (this) {
                        if (!called) {
                            called = true;
                            res = f.get();
                        }
                    }
                }
                return res;
            }
        };
    }

    public static <T> Lazy<T> createLazyMultithreadLockFree(Supplier<T> f) {
        return new Lazy<T>() {
            private volatile T res;

            @Override
            public T get() {
                AtomicReferenceFieldUpdater updater =
                        AtomicReferenceFieldUpdater.newUpdater(this.getClass(), Object.class, "res");
                updater.compareAndSet(this, null, f.get());
                return res;
            }
        };
    }

}
