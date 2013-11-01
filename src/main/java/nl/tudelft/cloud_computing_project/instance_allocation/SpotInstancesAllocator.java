package nl.tudelft.cloud_computing_project.instance_allocation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import nl.tudelft.cloud_computing_project.AmazonEC2Initializer;
import nl.tudelft.cloud_computing_project.CloudOCR;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.model.CancelSpotInstanceRequestsRequest;
import com.amazonaws.services.ec2.model.CancelSpotInstanceRequestsResult;
import com.amazonaws.services.ec2.model.CancelledSpotInstanceRequest;
import com.amazonaws.services.ec2.model.CreateTagsRequest;
import com.amazonaws.services.ec2.model.DescribeSpotInstanceRequestsRequest;
import com.amazonaws.services.ec2.model.DescribeSpotInstanceRequestsResult;
import com.amazonaws.services.ec2.model.LaunchSpecification;
import com.amazonaws.services.ec2.model.RequestSpotInstancesRequest;
import com.amazonaws.services.ec2.model.RequestSpotInstancesResult;
import com.amazonaws.services.ec2.model.SpotInstanceRequest;
import com.amazonaws.services.ec2.model.Tag;

public class SpotInstancesAllocator {

	private static final String SPOT_PRICE = (String)CloudOCR.Configuration.get("SPOT_PRICE");
	private static Base64 base64 = new Base64();
	private static String WORKER_SCRIPT = new String(base64.encode("#!/bin/bash\njava -jar /home/ubuntu/Worker/worker.jar\nexit 0".getBytes()));
	private static Logger LOG = LoggerFactory.getLogger(SpotInstancesAllocator.class);
	private static SpotInstancesAllocator instance;
	private static List<String> spotInstanceRequestIds = Collections.synchronizedList(new ArrayList<String>());
	private AmazonEC2 ec2 = AmazonEC2Initializer.getInstance();

	private SpotInstancesAllocator(){}

	public static SpotInstancesAllocator getInstance() {
		if(instance == null) instance = new SpotInstancesAllocator();
		return instance;
	}
	
	public void requestSpotInstances (int instancesToAllocate) {
		
		if(instancesToAllocate == 0) return;
		
		// Create Request for spot instance
		RequestSpotInstancesRequest requestRequest = new RequestSpotInstancesRequest();
		requestRequest.setSpotPrice(SPOT_PRICE);
		requestRequest.setInstanceCount(instancesToAllocate);
		LaunchSpecification launchSpecification = new LaunchSpecification();
		launchSpecification.setImageId("ami-836685f4");
		launchSpecification.setInstanceType("t1.micro");
		launchSpecification.setUserData(WORKER_SCRIPT);
		ArrayList<String> securityGroups = new ArrayList<String>();
		securityGroups.add("cloudocr-worker");
		launchSpecification.setSecurityGroups(securityGroups);
		requestRequest.setLaunchSpecification(launchSpecification);

		// Call the RequestSpotInstance API.
		RequestSpotInstancesResult requestResult = ec2.requestSpotInstances(requestRequest);
		List<SpotInstanceRequest> requestResponses = requestResult.getSpotInstanceRequests();

		for (SpotInstanceRequest requestResponse : requestResponses) {
			LOG.info("Created Spot Request: "+requestResponse.getSpotInstanceRequestId());
			addToSpotInstanceRequestIds(requestResponse.getSpotInstanceRequestId());
		}

	}
	
	public synchronized void addToSpotInstanceRequestIds(String requestId) {
		spotInstanceRequestIds.add(requestId);
	}
	
	public synchronized void removeFromSpotInstanceRequestIds(String requestId) {
		spotInstanceRequestIds.remove(requestId);
	}

	public void monitorSpotInstancesRequests() {

		if(spotInstanceRequestIds.isEmpty())
			return;
		
		// Retrieve all of the requests we want to monitor.
		DescribeSpotInstanceRequestsRequest describeRequest = new DescribeSpotInstanceRequestsRequest();
		DescribeSpotInstanceRequestsResult describeResult = ec2.describeSpotInstanceRequests(describeRequest);
		List<SpotInstanceRequest> describeResponses = describeResult.getSpotInstanceRequests();
		
		//Exit if there are no active requests
		if(describeResponses == null || describeResponses.size() == 0) 
			return;
		
		for(SpotInstanceRequest describeResponse : describeResponses) {
			
			if(describeResponse.getState().equals("active")) {
				
				String instanceId = describeResponse.getInstanceId();
				AllocationManager am = AllocationManager.getInstance();
				Map<String, Date> protectedInstances = am.getProtectedInstance();
				long startTime = describeResponse.getCreateTime().getTime();
				long currentTime = new Date().getTime();
				
				LOG.info("Updating spot instances information: " + instanceId);
				
				if(!protectedInstances.containsKey(instanceId) && (startTime > (currentTime - (5 * 60000)))) {
					AllocationManager.getInstance().setProtectedInstance(instanceId);
					removeFromSpotInstanceRequestIds(instanceId);
				}
				
				//LOG.info("Spot Requests: 1 spot instance created");
			}
			
		}
		
	}
	
	//UNUSED
	private void tagInstance(String instanceId) {
		// Tag the created instances
		Tag tag = new Tag().withKey("cloudocr").withValue("spotinstance");

		CreateTagsRequest createTagsRequest = new CreateTagsRequest();
		createTagsRequest.withResources(instanceId).withTags(tag);

		ec2.createTags(createTagsRequest);

	}

