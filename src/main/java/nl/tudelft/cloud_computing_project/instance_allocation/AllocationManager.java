package nl.tudelft.cloud_computing_project.instance_allocation;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

import nl.tudelft.cloud_computing_project.AmazonEC2Initializer;
import nl.tudelft.cloud_computing_project.CloudOCR;
import nl.tudelft.cloud_computing_project.FaultManager;
import nl.tudelft.cloud_computing_project.Monitor;
import nl.tudelft.cloud_computing_project.model.Database;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sql2o.Sql2o;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.InstanceStateChange;
import com.amazonaws.services.ec2.model.Reservation;
import com.amazonaws.services.ec2.model.StartInstancesRequest;
import com.amazonaws.services.ec2.model.StartInstancesResult;
import com.amazonaws.services.ec2.model.Tag;
import com.amazonaws.services.ec2.model.TerminateInstancesRequest;
import com.amazonaws.services.ec2.model.TerminateInstancesResult;

public class AllocationManager {
	
	private final String delete_instance_assignment_sql 	
			= "DELETE * "
			+ "FROM Assignment "
			+ "WHERE worker_instanceid = :instanceId";
	
	private static final String PROVVISIONING_POLICY_CLASS = (String)CloudOCR.Configuration.get("PROVVISIONING_POLICY_CLASS");
	private static final int MAX_NORMAL_INSTANCES = Integer.parseInt((String)CloudOCR.Configuration.get("MAX_NORMAL_INSTANCES"));
	private static Logger LOG = LoggerFactory.getLogger(FaultManager.class);
	private static AllocationManager instance;
	private AmazonEC2 ec2 = AmazonEC2Initializer.getInstance();
	private Sql2o sql2o;
	private ProvisioningPolicyInterface provisioningPolicy;

	private AllocationManager(){
		try {
			 provisioningPolicy = (ProvisioningPolicyInterface) Class.forName(PROVVISIONING_POLICY_CLASS).newInstance();
		} catch (Exception e) {
			LOG.error("Error instantiating ProvisioningPolicy class:\n" + e.getMessage());
		}
	}
	
	public static AllocationManager getInstance(){
		if(instance == null) { instance = new AllocationManager(); }
		return instance;
	}
	
	
	public void applyProvvisioningPolicy() {
		
		LOG.info("Applying provisioning policy");
		int provisioningPolicyResult = provisioningPolicy.applyProvisioningPolicy();
		LOG.info(provisioningPolicyResult + " instances will be (un)allocated");

		
		// INSTANCE ALLOCATION 
		if(provisioningPolicyResult > 0)
			allocateMachines(provisioningPolicyResult);
	
		// INSTANCE DEALLOCATION 
		else if (provisioningPolicyResult < 0) {
			int result = deallocateMachines(Math.abs(provisioningPolicyResult), true);
			if(result < Math.abs(provisioningPolicyResult))
				LOG.error("Error in deallocating machines");
		}

		
	}
	
	private void allocateMachines(int instancesToAllocate) {
		
		int normalInstancesRunning = Monitor.getInstance().getNumRunningNormalInstances();
		if (normalInstancesRunning < MAX_NORMAL_INSTANCES){
			int allocatedNormalInstances = allocateNormalInstances(MAX_NORMAL_INSTANCES - normalInstancesRunning);
			instancesToAllocate -= (allocatedNormalInstances);
			LOG.info("Allocated " + allocatedNormalInstances + " default instances");
		}
		else {
			Thread SpotInstancesThread = new SpotInstancesThread (instancesToAllocate);
			SpotInstancesThread.run();
		}
		
	}

	private int allocateNormalInstances(int instancesToAllocate) {
		int startedInstances = 0;
		
		try {
			for (startedInstances = 0; startedInstances < instancesToAllocate; startedInstances++) {
				StartInstancesRequest startRequest = new StartInstancesRequest();
			    StartInstancesResult startResult = ec2.startInstances(startRequest);
			    List<InstanceStateChange> stateChangeList = startResult.getStartingInstances();
			    
			    for (InstanceStateChange instanceStateChange : stateChangeList) {
			    	LOG.info("Starting instance '" + instanceStateChange.getInstanceId() + "':");
			    }
			}
		} catch (AmazonServiceException ase) {
			LOG.error("Caught Exception: " + ase.getMessage());
			LOG.error("Reponse Status Code: " + ase.getStatusCode());
			LOG.error("Error Code: " + ase.getErrorCode());
			LOG.error("Request ID: " + ase.getRequestId());
		}
		
		return startedInstances;
	}

