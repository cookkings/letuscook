package ch.unibas.dmi.dbis.cs108.letuscook.util;
/**
 * MalformedException represents an exception that is thrown when encountering malformed data or input.
 */
public class MalformedException extends Exception {
	/**
	 * Constructs a new MalformedException with the specified detail message.
	 *
	 * @param message The detail message.
	 */

	public MalformedException(String message) {
		super(message);
	}
}
