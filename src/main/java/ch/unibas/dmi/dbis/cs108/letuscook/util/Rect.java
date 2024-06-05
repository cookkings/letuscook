package ch.unibas.dmi.dbis.cs108.letuscook.util;

import ch.unibas.dmi.dbis.cs108.letuscook.gui.Units;

/**
 * Coordinates and dimensions.
 */
public class Rect extends Coords {

	/**
	 * A margin on top of the interaction radius.
	 */
	private static final Units INTERACTION_RADIUS_MARGIN = new Units(0.1);

	/**
	 * The width.
	 */
	private final Units width;

	/**
	 * The height.
	 */
	private final Units height;

	/**
	 * Create a rect with the given coordinates and size.
	 *
	 * @param x      the horizontal component of the coordinates.
	 * @param y      the vertical component of the coordinates.
	 * @param width  the width.
	 * @param height the height.
	 */
	public Rect(final Units x, final Units y, final Units width, final Units height) {
		super(x, y);

		assert width.u() >= 0 && height.u() >= 0 : "negative size(s)";

		this.width = width;
		this.height = height;
	}

	/**
	 * @return a copy of the underlying coords.
	 */
	public Coords asCoords() {
		return new Coords(this.getX(), this.getY());
	}

	/**
	 * @return the horizontal component in the top left corner.
	 */
	public Units getLeft() {
		return new Units(this.getX().u() - this.width.u() / 2);
	}

	/**
	 * Set the horizontal coordinates relative to the left border.
	 *
	 * @param x the horizontal component relative to the left border.
	 */
	public void setLeft(final Units x) {
		this.setX(new Units(x.u() + this.width.u() / 2));
	}

	/**
	 * @return the vertical component in the top left corner.
	 */
	public Units getTop() {
		return new Units(this.getY().u() - this.height.u() / 2);
	}

	/**
	 * Set the vertical coordinates relative to the top border.
	 *
	 * @param y the vertical component relative to the top border.
	 */
	public void setTop(final Units y) {
		this.setY(new Units(y.u() + this.height.u() / 2));
	}

	/**
	 * @return the horizontal component in the bottom right corner.
	 */
	public Units getRight() {
		return new Units(this.getLeft().u() + this.width.u());
	}

	/**
	 * Set the horizontal coordinates relative to the right border.
	 *
	 * @param x the horizontal component relative to the right border.
	 */
	public void setRight(final Units x) {
		this.setX(new Units(x.u() - this.width.u() / 2));
	}

	/**
	 * @return the vertical component in the bottom right corner.
	 */
	public Units getBottom() {
		return new Units(this.getTop().u() + this.height.u());
	}

	/**
	 * Set the vertical coordinates relative to the bottom border.
	 *
	 * @param y the vertical component relative to the bottom border.
	 */
	public void setBottom(final Units y) {
		this.setY(new Units(y.u() - this.height.u() / 2));
	}

	/**
	 * @return the width.
	 */
	public Units getWidth() {
		return this.width;
	}

	/**
	 * @return the height.
	 */
	public Units getHeight() {
		return this.height;
	}

	/**
	 * @return these coordinates' interaction radius.
	 */
	public Units getInteractionRadius() {
		return new Units(Math.sqrt(Math.pow(this.width.u(), 2) + Math.pow(this.height.u(), 2)) / 2
			+ INTERACTION_RADIUS_MARGIN.u());
	}

	/**
	 * Check whether the subject is within this rect's interaction radius.
	 *
	 * @param subject the subject.
	 * @return whether the subject is within these coordinates' interaction radius.
	 */
	public boolean isInInteractionRadius(Rect subject) {
		double xDistance = Math.abs(this.getX().u() - subject.getX().u());
		double yDistance = Math.abs(this.getY().u() - subject.getY().u());

		if ((xDistance > subject.getWidth().u() / 2 + this.getInteractionRadius().u()) || (yDistance
			> subject.getHeight().u() / 2 + this.getInteractionRadius().u())) {
			return false;
		}

		if (xDistance <= subject.getWidth().u() / 2 || yDistance <= subject.getHeight().u() / 2) {
			return true;
		}

		double cornerDistanceSquared =
			Math.pow(xDistance - subject.getWidth().u() / 2, 2) + Math.pow(
				yDistance - subject.getHeight()
					.u() / 2, 2);

		return cornerDistanceSquared <= Math.pow(this.getInteractionRadius().u(), 2);
	}

	/**
	 * Check whether these coordinates collide with the given object.
	 *
	 * @param object the object.
	 * @return whether these coordinates collide with the given object.
	 */
	public boolean isWithin(Rect object) {
		return this.getRight().u() > object.getLeft().u()
			&& this.getBottom().u() > object.getTop().u()
			&& this.getLeft().u() < object.getRight().u()
			&& this.getTop().u() < object.getBottom().u();
	}

	/**
	 * @return a copy of this rect.
	 */
	public Rect copy() {
		return new Rect(this.getX(), this.getY(), this.getWidth(), this.getHeight());
	}

	/**
	 * @return a textual representation of this rect.
	 */
	@Override
	public String toString() {
		return super.toString() + "," + this.width + "," + this.height;
	}
}
