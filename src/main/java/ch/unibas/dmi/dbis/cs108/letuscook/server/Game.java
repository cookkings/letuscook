package ch.unibas.dmi.dbis.cs108.letuscook.server;

import ch.unibas.dmi.dbis.cs108.letuscook.client.Client;
import ch.unibas.dmi.dbis.cs108.letuscook.commands.Command;
import ch.unibas.dmi.dbis.cs108.letuscook.commands.GameScoreCommand;
import ch.unibas.dmi.dbis.cs108.letuscook.commands.GameTimeCommand;
import ch.unibas.dmi.dbis.cs108.letuscook.commands.GameUpdateWorkbenchCommand;
import ch.unibas.dmi.dbis.cs108.letuscook.commands.PlayerHoldingCommand;
import ch.unibas.dmi.dbis.cs108.letuscook.gui.ClientGUI;
import ch.unibas.dmi.dbis.cs108.letuscook.gui.KitchenView;
import ch.unibas.dmi.dbis.cs108.letuscook.gui.Units;
import ch.unibas.dmi.dbis.cs108.letuscook.orders.BinWorkbench;
import ch.unibas.dmi.dbis.cs108.letuscook.orders.ChoppingWorkbench;
import ch.unibas.dmi.dbis.cs108.letuscook.orders.CustomerWorkbench;
import ch.unibas.dmi.dbis.cs108.letuscook.orders.FryerWorkbench;
import ch.unibas.dmi.dbis.cs108.letuscook.orders.GrillWorkbench;
import ch.unibas.dmi.dbis.cs108.letuscook.orders.Item;
import ch.unibas.dmi.dbis.cs108.letuscook.orders.ItemWorkbench;
import ch.unibas.dmi.dbis.cs108.letuscook.orders.Order;
import ch.unibas.dmi.dbis.cs108.letuscook.orders.OvenWorkbench;
import ch.unibas.dmi.dbis.cs108.letuscook.orders.PlateWorkbench;
import ch.unibas.dmi.dbis.cs108.letuscook.orders.State;
import ch.unibas.dmi.dbis.cs108.letuscook.orders.TransformerWorkbench;
import ch.unibas.dmi.dbis.cs108.letuscook.orders.Workbench;
import ch.unibas.dmi.dbis.cs108.letuscook.util.Coords;
import ch.unibas.dmi.dbis.cs108.letuscook.util.Identifier;
import ch.unibas.dmi.dbis.cs108.letuscook.util.IdentifierFactory;
import ch.unibas.dmi.dbis.cs108.letuscook.util.MalformedException;
import ch.unibas.dmi.dbis.cs108.letuscook.util.Messenger;
import ch.unibas.dmi.dbis.cs108.letuscook.util.Rect;
import ch.unibas.dmi.dbis.cs108.letuscook.util.Schedule;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import javafx.scene.input.KeyCode;

/**
 * The game state.
 */
public class Game {

	/**
	 * The height of the game field.
	 */
	public static final Units HEIGHT = new Units(10);

	/*
	 * The width of the game field.
	 */
	public static final Units WIDTH = new Units(11);

	public static final Rect PLAYABLE_AREA = Game.slots(2, 3, Game.WIDTH.u() - 2,
		Game.HEIGHT.u() - 3);

	/**
	 * Ticks per second.
	 */
	public static final int TPS = 30;

	/**
	 * Position ticks per second.
	 */
	public static final int POSITION_TPS = 3;

	/**
	 * Workbench ticks per second.
	 */
	public static final int WORKBENCH_TPS = 5; // TODO: Change this rate?

	/**
	 * The duration of a game in seconds.
	 */
	public static final int DURATION_SECONDS = 120;

	/**
	 * The movement speed.
	 */
	public static final Units UNITS_PER_TICK = new Units(4d / TPS);

	/**
	 * Barriers.
	 */
	public final Rect[] barriers = new Rect[]{
		slots(4, 3, 6, 1), /* Top. */
		slots(Game.WIDTH.u() - 1, 4.5, 1, 5.5), /* Right. */
		slots(2, Game.HEIGHT.u() - 1, Game.WIDTH.u() - 2, 1), /* Bottom. */
		slots(2, 3, 1, 7), /* Left. */
		slots(5.25, 6, 2.5, 0.75), /* Plates. */
	};

