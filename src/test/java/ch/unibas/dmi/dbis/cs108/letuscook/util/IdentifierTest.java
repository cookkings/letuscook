package ch.unibas.dmi.dbis.cs108.letuscook.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class IdentifierTest {

	@Test
	void testToString() {
		assertEquals("0", Identifier.NONE.toString());
	}

	@Test
	void testFromString() throws MalformedException {
		assertEquals(Identifier.NONE, Identifier.fromString("0"));
	}

	@Test
	void testNoneIsNone() {
		assertTrue(Identifier.NONE.isNone());
	}

	@Test
	void testNonNumericIsMalformed() {
		assertThrows(MalformedException.class, () -> Identifier.fromString(""));
		assertThrows(MalformedException.class, () -> Identifier.fromString(" "));
		assertThrows(MalformedException.class, () -> Identifier.fromString("x"));
	}

	@Test
	void testNegativeIsMalformed() {
		assertThrows(MalformedException.class, () -> Identifier.fromString("-1"));
	}

	@Test
	void testIdentifierIsIncremented() {
		IdentifierFactory if_ = new IdentifierFactory();
		assertEquals("1", if_.next().toString());
		assertEquals("2", if_.next().toString());
	}
}
