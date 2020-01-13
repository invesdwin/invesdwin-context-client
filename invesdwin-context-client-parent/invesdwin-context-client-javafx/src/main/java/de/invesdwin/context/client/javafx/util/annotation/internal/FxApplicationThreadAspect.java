package de.invesdwin.context.client.javafx.util.annotation.internal;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import javax.annotation.concurrent.ThreadSafe;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

import com.google.common.util.concurrent.ForwardingFuture.SimpleForwardingFuture;

import de.invesdwin.aspects.ProceedingJoinPoints;
import de.invesdwin.context.client.javafx.util.FxApplicationThreadUtil;
import de.invesdwin.context.client.javafx.util.annotation.FxApplicationThread;
import de.invesdwin.context.client.javafx.util.annotation.FxApplicationThread.InvocationType;
import de.invesdwin.norva.marker.SerializableVoid;
import de.invesdwin.util.error.UnknownArgumentException;

@ThreadSafe
@Aspect
public class FxApplicationThreadAspect {

    @Around("execution(* *(..)) && @annotation(de.invesdwin.context.client.javafx.util.annotation.FxApplicationThread)")
    public Object eventDispatchThread(final ProceedingJoinPoint pjp) throws Throwable {
        final FxApplicationThread annotation = ProceedingJoinPoints.getAnnotation(pjp, FxApplicationThread.class);
        final InvocationType invocationType = annotation.value();
        final Class<?> returnType = ProceedingJoinPoints.getMethod(pjp).getReturnType();

        if (returnType.equals(Void.TYPE) || returnType.equals(Void.class)
                || returnType.equals(SerializableVoid.class)) {
            return handleVoid(pjp, invocationType);
        } else if (Future.class.isAssignableFrom(returnType)) {
            return handleFuture(pjp, invocationType);
        } else {
            return handleObject(pjp, invocationType);
        }
    }

    private Object handleVoid(final ProceedingJoinPoint pjp, final InvocationType invocationType) throws Exception {
        final ProceedingJoinPointRunnable runnable = new ProceedingJoinPointRunnable(pjp);
        switch (invocationType) {
        case RUN_AND_WAIT:
            FxApplicationThreadUtil.runAndWait(runnable);
            break;
        case RUN_LATER:
            FxApplicationThreadUtil.runLater(runnable);
            break;
        case RUN_LATER_IF_NOT_IN_FAT:
            FxApplicationThreadUtil.runLaterIfNotInFAT(runnable);
            break;
        default:
            throw UnknownArgumentException.newInstance(InvocationType.class, invocationType);
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private Object handleFuture(final ProceedingJoinPoint pjp, final InvocationType invocationType) throws Exception {
        final ProceedingJoinPointCallable callable = new ProceedingJoinPointCallable(pjp);
        final Future<Object> f;
        switch (invocationType) {
        case RUN_AND_WAIT:
            f = (Future<Object>) FxApplicationThreadUtil.runAndWait(callable);
            break;
        case RUN_LATER:
            f = FxApplicationThreadUtil.runLater(callable);
            break;
        case RUN_LATER_IF_NOT_IN_FAT:
            f = FxApplicationThreadUtil.runLaterIfNotInFAT(callable);
            break;
        default:
            throw UnknownArgumentException.newInstance(InvocationType.class, invocationType);
        }
        //ForwardingFuture over 2 Levels
        return new SimpleForwardingFuture<Object>(f) {
            @Override
            public Object get() throws InterruptedException, ExecutionException {
                final Future<Object> f = (Future<Object>) super.get();
                if (f == null) {
                    return null;
                } else {
                    return f.get();
                }
            }
        };
    }

    private Object handleObject(final ProceedingJoinPoint pjp, final InvocationType invocationType) throws Throwable {
        final ProceedingJoinPointCallable callable = new ProceedingJoinPointCallable(pjp);
        try {
            switch (invocationType) {
            case RUN_AND_WAIT:
                return FxApplicationThreadUtil.runAndWait(callable);
            case RUN_LATER:
                return get(FxApplicationThreadUtil.runLater(callable));
            case RUN_LATER_IF_NOT_IN_FAT:
                return get(FxApplicationThreadUtil.runLaterIfNotInFAT(callable));
            default:
                throw UnknownArgumentException.newInstance(InvocationType.class, invocationType);
            }
        } catch (final RuntimeException e) {
            //Always unwrap, anything other than a RuntimeException cannot occur
            throw e.getCause();
        }
    }

    /**
     * Futures.get() hinders that the ExecutionException gets handled as it is.
     */
    private Object get(final Future<?> future) throws Throwable {
        try {
            return future.get();
        } catch (final ExecutionException e) {
            throw e.getCause();
        }
    }

    private static class ProceedingJoinPointRunnable implements Runnable {

        private final ProceedingJoinPoint pjp;

        ProceedingJoinPointRunnable(final ProceedingJoinPoint pjp) {
            this.pjp = pjp;
        }

        @Override
        public void run() {
            try {
                pjp.proceed();
            } catch (final Throwable e) {
                throw new RuntimeException(e);
            }
        }

    }

    private static class ProceedingJoinPointCallable implements Callable<Object> {

        private final ProceedingJoinPoint pjp;

        ProceedingJoinPointCallable(final ProceedingJoinPoint pjp) {
            this.pjp = pjp;
        }

        @Override
        public Object call() throws Exception {
            try {
                return pjp.proceed();
            } catch (final Throwable e) {
                //Always wrap so that RuntimeExceptions get handled properly
                throw new RuntimeException(e);
            }
        }

    }

}
