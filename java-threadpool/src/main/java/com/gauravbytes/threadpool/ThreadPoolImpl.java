package com.gauravbytes.threadpool;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import com.gauravbytes.threadpool.ThreadPools.ThreadFactory;

/**
 * 
 * @author Gaurav Rai Mazra
 *
 */
public class ThreadPoolImpl implements ThreadPool {
	private final int corePoolSize;
	private final ThreadFactory threadFactory;
	private final BlockingQueue<Runnable> tasks;
	private final BlockingQueue<Worker> workers;
	private AtomicInteger totalWorkers = new AtomicInteger(0);
	private final static int MAX_TASKS_PER_THREAD = 5;

	public ThreadPoolImpl(final int corePoolSize, final ThreadFactory threadFactory) {
		this.corePoolSize = corePoolSize;
		this.threadFactory = threadFactory;
		this.workers = new LinkedBlockingQueue<>(corePoolSize);
		this.tasks = new LinkedBlockingQueue<>(corePoolSize * MAX_TASKS_PER_THREAD);
	}

	@Override
	public void execute(Runnable task) {
		int t = totalWorkers.get();
		if (t < this.corePoolSize || !addWorker(task)) {
			addToQueue(task);
		}
	}

	private void addToQueue(Runnable task) {
		try {
			this.tasks.put(task);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private boolean addWorker(Runnable task) {
		int workerCount = totalWorkers.get();
		boolean added = false;
		while (workerCount < this.corePoolSize) {
			added = this.totalWorkers.compareAndSet(workerCount, workerCount + 1);
			workerCount = totalWorkers.get();
			if (added)
				break;
		}

		if (added) {
			System.out.println(
					"Worker added by " + Thread.currentThread().getName() + " " + " and worker count: " + workerCount);
			Worker w = new Worker(task);
			this.workers.add(w);
			w.t.start();
		}
		return added;
	}

	@Override
	public void shutDown() {
		for (Worker w : this.workers) {
			w.setState(WorkerState.STOP);
			w.interuptMe();
		}
	}

	enum WorkerState {
		NEW, RUNNING, WAITING, STOP, DIRTY
	}

	class Worker implements Runnable {
		final Thread t;
		Runnable firstTask;
		volatile WorkerState state;

		public Worker(Runnable firstTask) {
			this.firstTask = firstTask;
			setState(WorkerState.NEW);
			this.t = getThreadFactory().createThread(this);
		}

		@Override
		public void run() {
			runWorker(this);
		}

		private void runWorker(Worker w) {
			while (this.state != WorkerState.STOP && !t.isInterrupted()) {
				try {
					Runnable firstTask = w.firstTask;
					w.firstTask = null;

					if (firstTask != null) {
						setState(WorkerState.RUNNING);
						firstTask.run();
					}
					setState(WorkerState.WAITING);
					Runnable next = getNextTask();
					w.firstTask = next;
				} catch (InterruptedException ie) {
					setState(WorkerState.DIRTY);
					Thread.currentThread().interrupt();
				}
			}
			workers.remove(w);
			totalWorkers.decrementAndGet();
		}

		private Runnable getNextTask() throws InterruptedException {
			return ThreadPoolImpl.this.tasks.take();
		}

		private void setState(WorkerState currentState) {
			this.state = currentState;
		}

		private ThreadFactory getThreadFactory() {
			return ThreadPoolImpl.this.threadFactory;
		}

		void interuptMe() {
			try {
				t.interrupt();
			} catch (SecurityException se) {
				// Do I need to be cached. Not sure
			}
		}
	}

}
