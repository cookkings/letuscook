package ch.unibas.dmi.dbis.cs108.letuscook.util;

import java.util.Objects;

/**
 * Uniquely identifies a client.
 */
public class Identifier {

	/**
	 * Represents the absence of an identifier.
	 */
	public static final Identifier NONE = new Identifier(0);

	/**
	 * The internal identifier.
	 */
	final int identifier;

	/**
	 * Create an identifier.
	 *
	 * @param identifier the internal identifier.
	 */
	Identifier(int identifier) {
		assert identifier >= 0;

		this.identifier = identifier;
	}

	/**
	 * Parse an identifier.
	 *
	 * @param string the string containing the identifier.
	 * @return the identifier.
	 * @throws MalformedException if the string contains a malformed identifier.
	 */
	public static Identifier fromString(String string) throws MalformedException {
		int identifier;

		try {
			identifier = Integer.parseInt(string);
		} catch (NumberFormatException e) {
			identifier = -1;
		}

		if (identifier < 0) {
			throw new MalformedException("malformed identifier");
		}

		return new Identifier(identifier);
	}

	/**
	 * @return whether this identifier is equal to {@link Identifier#NONE}.
	 */
	public boolean isNone() {
		return this.equals(Identifier.NONE);
	}

	/**
	 * @return whether this identifier is not equal to {@link Identifier#NONE}.
	 */
	public boolean isSome() {
		return !this.isNone();
	}

	/**
	 * Compare two identifiers for equality.
	 *
	 * @param that the identifier to compare to.
	 * @return whether the two identifiers are equal.
	 */
	@Override
	public boolean equals(Object that) {
		if (!(that instanceof Identifier)) {
			return false;
		}

		return this.identifier == ((Identifier) that).identifier;
	}

	/**
	 * @return a hash.
	 */
	@Override
	public int hashCode() {
		return Objects.hash(this.identifier);
	}

	/**
	 * Get a textual representation of an identifier.
	 *
	 * @return the textual representation.
	 */
	@Override
	public String toString() {
		return String.valueOf(this.identifier);
	}
}