	public void cancelSpotInstancesRequests() {
		try {
			
			if(spotInstanceRequestIds.isEmpty())
				return;
			
			DescribeSpotInstanceRequestsRequest spotInstanceRequestsRequest = new DescribeSpotInstanceRequestsRequest();
			DescribeSpotInstanceRequestsResult spotInstanceRequestsResult = ec2.describeSpotInstanceRequests(spotInstanceRequestsRequest);
			List<SpotInstanceRequest> requestList = spotInstanceRequestsResult.getSpotInstanceRequests();
			
			ArrayList<String> deleteSpotInstanceRequestIds = new ArrayList<String>();
			int openedSpotInstanceRequestsNumber = 0;
			
			for (SpotInstanceRequest request : requestList) {
				if(request.getState().equals("open"))
					openedSpotInstanceRequestsNumber++;
				removeFromSpotInstanceRequestIds(request.getSpotInstanceRequestId());
				deleteSpotInstanceRequestIds.add(request.getSpotInstanceRequestId());
			}
			
			//Cancel requests.
			CancelSpotInstanceRequestsRequest cancelRequest = new CancelSpotInstanceRequestsRequest(deleteSpotInstanceRequestIds);
			CancelSpotInstanceRequestsResult cancelResult = ec2.cancelSpotInstanceRequests(cancelRequest);
			List<CancelledSpotInstanceRequest> cancelledRequests = cancelResult.getCancelledSpotInstanceRequests(); 

			LOG.info("Cancelled " + cancelledRequests.size() + " Spot Instance Requests ("+ openedSpotInstanceRequestsNumber +" opened)");

		} catch (AmazonServiceException e) {
			LOG.warn("Error canceling instances");
			LOG.warn("Caught Exception: " + e.getMessage());
			LOG.warn("Reponse Status Code: " + e.getStatusCode());
			LOG.warn("Error Code: " + e.getErrorCode());
			LOG.warn("Request ID: " + e.getRequestId());
		}
	}
	
	public void cancelSpotInstancesRequests(int requestsToBeDeleted) {
		try {
			
			if(spotInstanceRequestIds.isEmpty())
				return;
			
			DescribeSpotInstanceRequestsRequest spotInstanceRequestsRequest = new DescribeSpotInstanceRequestsRequest();
			DescribeSpotInstanceRequestsResult spotInstanceRequestsResult = ec2.describeSpotInstanceRequests(spotInstanceRequestsRequest);
			List<SpotInstanceRequest> requestList = spotInstanceRequestsResult.getSpotInstanceRequests();
			
			ArrayList<String> deleteSpotInstanceRequestIds = new ArrayList<String>();
			int openedSpotInstanceRequestsNumber = 0;
			
			int i = 0;
			while (i < requestsToBeDeleted) {
				if(requestList.get(i).getState().equals("open")) {
					i++;
					openedSpotInstanceRequestsNumber++;
					String idToBeRemoved = requestList.get(i).getSpotInstanceRequestId();
					removeFromSpotInstanceRequestIds(idToBeRemoved);
					deleteSpotInstanceRequestIds.add(idToBeRemoved);
				}
			}
			
			//Cancel requests.
			CancelSpotInstanceRequestsRequest cancelRequest = new CancelSpotInstanceRequestsRequest(deleteSpotInstanceRequestIds);
			CancelSpotInstanceRequestsResult cancelResult = ec2.cancelSpotInstanceRequests(cancelRequest);
			List<CancelledSpotInstanceRequest> cancelledRequests = cancelResult.getCancelledSpotInstanceRequests(); 

			LOG.info("Deleted " + cancelledRequests.size() + "Spot Instance Requests ("+ openedSpotInstanceRequestsNumber +" opened)");

		} catch (AmazonServiceException e) {
			LOG.warn("Error canceling instances");
			LOG.warn("Caught Exception: " + e.getMessage());
			LOG.warn("Reponse Status Code: " + e.getStatusCode());
			LOG.warn("Error Code: " + e.getErrorCode());
			LOG.warn("Request ID: " + e.getRequestId());
		}
	}

	public int getNumOpenedSpotInstancesRequests() {
		
		int openedSpotInstancesRequests = 0;
		int skippedRequests = 0;
		
		// Retrieve all of the requests
		if(spotInstanceRequestIds.isEmpty())
			return openedSpotInstancesRequests;
		
		DescribeSpotInstanceRequestsRequest describeRequest = new DescribeSpotInstanceRequestsRequest();
		DescribeSpotInstanceRequestsResult describeResult = ec2.describeSpotInstanceRequests(describeRequest);
		List<SpotInstanceRequest> describeResponses = describeResult.getSpotInstanceRequests();
		
		for (SpotInstanceRequest describeResponse : describeResponses) {
			if(describeResponse.getState().equals("open"))
				openedSpotInstancesRequests++;
			
			else if(describeResponse.getState().equals("active")) {
				if(!describeResponse.getStatus().equals("fulfilled"))
					openedSpotInstancesRequests++;
			}
			else
				skippedRequests++;
		}
		
		LOG.debug("getNumOpenedSpotInstancesRequests - Skipped requests (for non active/open): " + skippedRequests);
		
		return openedSpotInstancesRequests;
	}
}
