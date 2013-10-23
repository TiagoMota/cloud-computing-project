package nl.tudelft.cloud_computing_project;

import java.util.List;

import nl.tudelft.cloud_computing_project.database.Database;
import nl.tudelft.cloud_computing_project.database.Job;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sql2o.Sql2o;

public class Scheduler {
	private static Logger LOG = LoggerFactory.getLogger(Scheduler.class);
	private List<Job> queue;
	private List<WorkerLoad> workers; 
	private Sql2o sql2o;
	
	String queue_sql =  "SELECT Jid, priority, submission_time, jobstatus " +
						"FROM Job " +
						"WHERE jobstatus = " + Job.JobStatus.SUBMITTED.code + " " +
						"AND Jid NOT IN (SELECT Jid FORM Assignment) " +
						"ORDER BY submission_time and priority";
	
	String workers_sql = "SELECT Worker.Wid, SUM(filesize) AS total_load FROM Worker " +
						 	"JOIN Assignment ON Assignment.Wid = Worker.Wid " +
						 	"JOIN Job ON Job.Jid = Assignment.Jid " +
						 		"WHERE jobstatus = 1 " +
						 "GROUP BY Worker.Wid";
	
	String order_sql = "SELECT Wid, MAX(order) as Order FROM Assignment GROUP BY Wid";
	
	String assign_sql = "INSERT INTO Assignment(Jid, Wid, processing, order)" +
						"VALUES (:_Jid, :_Wid, :_processing, :_order)";
	
	public Scheduler(){
		
	}
	
	private class WorkerLoad {
		public int Wid;
		public long total_load;
	}
	
	private class WorkerOrder {
		public int Wid;
		public int Order;
	}
	
	/**
	 * Assigns jobs from the database queue to the current workers
	 */
	
	public void pullJobQueue(){
		// query database for the jobs order by priority and submission time.
		queue = sql2o.createQuery(queue_sql).executeAndFetch(Job.class);		
	}
	
	public void assignJob(int Jid, int Wid, int order){
		//Updates Assignment table on DB
		sql2o.createQuery(assign_sql)
	    .addParameter("_Jid", Jid)
	    .addParameter("_Wid", Wid)
	    .addParameter("_processing", false)
	    .addParameter("_order", order)
	    .executeUpdate();
	}
	
	public int getWorkerOrder(int Wid){
		//find the biggest value for order in the existent assignments for a specific worker, and then assign the new job with a value bigger than that.
		int max_order=0;
		List<WorkerOrder> worker_orders = sql2o.createQuery(order_sql).executeAndFetch(WorkerOrder.class);
		for(int i=0; i<worker_orders.size(); i++){
			if(worker_orders.get(i).Wid == Wid && worker_orders.get(i).Order > max_order){
				max_order = worker_orders.get(i).Order;
			}
		}
		return max_order;
	}
	
	public void schedule() {
		sql2o = Database.getConnection();	
		
		LOG.info("Querying DB for jobs");
		pullJobQueue();
		LOG.info("Now scheduling");
		
		// Query the database for available alive workers, order by total job length.
			
		workers = sql2o.createQuery(workers_sql).executeAndFetch(WorkerLoad.class);
		// Schedule first job in queue to the available worker with the least total job length
		while(!queue.isEmpty()){
			int minIndex = 0;
			for(int i=0; i<workers.size(); i++){
				if(workers.get(i).total_load < workers.get(minIndex).total_load)
					minIndex = i;
			}
			//add new filesize to the workers load
			workers.get(minIndex).total_load += queue.get(0).getFilesize();
			//find worker order value $ finally schedule job
			assignJob(queue.get(0).getJid(), workers.get(minIndex).Wid, getWorkerOrder(workers.get(minIndex).Wid));
			queue.remove(0);
		}
		Database.releaseConnection();
		
		
	}
	
	public static void main(String[] args) {
		Scheduler s = new Scheduler();
		s.schedule();
	}
}
