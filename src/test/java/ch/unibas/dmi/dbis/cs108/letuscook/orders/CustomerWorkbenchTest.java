package ch.unibas.dmi.dbis.cs108.letuscook.orders;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ch.unibas.dmi.dbis.cs108.letuscook.gui.Units;
import ch.unibas.dmi.dbis.cs108.letuscook.util.Coords;
import ch.unibas.dmi.dbis.cs108.letuscook.util.IdentifierFactory;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class CustomerWorkbenchTest {

	private CustomerWorkbench customerWorkbench;
	private Order order;

	/**
	 * Sets up all required prerequisites for the test units.
	 */
	@BeforeEach
	public void setUp() {
		order = Order.DOUBLE_CHEESEBURGER;
		customerWorkbench = new CustomerWorkbench(new IdentifierFactory().next(),
			new Coords(new Units(0), new Units(0)));
		customerWorkbench.forceSetState(State.ACTIVE);
	}

	/**
	 * This test checks if the CustomerWorkbench was created correctly. It asserts that the order of
	 * the customerWorkbench is not present after creation.
	 */
	@Test
	public void testCustomerWorkbenchCreation() {
		assertFalse(customerWorkbench.getOrder().isPresent());
	}

	/**
	 * This test checks if a new order is set correctly. It forces the order of the
	 * customerWorkbench to a specific order and then checks if the order was set correctly.
	 */
	@Test
	public void testForceSetOrder() {
		customerWorkbench.forceSetOrder(order);
		assertEquals(Optional.of(order), customerWorkbench.getOrder());
	}

	/**
	 * This test checks if the contents of the customerWorkbench are empty.
	 */
	@Test
	public void testPeekContents() {
		assertTrue(customerWorkbench.peekContents().isEmpty());
	}

	/**
	 * This test checks if the contents of the customerWorkbench are set correctly according to its
	 * state. It creates a new Stack as contents, sets the contents of the customerWorkbench
	 * according to its state, and then checks if the contents are empty.
	 */
	@Test
	public void testForceSetContentsAccordingToState() {
		Stack contents = new Stack();
		customerWorkbench.forceSetContentsAccordingToState(contents);
		assertTrue(customerWorkbench.peekContents().isEmpty());
	}

	/**
	 * This test checks if a trade with satisfying ingredients is acceptable. It creates a new Stack
	 * as an offer, forces the order of the customerWorkbench to a specific order, trades the offer
	 * with the customerWorkbench, and checks the result.
	 */
	@Test
	public void testTradeWithSatisfyingOffer() {
		Stack offer = Stack.of(Item.BREAD, Item.CHEESE, Item.CHEESE, Item.GRILLED_PATTY,
			Item.GRILLED_PATTY);
		customerWorkbench.forceSetOrder(order);
		Stack result = customerWorkbench.trade(offer);

		assertTrue(result.isEmpty());
	}

	/**
	 * This test checks if a trade with non-satisfying ingredients is acceptable. It creates a new
	 * Stack as an offer, forces the order of the customerWorkbench to a specific order, trades the
	 * offer with the customerWorkbench, and checks the result.
	 */
	@Test
	public void testTradeWithNonSatisfyingOffer() {
		Stack offer = Stack.of(Item.BREAD, Item.GRILLED_PATTY,
			Item.CHEESE);
		customerWorkbench.forceSetOrder(order);
		Stack result = customerWorkbench.trade(offer);

		assertEquals(offer, result);
	}
}
