package com.nathan818.polus.api.util.concurrent;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;

public interface Executor extends ScheduledExecutorService {
    boolean inExecutor();

    boolean inExecutor(Thread thread);

    default void executeNow(Runnable command) {
        if (inExecutor()) {
            command.run();
        } else {
            execute(command);
        }
    }

    default <T> Future<T> submitNow(Callable<T> task) {
        if (inExecutor()) {
            try {
                return CompletableFuture.completedFuture(task.call());
            } catch (Throwable t) {
                CompletableFuture<T> ret = new CompletableFuture<>();
                ret.completeExceptionally(t);
                return ret;
            }
        } else {
            return submit(task);
        }
    }

    default Future<?> submitNow(Runnable task) {
        if (inExecutor()) {
            try {
                task.run();
                return CompletableFuture.completedFuture(null);
            } catch (Throwable t) {
                CompletableFuture<?> ret = new CompletableFuture<>();
                ret.completeExceptionally(t);
                return ret;
            }
        } else {
            return submit(task);
        }
    }

    default <T> Future<T> submitNow(Runnable task, T result) {
        if (inExecutor()) {
            try {
                task.run();
                return CompletableFuture.completedFuture(result);
            } catch (Throwable t) {
                CompletableFuture<T> ret = new CompletableFuture<>();
                ret.completeExceptionally(t);
                return ret;
            }
        } else {
            return submit(task, result);
        }
    }
}
