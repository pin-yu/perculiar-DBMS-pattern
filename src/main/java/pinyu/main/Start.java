package pinyu.main;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import pinyu.pattern.FifoWithNotifyAll;
import pinyu.pattern.FifoWithoutNotifyAll;
import pinyu.pattern.Pattern;

public class Start {
	// FifoWithoutNotifyAll: 0, FifoWithNotifyAll: 1
	public static final int PATTERN_TYPE = 1;
	public static final int BENCHMARK_TIME_IN_MS = 5000;
	public static final int THREAD_SLEEP_TIME_IN_MS = 10;

	private static ExecutorService executor;
	private static final int THREAD_NUM = 100;

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		executor = Executors.newWorkStealingPool(THREAD_NUM);

		for (int i = 0; i < THREAD_NUM; i++) {
			executor.execute(jobFactory());
		}

		try {
			Thread.sleep(BENCHMARK_TIME_IN_MS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		Pattern.stop();
		
		// Generate a report
		FifoWithNotifyAll.generateReports();

		System.out.println("Benchmark completes");
	}

	public static Runnable jobFactory() {
		switch (PATTERN_TYPE) {
		case 0:
			return new FifoWithoutNotifyAll();
		case 1:
			return new FifoWithNotifyAll();
		default:
			return null;
		}
	}
}
