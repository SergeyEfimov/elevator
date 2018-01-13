package com.fsm.logic.delay;

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class InterruptDelay implements Delay{

    private final Future<Void> delayFuture;

    private InterruptDelay(long timeoutInMs) {
        delayFuture = Executors.newSingleThreadExecutor().submit(() -> {
            try {
                TimeUnit.MILLISECONDS.sleep(timeoutInMs);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return null;
        });
    }

    private InterruptDelay() {
        delayFuture = Executors.newSingleThreadExecutor().submit(() -> {
            try {
                //noinspection InfiniteLoopStatement
                while(true) {
                    TimeUnit.DAYS.sleep(Long.MAX_VALUE);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return null;
        });
    }

    public static InterruptDelay getInterruptDelay(long timeoutInMs) {
        return new InterruptDelay(timeoutInMs);
    }

    public static InterruptDelay getInfinitiveInterruptDelay() {
        return new InterruptDelay();
    }

    @Override
    public boolean waitForDelay() {
        try {
            delayFuture.get();
            return false;
        } catch (CancellationException e) {
            return true;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return true;
        } catch (ExecutionException e) {
            throw new IllegalStateException("Unexpected exception", e);
        }
    }

    @Override
    public boolean interrupt() {
        return delayFuture.cancel(true);
    }
}
