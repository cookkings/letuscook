package ch.unibas.dmi.dbis.cs108.letuscook.commands;

import ch.unibas.dmi.dbis.cs108.letuscook.util.MalformedException;
import ch.unibas.dmi.dbis.cs108.letuscook.util.SanitizedName;

/**
 * Sent by the client to join a lobby. Sent by the server to indicate any client joining any lobby.
 */
public class LobbyJoinCommand extends Command {

	/**
	 * The keyword indicating the type of command.
	 */
	public static final String KEYWORD = "JOIN";
	/**
	 * The sanitized name of the lobby to join.
	 */
	private final SanitizedName lobbyName;

	/**
	 * Constructs a new JoinCommand object with the specified lobby name.
	 *
	 * @param lobbyName The name of the lobby to join.
	 * @throws MalformedException If the lobby name is malformed.
	 */
	public LobbyJoinCommand(String lobbyName) throws MalformedException {
		this.lobbyName = SanitizedName.createOrThrow(lobbyName);
	}

	/**
	 * Creates an anonymous instance of this command, given a string of arguments.
	 *
	 * @param arguments the arguments
	 * @return the command.
	 * @throws MalformedException if the arguments are malformed.
	 */
	public static LobbyJoinCommand fromArguments(String arguments)
		throws MalformedException {
		return new LobbyJoinCommand(arguments);
	}

	/**
	 * Gets the sanitized name of the lobby to join.
	 *
	 * @return The sanitized lobby name.
	 */
	public String getLobbyName() {
		return this.lobbyName.toString();
	}

	/**
	 * Returns a string representation of the JoinCommand.
	 *
	 * @return A string representation of the JoinCommand.
	 */
	@Override
	public String toString() {
		return super.toString() + LobbyJoinCommand.KEYWORD + " " + this.lobbyName;
	}
}