	private int deallocateMachines(int instancesToTerminateNum, boolean onlySpotInstances) {
		
		int terminatedInstancesCount = 0;
		int launchedMinutes, currentMinutes, minutesToCharging;
		String instanceId, instanceIdToBeRemoved;
		Date launchTime;
		Date currentTime = new Date();
		Calendar calendar = Calendar.getInstance();
		TreeMap<Integer, List<String>> orderedInstances = new TreeMap<Integer, List<String>>(); 
		
		
		try {

			//Cancel spot instance requests
			SpotInstancesAllocator.getInstance().cancelSpotInstancesRequests();

			
			//Retrieve the list of instances from EC2
			DescribeInstancesResult describeInstancesRequest = ec2.describeInstances();
			List<Reservation> reservations = describeInstancesRequest.getReservations();
			Set<Instance> instances = new HashSet<Instance>();

			for (Reservation reservation : reservations) 
				instances.addAll(reservation.getInstances());

			//Decide which instances to terminate
			for (Instance instance : instances) {
				
				//Only deallocates spot Instances
				if (!instance.getTags().contains(new Tag("cloudocr", "spotinstance")))
					if (onlySpotInstances || instance.getTags().contains(new Tag("cloudocr", "master")))
						continue;
				
				//Retrieves the launchTime of the instance
				launchTime = instance.getLaunchTime();
				
				calendar.setTime(launchTime);
				launchedMinutes = calendar.get(Calendar.MINUTE);

				calendar.setTime(currentTime);
				currentMinutes = calendar.get(Calendar.MINUTE);

				//Calculates the minutes to charging for the instance
				minutesToCharging = launchedMinutes - currentMinutes;
				if (minutesToCharging < 0) 
					minutesToCharging += 60;
				
				instanceId = instance.getInstanceId();
				
				//Inserts the pair in a ordered map
				Integer keyMinutes = new Integer(minutesToCharging);
			
				List<String> tempList = orderedInstances.get(keyMinutes);
				tempList = (tempList == null? new ArrayList<String>() : tempList);
				tempList.add(instanceId);
				
				orderedInstances.put(keyMinutes, tempList);

			}
			
			//This loop scans the ordered map and issues the instances terminations.
			while (!orderedInstances.isEmpty() && terminatedInstancesCount < instancesToTerminateNum) {
				
				//Determinate which instance to terminate
				int firstKey = orderedInstances.firstKey();
				List<String> instanceList = orderedInstances.get(firstKey);
				
				while (!instanceList.isEmpty() && terminatedInstancesCount < instancesToTerminateNum) {
					
					instanceIdToBeRemoved = instanceList.get(0);
					
					//Terminates the required instances and logs the information
					TerminateInstancesRequest terminateRequest = new TerminateInstancesRequest().withInstanceIds(instanceIdToBeRemoved);
					TerminateInstancesResult terminateResult = ec2.terminateInstances(terminateRequest);
					LOG.warn("Allocation Manager stopped Instance: " + terminateResult.toString());

					sql2o = Database.getConnection();
					sql2o.createQuery(delete_instance_assignment_sql, "delete_instance_assignment_sql").addParameter("instanceId", instanceIdToBeRemoved).executeUpdate();
					
					instanceList.remove(instanceIdToBeRemoved);
					terminatedInstancesCount++;
					
				}
				
				//Removes the key from the map and continues
				orderedInstances.remove(firstKey);
				
			}
			
		} catch (AmazonServiceException ase) {
			LOG.error("Caught Exception: " + ase.getMessage());
			LOG.error("Reponse Status Code: " + ase.getStatusCode());
			LOG.error("Error Code: " + ase.getErrorCode());
			LOG.error("Request ID: " + ase.getRequestId());
		}
		
		if (terminatedInstancesCount < instancesToTerminateNum)
			return deallocateMachines(instancesToTerminateNum - terminatedInstancesCount, false);
		
		LOG.info("Deallocated " + terminatedInstancesCount + " default instances");
		return terminatedInstancesCount;


	}
	
	/**
	 * This class is used to thread upon the allocation of spot instances.
	 */
	private class SpotInstancesThread extends Thread {
		private int instancesToAllocate;
		
		public SpotInstancesThread (int instancesToAllocate) {
			this.instancesToAllocate = instancesToAllocate;
		}
		
		public void run(){
			SpotInstancesAllocator.getInstance().requestSpotInstances(instancesToAllocate);
		}
	}
}
