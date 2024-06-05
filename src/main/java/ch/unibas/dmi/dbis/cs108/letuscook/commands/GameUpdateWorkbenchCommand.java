package ch.unibas.dmi.dbis.cs108.letuscook.commands;

import ch.unibas.dmi.dbis.cs108.letuscook.orders.Stack;
import ch.unibas.dmi.dbis.cs108.letuscook.orders.State;
import ch.unibas.dmi.dbis.cs108.letuscook.util.Identifier;
import ch.unibas.dmi.dbis.cs108.letuscook.util.MalformedException;

/**
 * Represents a command sent by the server to indicate the state of a workbench. Contains
 * information about the workbench identifier, state, and contents.
 */

public class GameUpdateWorkbenchCommand extends Command {

	/**
	 * The keyword used to identify this command.
	 */
	public static final String KEYWORD = "WORKBENCH";

	/**
	 * The workbench identifier.
	 */
	private final Identifier identifier;

	/**
	 * The workbench state.
	 */
	private final State state;

	/**
	 * The workbench contents.
	 */
	private final Stack contents;

	/**
	 * Ticks until state change.
	 */
	private final int ticksUntilStateChange;

	/**
	 * Constructs a new WorkbenchCommand.
	 */
	public GameUpdateWorkbenchCommand(Identifier identifier, State state, Stack contents,
		int ticksUntilStateChange) {
		this.identifier = identifier;
		this.state = state;
		this.contents = contents;
		this.ticksUntilStateChange = ticksUntilStateChange;
	}

	/**
	 * Creates an anonymous instance of this command, given a string of arguments.
	 *
	 * @param arguments the arguments
	 * @return the command.
	 * @throws MalformedException if the arguments are malformed.
	 */
	public static GameUpdateWorkbenchCommand fromArguments(String arguments)
		throws MalformedException {
		if (arguments == null) {
			throw new MalformedException("arguments is null");
		}

		var isct /* identifier, state, contents, ticks */ = arguments.split(" ", 4);
		if (isct.length != 4) {
			throw new MalformedException("malformed arguments");
		}

		return new GameUpdateWorkbenchCommand(Identifier.fromString(isct[0]),
			State.fromString(isct[1]), Stack.fromString(isct[2]), Integer.parseInt(isct[3]));
	}

	/**
	 * @return the workbench identifier.
	 */
	public Identifier getWorkbenchIdentifier() {
		return this.identifier;
	}

	/**
	 * @return the workbench state.
	 */
	public State getState() {
		return this.state;
	}

	/**
	 * @return the workbench contents.
	 */
	public Stack getContents() {
		return this.contents;
	}

	/**
	 * @return the ticks until finished.
	 */
	public int getTicksUntilStateChange() {
		return this.ticksUntilStateChange;
	}

	/**
	 * @return a textual representation of this command.
	 */
	@Override
	public String toString() {
		return super.toString() + GameUpdateWorkbenchCommand.KEYWORD + " "
			+ this.getWorkbenchIdentifier()
			+ " "
			+ this.getState() + " " + this.getContents() + " " + this.getTicksUntilStateChange();
	}
}
