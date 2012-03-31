package identifiers;

import java.io.Serializable;

public class SVN implements Serializable{

	private static final long serialVersionUID = -6264611582958483765L;
	private int changeCount;
	private IPP ippPrime;
	private IPP ippBackup;

	public SVN(int changeCount, IPP ippPrime, IPP ippBackup) {
	    this.changeCount = changeCount;
	    this.ippPrime = ippPrime;
	    this.ippBackup = ippBackup;
	}

	public int getChangeCount() {
		return changeCount;
	}

	public void setChangeCount(int changeCount) {
		this.changeCount = changeCount;
	}

	public IPP getIppPrime() {
		return ippPrime;
	}

	public IPP getIppBackup() {
		return ippBackup;
	}

	public void setIppPrime(IPP ippPrime) {
		this.ippPrime = ippPrime;
	}

	public void setIppBackup(IPP ippBackup) {
		this.ippBackup = ippBackup;
	}
	
	 @Override
     public String toString() {
         return Integer.toString(changeCount) + "_" + ippPrime.toString() + "_" + ippBackup.toString();
     }
}
