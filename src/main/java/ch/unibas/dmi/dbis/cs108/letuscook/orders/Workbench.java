package ch.unibas.dmi.dbis.cs108.letuscook.orders;

import ch.unibas.dmi.dbis.cs108.letuscook.client.Client;
import ch.unibas.dmi.dbis.cs108.letuscook.commands.Command;
import ch.unibas.dmi.dbis.cs108.letuscook.commands.GameUpdateWorkbenchCommand;
import ch.unibas.dmi.dbis.cs108.letuscook.gui.Units;
import ch.unibas.dmi.dbis.cs108.letuscook.util.Coords;
import ch.unibas.dmi.dbis.cs108.letuscook.util.Fonts;
import ch.unibas.dmi.dbis.cs108.letuscook.util.Identifier;
import ch.unibas.dmi.dbis.cs108.letuscook.util.Rect;
import ch.unibas.dmi.dbis.cs108.letuscook.util.Vector;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import javafx.geometry.VPos;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;

public abstract class Workbench {

	/**
	 * The generic workbench width and height.
	 */
	public static final Units SIZE = new Units(1);

	/**
	 * Ticks until state change.
	 */
	public final AtomicInteger ticksUntilStateChange = new AtomicInteger(0);

	/**
	 * The images to use to represent this workbench.
	 */
	final Map<State, Image> surfaceImages = new HashMap<>();

	/**
	 * The identifier.
	 */
	private final Identifier identifier;

	/**
	 * The rect.
	 */
	private final Rect rect;

	/**
	 * The game score when this workbench was last updated.
	 */
	int gameScoreAtLastUpdate = 0;

	/**
	 * The state.
	 */
	private volatile State state = State.IDLE;

	/**
	 * Create a workbench.
	 */
	public Workbench(Identifier identifier, Coords coords) {
		this.identifier = identifier;
		this.rect = new Rect(coords.getX(), coords.getY(), SIZE, SIZE);
	}

	/**
	 * @return the workbench identifier.
	 */
	public final Identifier getIdentifier() {
		return this.identifier;
	}

	/**
	 * @return the workbench rect.
	 */
	public final Rect getRect() {
		return this.rect;
	}

	/**
	 * @return the workbench state.
	 */
	public final State getState() {
		return this.state;
	}

	/**
	 * Forcefully set the workbench state.
	 *
	 * @param state the state.
	 */
	public final void forceSetState(State state) {
		this.state = state;
	}

	/**
	 * @return the (theoretical) workbench contents.
	 */
	public abstract Stack peekContents();

	/**
	 * Set the score at which this workbench was last updated.
	 *
	 * @param score the score.
	 */
	public final void setGameScoreAtLastUpdate(int score) {
		this.gameScoreAtLastUpdate = score;
	}

	/**
	 * Forcefully set the (theoretical) workbench contents.
	 *
	 * @param contents the content.
	 */
	public abstract void forceSetContentsAccordingToState(Stack contents);

	/**
	 * Trade a stack.
	 *
	 * @param offer the offer.
	 * @return the result of the trade.
	 */
	public abstract Stack trade(Stack offer);

	/**
	 * Get the fraction of time remaining until the next state change.
	 *
	 * @return the time fraction.
	 */
	public double getTimeFraction() {
		return 0;
	}

	/**
	 * Compute the surface images.
	 */
	abstract void computeSurfaceImages();

	/**
	 * Draw the progress.
	 */
	void drawProgress(GraphicsContext ctx) {
	}

	/**
	 * Draw the contents.
	 *
	 * @param ctx the graphics context.
	 */
	void drawContents(GraphicsContext ctx) {
		this.peekContents().draw(ctx,
			this.getRect().asCoords().displaced(new Vector(new Units(0), new Units(-0.25))));
	}

	/**
	 * Draw this workbench.
	 *
	 * @param ctx the graphics context.
	 */
	public final void draw(GraphicsContext ctx) {
		this.computeSurfaceImages();

		/*
		 * Interaction radius.
		 */
		if (Client.the().isDebugMode()) {
			ctx.setStroke(Color.ORANGE);

			var radius = rect.getInteractionRadius().px();
			ctx.setLineWidth(new Units(0.02).px());

			ctx.strokeOval(rect.getX().px() - radius,
				rect.getY().px() - radius, 2 * radius, 2 * radius);
		}

		/*
		 * Surface.
		 */
		Image surface = surfaceImages.getOrDefault(this.getState(), surfaceImages.get(null));
		ctx.drawImage(surface, rect.getLeft().px(), rect.getTop().px(), SIZE.px(), SIZE.px());

		/*
		 * Contents.
		 */
		this.drawContents(ctx);

		/*
		 * Progress.
		 */
		this.drawProgress(ctx);

		if (Client.the().isDebugMode()) {

			/*
			 * Debugging information.
			 */
			ctx.setTextBaseline(VPos.BOTTOM);
			ctx.setTextAlign(TextAlignment.LEFT);
			ctx.setFont(Fonts.get(Fonts.GAME_DEBUG));
			ctx.setFill(Color.RED);
			ctx.fillText(this.getIdentifier() + " " + this.getState() + ":"
				+ this.ticksUntilStateChange.get(), rect.getLeft().px(), rect.getBottom().px());
		}
	}

	public Command[] representedAsCommands() {
		return new Command[]{new GameUpdateWorkbenchCommand(this.getIdentifier(), this.getState(),
			this.peekContents(), this.ticksUntilStateChange.get())};
	}
}
