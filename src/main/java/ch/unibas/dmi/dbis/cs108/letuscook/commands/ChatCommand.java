package ch.unibas.dmi.dbis.cs108.letuscook.commands;

import ch.unibas.dmi.dbis.cs108.letuscook.util.Identifier;
import ch.unibas.dmi.dbis.cs108.letuscook.util.MalformedException;
import ch.unibas.dmi.dbis.cs108.letuscook.util.SanitizedLine;

/**
 * Sent by the client to submit a chat or whisper message. Sent by the server to deliver chat or
 * whisper messages.
 */
public class ChatCommand extends Command {

	public static final String KEYWORD = "SAY";

	private final Identifier recipient;

	private final SanitizedLine message;

	/**
	 * Constructs a new chat command with the specified message.
	 *
	 * @param message The message to be sent.
	 */
	public ChatCommand(String message) throws MalformedException {
		this.recipient = Identifier.NONE;
		this.message = SanitizedLine.createOrThrow(message);
	}

	/**
	 * Constructs a new whisper command with the specified message.
	 *
	 * @param recipient the recipient.
	 * @param message   The message to be sent.
	 */
	public ChatCommand(Identifier recipient, String message) throws MalformedException {
		if (recipient.isNone()) {
			throw new MalformedException("no recipient");
		}

		this.recipient = recipient;
		this.message = SanitizedLine.createOrThrow(message);
	}

	/**
	 * Creates an anonymous instance of this command, given a string of arguments.
	 *
	 * @param arguments the arguments
	 * @return the command.
	 * @throws MalformedException if the arguments are malformed.
	 */
	public static ChatCommand fromArguments(String arguments) throws MalformedException {
		if (arguments == null) {
			throw new MalformedException("arguments is null");
		}

		var recipientAndMessage = arguments.split(" ", 2);

		if (recipientAndMessage.length != 2) {
			throw new MalformedException("malformed arguments");
		}

		var recipient = Identifier.fromString(recipientAndMessage[0]);

		if (recipient.isNone()) {
			return new ChatCommand(recipientAndMessage[1]);
		}

		return new ChatCommand(recipient, recipientAndMessage[1]);
	}

	/**
	 * Gets the chat message associated with this command.
	 *
	 * @return The chat message.
	 */
	public String getMessage() {
		return this.message.toString();
	}

	/**
	 * @return the recipient.
	 */
	public Identifier getRecipient() {
		assert this.isWhispered() : "cannot get recipient of public chat command";

		return this.recipient;
	}

	/**
	 * @return whether this message is whispered.
	 */
	public boolean isWhispered() {
		return !this.recipient.isNone();
	}

	/**
	 * @return a textual representation of this command.
	 */
	@Override
	public String toString() {
		return super.toString() + ChatCommand.KEYWORD + " " + this.recipient + " "
			+ this.getMessage();
	}
}
