package nl.tudelft.cloud_computing_project.database;

import java.util.Date;

/**
 * Java representation of the Job table
 * The fields are public for easy access, but getters and setters are provided which will do data checking.
 */
public class Job {
	/**
	 * Job ID
	 */
	public int Jid;
	/**
	 * Filename of the submission (URI)
	 */
	public String filename;
	/**
	 * Filesize in bytes
	 */
	public long filesize;
	/**
	 * Priority of the Job, higher = higher priority
	 * 0 - 99. Default is 50.
	 */
	public int priority;
	/**
	 * Number of times this job has failed
	 */
	public int num_failures;
	/**
	 * Date and time of submission
	 */
	public Date submission_time;
	/**
	 * Current job status, see JobStatus enum
	 */
	public int jobstatus;
	
	public enum JobStatus {
		SUBMITTED(1, "submitted"),
		COMPLETED(2, "completed"),
		FAILED(3, "failed");
		
		public final int code;
		public final String name;
		
		private JobStatus(int code, String name) {
			this.code = code;
			this.name = name;
		}
	}

	/**
	 * @return the jid
	 */
	public int getJid() {
		return Jid;
	}

	/**
	 * @return the filename
	 */
	public String getFilename() {
		return filename;
	}

	/**
	 * @param filename the filename to set
	 */
	public void setFilename(String filename) {
		this.filename = filename;
	}

	/**
	 * @return the filesize
	 */
	public long getFilesize() {
		return filesize;
	}

	/**
	 * @param filesize the filesize to set
	 */
	public void setFilesize(long filesize) {
		this.filesize = filesize;
	}

	/**
	 * @return the priority
	 */
	public int getPriority() {
		return priority;
	}

	/**
	 * @param priority the priority to set
	 */
	public void setPriority(int priority) {
		this.priority = priority;
	}

	/**
	 * @return the num_failures
	 */
	public int getNum_failures() {
		return num_failures;
	}

	/**
	 * @param num_failures the num_failures to set
	 */
	public void setNum_failures(int num_failures) {
		this.num_failures = num_failures;
	}

	/**
	 * @return the submission_time
	 */
	public Date getSubmission_time() {
		return submission_time;
	}

	/**
	 * @param submission_time the submission_time to set
	 */
	public void setSubmission_time(Date submission_time) {
		this.submission_time = submission_time;
	}

	/**
	 * @return the jobstatus
	 */
	public int getJobstatus() {
		return jobstatus;
	}

	/**
	 * @param jobstatus the jobstatus to set
	 */
	public void setJobstatus(int jobstatus) {
		this.jobstatus = jobstatus;
	}
	
	
}
