package nl.tudelft.cloud_computing_project;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;

import nl.tudelft.cloud_computing_project.model.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sql2o.Connection;
import org.sql2o.Query;
import org.sql2o.Sql2o;
import org.sql2o.data.Row;
import org.sql2o.data.Table;

public class Scheduler {
	private static final Logger LOG = LoggerFactory.getLogger(Scheduler.class);
	
	private final String jobqueue_sql = 
		"SELECT Jid, priority, submission_time, jobstatus" +
		" FROM Job " +
		" WHERE jobstatus = " + Job.JobStatus.SUBMITTED.code +
		" AND Jid NOT IN (SELECT Jid FROM Assignment)" +
		" ORDER BY priority, submission_time";
	
	
	private final String workers_sql =
		"SELECT Worker.Wid AS Wid, COALESCE(MAX(Assignment.order), 0) AS maxorder, SUM(COALESCE(Job.filesize, 0)) AS worker_load" +
		" FROM Worker" +
		" LEFT JOIN Assignment ON Assignment.Wid = Worker.Wid" +
		" LEFT JOIN Job ON Job.Jid = Assignment.Jid " +
		" WHERE workerstatus = " + Worker.WorkerStatus.ALIVE.code +
		" GROUP BY Worker.Wid";
	
	private final String assign_sql = "INSERT INTO Assignment(Jid, Wid, order)" +
						"VALUES (:_Jid, :_Wid, :_order)";
	
	private Sql2o sql2o;
	// Jobs in the order they should be assigned to workers
	private Iterable<Job> jobs;
	
	// Worker information
	private Map<Integer, Long> worker_load;
	private Map<Integer, Integer> worker_maxorder;
	
	// TODO: Make this configurable
	/**
	 * Maximum load of a worker (in byte filesize of assigned jobs)
	 */
	private long getMaxWorkerLoad() {
		return 50 * 1024 * 1024;	// 50 MB
	}
	
	/**
	 * Get the current jobs to be scheduled
	 */
	private void pullJobQueue(){
		// TODO: Add some limit or change method so it won't hog up all memory if the number of jobs is large. E.g. iterate ResultSet manually
		// query database for the jobs order by priority and submission time.
		jobs = sql2o.createQuery(jobqueue_sql, "jobqueue_sql").executeAndFetch(Job.class);		
	}
	
	/**
	 * Gets the information for the available workers
	 */
	private void pullWorkerInfo() {
		Table t = sql2o.createQuery(workers_sql, "workers_sql").executeAndFetchTable();
		
		worker_load = new HashMap<Integer, Long>((int)(t.rows().size()*1.5));
		worker_maxorder = new HashMap<Integer, Integer>((int)(t.rows().size()*1.5));
		
		for(Row r : t.rows()) {
			worker_load.put(r.getInteger("Wid"), r.getLong("worker_load"));
			worker_maxorder.put(r.getInteger("Wid"), r.getInteger("maxorder"));
		}
	}
	
	/**
	 * Put assignments into the database
	 */
	private void assignJobs(Iterable<Assignment> assignments) {
		Connection c = sql2o.beginTransaction().setRollbackOnException(true);
		try {
			Query q = c.createQuery(assign_sql, "assign_sql");
			for(Assignment a : assignments) {
				q
			    .addParameter("_Jid", a.getJib())
			    .addParameter("_Wid", a.getWid())
			    .addParameter("_order", a.getOrder())
			    .addToBatch();
			}
			q.executeBatch();
		} catch(RuntimeException e) {
			c.rollback();
			LOG.error("Exception occured while putting assignments in database. Rolled back.", e);
			throw e;
		}
		c.commit();		
	}
	
	/**
	 * Schedule with a greedy load-balancing algorithm
	 * For example see http://www.cs.princeton.edu/courses/archive/spr05/cos423/lectures/11approx-alg.pdf
	 * @return The assignments
	 */
	protected Collection<Assignment> schedule_greedy_lb() {
		LOG.debug("Using greedy load-balancing algorithm");
		
		// Make a sorted datastructure that sorts the workers on their load
		PriorityQueue<Integer> workers = new PriorityQueue<Integer>(worker_load.size()+1, new Comparator<Integer>() {
			public int compare(Integer o1, Integer o2) {
				return Long.compare(worker_load.get(o1), worker_load.get(o2));
			}
		});
		// Keep all the assignments in memory so we can commit them into the database in one go
		Collection<Assignment> assignments = new ArrayList<Assignment>();
		
		// Go through all the jobs that can be assigned
		for(Job j : jobs) {
			Integer wid = null;
			// Get the preferred worker to assign to
			while(!workers.isEmpty() && wid == null) {
				// Get preferred worker
				wid = workers.poll();
				// Check if the current load is acceptable, otherwise keep removed from queue
				if(worker_load.get(wid) > getMaxWorkerLoad()) {
					wid = null;
				}
			}
			// Stop if no more workers are available
			if(wid == null) {
				break;
			}
			
			// Assign job to worker
			// TODO: Fix that this doesn't work if order is Integer.MAX_VALUE
			int nextorder = worker_maxorder.get(wid)+1;
			assignments.add(new Assignment(wid, j.getId(), nextorder));
			worker_maxorder.put(wid, nextorder);
		}
		return assignments;
	}
	
	/**
	 * Assign jobs to workers
	 */
	public void schedule() {
		LOG.info("Starting scheduler");
		
		sql2o = Database.getConnection();	
		pullJobQueue();
		pullWorkerInfo();
		
		Collection<Assignment> assignments = schedule_greedy_lb();
		
		assignJobs(assignments);
		
		LOG.info(String.format("Scheduled %d jobs", assignments.size()));
	}
	
	public static void main(String[] args) {
		Scheduler s = new Scheduler();
		s.schedule();
	}
}
