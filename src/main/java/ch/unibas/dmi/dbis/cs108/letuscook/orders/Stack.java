package ch.unibas.dmi.dbis.cs108.letuscook.orders;

import ch.unibas.dmi.dbis.cs108.letuscook.gui.Units;
import ch.unibas.dmi.dbis.cs108.letuscook.util.Coords;
import ch.unibas.dmi.dbis.cs108.letuscook.util.MalformedException;
import ch.unibas.dmi.dbis.cs108.letuscook.util.Vector;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import javafx.scene.canvas.GraphicsContext;

public class Stack {

	/**
	 * The stack's contents.
	 */
	private final List<Item> contents = Collections.synchronizedList(new ArrayList<>());

	/**
	 * Create a stack.
	 */
	public Stack() {
	}

	/**
	 * Create a stack from a listing of items.
	 *
	 * @param items the items.
	 * @return the stack.
	 */
	public static Stack of(Item... items) {
		Stack stack = new Stack();

		for (var item : items) {
			assert item != null;

			stack.contents.add(item);
		}

		return stack;
	}

	/**
	 * Parse a stack from a string.
	 *
	 * @param string the string.
	 * @return the stack.
	 * @throws MalformedException if the stack is malformed.
	 */
	public static Stack fromString(String string) throws MalformedException {
		if (string == null) {
			throw new MalformedException("string is null");
		}

		Stack stack = new Stack();

		String[] itemStrings = string.split(",");

		for (String itemString : itemStrings) {
			stack.contents.add(Item.fromString(itemString));
		}

		return stack;
	}

	/**
	 * Push the items of a stack onto the stack.
	 *
	 * @param stack the stack.
	 */
	public synchronized void push(final Stack stack) {
		this.contents.addAll(stack.contents);
	}

	/**
	 * @return whether this stack is empty.
	 */
	public boolean isEmpty() {
		return this.contents.isEmpty();
	}

	/**
	 * @return the single item contained in the stack.
	 */
	public Item reduceToSingleItem() {
		assert this.contents.size() == 1;

		return this.contents.get(0);
	}

	/**
	 * @return a sorted list of the items.
	 */
	ArrayList<Item> toSorted() {
		var sorted = new ArrayList<>(this.contents);
		sorted.sort(Comparator.comparingInt(Enum::ordinal));

		return sorted;
	}

	/**
	 * @return an array of the items.
	 */
	public Item[] toArray() {
		return this.contents.toArray(new Item[0]);
	}

	/**
	 * @return a copy of this stack.
	 */
	public Stack copy() {
		Stack copy = new Stack();

		copy.push(this);

		return copy;
	}

	/**
	 * Check whether this stack satisfies an order.
	 *
	 * @param order the order.
	 * @return whether this stack satisfies the order.
	 */
	public boolean satisfies(Order order) {
		return this.equals(order.stack());
	}

	/**
	 * Draw this stack.
	 *
	 * @param ctx    the graphics context.
	 * @param coords the coordinates.
	 */
	public void draw(GraphicsContext ctx, Coords coords) {
		for (var order : Order.values()) {
			if (this.satisfies(order)) {
				order.draw(ctx, coords);
				return;
			}
		}

		final Vector offset = new Vector(new Units(0), new Units(Item.SIZE.u() / -4));

		for (var item : this.toArray()) {
			item.draw(ctx, coords);

			coords.displace(offset);
		}
	}

	/**
	 * @return a textual representation of this stack.
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		if (this.isEmpty()) {
			sb.append(",");
			return sb.toString();
		}

		for (Item item : this.contents) {
			sb.append(item);
			sb.append(",");
		}

		sb.deleteCharAt(sb.length() - 1);

		return sb.toString();
	}

	/**
	 * Check whether another object is equal to this stack.
	 *
	 * @param object the object.
	 * @return whether the object is equal to this stack.
	 */
	@Override
	public boolean equals(final Object object) {
		if (!(object instanceof Stack that) || this.contents.size() != that.contents.size()) {
			return false;
		}

		var thisSorted = this.toSorted();
		var thatSorted = that.toSorted();

		for (int i = 0; i < thisSorted.size(); ++i) {
			if (!thisSorted.get(i).equals(thatSorted.get(i))) {
				return false;
			}
		}

		return true;
	}

	/**
	 * @return a cross-process hash.
	 */
	@Override
	public int hashCode() {
		var itemOrdinals = this.toSorted().stream()
			.map(Enum::ordinal)
			.collect(Collectors.toList());

		return Objects.hash(itemOrdinals);
	}
}
