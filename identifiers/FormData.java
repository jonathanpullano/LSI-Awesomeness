package identifiers;

import java.util.ArrayList;
import java.util.Date;

import rpc.RpcServer;
import server.SimpleDB;

public class FormData {
    private String message;
    private Date expiration;

    public enum Location { ippPrimary, ippBackup, cache, NotFound };
    private Location loc;
    private boolean newUpdated;
    private IPP ippPrimary;
    private IPP ippBackup;
    private long expTime;
    private long discardTime;
    private SID eviction;

    public FormData(String message, long expiration) {
        setMessage(message);
        setExpiration(expiration);
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Date getExpiration() {
        return expiration;
    }

    public IPP getServerID() {
        return RpcServer.getInstance().getIPPLocal();
    }

    public Location getLoc() {
    	if(loc == null)
    		return Location.NotFound;
        return loc;
    }
    
    public IPP getLocIPP(){
    	if(loc == null)
    		return IPP.getNullIpp();
    	
    	switch(loc){
    	case ippPrimary:
    		return getIppPrimary();
    	case ippBackup:
    		return getIppBackup();
    	case cache:
    		return RpcServer.getInstance().getIPPLocal();
    	}
    	
		return null;
    }

    public boolean isNewUpdated() {
        return newUpdated;
    }

    public IPP getIppPrimary() {
        return ippPrimary;
    }

    public IPP getIppBackup() {
        return ippBackup;
    }

    public long getExpTime() {
        return expTime;
    }

    public long getDiscardTime() {
        return discardTime;
    }

    public SID getEviction() {
        return eviction;
    }

    public ArrayList<IPP> getMemberSet() {
        return SimpleDB.getInstance().getLocalMembers();
    }

    public void setExpiration(long expiration) {
        this.expiration = new Date(expiration);
    }
}