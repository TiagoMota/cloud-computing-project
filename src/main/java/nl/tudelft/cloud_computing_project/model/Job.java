package nl.tudelft.cloud_computing_project.model;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Java representation of the Job table
 * The fields are public for easy access, but getters and setters are provided which will do data checking.
 */
public class Job {
	private static Logger LOG = LoggerFactory.getLogger(Job.class);
	
	private int id; 				/* Job ID */
	private String filename; 		/* Filename of the submission (URI) */
	private long filesize; 			/* Filesize in bytes */
	private int priority; 			/* Priority of the Job, higher = higher priority. 0 - 99. Default is 50. */
	private int num_failures; 		/* Number of times this job has failed */
	private Date submission_time; 	/* Date and time of submission */
	private int jobstatus; 			/* Current job status, see JobStatus enum */
	
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
		
		public static JobStatus getByCode(int code) {
			switch(code) {
			case 1:
				return SUBMITTED;
			case 2:
				return COMPLETED;
			case 3:
				return FAILED;
			default:
				throw new IllegalArgumentException("Invalid JobStatus Code");
			}
		}
	}

	/**
	 * @return The Job ID
	 */
	public int getId() {
		return id;
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
		if(filesize < 0) {
			LOG.warn("Filesize cannot be negative");
			filesize = 0;
		}
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
		if(priority < 0) {
			LOG.warn("Priority cannot be lower than 0");
			priority = 0;
		}
		if(priority > 99) {
			LOG.warn("Priority cannot be higher than 99");
		}
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
	public JobStatus getJobstatus() {
		return JobStatus.getByCode(jobstatus);
	}

	/**
	 * @param jobstatus the jobstatus to set
	 */
	public void setJobstatus(int jobstatus) {
		this.jobstatus = jobstatus;
	}
	
	
}
