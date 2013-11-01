package nl.tudelft.cloud_computing_project.instance_allocation;


import nl.tudelft.cloud_computing_project.CloudOCR;
import nl.tudelft.cloud_computing_project.Monitor;
import nl.tudelft.cloud_computing_project.model.Database;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sql2o.Sql2o;
import org.sql2o.data.Table;

public class ProvisioningPolicyBasic implements ProvisioningPolicyInterface {
	
	private final String get_jobs_info	
			= "SELECT "
			+ "COUNT(*) AS availablejobs "
			+ "FROM Job "
			+ "WHERE "
			+ "Job.jobstatus = 1 ";
	
	private static final int MIN_NORMAL_INSTANCES 						= Integer.parseInt((String)CloudOCR.Configuration.get("MIN_NORMAL_INSTANCES"));
	private static final int AVG_EXECUTABLE_JOBS_PER_MACHINE_PER_HOUR 	= Integer.parseInt((String)CloudOCR.Configuration.get("AVG_EXECUTABLE_JOBS_PER_MACHINE_PER_HOUR"));
	private static Logger LOG = LoggerFactory.getLogger(ProvisioningPolicyBasic.class);
	private Sql2o sql2o;

	public int applyProvisioningPolicy() {
		
		int runningInstancesNum, openedSpotRequests;
		
		//get number of active Instances 
		runningInstancesNum = Monitor.getInstance().getNumRunningOrPendingInstances();
		openedSpotRequests = SpotInstancesAllocator.getInstance().getNumOpenedSpotInstancesRequests();
		
		//get Jobs Info from DB
		sql2o = Database.getConnection();
		Table jobsDBInfo = sql2o.createQuery(get_jobs_info, "get_jobs_info").executeAndFetchTable();

		int availableJobs = jobsDBInfo.rows().get(0).getInteger("availablejobs");
		
		//Optimal number of instances to process every job in the queue in the next hour.
		int optimalInstanceNumber = (int) Math.ceil((double)availableJobs/(double)AVG_EXECUTABLE_JOBS_PER_MACHINE_PER_HOUR);
		
		//Check on minimum active instances
		if (optimalInstanceNumber == 0)
			optimalInstanceNumber = MIN_NORMAL_INSTANCES;
		
		LOG.info("Number of available Jobs (running + pending): " + availableJobs);
		LOG.info("Optimal Instance Number: " + optimalInstanceNumber);
		LOG.info("Running Instance Number: " + runningInstancesNum);
		LOG.info("Opened Spot Requests Number: " + openedSpotRequests);
		
		if(runningInstancesNum == optimalInstanceNumber) {
			
			SpotInstancesAllocator.getInstance().cancelSpotInstancesRequests();
			return 0;
			
		} else if(runningInstancesNum > optimalInstanceNumber) {
			
			SpotInstancesAllocator.getInstance().cancelSpotInstancesRequests();
			return optimalInstanceNumber - runningInstancesNum;
			
		} else if(runningInstancesNum < optimalInstanceNumber) {
			
			int toBeAllocatedInstanceNumber = optimalInstanceNumber - runningInstancesNum;
			
			if(openedSpotRequests > toBeAllocatedInstanceNumber) {
				
				SpotInstancesAllocator.getInstance().cancelSpotInstancesRequests(openedSpotRequests - toBeAllocatedInstanceNumber);
				return 0;
				
			} else {
				
				return toBeAllocatedInstanceNumber - openedSpotRequests;
				
			}
			
		}
		
		return 0;
		
	}

}