	/**
	 * How many ticks there are left until the game is over.
	 */
	public final AtomicInteger ticksUntilGameOver;

	/**
	 * The lobby this game is taking place in.
	 */
	private final Lobby lobby;

	/**
	 * The identifier factory.
	 */
	private final IdentifierFactory identifierFactory = new IdentifierFactory();

	/**
	 * The workbenches.
	 */
	public final Workbench[] workbenches = new Workbench[]{
		new CustomerWorkbench(this.identifierFactory.next(), slot(4, 3)),
		new CustomerWorkbench(this.identifierFactory.next(), slot(5, 3)),
		new CustomerWorkbench(this.identifierFactory.next(), slot(6, 3)),
		new CustomerWorkbench(this.identifierFactory.next(), slot(7, 3)),
		new CustomerWorkbench(this.identifierFactory.next(), slot(8, 3)),

		new ItemWorkbench(this.identifierFactory.next(), slot(2, 4), Item.DRINK),
		new GrillWorkbench(this.identifierFactory.next(), slot(2, 5)),
		new GrillWorkbench(this.identifierFactory.next(), slot(2, 6)),

		new FryerWorkbench(this.identifierFactory.next(), slot(2, 7)),
		new FryerWorkbench(this.identifierFactory.next(), slot(2, 8)),

		new ItemWorkbench(this.identifierFactory.next(), slot(3, 9), Item.BREAD),
		new ItemWorkbench(this.identifierFactory.next(), slot(4, 9), Item.CHEESE),
		new ItemWorkbench(this.identifierFactory.next(), slot(5, 9), Item.TOMATO),
		new ItemWorkbench(this.identifierFactory.next(), slot(6, 9), Item.RAW_PATTY),
		new ItemWorkbench(this.identifierFactory.next(), slot(7, 9), Item.RAW_CHICKEN),
		new ItemWorkbench(this.identifierFactory.next(), slot(8, 9), Item.POTATO),
		new ItemWorkbench(this.identifierFactory.next(), slot(9, 9), Item.SALAD_HEAD),

		new BinWorkbench(this.identifierFactory.next(), slot(10, 4)),
		new ChoppingWorkbench(this.identifierFactory.next(), slot(10, 5)),
		new ChoppingWorkbench(this.identifierFactory.next(), slot(10, 6)),
		new OvenWorkbench(this.identifierFactory.next(), slot(10, 7)),
		new OvenWorkbench(this.identifierFactory.next(), slot(10, 8)),

		new PlateWorkbench(this.identifierFactory.next(), slot(5, 6)),
		new PlateWorkbench(this.identifierFactory.next(), slot(6, 6)),
		new PlateWorkbench(this.identifierFactory.next(), slot(7, 6)),
	};

	private final Player tutorialPlayer;

	/**
	 * Represents the score of the game.
	 */
	private int score = 0;

	/**
	 * The tick thread.
	 */
	private final Schedule tick = Schedule.atFixedRate(this::tick, 1000 / TPS, "tick");

	/**
	 * Create a new Game instance.
	 *
	 * @param lobby the lobby this game is taking place in.
	 */
	public Game(Lobby lobby, int ticksUntilGameOver, Player tutorialPlayer) {
		assert lobby != null;

		this.lobby = lobby;
		this.ticksUntilGameOver = new AtomicInteger(ticksUntilGameOver);
		this.tutorialPlayer = tutorialPlayer;

		// this.printWorkbenches();
	}

	private static Coords slot(double x, double y) {
		return new Coords(new Units((x - 0.5) * Workbench.SIZE.u()),
			new Units((y - 0.5) * Workbench.SIZE.u()));
	}

	private static Rect slots(double x, double y, double width, double height) {
		Coords slot = Game.slot(x + (width - 1) / 2, y + (height - 1) / 2);

		return new Rect(slot.getX(), slot.getY(), new Units(width * Workbench.SIZE.u()),
			new Units(height * Workbench.SIZE.u()));
	}

	public Lobby getLobby() {
		if (getTutorialPlayer() != null) {
			this.lobby.setTutorial(true);
		}
		return this.lobby;
	}

	public Player getTutorialPlayer() {
		return this.tutorialPlayer;
	}

	/**
	 * Stop the game.
	 */
	public void stop() {
		this.tick.stop();
	}

