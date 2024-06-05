package ch.unibas.dmi.dbis.cs108.letuscook.util;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Region;

/**
 * A custom Region that wraps a Canvas, providing access to its GraphicsContext.
 */
public class CanvasPane extends Region {

	private final Canvas canvas;

	/**
	 * Constructs a CanvasPane with the specified width and height.
	 *
	 * @param width  The width of the canvas.
	 * @param height The height of the canvas.
	 */
	public CanvasPane(double width, double height) {
		this.canvas = new Canvas(width, height);
		getChildren().add(canvas);
	}

	/**
	 * Retrieves the GraphicsContext of the underlying Canvas.
	 *
	 * @return The GraphicsContext of the canvas.
	 */
	public GraphicsContext getGraphicsContext2D() {
		return canvas.getGraphicsContext2D();
	}

	/**
	 * Overrides the layoutChildren method to resize the canvas to match the size of the region.
	 */
	@Override
	protected void layoutChildren() {
		super.layoutChildren();
		double width = getWidth();
		canvas.setWidth(width);
		double height = getHeight();
		canvas.setHeight(height);
	}
}
