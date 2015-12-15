package in.javawithgaurav.blogspot.threadpool;

/**
 * 
 * @author Gaurav Rai Mazra
 *
 */
public interface ThreadPool {
	public void execute(Runnable task);
	public void shutDown();
}
