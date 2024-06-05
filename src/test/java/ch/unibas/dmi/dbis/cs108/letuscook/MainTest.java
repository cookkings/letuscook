package ch.unibas.dmi.dbis.cs108.letuscook;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

/**
 * An example test class. Checks the output of the {@link Main} class and makes sure it contains
 * "Hello World"
 */
public class MainTest {

	/*
	 * Streams to store system.out and system.err content
	 */
	private final ByteArrayOutputStream outStream = new ByteArrayOutputStream();
	private final ByteArrayOutputStream errStream = new ByteArrayOutputStream();

	/*
	 * Here we store the previous pointers to system.out / system.err
	 */
	private PrintStream outBackup;
	private PrintStream errBackup;

	/**
	 * This is a normal JUnit-Test. It executes the HelloWorld-Method and verifies that it actually
	 * wrote "Hello World" to stdout
	 */
	// @Test
	// public void testMain() {
	//     Main.main(new String[0]);
	//     String toTest = outStream.toString();
	//     toTest = removeNewline(toTest);
	//     assertTrue(toTest.contains("Hello World"));
	// }
	private static String removeNewline(String str) {
		return str.replace("\n", "").replace("\r", "");
	}

	/**
	 * This method is executed before each test. It redirects System.out and System.err to our
	 * variables {@link #outStream} and {@link #errStream}. This allows us to test their content
	 * later.
	 */
	@BeforeEach
	public void redirectStdOutStdErr() {
		outBackup = System.out;
		errBackup = System.err;
		System.setOut(new PrintStream(outStream));
		System.setErr(new PrintStream(errStream));
	}

	/**
	 * This method is run after each test. It redirects System.out / System.err back to the normal
	 * streams.
	 */
	@AfterEach
	public void reestablishStdOutStdErr() {
		System.setOut(outBackup);
		System.setErr(errBackup);
	}
}
