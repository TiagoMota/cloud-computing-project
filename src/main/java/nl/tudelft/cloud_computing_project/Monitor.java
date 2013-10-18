package nl.tudelft.cloud_computing_project;

public class Monitor{
	
	private static Monitor instance;
	
	public Monitor(){
		System.out.println("Monitor thread started!");
	}
	
	public static Monitor getInstance(){
		
		if(instance == null) {
			instance = new Monitor();
			instance.monitorSystem();
		}
		
		return instance;
	
	}
	
	public void monitorSystem(){
		
		
		
	}
	
}
