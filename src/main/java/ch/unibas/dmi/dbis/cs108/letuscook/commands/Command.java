package ch.unibas.dmi.dbis.cs108.letuscook.commands;

import ch.unibas.dmi.dbis.cs108.letuscook.util.Identifier;
import ch.unibas.dmi.dbis.cs108.letuscook.util.MalformedException;
import ch.unibas.dmi.dbis.cs108.letuscook.util.SanitizedLine;

/**
 * This abstract class is responsible for communication between client and server using commands.
 * Each command is responsible for a significant execution.
 */
public abstract class Command {

	/**
	 * Whom this command concerns.
	 */
	private Identifier subject = Identifier.NONE;

	/**
	 * Parse a command.
	 *
	 * @param string the string containing the textual representation of the command.
	 * @return the resulting command.
	 * @throws MalformedException if the command is malformed.
	 */
	public static Command fromString(String string) throws MalformedException {
		if (!SanitizedLine.qualifies(string)) {
			throw new MalformedException("not a safe string");
		}

		String[] ika = string.split(" ", 3); /* identifier, keyword, and arguments */
		if (ika.length < 2) {
			throw new MalformedException("missing keyword");
		}
		String arguments = ika.length == 3 ? ika[2] : null;

		Command command = switch (ika[1]) {
			case PingCommand.KEYWORD -> PingCommand.fromArguments(arguments);
			case PongCommand.KEYWORD -> PongCommand.fromArguments(arguments);
			case DisappearCommand.KEYWORD -> DisappearCommand.fromArguments(arguments);
			case RefreshCommand.KEYWORD -> RefreshCommand.fromArguments(arguments);
			case IntroduceCommand.KEYWORD -> IntroduceCommand.fromArguments(arguments);
			case LobbyOpenCommand.KEYWORD -> LobbyOpenCommand.fromArguments(arguments);
			case LobbyCloseCommand.KEYWORD -> LobbyCloseCommand.fromArguments(arguments);
			case LobbyJoinCommand.KEYWORD -> LobbyJoinCommand.fromArguments(arguments);
			case LobbyLeaveCommand.KEYWORD -> LobbyLeaveCommand.fromArguments(arguments);
			case LobbyReadyCommand.KEYWORD -> LobbyReadyCommand.fromArguments(arguments);
			case GameRequestStartCommand.KEYWORD ->
				GameRequestStartCommand.fromArguments(arguments);
			case GameForceStopCommand.KEYWORD -> GameForceStopCommand.fromArguments(arguments);
			case GameTimeCommand.KEYWORD -> GameTimeCommand.fromArguments(arguments);
			case PlayerPositionCommand.KEYWORD -> PlayerPositionCommand.fromArguments(arguments);
			case ChatCommand.KEYWORD -> ChatCommand.fromArguments(arguments);
			case YellCommand.KEYWORD -> YellCommand.fromArguments(arguments);
			case GameUpdateWorkbenchCommand.KEYWORD ->
				GameUpdateWorkbenchCommand.fromArguments(arguments);
			case GameOrderCommand.KEYWORD -> GameOrderCommand.fromArguments(arguments);
			case PlayerInteractCommand.KEYWORD -> PlayerInteractCommand.fromArguments(arguments);
			case PlayerHoldingCommand.KEYWORD -> PlayerHoldingCommand.fromArguments(arguments);
			case GameScoreCommand.KEYWORD -> GameScoreCommand.fromArguments(arguments);
			case HighscoresCommand.KEYWORD -> HighscoresCommand.fromArguments(arguments);
			case GameParticipateCommand.KEYWORD -> GameParticipateCommand.fromArguments(arguments);
			default -> throw new MalformedException("unknown keyword");
		};

		command.setSubject(Identifier.fromString(ika[0]));

		return command;
	}

	public static Command withSubject(Identifier subject, Command command) {
		assert subject.isSome();

		command.setSubject(subject);

		return command;
	}

	/**
	 * @return whom this command concerns.
	 */
	public Identifier getSubject() {
		return this.subject;
	}

	/**
	 * Sets the identifier associated with this command.
	 *
	 * @param subject The identifier to be set.
	 */
	public void setSubject(Identifier subject) {
		this.subject = subject;
	}

	/**
	 * @return a textual representation of the command.
	 */
	@Override
	public String toString() {
		return this.subject + " ";
	}
}
