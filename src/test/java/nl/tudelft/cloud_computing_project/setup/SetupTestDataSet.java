package nl.tudelft.cloud_computing_project.setup;

import java.text.ParseException;
import java.util.Date;
import java.util.Random;
import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sql2o.Connection;
import org.sql2o.Query;
import org.sql2o.StatementRunnable;

import nl.tudelft.cloud_computing_project.model.Database;
import nl.tudelft.cloud_computing_project.model.Job;

public class SetupTestDataSet {
	private static final Logger LOG = LoggerFactory.getLogger(SetupTestDataSet.class);
	
	private static final Date submissionTime = new Date();
	
	private static Job[] testjobs = new Job[] {
			Job.factory()
			.setFilename("https://s3-eu-west-1.amazonaws.com/cloudocr-test/input/file1m.jpg")
			.setFilesize(1880893)
			.setPriority(50)
			.setSubmissionTime(submissionTime)
			.create(),
			Job.factory()
			.setFilename("https://s3-eu-west-1.amazonaws.com/cloudocr-test/input/file1s.jpg")
			.setFilesize(103084)
			.setPriority(50)
			.setSubmissionTime(submissionTime)
			.create(),
			Job.factory()
			.setFilename("https://s3-eu-west-1.amazonaws.com/cloudocr-test/input/file2m.jpg")
			.setFilesize(1654036)
			.setPriority(50)
			.setSubmissionTime(submissionTime)
			.create(),
			Job.factory()
			.setFilename("https://s3-eu-west-1.amazonaws.com/cloudocr-test/input/file2s.jpg")
			.setFilesize(79963)
			.setPriority(50)
			.setSubmissionTime(submissionTime)
			.create(),
			Job.factory()
			.setFilename("https://s3-eu-west-1.amazonaws.com/cloudocr-test/input/file3m.jpg")
			.setFilesize(1959810)
			.setPriority(50)
			.setSubmissionTime(submissionTime)
			.create(),
			Job.factory()
			.setFilename("https://s3-eu-west-1.amazonaws.com/cloudocr-test/input/file2s.jpg")
			.setFilesize(100188)
			.setPriority(50)
			.setSubmissionTime(submissionTime)
			.create()
	};
	
	private static final String insertjobquery =
			"INSERT INTO Job (filename, filesize, priority, submission_time) VALUES "
			+ "(:filename, :filesize, :priority, :submission_time)";
	
	public static void main(String[] args) {
		int datasetsize = 0;
		try{
			if(args.length > 0) {
				datasetsize = Integer.parseInt(args[0]);
			} else {
				System.out.println("Input test dataset size (will add to existing jobs):");
				Scanner s = new Scanner(System.in);
				datasetsize = s.nextInt();
				s.close();
			}
		} catch (NumberFormatException e) {
			System.out.println("Invalid argument");
			System.exit(1);
		}
		
		LOG.info(String.format("Adding %d test jobs to the database", datasetsize));
		
		Connection c = Database.getConnection().beginTransaction();
		Database.getConnection().runInTransaction(new StatementRunnable() {
			
			@Override
			public void run(Connection connection, Object datasetsize) throws Throwable {
				Query q = connection.createQuery(insertjobquery);
				
				Random r = new Random();
				for(int i = 0; i < (Integer)datasetsize; i++) {
					int choise = r.nextInt(testjobs.length);
					q.addParameter("filename", testjobs[choise].getFilename())
					.addParameter("filesize", testjobs[choise].getFilesize())
					.addParameter("priority", testjobs[choise].getPriority())
					.addParameter("submission_time", new java.sql.Timestamp(testjobs[choise].getSubmission_time().getTime()))
					.addToBatch();
				}
				
				
				q.executeBatch();
			}
		}, datasetsize);
	}

}
