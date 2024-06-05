package ch.unibas.dmi.dbis.cs108.letuscook.orders;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ch.unibas.dmi.dbis.cs108.letuscook.gui.Units;
import ch.unibas.dmi.dbis.cs108.letuscook.util.Coords;
import ch.unibas.dmi.dbis.cs108.letuscook.util.IdentifierFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class GrillWorkbenchTest {

	private GrillWorkbench grill;

	/**
	 * Sets up all required prerequisites for the test units.
	 */
	@BeforeEach
	public void setUp() {
		grill = new GrillWorkbench(new IdentifierFactory().next(),
			new Coords(new Units(0), new Units(0)));
	}

	/**
	 * This test checks if the GrillWorkbench was created correctly. It asserts that the contents of
	 * the grill are empty after creation.
	 */
	@Test
	public void testGrillWorkbenchCreation() {
		assertTrue(grill.peekContents().isEmpty());
	}

	/**
	 * This test checks if the state of the workbench is set correctly. It forces the state of the
	 * grill to ACTIVE and then sets the contents according to the state. It asserts that the
	 * contents of the grill contain the RAW_PATTY item after these operations.
	 */
	@Test
	public void testForceSetContentsAccordingToState() {
		grill.forceSetState(State.ACTIVE);
		grill.forceSetContentsAccordingToState(Stack.of(Item.RAW_PATTY));
		assertEquals(Item.RAW_PATTY, grill.peekContents().reduceToSingleItem());
	}

	/**
	 * This test checks if the workbench implements the recipe correctly. It asserts that the result
	 * of finding a recipe by items in the grill equals a Stack of GRILLED_PATTY.
	 */
	@Test
	public void testRecipe() {
		assertEquals(Stack.of(Item.GRILLED_PATTY),
			grill.findRecipeByItems(Stack.of(Item.RAW_PATTY)).orElseThrow().result());
	}

	/**
	 * This test checks that the grill accepts the offer and gives the correct ingredient back. It
	 * creates a new Stack of RAW_PATTY as an offer, trades it with the grill, and checks the
	 * result. It asserts that the result is empty, the contents of the grill are not empty and
	 * contain the RAW_PATTY item after the trade. It then forces the state of the grill to FINISHED
	 * and asserts that the contents of the grill equal a Stack of GRILLED_PATTY.
	 */
	@Test
	public void testTrade() {
		Stack offer = Stack.of(Item.RAW_PATTY);
		Stack result = grill.trade(offer);

		assertTrue(result.isEmpty());
		assertFalse(grill.peekContents().isEmpty());
		assertEquals(Item.RAW_PATTY, grill.peekContents().reduceToSingleItem());

		grill.forceSetState(State.FINISHED);

		assertEquals(Stack.of(Item.GRILLED_PATTY), grill.peekContents());
	}

}
