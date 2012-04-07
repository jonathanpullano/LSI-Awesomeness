package identifiers;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;

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
    private Date expiration;

    public enum Location { ippPrimary, ippBackup, cache };
    private Location loc;
    private boolean newUpdated;
    private IPP ippPrimary;
    private IPP ippBackup;
    private long discardTime;
    private SID eviction;
    
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Location getLoc() {
        return loc;
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

    public void setDiscardTime(long discardTime) {
        this.discardTime = discardTime;
    }

    public SID getEviction() {
        return eviction;
    }

    public void setEviction(SID eviction) {
        this.eviction = eviction;
    }

    public Date getExpiration() {
        return expiration;
    }

    public IPP getServerID() {
        return RpcServer.getInstance().getIPPLocal();
    }

    public ArrayList<IPP> getMemberSet() {
        return SimpleDB.getInstance().getMembers();
    }

    public void setExpiration(long expiration) {
        this.expiration = new Date(expiration);
    }
}