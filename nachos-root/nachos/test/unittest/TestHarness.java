/**
 * 
 */
package nachos.test.unittest;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import junit.framework.TestCase;
import nachos.machine.Machine;
import nachos.threads.RoundRobinScheduler;
import nachos.threads.Scheduler;

import org.junit.AfterClass;
import org.junit.BeforeClass;

/**
 * @author Sasha
 * 
 */
public abstract class TestHarness {

	protected static BlockingQueue<Runnable> instructionQueue;
	protected static BlockingQueue<Object> messageQueue;
	private static ExecutorService nachosExecutor;

	protected static Class<? extends Scheduler> getScheduler() {
		return RoundRobinScheduler.class;
	}

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static final void setUpBeforeClass() throws Exception {

		instructionQueue = new LinkedBlockingQueue<Runnable>();
		messageQueue = new LinkedBlockingQueue<Object>();
		TestingAutoGrader.setScheduler(getScheduler());
		TestingAutoGrader.setInstructionQueue(instructionQueue);
		TestingAutoGrader.setMessageQueue(messageQueue);
		Callable<Object> nachosTask = new Callable<Object>() {

			@Override
			public Object call() {
				Machine.main(new String[] { "--",
						"nachos.test.unittest.TestingAutoGrader" });
				return null;
			}
		};
		nachosExecutor = Executors.newSingleThreadExecutor();
		nachosExecutor.submit(nachosTask);
		messageQueue.take();
	}

	/**
	 * Queues a job for the testing machine
	 * 
	 * @param r
	 *            task to run
	 * @return
	 */
	public static void enqueueJob(Runnable r) {
		instructionQueue.offer(r);
		Thread.yield();
		try {
			messageQueue.take();
		} catch (InterruptedException e) {
			e.printStackTrace();
			TestCase.fail(e.getMessage());
		}
	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static final void tearDownAfterClass() throws Exception {
		instructionQueue.offer(new Runnable() {
			@Override
			public void run() {
				Machine.halt();
			}
		});
		Thread.yield();
	}
}
