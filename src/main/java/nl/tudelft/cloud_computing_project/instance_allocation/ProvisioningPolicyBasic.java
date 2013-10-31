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
			+ "COUNT(*) AS unassignedjobs, "
			+ "SUM(filesize) AS unassignedjobsize "
			+ "FROM Job "
			+ "LEFT JOIN Assignment ON Assignment.job_id = Job.id "
			+ "WHERE "
			+ "Job.jobstatus = 1 AND Assignment.job_id IS NULL";
	
	private static final int MIN_NORMAL_INSTANCES 						= Integer.parseInt((String)CloudOCR.Configuration.get("MIN_NORMAL_INSTANCES"));
	private static final int AVG_EXECUTABLE_JOBS_PER_MACHINE_PER_HOUR 	= Integer.parseInt((String)CloudOCR.Configuration.get("AVG_EXECUTABLE_JOBS_PER_MACHINE_PER_HOUR"));
	private static Logger LOG = LoggerFactory.getLogger(ProvisioningPolicyBasic.class);
	private Sql2o sql2o;

	public int applyProvisioningPolicy() {
		
		int runningInstancesNum;
		
		//get number of active Instances 
		runningInstancesNum = Monitor.getInstance().getNumRunningInstances();
		
		//get Jobs Info from DB
		sql2o = Database.getConnection();
		Table jobsDBInfo = sql2o.createQuery(get_jobs_info).executeAndFetchTable();

		int unassignedJobs = jobsDBInfo.rows().get(0).getInteger("unassignedjobs");
		
		//Optimal number of instances to process every job in the queue in the next hour.
		int optimalInstanceNumber = (int) Math.ceil((double)unassignedJobs/(double)AVG_EXECUTABLE_JOBS_PER_MACHINE_PER_HOUR);
		
		//Check on minimum active instances
		if (optimalInstanceNumber == 0)
			optimalInstanceNumber = MIN_NORMAL_INSTANCES;
		
		LOG.info("Number of unassigned Jobs: " + unassignedJobs);
		LOG.info("Optimal Instance Number: " + optimalInstanceNumber);
		LOG.info("Running Instance Number: " + runningInstancesNum);
		
		return optimalInstanceNumber - runningInstancesNum;
		
	}

}
