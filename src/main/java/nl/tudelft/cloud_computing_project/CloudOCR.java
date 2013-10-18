package nl.tudelft.cloud_computing_project;


public class CloudOCR {
	
	public static void main(String[] args) {
		System.out.println("Welcome to Cloud OCR!");
		Thread SchedulerThread = new Thread() {
			public void run(){
				new Scheduler();
				while(true){
					try {
						Thread.sleep(500);
						System.out.println("Ping from Scheduler");
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		};
		
		Thread MonitorThread = new Thread() {
			public void run(){
				new Monitor();
				while(true){
					try {
						Thread.sleep(500);
						System.out.println("Ping from Monitor");
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		};
		
		Thread AllocationManagerThread = new Thread() {
			public void run(){
				new AllocationManager();
				while(true){
					try {
						Thread.sleep(500);
						System.out.println("Ping from Allocation Manager");
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		};
		
		SchedulerThread.start();
		MonitorThread.start();
		AllocationManagerThread.start();
	}

}
