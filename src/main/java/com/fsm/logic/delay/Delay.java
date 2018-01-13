package com.fsm.logic.delay;

public interface Delay {
    /**
     * wait for delay
     * @return true if delay was interrupted, otherwise false
     */
    boolean waitForDelay();

    /**
     * interrupt delay
     * @return true if dalay was really interrupted, otherwise false
     */
    @SuppressWarnings("UnusedReturnValue")
    boolean interrupt();
}
