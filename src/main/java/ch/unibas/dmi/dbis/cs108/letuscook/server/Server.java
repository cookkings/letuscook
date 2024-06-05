package ch.unibas.dmi.dbis.cs108.letuscook.server;

import ch.unibas.dmi.dbis.cs108.letuscook.Main;
import ch.unibas.dmi.dbis.cs108.letuscook.commands.ChatCommand;
import ch.unibas.dmi.dbis.cs108.letuscook.commands.Command;
import ch.unibas.dmi.dbis.cs108.letuscook.commands.DisappearCommand;
import ch.unibas.dmi.dbis.cs108.letuscook.commands.GameForceStopCommand;
import ch.unibas.dmi.dbis.cs108.letuscook.commands.GameParticipateCommand;
import ch.unibas.dmi.dbis.cs108.letuscook.commands.GameRequestStartCommand;
import ch.unibas.dmi.dbis.cs108.letuscook.commands.GameTimeCommand;
import ch.unibas.dmi.dbis.cs108.letuscook.commands.HighscoresCommand;
import ch.unibas.dmi.dbis.cs108.letuscook.commands.IntroduceCommand;
import ch.unibas.dmi.dbis.cs108.letuscook.commands.LobbyCloseCommand;
import ch.unibas.dmi.dbis.cs108.letuscook.commands.LobbyJoinCommand;
import ch.unibas.dmi.dbis.cs108.letuscook.commands.LobbyLeaveCommand;
import ch.unibas.dmi.dbis.cs108.letuscook.commands.LobbyOpenCommand;
import ch.unibas.dmi.dbis.cs108.letuscook.commands.LobbyReadyCommand;
import ch.unibas.dmi.dbis.cs108.letuscook.commands.PlayerInteractCommand;
import ch.unibas.dmi.dbis.cs108.letuscook.commands.PlayerPositionCommand;
import ch.unibas.dmi.dbis.cs108.letuscook.commands.RefreshCommand;
import ch.unibas.dmi.dbis.cs108.letuscook.commands.YellCommand;
import ch.unibas.dmi.dbis.cs108.letuscook.gui.Units;
import ch.unibas.dmi.dbis.cs108.letuscook.orders.Workbench;
import ch.unibas.dmi.dbis.cs108.letuscook.util.Connection;
import ch.unibas.dmi.dbis.cs108.letuscook.util.Coords;
import ch.unibas.dmi.dbis.cs108.letuscook.util.Identifier;
import ch.unibas.dmi.dbis.cs108.letuscook.util.IdentifierFactory;
import ch.unibas.dmi.dbis.cs108.letuscook.util.MalformedException;
import ch.unibas.dmi.dbis.cs108.letuscook.util.Messenger;
import ch.unibas.dmi.dbis.cs108.letuscook.util.Schedule;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Holds records, accepts incoming connections, and manages actors.
 */
public class Server {

	private static final String REQUEST_CONSUMER_THREAD_NAME = "requests";

	private static volatile Server the;

	private final IdentifierFactory identifierFactory = new IdentifierFactory();

	/**
	 * The highscores.
	 */
	private final Highscores highscores;

	/**
	 * All the records stored on the server.
	 */
	private final List<Record> records = Collections.synchronizedList(new ArrayList<>());

	/**
	 * All active or suspended actors.
	 */
	private final List<Actor> actors = Collections.synchronizedList(new ArrayList<>());

	/**
	 * All open lobbies.
	 */
	private final List<Lobby> lobbies = Collections.synchronizedList(new ArrayList<>());

	/**
	 * The request queue.
	 */
	private final LinkedBlockingQueue<Request> requests = new LinkedBlockingQueue<>();

	/**
	 * The port.
	 */
	private final int port;

	/**
	 * The server socket through which the server accepts new connections.
	 */
	private ServerSocket serverSocket;

	/**
	 * Accepts incoming connections.
	 */
	private Thread connector;

	/**
	 * Periodically check that connections are still intact.
	 */
	private Schedule heartbeats;

	/**
	 * Consumes requests.
	 */
	private Thread requestConsumer;

	/**
	 * Create a server.
	 *
	 * @param port the port of the server.
	 */
	public Server(int port) {
		assert Server.the == null;

		this.port = port;
		this.highscores = new Highscores();

		Server.the = this;
	}

