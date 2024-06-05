package ch.unibas.dmi.dbis.cs108.letuscook.util;
/**
 * FatalExceptionHandler is an implementation of Thread.UncaughtExceptionHandler
 * that prints the stack trace of the uncaught exception and exits the application.
 */
public class FatalExceptionHandler implements Thread.UncaughtExceptionHandler {
	/**
	 * Prints the stack trace of the uncaught exception and exits the application.
	 *
	 * @param t The thread where the uncaught exception occurred.
	 * @param e The uncaught exception.
	 */
	@Override
	public void uncaughtException(Thread t, Throwable e) {
		e.printStackTrace();
		System.exit(1);
	}
}
