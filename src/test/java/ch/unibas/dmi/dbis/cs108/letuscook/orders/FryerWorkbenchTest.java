package ch.unibas.dmi.dbis.cs108.letuscook.orders;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ch.unibas.dmi.dbis.cs108.letuscook.gui.Units;
import ch.unibas.dmi.dbis.cs108.letuscook.util.Coords;
import ch.unibas.dmi.dbis.cs108.letuscook.util.IdentifierFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class FryerWorkbenchTest {

	private FryerWorkbench fryer;

	/**
	 * Sets up all required prerequisites for the test units.
	 */
	@BeforeEach
	public void setUp() {
		fryer = new FryerWorkbench(new IdentifierFactory().next(),
			new Coords(new Units(0), new Units(0)));
	}

	/**
	 * This test checks if the FryerWorkbench was created correctly. It asserts that the contents of
	 * the fryer are empty after creation.
	 */
	@Test
	public void testGrillWorkbenchCreation() {
		assertTrue(fryer.peekContents().isEmpty());
	}

	/**
	 * This test checks if the state of the workbench is set correctly. It forces the state of the
	 * fryer to ACTIVE and then sets the contents according to the state. It asserts that the
	 * contents of the fryer contain the FROZEN_FRIES item after these operations.
	 */
	@Test
	public void testForceSetContentsAccordingToState() {
		fryer.forceSetState(State.ACTIVE);
		fryer.forceSetContentsAccordingToState(Stack.of(Item.RAW_FRIES));
		assertEquals(Item.RAW_FRIES, fryer.peekContents().reduceToSingleItem());
	}

	/**
	 * This test checks if the workbench implements the recipe correctly. It asserts that the result
	 * of finding a recipe by items in the fryer equals a Stack of FRIES.
	 */
	@Test
	public void testRecipe() {
		assertEquals(Stack.of(Item.FRIES),
			fryer.findRecipeByItems(Stack.of(Item.RAW_FRIES)).orElseThrow().result());
	}

	/**
	 * This test checks that the fryer accepts the offer and gives the correct ingredient back. It
	 * creates a new Stack of FROZEN_FRIES as an offer, trades it with the fryer, and checks the
	 * result. It asserts that the result is empty, the contents of the fryer are not empty and
	 * contain the FROZEN_FRIES item after the trade. It then forces the state of the fryer to
	 * FINISHED and asserts that the contents of the fryer equal a Stack of FRIES.
	 */
	@Test
	public void testTrade() {
		Stack offer = Stack.of(Item.RAW_FRIES);
		Stack result = fryer.trade(offer);

		assertTrue(result.isEmpty()); // This assumes that the fryer accepts the offer.
		assertFalse(fryer.peekContents().isEmpty());
		assertEquals(Item.RAW_FRIES, fryer.peekContents().reduceToSingleItem());

		fryer.forceSetState(State.FINISHED);

		assertEquals(Stack.of(Item.FRIES), fryer.peekContents());
	}

}
