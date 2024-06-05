package ch.unibas.dmi.dbis.cs108.letuscook.commands;

import ch.unibas.dmi.dbis.cs108.letuscook.util.MalformedException;

/**
 * Represents a command sent by the server to indicate an interaction. This command does not require
 * any arguments.
 */
public class PlayerInteractCommand extends Command {

	/**
	 * The keyword used to identify this command.
	 */
	public static final String KEYWORD = "INTERACT";

	/**
	 * Constructs a new InteractCommand.
	 */
	public PlayerInteractCommand() {
	}

	/**
	 * Creates an anonymous instance of this command, given a string of arguments.
	 *
	 * @param arguments the arguments
	 * @return the command.
	 * @throws MalformedException if the arguments are malformed.
	 */
	public static PlayerInteractCommand fromArguments(String arguments) throws MalformedException {
		if (arguments != null && !arguments.isEmpty()) {
			throw new MalformedException("expected null or empty arguments");
		}

		return new PlayerInteractCommand();
	}

	/**
	 * Returns a string representation of this command.
	 *
	 * @return A string representation of the command.
	 */
	@Override
	public String toString() {
		return super.toString() + PlayerInteractCommand.KEYWORD;
	}
}
