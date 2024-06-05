package ch.unibas.dmi.dbis.cs108.letuscook.orders;

import ch.unibas.dmi.dbis.cs108.letuscook.gui.Units;
import ch.unibas.dmi.dbis.cs108.letuscook.util.Coords;
import ch.unibas.dmi.dbis.cs108.letuscook.util.MalformedException;
import ch.unibas.dmi.dbis.cs108.letuscook.util.Resource;
import java.util.Optional;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

/**
 * Represents various types of items that can be used in orders.
 */
public enum Item {

	BREAD(1),
	CHEESE(1),
	TOMATO(0), TOMATO_SAUCE(1),
	RAW_PATTY(0), GRILLED_PATTY(3), BURNT_PATTY(0),
	RAW_CHICKEN(0), GRILLED_CHICKEN(3), BURNT_CHICKEN(0),
	RAW_CHICKEN_NUGGETS(0), FRIED_CHICKEN_NUGGETS(4), BURNT_CHICKEN_NUGGETS(0),
	POTATO(0), RAW_FRIES(0), FRIES(4), BURNT_FRIES(0),
	DRINK(2),
	SALAD_HEAD(0), CHOPPED_SALAD(2),
	PIZZA_MARGHERITA(5), BURNT_PIZZA_MARGHERITA(0),
	;

	/**
	 * The graphical size of an item.
	 */
	public static final Units SIZE = new Units(Workbench.SIZE.u() / 2);

	/**
	 * The price of the item.
	 */
	public final int price;

	/**
	 * The image to use to represent this item.
	 */
	private Image image = null;

	/**
	 * Constructs a new Item with the specified price.
	 *
	 * @param price The price of the item.
	 */
	Item(final int price) {
		this.price = price;
	}

	/**
	 * Parses a string representation of an Item.
	 *
	 * @param string The string representation of the item.
	 * @return The Item corresponding to the string.
	 * @throws MalformedException if the string does not represent a valid item.
	 */
	public static Item fromString(String string) throws MalformedException {
		try {
			return Item.valueOf(string);
		} catch (IllegalArgumentException e) {
			throw new MalformedException("bad item");
		}
	}

	/**
	 * Parses a string representation of an Item into an Optional.
	 *
	 * @param string The string representation of the item.
	 * @return An Optional containing the Item if the string is valid, otherwise empty.
	 */
	public static Optional<Item> optionalFromString(String string) {
		try {
			return Optional.of(Item.fromString(string));
		} catch (MalformedException e) {
			return Optional.empty();
		}
	}

	/**
	 * @return the image used to represent this item.
	 */
	public Image getImage() {
		if (this.image == null) {
			this.image = new Image(Resource.get("item/" + this.toString().toLowerCase() + ".png"),
				SIZE.px(), SIZE.px(), true, false);
		}

		return this.image;
	}

	/**
	 * Draw this item.
	 *
	 * @param ctx    the graphics context.
	 * @param coords the coordinates.
	 */
	public void draw(GraphicsContext ctx, Coords coords) {
		ctx.drawImage(this.getImage(), coords.getX().px() - SIZE.px() / 2,
			coords.getY().px() - SIZE.px() / 2, SIZE.px(),
			SIZE.px());
	}

	/**
	 * Returns the string representation of the Item.
	 *
	 * @return The name of the Item.
	 */
	@Override
	public final String toString() {
		return this.name();
	}
}
