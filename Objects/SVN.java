package Objects;

import java.io.Serializable;

public class SVN implements Serializable{
	
	private static final long serialVersionUID = -6264611582958483765L;
	private int changeCount;
	private IPP ippPrime;
	private IPP ippBackup;
	
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

}
