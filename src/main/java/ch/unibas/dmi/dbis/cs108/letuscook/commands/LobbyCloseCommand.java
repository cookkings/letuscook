package ch.unibas.dmi.dbis.cs108.letuscook.commands;

import ch.unibas.dmi.dbis.cs108.letuscook.util.MalformedException;
import ch.unibas.dmi.dbis.cs108.letuscook.util.SanitizedName;

/**
 * Sent by the server to announce the closing of a lobby.
 */
public class LobbyCloseCommand extends Command {

	public static final String KEYWORD = "CLOSE";

	private final SanitizedName lobbyName;

	public LobbyCloseCommand(String lobbyName) throws MalformedException {
		this.lobbyName = SanitizedName.createOrThrow(lobbyName);
	}

	/**
	 * Creates an anonymous instance of this command, given a string of arguments.
	 *
	 * @param arguments the arguments
	 * @return the command.
	 * @throws MalformedException if the arguments are malformed.
	 */
	public static LobbyCloseCommand fromArguments(String arguments) throws MalformedException {
		return new LobbyCloseCommand(arguments);
	}

	public String getLobbyName() {
		return this.lobbyName.toString();
	}

	@Override
	public String toString() {
		return super.toString() + LobbyCloseCommand.KEYWORD + " " + this.lobbyName;
	}
}
