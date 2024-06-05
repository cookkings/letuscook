package ch.unibas.dmi.dbis.cs108.letuscook.server;

import ch.unibas.dmi.dbis.cs108.letuscook.util.MalformedException;
import ch.unibas.dmi.dbis.cs108.letuscook.util.Messenger;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * List of highscores
 */
public class Highscores {

	private static final String FILE_PATH = "highscores.txt";

	private final List<Highscore> highscores = new ArrayList<>();

	public Highscores() {
		try {
			BufferedReader reader;

			reader = new BufferedReader(new FileReader(FILE_PATH, StandardCharsets.UTF_8));

			String line;

			while ((line = reader.readLine()) != null) {
				try {
					this.highscores.add(Highscore.fromString(line));
				} catch (MalformedException e) {
					Messenger.warn(
						"Could not parse highscore with format '" + line + "' - skipping");
				}
			}

			reader.close();
			this.highscores.sort(Comparator.comparingInt(Highscore::getScore).reversed());
		} catch (IOException e) {
			Messenger.warn("IO exception while trying to read " + FILE_PATH + " - ignoring");
		}

		Messenger.info("Parsed highscores: " + this);
	}

	public Highscores(List<Highscore> highscores) {
		this.highscores.addAll(highscores);
	}

	public static Highscores fromString(String string) throws MalformedException {
		List<Highscore> highscores = new ArrayList<>();

		for (String highscoreString : string.split(";")) {
			if (!highscoreString.isEmpty()) {
				highscores.add(Highscore.fromString(highscoreString));
			}
		}

		return new Highscores(highscores);
	}

	public List<Highscore> getHighscores() {
		return Collections.unmodifiableList(this.highscores);
	}

	public void submitScore(final Highscore score) {
		if (score.getNames().length == 0) {
			/* This can happen if the server stops a game because all players left. */
			return;
		}

		boolean overwritesExistingHighscore = false;

		for (Highscore existingHighscore : this.highscores) {
			if (existingHighscore.hasSameNamesAs(score)) {
				if (score.getScore() > existingHighscore.getScore()) {
					existingHighscore.setScore(score.getScore());
				}
				overwritesExistingHighscore = true;
				break;
			}
		}

		if (!overwritesExistingHighscore) {
			this.highscores.add(score);
			this.highscores.sort(Comparator.comparingInt(Highscore::getScore).reversed());
		}

		try {
			var writer = new FileWriter(FILE_PATH, StandardCharsets.UTF_8, false);

			for (var highscore : this.highscores) {
				writer.write(highscore.toString() + '\n');
			}

			writer.close();
		} catch (IOException e) {
			Messenger.error("IO exception while trying to write " + FILE_PATH + " - ignoring");
		}
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		if (this.highscores.isEmpty()) {
			sb.append(";");
			return sb.toString();
		}

		for (Highscore highscore : this.highscores) {
			sb.append(highscore.toString());
			sb.append(';');
		}

		if (!sb.isEmpty()) {
			sb.deleteCharAt(sb.length() - 1);
		}

		return sb.toString();
	}
}