	public static Server the() {
		assert Server.the != null : "server not initialized";

		return Server.the;
	}

	public Highscores getHighscores() {
		return this.highscores;
	}

	public Identifier nextIdentifier() {
		return this.identifierFactory.next();
	}

	/**
	 * Start the server. On failure, logs the error and does nothing.
	 */
	public void start() {
		assert this.serverSocket == null : "serverSocket not null";
		assert this.connector == null : "connector not null";
		assert this.heartbeats == null : "heartbeats not null";

		try {
			this.serverSocket = new ServerSocket(this.port);
		} catch (IOException | SecurityException e) {
			Messenger.error(e, "Cannot start the server. Is the port occupied? - aborting start()");
			return;
		} catch (IllegalArgumentException e) {
			Messenger.error(e, "Port is outside the valid range of values - aborting start()");
			return;
		}

		this.startRequestConsumer();
		this.startHeartbeats();
		this.startConnector();

		Messenger.info("Started");
	}

	/**
	 * Stop the server.
	 */
	public void stop() {
		assert this.connector != null : "connector is null";
		assert this.serverSocket != null : "serverSocket is null";
		assert this.heartbeats != null : "heartbeats is null";
		assert this.requestConsumer != null : "requestConsumer is null";

		this.connector.interrupt();
		try {
			this.serverSocket.close();
		} catch (IOException e) {
			Messenger.warn(e, "An IO error occurred while closing the server socket - ignoring");
		}
		this.serverSocket = null;
		this.connector = null;

		this.heartbeats.stop();
		this.heartbeats = null;

		this.requestConsumer.interrupt(); // FIXME: This will probably not work because the thread is blocking.

		this.clearLobbies();
		this.clearActors();
	}

	/**
	 * Start the connector thread.
	 */
	private void startConnector() {
		assert this.connector == null : "connector not null";

		this.connector = new Thread(() -> {
			while (!Thread.currentThread().isInterrupted()) {
				try {
					Socket socket = this.serverSocket.accept();

					/* Revive dead actor or create new one. */
					synchronized (this.actors) {
						var existingDeadActorOrEmpty = this.findActorByAddress(
							Connection.addressForSocket(socket), true);
						if (existingDeadActorOrEmpty.isPresent()) {
							existingDeadActorOrEmpty.get().replaceConnection(socket);
							this.queueCommand(
								new Request(existingDeadActorOrEmpty.get(), new RefreshCommand()));
						} else {
							Actor actor = new Actor();
							actor.createConnection(socket);
							this.addActor(actor);
						}
					}

				} catch (SocketException e) {
					if (Thread.currentThread().isInterrupted()) {
						Messenger.debug(
							"Server socket was closed, thread is interrupted - stopping connector");
					} else {
						Messenger.error("Cannot access server socket - stopping connector");
					}
					break;
				} catch (IOException e) {
					Messenger.error(e,
						"An IO error occurred while awaiting connections - stopping connector");
					break;
				} catch (SecurityException e) {
					Messenger.error(e, "Cannot establish connection - stopping connector");
					break;
				}
			}
		}, "connector");

		this.connector.setDaemon(true);
		this.connector.start();
	}

	/**
	 * Start the heartbeats schedule.
	 */
	private void startHeartbeats() {
		assert this.heartbeats == null : "heartbeats not null";

		this.heartbeats = Schedule.withFixedDelay(() -> {
			synchronized (this.actors) {
				for (var actor : this.actors) {
					if (actor.connection().isEmpty()) {
						continue;
					}

					Connection connection = actor.connection().orElseThrow();

					if (connection.isAwaitingPong()) {
						Messenger.error("Connection timed out - closing actor");
						this.queueCommand(new Request(actor, new DisappearCommand()));
						continue;
					}

					connection.ping();
				}
			}
			return null;
		}, Main.PONG_WAIT_MS, "heartbeats");
	}

