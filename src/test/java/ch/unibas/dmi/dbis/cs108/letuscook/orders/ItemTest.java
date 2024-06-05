package ch.unibas.dmi.dbis.cs108.letuscook.orders;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import ch.unibas.dmi.dbis.cs108.letuscook.util.MalformedException;
import org.junit.jupiter.api.Test;

public class ItemTest {

	/**
	 * This test checks the fromString method of the Item class. It verifies that the method
	 * correctly converts a string to its corresponding Item. If the string does not correspond to
	 * any Item, a MalformedException is expected to be thrown.
	 */
	@Test
	public void testFromString() {
		try {
			assertEquals(Item.BREAD, Item.fromString("BREAD"));
			assertEquals(Item.RAW_PATTY, Item.fromString("RAW_PATTY"));
			assertEquals(Item.CHEESE, Item.fromString("CHEESE"));
			assertEquals(Item.RAW_CHICKEN, Item.fromString("RAW_CHICKEN"));
		} catch (MalformedException e) {
			fail("Exception should not be thrown");
		}
	}

	/**
	 * This test checks the optionalFromString method of the Item class. It verifies that the method
	 * correctly converts a string to an Optional<Item>. If the string corresponds to an Item, the
	 * returned Optional should be non-empty. If the string does not correspond to any Item, the
	 * returned Optional should be empty.
	 */
	@Test
	public void testOptionalFromString() {
		assertTrue(Item.optionalFromString("BREAD").isPresent());
		assertTrue(Item.optionalFromString("RAW_PATTY").isPresent());
		assertTrue(Item.optionalFromString("CHEESE").isPresent());
		assertTrue(Item.optionalFromString("RAW_CHICKEN").isPresent());
		assertFalse(Item.optionalFromString("KUCHEN").isPresent());
	}

	/**
	 * This test checks the toString method of the Item class. It verifies that the method correctly
	 * converts an Item to its corresponding string representation.
	 */
	@Test
	public void testToString() {
		assertEquals("BREAD", Item.BREAD.toString());
		assertEquals("RAW_PATTY", Item.RAW_PATTY.toString());
		assertEquals("CHEESE", Item.CHEESE.toString());
		assertEquals("RAW_CHICKEN", Item.RAW_CHICKEN.toString());
	}
}
