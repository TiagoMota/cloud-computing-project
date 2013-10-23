/**
 * 
 */
package nl.tudelft.cloud_computing_project.database;

/**
 * Java representation of the Assignment Table
 * The fields are public for easy access, but getters and setters are provided which will do data checking.
 */
public class Assignment {
	public int Wid;
	public int Jib;
	public boolean processing;
	public int order;
	
	/**
	 * @return the wid
	 */
	public int getWid() {
		return Wid;
	}
	/**
	 * @param wid the wid to set
	 */
	public void setWid(int wid) {
		Wid = wid;
	}
	/**
	 * @return the jib
	 */
	public int getJib() {
		return Jib;
	}

	/**
	 * @return the processing
	 */
	public boolean isProcessing() {
		return processing;
	}
	/**
	 * @param processing the processing to set
	 */
	public void setProcessing(boolean processing) {
		this.processing = processing;
	}
	
	/**
	 * @return the order
	 */
	public int getOrder() {
		return order;
	}
	
	
}