	/**
	 * Returns the current score of the game.
	 *
	 * @return The current score.
	 */
	public int getScore() {
		return this.score;
	}

	/**
	 * Forcefully set the score.
	 *
	 * @param score the score.
	 */
	public void forceSetScore(int score) {
		this.score = score;
	}

	/**
	 * Get a workbench via its identifier.
	 *
	 * @param identifier the identifier.
	 * @return the workbench.
	 */
	private Workbench getWorkbenchByIdentifier(Identifier identifier) {
		for (var workbench : this.workbenches) {
			if (workbench.getIdentifier().equals(identifier)) {
				return workbench;
			}
		}

		assert false : "unknown workbench identifier";

		return null;
	}

	/**
	 * Gets the closest workbench near the player and checks if it is in interaction radius.
	 *
	 * @param player The player who wants to interact
	 * @return The closest workbench
	 */
	public Optional<Workbench> getWorkbenchIfInReach(Player player) {
		Workbench closestWorkbench = null;
		Units smallestDistance = new Units(0);

		for (var workbench : this.workbenches) {
			Units distance = Coords.distance(workbench.getRect(),
				player.getRect());
			if (distance.u() < smallestDistance.u() || closestWorkbench == null) {
				closestWorkbench = workbench;
				smallestDistance = distance;
			}
		}

		if (closestWorkbench != null && closestWorkbench.getRect()
			.isInInteractionRadius(player.getRect())) {
			return Optional.of(closestWorkbench);
		}

		return Optional.empty();
	}

	public Optional<Workbench> interact(Player player) {
		var workbenchOrEmpty = this.getWorkbenchIfInReach(player);

		if (workbenchOrEmpty.isEmpty()) {
			return Optional.empty();
		}

		Workbench workbench = workbenchOrEmpty.get();

		player.setHolding(workbench.trade(player.getHolding()));

		return workbenchOrEmpty;
	}

	public Workbench consumeWorkbenchCommand(
		GameUpdateWorkbenchCommand gameUpdateWorkbenchCommand) {
		Workbench workbench = this.getWorkbenchByIdentifier(
			gameUpdateWorkbenchCommand.getWorkbenchIdentifier());

		var oldState = Objects.requireNonNull(workbench).getState();

		synchronized (Objects.requireNonNull(workbench)) {
			workbench.forceSetState(gameUpdateWorkbenchCommand.getState());
			workbench.forceSetContentsAccordingToState(gameUpdateWorkbenchCommand.getContents());
			workbench.ticksUntilStateChange.set(
				gameUpdateWorkbenchCommand.getTicksUntilStateChange());
			workbench.setGameScoreAtLastUpdate(this.getScore());
		}

		if (workbench instanceof CustomerWorkbench customerWorkbench
			&& workbench.getState() == State.ACTIVE && oldState == State.IDLE) {
			customerWorkbench.nextRandomInteger();
		}

		return workbench;
	}

	public void consumeHoldingCommand(Player player, PlayerHoldingCommand playerHoldingCommand) {
		player.setHolding(playerHoldingCommand.getStack());
	}

