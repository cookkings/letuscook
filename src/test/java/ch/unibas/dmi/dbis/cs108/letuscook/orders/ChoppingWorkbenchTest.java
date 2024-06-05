package ch.unibas.dmi.dbis.cs108.letuscook.orders;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ch.unibas.dmi.dbis.cs108.letuscook.gui.Units;
import ch.unibas.dmi.dbis.cs108.letuscook.util.Coords;
import ch.unibas.dmi.dbis.cs108.letuscook.util.IdentifierFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ChoppingWorkbenchTest {

	private ChoppingWorkbench choppingWorkbench;

	/**
	 * Sets up all required prerequisites for the test units.
	 */
	@BeforeEach
	public void setUp() {
		choppingWorkbench = new ChoppingWorkbench(new IdentifierFactory().next(),
			new Coords(new Units(0), new Units(0)));
	}

	/**
	 * This test checks if the ChoppingWorkbench was created correctly. It asserts that the contents
	 * of the choppingWorkbench are empty after creation
	 */
	@Test
	public void testChoppingWorkbenchCreation() {
		assertTrue(choppingWorkbench.peekContents().isEmpty());
	}

	/**
	 * This test checks if the state of the workbench is set correctly. It forces the state of the
	 * choppingWorkbench to ACTIVE and then sets the contents according to the state. It asserts
	 * that the contents of the choppingWorkbench contain the SALAD_HEAD item after these
	 * operations.
	 */
	@Test
	public void testForceSetContentsAccordingToState() {
		choppingWorkbench.forceSetState(State.ACTIVE);
		choppingWorkbench.forceSetContentsAccordingToState(Stack.of(Item.SALAD_HEAD));
		assertEquals(Item.SALAD_HEAD, choppingWorkbench.peekContents().reduceToSingleItem());
	}

	/**
	 * This test checks if the workbench gives the correct ingredient back after processing. It
	 * asserts that the result of finding a recipe by items in the choppingWorkbench equals a Stack
	 * of CHOPPED_SALAD.
	 */
	@Test
	public void testRecipe() {
		assertEquals(Stack.of(Item.CHOPPED_SALAD),
			choppingWorkbench.findRecipeByItems(Stack.of(Item.SALAD_HEAD)).orElseThrow().result());
	}

	/**
	 * This test checks that the choppingWorkbench accepts the offer and gives the correct
	 * ingredient back. It creates a new Stack of SALAD_HEAD as an offer, trades it with the
	 * choppingWorkbench, and checks the result. It asserts that the result is empty, the contents
	 * of the choppingWorkbench are not empty and contain the SALAD_HEAD item after the trade. It
	 * then forces the state of the choppingWorkbench to FINISHED and asserts that the contents of
	 * the choppingWorkbench equal a Stack of CHOPPED_SALAD.
	 */
	@Test
	public void testTrade() {
		Stack offer = Stack.of(Item.SALAD_HEAD);
		Stack result = choppingWorkbench.trade(offer);

		assertTrue(result.isEmpty());
		assertFalse(choppingWorkbench.peekContents().isEmpty());
		assertEquals(Item.SALAD_HEAD, choppingWorkbench.peekContents().reduceToSingleItem());

		choppingWorkbench.forceSetState(State.FINISHED);

		assertEquals(Stack.of(Item.CHOPPED_SALAD), choppingWorkbench.peekContents());
	}
}
