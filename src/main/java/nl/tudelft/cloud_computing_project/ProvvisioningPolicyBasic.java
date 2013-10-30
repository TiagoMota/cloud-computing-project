package nl.tudelft.cloud_computing_project;

import org.sql2o.Sql2o;

public class ProvvisioningPolicyBasic implements ProvvisioningPolicy {
	
	private static final int MAX_INSTANCE_JOBS 		= Integer.parseInt((String)CloudOCR.Configuration.get("MAX_INSTANCE_JOBS"));;
	private static final int ALLOCATION_THRESHOLD 	= Integer.parseInt((String)CloudOCR.Configuration.get("ALLOCATION_THRESHOLD"));;
	
	private Sql2o sql2o;
		
	public void applyProvvisioningPolicy() {
		
		int runningInstancesNum;
		int pendingJobsNum;
		int activeJobsNum;
		
		//get num of instances 
		runningInstancesNum = Monitor.getInstance().getNumAvailableInstances();
		
		//get num of pending jobs
		//get num of active jobs

		/** MACHINE ALLOCATION: either the job number is over a certain threshold*/
		//if num_jobs > instances * max_instance_job - gap_threshold
		//allocate 1 vm (if num_vm < max_vm)

		/** or because there are too many jobs pending*/
		//if num_pending >

		/** MACHINE DISPOSAL: running jobs + queue jobs are less than a threshold(function of max vm capacity) */
	}

}
