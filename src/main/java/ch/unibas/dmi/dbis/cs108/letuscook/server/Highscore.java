package ch.unibas.dmi.dbis.cs108.letuscook.server;

import ch.unibas.dmi.dbis.cs108.letuscook.util.MalformedException;
import java.util.Arrays;

/**
 * Saves the highscore of the game
 */
public class Highscore {

	private final String[] names;

	private int score;

	public Highscore(String[] names, int score) {
		Arrays.sort(names);
		this.names = names;
		this.score = score;
	}

	public static Highscore fromString(String string) throws MalformedException {
		try {
			var scoreAndNames = string.split(":", 2);
			if (scoreAndNames.length != 2) {
				throw new MalformedException("missing score and/or names");
			}

			int score = Integer.parseInt(scoreAndNames[0]);
			String[] names = scoreAndNames[1].split(",");
			if (names.length < 1) {
				throw new MalformedException("not enough names");
			}
			Arrays.sort(names);

			return new Highscore(names, score);
		} catch (NumberFormatException e) {
			throw new MalformedException("malformed score");
		}
	}

	public String[] getNames() {
		return this.names;
	}

	public int getScore() {
		return this.score;
	}

	public void setScore(int score) {
		assert score >= this.score;

		this.score = score;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(String.valueOf(this.score));
		sb.append(':');

		for (String name : this.names) {
			sb.append(name);
			sb.append(',');
		}

		sb.deleteCharAt(sb.length() - 1);

		return sb.toString();
	}

	public boolean hasSameNamesAs(Highscore that) {
		Arrays.sort(this.names);
		Arrays.sort(that.names);

		return Arrays.equals(this.names, that.names);
	}
}
