package nl.tudelft.cloud_computing_project;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import nl.tudelft.cloud_computing_project.instance_allocation.AllocationManager;
import nl.tudelft.cloud_computing_project.instance_allocation.SpotInstancesAllocator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.ClasspathPropertiesFileCredentialsProvider;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.DescribeInstanceStatusRequest;
import com.amazonaws.services.ec2.model.DescribeInstanceStatusResult;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.InstanceStatus;
import com.amazonaws.services.ec2.model.Reservation;
import com.amazonaws.services.ec2.model.Tag;


public class Monitor{

	private static Monitor 	instance;
	private static Logger 	LOG = LoggerFactory.getLogger(Monitor.class);
	private AmazonEC2 		ec2 = AmazonEC2Initializer.getInstance();
	private FaultManager 	faultManager;


	private Monitor(){
		faultManager = FaultManager.getInstance();
		initEC2Instance();
	}

	public static Monitor getInstance(){
		if(instance == null) { instance = new Monitor(); }
		return instance;
	}

	public int getNumRunningOrPendingInstances() {

		return getRunningOrPendingInstancesId().size();

	}

	public int getNumRunningOrPendingNormalInstances() {

		Set<String> runningOrPendingInstancesId = getRunningOrPendingInstancesId();

		try {

			DescribeInstancesResult describeInstancesRequest = ec2.describeInstances(new DescribeInstancesRequest().withInstanceIds(runningOrPendingInstancesId));
            List<Reservation> reservations = describeInstancesRequest.getReservations();
            
            for (Reservation reservation : reservations) {
				for (Instance instance : reservation.getInstances()) {
					for (Tag tag : instance.getTags()){
						if(tag.getKey().equals("cloudocr") && !tag.getValue().equals("worker")){
							runningOrPendingInstancesId.remove(instance.getInstanceId());
							break;
						}	
					}
	
				}
            }

		} catch (AmazonServiceException ase) {
			LOG.error("Caught Exception: " + ase.getMessage());
			LOG.error("Reponse Status Code: " + ase.getStatusCode());
			LOG.error("Error Code: " + ase.getErrorCode());
			LOG.error("Request ID: " + ase.getRequestId());
		}

		return runningOrPendingInstancesId.size();

	}
	
	public Set<String> getRunningOrPendingInstancesId() {
		Set<String> runningOrPendingInstancesId = new TreeSet<String>();

		try {
			//Retrieve instances status
			DescribeInstanceStatusResult describeInstanceResult = ec2.describeInstanceStatus(new DescribeInstanceStatusRequest());
			List<InstanceStatus> state = describeInstanceResult.getInstanceStatuses();

			for (InstanceStatus instanceStatusInfo : state){
				//Retrieve machine state (running, stopped, booting)
				String machineState = instanceStatusInfo.getInstanceState().getName();

				if(machineState.equalsIgnoreCase("running") || machineState.equalsIgnoreCase("pending")) {
					runningOrPendingInstancesId.add(instanceStatusInfo.getInstanceId());
				}
			}
			
			DescribeInstancesResult describeInstancesRequest = ec2.describeInstances(new DescribeInstancesRequest().withInstanceIds(runningOrPendingInstancesId));
            List<Reservation> reservations = describeInstancesRequest.getReservations();
			
            for (Reservation reservation : reservations) {
				for (Instance instance : reservation.getInstances()) {
					if(!instance.getTags().isEmpty()) {
						for (Tag tag : instance.getTags()){
							if(!tag.getKey().equals("cloudocr") || (tag.getKey().equals("cloudocr") && !(tag.getValue().equals("worker")))) {
								runningOrPendingInstancesId.remove(instance.getInstanceId());
								break;
							}
						}
					}
				}
            }
			
		} catch (AmazonServiceException ase) {
			LOG.error("Caught Exception: " + ase.getMessage());
			LOG.error("Reponse Status Code: " + ase.getStatusCode());
			LOG.error("Error Code: " + ase.getErrorCode());
			LOG.error("Request ID: " + ase.getRequestId());
		}

		return runningOrPendingInstancesId;

	}