	public void movePlayerBy(Player player, Units x, Units y) {
		Rect now = player.getRect().copy();
		Rect next = now.copy();

		/* Move. */
		next.setLeft(new Units(Math.max(PLAYABLE_AREA.getLeft().u(), now.getLeft().u() + x.u())));
		next.setTop(new Units(Math.max(PLAYABLE_AREA.getTop().u(), now.getTop().u() + y.u())));

		/* Collide with barriers. */
		int iterations = 0;
		while (true) {
			++iterations;
			assert iterations < 100 : "could not avoid collision";

			/* Search for a collision. */
			Rect obstacle = null;
			for (var barrier : this.barriers) {
				if (next.isWithin(barrier)) {
					obstacle = barrier;
				}
			}
			if (obstacle == null) {
				break;
			}

			boolean nowBelowObstacleBottom = now.getTop().u() >= obstacle.getBottom().u();
			boolean nextAboveObstacleBottom = next.getTop().u() < obstacle.getBottom().u();

			boolean nowAboveObstacleTop = now.getBottom().u() <= obstacle.getTop().u();
			boolean nextBelowObstacleTop = next.getBottom().u() > obstacle.getTop().u();

			boolean nowWestOfObstacleLeft = now.getRight().u() <= obstacle.getLeft().u();
			boolean nextEastOfObstacleLeft = next.getRight().u() > obstacle.getLeft().u();

			boolean nowEastOfObstacleRight = now.getLeft().u() >= obstacle.getRight().u();
			boolean nextWestOfObstacleRight = next.getLeft().u() < obstacle.getRight().u();

			if (nowBelowObstacleBottom && nextAboveObstacleBottom) { /* bottom collision */
				next.setTop(obstacle.getBottom());
			} else if (nowAboveObstacleTop && nextBelowObstacleTop) { /* top collision */
				next.setBottom(obstacle.getTop());
			}

			if (nowWestOfObstacleLeft && nextEastOfObstacleLeft) { /* left collision */
				next.setRight(obstacle.getLeft());
			} else if (nowEastOfObstacleRight && nextWestOfObstacleRight) { /* right collision */
				next.setLeft(obstacle.getRight());
			}
		}

		/* Clamp coordinates to world size. */
		next.setLeft(new Units(
			Math.min(next.getLeft().u(), PLAYABLE_AREA.getRight().u() - now.getWidth().u())));
		next.setTop(new Units(
			Math.min(next.getTop().u(), PLAYABLE_AREA.getBottom().u() - now.getHeight().u())));

		player.getRect().setX(next.getX());
		player.getRect().setY(next.getY());
	}

	private Object tick() {
		if (!this.lobby.isServerSide && !Client.the().hasOwnActor()) {
			Messenger.warn("Not ticking - have no own actor.");
			return null;
		}

		int currentTick = this.ticksUntilGameOver.decrementAndGet() % TPS; // FIXME: Remove % TPS?

		/*
		 * Update workbenches.
		 */
		if (currentTick % (TPS / WORKBENCH_TPS) == 0) {
			for (var workbench : this.workbenches) {
				/* Determine if we're ticking this workbench. */
				boolean tickingTransformerWorkbench =
					workbench instanceof TransformerWorkbench && (
						workbench.getState() == State.ACTIVE || workbench.getState()
							== State.FINISHED);
				boolean tickingCustomerWorkbench = workbench instanceof CustomerWorkbench;

				/* Update ticks until state change. */
				var ticks = 0;
				if (tickingTransformerWorkbench || tickingCustomerWorkbench) {
					ticks = workbench.ticksUntilStateChange.addAndGet(
						-(TPS / WORKBENCH_TPS) /* FIXME: */);
				}

				/* Check if we should apply the state change. */
				boolean applyStateChange = ticks < 0 && this.lobby.isServerSide;

				/* Apply the state change. */
				if (applyStateChange) {
					if (tickingTransformerWorkbench) {
						switch (workbench.getState()) {
							case ACTIVE -> {
								workbench.forceSetState(State.FINISHED);
								workbench.ticksUntilStateChange.set(
									((TransformerWorkbench) workbench).getRecipe()
										.expirationTimeSeconds() * TPS);
							}
							case FINISHED -> {
								if (((TransformerWorkbench) workbench).getRecipe()
									.expirationTimeSeconds() > 0) {
									workbench.forceSetState(State.EXPIRED);
								} else {
									/* The transformation is finished and cannot expire. No state has changed. */
									applyStateChange = false;
								}
							}
						}
					} else {
						switch (workbench.getState()) {
							case ACTIVE -> {
								workbench.forceSetState(State.EXPIRED);
								workbench.ticksUntilStateChange.set(
									CustomerWorkbench.EXPIRED_COOLDOWN_SECONDS * TPS);
							}
							case FINISHED, EXPIRED -> {
								if (workbench.getState() == State.FINISHED) {
									this.score += ((CustomerWorkbench) workbench).getOrder()
										.orElseThrow().getPrice();
									this.lobby.broadcast(
										new GameScoreCommand(this.getScore()));
								}
								workbench.forceSetState(State.IDLE);
								((CustomerWorkbench) workbench).forceSetOrder(null);
								workbench.ticksUntilStateChange.set(
									CustomerWorkbench.IDLE_COOLDOWN_SECONDS * TPS);
							}
							case IDLE -> {
								CustomerWorkbench customerWorkbench = (CustomerWorkbench) workbench;
								customerWorkbench.forceSetState(State.ACTIVE);
								customerWorkbench.forceSetOrder(Order.pickRandom());
								customerWorkbench.ticksUntilStateChange.set(
									customerWorkbench.getOrder().orElseThrow()
										.getExpirationTimeSeconds() * TPS);
								Messenger.debug("Announced new order at customer workbench "
									+ customerWorkbench.getIdentifier());
							}
						}
					}
				}

				if (applyStateChange) {
					/* Broadcast the state change. */
					this.lobby.broadcast(workbench.representedAsCommands());
					Messenger.debug(
						"State of workbench " + workbench.getIdentifier() + " changed");
				}
			}
		}

		/*
		 * Client-specific code.
		 */
		if (this.lobby.isServerSide) {
			return null;
		}

		Actor actor = Client.the().getOwnActor();
		Player player;
		if (this.getTutorialPlayer() != null) {
			player = this.getTutorialPlayer();
		} else {
			var playerOrEmpty = actor.member().orElseThrow().player();
			if (playerOrEmpty.isEmpty()) {
				return null;
			}
			player = playerOrEmpty.orElseThrow();
		}

		/*
		 * Move.
		 */
		if (ClientGUI.exists() && ClientGUI.the()
			.getActiveView() instanceof KitchenView kitchenView) {
			Units vx = new Units(
				UNITS_PER_TICK.u() * ((kitchenView.keys.getOrDefault(KeyCode.D, false) ? 1 : 0)
					- (
					kitchenView.keys.getOrDefault(KeyCode.A, false) ? 1 : 0)));
			Units vy = new Units(
				UNITS_PER_TICK.u() * ((kitchenView.keys.getOrDefault(KeyCode.S, false) ? 1 : 0)
					- (
					kitchenView.keys.getOrDefault(KeyCode.W, false) ? 1 : 0)));

			/* Normalize diagonal movement speed. */
			if (vx.u() != 0 && vy.u() != 0) {
				vx = new Units(vx.u() * Math.cos(Math.PI / 4));
				vy = new Units(vy.u() * Math.sin(Math.PI / 4));
			}
			this.movePlayerBy(player, vx, vy);
		}

		/*
		 * Send new position to server.
		 */
		if (this.getTutorialPlayer() == null && currentTick % (TPS / POSITION_TPS) == 0) {
			Client.the().tryMove(player.getRect());
		}

		return null;
	}

