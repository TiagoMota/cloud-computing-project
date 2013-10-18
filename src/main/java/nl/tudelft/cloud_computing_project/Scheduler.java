package nl.tudelft.cloud_computing_project;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Scheduler {
	private static Logger LOG = LoggerFactory.getLogger(Scheduler.class);
	
	public Scheduler(){
	}
	
	/**
	 * Assigns jobs from the database queue to the current workers
	 */
	public void schedule() {
		// TODO: Implement scheduler
		LOG.info("Now scheduling");
	}
}
