package server;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Manages the Session Table
 * @author jonathan
 *
 */
public class SessionTable {
    public static SessionTable table = null;

    public HashMap<Integer, Entry> sessionTable = new HashMap<Integer, Entry>();

    /**
     * Represents a session
     * @author jonathan
     */
    public static class Entry {
      public int version;
      public String message;
      public long expiration;

      /**
       * Constructor, for convenience
       * @param version
       * @param message
       * @param expiration
       */
      public Entry(int version, String message, long expiration) {
          this.version = version;
          this.message = message;
          this.expiration = expiration;
      }

      @Override
      public String toString() {
          return this.version + ":" + this.message + ":" + this.expiration;
      }
    };

    private SessionTable() {};

    /**
     * Looks up a session
     * @param sessionID
     * @return
     */
    public Entry get(int sessionID) {
        return sessionTable.get(sessionID);
    }

    /**
     * Adds a new session to the table
     * @param sessionID
     * @param entry
     */
    public void put(int sessionID, Entry entry) {
        sessionTable.put(sessionID, entry);
    }

    public void destroySession(int sessionID, int version) {
        Entry session = sessionTable.get(sessionID);
        if(session.version <= version)
            sessionTable.remove(session);
    }

    /**
     * Destroys the session with the given ID
     * @param sessionID
     */
    public void destroySession(int sessionID) {
        sessionTable.remove(sessionID);
    }

    /**
     * Removes all sessions that have expired from this table
     */
    public void cleanExpiredSessions() {
        Date now = Calendar.getInstance().getTime();
        Iterator<Map.Entry<Integer, Entry>> iter = sessionTable.entrySet().iterator();
        while(iter.hasNext()) {
            Map.Entry<Integer, Entry> next = iter.next();
            if(new Date(next.getValue().expiration).before(now)) {
                iter.remove();
            }
        }
    }

    public static SessionTable getInstance() {
        if(table == null)
            table = new SessionTable();
        return table;
    }

    @Override
    public String toString() {
        return sessionTable.toString();
    }
}
