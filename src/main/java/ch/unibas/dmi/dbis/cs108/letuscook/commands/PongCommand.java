package ch.unibas.dmi.dbis.cs108.letuscook.commands;

import ch.unibas.dmi.dbis.cs108.letuscook.util.MalformedException;

/**
 * Sent by either the client or the server in response to a {@link PingCommand}.
 */
public class PongCommand extends Command {

	public static final String KEYWORD = "PONG";

	/**
	 * Constructs a PongCommand.
	 */
	public PongCommand() {
	}

	/**
	 * Creates an anonymous instance of this command, given a string of arguments.
	 *
	 * @param arguments the arguments
	 * @return the command.
	 * @throws MalformedException if the arguments are malformed.
	 */
	public static PongCommand fromArguments(String arguments) throws MalformedException {
		if (arguments != null && !arguments.isEmpty()) {
			throw new MalformedException("expected null or empty arguments");
		}

		return new PongCommand();
	}

	/**
	 * Returns a string representation of this command.
	 *
	 * @return A string representation of the command in the format: "[identifier] PONG".
	 */
	@Override
	public String toString() {
		return super.toString() + PongCommand.KEYWORD;
	}
}
