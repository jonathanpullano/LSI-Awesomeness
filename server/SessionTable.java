package server;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.ServletContext;

/**
 * Manages the Session Table
 * @author jonathan
 *
 */
public class SessionTable {
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

    /**
     * Commits changes to the session table to the context
     * @param context
     */
    public synchronized void commit(ServletContext context) {
        context.setAttribute("sessionTable", this);
    }

    /**
     * Gets the session table, or creates one if it doesn't exist yet
     * @param context
     * @return
     */
    public static synchronized SessionTable getSessionTable(ServletContext context) {
        SessionTable table = (SessionTable)context.getAttribute("sessionTable");
        if(table == null) {
            table = new SessionTable();
            context.setAttribute("sessionTable", table);
        }
        return table;
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

    @Override
    public String toString() {
        return sessionTable.toString();
    }
}
