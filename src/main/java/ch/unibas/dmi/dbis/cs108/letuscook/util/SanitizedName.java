package ch.unibas.dmi.dbis.cs108.letuscook.util;

/**
 * Wraps a {@link String} and guarantees it is non-null, non-empty, only contains allowed
 * characters, and is no longer than {@link SanitizedName#MAX_LENGTH} characters.
 */
public class SanitizedName {

	/**
	 * The maximum length for a sanitized name.
	 */
	public static final int MAX_LENGTH = 20;

	/**
	 * The sanitized content.
	 */
	private final String content;

	/**
	 * Construct a SanitizedName instance.
	 *
	 * @param content the sanitized name.
	 */
	private SanitizedName(String content) {
		this.content = content;
	}

	/**
	 * Checks if the given character is allowed.
	 *
	 * @param c the character.
	 * @return whether the character is allowed.
	 */
	public static boolean canContain(int c) {
		return
			c == '_' ||
				(c >= 'a' && c <= 'z') ||
				(c >= 'A' && c <= 'Z') ||
				(c >= '0' && c <= '9');
	}

	/**
	 * Create a sanitized name.
	 *
	 * @param string the string to be sanitized.
	 * @return the sanitized string.
	 * @throws MalformedException if the string is null, empty, or contains invalid characters.
	 */
	public static SanitizedName createOrThrow(String string) throws MalformedException {
		if (string == null) {
			throw new MalformedException("string is null");
		}

		StringBuilder sb = new StringBuilder();

		for (var c : string.toCharArray()) {
			if (SanitizedName.canContain(c)) {
				sb.append(c);
			}
		}

		if (sb.isEmpty()) {
			throw new MalformedException("name is empty");
		}

		return new SanitizedName(sb.substring(0, Math.min(sb.length(), MAX_LENGTH)));
	}

	/**
	 * Create a sanitized name. Use a fallback if the sanitization fails.
	 *
	 * @param string the string to be sanitized.
	 * @return the sanitized string.
	 */
	public static SanitizedName createOrUseFallback(String string, SanitizedName fallback) {
		try {
			return SanitizedName.createOrThrow(string);
		} catch (MalformedException e) {
			return fallback;
		}
	}

	/**
	 * Create a sanitized name. Assert that the sanitization succeeds.
	 *
	 * @param string the string to be sanitized.
	 * @return the sanitized string.
	 */
	public static SanitizedName createUnsafe(String string) {
		try {
			return SanitizedName.createOrThrow(string);
		} catch (MalformedException e) {
			assert false : "unsafe sanitization failed";

			return null;
		}
	}

	/**
	 * Check if a string can be sanitized.
	 *
	 * @param string the string to be checked.
	 * @return whether the string can be sanitized.
	 */
	public static boolean qualifies(String string) {
		try {
			SanitizedName.createOrThrow(string);
		} catch (MalformedException e) {
			return false;
		}

		return true;
	}

	/**
	 * @return a textual representation of the sanitized name.
	 */
	@Override
	public String toString() {
		return this.content;
	}
}
