package nl.tudelft.cloud_computing_project.experimental.allocation_benchmarks;

import java.util.Random;

import nl.tudelft.cloud_computing_project.experimental.SetupTestDataSet;

public class DynamicJobSubmissionExperiment {

	public static void dynamicJobSubmissionExperiment(int JobsToSubmitNumber) {
		
		int submittedJobsNumber = 0;
		int sleepMinutes;
		Random randomGenerator = new Random();
		
		while (submittedJobsNumber != JobsToSubmitNumber) {
			
			SetupTestDataSet.addjobs(10);
			submittedJobsNumber += 10;
			
			try {
				sleepMinutes = randomGenerator.nextInt(10);
				Thread.sleep((sleepMinutes + 5) * 60000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
		}
		
	}
	
}
