package Objects;

import java.io.Serializable;

public class SID  implements Serializable{

	private static final long serialVersionUID = 7250440382030198587L;
	private int sessNum;
	private int port;
	
	//TODO dont forget to instantiate an IPP object when you declare this class...
	private IPP ipp;

	public IPP getIpp() {
		return ipp;
	}

	public void setIpp(IPP ipp) {
		this.ipp = ipp;
	}

	public int getSessNum() {
		return sessNum;
	}

	public int getPORT() {
		return port;
	}

	public void setSess_num(int sessNum) {
		this.sessNum = sessNum;
	}

	public void setPORT(int port) {
		this.port = port;
	}

}
