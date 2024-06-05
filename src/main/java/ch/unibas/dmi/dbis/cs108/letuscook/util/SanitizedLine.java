package ch.unibas.dmi.dbis.cs108.letuscook.util;

/**
 * Wraps a {@link String} and guarantees it is non-null, non-blank and only contains allowed
 * characters.
 */
public class SanitizedLine {

	/**
	 * The sanitized content.
	 */
	private final String content;

	/**
	 * Construct a SanitizedLine instance.
	 *
	 * @param content the sanitized line.
	 */
	private SanitizedLine(String content) {
		this.content = content;
	}

	/**
	 * Checks if the given character is allowed.
	 *
	 * @param c the character.
	 * @return whether the character is allowed.
	 */
	public static boolean canContain(int c) {
		return c != '\r' && c != '\n';
	}

	/**
	 * Create a sanitized line.
	 *
	 * @param string the string to be sanitized.
	 * @return the sanitized line.
	 * @throws MalformedException if the string is null, blank, or contains invalid characters.
	 */
	public static SanitizedLine createOrThrow(String string) throws MalformedException {
		if (string == null) {
			throw new MalformedException("string is null");
		}

		if (string.isBlank()) {
			throw new MalformedException("string is blank");
		}

		if (string.chars().anyMatch(c -> !SanitizedLine.canContain(c))) {
			throw new MalformedException("string contains illegal character(s)");
		}

		return new SanitizedLine(string);
	}

	/**
	 * Check if a string is allowed.
	 *
	 * @param string the string to be checked.
	 * @return whether the string is allowed.
	 */
	public static boolean qualifies(String string) {
		try {
			SanitizedLine.createOrThrow(string);
		} catch (MalformedException e) {
			return false;
		}

		return true;
	}

	/**
	 * @return a textual representation of the sanitized line.
	 */
	@Override
	public String toString() {
		return this.content;
	}
}
