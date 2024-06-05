package ch.unibas.dmi.dbis.cs108.letuscook.commands;

import ch.unibas.dmi.dbis.cs108.letuscook.util.Identifier;
import ch.unibas.dmi.dbis.cs108.letuscook.util.MalformedException;

/**
 * Sent by the client to inform the server that it wants to disconnect. Sent by the server to
 * indicate a client disconnecting (the identifier may be {@link Identifier#NONE} if this command is
 * sent to a client without an attached record).
 */
public class DisappearCommand extends Command {

	public static final String KEYWORD = "BYE";

	/**
	 * Constructs a DisconnectCommand.
	 */
	public DisappearCommand() {
	}

	/**
	 * Creates an anonymous instance of this command, given a string of arguments.
	 *
	 * @param arguments the arguments
	 * @return the command.
	 * @throws MalformedException if the arguments are malformed.
	 */
	public static DisappearCommand fromArguments(String arguments) throws MalformedException {
		if (arguments != null && !arguments.isEmpty()) {
			throw new MalformedException("expected null or empty arguments");
		}

		return new DisappearCommand();
	}

	/**
	 * Returns a string representation of this command.
	 *
	 * @return A string representation of the command.
	 */
	@Override
	public String toString() {
		return super.toString() + DisappearCommand.KEYWORD;
	}
}
