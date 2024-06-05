package ch.unibas.dmi.dbis.cs108.letuscook.commands;

import ch.unibas.dmi.dbis.cs108.letuscook.util.MalformedException;
import ch.unibas.dmi.dbis.cs108.letuscook.util.SanitizedName;

/**
 * Sent by the client to set its own nickname. Sent by the server to announce any actor's nickname.
 */
public class IntroduceCommand extends Command {

	public static final String KEYWORD = "INTRO";

	private final SanitizedName nickname;

	/**
	 * Constructs a IntroduceCommand with the specified nickname.
	 *
	 * @param nickname The nickname to be set.
	 */
	public IntroduceCommand(String nickname) throws MalformedException {
		this.nickname = SanitizedName.createOrThrow(nickname);
	}

	/**
	 * Creates an anonymous instance of this command, given a string of arguments.
	 *
	 * @param arguments the arguments
	 * @return the command.
	 * @throws MalformedException if the arguments are malformed.
	 */
	public static IntroduceCommand fromArguments(String arguments) throws MalformedException {
		return new IntroduceCommand(arguments);
	}

	/**
	 * Gets the nickname associated with this command.
	 *
	 * @return The nickname.
	 */
	public String getNickname() {
		return this.nickname.toString();
	}

	/**
	 * Returns a string representation of this command.
	 *
	 * @return A string representation of the command in the format: "[identifier] NICKNAME
	 * [nickname]".
	 */
	@Override
	public String toString() {
		return super.toString() + IntroduceCommand.KEYWORD + " " + this.nickname;
	}
}
