package nl.tudelft.cloud_computing_project;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.ClasspathPropertiesFileCredentialsProvider;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.Reservation;


public class Monitor{
	
	private static Monitor		instance;
	private static AmazonEC2 	ec2;

	public static Logger LOG = LoggerFactory.getLogger(Monitor.class);
	
	public static Monitor getInstance(){
		if(instance == null) { instance = new Monitor(); }
		return instance;
	}
	
	private Monitor(){
		LOG.info("Monitor Thread started succesfully.");
	}
	
	public void monitorSystem(){

		 init();

		 try {
			 DescribeInstancesResult describeInstancesRequest = ec2.describeInstances();
			 List<Reservation> reservations = describeInstancesRequest.getReservations();
			 Set<Instance> instances = new HashSet<Instance>();

			 for (Reservation reservation : reservations) {
				 instances.addAll(reservation.getInstances());
			 }

			 LOG.debug("You have " + instances.size() + " Amazon EC2 instance(s) running.");
		 } catch (AmazonServiceException ase) {
			 LOG.error("Caught Exception: " + ase.getMessage());
			 LOG.error("Reponse Status Code: " + ase.getStatusCode());
			 LOG.error("Error Code: " + ase.getErrorCode());
			 LOG.error("Request ID: " + ase.getRequestId());
		 }

			
	}
	
	private void init() {
		try {
			AWSCredentialsProvider credentialsProvider = new ClasspathPropertiesFileCredentialsProvider();
			ec2 = new AmazonEC2Client(credentialsProvider);
			ec2.setEndpoint("ec2.eu-west-1.amazonaws.com");
		}
		catch (Exception e) {
			LOG.error(e.getMessage());
		}
	}
	
}
