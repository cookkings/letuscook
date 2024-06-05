package ch.unibas.dmi.dbis.cs108.letuscook.cli;

import ch.unibas.dmi.dbis.cs108.letuscook.client.Client;
import ch.unibas.dmi.dbis.cs108.letuscook.gui.Units;
import ch.unibas.dmi.dbis.cs108.letuscook.server.Game;
import ch.unibas.dmi.dbis.cs108.letuscook.server.Player;
import ch.unibas.dmi.dbis.cs108.letuscook.util.Coords;
import ch.unibas.dmi.dbis.cs108.letuscook.util.MalformedException;
import ch.unibas.dmi.dbis.cs108.letuscook.util.Messenger;
import ch.unibas.dmi.dbis.cs108.letuscook.util.SanitizedName;
import java.io.IOException;

public class ClientCLI {

	/**
	 * Consume a client action.
	 *
	 * @param action the client action.
	 * @return whether the caller should continue awaiting actions after this action is consumed.
	 */
	public static boolean consumeAction(String action) {
		/*
		 * Filter.
		 */
		if (action.startsWith("#")) {
			var filterAndAction = action.trim().split("\\s+", 2);
			if (!filterAndAction[0].substring(1)
				.equals(Client.the().getOwnIdentifier().toString())) {
				return true;
			}
			action = filterAndAction[1];
		}

		/*
		 * Parse action.
		 */
		var keywordAndArguments = action.trim().split("\\s+", 2);
		var keyword = keywordAndArguments[0];
		var argumentString = keywordAndArguments.length == 2 ? keywordAndArguments[1] : "";
		var arguments = argumentString.split("\\s+", 2);
		String[] argumentList = {arguments[0], arguments.length == 2 ? arguments[1] : ""};

		/*
		 * Slash actions.
		 */
		if (keyword.startsWith("/")) {
			switch (keyword) {
				case "/clear" -> Console.clear();
				case "/conn" -> {
					if (!argumentString.trim().isEmpty()) {
						try {
							Client.the().setLoginNickname(
								SanitizedName.createOrThrow(argumentString));
						} catch (MalformedException e) {
							Messenger.warn("Ignoring malformed login nickname.");
						}
					}
					Client.the().tryConnect();
				}
				case "/disc" -> Client.the().tryDisconnect();
				case "/nick" -> Client.the().tryNickname(argumentString);
				case "/refresh" -> Client.the().tryRefresh();
				case "/lobbies" -> {
					var lobbies = Client.the().getLobbies();
					StringBuilder sb = new StringBuilder("Lobbies: ");
					for (var lobby : lobbies) {
						sb.append("\n- ").append(lobby.getName()).append(" (")
							.append(lobby.gameIsRunning() ? "in-game" : "idle").append("): ");
						var members = lobby.getActors();
						synchronized (members) {
							for (int i = 0; i < members.length; ++i) {
								sb.append(members[i].record().orElseThrow().getNickname());
								if (i < members.length - 1) {
									sb.append(", ");
								}
							}
						}
					}
					Messenger.info(sb.toString());
				}
				case "/all" -> {
					var actors = Client.the().getActors();
					StringBuilder sb = new StringBuilder("All: ");
					synchronized (actors) {
						for (var actor : actors) {
							sb.append(actor.record().orElseThrow().getNickname()).append(", ");
						}
					}
					Messenger.info(sb.substring(0, sb.length() - 2));
				}
				case "/here" -> {
					if (!Client.the().hasOwnActor()) {
						Messenger.error("Not logged in");
						break;
					}
					var actor = Client.the().getOwnActor();
					if (actor.member().isEmpty()) {
						Messenger.error("Not a member of a lobby");
						break;
					}
					var lobby = actor.member().orElseThrow().getLobby();
					var members = lobby.getActors();
					StringBuilder sb = new StringBuilder("Here: ");
					synchronized (members) {
						for (var member : members) {
							sb.append(member.member().orElseThrow().player().isEmpty() ? "?" : "")
								.append(member.record().orElseThrow().getNickname()).append(", ");
						}
					}
					Messenger.info(sb.substring(0, sb.length() - 2));
				}
				case "/open" -> Client.the().tryOpenLobby(argumentString);
				case "/join" -> Client.the().tryJoinLobby(argumentString);
				case "/leave" -> Client.the().tryLeaveLobby();
				case "/ready" -> Client.the().sendReady();
				case "/start" -> Client.the().tryStartGame();
				case "/yell" -> Client.the().tryYell(argumentString);
				case "/die" -> System.exit(1);
				case "/quit" -> {
					if (Client.the().hasConnection()) {
						ClientCLI.consumeAction("/disc");
					}
					return false;
				}
				case "/wait" -> {
					try {
						Thread.sleep(Integer.parseInt(argumentString));
					} catch (NumberFormatException e) {
						Messenger.error("Malformed millisecond amount");
					} catch (InterruptedException e) {
						Messenger.warn("Interrupted");
					}
				}
				case "/pos" -> {
					if (argumentString.isBlank()) {
						Messenger.info(
							Client.the().getOwnActor().member().orElseThrow().player().orElseThrow()
								.getRect().toString());
					} else {
						try {
							Coords coords = new Coords(argumentString);
							Game game = Client.the().getOwnActor().member().orElseThrow().getLobby()
								.game().orElseThrow();
							Player player = Client.the().getOwnActor().member().orElseThrow()
								.player().orElseThrow();
							game.movePlayerBy(player,
								new Units(coords.getX().u() - player.getRect().getX().u()),
								new Units(coords.getY().u() - player.getRect().getY().u()));
						} catch (MalformedException e) {
							Messenger.error("Ignoring malformed Position");
						}
					}
				}
				case "/act" -> Client.the().tryInteract();
				case "/hand" -> Messenger.info(
					"Hand: " + Client.the().getOwnActor().member().orElseThrow().player()
						.orElseThrow().getHolding().toString());
				default -> Messenger.error("Unknown slash action");
			}
			return true;
		}

		/*
		 * Whispering.
		 */
		if (keyword.startsWith("@")) {
			Client.the().tryWhisperViaNickname(keyword.substring(1), argumentString);
			return true;
		}

		/*
		 * Chat.
		 */
		Client.the().tryChat(action);
		return true;
	}

	/**
	 * Await and consume client actions.
	 */
	public static void awaitAndConsumeActions() {
		Messenger.info("Your login nickname is '" + Client.the().getLoginNickname()
			+ "'. Use /conn <nickname> to use a different login nickname.");

		/*
		 * Await and consume actions.
		 */
		String action;
		try {
			do {
				action = Console.readln();
			} while (ClientCLI.consumeAction(action));
		} catch (IOException e) {
			Messenger.fatal(e,
				"An IO error occurred while reading from the console - quitting");
		}
	}
}
