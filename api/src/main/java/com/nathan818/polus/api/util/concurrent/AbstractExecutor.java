package com.nathan818.polus.api.util.concurrent;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public abstract class AbstractExecutor implements Executor {
    private final ScheduledExecutorService handle;

    public AbstractExecutor(ScheduledExecutorService handle) {
        this.handle = handle;
    }

    /*
     * Delegate ScheduledExecutorService
     */

    @Override
    public ScheduledFuture<?> schedule(Runnable runnable, long l, TimeUnit timeUnit) {
        return handle.schedule(wrap(runnable), l, timeUnit);
    }

    @Override
    public <V> ScheduledFuture<V> schedule(Callable<V> callable, long l, TimeUnit timeUnit) {
        return handle.schedule(callable, l, timeUnit);
    }

    @Override
    public ScheduledFuture<?> scheduleAtFixedRate(Runnable runnable, long l, long l1, TimeUnit timeUnit) {
        return handle.scheduleAtFixedRate(wrap(runnable), l, l1, timeUnit);
    }

    @Override
    public ScheduledFuture<?> scheduleWithFixedDelay(Runnable runnable, long l, long l1, TimeUnit timeUnit) {
        return handle.scheduleWithFixedDelay(wrap(runnable), l, l1, timeUnit);
    }

    /*
     * Delegate ExecutorService
     */

    @Override
    public void shutdown() {
        handle.shutdown();
    }

    @Override
    public List<Runnable> shutdownNow() {
        return handle.shutdownNow();
    }

    @Override
    public boolean isShutdown() {
        return handle.isShutdown();
    }

    @Override
    public boolean isTerminated() {
        return handle.isTerminated();
    }

    @Override
    public boolean awaitTermination(long l, TimeUnit timeUnit) throws InterruptedException {
        return handle.awaitTermination(l, timeUnit);
    }

    @Override
    public <T> Future<T> submit(Callable<T> callable) {
        return handle.submit(callable);
    }

    @Override
    public <T> Future<T> submit(Runnable runnable, T t) {
        return handle.submit(wrap(runnable), t);
    }

    @Override
    public Future<?> submit(Runnable runnable) {
        return handle.submit(wrap(runnable));
    }

    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> collection) throws InterruptedException {
        return handle.invokeAll(collection);
    }

    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> collection, long l, TimeUnit timeUnit) throws InterruptedException {
        return handle.invokeAll(collection, l, timeUnit);
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> collection) throws InterruptedException, ExecutionException {
        return handle.invokeAny(collection);
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> collection, long l, TimeUnit timeUnit) throws InterruptedException, ExecutionException, TimeoutException {
        return handle.invokeAny(collection, l, timeUnit);
    }

    /*
     * Delegate Executor
     */

    @Override
    public void execute(Runnable runnable) {
        handle.execute(wrap(runnable));
    }

    /*
     * ----------
     */

    protected Runnable wrap(Runnable runnable) {
        return runnable;
    }
}
