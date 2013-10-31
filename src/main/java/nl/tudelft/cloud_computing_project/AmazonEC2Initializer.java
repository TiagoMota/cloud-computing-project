package nl.tudelft.cloud_computing_project;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.ClasspathPropertiesFileCredentialsProvider;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2Client;

public class AmazonEC2Initializer {
	
	private static AmazonEC2 ec2;
	private static Logger LOG = LoggerFactory.getLogger(Monitor.class);

	
	public static AmazonEC2 getInstance() {
		if (ec2 == null) initEC2Instance();
		return ec2;
	}
	
	private AmazonEC2Initializer(){}
	
	/**
	 * This method initializes the ec2 instance with the correct parameters.
	 */
	private static void initEC2Instance() {
		try {
			//Init ec2 instance
			AWSCredentialsProvider credentialsProvider = new ClasspathPropertiesFileCredentialsProvider();
			ec2 = new AmazonEC2Client(credentialsProvider);
			ec2.setEndpoint("ec2.eu-west-1.amazonaws.com");
		}
		catch (Exception e) {
			LOG.error(e.getMessage());
		}
	}
}
