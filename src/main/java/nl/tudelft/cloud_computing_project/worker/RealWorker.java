package nl.tudelft.cloud_computing_project.worker;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import nl.tudelft.cloud_computing_project.database.Database;
import nl.tudelft.cloud_computing_project.database.Job;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sql2o.Sql2o;

public class RealWorker {
	private static final Logger LOG = LoggerFactory.getLogger(RealWorker.class);
	//Worker EC2 instance ID
	private String my_id = "";

	//sql query to fetch assigned jobs to this worker
	private final String jobs_sql = 
			"SELECT Job.filename" +
					" FROM Job " +
					" WHERE Job.id = ( " +
					"SELECT Assignment.job_id" +
					" FROM Assignment" +
					" WHERE Assignment.worker_instanceid = '" + my_id + "'" +
					" ORDER BY Assignment.order )";


	private Sql2o sql2o;

	private Iterable<Job> jobs;

	private String getInstanceID() throws Exception{
		String url = "http://169.254.169.254/latest/meta-data/instance-id";

		URL obj = new URL(url);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();
		con.setRequestMethod("GET");

		BufferedReader in = new BufferedReader(
				new InputStreamReader(con.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();

		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();

		return response.toString();
	}



	private void fetchAssignedJobs(){
		jobs = sql2o.createQuery(jobs_sql).executeAndFetch(Job.class);
	}

	private void executeJobs(){
		for(Job j: jobs){
			String wgetCommand = "wget " + j.getFilename();                
			// Execute UNIX command  
			Process pWGet;
			try {
				pWGet = Runtime.getRuntime().exec(wgetCommand);
				int iTerminationStatus = pWGet.waitFor();  
				if (pWGet.exitValue() != 0)   
				{  
					System.out.println("Grep Command failed. Exit value=" +   
							pWGet.exitValue());  
				}  
			} catch (Exception e) {
				LOG.error("Exception occured while downloading images", e);
				e.printStackTrace();
			} 
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		RealWorker worker = new RealWorker();
		worker.sql2o = Database.getConnection();
		try {
			//find own id
			worker.my_id = worker.getInstanceID();
			// check own assignments in DB and fetch jobs to run
			worker.fetchAssignedJobs();

			worker.executeJobs();
		} catch (Exception e) {
			e.printStackTrace();
		}




		//update DB when job finishes


	}

}
