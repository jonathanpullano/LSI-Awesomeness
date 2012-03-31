package Objects;

import java.io.Serializable;
import java.net.InetAddress;

/**
 *
 * Description goes here ..
 */
public class SID  implements Serializable{

	private static final long serialVersionUID = 7250440382030198587L;
	private int sessNum;
	private int port;
	private InetAddress ip;

	public int getSessNum() {
		return sessNum;
	}

	public int getPORT() {
		return port;
	}

	public InetAddress getIp() {
		return ip;
	}

	public void setSess_num(int sessNum) {
		this.sessNum = sessNum;
	}

	public void setPORT(int port) {
		this.port = port;
	}

	public void setIp(InetAddress ip) {
		this.ip = ip;
	}

}
