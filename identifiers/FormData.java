package identifiers;

import java.util.ArrayList;
import java.util.Date;

import rpc.RpcServer;
import server.SimpleDB;

/**
 * A Javabean-ish data structure passed to 
 * the jsp page containing view data to render
 * as per MVC pattern
 * @author jonathanpullano
 */
public class FormData {
    private String message;
    private long discardTime;
    private Date expiration;

    public enum Location { ippPrimary, ippBackup, cache, NotFound };
    private Location loc;
    private boolean newUpdated;
    private IPP ippPrimary;
    private IPP ippBackup;
    
    private SID eviction;
    
    public FormData() {}
    
    public FormData(String message, long expiration) {
        this.message = message;
        this.discardTime = expiration;
        this.expiration = new Date(expiration);
    }
    
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
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

    public void setLoc(Location loc) {
        this.loc = loc;
    }

    public boolean isNewUpdated() {
        return newUpdated;
    }

    public void setNewUpdated(boolean newUpdated) {
        this.newUpdated = newUpdated;
    }

    public IPP getIppPrimary() {
        return ippPrimary;
    }

    public void setIppPrimary(IPP ippPrimary) {
        this.ippPrimary = ippPrimary;
    }

    public IPP getIppBackup() {
        return ippBackup;
    }

    public void setIppBackup(IPP ippBackup) {
        this.ippBackup = ippBackup;
    }

    public long getDiscardTime() {
        return discardTime;
    }

    public String getEviction() {
        if(eviction == null)
            return "";
        else
            return "<p> Eviction: " + eviction + "</p>";
    }

    public void setEviction(SID eviction) {
        this.eviction = eviction;
    }

    public Date getExpiration() {
        return expiration;
    }
    
    public String getHTML() {
        if(newUpdated) {
            return "<p><b>Updated/New Session</b></p>" +
                   "<p>IppPrimary: " + getIppPrimary() + "</p>" +
                   "<p>IppBackup: " + getIppBackup() + "</p>" +
                   "<p>Discard Time: " + getDiscardTime() + "</p>";
        } else {
            return "<p><b>Existing Session</b><br>" +
                    getLoc() + ": " + getLocIPP() + "</p>";
        }
    }

    public IPP getServerID() {
        return RpcServer.getInstance().getIPPLocal();
    }

    public ArrayList<IPP> getMemberSet() {
        return SimpleDB.getInstance().getLocalMembers();
    }

    public void setExpiration(long expiration) {
        this.discardTime = expiration;
        this.expiration = new Date(expiration);
    }
}