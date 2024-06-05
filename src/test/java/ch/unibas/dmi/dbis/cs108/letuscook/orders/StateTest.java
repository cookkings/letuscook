package ch.unibas.dmi.dbis.cs108.letuscook.orders;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import ch.unibas.dmi.dbis.cs108.letuscook.util.MalformedException;
import java.util.Optional;
import org.junit.jupiter.api.Test;

public class StateTest {

	/**
	 * This test checks the fromString method of the State class with a valid state. It verifies
	 * that the method correctly converts a string to its corresponding State. If the string does
	 * not correspond to any State, a MalformedException is expected to be thrown.
	 */
	@Test
	public void testFromStringValid() {
		try {
			State state = State.fromString("ACTIVE");
			assertEquals(State.ACTIVE, state);
		} catch (MalformedException e) {
			fail("Exception should not be thrown for valid state");
		}
	}

	/**
	 * This test checks the fromString method of the State class with an invalid state. It verifies
	 * that the method throws a MalformedException when the string does not correspond to any
	 * State.
	 */
	@Test
	public void testFromStringInvalid() {
		assertThrows(MalformedException.class, () -> State.fromString("INVALID"));
	}

	/**
	 * This test checks the optionalFromString method of the State class with a valid state. It
	 * verifies that the method correctly converts a string to an Optional<State>. If the string
	 * corresponds to a State, the returned Optional should be non-empty.
	 */
	@Test
	public void testOptionalFromStringValid() {
		Optional<State> state = State.optionalFromString("FINISHED");
		assertTrue(state.isPresent());
		assertEquals(State.FINISHED, ((Optional<?>) state).orElseThrow());
	}

	/**
	 * This test checks the optionalFromString method of the State class with an invalid state. It
	 * verifies that if the string does not correspond to any State, the returned Optional should be
	 * empty.
	 */
	@Test
	public void testOptionalFromStringInvalid() {
		Optional<State> state = State.optionalFromString("INVALID");
		assertFalse(state.isPresent());
	}
}
