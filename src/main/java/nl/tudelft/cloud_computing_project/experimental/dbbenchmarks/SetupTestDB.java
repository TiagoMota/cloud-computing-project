package nl.tudelft.cloud_computing_project.experimental.dbbenchmarks;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

import javax.management.RuntimeErrorException;


public class SetupTestDB {

	private final String setupfile = System.getProperty("user.dir") + "/src/main/java/nl/tudelft/cloud_computing_project/experimental/dbbenchmarks/cloudocr_db.sql";
	
	private static final String LINESEP = System.getProperty("line.separator");
	
	private final String insertTemplate = "INSERT INTO "
			+ "Job (filename, filesize, priority, num_failures, submission_time, jobstatus) "
			+ "VALUES ('http://example.org/some/file.jpg', 1000, 50, 0, '2013-01-01 01:02:03', 1);"
			+ LINESEP;
	
	public void run(int datasetsize, String outputfile) throws IOException {
		File fout = new File(outputfile);
		if(fout.exists()) {
			fout.delete();
		} else {
			fout.createNewFile();
		}
		
		File fsetup = new File(setupfile);
		if(!fsetup.exists()) {
			throw new RuntimeException(String.format("Setupfile %s not found", setupfile));
		}
		
		BufferedWriter w = new BufferedWriter(new FileWriter(fout));
		
		// Start with the setup block
		BufferedReader setupr = new BufferedReader(new FileReader(fsetup));
		String line;
		while((line = setupr.readLine()) != null) {
			w.write(line);
			w.write(LINESEP);
		}
		w.write(LINESEP + LINESEP + LINESEP);
		setupr.close();
		
		w.write("START TRANSACTION;" + LINESEP);
		w.write("USE `cloudocr_db`;" + LINESEP);
		for(int i=0;i< datasetsize; i++) {
			w.write(insertTemplate);
		}
		w.write("COMMIT;");
		w.close();
	}
	
	public static void main(String[] args) throws IOException {
		System.out.println("Number of jobs to generate:");
		Scanner s = new Scanner(System.in);
		int datasetsize = s.nextInt();
		s.close();
		
		(new SetupTestDB()).run(datasetsize, System.getProperty("user.dir") + "/src/main/java/nl/tudelft/cloud_computing_project/experimental/dbbenchmarks/" + datasetsize + ".sql");
		
		System.out.println("Done!");
	}

}
