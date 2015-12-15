package in.javawithgaurav.blogspot.threadpool;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import in.javawithgaurav.blogspot.threadpool.ThreadPools.ThreadFactory;

/**
 * 
 * @author Gaurav Rai Mazra
 *
 */
public class ThreadPoolImpl implements ThreadPool {
	private final int corePoolSize;
	private final ThreadFactory threadFactory;
	private final BlockingQueue<Runnable> tasks = new LinkedBlockingQueue<>(25);
	private final BlockingQueue<Worker> workers;
	private AtomicInteger totalWorkers = new AtomicInteger(0);
	
	public ThreadPoolImpl(final int corePoolSize, final ThreadFactory threadFactory) {
		this.corePoolSize = corePoolSize;
		this.threadFactory = threadFactory;
		this.workers = new LinkedBlockingQueue<>(corePoolSize);
	}

	@Override
	public void execute(Runnable task) {
		while (true) {
			int t = totalWorkers.get();
			if(t < this.corePoolSize) {
				if (addWorker(task))
					break;
			}
		}
	}

	private boolean addWorker(Runnable task) {
		
		return false;
	}

	@Override
	public void shutDown() {
		for (Worker w : this.workers) {
			w.setState(WorkerState.STOP);
		}
	}
	
	enum WorkerState {
		NEW, RUNNING, WAITING, STOP, DIRTY
	}
	
	class Worker implements Runnable {
		final Thread t;
		volatile Runnable firstTask;
		volatile WorkerState state;
		
		
		public Worker (Runnable firstTask) {
			this.firstTask = firstTask;
			setState(WorkerState.NEW);
			this.t = getThreadFactory().createThread(this);
		}
		
		@Override
		public void run() {
			runWorker(this);
		}
		
		private void runWorker(Worker w) {
			while (this.state != WorkerState.STOP) {
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
				}
				catch(InterruptedException ie) {
					setState(WorkerState.DIRTY);
					Thread.currentThread().interrupt();
				}
			}
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
			}
			catch (SecurityException se) {
				//Do I need to be cached. Not sure
			}
		}
	}

}
