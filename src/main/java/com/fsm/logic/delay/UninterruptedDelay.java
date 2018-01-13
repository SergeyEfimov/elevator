package com.fsm.logic.delay;

import java.util.concurrent.TimeUnit;

public class UninterruptedDelay implements Delay {

    private final long timeoutInMs;

    public UninterruptedDelay(long timeoutInMs) {
        this.timeoutInMs = timeoutInMs;
    }

    @Override
    public boolean waitForDelay() {
        try {
            TimeUnit.MILLISECONDS.sleep(timeoutInMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return false;
    }

    @Override
    public boolean interrupt() {
        throw new UnsupportedOperationException("Trying to interrupt uninterruptible delay");
    }
}
