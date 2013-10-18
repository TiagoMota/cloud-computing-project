package nl.tudelft.cloud_computing_project;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CloudOCR {
	
	public static Logger MAINLOG = LoggerFactory.getLogger(CloudOCR.class);
	
	private static Thread SchedulerThread;
	private static Thread MonitorThread;
	private static Thread AllocationManagerThread;
	
	
	public static void main(String[] args) {
		MAINLOG.info("Entering Cloud OCR!");
		
		
		MAINLOG.error("Boo");
		
		// Thread that runs the scheduler every 5s
		SchedulerThread = new Thread() {
			public void run(){
				Scheduler s = new Scheduler();
				while(true){
					try {
						s.schedule();
						Thread.sleep(5000);
					} catch (InterruptedException e) {
						MAINLOG.warn("SchedulerThread was interrupted", e);
						e.printStackTrace();
					}
				}
			}
		};
		// Start the scheduler
		SchedulerThread.run();
		
		MonitorThread = new Thread() {
			public void run(){
				Monitor m = new Monitor();
				while(true){
					try {
						m.monitorSystem();
						Thread.sleep(5000);
					} catch (InterruptedException e) {
						MAINLOG.warn("MonitorThread was interrupted", e);
						e.printStackTrace();
					}
				}
			}
		};
		// Start the Monitor
		MonitorThread.run();
		
		AllocationManagerThread = new Thread() {
			public void run(){
				AllocationManager am = new AllocationManager();
				while(true){
					try {
						am.allocate();
						Thread.sleep(5000);
					} catch (InterruptedException e) {
						MAINLOG.warn("AllocationManagerThread was interrupted", e);
						e.printStackTrace();
					}
				}
			}
		};
		// Start the Allocation Manager
		AllocationManagerThread.run();
	}

}
