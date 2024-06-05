package ch.unibas.dmi.dbis.cs108.letuscook.util;

import ch.unibas.dmi.dbis.cs108.letuscook.cli.Console;
import ch.unibas.dmi.dbis.cs108.letuscook.gui.ClientGUI;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * The Messenger class provides methods for outputting messages with various styles and formats.
 */
public class Messenger {

	private static Logger LOGGER;

	public static Logger getLogger() {
		assert Messenger.LOGGER != null : "no logger selected";

		return Messenger.LOGGER;
	}

	public static void selectLogger(String name) {
		assert name != null : "no logger name provided";

		Messenger.LOGGER = LogManager.getLogger(name);
	}

	/**
	 * Outputs a chat message.
	 *
	 * @param author  The author of the message.
	 * @param message The message.
	 */
	public static void chat(String author, String message) {
		Console.println("\033[97m[chat] " + author + ": " + message + "\033[0m");
		Messenger.getLogger().info("{}: {}", author, message);
		if (ClientGUI.exists()) {
			ClientGUI.the().addMessage("chat", author, null, message);
		}
	}

	/**
	 * Outputs a whisper message.
	 *
	 * @param author    The author of the message.
	 * @param recipient the recipient.
	 * @param message   The message.
	 */
	public static void chat(String author, String recipient, String message) {
		Console.println(
			"\033[90m[whisper] (to " + recipient + ") " + author + ": " + message + "\033[0m");
		Messenger.getLogger().info("(to {}) {}: {}", recipient, author, message);
		if (ClientGUI.exists()) {
			ClientGUI.the().addMessage("whisper", author, recipient, message);
		}
	}

	/**
	 * Outputs a yell message.
	 *
	 * @param author  the author of the message.
	 * @param message the message.
	 */
	public static void yell(String author, String message) {
		Console.println("\033[92m[yell] " + author + ": " + message + "\033[0m");
		Messenger.getLogger().info("(yelled) {}: {}", author, message);
		if (ClientGUI.exists()) {
			ClientGUI.the().addMessage("yell", author, null, message);
		}
	}

	/**
	 * Outputs a debug message.
	 *
	 * @param message The debug message.
	 */
	public static void debug(String message) {
		Messenger.getLogger().debug(message);
	}

	/**
	 * Outputs an information message.
	 *
	 * @param message The information message.
	 */
	public static void info(String message) {
		Console.println("\033[94m[info] " + message + "\033[0m");
		Messenger.getLogger().info(message);
	}

	/**
	 * Outputs a warning.
	 *
	 * @param e       The exception that caused the warning.
	 * @param message The warning message.
	 */
	public static void warn(Exception e, String message) {
		warn(message);
	}

	/**
	 * Outputs a warning.
	 *
	 * @param message The warning message.
	 */
	public static void warn(String message) {
		Console.println("\033[93m[warn] " + message + "\033[0m");
		Messenger.getLogger().warn(message);
	}

	/**
	 * Outputs an error.
	 *
	 * @param e       The exception that caused the error.
	 * @param message The error message.
	 */
	public static void error(Exception e, String message) {
		error(message);
	}

	/**
	 * Outputs an error.
	 *
	 * @param message The error message.
	 */
	public static void error(String message) {
		Console.println("\033[91m[error] " + message + "\033[0m");
		Messenger.getLogger().error(message);
	}

	/**
	 * Outputs a fatal error message.
	 *
	 * @param e       The exception that caused the fatal error.
	 * @param message The fatal error message.
	 */
	public static void fatal(Exception e, String message) {
		fatal(message);
	}

	/**
	 * Outputs a fatal error message.
	 *
	 * @param message The fatal error message.
	 */
	public static void fatal(String message) {
		Console.println("\033[31m[fatal] " + message + "\033[0m");
		Messenger.getLogger().fatal(message);
	}

	/**
	 * Outputs a user message.
	 *
	 * @param message the message.
	 */
	public static void user(String message) {
		Console.println("\033[96m[user] " + message + "\033[0m");
		Messenger.getLogger().info("* {}", message);
		if (ClientGUI.exists()) {
			ClientGUI.the().addMessage("user", null, null, message);
		}
	}
}
