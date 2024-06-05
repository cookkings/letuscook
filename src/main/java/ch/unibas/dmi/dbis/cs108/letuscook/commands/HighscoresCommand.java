package ch.unibas.dmi.dbis.cs108.letuscook.commands;

import ch.unibas.dmi.dbis.cs108.letuscook.server.Highscores;
import ch.unibas.dmi.dbis.cs108.letuscook.util.MalformedException;

public class HighscoresCommand extends Command {

	public static final String KEYWORD = "SCORES";

	private final Highscores highscores;

	public HighscoresCommand(Highscores highscores) {
		this.highscores = highscores;
	}

	/**
	 * Creates an anonymous instance of this command, given a string of arguments.
	 *
	 * @param arguments the arguments
	 * @return the command.
	 * @throws MalformedException if the arguments are malformed.
	 */
	public static HighscoresCommand fromArguments(String arguments) throws MalformedException {
		return new HighscoresCommand(Highscores.fromString(arguments));
	}

	public Highscores getHighscores() {
		return this.highscores;
	}

	@Override
	public String toString() {
		return super.toString() + HighscoresCommand.KEYWORD + " " + this.getHighscores().toString();
	}
}
