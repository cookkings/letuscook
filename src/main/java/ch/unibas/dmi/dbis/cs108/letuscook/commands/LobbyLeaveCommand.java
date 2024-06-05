package ch.unibas.dmi.dbis.cs108.letuscook.commands;

import ch.unibas.dmi.dbis.cs108.letuscook.util.MalformedException;

/**
 * Sent by the client to leave a lobby. Sent by the server to indicate any client leaving any
 * lobby.
 */
public class LobbyLeaveCommand extends Command {

	/**
	 * The keyword indicating the type of command.
	 */
	public static final String KEYWORD = "LEAVE";

	/**
	 * Constructs a new LeaveCommand object.
	 */
	public LobbyLeaveCommand() {
	}

	/**
	 * Creates an anonymous instance of this command, given a string of arguments.
	 *
	 * @param arguments the arguments
	 * @return the command.
	 * @throws MalformedException if the arguments are malformed.
	 */
	public static LobbyLeaveCommand fromArguments(String arguments)
		throws MalformedException {
		if (arguments != null && !arguments.isEmpty()) {
			throw new MalformedException("expected null or empty arguments");
		}

		return new LobbyLeaveCommand();
	}

	/**
	 * Returns a string representation of the LeaveCommand.
	 *
	 * @return A string representation of the LeaveCommand.
	 */
	@Override
	public String toString() {
		return super.toString() + LobbyLeaveCommand.KEYWORD;
	}
}
