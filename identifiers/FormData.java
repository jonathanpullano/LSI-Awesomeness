package identifiers;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;

import rpc.RpcServer;
import server.SimpleDB;

public class FormData {
    private String message;
    private Date expiration;

    public enum Location { ippPrimary, ippBackup, cache };
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
        return loc;
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
        return SimpleDB.getInstance().getMembers();
    }

    public void setExpiration(long expiration) {
        this.expiration = new Date(expiration);
    }
}