package nl.tudelft.cloud_computing_project;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.ec2.AmazonEC2;

public class FaultManager {
	
	private static FaultManager	instance;
	private static AmazonEC2 	ec2;

	public static Logger LOG = LoggerFactory.getLogger(FaultManager.class);
	
	public static FaultManager getInstance(){
		if(instance == null) { instance = new FaultManager(); }
		return instance;
	}
	
	private FaultManager(){
		LOG.info("FaultManager Thread started succesfully.");
	}

	public void JobFailure(){
		
	}
	public void WorkerFailure(){
		
	}
	
}
