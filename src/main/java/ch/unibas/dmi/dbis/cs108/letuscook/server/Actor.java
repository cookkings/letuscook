package ch.unibas.dmi.dbis.cs108.letuscook.server;

import ch.unibas.dmi.dbis.cs108.letuscook.commands.Command;
import ch.unibas.dmi.dbis.cs108.letuscook.commands.IntroduceCommand;
import ch.unibas.dmi.dbis.cs108.letuscook.commands.LobbyJoinCommand;
import ch.unibas.dmi.dbis.cs108.letuscook.util.Connection;
import ch.unibas.dmi.dbis.cs108.letuscook.util.Identifier;
import ch.unibas.dmi.dbis.cs108.letuscook.util.MalformedException;
import ch.unibas.dmi.dbis.cs108.letuscook.util.Messenger;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * An abstraction to represent any entity interacting with the server.
 */
public final class Actor {

	/**
	 * This actor's identifier.
	 */
	private final Identifier identifier;

	/**
	 * A {@link Connection} that controls this actor.
	 */
	private Connection connection;

	/**
	 * A {@link Record} containing information about the person represented by this actor.
	 */
	private Record record;

	/**
	 * This actor's representation in their lobby.
	 */
	private Member member;

	/**
	 * Create an actor.
	 */
	Actor() {
		this.identifier = Server.the().nextIdentifier();
	}

	/**
	 * Create an actor with a specific identifier.
	 */
	public Actor(Identifier identifier) {
		this.identifier = identifier;
	}

	/**
	 * @return this actor's identifier.
	 */
	public Identifier getIdentifier() {
		return this.identifier;
	}

	/**
	 * Consume a command. This method is supplied to
	 * {@link Connection#Connection(String, Socket, Consumer)} during
	 * {@link #createConnection(Socket)}.
	 *
	 * @param command the command to consume.
	 */
	private void consumeCommand(Command command) {
		/*
		 * Add our identifier to this command.
		 */
		if (!command.getSubject().isNone()) {
			Messenger.warn("Receiving non-anonymous command \"" + command + "\" from client");
		}
		command.setSubject(this.identifier);

		/*
		 * Dispatch this command to the server thread.
		 */
		Server.the().queueCommand(new Request(this, command));
	}

	/**
	 * Set this actor's connection.
	 *
	 * @param socket the socket to use when creating this actor's {@link #connection}.
	 * @throws IOException see {@link Connection#Connection(String, Socket, Consumer)}.
	 */
	void createConnection(Socket socket) throws IOException {
		assert this.connection().isEmpty();
		assert socket != null : "socket is null";

		this.connection = new Connection("conn-" + this.getIdentifier(), socket,
			this::consumeCommand);
	}

	/**
	 * @return this actor's connection.
	 */
	Optional<Connection> connection() {
		return this.connection == null ? Optional.empty() : Optional.of(this.connection);
	}

	/**
	 * @return See {@link Connection#getAddress()}.
	 */
	String getAddress() {
		assert this.connection().isPresent() : "no connection";

		return this.connection.getAddress();
	}

	/**
	 * Send commands to this actor's underlying {@link #connection}.
	 *
	 * @param commands the commands to send.
	 */
	void sendCommands(Command... commands) {
		assert this.connection().isPresent() : "no connection";

		for (var command : commands) {
			assert command != null : "command is null";

			this.connection.sendCommandIfAlive(command);
		}
	}

	/**
	 * Destroy this actor's connection.
	 */
	void destroyAndRemoveConnection() {
		assert this.connection().isPresent() : "cannot destroy non-existent connection";

		this.connection.destroy();
		this.connection = null;
	}

	/**
	 * Replace an actor's existing dead connection.
	 *
	 * @param newSocket the new socket.
	 */
	void replaceConnection(Socket newSocket) throws IOException {
		this.destroyAndRemoveConnection();
		this.createConnection(newSocket);
	}

	/**
	 * @return this actor's record.
	 */
	public synchronized Optional<Record> record() {
		return this.record == null ? Optional.empty() : Optional.of(this.record);
	}

	/**
	 * Set the record this actor is attached to. <b>This method may only be used from within
	 * {@link Server#attachActorToRecord(Actor, Record)}.</b>
	 *
	 * @param record the record.
	 */
	public synchronized void setRecord(Record record) {
		assert this.record().isEmpty() : "actor already attached to record";
		assert record != null : "record is null";

		this.record = record;
	}

	/**
	 * @return this actor's member.
	 */
	public synchronized Optional<Member> member() {
		return this.member == null ? Optional.empty() : Optional.of(this.member);
	}

	/**
	 * Set the lobby this actor is a member of. <b>This method may only be used from within
	 * {@link Server#addActorToLobby(Actor, Lobby)}.</b>
	 *
	 * @param lobby the lobby.
	 */
	public synchronized void setLobby(Lobby lobby) {
		assert this.member().isEmpty() : "already member of a lobby";
		assert lobby != null : "lobby is null";
		assert !lobby.contains(this) : "lobby already contains this actor";

		this.member = new Member(lobby);
	}

	/**
	 * Clear this actor's lobby.
	 */
	public synchronized void clearLobby() {
		assert this.member().isPresent();

		this.member = null;
	}

	/**
	 * Close all resources used by this actor. <b>Once called, the actor must be discarded.</b>
	 */
	public void destroy() {
		if (this.connection().isPresent()) {
			this.destroyAndRemoveConnection();
		}
	}

	public Command[] representedAsCommands() {
		var commands = new ArrayList<Command>();

		if (this.record().isPresent()) {
			try {
				commands.add(Command.withSubject(this.getIdentifier(),
					new IntroduceCommand(this.record().orElseThrow().getNickname())));

				if (this.member().isPresent()) {
					commands.add(Command.withSubject(this.getIdentifier(), new LobbyJoinCommand(
						this.member().orElseThrow().getLobby().getName())));
				}
			} catch (MalformedException e) {
				assert false : "actor or lobby returned malformed nickname or lobby name";
			}
		}

		return commands.toArray(new Command[0]);
	}
}
