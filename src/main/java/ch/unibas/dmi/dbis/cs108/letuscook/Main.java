package ch.unibas.dmi.dbis.cs108.letuscook;

import ch.unibas.dmi.dbis.cs108.letuscook.cli.ClientCLI;
import ch.unibas.dmi.dbis.cs108.letuscook.cli.ServerCLI;
import ch.unibas.dmi.dbis.cs108.letuscook.client.Client;
import ch.unibas.dmi.dbis.cs108.letuscook.gui.ClientGUI;
import ch.unibas.dmi.dbis.cs108.letuscook.server.Server;
import ch.unibas.dmi.dbis.cs108.letuscook.util.FatalExceptionHandler;
import ch.unibas.dmi.dbis.cs108.letuscook.util.MalformedException;
import ch.unibas.dmi.dbis.cs108.letuscook.util.Messenger;
import ch.unibas.dmi.dbis.cs108.letuscook.util.SanitizedName;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import javafx.application.Application;

/**
 * The Main class starts either a server or a client for the LetUsCook application based on the
 * command line arguments.
 */
public class Main {

	/**
	 * The duration in milliseconds to wait before checking if a pinged connection responded with a
	 * pong.
	 */
	public static final long PONG_WAIT_MS;

	/**
	 * The command line options for the application.
	 */
	private static final String USAGE =
		"""
			Usage:
			- server <port>
			- client <address>:<port> [<nickname>|$]
			  If "$" is supplied as the nickname, the system name is used.""";

	/**
	 * The contents of the 'test.ini' file, if it exists. Used for testing during development.
	 */
	private static final List<String[]> TEST_INI = new ArrayList<>();

	/*
	 * Initialization.
	 */
	static {
		for (String[] entry : new String[][]{
			{"src_pong_wait_ms", "15000"},
			{"src_enable_client_gui", "1"},
			{"src_clientcli_startup_actions_delay", "250"},
			{"src_clientcli_startup_actions", ""},
		}) {
			setTestingConfigurationEntry(entry);
		}

		parseTestingConfiguration();

		PONG_WAIT_MS = Long.parseLong(getTestingConfigurationEntry("src_pong_wait_ms")[1]);
	}

	/**
	 * The main method is the entry point of the program, which starts either a server or a client.
	 *
	 * @param args The command line arguments specifying the startup mode and optionally other
	 *             required parameters.
	 */
	public static void main(String[] args) {
		Thread.setDefaultUncaughtExceptionHandler(new FatalExceptionHandler());

		if (args.length < 1) {
			Main.printUsageAndDie();
		}

		/*
		 * Server.
		 */
		if (args[0].equalsIgnoreCase("server")) {
			if (args.length < 2) {
				Main.printUsageAndDie("Missing server port");
			}
			int port = 0;
			try {
				port = Main.parsePort(args[1]);
			} catch (NumberFormatException e) {
				Main.printUsageAndDie("Invalid port");
			}

			Messenger.selectLogger("server");
			new Server(port);
			ServerCLI.awaitAndConsumeActions();
			return;
		}

		/*
		 * Client.
		 */
		if (args[0].equalsIgnoreCase("client")) {
			if (args.length < 2) {
				Main.printUsageAndDie("Missing server address");
			}
			String[] addressPortTokens = args[1].split(":");
			if (addressPortTokens.length != 2) {
				Main.printUsageAndDie("Invalid format of address");
			}

			InetAddress address = null;
			int port = 0;
			try {
				address = InetAddress.getByName(addressPortTokens[0]);
				port = Main.parsePort(addressPortTokens[1]);
			} catch (UnknownHostException | SecurityException | NumberFormatException e) {
				Main.printUsageAndDie("Unknown or disallowed server address and port");
			}

			Messenger.selectLogger("client");

			new Client(address, port);

			if (args.length >= 3) {
				try {
					Client.the().setLoginNickname(SanitizedName.createOrThrow(
						args[2].equals("$") ? System.getProperty("user.name") : args[2]));
				} catch (MalformedException e) {
					Messenger.warn("Ignoring malformed login nickname.");
				}
			}

			if (Main.getTestingConfigurationEntry("src_enable_client_gui")[1].equals("1")) {
				new Thread(() -> Application.launch(ClientGUI.class, args),
					"luc-jfx-launcher").start();
			}

			var delay = Main.getTestingConfigurationEntry("src_clientcli_startup_actions_delay");
			String[] actions = Main.getTestingConfigurationEntry("src_clientcli_startup_actions");
			for (int i = 1; i < actions.length; ++i) {
				if (!actions[i].isBlank()) {
					Messenger.warn("(test.ini) Simulating: " + actions[i]);
					ClientCLI.consumeAction(actions[i]);
					ClientCLI.consumeAction("/wait " + delay[1]);
				}
			}

			ClientCLI.awaitAndConsumeActions();

			return;
		}

		Main.printUsageAndDie();
	}

	/**
	 * Parses the port from the given string.
	 *
	 * @param port The port as a string.
	 * @return The parsed port as an integer.
	 * @throws NumberFormatException If the string cannot be parsed as an integer.
	 */
	private static int parsePort(String port) throws NumberFormatException {
		return Integer.parseInt(port);
	}

	/**
	 * Prints the instructions for using the LetUsCook application and exits the program.
	 *
	 * @param message An optional error message to be displayed.
	 */
	public static void printUsageAndDie(String message) {
		System.err.println("Error: " + message + "\n");

		Main.printUsageAndDie();
	}

	/**
	 * Prints the instructions for using the LetUsCook application and exits the program.
	 */
	public static void printUsageAndDie() {
		System.err.println(Main.USAGE);

		System.exit(1);
	}

	/**
	 * Parse the 'test.ini' file, if it exists.
	 */
	private static void parseTestingConfiguration() {
		try {
			var reader = new BufferedReader(new FileReader("test.ini"));
			String line;
			while ((line = reader.readLine()) != null) {
				var keyAndValues = line.split("=", 2);
				if (keyAndValues.length != 2) {
					throw new MalformedException("Missing value");
				}
				var key = keyAndValues[0];
				var values =
					keyAndValues[1].isEmpty() ? new String[]{} : keyAndValues[1].split(";");
				var entry = new String[1 + values.length];
				entry[0] = key;
				System.arraycopy(values, 0, entry, 1, values.length);
				Main.setTestingConfigurationEntry(entry);
			}
			reader.close();
		} catch (FileNotFoundException ignored) {
		} catch (MalformedException e) {
			System.err.println("Malformed test.ini: " + e.getMessage());
		} catch (IOException e) {
			System.err.println("An IOException occurred while reading test.ini");
		}
	}

	/**
	 * Retrieve an entry from the testing configuration.
	 *
	 * @param key the key.
	 * @return the entry.
	 */
	public static String[] getTestingConfigurationEntry(String key) {
		for (var entry : Main.TEST_INI) {
			if (entry[0].equals(key)) {
				return entry;
			}
		}

		return new String[]{key};
	}

	/**
	 * Set an entry in the testing configuration.
	 *
	 * @param newEntry the entry.
	 */
	private static void setTestingConfigurationEntry(String[] newEntry) {
		Main.TEST_INI.removeIf(entry -> entry[0].equals(newEntry[0]));
		Main.TEST_INI.add(newEntry);
	}
}
