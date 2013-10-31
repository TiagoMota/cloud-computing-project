package nl.tudelft.cloud_computing_project;

import java.util.ArrayList;
import java.util.List;

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
	private static Logger LOG = LoggerFactory.getLogger(FaultManager.class);
	private static SpotInstancesAllocator instance;
	private AmazonEC2 ec2 = AmazonEC2Initializer.getInstance();
	private ArrayList<String> instanceIds;
	private ArrayList<String> spotInstanceRequestIds;

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
		launchSpecification.setImageId("ami-f720c380");
		launchSpecification.setInstanceType("t1.micro");
		ArrayList<String> securityGroups = new ArrayList<String>();
		securityGroups.add("cloudocr-worker");
		launchSpecification.setSecurityGroups(securityGroups);
		requestRequest.setLaunchSpecification(launchSpecification);

		// Call the RequestSpotInstance API.
		RequestSpotInstancesResult requestResult = ec2.requestSpotInstances(requestRequest);
		List<SpotInstanceRequest> requestResponses = requestResult.getSpotInstanceRequests();

		spotInstanceRequestIds = new ArrayList<String>();
		instanceIds = new ArrayList<String>();

		for (SpotInstanceRequest requestResponse : requestResponses) {
			System.out.println("Created Spot Request: "+requestResponse.getSpotInstanceRequestId());
			spotInstanceRequestIds.add(requestResponse.getSpotInstanceRequestId());
		}

		monitorSpotInstancesRequests();

	}

	private void monitorSpotInstancesRequests() {

		boolean anyRequestOpen;

		do {

			anyRequestOpen = false;

			DescribeSpotInstanceRequestsRequest describeRequest = new DescribeSpotInstanceRequestsRequest();

			try {
				// Retrieve all of the requests we want to monitor.
				DescribeSpotInstanceRequestsResult describeResult = ec2.describeSpotInstanceRequests(describeRequest);
				List<SpotInstanceRequest> describeResponses = describeResult.getSpotInstanceRequests();

				//Exit if there are no active requests
				if(describeResponses == null) 
					return;

				// Look through each request and determine if they are all in the active state.
				for (SpotInstanceRequest describeResponse : describeResponses) {
					if (describeResponse.getState().equals("open")) {
						anyRequestOpen = true;
						break;
					}

					instanceIds.add(describeResponse.getInstanceId());
					tagInstances();
				}
			} catch (AmazonServiceException e) {
				// If we have an exception, ensure we don't break out
				// of the loop. This prevents the scenario where there
				// was blip on the wire.
				anyRequestOpen = true;
			}

			try {
				// Sleep for 60 seconds.
				Thread.sleep(60*1000);
			} catch (Exception e) {
				// Do nothing because it woke up early.
			}

		} while (anyRequestOpen);

	}

	private void tagInstances() {
		// Tag the created instances
		ArrayList<Tag> instanceTags = new ArrayList<Tag>();
		instanceTags.add(new Tag("cloudocr","spotinstance"));

		CreateTagsRequest createTagsRequest_instances = new CreateTagsRequest();
		createTagsRequest_instances.setResources(instanceIds);
		createTagsRequest_instances.setTags(instanceTags);

		try {
			ec2.createTags(createTagsRequest_instances);
		} catch (AmazonServiceException e) {
			LOG.warn("Error tagging instances");
			LOG.warn("Caught Exception: " + e.getMessage());
			LOG.warn("Reponse Status Code: " + e.getStatusCode());
			LOG.warn("Error Code: " + e.getErrorCode());
			LOG.warn("Request ID: " + e.getRequestId());
		}
	}

	public void cancelSpotInstancesRequests() {
		try {

			//Cancel requests.
			CancelSpotInstanceRequestsRequest cancelRequest = new CancelSpotInstanceRequestsRequest();
			CancelSpotInstanceRequestsResult cancelResult = ec2.cancelSpotInstanceRequests(cancelRequest);
			List<CancelledSpotInstanceRequest> cancelledRequests = cancelResult.getCancelledSpotInstanceRequests(); 

			LOG.info("Cancelled " + cancelledRequests.size() + "Spot Instance Requests.");

		} catch (AmazonServiceException e) {
			LOG.warn("Error canceling instances");
			LOG.warn("Caught Exception: " + e.getMessage());
			LOG.warn("Reponse Status Code: " + e.getStatusCode());
			LOG.warn("Error Code: " + e.getErrorCode());
			LOG.warn("Request ID: " + e.getRequestId());
		}
	}
}
