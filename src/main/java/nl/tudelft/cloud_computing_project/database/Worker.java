package nl.tudelft.cloud_computing_project.database;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

import nl.tudelft.cloud_computing_project.CloudOCR;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Java representation of the Worker table
 * The fields are public for easy access, but getters and setters are provided which will do data checking.
 */
public class Worker {
	private static Logger LOG = LoggerFactory.getLogger(Worker.class);
	
	/**
	 * Worker ID
	 */
	public int Wid;
	
	/**
	 * IP adress in numeric form. Don't edit this directly but use the getters and setters.
	 */
	public int ip;
	
	/**
	 * Worker Status, see WorkerStatus enum.
	 */
	public int workerstatus;
	
	public enum WorkerStatus {
		ALIVE(1, "alive"),
		DEAD(2, "dead");
		
		public final int code;
		public final String name;
		
		private WorkerStatus(int code, String name) {
			this.code = code;
			this.name = name;
		}
		
		public static Worker.WorkerStatus getByCode(int code) {
			switch(code) {
			case 1:
				return ALIVE;
			case 2:
				return DEAD;
			default:
				throw new IllegalArgumentException("Invalid WorkerStatus Code");
			}
		}
	}

	/**
	 * @return the wid
	 */
	public int getWid() {
		return Wid;
	}

	/**
	 * Returns the IP in a usable format
	 * Returns null if IP adress couldn't be parsed, but this shouldn't happen.
	 * @return the ip
	 */
	public Inet4Address getIp() {
		try {
			return (Inet4Address)InetAddress.getByAddress(
						new byte[] {
								(byte)((ip >> 24) & 0xFF),
								(byte)((ip >> 16) & 0xFF),
								(byte)((ip >>  8) & 0xFF),
								(byte)((ip      ) & 0xFF)
						}
					);
		} catch (UnknownHostException e) {
			LOG.error("Unexpected UnknownHostException in Worker", e);
			return null;
		}
	}

	/**
	 * @param ip the ip to set
	 */
	public void setIp(Inet4Address ip) {
		byte[] br = ip.getAddress();
		this.ip = ((br[0] & 0xFF) << 24) |
                  ((br[1] & 0xFF) << 16) |
                  ((br[2] & 0xFF) << 8)  |
                  ((br[3] & 0xFF)     );
	}

	/**
	 * @return the workerstatus
	 */
	public WorkerStatus getWorkerstatus() {
		return WorkerStatus.getByCode(this.workerstatus);
	}

	/**
	 * @param workerstatus the workerstatus to set
	 */
	public void setWorkerstatus(WorkerStatus ws) {
		this.workerstatus = ws.code;
	}
	
	
}
