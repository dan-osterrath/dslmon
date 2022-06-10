package net.packsam.dslmon;

import lombok.Getter;
import net.packsam.dslmon.commands.InspectCommand;
import picocli.CommandLine;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.HelpCommand;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.Spec;

import java.util.concurrent.Callable;

@Command(
		name = "dslmon",
		mixinStandardHelpOptions = true,
		description = "Monitor Fritz!Box DSL connectivity",
		version = "dslmon 1.0.0",
		subcommands = {
				HelpCommand.class,
				InspectCommand.class,
		}
)
public class DSLMonitor implements Callable<Integer> {

	public static final String DEFAULT_USERNAME_ENV_NAME = "FRITZBOX_USERNAME";
	public static final String DEFAULT_PASSWORD_ENV_NAME = "FRITZBOX_PASSWORD";
	public static final String DEFAULT_HOST = "fritz.box";
	public static final String DEFAULT_PORT = "49000";

	@SuppressWarnings("unused")
	@Option(
			names = {"-h", "--host"},
			description = "Hostname for the Fritz!Box. Default: \"${DEFAULT-VALUE}\"",
			defaultValue = DEFAULT_HOST
	)
	@Getter
	private String host;

	@SuppressWarnings("unused")
	@Option(
			names = {"-p", "--port"},
			description = "TCP port for the Fritz!Box management interface. Default: \"${DEFAULT-VALUE}\"",
			defaultValue = DEFAULT_PORT
	)
	@Getter
	private int port;

	@SuppressWarnings("unused")
	@ArgGroup(heading = "Username")
	private UsernameOption usernameOption;

	@SuppressWarnings("unused")
	@ArgGroup(heading = "Password")
	private PasswordOption passwordOption;

	@SuppressWarnings("unused")
	@Spec
	private CommandSpec spec;

	@Override
	public Integer call() {
		CommandLine.usage(this, System.out);
		return 0;
	}

	public static void main(String[] args) {
		var cli = new CommandLine(new DSLMonitor());
		var exitCode = cli.execute(args);
		System.exit(exitCode);
	}

	public String getUsername() {
		if (usernameOption == null) {
			var envUsername = System.getenv(DEFAULT_USERNAME_ENV_NAME);
			if (envUsername != null && !envUsername.isBlank()) {
				return envUsername;
			}
		} else {
			if (usernameOption.username != null) {
				return usernameOption.username;
			}

			if (usernameOption.usernameEnvName != null) {
				var envUsername = System.getenv(usernameOption.usernameEnvName);
				if (envUsername != null && !envUsername.isBlank()) {
					return envUsername;
				}
			}
		}

		throw new CommandLine.ParameterException(spec.commandLine(), "Username required");

	}

	public String getPassword() {
		if (passwordOption == null) {
			var envPassword = System.getenv(DEFAULT_PASSWORD_ENV_NAME);
			if (envPassword != null && !envPassword.isBlank()) {
				return envPassword;
			}
		} else {
			if (passwordOption.password != null) {
				return passwordOption.password;
			}

			if (passwordOption.passwordEnvName != null) {
				var envPassword = System.getenv(passwordOption.passwordEnvName);
				if (envPassword != null && !envPassword.isBlank()) {
					return envPassword;
				}
			}
		}

		throw new CommandLine.ParameterException(spec.commandLine(), "Password required");

	}

	public static class UsernameOption {
		@SuppressWarnings("unused")
		@Option(
				names = {"-u", "--username"},
				description = "Username for accessing the Fritz!Box management interface."
		)
		private String username;

		@SuppressWarnings("unused")
		@Option(
				names = {"--username:env"},
				description = "Environment variable containing the username for accessing the Fritz!Box management interface. Default: \"" + DEFAULT_USERNAME_ENV_NAME + "\""
		)
		private String usernameEnvName;
	}

	public static class PasswordOption {
		@SuppressWarnings("unused")
		@Option(
				names = {"--password"},
				description = "Password for accessing the Fritz!Box management interface.",
				interactive = true
		)
		private String password;

		@SuppressWarnings("unused")
		@Option(
				names = {"--password:env"},
				description = "Environment variable containing the password for accessing the Fritz!Box management interface. Default: \"" + DEFAULT_PASSWORD_ENV_NAME + "\""
		)
		private String passwordEnvName;
	}
}
