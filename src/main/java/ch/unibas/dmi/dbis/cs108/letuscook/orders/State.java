package ch.unibas.dmi.dbis.cs108.letuscook.orders;

import ch.unibas.dmi.dbis.cs108.letuscook.util.MalformedException;
import java.util.Optional;

/**
 * State represents the possible states of an order.
 */
public enum State {

	IDLE, ACTIVE, FINISHED, EXPIRED;

	/**
	 * Parses a string representation of a State.
	 *
	 * @param string The string representation of the state.
	 * @return The State corresponding to the string.
	 * @throws MalformedException if the string does not represent a valid state.
	 */
	public static State fromString(String string) throws MalformedException {
		try {
			return State.valueOf(string);
		} catch (IllegalArgumentException e) {
			throw new MalformedException("bad state");
		}
	}

	/**
	 * Parses a string representation of a State into an Optional.
	 *
	 * @param string The string representation of the state.
	 * @return An Optional containing the State if the string is valid, otherwise empty.
	 */
	public static Optional<State> optionalFromString(String string) {
		try {
			return Optional.of(State.fromString(string));
		} catch (MalformedException e) {
			return Optional.empty();
		}
	}

	/**
	 * Returns the string representation of the State.
	 *
	 * @return The name of the State.
	 */
	@Override
	public final String toString() {
		return this.name();
	}
}
