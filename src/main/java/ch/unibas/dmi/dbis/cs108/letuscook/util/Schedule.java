package ch.unibas.dmi.dbis.cs108.letuscook.util;

import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;

/**
 * Schedule provides utility methods for scheduling tasks with fixed rates or delays. It uses a
 * single-threaded scheduled executor service internally.
 */
public class Schedule {

	private final ScheduledFuture<?> future;

	/**
	 * Constructs a new Schedule object with the provided ScheduledFuture. This constructor is
	 * private and intended for internal use only.
	 *
	 * @param future The ScheduledFuture representing the scheduled task.
	 */
	private Schedule(ScheduledFuture<?> future) {
		this.future = future;
	}

	/**
	 * Creates and returns a new ScheduledExecutorService with a single thread and a custom thread
	 * factory that creates daemon threads with the specified name.
	 *
	 * @param name The name of the thread to be created.
	 * @return A ScheduledExecutorService with a single daemon thread.
	 */
	private static ScheduledExecutorService newScheduledExecutorService(String name) {
		return Executors.newScheduledThreadPool(1, runnable -> {
			Thread thread = new Thread(runnable, name);
			thread.setDaemon(true);
			return thread;
		});
	}

	/**
	 * Creates and returns a Schedule object that executes the provided Callable at a fixed rate,
	 * starting immediately and repeating every specified period.
	 *
	 * @param callable The Callable task to be executed.
	 * @param period   The time between the start of each execution, in milliseconds.
	 * @param name     The name of the thread executing the task.
	 * @return A Schedule object for managing the scheduled task.
	 */
	public static Schedule atFixedRate(Callable<?> callable, long period, String name) {
		return new Schedule(Schedule.newScheduledExecutorService(name)
			.scheduleAtFixedRate(() -> callAndHandleExceptions(callable), 0, period,
				TimeUnit.MILLISECONDS));
	}

	/**
	 * Creates and returns a Schedule object that executes the provided Callable with a fixed delay
	 * between the termination of one execution and the start of the next.
	 *
	 * @param callable The Callable task to be executed.
	 * @param period   The delay between the end of one execution and the start of the next, in
	 *                 milliseconds.
	 * @param name     The name of the thread executing the task.
	 * @return A Schedule object for managing the scheduled task.
	 */
	public static Schedule withFixedDelay(Callable<Object> callable, long period,
		String name) {
		return new Schedule(Schedule.newScheduledExecutorService(name)
			.scheduleWithFixedDelay(() -> callAndHandleExceptions(callable), 0, period,
				TimeUnit.MILLISECONDS));
	}

	/**
	 * Creates a schedule that checks <code>predicate</code> every <code>period</code> milliseconds,
	 * calling <code>consequence</code> and stopping once the predicate is no longer true.
	 *
	 * @param predicate   the condition that must be met for the schedule to remain alive.
	 * @param consequence the consequence once the predicate is no longer true.
	 * @param period      the period in milliseconds in which the predicate is checked.
	 * @return the schedule.
	 */
	public static void waitWhile(BooleanSupplier predicate,
		Callable<Object> consequence, long period, String name) {
		var thread = new Thread(() -> {
			try {
				while (predicate.getAsBoolean()) {
					Thread.sleep(period);
				}
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
			callAndHandleExceptions(consequence);
		}, name);
		thread.setDaemon(true);
		thread.start();
	}

	/**
	 * Calls the provided Callable and handles any exceptions thrown during execution. If an
	 * exception occurs, it is forwarded to the default uncaught exception handler.
	 *
	 * @param callable The Callable task to be executed.
	 */
	private static void callAndHandleExceptions(Callable<?> callable) {
		try {
			callable.call();
		} catch (Throwable t) {
			if (t instanceof CompletedException) {
				Messenger.debug(Thread.currentThread().getName() + " completed normally.");
			} else {
				Thread.getDefaultUncaughtExceptionHandler()
					.uncaughtException(Thread.currentThread(), t);
			}
		}
	}

	/**
	 * Attempts to cancel the scheduled task associated with this Schedule object. If the task has
	 * already been completed or cancelled, the assertion fails. The task is cancelled in any case.
	 */
	public void stop() {
		assert !this.future.isDone() : "future already cancelled";

		this.future.cancel(true);
	}
}
