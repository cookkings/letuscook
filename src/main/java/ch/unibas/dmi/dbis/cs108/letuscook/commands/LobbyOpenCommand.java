package ch.unibas.dmi.dbis.cs108.letuscook.commands;

import ch.unibas.dmi.dbis.cs108.letuscook.util.MalformedException;
import ch.unibas.dmi.dbis.cs108.letuscook.util.SanitizedName;

/**
 * Sent by the client to open and immediately join a lobby. If successful, the server responds with
 * an {@link LobbyOpenCommand} followed by a {@link LobbyJoinCommand}. Clients other than the one
 * that issued the request receive only the {@link LobbyOpenCommand}.
 */
public class LobbyOpenCommand extends Command {

	public static final String KEYWORD = "OPEN";

	private final SanitizedName lobbyName;

	public LobbyOpenCommand(String lobbyName) throws MalformedException {
		this.lobbyName = SanitizedName.createOrThrow(lobbyName);
	}

	/**
	 * Creates an anonymous instance of this command, given a string of arguments.
	 *
	 * @param arguments the arguments
	 * @return the command.
	 * @throws MalformedException if the arguments are malformed.
	 */
	public static LobbyOpenCommand fromArguments(String arguments) throws MalformedException {
		return new LobbyOpenCommand(arguments);
	}

	public String getLobbyName() {
		return this.lobbyName.toString();
	}

	@Override
	public String toString() {
		return super.toString() + LobbyOpenCommand.KEYWORD + " " + this.lobbyName;
	}
}