	/**
	 * Start the requests thread.
	 */
	private void startRequestConsumer() {
		assert this.requestConsumer == null : "requestConsumer not null";

		this.requestConsumer = new Thread(() -> {
			while (!Thread.currentThread().isInterrupted()) {
				try {
					this.consumeRequest(this.requests.take());
				} catch (InterruptedException e) {
					Messenger.debug(
						"Request consumer interrupted - stopping request consumer");
				}
			}

			Messenger.debug("Request consumer stopped");
		}, Server.REQUEST_CONSUMER_THREAD_NAME);

		this.requestConsumer.setDaemon(true);
		this.requestConsumer.start();
	}

	/**
	 * Send commands to all actors.
	 *
	 * @param commands the commands to send.
	 */
	public void broadcastToActorsWithRecord(Command... commands) {
		synchronized (this.actors) {
			for (var actor : this.actors) {
				if (actor.record().isPresent()) {
					actor.sendCommands(commands);
				}
			}
		}
	}

	/**
	 * Send commands to all actors except the exception.
	 *
	 * @param exception the actor to skip.
	 * @param commands  the commands to send.
	 */
	void broadcastToOtherActorsWithRecord(Actor exception, Command... commands) {
		synchronized (this.actors) {
			for (var actor : this.actors) {
				if (actor != exception) {
					actor.sendCommands(commands);
				}
			}
		}
	}

