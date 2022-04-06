package pinyu.main;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import pinyu.pattern.FifoPattern;

public class Start {
	public static final int THREAD_SLEEP_TIME_IN_MS = 100;

	private static ExecutorService executor;
	private static final int THREAD_NUM = 100;

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		executor = Executors.newWorkStealingPool(THREAD_NUM);

		for (int i = 0; i < THREAD_NUM; i++) {
			executor.execute(new FifoPattern());
		}

		Thread ct = Thread.currentThread();
		try {
			ct.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println("main thread is terminated");
	}
}
