package ch.unibas.dmi.dbis.cs108.letuscook.orders;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ch.unibas.dmi.dbis.cs108.letuscook.util.MalformedException;
import org.junit.jupiter.api.Test;

public class StackTest {

	/**
	 * This test checks the creation of a Stack. It verifies that a new Stack is correctly
	 * initialized as empty.
	 */
	@Test
	public void testStackCreation() {
		Stack stack = new Stack();
		assertTrue(stack.isEmpty());
	}

	/**
	 * This test checks the creation of a Stack from a single item. It verifies that a new Stack
	 * created with a single item is not empty and contains the correct item.
	 */
	@Test
	public void testFromSingleItem() {
		// Test creation of a stack from a single item
		Stack stack = Stack.of(Item.CHEESE);
		assertFalse(stack.isEmpty());
		assertEquals(Item.CHEESE, stack.reduceToSingleItem());
	}

	/**
	 * This test checks the push method of the Stack class. It verifies that items can be correctly
	 * pushed onto the Stack.
	 */
	@Test
	public void testPush() {
		// Test pushing items onto the stack
		Stack stack1 = Stack.of(Item.BREAD);
		Stack stack2 = Stack.of(Item.CHEESE);
		stack1.push(stack2);
		assertFalse(stack1.isEmpty());

	}

	/**
	 * This test checks the fromString method of the Stack class. It verifies that a Stack can be
	 * correctly created from a string representation of items.
	 *
	 * @throws MalformedException
	 */
	@Test
	public void testFromString() throws MalformedException {
		Stack stack = Stack.fromString(
			Item.BREAD + "," + Item.CHEESE);
		assertFalse(stack.isEmpty());

	}

	/**
	 * This test checks the equality of Stacks. It verifies that two Stacks with the same items are
	 * considered equal.
	 */
	@Test
	public void testEquals() {
		Stack stack1 = Stack.of(Item.BREAD);
		Stack stack2 = Stack.of(Item.BREAD);
		assertEquals(stack1, stack2);
	}

}
