package Objects;

import java.io.Serializable;
import java.net.InetAddress;

public class SVN implements Serializable{
	
	private static final long serialVersionUID = -6264611582958483765L;
	private int changeCount;
	private InetAddress ipPrime;
	private InetAddress ipBackup;
	
	public int getChangeCount() {
		return changeCount;
	}
	public InetAddress getIpPrime() {
		return ipPrime;
	}
	public InetAddress getIpBackup() {
		return ipBackup;
	}
	public void setChangeCount(int changeCount) {
		this.changeCount = changeCount;
	}
	public void setIpPrime(InetAddress ipPrime) {
		this.ipPrime = ipPrime;
	}
	public void setIpBackup(InetAddress ipBackup) {
		this.ipBackup = ipBackup;
	}

}
