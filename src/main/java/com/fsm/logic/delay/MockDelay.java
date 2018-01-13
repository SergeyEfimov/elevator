package com.fsm.logic.delay;

public class MockDelay implements Delay{
    @Override
    public boolean waitForDelay() {
        return false;
    }

    @Override
    public boolean interrupt() {
        return false;
    }
}
