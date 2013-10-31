package nl.tudelft.cloud_computing_project;


import java.util.List;

import nl.tudelft.cloud_computing_project.model.Database;

import org.sql2o.Sql2o;

public class ProvisioningPolicyBasic implements ProvisioningPolicyInterface {
	
	private final String get_jobs_info	
			= "SELECT "
			+ "COUNT(*) AS totaljobs,"
			+ "SUM(filesize) AS totaljobsize,"
			+ "SUM(CASE when Assignment.job_id IS NOT NULL THEN 1 ELSE 0 end) AS assignedjobs,"
			+ "SUM(CASE when Assignment.job_id IS NOT NULL THEN filesize ELSE 0 end) AS assignedjobssize,"
			+ "SUM(CASE when Assignment.job_id IS NULL THEN 1 ELSE 0 end) AS unassignedjobs,"
			+ "SUM(CASE when Assignment.job_id IS NULL THEN filesize ELSE 0 end) AS unassignedjobsize"
			+ "FROM Job"
			+ "LEFT JOIN Assignment ON Assignment.job_id = Job.id"
			+ "WHERE"
			+ "Job.jobstatus = 1";
	
	private static final int MIN_NORMAL_INSTANCES 						= Integer.parseInt((String)CloudOCR.Configuration.get("MIN_NORMAL_INSTANCES"));
	private static final int AVG_EXECUTABLE_JOBS_PER_MACHINE_PER_HOUR 	= Integer.parseInt((String)CloudOCR.Configuration.get("AVG_EXECUTABLE_JOBS_PER_MACHINE_PER_HOUR"));
	private Sql2o sql2o;

	//TODO many things are not used here, to be revised.
	public int applyProvisioningPolicy() {
		
		int runningInstancesNum;
		JobsInfo jobsInfo;
		
		//get number of active Instances 
		runningInstancesNum = Monitor.getInstance().getNumAvailableInstances();
		
		//get Jobs Info from DB
		sql2o = Database.getConnection();
		List<Integer> jobsDBInfo = sql2o.createQuery(get_jobs_info).executeAndFetch(JobsInfo.class);
		jobsInfo = new JobsInfo(jobsDBInfo);

		//Optimal number of instances to process every job in the queue in the next hour.
		int optimalInstanceNumber = (int) Math.ceil((double)jobsInfo.unassignedJobs/(double)AVG_EXECUTABLE_JOBS_PER_MACHINE_PER_HOUR);
		
		//Check on minimum active instances
		if (optimalInstanceNumber == 0)
			optimalInstanceNumber = MIN_NORMAL_INSTANCES;
		
		return optimalInstanceNumber - runningInstancesNum;
		
	}
	
	private class JobsInfo {
		int totalJobs;
		int totalJobsSize;
		int assignedJobs;
		int assignedJobsSize;
		int unassignedJobs;
		int unassignedJobsize;
		
		public JobsInfo (List<Integer> jobsInfo) {
			 totalJobs 			= jobsInfo.get(0);
			 totalJobsSize 		= jobsInfo.get(1);
			 assignedJobs 		= jobsInfo.get(2);
			 assignedJobsSize 	= jobsInfo.get(3);
			 unassignedJobs 	= jobsInfo.get(4);
			 unassignedJobsize 	= jobsInfo.get(5);
		}
	}

}
