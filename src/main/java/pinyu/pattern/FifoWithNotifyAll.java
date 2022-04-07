package pinyu.pattern;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;

import pinyu.main.Start;

public class FifoWithNotifyAll extends Pattern implements Runnable {
	private static Object record = new Object();
	private static AtomicInteger locker = new AtomicInteger(-1);

	private static Queue<Integer> requestQueue = new LinkedList<Integer>();
	private static AtomicInteger txNum = new AtomicInteger(1);

	private static List<Integer> wakeUpOrders = new ArrayList<Integer>();
	private static List<Integer> executionOrders = new ArrayList<Integer>();

	public void run() {
		int myTxNum = txNum.getAndIncrement();
		while (!stopSignal.get()) {
			synchronized (record) {
				requestQueue.add(myTxNum);
			}
			
			try {
				Thread.sleep(Start.THREAD_SLEEP_TIME_IN_MS);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			synchronized (record) {
				while ((locker.get() != -1 && locker.get() != myTxNum) || requestQueue.peek() != myTxNum) {
					try {
						System.out.println("Tx " + myTxNum + " waits");
						record.wait();
						System.out.println("Tx " + myTxNum + " is waked up");

						wakeUpOrders.add(myTxNum);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}

				executionOrders.add(myTxNum);
				requestQueue.poll();
				locker.set(myTxNum);
			}

			System.out.println("Tx " + myTxNum + " acquires the lock");

			try {
				Thread.sleep(Start.THREAD_SLEEP_TIME_IN_MS);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			locker.set(-1);
			System.out.println("Tx " + myTxNum + " releases the lock");
			synchronized (record) {
				System.out.println("Tx " + myTxNum + " notifies all txs waiting on the same record");
				record.notifyAll();
			}
		}
	}

	public static void generateReports() {
		if (executionOrders.size() == 0) {
			return;
		}

		BufferedWriter ex = null;
		BufferedWriter wk = null;
		try {
			ex = new BufferedWriter(new FileWriter("execution-order.csv"));
			wk = new BufferedWriter(new FileWriter("wakeup-order.csv"));

			for (int i = 0; i < executionOrders.size(); i++) {
				ex.append(executionOrders.get(i).toString() + "\n");
			}

			for (int i = 0; i < wakeUpOrders.size(); i++) {
				wk.append(wakeUpOrders.get(i).toString() + "\n");
			}

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				ex.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			System.out.println("The reports are generated");
		}
	}
}
