package ch.unibas.dmi.dbis.cs108.letuscook.client;

import ch.unibas.dmi.dbis.cs108.letuscook.Main;
import ch.unibas.dmi.dbis.cs108.letuscook.commands.ChatCommand;
import ch.unibas.dmi.dbis.cs108.letuscook.commands.Command;
import ch.unibas.dmi.dbis.cs108.letuscook.commands.DisappearCommand;
import ch.unibas.dmi.dbis.cs108.letuscook.commands.GameParticipateCommand;
import ch.unibas.dmi.dbis.cs108.letuscook.commands.GameRequestStartCommand;
import ch.unibas.dmi.dbis.cs108.letuscook.commands.GameScoreCommand;
import ch.unibas.dmi.dbis.cs108.letuscook.commands.GameTimeCommand;
import ch.unibas.dmi.dbis.cs108.letuscook.commands.GameUpdateWorkbenchCommand;
import ch.unibas.dmi.dbis.cs108.letuscook.commands.HighscoresCommand;
import ch.unibas.dmi.dbis.cs108.letuscook.commands.IntroduceCommand;
import ch.unibas.dmi.dbis.cs108.letuscook.commands.LobbyCloseCommand;
import ch.unibas.dmi.dbis.cs108.letuscook.commands.LobbyJoinCommand;
import ch.unibas.dmi.dbis.cs108.letuscook.commands.LobbyLeaveCommand;
import ch.unibas.dmi.dbis.cs108.letuscook.commands.LobbyOpenCommand;
import ch.unibas.dmi.dbis.cs108.letuscook.commands.LobbyReadyCommand;
import ch.unibas.dmi.dbis.cs108.letuscook.commands.PlayerHoldingCommand;
import ch.unibas.dmi.dbis.cs108.letuscook.commands.PlayerInteractCommand;
import ch.unibas.dmi.dbis.cs108.letuscook.commands.PlayerPositionCommand;
import ch.unibas.dmi.dbis.cs108.letuscook.commands.RefreshCommand;
import ch.unibas.dmi.dbis.cs108.letuscook.commands.YellCommand;
import ch.unibas.dmi.dbis.cs108.letuscook.gui.ClientGUI;
import ch.unibas.dmi.dbis.cs108.letuscook.gui.View;
import ch.unibas.dmi.dbis.cs108.letuscook.gui.Views;
import ch.unibas.dmi.dbis.cs108.letuscook.orders.ChoppingWorkbench;
import ch.unibas.dmi.dbis.cs108.letuscook.orders.CustomerWorkbench;
import ch.unibas.dmi.dbis.cs108.letuscook.orders.State;
import ch.unibas.dmi.dbis.cs108.letuscook.orders.TransformerWorkbench;
import ch.unibas.dmi.dbis.cs108.letuscook.orders.Workbench;
import ch.unibas.dmi.dbis.cs108.letuscook.server.Actor;
import ch.unibas.dmi.dbis.cs108.letuscook.server.Highscores;
import ch.unibas.dmi.dbis.cs108.letuscook.server.Lobby;
import ch.unibas.dmi.dbis.cs108.letuscook.server.Member;
import ch.unibas.dmi.dbis.cs108.letuscook.server.Player;
import ch.unibas.dmi.dbis.cs108.letuscook.server.Record;
import ch.unibas.dmi.dbis.cs108.letuscook.util.Connection;
import ch.unibas.dmi.dbis.cs108.letuscook.util.Coords;
import ch.unibas.dmi.dbis.cs108.letuscook.util.Identifier;
import ch.unibas.dmi.dbis.cs108.letuscook.util.MalformedException;
import ch.unibas.dmi.dbis.cs108.letuscook.util.Messenger;
import ch.unibas.dmi.dbis.cs108.letuscook.util.SanitizedName;
import ch.unibas.dmi.dbis.cs108.letuscook.util.Schedule;
import ch.unibas.dmi.dbis.cs108.letuscook.util.Sounds;
import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class Client {

	private static volatile Client the;

	/**
	 * The address we connect to.
	 */
	private final InetAddress address;

	/**
	 * The port we connect to.
	 */
	private final int port;

	/**
	 * All known actors.
	 */
	private final List<Actor> actors = Collections.synchronizedList(new ArrayList<>());

	/**
	 * All known lobbies.
	 */
	private final List<Lobby> lobbies = Collections.synchronizedList(new ArrayList<>());

	/**
	 * The login nickname.
	 */
	private volatile SanitizedName loginNickname = SanitizedName.createOrUseFallback(
		System.getProperty("user.name"), SanitizedName.createUnsafe("user"));

	/**
	 * The {@link Connection} we use to control our actor on the server.
	 */
	private Connection connection;

	/**
	 * Our own {@link Identifier}.
	 */
	private Identifier identifier = Identifier.NONE;

	/**
	 * Periodically check that the connection is still intact.
	 */
	private Schedule heartbeat;

	/**
	 * The highscores.
	 */
	private Highscores highscores = new Highscores(new ArrayList<>());

	/**
	 * Denotes if the debug mode is active.
	 */
	private boolean debugMode = false;

	/**
	 * Create a client.
	 *
	 * @param address the address to connect to on {@link #tryConnect()}.
	 * @param port    the port to connect to on {@link #tryConnect()}.
	 */
	public Client(InetAddress address, int port) {
		assert Client.the == null;
		assert address != null;

		this.address = address;
		this.port = port;

		Client.the = this;
	}

	public static Client the() {
		assert Client.the != null : "client not initialized";

		return Client.the;
	}

	/**
	 * @return the address and port in textual form.
	 */
	private String getAddressAndPort() {
		return this.address.getHostAddress() + ":" + port;
	}

	/**
	 * @return the login nickname.
	 */
	public SanitizedName getLoginNickname() {
		return this.loginNickname;
	}

	/**
	 * Set the login nickname.
	 *
	 * @param loginNickname the login nickname.
	 */
	public void setLoginNickname(SanitizedName loginNickname) {
		assert loginNickname != null;

		this.loginNickname = loginNickname;
	}

	/**
	 * Attempt to connect to the server.
	 */
	public void tryConnect() {
		if (this.hasConnection()) {
			Messenger.error("Cannot connect while already connected.");
			return;
		}

		Messenger.info("Connecting to " + this.getAddressAndPort());

		try {
			this.connection = new Connection("conn", this.address, this.port, this::consumeCommand);
		} catch (IOException | SecurityException | NullPointerException e) {
			Messenger.error(e, "Cannot connect to server - aborting connect()");
			return;
		} catch (IllegalArgumentException e) {
			Messenger.error(e, "Port is outside the valid range of values - aborting connect()");
			return;
		}

		this.startHeartbeat();

		/*
		 * Introduce ourselves to the server.
		 */
		try {
			this.sendCommand(new IntroduceCommand(this.loginNickname.toString()));
		} catch (MalformedException e) {
			assert false : "loginNickname contained malformed name";
		}
	}

	/**
	 * @return whether the client has a connection.
	 */
	public boolean hasConnection() {
		return this.connection != null;
	}

	/**
	 * @return whether this client has its own actor (i.e., is logged in).
	 */
	public boolean hasOwnActor() {
		return this.hasConnection() && !this.getOwnIdentifier().isNone()
			&& this.findActorByIdentifier(this.getOwnIdentifier()).isPresent();
	}

	/**
	 * @return the client's connection.
	 */
	private Connection getConnection() {
		assert this.hasConnection();

		return this.connection;
	}

	/**
	 * Send a command to this client's underlying {@link #connection}.
	 *
	 * @param command the command to send.
	 */
	private void sendCommand(Command command) {
		assert command != null : "cannot send null command";
		assert this.hasConnection() : "cannot send command while disconnected";

		this.connection.sendCommandIfAlive(command);
	}

	/**
	 * Disconnect from the server.
	 */
	public void tryDisconnect() {
		if (!this.hasConnection()) {
			Messenger.error("Cannot disconnect while not connected.");
			return;
		}

		this.sendCommand(new DisappearCommand());

		this.heartbeat.stop();
		this.heartbeat = null;

		this.connection.destroy();
		this.connection = null;

		this.identifier = Identifier.NONE;

		this.forgetState();

		Messenger.info("Disconnected");

		ClientGUI.changeRequestedView(Views.START);

	}

	/**
	 * Start the heartbeat schedule.
	 */
	private void startHeartbeat() {
		assert this.heartbeat == null : "heartbeat not null";

		this.heartbeat = Schedule.withFixedDelay(() -> {
			if (this.hasConnection()) {
				Connection connection = this.getConnection();

				if (connection.isAwaitingPong()) {
					Messenger.error("Connection timed out - disconnecting");
					this.tryDisconnect();
				} else {
					connection.ping();
				}
			}
			return null;
		}, Main.PONG_WAIT_MS, "heartbeat");
	}

	/**
	 * @return this client's {@link #identifier}.
	 */
	public Identifier getOwnIdentifier() {
		return this.identifier;
	}

	/**
	 * Set this client's {@link #identifier}.
	 *
	 * @param identifier the {@link Identifier}.
	 */
	private void setOwnIdentifier(Identifier identifier) {
		assert this.getOwnIdentifier().isNone() : "already has identifier";
		assert identifier != null : "identifier is null";
		assert !identifier.isNone() : "identifier is none";

		this.identifier = identifier;

		Messenger.info("Set own identifier to " + this.getOwnIdentifier());
	}

	/**
	 * @return all known actors.
	 */
	public Actor[] getActors() {
		return this.actors.toArray(new Actor[0]);
	}

	/**
	 * @return the highscores.
	 */
	public Highscores getHighscores() {
		return this.highscores;
	}

	/**
	 * @return whether the debug mode is active.
	 */
	public boolean isDebugMode() {
		return this.debugMode;
	}

	/**
	 * Set whether the debug mode is active.
	 *
	 * @param debugMode whether the debug mode is active.
	 */
	public void setDebugMode(final boolean debugMode) {
		this.debugMode = debugMode;
	}

	/**
	 * Add an actor.
	 *
	 * @param identifier the actor's identifier.
	 * @param nickname   the actor's nickname.
	 */
	private void addActor(Identifier identifier, final String nickname) {
		assert !identifier.isNone() : "invalid identifier";
		assert nickname != null : "nickname is null";
		assert this.findActorByIdentifier(identifier).isEmpty() : "actor already known";

		Actor actor = new Actor(identifier);
		actor.setRecord(new Record(nickname, null));

		synchronized (this.actors) {
			this.actors.add(actor);
		}

		Messenger.info("'" + nickname + "' exists with identifier " + identifier);
	}

	/**
	 * Get an actor via its identifier.
	 *
	 * @param identifier the actor's identifier.
	 * @return an {@link Optional} that may or may not contain the actor, depending on if it is
	 * found.
	 */
	private Optional<Actor> findActorByIdentifier(Identifier identifier) {
		if (identifier.isNone()) {
			return Optional.empty();
		}

		synchronized (this.actors) {
			for (var actor : this.actors) {
				if (actor.getIdentifier().equals(identifier)) {
					return Optional.of(actor);
				}
			}
		}

		return Optional.empty();
	}

	/**
	 * Get an actor via its nickname.
	 *
	 * @param nickname the actor's nickname.
	 * @return an {@link Optional} that may or may not contain the actor, depending on if it is
	 * found.
	 */
	private Optional<Actor> findActorByNickname(String nickname) {
		assert nickname != null : "nickname is null";

		synchronized (this.actors) {
			for (var actor : this.actors) {
				if (actor.record().orElseThrow().getNickname().equals(nickname)) {
					return Optional.of(actor);
				}
			}
		}

		return Optional.empty();
	}

	/**
	 * Remove an actor.
	 *
	 * @param actor the actor.
	 */
	private void removeActor(Actor actor) {
		assert actor != null : "actor is null";

		synchronized (this.actors) {
			assert this.actors.contains(actor) : "actor not known";

			this.actors.remove(actor);
		}

		Messenger.info("'" + actor.record().orElseThrow().getNickname() + "' logged out");
	}

	/**
	 * Get our own actor.
	 *
	 * @return our actor.
	 */
	public Actor getOwnActor() {
		assert hasOwnActor() : "have no own actor";

		var actorOrEmpty = this.findActorByIdentifier(this.getOwnIdentifier());
		assert actorOrEmpty.isPresent();

		return actorOrEmpty.orElseThrow();
	}

	/**
	 * All known lobbies.
	 */
	public synchronized Lobby[] getLobbies() {
		return this.lobbies.toArray(new Lobby[0]);
	}

	private synchronized void addLobby(String name) {
		assert name != null : "name is null";
		assert this.findLobby(name).isEmpty() : "lobby already exists";

		Lobby lobby;

		lobby = new Lobby(false, name);
		this.lobbies.add(lobby);

		Messenger.info("Lobby '" + lobby.getName() + "' is open");
	}

	private synchronized Optional<Lobby> findLobby(String name) {
		assert name != null : "name is null";

		for (var lobby : this.lobbies) {
			if (lobby.getName().equals(name)) {
				return Optional.of(lobby);
			}
		}

		return Optional.empty();
	}

	private synchronized void addActorToLobby(Actor actor, Lobby lobby) {
		assert actor != null : "actor is null";
		assert lobby != null : "lobby is null";

		actor.setLobby(lobby);
		lobby.addMember(actor);

		Messenger.info(
			"'" + actor.record().orElseThrow().getNickname() + "' is in lobby '" + lobby.getName()
				+ "'");
	}

	private synchronized void removeActorFromLobby(Actor actor) {
		assert actor != null : "actor is null";
		assert actor.member().isPresent() : "not member of a lobby";
		assert actor.member().orElseThrow().getLobby()
			.contains(actor) : "lobby does not contain actor";

		Lobby lobby = actor.member().orElseThrow().getLobby();

		if (actor.getIdentifier().equals(this.getOwnIdentifier())) {
			lobby.stopGame(true);
		}
		lobby.removeMember(actor);
		actor.clearLobby();
	}

	private synchronized void removeLobby(Lobby lobby) {
		assert lobby != null : "lobby is null";

		assert this.lobbies.contains(lobby) : "unknown lobby";
		assert lobby.isEmpty();

		this.lobbies.remove(lobby);
	}

	/**
	 * Consume a command. This method is supplied to
	 * {@link Connection#Connection(String, InetAddress, int, Consumer)} during
	 * {@link #tryConnect()}.
	 *
	 * @param command the command to process.
	 */
	private void consumeCommand(Command command) {
		/*
		 * These commands can always be consumed.
		 */

		Identifier identifier = command.getSubject();
		var actorOrEmpty = this.findActorByIdentifier(identifier);

		if (command instanceof HighscoresCommand highscoresCommand) {
			this.highscores = highscoresCommand.getHighscores();
			return;
		}

		if (command instanceof IntroduceCommand introduceCommand) {
			if (actorOrEmpty.isEmpty()) {
				this.addActor(identifier, introduceCommand.getNickname());
				return;
			}

			String nickname = introduceCommand.getNickname();
			var actor = actorOrEmpty.orElseThrow();
			Messenger.user(
				actor.record().orElseThrow().getNickname() + " changed their name to " + nickname
					+ ".");
			actor.record().orElseThrow().setNickname(nickname);
			return;
		}

		if (command instanceof DisappearCommand && identifier.equals(Identifier.NONE)) {
			this.tryDisconnect();
			return;
		}

		if (command instanceof RefreshCommand) {
			// TODO: Only do this if this is the first time we're refreshing.
			ClientGUI.changeRequestedView(Views.LOBBIES);
			this.forgetState();
			this.setOwnIdentifier(identifier);
			return;
		}

		if (command instanceof LobbyOpenCommand lobbyOpenCommand) {
			this.addLobby(lobbyOpenCommand.getLobbyName());
			return;
		}

		if (command instanceof GameTimeCommand gameTimeCommand) {
			var lobbyOrEmpty = this.findLobby(gameTimeCommand.getLobbyName());
			if (lobbyOrEmpty.isEmpty()) {
				/* This is expected: the server informs us that a game has ended because a lobby was closed, but we've already closed it locally. */
				assert gameTimeCommand.getTicksUntilFinished() == 0;
				return;
			}

			Lobby lobby = lobbyOrEmpty.orElseThrow();
			boolean isOwnLobby = this.hasOwnActor() && this.getOwnActor().member().isPresent()
				&& gameTimeCommand.getLobbyName()
				.equals(this.getOwnActor().member().orElseThrow().getLobby().getName());

			/* Stop an ongoing game. */
			if (gameTimeCommand.getTicksUntilFinished() <= 0) {
				if (isOwnLobby) {
					ClientGUI.changeRequestedView(Views.LOBBY);
					Messenger.user("Game over!");
				}
				lobby.stopGame(false);
				return;
			}

			/* Update an ongoing game. */
			if (lobby.gameIsRunning() && lobby.game().isPresent()) {
				lobby.game().orElseThrow().ticksUntilGameOver.set(
					gameTimeCommand.getTicksUntilFinished());
				return;
			}

			/* Start a new game. */
			if (isOwnLobby) {
				lobby.startGame(gameTimeCommand.getTicksUntilFinished());
				ClientGUI.changeRequestedView(Views.GAME);
				Messenger.user("Game started!");
			} else {
				lobby.setGameIsRunning(true);
			}
			return;
		}

		if (command instanceof LobbyCloseCommand lobbyCloseCommand) {
			var lobbyOrEmpty = this.findLobby(lobbyCloseCommand.getLobbyName());
			assert lobbyOrEmpty.isPresent() : "received close for unknown lobby";
			Lobby lobby = lobbyOrEmpty.orElseThrow();
			assert lobby.isEmpty();

			Messenger.info("'" + lobby.getName() + "' closed.");
			this.removeLobby(lobby);
			return;
		}

		if (command instanceof GameUpdateWorkbenchCommand gameUpdateWorkbenchCommand) {
			if (!this.hasOwnActor() || this.getOwnActor().member().isEmpty() || !this.getOwnActor()
				.member().orElseThrow().getLobby().gameIsRunning()) {
				Messenger.warn("Received WorkbenchCommand command while game is not running");
				return;
			}

			Workbench affectedWorkbench = this.getOwnActor().member().orElseThrow().getLobby()
				.game().orElseThrow().consumeWorkbenchCommand(gameUpdateWorkbenchCommand);

			if (affectedWorkbench instanceof CustomerWorkbench customerWorkbench
				&& customerWorkbench.getState() == State.EXPIRED) {
				// Sounds.ANGRY.play();
			} else if (affectedWorkbench instanceof TransformerWorkbench) {
				if (affectedWorkbench.getState() == State.EXPIRED) {
					Sounds.EXPIRE.play();
				} else if (!(affectedWorkbench instanceof ChoppingWorkbench)) {
					// Sounds.UPDATE.play();
				}
			}

			Messenger.debug(
				"Workbench " + affectedWorkbench.getIdentifier() + " is now "
					+ affectedWorkbench.getState()
					+ " with contents: " + affectedWorkbench.peekContents());
			return;
		}

		if (command instanceof GameScoreCommand gameScoreCommand) {
			if (!this.hasOwnActor() || this.getOwnActor().member().isEmpty() || !this.getOwnActor()
				.member().orElseThrow().getLobby().gameIsRunning()) {
				Messenger.warn("Received ScoreCommand while game is not running");
				return;
			}

			this.getOwnActor().member().orElseThrow().getLobby().game().orElseThrow()
				.forceSetScore(gameScoreCommand.getScore());

			return;
		}

		/*
		 * These commands cannot be anonymous and require us to know our own identifier.
		 */

		if (this.identifier.isNone()) {
			Messenger.warn("Ignoring incoming command before log-in");
			return;
		}
		assert !identifier.isNone() : "server is sending forbidden anonymous command";

		/*
		 * These commands require the actor to be known.
		 */

		assert actorOrEmpty.isPresent() : "unknown actor";

		if (command instanceof DisappearCommand) {
			this.removeActor(actorOrEmpty.orElseThrow());
			if (identifier.equals(this.getOwnIdentifier())) {
				this.tryDisconnect();
			}
			return;
		}

		if (command instanceof LobbyJoinCommand lobbyJoinCommand) {
			var lobbyOrEmpty = this.findLobby(lobbyJoinCommand.getLobbyName());
			assert lobbyOrEmpty.isPresent() : "received join to unknown lobby";
			this.addActorToLobby(actorOrEmpty.orElseThrow(), lobbyOrEmpty.orElseThrow());
			if (actorOrEmpty.orElseThrow().getIdentifier().equals(this.getOwnIdentifier())) {
				ClientGUI.changeRequestedView(Views.LOBBY);
			}
			return;
		}

		if (command instanceof LobbyLeaveCommand) {
			Messenger.info(
				"'" + actorOrEmpty.orElseThrow().record().orElseThrow().getNickname()
					+ "' left lobby '" + actorOrEmpty.orElseThrow().member().orElseThrow()
					.getLobby().getName()
					+ "'");
			this.removeActorFromLobby(actorOrEmpty.orElseThrow());
			if (this.hasOwnActor() && actorOrEmpty.orElseThrow().getIdentifier()
				.equals(this.getOwnIdentifier())) {
				ClientGUI.changeRequestedView(Views.LOBBIES);
			}
			return;
		}

		if (command instanceof ChatCommand chatCommand) {
			assert !chatCommand.isWhispered()
				|| identifier.equals(this.getOwnIdentifier())
				|| chatCommand.getRecipient().equals(this.getOwnIdentifier())
				: "received leaked private message";
			if (chatCommand.isWhispered()) {
				var recipientOrEmpty = this.findActorByIdentifier(
					chatCommand.getRecipient());
				if (recipientOrEmpty.isEmpty()) {
					Messenger.error("Received whisper from unknown actor");
				} else {
					Messenger.chat(actorOrEmpty.orElseThrow().record().orElseThrow().getNickname(),
						recipientOrEmpty.orElseThrow().record().orElseThrow().getNickname(),
						chatCommand.getMessage());
				}
			} else {
				Messenger.chat(actorOrEmpty.orElseThrow().record().orElseThrow().getNickname(),
					chatCommand.getMessage());
			}
			return;
		}

		if (command instanceof YellCommand yellCommand) {
			Messenger.yell(actorOrEmpty.orElseThrow().record().orElseThrow().getNickname(),
				yellCommand.getMessage());
			return;
		}

		if (command instanceof LobbyReadyCommand lobbyReadyCommand) {
			actorOrEmpty.orElseThrow().member().orElseThrow()
				.setReady(lobbyReadyCommand.getReady());
			return;
		}

		if (command instanceof PlayerPositionCommand playerPositionCommand) {
			if (!this.hasOwnActor() || this.getOwnActor().member().isEmpty() || !this.getOwnActor()
				.member().orElseThrow().getLobby().gameIsRunning()) {
				Messenger.warn("Received PositionCommand command while own game is not running");
				return;
			}

			/* Create a player if one doesn't already exist. */
			Member member = actorOrEmpty.orElseThrow().member().orElseThrow();
			Player player;
			if (member.player().isEmpty()) {
				member.setPlayer(new Player(playerPositionCommand.getCoords()));
				player = member.player().orElseThrow();
			} else {
				player = actorOrEmpty.orElseThrow().member().orElseThrow().player()
					.orElseThrow();
			}

			player.getPreviousCoords().setX(player.getRealCoords().getX());
			player.getPreviousCoords().setY(player.getRealCoords().getY());
			if (actorOrEmpty.orElseThrow().getIdentifier()
				.equals(Client.the().getOwnIdentifier())) {
				player.getRect().setX(player.getRealCoords().getX());
				player.getRect().setY(player.getRealCoords().getY());
			}
			player.getRealCoords().setX(playerPositionCommand.getCoords().getX());
			player.getRealCoords().setY(playerPositionCommand.getCoords().getY());
			return;
		}

		if (command instanceof PlayerHoldingCommand playerHoldingCommand) {
			if (!this.hasOwnActor() || this.getOwnActor().member().isEmpty() || !this.getOwnActor()
				.member().orElseThrow().getLobby().gameIsRunning()) {
				Messenger.warn("Received HoldingCommand command while game is not running");
				return;
			}

			Player player = actorOrEmpty.orElseThrow().member().orElseThrow().player()
				.orElseThrow();

			this.getOwnActor().member().orElseThrow().getLobby().game().orElseThrow()
				.consumeHoldingCommand(player, playerHoldingCommand);

			Messenger.debug(
				"'" + actorOrEmpty.orElseThrow().record().orElseThrow().getNickname()
					+ "' is now holding: "
					+ player.getHolding());
			return;
		}

		/*
		 * These commands require the game to be running.
		 */

		assert this.getOwnActor().member().orElseThrow().getLobby().game()
			.isPresent() : "game not running";

		Messenger.error("Ignoring unsupported command: " + command);
	}

	public void sendReady() {

		this.sendCommand(new LobbyReadyCommand(true));
	}

	/**
	 * Suggest a new nickname for ourselves. The server cannot guarantee to accept our suggestion
	 * exactly as we provide it: see {@link Record#setNickname(String)}.
	 *
	 * @param nickname the suggestion.
	 */
	public void tryNickname(String nickname) {
		if (this.hasConnection()) {
			try {
				this.sendCommand(new IntroduceCommand(nickname));
			} catch (MalformedException e) {
				Messenger.error("Malformed nickname.");
			}
		} else {
			Messenger.error("Cannot set nickname while disconnected.");
		}
	}

	/**
	 * Open a new lobby. The server cannot guarantee to accept our suggested lobby name exactly as
	 * we provide it: see
	 * {@link ch.unibas.dmi.dbis.cs108.letuscook.server.Server#createLobbyThenAddActor(String,
	 * Actor)}.
	 *
	 * @param suggestedName the suggested lobby name.
	 */
	public void tryOpenLobby(String suggestedName) {
		if (this.hasConnection()) {
			try {
				this.sendCommand(new LobbyOpenCommand(suggestedName));
			} catch (MalformedException e) {
				Messenger.error("Malformed lobby name.");
			}
		} else {
			Messenger.error("Cannot open lobby while disconnected.");
		}
	}

	/**
	 * Attempt to join a lobby.
	 *
	 * @param name the lobby name.
	 */
	public void tryJoinLobby(String name) {
		if (this.hasConnection()) {
			try {
				this.sendCommand(new LobbyJoinCommand(name));
			} catch (MalformedException e) {
				Messenger.error("Malformed lobby name.");
			}
		} else {
			Messenger.error("Cannot join lobby while disconnected.");
		}
	}

	public void tryParticipate() {
		if (this.getOwnActor().member().orElseThrow().getLobby().gameIsRunning()) {
			this.sendCommand(new GameParticipateCommand());
		} else {
			Messenger.error("No participation request sent because game is not running");
		}
	}

	public void tryMove(Coords coords) {
		if (this.getOwnActor().member().orElseThrow().player().isPresent()) {
			this.sendCommand(new PlayerPositionCommand(coords));
		} else {
			Messenger.error("No coordinates sent cause game is not running");
		}
	}

	public void tryInteract() {
		if (this.getOwnActor().member().orElseThrow().player().isPresent()) {
			this.tryMove(
				this.getOwnActor().member().orElseThrow().player().orElseThrow().getRect());
			this.sendCommand(new PlayerInteractCommand());
		} else {
			Messenger.error("Not interacting cause game is not running");
		}
	}

	/**
	 * Leave the lobby.
	 */
	public void tryLeaveLobby() {
		if (this.getOwnActor().member().isEmpty()) {
			Messenger.error("Cannot leave lobby - not a member of a lobby");
			return;
		}

		this.sendCommand(new LobbyLeaveCommand());
	}

	public void tryChat(String message) {
		if (!this.hasConnection()) {
			Messenger.error("Cannot chat while disconnected");
			return;
		}

		try {
			this.sendCommand(new ChatCommand(message));
		} catch (MalformedException e) {
			Messenger.error("Malformed chat message");
		}
	}

	public void tryYell(String message) {
		if (!this.hasConnection()) {
			Messenger.error("Cannot yell while disconnected");
			return;
		}

		try {
			this.sendCommand(new YellCommand(message));
		} catch (MalformedException e) {
			Messenger.error("Malformed yell message.");
		}
	}

	public void tryStartGame() {
		if (this.getOwnActor().member().isEmpty()) {
			Messenger.error("Cannot start game outside lobby");
			return;
		}
		if (this.getOwnActor().member().orElseThrow().getLobby().gameIsRunning()) {
			Messenger.error("Game is already running.");
			return;
		}

		Messenger.info("Requesting to start game");

		this.sendCommand(new GameRequestStartCommand());
	}

	public void tryWhisperViaNickname(String recipient, String message) {
		assert recipient != null : "recipient is null";
		assert message != null : "message is null";

		if (!this.hasConnection()) {
			Messenger.error("Cannot whisper while disconnected");
			return;
		}

		var actorOrEmpty = this.findActorByNickname(recipient);
		if (actorOrEmpty.isEmpty()) {
			Messenger.user("User does not exist.");
			return;
		}

		try {
			this.sendCommand(new ChatCommand(actorOrEmpty.orElseThrow().getIdentifier(), message));
		} catch (MalformedException e) {
			Messenger.error("Malformed whisper message");
		}
	}

	private synchronized void forgetState() {
		for (var actor : this.actors) {
			if (actor.member().isPresent()) {
				this.removeActorFromLobby(actor);
			}
		}

		for (var lobby : this.getLobbies()) {
			this.removeLobby(lobby);
		}

		this.lobbies.clear();
		this.actors.clear();

		this.identifier = Identifier.NONE;

		View.clearChat();
	}

	public void tryRefresh() {
		this.sendCommand(new RefreshCommand());
	}
}
