package ch.unibas.dmi.dbis.cs108.letuscook.orders;

import static org.junit.jupiter.api.Assertions.assertEquals;

import ch.unibas.dmi.dbis.cs108.letuscook.gui.Units;
import ch.unibas.dmi.dbis.cs108.letuscook.util.Coords;
import ch.unibas.dmi.dbis.cs108.letuscook.util.IdentifierFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ItemWorkbenchTest {

	private ItemWorkbench itemWorkbench;
	private Item content;

	/**
	 * Sets up all required prerequisites for the test units.
	 */
	@BeforeEach
	public void setUp() {
		content = Item.SALAD_HEAD;
		itemWorkbench = new ItemWorkbench(new IdentifierFactory().next(),
			new Coords(new Units(0), new Units(0)), content);
	}

	/**
	 * This test checks the creation of an ItemWorkbench. It verifies that the ItemWorkbench is
	 * correctly initialized with the given content.
	 */
	@Test
	public void testItemWorkbenchCreation() {
		assertEquals(Stack.of(content), itemWorkbench.peekContents());
	}

	/**
	 * This test checks the forceSetContentsAccordingToState method of the ItemWorkbench class. It
	 * verifies that the state of the workbench is correctly set according to the given Stack of
	 * contents.
	 */
	@Test
	public void testForceSetContentsAccordingToState() {
		Stack contents = Stack.of(content);
		itemWorkbench.forceSetContentsAccordingToState(contents);
		assertEquals(contents, itemWorkbench.peekContents());
	}

	/**
	 * This test checks the trade method of the ItemWorkbench class with an empty offer. It verifies
	 * that the method correctly handles the trade when the offer is empty.
	 */
	@Test
	public void testTradeWithEmptyOffer() {
		Stack offer = new Stack();
		Stack result = itemWorkbench.trade(offer);

		assertEquals(Stack.of(content), result);
	}

	/**
	 * This test checks the trade method of the ItemWorkbench class with a non-empty offer. It
	 * verifies that the method correctly handles the trade when the offer is not empty.
	 */
	@Test
	public void testTradeWithNonEmptyOffer() {
		Stack offer = Stack.of(Item.SALAD_HEAD);
		Stack result = itemWorkbench.trade(offer);

		assertEquals(offer, result);
	}
}
