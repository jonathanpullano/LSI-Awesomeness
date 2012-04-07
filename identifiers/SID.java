package identifiers;

import java.io.Serializable;

/**
 * SessionID, as defined in spec
 * @author jonathanpullano
 *
 */
public class SID  implements Serializable{

	private static final long serialVersionUID = 7250440382030198587L;
	private int sessNum;
	private IPP ipp;

	public SID(int sessNum, IPP ipp) {
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

	public static SID getSID(String sidString) {
	     String[] split = sidString.split("_");
	     return new SID(Integer.parseInt(split[0]), IPP.getIPP(split[1]));
	}

     @Override
     public boolean equals(Object other) {
         if (this == other) return true;
         if (!(other instanceof SID)) return false;
         SID otherSID = (SID)other;
         return otherSID.getSessNum() == getSessNum() && otherSID.getIpp().equals(getIpp());
     }

     @Override
     public int hashCode() {
         return getIpp().toString().hashCode() + getSessNum();
     }
}
