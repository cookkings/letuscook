package ch.unibas.dmi.dbis.cs108.letuscook.server;

import ch.unibas.dmi.dbis.cs108.letuscook.commands.Command;
import ch.unibas.dmi.dbis.cs108.letuscook.commands.GameTimeCommand;
import ch.unibas.dmi.dbis.cs108.letuscook.commands.HighscoresCommand;
import ch.unibas.dmi.dbis.cs108.letuscook.commands.LobbyOpenCommand;
import ch.unibas.dmi.dbis.cs108.letuscook.commands.LobbyReadyCommand;
import ch.unibas.dmi.dbis.cs108.letuscook.util.MalformedException;
import ch.unibas.dmi.dbis.cs108.letuscook.util.Messenger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * A group of actors (members).
 */
public class Lobby {

	/**
	 * Whether this lobby is server-side.
	 */
	public final boolean isServerSide;

	/**
	 * The name of this lobby.
	 */
	private final String name;

	/**
	 * The actors that are members of this lobby.
	 */
	private final List<Actor> actors = Collections.synchronizedList(new ArrayList<Actor>());

	boolean tutorial;

	/**
	 * Whether a game is running in this lobby.
	 */
	boolean gameIsRunning;

	/**
	 * The latest score achieved in this lobby. This field is only used client-side.
	 */
	private Integer clientLatestScore = null;

	/**
	 * The game.
	 */
	private Game game;

	/**
	 * Create a lobby.
	 *
	 * @param isServerSide whether this lobby is server-side.
	 * @param name         the lobby name.
	 */
	public Lobby(boolean isServerSide, String name) {
		assert name != null : "name is null";

		this.isServerSide = isServerSide;
		this.name = name;
	}

	public boolean isTutorial() {
		return tutorial;
	}

	public void setTutorial(boolean tutorial) {
		this.tutorial = tutorial;
	}

	/**
	 * @return this lobby's name.
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * @return the members.
	 */
	public Actor[] getActors() {
		return this.actors.toArray(new Actor[0]);
	}

	public Optional<Integer> getClientLatestScore() {
		return Optional.ofNullable(this.clientLatestScore);
	}

	/**
	 * @return whether the lobby is empty.
	 */
	public boolean isEmpty() {
		return this.actors.isEmpty();
	}

	/**
	 * Check if an actor is member of this lobby.
	 *
	 * @param actor the actor.
	 * @return whether the actor is member of this lobby.
	 */
	public boolean contains(Actor actor) {
		return this.actors.contains(actor);
	}

	/**
	 * Add a member to this lobby. <b>This method should only be used from within
	 * {@link Server#addActorToLobby(Actor, Lobby)}.</b>
	 *
	 * @param actor the actor joining the lobby.
	 */
	public void addMember(Actor actor) {
		assert actor != null : "actor is null";

		synchronized (this.actors) {
			assert !this.actors.contains(actor) : "members already contain this actor";

			this.actors.add(actor);
		}
	}

	/**
	 * Remove a member from this lobby. <b>This method should only be used from within
	 * {@link Server#removeActorFromLobby(Actor)}.</b>
	 *
	 * @param actor the actor leaving the lobby.
	 */
	public void removeMember(Actor actor) {
		assert actor != null : "actor is null";

		synchronized (this.actors) {
			assert this.actors.contains(actor) : "members do not contain this actor";

			this.actors.remove(actor);
		}
	}

	/**
	 * Send commands to all members.
	 *
	 * @param commands the commands to send.
	 */
	void broadcast(Command... commands) {
		synchronized (this.actors) { /* Reminder: Synchronizing things like these is absolutely still necessary! See how actors handle chat messages! */
			for (var member : this.actors) {
				member.sendCommands(commands);
			}
		}
	}

	/**
	 * Send a command to all members except the exception.
	 *
	 * @param command   the command to send.
	 * @param exception the actor to skip.
	 */
	void broadcastToOthers(Command command, Actor exception) {
		synchronized (this.actors) { /* Reminder: Synchronizing things like these is absolutely still necessary! See how actors handle chat messages! */
			for (var actor : this.actors) {
				if (actor != exception) {
					actor.sendCommands(command);
				}
			}
		}
	}

	public synchronized boolean gameIsRunning() {
		assert this.game == null || this.gameIsRunning : "game exists but is not marked as running";

		return this.gameIsRunning;
	}

	public synchronized boolean isEveryoneReady() {
		synchronized (this.actors) {
			for (Actor actor : this.actors) {
				if (!actor.member().orElseThrow().isReady()) {
					return false;
				}
			}
		}

		return true;
	}

	public synchronized void setGameIsRunning(boolean value) {
		this.gameIsRunning = value;
	}

	public synchronized void startGame(int ticksUntilFinished) {
		this.setGameIsRunning(true);

		this.game = new Game(this, ticksUntilFinished, null);
	}

	public Optional<Game> game() {
		return Optional.ofNullable(this.game);
	}

	public synchronized void stopGame(boolean keepGameIsRunningState) {
		if (!this.gameIsRunning()) {
			return;
		}

		this.setGameIsRunning(keepGameIsRunningState);

		if (this.game == null) {
			return;
		}

		this.clientLatestScore = this.game.getScore();
		this.game.stop();

		/* Submit score. */
		if (this.isServerSide) {
			List<String> names = new ArrayList<>();

			for (var actor : this.getActors()) {
				if (actor.member().orElseThrow().player().isPresent()) {
					names.add(actor.record().orElseThrow().getNickname());
				}
			}

			Server.the().getHighscores()
				.submitScore(new Highscore(names.toArray(new String[0]), this.game.getScore()));

			Messenger.info("Sent highscores.");
			Server.the()
				.broadcastToActorsWithRecord(new HighscoresCommand(Server.the().getHighscores()));
		}

		this.game = null;
		synchronized (this.actors) {
			for (var actor : this.actors) {
				actor.member().orElseThrow().clearPlayer();
			}
		}
	}

	public Command[] readyStatesRepresentedAsCommands() {
		var commands = new ArrayList<Command>();

		for (var actor : this.getActors()) {
			commands.add(Command.withSubject(actor.getIdentifier(),
				new LobbyReadyCommand(actor.member().orElseThrow().isReady())));
		}

		return commands.toArray(new Command[0]);
	}

	public Command[] representedAsCommands() {
		var commands = new ArrayList<Command>();

		try {
			commands.add(new LobbyOpenCommand(this.getName()));

			if (this.gameIsRunning()) {
				try {
					commands.add(new GameTimeCommand(this.getName(),
						this.game().orElseThrow().ticksUntilGameOver.get()));
				} catch (MalformedException e) {
					assert false : "lobby returned malformed name";
				}
			}
		} catch (MalformedException ignored) {
			assert false : "record returned malformed nickname";
		}

		return commands.toArray(new Command[0]);
	}
}
