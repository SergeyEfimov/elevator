package com.fsm.logic;

import com.fsm.logic.delay.InterruptDelay;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.hamcrest.Matchers.greaterThan;

import org.junit.Test;

public class InterruptDelayTest {
    @Test
    public void testNonInterruptedCase() {
        long startTime = System.currentTimeMillis();
        InterruptDelay interruptDelay = InterruptDelay.getInterruptDelay(1000);
        assertFalse(interruptDelay.waitForDelay());
        assertThat(System.currentTimeMillis() - startTime, greaterThan(1000L));
    }

    @Test
    public void testInterruptedCase() {
        InterruptDelay interruptDelay = InterruptDelay.getInterruptDelay(2000);
        Thread interruptThread = new Thread(() -> {
            try {
                TimeUnit.MILLISECONDS.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            interruptDelay.interrupt();

        });
        interruptThread.start();
        assertTrue(interruptDelay.waitForDelay());
    }

    @Test
    public void testAlreadyInterruptedTimer() {
        InterruptDelay interruptDelay = InterruptDelay.getInterruptDelay(1000);
        interruptDelay.interrupt();
        assertTrue(interruptDelay.waitForDelay());
    }
}