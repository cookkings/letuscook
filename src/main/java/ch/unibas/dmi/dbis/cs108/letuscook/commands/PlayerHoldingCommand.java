package ch.unibas.dmi.dbis.cs108.letuscook.commands;

import ch.unibas.dmi.dbis.cs108.letuscook.orders.Stack;
import ch.unibas.dmi.dbis.cs108.letuscook.util.MalformedException;

/**
 * Represents a command sent by the server to indicate the current holding stack. Contains
 * information about the items in the stack.
 */
public class PlayerHoldingCommand extends Command {

	public static final String KEYWORD = "HAND";

	private final Stack stack;

	/**
	 * Construct a new HoldingCommand.
	 *
	 * @param stack the stack.
	 */
	public PlayerHoldingCommand(Stack stack) {
		assert stack != null;

		this.stack = stack;
	}

	/**
	 * Creates an anonymous instance of this command, given a string of arguments.
	 *
	 * @param arguments the arguments
	 * @return the command.
	 * @throws MalformedException if the arguments are malformed.
	 */
	public static PlayerHoldingCommand fromArguments(String arguments) throws MalformedException {
		return new PlayerHoldingCommand(Stack.fromString(arguments));
	}

	/**
	 * @return the stack.
	 */
	public Stack getStack() {
		return this.stack;
	}

	/**
	 * @return a textual representation of this command.
	 */
	@Override
	public String toString() {
		return super.toString() + PlayerHoldingCommand.KEYWORD + " " + this.stack;
	}
}
