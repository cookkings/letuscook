package ch.unibas.dmi.dbis.cs108.letuscook.commands;

import ch.unibas.dmi.dbis.cs108.letuscook.util.MalformedException;

/**
 * Sent by either the client or the server to provoke a health check.
 */
public class PingCommand extends Command {

	public static final String KEYWORD = "PING";

	/**
	 * Constructs a PingCommand.
	 */
	public PingCommand() {
	}

	/**
	 * Creates an anonymous instance of this command, given a string of arguments.
	 *
	 * @param arguments the arguments
	 * @return the command.
	 * @throws MalformedException if the arguments are malformed.
	 */
	public static PingCommand fromArguments(String arguments) throws MalformedException {
		if (arguments != null && !arguments.isEmpty()) {
			throw new MalformedException("expected null or empty arguments");
		}

		return new PingCommand();
	}

	/**
	 * Returns a string representation of this command.
	 *
	 * @return A string representation of the command in the format: "[identifier] PING".
	 */
	@Override
	public String toString() {
		return super.toString() + PingCommand.KEYWORD;
	}

}
