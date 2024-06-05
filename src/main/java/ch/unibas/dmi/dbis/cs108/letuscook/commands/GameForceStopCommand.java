package ch.unibas.dmi.dbis.cs108.letuscook.commands;

import ch.unibas.dmi.dbis.cs108.letuscook.util.MalformedException;

/**
 * Represents a command sent by the client to stop a game.
 */
public class GameForceStopCommand extends Command {

	/**
	 * The keyword used to identify this command.
	 */
	public static final String KEYWORD = "STOP";

	/**
	 * Constructs a new StopGameCommand.
	 */
	public GameForceStopCommand() {
	}

	/**
	 * Creates an anonymous instance of this command, given a string of arguments.
	 *
	 * @param arguments the arguments
	 * @return the command.
	 * @throws MalformedException if the arguments are malformed.
	 */
	public static GameForceStopCommand fromArguments(String arguments) throws MalformedException {
		if (arguments != null && !arguments.isEmpty()) {
			throw new MalformedException("expected null or empty arguments");
		}

		return new GameForceStopCommand();
	}

	/**
	 * Returns a string representation of this command.
	 *
	 * @return A string representation of the command.
	 */
	@Override
	public String toString() {
		return super.toString() + GameForceStopCommand.KEYWORD;
	}
}
