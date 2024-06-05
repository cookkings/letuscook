package ch.unibas.dmi.dbis.cs108.letuscook.server;

import ch.unibas.dmi.dbis.cs108.letuscook.commands.Command;

/**
 * The Request class represents a request received by the server from an actor.
 */
public class Request {

	/**
	 * The actor associated with the request.
	 */
	private final Actor actor;

	/**
	 * The command associated with the request.
	 */
	private final Command command;

	/**
	 * The lobby associated with the request.
	 */
	private final Lobby lobby;

	/**
	 * Constructs a new Request with the specified actor and command.
	 *
	 * @param actor   The actor making the request.
	 * @param command The command associated with the request.
	 */
	public Request(Actor actor, Command command) {
		assert actor != null : "actor is null";
		assert command != null : "command is null";

		this.actor = actor;
		this.command = command;
		this.lobby = null;
	}

	public Request(Lobby lobby, Command command) {
		assert lobby != null : "lobby is null";
		assert command != null : "command is null";

		this.actor = null;
		this.command = command;
		this.lobby = lobby;
	}

	/**
	 * Gets the actor associated with the request.
	 *
	 * @return The actor.
	 */
	public Actor getActor() {
		return this.actor;
	}

	/**
	 * Gets the command associated with the request.
	 *
	 * @return The command.
	 */
	public Command getCommand() {
		return this.command;
	}

	public Lobby getLobby() {
		return this.lobby;
	}
}
