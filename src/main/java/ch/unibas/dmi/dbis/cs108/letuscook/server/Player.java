package ch.unibas.dmi.dbis.cs108.letuscook.server;

import ch.unibas.dmi.dbis.cs108.letuscook.client.Client;
import ch.unibas.dmi.dbis.cs108.letuscook.commands.Command;
import ch.unibas.dmi.dbis.cs108.letuscook.commands.PlayerHoldingCommand;
import ch.unibas.dmi.dbis.cs108.letuscook.commands.PlayerPositionCommand;
import ch.unibas.dmi.dbis.cs108.letuscook.gui.Units;
import ch.unibas.dmi.dbis.cs108.letuscook.orders.Stack;
import ch.unibas.dmi.dbis.cs108.letuscook.util.Coords;
import ch.unibas.dmi.dbis.cs108.letuscook.util.Fonts;
import ch.unibas.dmi.dbis.cs108.letuscook.util.Rect;
import ch.unibas.dmi.dbis.cs108.letuscook.util.Resource;
import java.util.HashMap;
import java.util.Map;
import javafx.geometry.VPos;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;

public class Player {

	/**
	 * The size of a player.
	 */
	public static final Units SIZE = new Units(0.75);

	/**
	 * The spawn position.
	 */
	public static final Coords SPAWN = new Coords(new Units(Game.WIDTH.u() / 2), new Units(4));

	/**
	 * The images used to represent the character.
	 */
	private static Map<Facing, Image[]> characterImages = null;

	/**
	 * The images used to represent the monkey character.
	 */
	private static Map<Facing, Image[]> monkeyImages = null;

	/**
	 * The rect defining the player's size and our opinion of its position. We say "opinion" because
	 * on the client, we interpolate between real position updates to render smoother movement, and
	 * only jump to real positions whenever we receive them from the server.
	 */
	private final Rect rect;

	/**
	 * The player's position. Unlike {@link #rect}, this position is guaranteed to be "real".
	 */
	private final Coords realCoords;

	/**
	 * The player's previous position, before {@link #realCoords}.
	 */
	private final Coords previousCoords;

	/**
	 * The current item.
	 */
	private volatile Stack holding = new Stack();

	private Facing facing = Facing.SOUTH;

	private boolean moving = false;

	/**
	 * Create a player.
	 */
	public Player() {
		this(new Coords(SPAWN.getX(), SPAWN.getY()));
	}

	/**
	 * Create a player, specifying the initial position.
	 */
	public Player(Coords coords) {
		this.realCoords = new Coords(coords.getX(), coords.getY());
		this.previousCoords = new Coords(this.getRealCoords().getX(), this.getRealCoords().getY());
		this.rect = new Rect(this.getRealCoords().getX(), this.getRealCoords().getY(),
			SIZE, SIZE);
	}

	/**
	 * Get the player's rect. If it is crucial for the position to be "real", use
	 * {@link #getRealCoords()} instead.
	 *
	 * @return the rect.
	 */
	public Rect getRect() {
		return this.rect;
	}

	/**
	 * Get the player's real coordinates. When drawing the player, use {@link #getRect()} instead.
	 *
	 * @return the coordinates.
	 */
	public Coords getRealCoords() {
		return this.realCoords;
	}

	/**
	 * @return the previous coordinates, before {@link #realCoords}.
	 */
	public Coords getPreviousCoords() {
		return this.previousCoords;
	}

	/**
	 * @return the stack the player is currently holding.
	 */
	public Stack getHolding() {
		return this.holding;
	}

	/**
	 * Set the stack the player is holding.
	 *
	 * @param holding the stack to hold.
	 */
	public void setHolding(Stack holding) {
		assert holding != null;

		this.holding = holding;
	}

	public void setFacing(Facing facing) {
		assert facing != null;

		this.facing = facing;
	}

	public void setMoving(boolean moving) {
		this.moving = moving;
	}

	/**
	 * Draw this workbench.
	 *
	 * @param ctx the graphics context.
	 */
	public final void draw(GraphicsContext ctx, String name) {
		/*
		 * Initialize resources.
		 */
		final int frameCount = 3;
		if (characterImages == null) {
			characterImages = new HashMap<>();
			monkeyImages = new HashMap<>();
			for (var direction : Facing.values()) {
				var frames = new Image[frameCount];
				var monkeyFrames = new Image[frameCount];

				for (int i = 0; i < frameCount; ++i) {
					frames[i] = new Image(Resource.get("player/" + direction + "_" + i + ".png"),
						this.getRect().getWidth().px(), this.getRect().getHeight().px(), true,
						false);
					monkeyFrames[i] = new Image(
						Resource.get("monkey/ape_" + direction + "_" + i + ".png"),
						this.getRect().getWidth().px(), this.getRect().getHeight().px(), true,
						false);
				}
				characterImages.put(direction, frames);
				monkeyImages.put(direction, monkeyFrames);
			}
		}

		/*
		 * Draw the character.
		 */
		int frame =
			!this.moving ? 0
				: switch ((int) (System.currentTimeMillis() / 100 % (frameCount + 1))) {
					case 1 -> 1;
					case 3 -> 2;
					default -> 0;
				};
		Image image = (name.equals("Monkey") ? monkeyImages : characterImages).get(
			this.facing)[frame];
		if (!name.equals(Client.the().getOwnActor().record().orElseThrow().getNickname())) {
			ctx.setGlobalAlpha(0.5);
		}
		ctx.drawImage(image, this.getRect().getLeft().px(),
			this.getRect().getTop().px(), this.getRect().getWidth().px(), this.getRect().getHeight()
				.px());
		ctx.setGlobalAlpha(1);

		/*
		 * Draw what the player is holding.
		 */
		this.getHolding().draw(ctx, new Coords(this.getRect().getX(), this.getRect().getTop()));

		/*
		 * Draw the name.
		 */
		ctx.setTextAlign(TextAlignment.CENTER);
		ctx.setTextBaseline(VPos.TOP);
		ctx.setFont(Fonts.get(Fonts.GAME_NICKNAME));
		ctx.setFill(Color.BLACK);
		ctx.fillText(name, this.getRect().getX().px(), this.getRect().getBottom().px());
	}

	public Command positionRepresentedAsCommand(Actor actor) {
		return Command.withSubject(actor.getIdentifier(),
			new PlayerPositionCommand(this.getRect().asCoords()));
	}

	public Command holdingRepresentedAsCommand(Actor actor) {
		return Command.withSubject(actor.getIdentifier(),
			new PlayerHoldingCommand(this.getHolding()));
	}

	public Command[] representedAsCommands(Actor actor) {
		return new Command[]{
			this.positionRepresentedAsCommand(actor),
			this.holdingRepresentedAsCommand(actor)
		};
	}

	/**
	 * The direction a player should face.
	 */
	public enum Facing {
		NORTH, NORTHEAST, EAST, SOUTHEAST, SOUTH, SOUTHWEST, WEST, NORTHWEST;

		@Override
		public String toString() {
			return this.name().toLowerCase();
		}
	}
}
