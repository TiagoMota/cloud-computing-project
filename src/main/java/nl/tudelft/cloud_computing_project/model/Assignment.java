/**
 * 
 */
package nl.tudelft.cloud_computing_project.model;

/**
 * Java representation of the Assignment Table
 * The fields are public for easy access, but getters and setters are provided which will do data checking.
 */
public class Assignment {
	
	private String worker_instanceid;
	private int job_id;
	private int order;
	
	public Assignment() {}
	
	public Assignment(String worker_instanceid, int job_id, int order) {
		this.worker_instanceid = worker_instanceid;
		this.job_id = job_id;
		this.order = order;
	}

	/**
	 * @return the worker_instanceid
	 */
	public String getWorker() {
		return worker_instanceid;
	}

	/**
	 * @param worker_instanceid the worker_instanceid to set
	 */
	public void setWorker(String worker_instanceid) {
		this.worker_instanceid = worker_instanceid;
	}

	/**
	 * @return the job_id
	 */
	public int getJobId() {
		return job_id;
	}

	/**
	 * @param job_id the job_id to set
	 */
	public void setJobId(int job_id) {
		this.job_id = job_id;
	}

	/**
	 * @return the order
	 */
	public int getOrder() {
		return order;
	}

	/**
	 * @param order the order to set
	 */
	public void setOrder(int order) {
		this.order = order;
	}
	
	
}
