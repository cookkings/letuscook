package ch.unibas.dmi.dbis.cs108.letuscook.gui;

import ch.unibas.dmi.dbis.cs108.letuscook.client.Client;
import ch.unibas.dmi.dbis.cs108.letuscook.orders.Workbench;
import ch.unibas.dmi.dbis.cs108.letuscook.server.Actor;
import ch.unibas.dmi.dbis.cs108.letuscook.server.Game;
import ch.unibas.dmi.dbis.cs108.letuscook.server.Player;
import ch.unibas.dmi.dbis.cs108.letuscook.server.Player.Facing;
import ch.unibas.dmi.dbis.cs108.letuscook.util.CanvasPane;
import ch.unibas.dmi.dbis.cs108.letuscook.util.Coords;
import ch.unibas.dmi.dbis.cs108.letuscook.util.Fonts;
import ch.unibas.dmi.dbis.cs108.letuscook.util.Images;
import ch.unibas.dmi.dbis.cs108.letuscook.util.Vector;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;

/**
 * The `KitchenView` class represents the graphical user interface (GUI) for the main game screen in
 * the Let Us Cook game. It handles the rendering of various game elements including the kitchen
 * floor, workbenches, orders, players, and game timer.
 */
public abstract class KitchenView extends View<BorderPane> {

	/**
	 * Key states.
	 */
	public final Map<KeyCode, Boolean> keys = new ConcurrentHashMap<>();

	/**
	 * The graphics context.
	 */
	private final GraphicsContext gc;

	/**
	 * The smallest movement speed allowed when interpolating player movement (experimental).
	 */
	int interpolMinSpeed = 5;

	/**
	 * The game.
	 */
	private Game game;

	/**
	 * Constructs a new `KitchenView` object with the specified stage.
	 *
	 * @param stage The stage for the game view
	 */
	KitchenView(Stage stage, Views view, Images returnButtonIcon, Runnable returnButtonAction,
		String returnButtonLabel) {
		super(
			stage,
			view,
			new BorderPane(),
			800d, 600d,
			"Let Us Cook!",
			Images.get(Images.BACKGROUND),
			returnButtonIcon,
			returnButtonAction,
			returnButtonLabel
		);

		this.game =
			Client.the().getOwnActor().member().isPresent() ? Client.the().getOwnActor().member()
				.orElseThrow().getLobby().game()
				.orElseThrow() : null;

		CanvasPane canvas = new CanvasPane(Game.WIDTH.px(), Game.HEIGHT.px());
		this.gc = canvas.getGraphicsContext2D();
		HBox canvasBox = new HBox();
		canvasBox.setAlignment(Pos.CENTER);
		canvasBox.setPrefWidth(this.getCenterWidth());
		canvasBox.getChildren().add(canvas);
		this.pane.setCenter(canvasBox);

		this.scene.setOnKeyPressed(event -> this.keys.put(event.getCode(), true));

		var superOnKeyReleased = this.scene.getOnKeyReleased();
		this.scene.setOnKeyReleased(event -> {
			this.keys.put(event.getCode(), false);

			switch (event.getCode()) {
				case F3 -> Client.the().setDebugMode(!Client.the().isDebugMode());
				case F5 -> this.interpolMinSpeed = (this.interpolMinSpeed + 1) % 11;
			}

			superOnKeyReleased.handle(event);
		});
	}

	public Game getGame() {
		return this.game;
	}

	public void setGame(Game game) {
		this.game = game;
	}

	private void drawFloor() {
		gc.drawImage(Images.get(Images.FLOOR), 0, 0, Game.WIDTH.px(), Game.HEIGHT.px());

		if (Client.the().isDebugMode()) {
			this.gc.setFill(Color.GREEN);
			this.gc.setGlobalAlpha(0.1);
			this.gc.fillRect(Game.PLAYABLE_AREA.getLeft().px(), Game.PLAYABLE_AREA.getTop().px(),
				Game.PLAYABLE_AREA.getWidth().px(),
				Game.PLAYABLE_AREA.getHeight().px());
			this.gc.setGlobalAlpha(1);
			this.gc.setStroke(Color.GREEN);
			this.gc.strokeRect(Game.PLAYABLE_AREA.getLeft().px(), Game.PLAYABLE_AREA.getTop().px(),
				Game.PLAYABLE_AREA.getWidth().px(),
				Game.PLAYABLE_AREA.getHeight().px());
		}
	}

	private void drawHeader() {
		this.gc.setTextBaseline(VPos.CENTER);

		/*
		 * Mode.
		 */
		if (Client.the().getOwnActor().member().isPresent() && Client.the().getOwnActor().member()
			.orElseThrow().player().isEmpty()) {
			this.gc.setTextAlign(TextAlignment.LEFT);
			this.gc.setFont(Fonts.get(Fonts.GAME_SUBTITLE));
			this.gc.setFill(Color.DARKGRAY);
			this.gc.fillText("Spectating", Workbench.SIZE.px(), Workbench.SIZE.px() / 1.75);
		}

		/*
		 * Time remaining.
		 */
		int ticks = this.game.ticksUntilGameOver.get();
		int minutes = Math.max(0, ticks / Game.TPS / 60);
		int seconds = Math.max(0, ticks / Game.TPS % 60);

		this.gc.setTextAlign(TextAlignment.CENTER);
		this.gc.setFont(Fonts.get(Fonts.GAME_TITLE));
		this.gc.setFill(Color.SILVER);
		this.gc.fillText(
			Client.the().isDebugMode() ? String.valueOf(ticks)
				: String.format("%02d:%02d", minutes, seconds),
			Game.WIDTH.px() / 2, Workbench.SIZE.px() / 2.25);

		/*
		 * Score.
		 */
		this.gc.setTextAlign(TextAlignment.RIGHT);
		this.gc.setFont(Fonts.get(Fonts.GAME_TITLE));
		this.gc.setFill(Color.GREEN);
		this.gc.fillText(String.valueOf(this.game.getScore()),
			Game.WIDTH.px() - Workbench.SIZE.px(),
			Workbench.SIZE.px() / 2.25);
	}

