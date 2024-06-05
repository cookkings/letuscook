package ch.unibas.dmi.dbis.cs108.letuscook.commands;

import ch.unibas.dmi.dbis.cs108.letuscook.util.MalformedException;

/**
 * Sent by the client to change its ready state. Sent by the server to indicate a change in ready
 * state.
 */
public class LobbyReadyCommand extends Command {

	/**
	 * The keyword used to identify this command.
	 */
	public static final String KEYWORD = "READY";

	/**
	 * The new value of the ready state.
	 */
	private final boolean ready;

	/**
	 * Constructs a new ReadyCommand with the specified ready state.
	 *
	 * @param ready the new ready state.
	 */
	public LobbyReadyCommand(boolean ready) {
		this.ready = ready;
	}

	/**
	 * Creates an anonymous instance of this command, given a string of arguments.
	 *
	 * @param arguments the arguments
	 * @return the command.
	 * @throws MalformedException if the arguments are malformed.
	 */
	public static LobbyReadyCommand fromArguments(String arguments)
		throws MalformedException {
		return new LobbyReadyCommand(Boolean.parseBoolean(arguments));
	}

	/**
	 * @return the ready state.
	 */
	public boolean getReady() {
		return this.ready;
	}

	/**
	 * @return a textual representation of this command.
	 */
	@Override
	public String toString() {
		return super.toString() + LobbyReadyCommand.KEYWORD + " " + this.getReady();
	}
}
