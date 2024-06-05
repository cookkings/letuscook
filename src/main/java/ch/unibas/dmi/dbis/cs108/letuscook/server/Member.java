package ch.unibas.dmi.dbis.cs108.letuscook.server;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * An {@link Actor}'s representation for a {@link Lobby}.
 */
public class Member {

	/**
	 * The lobby this member is in.
	 */
	private final Lobby lobby;

	/**
	 * This members' ready state.
	 */
	private final AtomicBoolean ready = new AtomicBoolean();

	/**
	 * The player.
	 */
	private Player player;

	/**
	 * Create a member.
	 *
	 * @param lobby the lobby this member is part of.
	 */
	public Member(Lobby lobby) {
		assert lobby != null : "lobby is null";

		this.lobby = lobby;
	}

	/**
	 * @return whether this member is ready to start a game.
	 */
	public boolean isReady() {
		return this.ready.get();
	}

	/**
	 * Set this member's ready state.
	 *
	 * @param ready whether this member is ready to start a game.
	 */
	public void setReady(boolean ready) {
		this.ready.set(ready);
	}

	/**
	 * @return this member's player.
	 */
	public synchronized Optional<Player> player() {
		if (this.player == null) {
			return Optional.empty();
		}

		return Optional.of(this.player);
	}

	/**
	 * Set this member's player.
	 *
	 * @param player the player.
	 */
	public synchronized void setPlayer(Player player) {
		assert this.player().isEmpty();

		this.player = player;
	}

	/**
	 * Clear this member's player.
	 */
	public synchronized void clearPlayer() {
		this.player = null;
	}

	/**
	 * @return the lobby this member is part of.
	 */
	public Lobby getLobby() {
		return this.lobby;
	}
}