	public Set<String> getAvailableInstancesId() {
		Set<String> availableInstancesId = new TreeSet<String>();

		try {
			//Retrieve instances status
			DescribeInstanceStatusResult describeInstanceResult = ec2.describeInstanceStatus(new DescribeInstanceStatusRequest());
			List<InstanceStatus> state = describeInstanceResult.getInstanceStatuses();

			for (InstanceStatus instanceStatusInfo : state){
				//Retrieve machine state (running, stopped, booting)
				String machineState = instanceStatusInfo.getInstanceState().getName();

				if(machineState.equalsIgnoreCase("running")) {
					//Retrieve status info
					String instanceStatus = instanceStatusInfo.getInstanceStatus().getStatus();
					String systemStatus = instanceStatusInfo.getSystemStatus().getStatus();

					if(instanceStatus.equalsIgnoreCase("ok") && systemStatus.equalsIgnoreCase("ok")) {
						availableInstancesId.add(instanceStatusInfo.getInstanceId());
					}	
				}
			}
			
			DescribeInstancesResult describeInstancesRequest = ec2.describeInstances(new DescribeInstancesRequest().withInstanceIds(availableInstancesId));
            List<Reservation> reservations = describeInstancesRequest.getReservations();
			
            for (Reservation reservation : reservations) {
				for (Instance instance : reservation.getInstances()) {
					if(!instance.getTags().isEmpty()) {
						for (Tag tag : instance.getTags()){
							if(!tag.getKey().equals("cloudocr") || (tag.getKey().equals("cloudocr") && !(tag.getValue().equals("worker")))) {
								availableInstancesId.remove(instance.getInstanceId());
								break;
							}
						}
					}
				}
            }
			
		} catch (AmazonServiceException ase) {
			LOG.error("Caught Exception: " + ase.getMessage());
			LOG.error("Reponse Status Code: " + ase.getStatusCode());
			LOG.error("Error Code: " + ase.getErrorCode());
			LOG.error("Request ID: " + ase.getRequestId());
		}

		return availableInstancesId;

	}

	public void monitorSystem(){
		
		LOG.info("Monitoring System..");

		try {
			
			//UPDATE status on Spot Requests
			SpotInstancesAllocator.getInstance().monitorSpotInstancesRequests();
			
			//UPDATE protected instances
			AllocationManager.getInstance().updateProtectedInstances();

			//CHECK for failures
			DescribeInstanceStatusResult describeInstanceResult = ec2.describeInstanceStatus(new DescribeInstanceStatusRequest());
			List<InstanceStatus> state = describeInstanceResult.getInstanceStatuses();

			for (InstanceStatus instanceStatusInfo : state){
				//Retrieve machine state (running, stopped, booting)
				String machineState = instanceStatusInfo.getInstanceState().getName();

				if(machineState.equalsIgnoreCase("running")) {
					//Retrieve status info
					String instanceStatus = instanceStatusInfo.getInstanceStatus().getStatus();
					String systemStatus = instanceStatusInfo.getSystemStatus().getStatus();

					//Call Fault Manager to handle failure
					if(!instanceStatus.equalsIgnoreCase("ok") || !systemStatus.equalsIgnoreCase("ok")) {
						
						//Check if the machine is protected
						if(AllocationManager.getInstance().getProtectedInstance().containsKey(instanceStatusInfo.getInstanceId()))
							continue;
							
						LOG.info("Found a failing machine: " + instanceStatusInfo.getInstanceId());
						
						//Run Thread that deals with the failed machine
						FaultManagerThread faultManagerThread = new FaultManagerThread(faultManager, instanceStatusInfo.getInstanceId());
						faultManagerThread.start();
						
					}	
				}
			}
		} catch (AmazonServiceException ase) {
			LOG.error("Caught Exception: " + ase.getMessage());
			LOG.error("Reponse Status Code: " + ase.getStatusCode());
			LOG.error("Error Code: " + ase.getErrorCode());
			LOG.error("Request ID: " + ase.getRequestId());
		}


	}

	/**
	 * This method initializes the ec2 instance with the correct parameters.
	 */
	private void initEC2Instance() {
		try {
			//Init ec2 instance
			AWSCredentialsProvider credentialsProvider = new ClasspathPropertiesFileCredentialsProvider();
			ec2 = new AmazonEC2Client(credentialsProvider);
			ec2.setEndpoint("ec2.eu-west-1.amazonaws.com");
		}
		catch (Exception e) {
			LOG.error(e.getMessage());
		}
	}
	
	/**
	 * This class is used to thread upon the discovery of a failed machine.
	 */
	private class FaultManagerThread extends Thread {
		private FaultManager fm;
		private String instanceId;

		public FaultManagerThread (FaultManager fm, String instanceId) {
			this.fm = fm;
			this.instanceId = instanceId;
		}

		public void run(){
			fm.WorkerFailure(instanceId);
		}
	}

}
