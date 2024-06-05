package ch.unibas.dmi.dbis.cs108.letuscook.orders;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

import ch.unibas.dmi.dbis.cs108.letuscook.util.MalformedException;
import org.junit.jupiter.api.Test;

public class OrderTest {

	/**
	 * This test checks the getPrice method of the Order class. It verifies that the method
	 * correctly returns the price of the HAMBURGER order.
	 */
	@Test
	public void testGetPriceCorrect() {
		int price = Order.HAMBURGER.getPrice();
		assertEquals(4, price);
	}

	/**
	 * This test checks the getPrice method of the Order class. It verifies that the method does not
	 * return an incorrect price for the HAMBURGER order.
	 */
	@Test
	public void testGetPriceIncorrect() {
		int price = Order.HAMBURGER.getPrice();
		assertNotEquals(5, price, "Price should be 3");
	}

	/**
	 * This test checks the fromString method of the Order class. It verifies that the method
	 * correctly converts a string to its corresponding Order. If the string does not correspond to
	 * any Order, a MalformedException is expected to be thrown.
	 */
	@Test
	public void testFromString() {
		try {
			Order order = Order.fromString("HAMBURGER");
			assertEquals(Order.HAMBURGER, order);
		} catch (MalformedException e) {
			fail("Exception should not be thrown for valid order");
		}
	}

	/**
	 * This test checks the fromString method of the Order class with an invalid string. It verifies
	 * that the method throws a MalformedException when the string does not correspond to any
	 * Order.
	 */
	@Test
	public void testFromStringInvalid() {
		assertThrows(MalformedException.class, () -> Order.fromString("INVALID_ORDER"));
	}
}
