package net.packsam.dslmon.commands;

import picocli.CommandLine.Command;
import picocli.CommandLine.HelpCommand;

import java.util.concurrent.Callable;

@Command(
        name = "status",
        description = "Prints the current DSL status",
        subcommands = {
                HelpCommand.class,
        }
)
public class StatusCommand implements Callable<Integer> {
    @Override
    public Integer call() throws Exception {
        return 0;
    }
}
