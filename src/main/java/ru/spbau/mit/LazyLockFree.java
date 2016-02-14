package ru.spbau.mit;

import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import java.util.function.Supplier;

/**
 * Created by the7winds on 14.02.16.
 */
public class LazyLockFree<T> implements Lazy<T> {
    private static final AtomicReferenceFieldUpdater<LazyLockFree, Object> UPDATER =
            AtomicReferenceFieldUpdater.newUpdater(LazyLockFree.class, Object.class, "res");
    private volatile Supplier<T> f;
    private volatile T res;

    public LazyLockFree(Supplier<T> foo) {
        f = foo;
    }

    @Override
    public T get() {
        Supplier<T> fun = f;

        if (fun != null) {
            if (UPDATER.compareAndSet(this, null, fun.get())) {
                f = null;
            }
        }

        return res;
    }
}