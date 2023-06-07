package de.invesdwin.context.client.javafx.util;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

import javax.annotation.concurrent.Immutable;

import de.invesdwin.util.concurrent.Executors;
import de.invesdwin.util.concurrent.WrappedExecutorService;
import de.invesdwin.util.concurrent.future.Futures;
import io.netty.util.concurrent.FastThreadLocal;
import javafx.application.Platform;
import javafx.concurrent.Task;

/**
 * InterruptedExceptions are handled here transparently, because in GUI applications these normally shouldn't occur. If
 * they do, one can check with Thread.currentThread().isInterrupted() inside loops.
 * 
 */
@Immutable
public final class FxApplicationThreadUtil {

    /**
     * threadlocal makes this a lot faster
     */
    private static final FastThreadLocal<Boolean> IS_FX_APPLICATION_THREAD = new FastThreadLocal<Boolean>() {
        @Override
        protected Boolean initialValue() throws Exception {
            //CHECKSTYLE:OFF must only be called by this class anyway
            return Platform.isFxApplicationThread();
            //CHECKSTYLE:ON
        }
    };
    /**
     * swingworker also has 10 as MAX_WORKER_THREADS
     */
    private static final WrappedExecutorService TASK_EXECUTOR = Executors
            .newFixedThreadPool(FxApplicationThreadUtil.class.getSimpleName() + "_TASKS", 10);

    private FxApplicationThreadUtil() {}

    public static <V> V executeAndWait(final Task<V> task) {
        TASK_EXECUTOR.execute(task);
        try {
            return Futures.get(task);
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        }
    }

    public static <V> Future<V> execute(final Task<V> task) {
        TASK_EXECUTOR.execute(task);
        return task;
    }

    public static <V> V runAndWait(final Callable<V> callable) throws InterruptedException {
        final FutureTask<V> future = new FutureTask<V>(callable);
        runLater(future);
        return Futures.get(future);
    }

    public static <V> Future<V> runLaterIfNotInFAT(final Callable<V> callable) {
        final FutureTask<V> future = new FutureTask<V>(callable);
        runLaterIfNotInFAT(future);
        return future;
    }

    public static <V> Future<V> runLater(final Callable<V> callable) {
        final FutureTask<V> future = new FutureTask<V>(callable);
        runLater(future);
        return future;
    }

    public static void runAndWait(final Runnable runnable) throws InterruptedException {
        final FutureTask<Void> future = new FutureTask<Void>(runnable, null);
        runLater(future);
        Futures.get(future);
    }

    public static void runLaterIfNotInFAT(final Runnable runnable) {
        if (isFxApplicationThread()) {
            runnable.run();
        } else {
            runLater(runnable);
        }
    }

    public static void runLater(final Runnable runnable) {
        //CHECKSTYLE:OFF must only be called by this class anyway
        Platform.runLater(runnable);
        //CHECKSTYLE:ON
    }

    public static void assertFxApplicationThread() {
        if (!isFxApplicationThread()) {
            throw new IllegalStateException("This should be called from inside the fx application thread!");
        }
    }

    public static void assertNotFxApplicationThread() {
        if (isFxApplicationThread()) {
            throw new IllegalStateException("This should be called from outside the fx application thread!");
        }
    }

    public static boolean isFxApplicationThread() {
        return IS_FX_APPLICATION_THREAD.get();
    }

}
