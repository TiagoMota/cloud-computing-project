package nl.tudelft.cloud_computing_project;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AllocationManager {
	
	private static final String PROVVISIONING_POLICY_CLASS = (String)CloudOCR.Configuration.get("PROVVISIONING_POLICY_CLASS");
	private static Logger LOG = LoggerFactory.getLogger(FaultManager.class);
	private static AllocationManager instance;
	private ProvvisioningPolicy provvisioningPolicy;

	private AllocationManager(){
		try {
			 provvisioningPolicy = (ProvvisioningPolicy) Class.forName(PROVVISIONING_POLICY_CLASS).newInstance();
		} catch (Exception e) {
			LOG.error("Error instantiating ProvvisioningPolicy class:\n" + e.getMessage());
		}
	}
	
	public static AllocationManager getInstance(){
		if(instance == null) { instance = new AllocationManager(); }
		return instance;
	}
	
	
	public void applyProvvisioningPolicy() {
		
		provvisioningPolicy.applyProvvisioningPolicy();
		
	}
}
