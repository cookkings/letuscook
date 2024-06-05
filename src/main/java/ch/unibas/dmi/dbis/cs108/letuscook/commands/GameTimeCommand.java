package ch.unibas.dmi.dbis.cs108.letuscook.commands;

import ch.unibas.dmi.dbis.cs108.letuscook.util.MalformedException;
import ch.unibas.dmi.dbis.cs108.letuscook.util.SanitizedName;

/**
 * Sent by the server the inform the client how many ticks are left in a game. The client processes
 * this information as follows:
 * <ol>
 *     <li>If there are 0 ticks remaining, the command is understood as a "Game Over"-notice.</li>
 *     <li>If the client did not previously know a game was running in the provided lobby, it starts the game.</li>
 *     <li>If the client already knew a game was running, it updates the ticks remaining.</li>
 * </ol>
 */
public class GameTimeCommand extends Command {

	/**
	 * The keyword used to identify this command.
	 */
	public static final String KEYWORD = "TIME";

	/**
	 * The sanitized name of the lobby where the game started.
	 */
	private final SanitizedName lobbyName;

	/**
	 * How many ticks are left in this game.
	 */
	private final int ticksUntilFinished;

	/**
	 * Constructs a new GameStartedCommand with the specified lobby name.
	 *
	 * @param lobbyName The name of the lobby where the game started.
	 * @throws MalformedException If the lobby name is malformed.
	 */
	public GameTimeCommand(String lobbyName, int ticksUntilFinished) throws MalformedException {
		this.lobbyName = SanitizedName.createOrThrow(lobbyName);
		this.ticksUntilFinished = ticksUntilFinished;
	}

	/**
	 * Creates an anonymous instance of this command, given a string of arguments.
	 *
	 * @param arguments the arguments
	 * @return the command.
	 * @throws MalformedException if the arguments are malformed.
	 */
	public static GameTimeCommand fromArguments(String arguments) throws MalformedException {
		if (arguments == null) {
			throw new MalformedException("arguments is null");
		}

		var lobbyAndTicks = arguments.split(" ", 2);

		if (lobbyAndTicks.length != 2) {
			throw new MalformedException("malformed arguments");
		}

		return new GameTimeCommand(lobbyAndTicks[0], Integer.parseUnsignedInt(lobbyAndTicks[1]));
	}

	/**
	 * Gets the name of the lobby where the game started.
	 *
	 * @return The name of the lobby.
	 */
	public String getLobbyName() {
		return this.lobbyName.toString();
	}

	/**
	 * @return the ticks until the game is finished.
	 */
	public int getTicksUntilFinished() {
		return this.ticksUntilFinished;
	}

	/**
	 * Returns a string representation of this command.
	 *
	 * @return A string representation of the command.
	 */
	@Override
	public String toString() {
		return super.toString() + GameTimeCommand.KEYWORD + " " + this.lobbyName + " "
			+ this.ticksUntilFinished;
	}
}
