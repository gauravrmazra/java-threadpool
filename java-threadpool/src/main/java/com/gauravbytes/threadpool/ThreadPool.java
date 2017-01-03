package com.gauravbytes.threadpool;

/**
 * 
 * @author Gaurav Rai Mazra
 *
 */
public interface ThreadPool {
	public void execute(Runnable task);
	public void shutDown();
}
