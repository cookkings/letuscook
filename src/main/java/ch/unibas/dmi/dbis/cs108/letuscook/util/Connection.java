package ch.unibas.dmi.dbis.cs108.letuscook.util;

import ch.unibas.dmi.dbis.cs108.letuscook.commands.Command;
import ch.unibas.dmi.dbis.cs108.letuscook.commands.PingCommand;
import ch.unibas.dmi.dbis.cs108.letuscook.commands.PongCommand;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * Manage resources and respond to pings/pongs.
 */
public class Connection {

	/**
	 * The client socket, received or created in the constructor.
	 */
	private final Socket socket;

	/**
	 * The input stream read by {@link #listener}.
	 */
	private final BufferedReader in;

	/**
	 * The output stream written to by {@link #sendCommandIfAlive}.
	 */
	private final PrintWriter out;

	/**
	 * How to handle commands.
	 */
	private final Consumer<Command> commandConsumer;

	/**
	 * Awaits, parses, and dispatches incoming commands, as well as responding to pings and pongs.
	 */
	private final Thread listener;

	/**
	 * Whether the listener is alive.
	 */
	private final AtomicBoolean listenerAlive = new AtomicBoolean(true);

	/**
	 * Denotes that we've sent a ping and are currently awaiting a pong.
	 */
	private final AtomicBoolean awaitingPong = new AtomicBoolean();

	/**
	 * Create a connection from an {@link InetAddress} and a port.
	 *
	 * @param address the address to connect to.
	 * @param port    the port to connect to.
	 * @throws IOException              see
	 *                                  {@link Connection#Connection(String, Socket, Consumer)}.
	 * @throws SecurityException        see {@link Socket#Socket(InetAddress, int)}.
	 * @throws IllegalArgumentException see {@link Socket#Socket(InetAddress, int)}.
	 * @throws NullPointerException     see {@link Socket#Socket(InetAddress, int)}.
	 */
	public Connection(String name, InetAddress address, int port, Consumer<Command> commandConsumer)
		throws IOException, SecurityException, IllegalArgumentException, NullPointerException {
		this(name, new Socket(address, port), commandConsumer);
	}

	/**
	 * Create a connection from a {@link Socket}.
	 *
	 * @param socket the socket representing the connection.
	 * @throws IOException if we fail to create the {@link #in} or {@link #out} streams.
	 */
	public Connection(String name, Socket socket, Consumer<Command> commandConsumer)
		throws IOException {
		assert name != null : "name cannot be null";
		assert socket != null : "socket cannot be null";
		assert commandConsumer != null : "commandConsumer cannot be null";

		this.socket = socket;

		this.in = new BufferedReader(
			new InputStreamReader(this.socket.getInputStream(), StandardCharsets.UTF_8));

		this.out = new PrintWriter(
			new OutputStreamWriter(this.socket.getOutputStream(), StandardCharsets.UTF_8), true);

		this.commandConsumer = commandConsumer;

		this.listener = new Thread(this::listen, name + "-listen");
		this.listener.setDaemon(true);
		this.listener.start();
	}

	public static String addressForSocket(Socket socket) {
		return (((InetSocketAddress) socket.getRemoteSocketAddress()).getAddress()).toString()
			.replace("/", "");
	}

	/**
	 * Enter the command listener. It reads incoming messages, parses them to {@link Command}
	 * objects, responds to pings and pongs, and dispatches remaining commands to the
	 * {@link #commandConsumer}.
	 */
	private void listen() {
		String commandString;

		while (!Thread.currentThread().isInterrupted()) {
			try {
				commandString = this.in.readLine();
			} catch (SocketException e) {
				if (Thread.currentThread().isInterrupted()) {
					Messenger.debug(
						"Socket was closed, listener is interrupted - stopping listener");
				} else {
					Messenger.error(e, "Cannot access socket - stopping listener");
				}
				break;
			} catch (IOException e) {
				Messenger.error(e, "An IO error occurred while listening - stopping listener");
				break;
			}
			if (commandString == null) {
				Messenger.warn("Cannot read from in stream - stopping listener");
				break;
			}

			this.consumeString(commandString);
		}

		this.listenerAlive.set(false);

		Messenger.info("Listener died");
	}

	/**
	 * Parse a {@link Command} from a string and consume it. If the command is not intended for this
	 * consumer, forward it to {@link #commandConsumer}.
	 *
	 * @param string the string.
	 */
	private void consumeString(String string) {
		Command command;
		try {
			command = Command.fromString(string);
		} catch (MalformedException e) {
			Messenger.warn("Ignoring malformed command: " + string);
			return;
		}

		if (command instanceof PingCommand) {
			this.sendCommandIfAlive(new PongCommand());
		} else if (command instanceof PongCommand) {
			this.awaitingPong.set(false);
		} else {
			Messenger.debug("Accepting command: " + command);
			this.commandConsumer.accept(command);
		}
	}

	/**
	 * Check if this connection is dead. A connection dies if its listener stops and/or if
	 * {@link #destroy()} is called.
	 *
	 * @return whether this connection is dead.
	 */
	public boolean isDead() {
		return !this.listenerAlive.get();
	}

	/**
	 * Send a command.
	 *
	 * @param command the command.
	 */
	public void sendCommandIfAlive(Command command) {
		if (this.isDead()) {
			Messenger.debug("Connection is dead - not sending command: " + command);
			return;
		}

		Messenger.debug("Sending: " + command);

		this.out.println(command);
	}

	/**
	 * @return whether this connection is currently awaiting a pong.
	 */
	public boolean isAwaitingPong() {
		return this.awaitingPong.get();
	}

	/**
	 * Ping this connection.
	 */
	public void ping() {
		/*
		 * It is crucial that we set awaitingPong *before* sending the command, as
		 * otherwise there is a chance that the server responds "between" these two
		 * lines, causing the pong to be lost.
		 */
		this.awaitingPong.set(true);
		this.sendCommandIfAlive(new PingCommand());
	}

	/**
	 * Get this connection's IP address.
	 *
	 * @return this connection's IP address.
	 */
	public String getAddress() {
//		if (this.isDead()) {
//			Messenger.debug("Connection is dead - returning null address");
//			return null;
//		}

		return Connection.addressForSocket(this.socket);
	}

	/**
	 * Close all resources and stop all threads. <b>Once called, this connection must be
	 * discarded.</b>
	 */
	public void destroy() {
		assert this.socket != null : "socket is null";
		assert this.in != null : "in is null";
		assert this.out != null : "out is null";

		/*
		 * If the listener is alive, it holds the lock for the BufferedReader.
		 * Therefore, we must first kill the listener before we can close 'this.in'.
		 */
		if (!this.isDead()) {
			this.listener.interrupt();
		}
		/*
		 * As long as the socket is open, the listener blocks on 'this.in.readLine()'.
		 * We close the socket to end this block, allowing the listener to stop.
		 */
		try {
			this.socket.close();
		} catch (IOException e) {
			Messenger.warn(e,
				"An IO error occurred while closing a connection's socket - ignoring");
		}

		/*
		 * Now that the listener is stopped we can close the streams.
		 */

		try {
			this.in.close();
		} catch (IOException e) {
			Messenger.warn(e,
				"An IO error occurred while closing a connection's 'in' - ignoring");
		}

		this.out.close();

		Messenger.debug("Connection destroyed");
	}
}
