package ch.unibas.dmi.dbis.cs108.letuscook.commands;

import ch.unibas.dmi.dbis.cs108.letuscook.util.MalformedException;
import ch.unibas.dmi.dbis.cs108.letuscook.util.SanitizedLine;

/**
 * Sent by the client to submit a chat message to all clients connected to the server.
 */
public class YellCommand extends Command {

	public static final String KEYWORD = "YELL";

	private final SanitizedLine message;

	/**
	 * Constructs a new yell command with the specified message.
	 *
	 * @param message the message to be yelled.
	 */
	public YellCommand(String message) throws MalformedException {
		this.message = SanitizedLine.createOrThrow(message);
	}

	/**
	 * Creates an anonymous instance of this command, given a string of arguments.
	 *
	 * @param arguments the arguments
	 * @return the command.
	 * @throws MalformedException if the arguments are malformed.
	 */
	public static YellCommand fromArguments(String arguments) throws MalformedException {
		return new YellCommand(arguments);
	}

	/**
	 * @return the message.
	 */
	public String getMessage() {
		return this.message.toString();
	}

	/**
	 * @return a textual representation of this command.
	 */
	@Override
	public String toString() {
		return super.toString() + YellCommand.KEYWORD + " " + this.getMessage();
	}
}
