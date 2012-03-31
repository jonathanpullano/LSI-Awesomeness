package Objects;

import java.io.Serializable;
import java.net.InetAddress;

public class SID  implements Serializable{
	
	/**
	 * descriptoin goes here...
	 */
	private static final long serialVersionUID = 7250440382030198587L;
	private int sess_num;
	private int PORT;
	private InetAddress ip;
	
	public int getSess_num() {
		return sess_num;
	}
	public int getPORT() {
		return PORT;
	}
	public InetAddress getIp() {
		return ip;
	}
	public void setSess_num(int sess_num) {
		this.sess_num = sess_num;
	}
	public void setPORT(int pORT) {
		PORT = pORT;
	}
	public void setIp(InetAddress ip) {
		this.ip = ip;
	}
	
}
