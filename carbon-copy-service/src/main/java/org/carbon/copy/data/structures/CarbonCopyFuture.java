package org.carbon.copy.data.structures;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * CarbonCopys own Future. Using in conjunction with Messenger
 * to make it easier for people to use galaxys messenging infrastructure.
 */
class CarbonCopyFuture<T> implements Future<T> {
    private final CountDownLatch latch = new CountDownLatch(1);
    private Object result;

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return false;
    }

    @Override
    public boolean isCancelled() {
        return false;
    }

    @Override
    public boolean isDone() {
        return latch.getCount() < 1;
    }

    @Override
    public T get() throws InterruptedException, ExecutionException {
        latch.await();
        return castToGeneric(result);
    }

    @Override
    public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        latch.await(timeout, unit);
        return castToGeneric(result);
    }

    @SuppressWarnings("unchecked")
    private T castToGeneric(Object o) {
        return (T) o;
    }

    void complete(Object result) {
        this.result = result;
    }
}
