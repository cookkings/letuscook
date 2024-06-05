package ch.unibas.dmi.dbis.cs108.letuscook.cli;

import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.NoSuchElementException;
import java.util.Scanner;

/**
 * Helpers for reading from and writing to the console with a custom layout.
 */
public class Console {

	private static final boolean DEBUG_DEACTIVATE = false;

	/**
	 * A scanner to read console input.
	 */
	private static final Scanner scanner = new Scanner(System.in, StandardCharsets.UTF_8);

	/*
	 * Initialize the console.
	 */
	static {
		/* Write UTF-8. */
		System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out), true,
			StandardCharsets.UTF_8));

		Console.clear();
	}

	/**
	 * Read a line from the console.
	 *
	 * @return the line read from the console.
	 * @throws IOException if an I/O error occurs.
	 */
	public static String readln() throws IOException {
		/* Move cursor to text input line. */
		System.out.print("\033[H\033[K\033[2B\033[K\033[A > \033[K");

		String input;
		try {
			input = scanner.nextLine();
		} catch (NoSuchElementException | IllegalStateException e) {
			input = null;
		}

		/* Move cursor back to previous position. */
		System.out.print("\0338");

		if (input == null) {
			throw new IOException();
		}
		return input;
	}

	/**
	 * Print a string to the console, followed by a line break.
	 *
	 * @param string the string to print.
	 */
	public static synchronized void println(String string) {
		if (!DEBUG_DEACTIVATE) {
			System.out.print("\0338" + string + "\n\0337\033[2;4H");
		}
	}

	/**
	 * Clear the console.
	 */
	public static synchronized void clear() {
		if (!DEBUG_DEACTIVATE) {
			System.out.print("\033[2J\033[4H\0337");
		}
	}
}
