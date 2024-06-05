package ch.unibas.dmi.dbis.cs108.letuscook.commands;

import ch.unibas.dmi.dbis.cs108.letuscook.util.Coords;
import ch.unibas.dmi.dbis.cs108.letuscook.util.MalformedException;

/**
 * Sent by the client to update its position. Sent by the server to update the player's positions.
 */
public class PlayerPositionCommand extends Command {

	/**
	 * The keyword.
	 */
	public static final String KEYWORD = "POSITION";

	/**
	 * The coordinates.
	 */
	private final Coords coords;

	/**
	 * Create a new instance of this command.
	 *
	 * @param coordsOrRect the coordinates (or rect).
	 */
	public PlayerPositionCommand(Coords coordsOrRect) {
		this.coords = new Coords(coordsOrRect.getX(), coordsOrRect.getY());
	}

	/**
	 * Creates an anonymous instance of this command, given a string of arguments.
	 *
	 * @param arguments the arguments
	 * @return the command.
	 * @throws MalformedException if the arguments are malformed.
	 */
	public static PlayerPositionCommand fromArguments(String arguments) throws MalformedException {
		if (arguments == null) {
			throw new MalformedException("null arguments");
		}

		return new PlayerPositionCommand(new Coords(arguments));
	}

	/**
	 * @return the coordinates.
	 */
	public Coords getCoords() {
		return this.coords;
	}

	/**
	 * @return a textual representation of the command.
	 */
	@Override
	public String toString() {
		return super.toString() + PlayerPositionCommand.KEYWORD + " " + this.getCoords();
	}
}
