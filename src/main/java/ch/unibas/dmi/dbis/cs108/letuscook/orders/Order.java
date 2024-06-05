package ch.unibas.dmi.dbis.cs108.letuscook.orders;

import ch.unibas.dmi.dbis.cs108.letuscook.gui.Units;
import ch.unibas.dmi.dbis.cs108.letuscook.util.Coords;
import ch.unibas.dmi.dbis.cs108.letuscook.util.MalformedException;
import ch.unibas.dmi.dbis.cs108.letuscook.util.Resource;
import java.util.Optional;
import java.util.Random;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

/**
 * Represents various types of orders that can be placed.
 */
public enum Order {

	HAMBURGER(
		Stack.of(
			Item.BREAD,
			Item.GRILLED_PATTY
		), 0, 35),
	CHEESEBURGER(
		Stack.of(
			Item.BREAD,
			Item.GRILLED_PATTY,
			Item.CHEESE
		), 0, 40),
	DOUBLE_CHEESEBURGER(
		Stack.of(
			Item.BREAD,
			Item.GRILLED_PATTY,
			Item.CHEESE,
			Item.GRILLED_PATTY,
			Item.CHEESE
		), 2, 45),
	CHICKEN_BURGER(
		Stack.of(
			Item.BREAD,
			Item.GRILLED_CHICKEN
		), 0, 35),
	PIZZA_MARGHERITA(
		Stack.of(
			Item.PIZZA_MARGHERITA
		), 2, 50),
	SALAD(
		Stack.of(
			Item.CHOPPED_SALAD,
			Item.TOMATO
		), 0, 35),
	CHICKEN_NUGGETS(
		Stack.of(
			Item.FRIED_CHICKEN_NUGGETS
		), 0, 40),
	DRINK(
		Stack.of(
			Item.DRINK
		), 0, 20),
	//	SUPER_CHEESY(
//		Stack.of(
//			Item.BREAD,
//			Item.CHEESE,
//			Item.CHEESE,
//			Item.CHEESE,
//			Item.CHEESE,
//			Item.CHEESE,
//			Item.CHEESE
//		), 2, 40),
	FRIES(
		Stack.of(
			Item.FRIES
		), 0, 30);

	/**
	 * The graphical size of an order.
	 */
	public static final Units SIZE = Item.SIZE;

	/**
	 * A Random object for random order selection.
	 */
	private static final Random RANDOMIZER = new Random();

	/**
	 * The list of items included in the order.
	 */
	private final Stack stack;

	/**
	 * The markup price for the order.
	 */
	private final int markup;

	/**
	 * The expiration time for the order, in seconds.
	 */
	private final int expirationTimeSeconds;

	/**
	 * The image to use to represent this order.
	 */
	private Image image = null;

	/**
	 * Constructs a new Order with the specified items, markup, and expiration time.
	 *
	 * @param stack                 The list of items included in the order.
	 * @param markup                The markup price for the order.
	 * @param expirationTimeSeconds The expiration time for the order, in seconds.
	 */
	Order(final Stack stack, final int markup, final int expirationTimeSeconds) {
		this.stack = stack;
		this.markup = markup;
		this.expirationTimeSeconds = expirationTimeSeconds;
	}

	/**
	 * Parses a string representation of an Order.
	 *
	 * @param string The string representation of the order.
	 * @return The Order corresponding to the string.
	 * @throws MalformedException if the string does not represent a valid order.
	 */
	public static Order fromString(String string) throws MalformedException {
		try {
			return Order.valueOf(string);
		} catch (IllegalArgumentException e) {
			throw new MalformedException("bad order");
		}
	}

	public static Optional<Order> findByStack(Stack stack) {
		for (var order : Order.values()) {
			if (stack.satisfies(order)) {
				return Optional.of(order);
			}
		}

		return Optional.empty();
	}

	/**
	 * Picks a random Order.
	 *
	 * @return A randomly selected Order.
	 */
	public static Order pickRandom() {
		return Order.values()[RANDOMIZER.nextInt(Order.values().length)];
	}

	public Stack stack() {
		return this.stack.copy();
	}

	/**
	 * Calculates the price of the order.
	 *
	 * @return The total price of the order.
	 */
	public int getPrice() {
		int price = this.markup;
		for (var item : this.stack.toArray()) {
			price += item.price;
		}
		return price;
	}

	/**
	 * Retrieves the expiration time of the order.
	 *
	 * @return The expiration time of the order, in seconds.
	 */
	public int getExpirationTimeSeconds() {
		return this.expirationTimeSeconds;
	}

	/**
	 * Draw this order.
	 *
	 * @param ctx    the graphics context.
	 * @param coords the coordinates.
	 */
	public void draw(GraphicsContext ctx, Coords coords) {
		if (this.image == null) {
			if (this.stack.toArray().length == 1) {
				this.image = this.stack.toArray()[0].getImage();
			} else {
				this.image = new Image(
					Resource.get("order/" + this.toString().toLowerCase() + ".png"), SIZE.px(),
					SIZE.px(), true, false);
			}
		}
		ctx.drawImage(this.image, coords.getX().px() - SIZE.px() / 2,
			coords.getY().px() - SIZE.px() / 2, SIZE.px(), SIZE.px());
	}

	/**
	 * Returns the string representation of the Order.
	 *
	 * @return The name of the Order.
	 */
	@Override
	public final String toString() {
		return this.name();
	}

	public Image getImage() {
		if (this.image == null) {
			this.image = new Image(Resource.get("order/" + this.toString().toLowerCase() + ".png"),
				SIZE.px(), SIZE.px(), true, false);
		}

		return this.image;
	}
}
