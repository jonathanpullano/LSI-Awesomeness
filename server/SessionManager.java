package server;

import java.util.Calendar;

import javax.servlet.ServletContext;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class SessionManager {
    public static String DEFAULT_MESSAGE = "Hello, user!";
    public static String COOKIE_NAME = "CS5300PROJECT1SESSION";
    public static int TIMEOUT = 1000 * 60 * 30;
    public static int COOKIE_TIMEOUT = TIMEOUT/1000;

    /**
     * Obtains the cookie for the current user. If none exists, creates a new one.
     * @param context
     * @param request
     * @param response
     * @return
     */
    public static Cookie getCookie(ServletContext context, HttpServletRequest request, HttpServletResponse response) {
        Cookie ourCookie = null;
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            //Look through all the cookies to find my cookie
            for(Cookie cookie : cookies) {
                if(cookie.getName().equals(COOKIE_NAME)) {
                    ourCookie = cookie;
                }
            }
            //We found a cookie, verify it's valid
            if(ourCookie != null) {
                SessionTable table = SessionTable.getSessionTable(context);
                SessionTable.Entry entry = table.get(new Integer(ourCookie.getValue().split(":")[0]));
                if(entry == null) {
                    //It's mangled. Throw it out.
                    ourCookie = null;
                }
            }
        }
        //No cookie found, so make a new one
        if(ourCookie == null) {
            SessionTable table = SessionTable.getSessionTable(context);
            int sessionID = getNewSessionID(context);
            SessionTable.Entry entry = new SessionTable.Entry(0, DEFAULT_MESSAGE, getExpirationTime());
            table.put(sessionID, entry);
            table.update(context);
            ourCookie = new Cookie(COOKIE_NAME, sessionID + ":" + 0);
        }
        ourCookie.setMaxAge(COOKIE_TIMEOUT);
        response.addCookie(ourCookie);
        return ourCookie;
    }

    /**
     * Gets a new sessionID
     * @param context
     * @return
     */
    public static synchronized int getNewSessionID(ServletContext context) {
        Integer sessionID = (Integer)context.getAttribute("sessionCounter");
        if(sessionID == null)
            sessionID = -1;
        context.setAttribute("sessionCounter", ++sessionID);
        return sessionID;
    }

    /**
     * What the expiration time should be for the current request
     * @return
     */
    public static long getExpirationTime() {
        Calendar now = Calendar.getInstance();
        return now.getTimeInMillis() + TIMEOUT;
    }

    /**
     * Called every page load.
     * Cleans expired sessions, updates the expiration time, and returns the current session
     * Called from JSP.
     * @param context
     * @param request
     * @param response
     * @return
     */
    public static SessionTable.Entry sessionRequest(ServletContext context, HttpServletRequest request, HttpServletResponse response) {
        cleanExpiredSessions(context);

        Cookie cookie = SessionManager.getCookie(context, request, response);

        SessionTable table = SessionTable.getSessionTable(context);
        SessionTable.Entry entry = table.get(new Integer(cookie.getValue().split(":")[0]));
        entry.expiration = SessionManager.getExpirationTime();
        table.update(context);
        return entry;
    }

    /**
     * Removes expired sessions from the main session table
     * @param context
     */
    public static void cleanExpiredSessions(ServletContext context) {
        SessionTable table = SessionTable.getSessionTable(context);
        table.cleanExpiredSessions();
        table.update(context);
    }
}
