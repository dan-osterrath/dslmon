package net.packsam.dslmon;

import net.packsam.dslmon.commands.StatusCommand;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.HelpCommand;

import java.util.concurrent.Callable;

@Command(
        name = "dslmon",
        mixinStandardHelpOptions = true,
        description = "Monitor Fritz!Box DSL connectivity",
        version = "dslmon 1.0.0",
        subcommands = {
                HelpCommand.class,
                StatusCommand.class,
        }
)
public class DSLMonitor implements Callable<Integer> {
    @Override
    public Integer call() {
        CommandLine.usage(this, System.out);
        return 0;
    }

    public static void main(String[] args) {
        CommandLine cli = new CommandLine(new DSLMonitor());
        var exitCode = cli.execute(args);
        System.exit(exitCode);
    }
}