	/**
	 * Renders the game view, including the kitchen floor, workbenches, orders, players, and game
	 * timer.
	 */
	@Override
	void draw() {
		drawFloor();

		drawHeader();

		/*
		 * Draw workbenches.
		 */
		for (Workbench workbench : this.game.workbenches) {
			workbench.draw(this.gc);
		}

		/*
		 * Draw players.
		 */
		for (Actor actor : this.game.getLobby().getActors()) {
			if (this.game.getTutorialPlayer() == null
				&& actor.member().orElseThrow().player().isEmpty()) {
				continue;
			}

			Player player;
			if (this.game.getTutorialPlayer() != null) {
				player = this.game.getTutorialPlayer();
			} else {
				player = actor.member().orElseThrow().player().orElseThrow();
			}

			/*
			 * Interpolate movement.
			 */
			if (!actor.getIdentifier().equals(Client.the().getOwnIdentifier())) {
				final double framesPerPositionTick = 1d * ClientGUI.FPS / Game.POSITION_TPS;
				final double framesPerTick = 1d * ClientGUI.FPS / Game.TPS;
				final double unitsPerFrame = Game.UNITS_PER_TICK.u() / framesPerTick;
				final double unitsPerPositionTick = unitsPerFrame * framesPerPositionTick;
				final double unitsMoved = Coords.distance(player.getPreviousCoords(),
					player.getRealCoords()).u();
				final double speedMultiplier = Math.max(interpolMinSpeed / 10d,
					unitsMoved / unitsPerPositionTick);
				final double unitsAbleToMove = speedMultiplier * unitsPerFrame;

				final Vector displacement = Vector.between(player.getRect(),
						player.getRealCoords())
					.withMagnitude(new Units(unitsAbleToMove));

				if (unitsAbleToMove >= Coords.distance(player.getRect(),
						player.getRealCoords())
					.u()) {
					player.getRect().setX(player.getRealCoords().getX());
					player.getRect().setY(player.getRealCoords().getY());
				} else {
					player.getRect().displace(displacement);
				}

				/*
				 * Draw displacement.
				 */
				if (Client.the().isDebugMode()) {
					this.gc.setStroke(Color.RED);
					this.gc.setLineWidth(new Units(0.05).px());
					this.gc.strokeLine(player.getRect().getX().px(), player.getRect().getY().px(),
						player.getRect().getX().px() + displacement.getX().px() * 10,
						player.getRect().getY().px() + displacement.getY().px() * 10);
				}
			}

			/*
			 * Determine which direction the player is facing.
			 */
			double vx, vy;
			if (actor.getIdentifier().equals(Client.the().getOwnIdentifier())) {
				vx =
					(keys.getOrDefault(KeyCode.D, false) ? 1 : 0) - (
						keys.getOrDefault(KeyCode.A, false)
							? 1 : 0);
				vy =
					(keys.getOrDefault(KeyCode.S, false) ? 1 : 0) - (
						keys.getOrDefault(KeyCode.W, false)
							? 1 : 0);
			} else {
				var displacement = Vector.between(player.getPreviousCoords(),
					player.getRealCoords());
				vx = displacement.getX().u();
				vy = displacement.getY().u();
			}

			Facing facing = null;
			if (vy < 0 && vx == 0) {
				facing = Facing.NORTH;
			}
			if (vy < 0 && vx > 0) {
				facing = Facing.NORTHEAST;
			}
			if (vy == 0 && vx > 0) {
				facing = Facing.EAST;
			}
			if (vy > 0 && vx > 0) {
				facing = Facing.SOUTHEAST;
			}
			if (vy > 0 && vx == 0) {
				facing = Facing.SOUTH;
			}
			if (vy > 0 && vx < 0) {
				facing = Facing.SOUTHWEST;
			}
			if (vy == 0 && vx < 0) {
				facing = Facing.WEST;
			}
			if (vy < 0 && vx < 0) {
				facing = Facing.NORTHWEST;
			}

			if (facing != null) {
				player.setFacing(facing);
				player.setMoving(true);
			} else {
				player.setMoving(false);
			}

			player.draw(this.gc, actor.record().orElseThrow().getNickname());
		}

		if (Client.the().isDebugMode()) {
			/*
			 * Draw barriers.
			 */
			for (var barrier : this.game.barriers) {
				this.gc.setFill(Color.RED);
				this.gc.setGlobalAlpha(0.1);
				this.gc.fillRect(barrier.getLeft().px(), barrier.getTop().px(),
					barrier.getWidth().px(),
					barrier.getHeight().px());
				this.gc.setGlobalAlpha(1);
				this.gc.setStroke(Color.RED);
				this.gc.strokeRect(barrier.getLeft().px(), barrier.getTop().px(),
					barrier.getWidth().px(),
					barrier.getHeight().px());
			}

			/*
			 * Draw experimental options.
			 */
			this.gc.setFont(Fonts.get(Fonts.GAME_DEBUG));
			this.gc.setFill(Color.BLACK);
			this.gc.setTextAlign(TextAlignment.LEFT);
			this.gc.setTextBaseline(VPos.TOP);
			this.gc.fillText(
				"[F5] Interpol. Min. Speed: " + this.interpolMinSpeed / 10d, 0, 30);
		}
	}
}