	/**
	 * Check if a nickname is occupied by a record.
	 *
	 * @param nickname the nickname to check.
	 * @return whether the nickname is occupied.
	 */
	public boolean nicknameOccupied(String nickname) {
		synchronized (this.records) {
			for (var record : this.records) {
				if (record.getNickname().equals(nickname)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Add an actor to the server.
	 *
	 * @param actor the actor.
	 */
	private void addActor(Actor actor) {
		assert actor != null : "actor is null";

		synchronized (this.actors) {
			this.actors.add(actor);
		}
	}

	private Optional<Actor> findActorByIdentifier(Identifier identifier) {
		assert identifier != null : "identifier is null";
		assert !identifier.isNone() : "identifier is NONE";

		synchronized (this.actors) {
			for (var actor : this.actors) {
				if (actor.getIdentifier().equals(identifier)) {
					return Optional.of(actor);
				}
			}
		}

		return Optional.empty();
	}

	private Optional<Actor> findActorByAddress(String address, boolean requireDead) {
		assert address != null : "address is null";

		synchronized (this.actors) {
			for (var actor : this.actors) {
				if (actor.getAddress().equals(address) && (!requireDead || actor.connection()
					.orElseThrow().isDead())) {
					return Optional.of(actor);
				}
			}
		}

		return Optional.empty();
	}

	/**
	 * Destroy and remove the provided actor from the server. <b>Once called, the actor must be
	 * discarded.</b> Also see {@link Actor#destroy()}.
	 *
	 * @param actor the actor.
	 */
	public void destroyAndRemoveActor(Actor actor) {
		assert actor != null : "actor is null";

		if (actor.member().isPresent()) {
			this.removeActorFromLobby(actor);
		}

		if (actor.record().isPresent()) {
			this.records.remove(actor.record().orElseThrow());
		}

		actor.destroy();
		synchronized (this.actors) {
			this.actors.remove(actor);
		}
	}

	/**
	 * Find a record by its address, or if it doesn't exist, create one with the provided address
	 * and nickname suggestion.
	 *
	 * @param address            the record's address.
	 * @param nicknameSuggestion a nickname suggestion if the record is newly created. May be
	 *                           <code>null</code> if the record is known to exist.
	 * @return the record.
	 */
	public Record findOrCreateRecord(String address, String nicknameSuggestion) {
		Record record = null;

		synchronized (this.records) {
			if (address != null) {
				for (var existingRecord : this.records) {
					if (existingRecord.getAddress().equals(address)) {
						record = existingRecord;
						break;
					}
				}
			}

			if (record == null) {
				assert nicknameSuggestion != null
					: "cannot find or create unknown record with null nickname suggestion";

				record = new Record(this.generateUniqueNickname(nicknameSuggestion), address);
				this.records.add(record);
			}
		}

		return record;
	}

	public Optional<Lobby> findLobby(String name) {
		assert name != null : "name is null";

		synchronized (this.lobbies) {
			for (var lobby : this.lobbies) {
				if (lobby.getName().equals(name)) {
					return Optional.of(lobby);
				}
			}
		}

		return Optional.empty();
	}

	public void createLobbyThenAddActor(String nameSuggestion, Actor actor) {
		String name = nameSuggestion;
		Lobby lobby;

		synchronized (this.lobbies) {
			/*
			 * Ensure lobby name is unique.
			 */
			if (this.findLobby(name).isPresent()) {
				int suffix = 0;
				do {
					name = nameSuggestion + "_" + ++suffix;
				} while (this.findLobby(name).isPresent());
			}

			lobby = new Lobby(true, name);
			this.lobbies.add(lobby);
		}

		this.broadcastToActorsWithRecord(lobby.representedAsCommands());
		Messenger.info(
			"'" + actor.record().orElseThrow().getNickname() + "' created lobby '" + lobby.getName()
				+ "'");

		this.addActorToLobby(actor, lobby);
	}

	public void removeLobby(Lobby lobby) {
		assert lobby != null : "lobby is null";

		synchronized (this.lobbies) {
			assert this.lobbies.contains(lobby) : "unknown lobby";
			assert lobby.isEmpty() : "lobby not empty";

			try {
				this.broadcastToActorsWithRecord(new LobbyCloseCommand(lobby.getName()));
			} catch (MalformedException e) {
				assert false : "lobby returned malformed name";
			}

			Messenger.info("'" + lobby.getName() + "' closed.");

			this.lobbies.remove(lobby);
		}
	}

	public void queueCommand(Request request) {
		assert request != null : "request is null";

		try {
			this.requests.put(request);
		} catch (InterruptedException e) {
			Messenger.warn("Interrupted while waiting to queue a request - ignoring");
		}
	}

	/**
	 * Consume a request.
	 *
	 * @param request the request to consume.
	 */
	public void consumeRequest(Request request) {
		assert Thread.currentThread().getName()
			.equals(Server.REQUEST_CONSUMER_THREAD_NAME) : "not in requests thread";
		assert request != null : "request is null";

		var actor = request.getActor();
		var command = request.getCommand();
		var requestLobby = request.getLobby();

		if (command instanceof IntroduceCommand introduceCommand) {
			/* Log-in new actors. */
			if (actor.record().isEmpty()) {
				this.attachActorToRequestedRecordAndSendRefresh(actor,
					introduceCommand.getNickname());
				return;
			}

			/* Change the nickname of existing actors. */
			if (!actor.record().orElseThrow().getNickname()
				.equals(introduceCommand.getNickname())) {
				actor.record().orElseThrow()
					.setNickname(
						this.generateUniqueNickname(introduceCommand.getNickname()));
				try {
					this.broadcastToActorsWithRecord(Command.withSubject(actor.getIdentifier(),
						new IntroduceCommand(
							actor.record().orElseThrow().getNickname())));
				} catch (MalformedException e) {
					assert false : "record returned malformed nickname";
				}
			}
			return;
		}

		if (command instanceof GameForceStopCommand) {
			if (command.getSubject().isSome()) {
				Messenger.warn("Ignoring attempt by actor with identifier '" + command.getSubject()
					+ "' to force-stop the game.");
				return;
			}
			requestLobby.stopGame(false);
			try {
				this.broadcastToActorsWithRecord(new GameTimeCommand(requestLobby.getName(), 0));
			} catch (MalformedException e) {
				assert false : "lobby returned malformed name";
			}
			Messenger.info("Announced end of game");
			return;
		}

		if (command instanceof DisappearCommand) {
			this.logoutThenDestroyAndRemoveActor(actor);
			return;
		}

		if (command instanceof RefreshCommand) {
			this.refreshActor(actor);
			return;
		}

		/*
		 * At this point, commands from actors that aren't attached to a record are
		 * disallowed.
		 */
		if (actor.record().isEmpty()) {
			Messenger.warn(
				"Ignoring request by record-less actor with address " + actor.getAddress());
			return;
		}

		if (command instanceof YellCommand yellCommand) {
			this.broadcastToActorsWithRecord(yellCommand);
			return;
		}

		if (command instanceof ChatCommand chatCommand) {
			if (chatCommand.isWhispered()) {
				var recipientOrEmpty = this.findActorByIdentifier(chatCommand.getRecipient());
				if (recipientOrEmpty.isEmpty()) {
					Messenger.warn("Ignoring whisper with unknown recipient");
				} else {
					recipientOrEmpty.orElseThrow().sendCommands(chatCommand);
					actor.sendCommands(chatCommand); /* Echo. */
				}
			} else if (actor.member().isPresent()) {
				actor.member().orElseThrow().getLobby().broadcast(chatCommand);
			} else {
				Messenger.warn("Ignoring chat from actor outside lobby");
			}
			return;
		}

		if (command instanceof LobbyOpenCommand lobbyOpenCommand) {
			if (actor.member().isPresent()) {
				Messenger.warn(
					"Ignoring request by '" + actor.record().orElseThrow().getNickname()
						+ "' to open lobby '"
						+ lobbyOpenCommand.getLobbyName()
						+ "' - actor is already member of a lobby");
				return;
			}
			this.createLobbyThenAddActor(lobbyOpenCommand.getLobbyName(), actor);
			return;
		}

		if (command instanceof LobbyJoinCommand lobbyJoinCommand) {
			if (actor.member().isPresent()) {
				Messenger.warn(
					"Ignoring request by '" + actor.record().orElseThrow().getNickname()
						+ "' to join lobby '"
						+ lobbyJoinCommand.getLobbyName()
						+ "' - actor is already member of a lobby");
				return;
			}
			var lobbyOrEmpty = this.findLobby(lobbyJoinCommand.getLobbyName());
			if (lobbyOrEmpty.isPresent()) {
				this.addActorToLobby(actor, lobbyOrEmpty.orElseThrow());
			} else {
				Messenger.warn(
					"Ignoring join to non-existent lobby '"
						+ lobbyJoinCommand.getLobbyName()
						+ "' by actor '" + actor.record().orElseThrow().getNickname() + "'");
			}
			return;
		}

		/*
		 * For the following requests, the actor must be in a lobby.
		 */
		if (actor.member().isEmpty()) {
			Messenger.warn(
				"Ignoring request '" + request.getCommand() + "' by '" + actor.record()
					.orElseThrow()
					.getNickname()
					+ "' - actor isn't member of a lobby");
			return;
		}

		if (command instanceof LobbyLeaveCommand) {
			this.removeActorFromLobby(actor);
			return;
		}

		if (command instanceof LobbyReadyCommand lobbyReadyCommand) {
			Member member = actor.member().orElseThrow();
			if (member.isReady() == lobbyReadyCommand.getReady()) {
				Messenger.warn("Ignoring request '" + request.getCommand() + "' by '" +
					actor.record().orElseThrow().getNickname() + "' - ready state already set");
				return;
			}
			member.setReady(lobbyReadyCommand.getReady());
			member.getLobby().broadcast(Command.withSubject(actor.getIdentifier(),
				new LobbyReadyCommand(member.isReady())));
			Messenger.info(
				"Set ready state of " + actor.record().orElseThrow().getNickname() + " to "
					+ lobbyReadyCommand.getReady());
			return;
		}

		if (command instanceof GameRequestStartCommand) {
			if (actor.member().orElseThrow().getLobby().gameIsRunning()) {
				Messenger.warn("Ignoring request to start game - game is already running");
				return;
			}
			if (!actor.member().orElseThrow().getLobby().isEveryoneReady()) {
				Messenger.warn("Ignoring request to start game - not all members ready");
				return;
			}

			Lobby lobby = actor.member().orElseThrow().getLobby();

			/* Start game. */
			lobby.startGame(Game.DURATION_SECONDS * Game.TPS);
			this.broadcastToActorsWithRecord(lobby.game().orElseThrow().timeRepresentedAsCommand());
			Messenger.info("Announced start of game");

			/* Schedule game end. */
			Schedule.waitWhile(() -> lobby.game().orElseThrow().ticksUntilGameOver.get() > 0,
				() -> {
					this.queueCommand(new Request(lobby, new GameForceStopCommand()));
					return null;
				},
				1000, "awaitGameOver");

			/* Queue participation request on behalf of all members. */
			// FIXME: I seem to remember there being a problem with this, but I can't find one...
			synchronized (this.actors) {
				for (var memberActor : lobby.getActors()) {
//					memberActor.member().orElseThrow().setPlayer(new Player());
					this.queueCommand(new Request(memberActor, new GameParticipateCommand()));
				}
			}
//			this.broadcastToActorsWithRecord(lobby.game().orElseThrow().timeRepresentedAsCommand());

			return;
		}

		/*
		 * For the following requests, the actor's game must be running.
		 */
		if (!actor.member().orElseThrow().getLobby().gameIsRunning()) {
			Messenger.warn(
				"Ignoring request '" + request.getCommand() + "' by '" + actor.record()
					.orElseThrow()
					.getNickname()
					+ "' - game is not running");
			return;
		}

		if (command instanceof GameParticipateCommand) {
			Member member = actor.member().orElseThrow();

			assert member.isReady();
			assert member.player().isEmpty();

			this.consumeRequest(new Request(actor, new LobbyReadyCommand(false)));

			member.setPlayer(new Player());

			member.getLobby()
				.broadcastToOthers(
					member.player().orElseThrow().positionRepresentedAsCommand(actor),
					actor);
			actor.sendCommands(member.getLobby().game().orElseThrow().representedAsCommands());

			Messenger.info("'" + actor.record().orElseThrow().getNickname()
				+ "' is now participating in the game");
			return;
		}

		if (command instanceof PlayerPositionCommand playerPositionCommand) {
			Player player = actor.member().orElseThrow().player().orElseThrow();

			actor.member().orElseThrow().getLobby().game()
				.orElseThrow()
				.movePlayerBy(player,
					new Units(
						playerPositionCommand.getCoords().getX().u() - player.getRect()
							.getX().u()),
					new Units(
						playerPositionCommand.getCoords().getY().u() - player.getRect()
							.getY().u()));

			Units error = Coords.distance(player.getRect().asCoords(),
				playerPositionCommand.getCoords());

			var positionCommand = player.positionRepresentedAsCommand(actor);
			if (error.u() > 1e-10) {
				Messenger.warn(
					"Player moved illegally - sending correction (off by " + error.u() + " units)");
				actor.sendCommands(positionCommand);
			}
			actor.member().orElseThrow().getLobby().broadcastToOthers(positionCommand, actor);

			Messenger.debug(
				"'" + actor.record().orElseThrow().getNickname() + "' moved to "
					+ player.getRect());

			return;
		}

		if (command instanceof PlayerInteractCommand) {
			Player player = actor.member().orElseThrow().player().orElseThrow();

			Lobby lobby = actor.member().orElseThrow().getLobby();

			var workbenchOrEmpty = lobby.game().orElseThrow().interact(player);

			if (workbenchOrEmpty.isEmpty()) {
				Messenger.info("Ignoring action by '" + actor.record().orElseThrow().getNickname()
					+ "' - not in reach of a workbench");
				return;
			}

			Workbench workbench = workbenchOrEmpty.get();
			lobby.broadcast(player.holdingRepresentedAsCommand(actor));
			lobby.broadcast(workbench.representedAsCommands());

			Messenger.info("'" + actor.record().orElseThrow().getNickname() + "' is now holding: "
				+ player.getHolding());
			Messenger.info(
				"Workbench " + workbench.getIdentifier() + " is now " + workbench.getState()
					+ " with contents: " + workbench.peekContents());

			return;
		}

		// FIXME: This logic doesn't quite work like we want it to. An unsupported
		// request would most likely produce an error in one of the checks above.
		Messenger.warn(
			"Ignoring unsupported request by '" + actor.record().orElseThrow().getNickname() + "': "
				+ command);
	}

	/**
	 * Attach this actor to an existing record. Doing so allows the actor to interact with the
	 * server on behalf of the person represented by the record.
	 *
	 * @param record the record to attach this actor to.
	 */
	public void attachActorToRecord(Actor actor, Record record) {
		assert actor != null : "actor is null";
		assert record != null : "record is null";

		actor.setRecord(record);
	}

	public void addActorToLobby(Actor actor, Lobby lobby) {
		assert actor != null : "actor is null";
		assert lobby != null : "lobby is null";

		actor.setLobby(lobby);
		lobby.addMember(actor);

		try {
			var joinCommand = new LobbyJoinCommand(lobby.getName());
			joinCommand.setSubject(actor.getIdentifier());
			this.broadcastToActorsWithRecord(joinCommand);
		} catch (MalformedException e) {
			assert false : "lobby returned malformed name";
		}

		actor.sendCommands(lobby.readyStatesRepresentedAsCommands());

		if (lobby.gameIsRunning()) {
			actor.sendCommands(lobby.game().orElseThrow().representedAsCommands());
		}

		Messenger.info(
			"'" + actor.record().orElseThrow().getNickname() + "' joined lobby '" + lobby.getName()
				+ "'");
	}

	public void removeActorFromLobby(Actor actor) {
		assert actor != null : "actor is null";
		assert actor.member().isPresent() : "not member of a lobby";
		assert actor.member().orElseThrow().getLobby()
			.contains(actor) : "lobby does not contain this actor";

		Lobby lobby = actor.member().orElseThrow().getLobby();

		lobby.removeMember(actor);
		actor.clearLobby();

		var leaveCommand = new LobbyLeaveCommand();
		leaveCommand.setSubject(actor.getIdentifier());
		this.broadcastToActorsWithRecord(leaveCommand);

		Messenger.info(
			"'" + actor.record().orElseThrow().getNickname() + "' left lobby '" + lobby.getName()
				+ "'");

		if (lobby.isEmpty()) {
			this.removeLobby(lobby);
		}
	}

	private void clearActors() {
		synchronized (this.actors) {
			for (var actor : this.actors) {
				actor.sendCommands(new DisappearCommand());
				this.destroyAndRemoveActor(actor);
			}
		}
	}

	private void clearLobbies() {
		synchronized (this.lobbies) {
			for (var lobby : this.lobbies) {
				this.removeLobby(lobby);
			}
		}
	}

	private void attachActorToRequestedRecordAndSendRefresh(Actor actor,
		String nicknameSuggestion) {

		if (actor.record().isEmpty()) {
			Record record = this.findOrCreateRecord(
				this.findActorByAddress(actor.getAddress(), false).isPresent() ? null
					: actor.getAddress(),
				nicknameSuggestion);

			this.attachActorToRecord(actor, record);

			this.broadcastToOtherActorsWithRecord(actor, actor.representedAsCommands());
		}

		this.refreshActor(actor);
	}

	private void logoutThenDestroyAndRemoveActor(Actor actor) {
		/* Announce. */
		var logoutCommand = new DisappearCommand();
		logoutCommand.setSubject(actor.getIdentifier());

		Messenger.info("'" + actor.record().orElseThrow().getNickname() + "' disconnected");

		this.destroyAndRemoveActor(actor);

		this.broadcastToActorsWithRecord(logoutCommand);
	}

	public void refreshActor(Actor actor) {
		actor.sendCommands(Command.withSubject(actor.getIdentifier(), new RefreshCommand()));

		/* General state. */
		actor.sendCommands(this.representedAsCommands());

		/* Lobby-dependant state. */
		if (actor.member().isPresent()) {
			var lobby = actor.member().get().getLobby();

			actor.sendCommands(lobby.readyStatesRepresentedAsCommands());

			if (lobby.gameIsRunning()) {
				actor.sendCommands(lobby.game().orElseThrow().representedAsCommands());
			}
		}
	}

	public String generateUniqueNickname(String suggestion) {
		String nickname = suggestion;

		if (Server.the().nicknameOccupied(nickname)) {
			String baseNickname = nickname + "_";
			int suffix = 1;
			do {
				nickname = baseNickname + suffix++;
			} while (Server.the().nicknameOccupied(nickname));
		}

		return nickname;
	}

	public Command[] representedAsCommands() {
		var commands = new ArrayList<Command>();

		/* Highscores. */
		commands.add(new HighscoresCommand(Server.the().getHighscores()));

		/* Lobbies. */
		synchronized (this.lobbies) {
			for (var lobby : this.lobbies) {
				commands.addAll(Arrays.asList(lobby.representedAsCommands()));
			}
		}

		/* Actors. */
		synchronized (this.actors) {
			for (var actor : this.actors) {
				commands.addAll(Arrays.asList(actor.representedAsCommands()));
			}
		}

		return commands.toArray(new Command[0]);
	}
}
