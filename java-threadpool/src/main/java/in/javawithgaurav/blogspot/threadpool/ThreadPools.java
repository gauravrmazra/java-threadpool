package in.javawithgaurav.blogspot.threadpool;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 
 * @author Gaurav Rai Mazra
 *
 */
public final class ThreadPools {
	
	public static ThreadPool newFixedThreadPool(int maxPoolSize) {
		ThreadPoolImpl impl = new ThreadPoolImpl(maxPoolSize, defaultThreadFactory());
		return impl;
	}
	
	
	public static ThreadFactory defaultThreadFactory() {
		return defaultThreadFactory("pool-alpha-");
	}
	public static ThreadFactory defaultThreadFactory(String prefix) {
		return new DefaultThreadFactory(prefix);
	}
	
	interface ThreadFactory {
		public Thread createThread(Runnable r);
	}
	
	static class DefaultThreadFactory implements ThreadFactory {
		private String prefix;
		private ThreadGroup g;
		private final AtomicInteger threadNumber = new AtomicInteger(0);
		private static final AtomicInteger poolCounter = new AtomicInteger(0);
		
		public DefaultThreadFactory(String prefix) {
			this.prefix = prefix + "-" + poolCounter.incrementAndGet();
			g = Thread.currentThread().getThreadGroup();
		}
		
		@Override
		public Thread createThread(Runnable r) {
			Thread t = new Thread(g, r, prefix + "-" + threadNumber.incrementAndGet());
			if (t.isDaemon())
				t.setDaemon(false);
			
			if (t.getPriority() != Thread.NORM_PRIORITY)
				t.setPriority(Thread.NORM_PRIORITY);
			
			return t;
		}
		
	}
}
