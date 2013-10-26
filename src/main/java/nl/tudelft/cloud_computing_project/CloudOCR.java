package nl.tudelft.cloud_computing_project;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CloudOCR {
	
	private static Logger LOG = LoggerFactory.getLogger(CloudOCR.class);
	
	// TODO: Make these configurable
	/**
	 * Number of milliseconds to wait between scheduling intervals
	 * Notice: The scheduler will wait this long after one cycle has completed.
	 */
	private static final long scheduler_interval 	= 10000;
	private static final long monitor_interval 		= 60000; /* 1 minute cycles*/
	private static final long failure_interval 		= 60000; /* 1 minute cycles*/
	private static final long allocation_interval 	= 60000;
	
	private static Thread SchedulerThread;
	private static Thread MonitorThread;
	private static Thread AllocationManagerThread;
	private static Thread FaultManagerThread;
	
	public static void main(String[] args) {
		LOG.info("Entering Cloud OCR!");
		
		// Thread that runs the Scheduler
		SchedulerThread = new Thread() {
			public void run(){
				Scheduler s = new Scheduler();
				while(true){
					try {
						s.schedule();
						Thread.sleep(scheduler_interval);
					} catch (InterruptedException e) {
						LOG.warn("SchedulerThread sleep was interrupted", e);
					}
				}
			}
		};
		// Start the scheduler
		SchedulerThread.run();
		
		// Thread that runs the Monitor
		MonitorThread = new Thread() {
			public void run(){
				Monitor m = Monitor.getInstance();
				while(true){
					try {
						m.monitorSystem();
						Thread.sleep(monitor_interval);
					} catch (InterruptedException e) {
						LOG.warn("MonitorThread sleep was interrupted", e);
					}
				}
			}
		};
		// Start the Monitor
		MonitorThread.run();
		
		// Thread that runs the FaultManager
		FaultManagerThread = new Thread() {
			public void run(){
				FaultManager fm = FaultManager.getInstance();
				while(true){
					try {
						fm.manageFailingJobs();
						Thread.sleep(failure_interval);
					} catch (InterruptedException e) {
						LOG.warn("FaultManagerThread sleep was interrupted", e);
					}
				}
			}
		};
		// Start the Fault Manager
		FaultManagerThread.run();
		
		// Thread that runs the AllocationManager
		AllocationManagerThread = new Thread() {
			public void run(){
				AllocationManager am = new AllocationManager();
				while(true){
					try {
						am.allocate();
						Thread.sleep(allocation_interval);
					} catch (InterruptedException e) {
						LOG.warn("AllocationManagerThread sleep was interrupted", e);
					}
				}
			}
		};
		// Start the Allocation Manager
		AllocationManagerThread.run();
	}

}
