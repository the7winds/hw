package ru.spbau.mit;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import java.util.LinkedList;
import java.util.List;

import static junit.framework.Assert.assertFalse;

/**
 * Created by the7winds on 20.03.16.
 */
public class ThreadRule implements TestRule {

    private volatile boolean exceptionHappened;
    private volatile boolean stillAlive;

    private final List<Thread> threads = new LinkedList<>();

    public void register(Thread thread) {
        threads.add(thread);
        thread.setUncaughtExceptionHandler((Thread t, Throwable e) -> {
            e.printStackTrace();
            exceptionHappened = true;
        });
    }

    @Override
    public Statement apply(Statement statement, Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                exceptionHappened = false;
                stillAlive = false;
                threads.clear();

                statement.evaluate();

                threads.forEach(thread -> stillAlive |= thread.isAlive());

                assertFalse(stillAlive);
                assertFalse(exceptionHappened);
            }
        };
    }
}
