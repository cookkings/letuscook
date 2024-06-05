package ch.unibas.dmi.dbis.cs108.letuscook.commands;

import ch.unibas.dmi.dbis.cs108.letuscook.util.MalformedException;

/**
 * Sent by the client to join the game.
 */
public class GameParticipateCommand extends Command {

	public static final String KEYWORD = "PARTICIPATE";

	/**
	 * Constructs a GameParticipateCommand.
	 */
	public GameParticipateCommand() {
	}

	/**
	 * Creates an anonymous instance of this command, given a string of arguments.
	 *
	 * @param arguments the arguments
	 * @return the command.
	 * @throws MalformedException if the arguments are malformed.
	 */
	public static GameParticipateCommand fromArguments(String arguments) throws MalformedException {
		if (arguments != null && !arguments.isEmpty()) {
			throw new MalformedException("expected null or empty arguments");
		}

		return new GameParticipateCommand();
	}

	/**
	 * @return a textual representation of this command.
	 */
	@Override
	public String toString() {
		return super.toString() + GameParticipateCommand.KEYWORD;
	}

}
