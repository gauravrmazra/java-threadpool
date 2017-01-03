package com.gauravbytes.threadpool;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class ThreadPoolTests {
	public static void main(String[] args) {
		ThreadPool p = ThreadPools.newFixedThreadPool(4);

		ThreadPoolSimulator t1 = new ThreadPoolSimulator(p);
		t1.setName("Number One Simulator");

		ThreadPoolSimulator t2 = new ThreadPoolSimulator(p);
		t2.setName("Number Two Simulator");

		t1.start();
		t2.start();

		try {
			t1.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			t2.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		p.shutDown();

	}

	static class ThreadPoolSimulator extends Thread {
		private static final AtomicInteger count = new AtomicInteger(0);
		ThreadPool pool;

		public ThreadPoolSimulator(ThreadPool pool) {
			this.pool = pool;
		}

		@Override
		public void run() {
			for (int i = 0; i < 20; i++) {
				try {
					//System.out.println("Task : " + count.incrementAndGet() + " name: " + this.getName());
					pool.execute(new Task(count.incrementAndGet()));
				} catch (Exception e) {
					System.err.println(e);
				}
			}
		}
	}

	static class Task implements Runnable {
		String createdBy;
		int number;

		Task(int number) {
			createdBy = Thread.currentThread().getName();
			this.number = number;
		}

		@Override
		public void run() {
			try {
				TimeUnit.MILLISECONDS.sleep(500l);
				System.out.println("Task: " + number + " is created by thread: " + createdBy + " and is executed by: "
						+ Thread.currentThread().getName());
			} catch (InterruptedException ie) {
				Thread.currentThread().interrupt();
			}
		}

	}
}
