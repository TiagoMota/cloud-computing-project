package nl.tudelft.cloud_computing_project;

import nl.tudelft.cloud_computing_project.model.Database;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sql2o.Sql2o;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.model.StopInstancesRequest;
import com.amazonaws.services.ec2.model.StopInstancesResult;

public class FaultManager {
	
	private static FaultManager	instance;
	private static AmazonEC2 	ec2;
	private static Logger LOG = LoggerFactory.getLogger(FaultManager.class);
	private Sql2o sql2o;
	
	private final String delete_instance_assignment_sql 	= "DELETE * "
															+ "FROM Assignment "
															+ "WHERE worker_instanceid = :instanceId";
	
	private final String update_job_num_failures_sql 		= "UPDATE Job "
															+ "JOIN Assignment ON Assignment.job_id = Job.id"
															+ "SET num_failures = num_failures + 1"
															+ "WHERE Assignment.worker_instanceid = :instanceId";
	
	
	private FaultManager(){}
	
	public static FaultManager getInstance(){
		if(instance == null) { instance = new FaultManager(); }
		return instance;
	}
	
	
	public void WorkerFailure(String instanceId){
		try {
			
			//Stops the failing machine and logs the information
			StopInstancesRequest stopRequest = new StopInstancesRequest().withInstanceIds(instanceId);
			StopInstancesResult stopResult = ec2.stopInstances(stopRequest);
			LOG.warn("Detected Failing Machine: " + stopResult.toString());
			
			sql2o = Database.getConnection();
			
			//Increase Job number of failures in DB table
			sql2o.createQuery(update_job_num_failures_sql, "update_job_num_failures_sql").addParameter("instanceId", instanceId).executeUpdate();

			//Delete Assignments related to the machine in DB table
			sql2o.createQuery(delete_instance_assignment_sql, "delete_instance_assignment_sql").addParameter("instanceId", instanceId).executeUpdate();
			
			
		} catch (AmazonServiceException ase) {
			 LOG.error("Caught Exception: " + ase.getMessage());
			 LOG.error("Reponse Status Code: " + ase.getStatusCode());
			 LOG.error("Error Code: " + ase.getErrorCode());
			 LOG.error("Request ID: " + ase.getRequestId());
		 }
	}

	public void manageFailingJobs() {
		// TODO Auto-generated method stub
		
	}
	
}
