package ch.unibas.dmi.dbis.cs108.letuscook.gui;

/**
 * Represents units with a scale factor for conversions.
 */
public record Units(double u) {

	private static final double PRECISION = 1e5;

	private static double scale = 0;

	public Units(double u) {
		this.u = Math.round(PRECISION * u) / PRECISION;
	}

	/**
	 * Retrieves the current scale factor.
	 *
	 * @return The scale factor.
	 */
	private static double scale() {
		return Units.scale;
	}

	/**
	 * Sets the scale factor for conversions.
	 *
	 * @param scale The scale factor.
	 */
	public static void scale(final double scale) {
		Units.scale = scale;
	}

	/**
	 * Converts the units to pixels using the current scale factor.
	 *
	 * @return The converted units in pixels.
	 */
	public double px() {
		return this.u() * Units.scale();
	}

	@Override
	public String toString() {
		return String.valueOf(this.u);
	}
}
