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
        cmdOptions.addOption(Option.builder().argName("openDoorDelayTime=value").longOpt("openOpenDelayTime")
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

        int levelsNumber = Integer.valueOf(cmd.getOptionValue("levels"));
        float levelHeight = Float.valueOf(cmd.getOptionValue("height"));
        float speed = Float.valueOf(cmd.getOptionValue("speed"));
        long doorMoveTime = Long.valueOf(cmd.getOptionValue("doorMoveTime"));
        long openDoorDelayTime = Long.valueOf(cmd.getOptionValue("openOpenDelayTime"));

        ElevatorDashboard dashboard = new ElevatorDashboard(levelsNumber);

        Elevator elevator =
                new Elevator(levelsNumber, levelHeight, speed, doorMoveTime, openDoorDelayTime,
                        dashboard::unselectLevelButtons, dashboard::unselectOpenButton, dashboard::setStatusText);

        dashboard.start(e -> elevator.addInternalCommand(e.getActionCommand()),
                e -> elevator.addExternalCommand(e.getActionCommand()));

        elevator.run();
    }

}
