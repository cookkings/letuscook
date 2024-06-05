package ch.unibas.dmi.dbis.cs108.letuscook.util;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Creates identifiers.
 */
public class IdentifierFactory {

	/**
	 * The previous unique internal identifier.
	 */
	private final AtomicInteger previous = new AtomicInteger(Identifier.NONE.identifier);

	/**
	 * Create a new unique identifier.
	 */
	public Identifier next() {
		return new Identifier(this.previous.incrementAndGet());
	}
}
