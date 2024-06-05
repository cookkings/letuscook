package ch.unibas.dmi.dbis.cs108.letuscook.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class SanitizedNameTest {

	@Test
	void testMaxLength() throws MalformedException {
		assertEquals("abc".length(), SanitizedName.createOrThrow("abc").toString().length());
		assertTrue(SanitizedName.createOrThrow("abcdefghijklmnopqrstuvwxyz").toString().length()
			<= SanitizedName.MAX_LENGTH);
	}
}
