package ch.unibas.dmi.dbis.cs108.letuscook.commands;

import ch.unibas.dmi.dbis.cs108.letuscook.util.MalformedException;

/**
 * Sent by the client to request a refresh. Sent by the server to refresh the client, in which case
 * the client reads this commands' identifier to determine its own identifier.
 */
public class RefreshCommand extends Command {

	/**
	 * The keyword used to identify this command.
	 */
	public static final String KEYWORD = "REFRESH";

	/**
	 * Constructs a new RefreshCommand.
	 */
	public RefreshCommand() {
	}

	/**
	 * Creates an anonymous instance of this command, given a string of arguments.
	 *
	 * @param arguments the arguments
	 * @return the command.
	 * @throws MalformedException if the arguments are malformed.
	 */
	public static RefreshCommand fromArguments(String arguments) throws MalformedException {
		if (arguments != null && !arguments.isEmpty()) {
			throw new MalformedException("expected null or empty arguments");
		}

		return new RefreshCommand();
	}

	/**
	 * Returns a string representation of this command.
	 *
	 * @return A string representation of the command.
	 */
	@Override
	public String toString() {
		return super.toString() + RefreshCommand.KEYWORD;
	}
}
