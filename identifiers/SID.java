package identifiers;

import java.io.Serializable;

public class SID  implements Serializable{

	private static final long serialVersionUID = 7250440382030198587L;
	private int sessNum;
	private IPP ipp;

	public SID(IPP ipp, int sessNum) {
	    this.ipp = ipp;
	    this.sessNum = sessNum;
	}

	public IPP getIpp() {
		return ipp;
	}

	public void setIpp(IPP ipp) {
		this.ipp = ipp;
	}

	public int getSessNum() {
		return sessNum;
	}

	public void setSessNum(int sessNum) {
		this.sessNum = sessNum;
	}
	
	 @Override
     public String toString() {
		 return Integer.toString(sessNum) + "_" + ipp.toString();
     }
}
