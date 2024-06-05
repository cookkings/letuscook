package ch.unibas.dmi.dbis.cs108.letuscook.orders;

import ch.unibas.dmi.dbis.cs108.letuscook.server.Game;
import ch.unibas.dmi.dbis.cs108.letuscook.util.Coords;
import ch.unibas.dmi.dbis.cs108.letuscook.util.Identifier;
import ch.unibas.dmi.dbis.cs108.letuscook.util.Images;
import java.util.Optional;
import java.util.Random;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

/**
 * Represents a customer workbench where orders are processed. Extends the abstract class
 * Workbench.
 */
public class CustomerWorkbench extends Workbench {

	/**
	 * The cooldown before a new order can be received.
	 */
	public static final int IDLE_COOLDOWN_SECONDS = 2;

	/**
	 * The cooldown after finishing.
	 */
	public static final int FINISHED_COOLDOWN_SECONDS = 2;

	/**
	 * The cooldown after an order expires.
	 */
	public static final int EXPIRED_COOLDOWN_SECONDS = 3;

	private static final Random random = new Random();

	/**
	 * The current order.
	 */
	private Order order = null;

	private int randomInteger = 0;

	/**
	 * Constructs a new CustomerWorkbench with the specified identifier and coordinates.
	 *
	 * @param identifier the unique identifier of the workbench
	 * @param coords     the coordinates of the workbench
	 */
	public CustomerWorkbench(Identifier identifier, Coords coords) {
		super(identifier, coords);
	}

	/**
	 * Retrieves the order currently being processed at the workbench.
	 *
	 * @return an Optional containing the order, or empty if no order is set
	 */
	public Optional<Order> getOrder() {
		if (this.getState() == State.IDLE) {
			return Optional.empty();
		}

		return Optional.ofNullable(this.order);
	}

	/**
	 * Sets the order to be processed at the workbench, forcefully activating the workbench.
	 *
	 * @param order the order to be processed
	 */
	public void forceSetOrder(Order order) {
		assert order != null || this.getState() == State.IDLE;

		this.order = order;
	}

	/**
	 * @return the contents of this workbench.
	 */
	@Override
	public Stack peekContents() {
		return this.getOrder().isPresent() ? this.getOrder().orElseThrow().stack() : new Stack();
	}

	/**
	 * Forcefully sets the contents of the workbench. Since CustomerWorkbench does not hold any
	 * contents, this method has no effect.
	 *
	 * @param contents the contents to be set (ignored)
	 */
	@Override
	public void forceSetContentsAccordingToState(Stack contents) {
		this.order = contents.isEmpty() ? null : Order.findByStack(contents).orElseThrow();
	}

	public void nextRandomInteger() {
		this.randomInteger = CustomerWorkbench.random.nextInt();
	}

	/**
	 * Processes a trade offer with the given offer stack. If the offer satisfies the current order,
	 * the workbench is marked as finished and an empty stack is returned. Otherwise, the offer
	 * stack is returned unchanged.
	 *
	 * @param offer the stack representing the trade offer
	 * @return an empty stack if the offer satisfies the order, otherwise the unchanged offer stack
	 */
	@Override
	public Stack trade(Stack offer) {
		if (this.getOrder().isPresent() && this.getState() == State.ACTIVE && offer.satisfies(
			order)) {
			this.forceSetState(State.FINISHED);
			this.ticksUntilStateChange.set(FINISHED_COOLDOWN_SECONDS * Game.TPS);

			return new Stack();
		}

		return offer;
	}

	/**
	 * Get the fraction of time remaining until the next state change.
	 *
	 * @return the time fraction.
	 */
	@Override
	public final double getTimeFraction() {
		Order order = this.order;
		if (order == null) {
			return 0;
		}

		double total = Game.TPS * switch (this.getState()) {
			case IDLE, FINISHED, EXPIRED -> 0;
			case ACTIVE -> order.getExpirationTimeSeconds();
		};

		return Math.min(total, Math.max(0.01, 1d * this.ticksUntilStateChange.get() / total));
	}

	/**
	 * Compute the surface images.
	 */
	@Override
	final void computeSurfaceImages() {
	}

	@Override
	final void drawProgress(GraphicsContext ctx) {
		double timeFraction = this.getTimeFraction();

		if (this.getState() == State.ACTIVE) {
			ctx.setFill(
				new Color(1 - timeFraction, 0, 0,
					(1 - timeFraction) * 0.5));
			ctx.fillRect(this.getRect().getLeft().px(), this.getRect().getTop().px(),
				SIZE.px() * timeFraction, SIZE.px());
		}
	}

	@Override
	final void drawContents(GraphicsContext ctx) {
		ctx.setGlobalAlpha(switch (this.getState()) {
			case FINISHED -> 1;
			case EXPIRED -> 0;
			default -> 0.6;
		});

		super.drawContents(ctx);

		ctx.setGlobalAlpha(1);

		if (this.getState() == State.IDLE) {
			return;
		}

		Image face = null;
		if (this.getOrder().isPresent()) {
			face = Images.customer(this.randomInteger, this.getState() == State.EXPIRED);
		}
		ctx.drawImage(face, this.getRect().getX().px() - SIZE.px() / 2,
			this.getRect().getY().px() - SIZE.px() * 1.5, SIZE.px(), SIZE.px());
	}
}
