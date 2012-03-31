package identifiers;

import java.io.Serializable;
import java.net.InetAddress;

public class IPP implements Serializable{
	private static final long serialVersionUID = 5041613869405327697L;
	private InetAddress ip;
	private int port;

	public IPP(InetAddress ip, int port) {
	    this.ip = ip;
	    this.port = port;
	}

	public InetAddress getIp() {
		return ip;
	}

	public void setIp(InetAddress ip) {
		this.ip = ip;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}
	
	 @Override
     public String toString() {
         return ip.getHostAddress() + "-" + Integer.toString(port);
     }
}
