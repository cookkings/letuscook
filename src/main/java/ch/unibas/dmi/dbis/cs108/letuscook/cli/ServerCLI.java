package ch.unibas.dmi.dbis.cs108.letuscook.cli;

import ch.unibas.dmi.dbis.cs108.letuscook.server.Server;
import ch.unibas.dmi.dbis.cs108.letuscook.util.Messenger;
import java.io.IOException;

public class ServerCLI {

	/**
	 * Consume a server action.
	 *
	 * @param action the server action.
	 * @return whether the caller should continue awaiting actions after this action is consumed.
	 */
	public static boolean consumeAction(String action) {
		switch (action) {
			case "clear" -> Console.clear();
			case "start" -> Server.the().start();
			case "stop" -> Server.the().stop();
			case "inspect" -> {
				// TODO:
			}
			case "quit" -> {
				return false;
			}
		}

		return true;
	}

	/**
	 * Await and consume server actions.
	 */
	public static void awaitAndConsumeActions() {
		/*
		 * Start-up actions.
		 */
		ServerCLI.consumeAction("start");

		/*
		 * Await and consume actions.
		 */
		String action;
		try {
			do {
				action = Console.readln();
			} while (ServerCLI.consumeAction(action));
		} catch (IOException e) {
			Messenger.fatal(e,
				"An IO error occurred while reading from the console - quitting");
		}

		/*
		 * Shut-down actions.
		 */
		ServerCLI.consumeAction("stop");
	}
}
