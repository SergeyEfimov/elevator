package com.fsm.logic;

import static java.lang.String.format;
import java.util.NavigableSet;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.function.Consumer;
import javax.annotation.Nonnull;
import com.fsm.logic.delay.Delay;
import com.fsm.logic.delay.MockDelay;

/**
 *
 */
public class Elevator implements Runnable {

    public static final String OPEN_COMMAND = "o";

    // elevator params
    private final int numberOfLevels;
    final long levelPassingTimeInMs;
    final long doorMoveTimeInMs;
    final long openDoorTimeInMs;

    // callbacks
    private final Consumer<String> printStatusCallback;
    private final Consumer<Integer> disableLevelButtonCallback;
    private final Runnable disableOpenButtonCallback;

    // elevator state
    private ElevatorState state;
    int currentLevel;
    // buttons state
    final NavigableSet<Integer> internalFloorsButtons = new ConcurrentSkipListSet<>();
    final NavigableSet<Integer> externalFloorsButtons = new ConcurrentSkipListSet<>();
    volatile boolean openButtonPressed = false;

    // state specific variables
    Direction direction;
    long closingTimeSpent;

    // these delay are not null to get rid from synchronization blocks (no need to check null values)
    volatile Delay idleInterruptDelay = new MockDelay();
    volatile Delay closingInterruptDelay = new MockDelay();

    public Elevator(int numberOfLevels, float levelHeightInMeter, float speedInMeterPerSecond, long doorMoveTimeInMs,
            long openDoorDelayTimeInMs, @Nonnull Consumer<Integer> disableLevelButtonCallback,
            @Nonnull Runnable disableOpenButtonCallback, @Nonnull Consumer<String> printStatusConsumer) {
        this.levelPassingTimeInMs = (long) (levelHeightInMeter * 1000 / speedInMeterPerSecond);
        this.numberOfLevels = numberOfLevels;
        this.doorMoveTimeInMs = doorMoveTimeInMs;
        this.openDoorTimeInMs = openDoorDelayTimeInMs;

        this.state = ElevatorState.INIT;

        this.printStatusCallback = printStatusConsumer;
        this.disableLevelButtonCallback = disableLevelButtonCallback;
        this.disableOpenButtonCallback = disableOpenButtonCallback;
    }

    @Override
    public void run() {
        // noinspection InfiniteLoopStatement
        while (true) {
            printStatusCallback.accept(format("Level: %d State: %s", currentLevel, state));
            state = state.next(this);
            // reset service button
            openButtonPressed = false;
            disableOpenButtonCallback.run();
        }
    }

    public void addInternalCommand(@Nonnull String command) throws IllegalArgumentException {
        if (command.toLowerCase().equals(OPEN_COMMAND)) {
            if (state == ElevatorState.CLOSING) {
                openButtonPressed = true;
                closingInterruptDelay.interrupt();
            }
        } else {
            internalFloorsButtons.add(getFloorNumberFromCommand(command));
            if (state == ElevatorState.IDLE) {
                idleInterruptDelay.interrupt();
            }

        }
    }

    public void addExternalCommand(@Nonnull String command) throws IllegalArgumentException {
        externalFloorsButtons.add(getFloorNumberFromCommand(command));
        if (state == ElevatorState.IDLE) {
            idleInterruptDelay.interrupt();
        }
    }

    void disableCurrentLevelButtons() {
        disableLevelButtonCallback.accept(currentLevel);
    }

    private int getFloorNumberFromCommand(@Nonnull String command) throws IllegalArgumentException {
        try {
            int floor = Integer.parseInt(command);
            if (floor < 1 && floor > numberOfLevels) {
                throw new IllegalArgumentException(
                        format("Wrong floor number %d. Should be between 1 and %d", floor, numberOfLevels));
            }
            return floor;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Unexpected format of command " + command, e);
        }
    }

    enum Direction {
        UP, DOWN
    }
}
