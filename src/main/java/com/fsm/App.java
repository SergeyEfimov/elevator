package com.fsm;

import java.io.IOException;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import com.fsm.gui.ElevatorDashboard;
import com.fsm.logic.Elevator;

public class App {

    public static void main(String[] args) throws IOException {

        Options cmdOptions = new Options();

        cmdOptions.addOption(Option.builder("l").argName("l=value").longOpt("levels").numberOfArgs(2)
                .valueSeparator().desc("Number of levels").required().build());
        cmdOptions.addOption(Option.builder("h").argName("h=value").longOpt("height").numberOfArgs(2)
                .valueSeparator().desc("Level height in meters").required().build());
        cmdOptions.addOption(Option.builder("s").argName("s=value").longOpt("speed").numberOfArgs(2)
                .valueSeparator().desc("Speed in meters per second").required().build());
        cmdOptions.addOption(Option.builder().argName("doorMoveTime=value").longOpt("doorMoveTime").numberOfArgs(2)
                .valueSeparator().desc("The time (in seconds) of the doors opening or closing")
                .required().build());
        cmdOptions.addOption(Option.builder().argName("openDoorDelayTime=value").longOpt("openDoorDelayTime")
                .numberOfArgs(2).valueSeparator()
                .desc("The delay (in seconds) between when the door was open and will be closed").required().build());

        CommandLineParser parser = new DefaultParser();

        CommandLine cmd;
        try {
            cmd = parser.parse(cmdOptions, args);
        } catch (ParseException e) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("App", cmdOptions);
            return;
        }

        int levelsNumber;
        float levelHeight;
        float speed;
        long doorMoveTime;
        long openDoorDelayTime;
        try {
            levelsNumber = Integer.valueOf(cmd.getOptionValue("levels"));
        } catch (NumberFormatException e) {
            System.out.println("--levels should be a number");
            return;
        }
        try {
            levelHeight = Float.valueOf(cmd.getOptionValue("height"));
        } catch (NumberFormatException e) {
            System.out.println("--height should be a number");
            return;
        }
        try {
            speed = Float.valueOf(cmd.getOptionValue("speed"));
        } catch (NumberFormatException e) {
            System.out.println("--speed should be a number");
            return;
        }
        try {
            doorMoveTime = Long.valueOf(cmd.getOptionValue("doorMoveTime"));
        } catch (NumberFormatException e) {
            System.out.println("--doorMoveTime should be a number");
            return;
        }
        try {
            openDoorDelayTime = Long.valueOf(cmd.getOptionValue("openDoorDelayTime"));
        } catch (NumberFormatException e) {
            System.out.println("--openDoorDelayTime should be a number");
            return;
        }

        // check params
        if (levelsNumber > 20 || levelsNumber < 5) {
            System.out.println("--levels value should be between 5 and 20 (including)");
            return;
        }
        if (levelHeight <= 0) {
            System.out.println("--height value should be more than zero");
            return;
        }
        if (speed <= 0) {
            System.out.println("--speed value should be more than zero");
            return;
        }
        if (doorMoveTime <= 0) {
            System.out.println("--doorMoveTime value should be more than zero");
            return;
        }
        if (openDoorDelayTime <= 0) {
            System.out.println("--doorMoveTime value should be more than zero");
            return;
        }

        ElevatorDashboard dashboard = new ElevatorDashboard(levelsNumber);

        Elevator elevator =
                new Elevator(levelsNumber, levelHeight, speed, doorMoveTime * 1000, openDoorDelayTime * 1000,
                        dashboard::deselectLevelButtons, dashboard::setStatusText, dashboard::deselectOpenButton);

        dashboard.start(e -> elevator.addInternalCommand(e.getActionCommand()),
                e -> elevator.addExternalCommand(e.getActionCommand()));

        elevator.run();
    }

}
