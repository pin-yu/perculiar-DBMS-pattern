package pinyu.pattern;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

import pinyu.main.Start;

/**
 * The implementation of <a href=
 * "https://github.com/elasql/elasql/tree/master/src/main/java/org/elasql/storage/tx/concurrency">Elasql's</a>
 * conservative lock table has room for improvement.
 * 
 * Elasql forces each worker thread that wants to access the same record to wait
 * on the same object. Once a thread commits and releases the locks, the
 * committed thread has the responsibility to NOTIFY ALL other threads that
 * intend to get the record. However, NOTIFY ALL is not guaranteed which threads
 * will be notified and the behavior contradict the conservative locking
 * protocol. The workaround in Elasql is to use a queue to maintain the request
 * order, and the waken thread will check the queue to see whether it has the
 * right to get the resource. These wake-then-check behavior causes quite a few
 * overhead.
 * 
 * FifoWithoutNotifyAll can be used to amortize the overhead because NOTIFY ALL
 * is eliminated. The key point is to let each thread wait on different objects
 * (proxy object). With the proper use of an atmoic queue
 * (concurrentLinkedQueue), a worker thread now can put a proxy object into the
 * queue, and wait on this proxy object until the head of the queue is itself.
 * After the previous worker thread commits, it will notify directly to the head
 * object of the queue.
 */
public class FifoWithoutNotifyAll extends Pattern implements Runnable {
	private static ConcurrentLinkedQueue<Object> requestQueue = new ConcurrentLinkedQueue<Object>();
	private static AtomicInteger txNum = new AtomicInteger(1);

	public void run() {
		int myTxNum = txNum.getAndIncrement();
		Object dummyObj = new Object();
		while (!stopSignal.get()) {
			requestQueue.add(dummyObj);

			while (requestQueue.peek() != dummyObj) {
				synchronized (dummyObj) {
					try {
						System.out.println("Tx " + myTxNum + " waits");
						dummyObj.wait();
						System.out.println("Tx " + myTxNum + " is waked up");
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}

			System.out.println("Tx " + myTxNum + " acquires the lock");

			try {
				Thread.sleep(Start.THREAD_SLEEP_TIME_IN_MS);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			requestQueue.remove();
			System.out.println("Tx " + myTxNum + " releases the lock");

			Object otherTxObj = requestQueue.peek();
			synchronized (otherTxObj) {
				System.out.println("Tx " + myTxNum + " notifies other tx");
				otherTxObj.notify();
			}
		}
	}
}
