package com.fsm.logic;

import static com.fsm.logic.Elevator.Direction.DOWN;
import static com.fsm.logic.Elevator.Direction.UP;
import static com.fsm.logic.delay.InterruptDelay.getInfinitiveInterruptDelay;
import static com.fsm.logic.delay.InterruptDelay.getInterruptDelay;
import static java.lang.Math.min;
import java.util.NavigableSet;
import java.util.function.Function;
import javax.annotation.Nonnull;
import com.fsm.logic.delay.UninterruptedDelay;

public enum ElevatorState {

    // @formatter:off
    IDLE("Idle"),
    CLOSING("Closing"),
    OPEN("Open"),
    OPENING("Opening"),
    MOVING_UP("Moving up"),
    MOVING_DOWN("Moving down"),
    INIT("Init"),
    CLOSE("CLOSE");
    // @formatter:on

    private Function<Elevator, ElevatorState> nextSupplier;
    private final String displayValue;

    static {
        // initialize nextSupplier here to avoid illegal forward reference
        CLOSE.nextSupplier = (elevator) -> {
            boolean hasPressedButtonOnCurrentLevel = elevator.internalFloorsButtons.contains(elevator.currentLevel)
                    || elevator.externalFloorsButtons.contains(elevator.currentLevel);

            if (hasPressedButtonOnCurrentLevel) {
                return OPENING;
            }

            if (elevator.direction == UP) {
                boolean hasPressedButtonUpper = (elevator.internalFloorsButtons.higher(elevator.currentLevel) != null)
                        || (elevator.externalFloorsButtons.higher(elevator.currentLevel) != null);
                if (hasPressedButtonUpper) {
                    return MOVING_UP;
                }
            }

            if (elevator.direction == DOWN) {
                boolean hasPressedButtonsBelow = (elevator.internalFloorsButtons.floor(elevator.currentLevel) != null)
                        || (elevator.externalFloorsButtons.floor(elevator.currentLevel) != null);
                if (hasPressedButtonsBelow) {
                    return MOVING_DOWN;
                }
            }

            elevator.direction = null;
            return IDLE;
        };

        IDLE.nextSupplier = (elevator) -> {
            NavigableSet<Integer> internalButtons = elevator.internalFloorsButtons;
            NavigableSet<Integer> externalButtons = elevator.externalFloorsButtons;

            elevator.idleInterruptDelay = getInfinitiveInterruptDelay();
            // we shouldn't wait buttons if someone was pressed
            if (internalButtons.isEmpty() && externalButtons.isEmpty()) {
                elevator.idleInterruptDelay.waitForDelay();
            }
            int currentLevel = elevator.currentLevel;
            if (internalButtons.contains(currentLevel) || externalButtons.contains(currentLevel)) {
                return OPENING;
            } else if (internalButtons.floor(currentLevel) != null) {
                return MOVING_DOWN;
            } else if (internalButtons.higher(currentLevel) != null) {
                return MOVING_UP;
            } else if (externalButtons.floor(currentLevel) != null) {
                return MOVING_DOWN;
            } else if (externalButtons.higher(currentLevel) != null) {
                return MOVING_UP;
            } else {
                // wouldn't happen
                throw new IllegalStateException("Unexpected elevator state");
            }
        };

        CLOSING.nextSupplier = (elevator) -> {
            boolean wasInterrupted;
            long closingStart = System.currentTimeMillis();
            elevator.closingInterruptDelay = getInterruptDelay(elevator.doorMoveTimeInMs);
            wasInterrupted = elevator.openButtonPressed || elevator.closingInterruptDelay.waitForDelay();
            elevator.closingTimeSpent =
                    min(System.currentTimeMillis() - closingStart, elevator.doorMoveTimeInMs);
            return wasInterrupted ? OPENING : CLOSE;
        };

        OPEN.nextSupplier = (elevator) -> {
            new UninterruptedDelay(elevator.openDoorTimeInMs).waitForDelay();
            return CLOSING;
        };

        OPENING.nextSupplier = (elevator) -> {
            elevator.disableCurrentLevelButtons();
            elevator.externalFloorsButtons.remove(elevator.currentLevel);
            elevator.internalFloorsButtons.remove(elevator.currentLevel);
            new UninterruptedDelay(
                    elevator.closingTimeSpent == 0 ? elevator.doorMoveTimeInMs : elevator.closingTimeSpent)
                            .waitForDelay();
            return OPEN;
        };

        MOVING_UP.nextSupplier = (elevator) -> {
            elevator.direction = UP;
            new UninterruptedDelay(elevator.levelPassingTimeInMs).waitForDelay();

            NavigableSet<Integer> internalButtons = elevator.internalFloorsButtons;
            NavigableSet<Integer> externalButtons = elevator.externalFloorsButtons;
            int currentLevel = ++elevator.currentLevel;

            if (internalButtons.contains(currentLevel) || ((internalButtons.higher(currentLevel) == null)
                    && externalButtons.contains(currentLevel) && (externalButtons.higher(currentLevel) == null))) {
                return OPENING;
            } else {
                return MOVING_UP;
            }
        };

        MOVING_DOWN.nextSupplier = (elevator) -> {
            elevator.direction = DOWN;
            new UninterruptedDelay(elevator.levelPassingTimeInMs).waitForDelay();

            elevator.currentLevel--;
            if (elevator.internalFloorsButtons.contains(elevator.currentLevel) ||
                    elevator.externalFloorsButtons.contains(elevator.currentLevel)) {
                return OPENING;
            } else {
                return MOVING_DOWN;
            }
        };

        INIT.nextSupplier = (elevator) -> {
            elevator.currentLevel = 1;
            return IDLE;
        };
    }

    ElevatorState(@Nonnull String displayValue) {
        this.displayValue = displayValue;
    }

    @Override
    public String toString() {
        return displayValue;
    }

    @Nonnull
    public ElevatorState next(@Nonnull Elevator elevator) {
        return nextSupplier.apply(elevator);
    }
}
