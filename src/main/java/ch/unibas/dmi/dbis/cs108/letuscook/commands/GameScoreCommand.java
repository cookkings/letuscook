package ch.unibas.dmi.dbis.cs108.letuscook.commands;

import ch.unibas.dmi.dbis.cs108.letuscook.util.MalformedException;

/**
 * Sent by the server to update the score.
 */
public class GameScoreCommand extends Command {

	/**
	 * The keyword.
	 */
	public static final String KEYWORD = "SCORE";

	/**
	 * The score.
	 */
	private final int score;

	/**
	 * Create a new instance of this command.
	 *
	 * @param score the score.
	 */
	public GameScoreCommand(final int score) {
		this.score = score;
	}

	/**
	 * Creates an anonymous instance of this command, given a string of arguments.
	 *
	 * @param arguments the arguments
	 * @return the command.
	 * @throws MalformedException if the arguments are malformed.
	 */
	public static GameScoreCommand fromArguments(String arguments) throws MalformedException {
		if (arguments == null) {
			throw new MalformedException("null arguments");
		}

		return new GameScoreCommand(Integer.parseInt(arguments));
	}

	/**
	 * @return the score.
	 */
	public int getScore() {
		return this.score;
	}

	/**
	 * @return a textual representation of the command.
	 */
	@Override
	public String toString() {
		return super.toString() + GameScoreCommand.KEYWORD + " " + this.getScore();
	}
}