	private void printWorkbenches() {
		StringBuilder sb = new StringBuilder("Workbenches:\n");
		for (var workbench : this.workbenches) {
			sb.append("[");
			sb.append(workbench.getIdentifier());
			sb.append("] ");
			if (workbench instanceof CustomerWorkbench) {
				sb.append("Customer");
			} else if (workbench instanceof GrillWorkbench) {
				sb.append("Grill");
			} else if (workbench instanceof ItemWorkbench) {
				sb.append("Item");
			} else if (workbench instanceof PlateWorkbench) {
				sb.append("Plate");
			}
			sb.append(" @ ");
			sb.append(String.format("%.2f,%.2f", workbench.getRect().getLeft().u(),
				workbench.getRect().getTop().u()));
			sb.append(" <");
			sb.append(workbench.getState());
			sb.append(">: ");
			sb.append(workbench.peekContents());
			sb.append("\n");
		}
		Messenger.info(sb.toString());
	}

	public Command timeRepresentedAsCommand() {
		GameTimeCommand time = null;

		try {
			time = new GameTimeCommand(this.lobby.getName(), this.ticksUntilGameOver.get());
		} catch (MalformedException e) {
			assert false : "lobby returned malformed name";
		}

		return time;
	}

	public Command[] representedAsCommands() {
		var commands = new ArrayList<Command>();

		/* Ticks remaining. */
		commands.add(this.timeRepresentedAsCommand());

		/* Score. */
		commands.add(new GameScoreCommand(this.score));

		/* Workbenches. */
		for (var workbench : this.workbenches) {
			commands.addAll(Arrays.asList(workbench.representedAsCommands()));
		}

		/* Players. */
		for (var actor : this.lobby.getActors()) {
			var playerOrEmpty = actor.member().orElseThrow().player();
			if (playerOrEmpty.isPresent()) {
				commands.addAll(
					Arrays.asList(
						playerOrEmpty.orElseThrow().representedAsCommands(actor)));
			}
		}

		return commands.toArray(new Command[0]);
	}
}
