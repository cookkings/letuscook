package ch.unibas.dmi.dbis.cs108.letuscook.commands;

import ch.unibas.dmi.dbis.cs108.letuscook.orders.Order;
import ch.unibas.dmi.dbis.cs108.letuscook.util.Identifier;
import ch.unibas.dmi.dbis.cs108.letuscook.util.MalformedException;

public class GameOrderCommand extends Command {

	/**
	 * The keyword used to identify this command.
	 */
	public static final String KEYWORD = "ORDER";

	/**
	 * The workbench identifier.
	 */
	private final Identifier identifier;

	private final Order order;

	/**
	 * Constructs a new WorkbenchCommand.
	 */
	public GameOrderCommand(Identifier identifier, Order order) {
		this.identifier = identifier;
		this.order = order;
	}

	/**
	 * Creates an anonymous instance of this command, given a string of arguments.
	 *
	 * @param arguments the arguments
	 * @return the command.
	 * @throws MalformedException if the arguments are malformed.
	 */
	public static GameOrderCommand fromArguments(String arguments) throws MalformedException {
		if (arguments == null) {
			throw new MalformedException("arguments is null");
		}

		var io /* identifier, order */ = arguments.split(" ", 2);
		if (io.length != 2) {
			throw new MalformedException("malformed arguments");
		}

		return new GameOrderCommand(Identifier.fromString(io[0]), Order.fromString(io[1]));
	}

	/**
	 * @return the workbench identifier.
	 */
	public Identifier getWorkbenchIdentifier() {
		return this.identifier;
	}

	/**
	 * @return the order.
	 */
	public Order getOrder() {
		return this.order;
	}

	/**
	 * @return a textual representation of this command.
	 */
	@Override
	public String toString() {
		return super.toString() + GameOrderCommand.KEYWORD + " " + this.getWorkbenchIdentifier()
			+ " " + this.getOrder();
	}
}
