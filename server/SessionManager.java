package server;

import identifiers.CookieVal;
import identifiers.FormData;
import identifiers.IPP;
import identifiers.SID;
import identifiers.SVN;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;

import javax.servlet.ServletContext;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import rpc.RpcServer;
import rpc.message.RpcMessageCall;
import rpc.message.RpcMessageCall.ReadResult;
import server.SessionTable.Entry;

//jonathan
//TODO: Handle contexts/synchrony
//TODO: Re-enable Garbage Collection
//TODO: Make sure cookies are not replicated and deleted properly
//TODO: Debug, Debug

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
                SessionTable table = SessionTable.getInstance();
                SessionTable.Entry entry = table.get(CookieVal.getCookieVal(ourCookie.getValue()).getSid());
                if(entry == null) {
                    //It's mangled. Throw it out.
                    ourCookie = null;
                }
            }
        }
        //No cookie found, so make a new one
        if(ourCookie == null)
            ourCookie = newSession(context);
        ourCookie.setMaxAge(COOKIE_TIMEOUT);
        response.addCookie(ourCookie);
        return ourCookie;
    }

    public static Cookie newSession(ServletContext context) {
        return writeRequest(context, DEFAULT_MESSAGE, new SID(getNewSessNum(context), RpcServer.getInstance().getIPPLocal()), new SVN(-1, IPP.getNullIpp(), IPP.getNullIpp())); //TODO:fix
    }

    public static Cookie writeRequest(ServletContext context, String newData, SID sid, SVN svn) {
        SessionTable table = SessionTable.getInstance();
        long discardTime = getExpirationTime();
        int newChangeCount = svn.getChangeCount()+1;
        SessionTable.Entry entry = new SessionTable.Entry(newChangeCount, newData, discardTime);
        table.put(sid, entry);
        context.setAttribute("data", new FormData(newData, discardTime));

        IPP ippLocal = RpcServer.getInstance().getIPPLocal();
        ArrayList<IPP> members = SimpleDB.getInstance().getLocalMembers();

        IPP ippPrimary = svn.getIppPrime();
        IPP ippBackup = svn.getIppBackup();
        
        //Check primary and backup first
        members.remove(ippPrimary);
        members.remove(ippBackup);
        members.add(0, ippPrimary);
        members.add(0, ippBackup);
        SVN newSvn = null;
        for(IPP ipp : members) {
            if(ipp.equals(ippLocal) || ipp.isNull())
                continue;
            if(RpcMessageCall.SessionWrite(ipp, sid, newChangeCount, discardTime)) {
                svn = new SVN(newChangeCount, ippLocal, ipp);
                HashSet<IPP> set = new HashSet<IPP>();
                set.add(ippPrimary);
                set.add(ippBackup);
                set.remove(ippLocal);
                set.remove(ipp);
                RpcMessageCall.SessionDelete(set, sid, newChangeCount);
                break;
            }
        }
        if(newSvn == null)
            newSvn = new SVN(newChangeCount, ippLocal, IPP.getNullIpp());
        return new Cookie(COOKIE_NAME, new CookieVal(sid, newSvn).toString());
    }

    public static FormData readRequest(HttpServletResponse response, SID sid, SVN svn) {
        IPP ippPrimary = svn.getIppPrime();
        IPP ippBackup = svn.getIppBackup();
        IPP ippLocal = RpcServer.getInstance().getIPPLocal();
        if(ippPrimary.equals(ippLocal)) {
            //We are the primary server, so return the data
            Entry entry = SessionTable.getInstance().get(sid);
            return new FormData(entry.message, entry.version);
        } else if(ippBackup.isNull()) {
            //Backup is null. Trigger self-repair case from 3.4
            ippBackup = ippLocal;
            SID newSid = new SID(sid.getSessNum(), RpcServer.getInstance().getIPPLocal());
            SVN newSvn = new SVN(svn.getChangeCount(), ippPrimary, ippBackup);
            response.addCookie(new Cookie(COOKIE_NAME, new CookieVal(newSid, newSvn).toString()));
        } else if(ippBackup.equals(ippLocal)) {
            //We are the backup server, so return the data
            Entry entry = SessionTable.getInstance().get(sid);
            return new FormData(entry.message, entry.version);
        }
        ArrayList<IPP> ippList = new ArrayList<IPP>();
        //Primary should never be null, and backup should be self-repaired
        ippList.add(ippPrimary);
        ippList.add(ippBackup);
        ReadResult result = null;
        if(!ippList.isEmpty())
            result = RpcMessageCall.SessionRead(ippList, sid, svn.getChangeCount());
        if(result != null)
            return new FormData(result.getData(), result.getDiscardTime());
        return null;
    }   

    /**
     * Gets a new sessionID
     * @param context
     * @return
     */
    public static synchronized int getNewSessNum(ServletContext context) {
        Integer sessNum = (Integer)context.getAttribute("sessionCounter");
        if(sessNum == null)
            sessNum = -1;
        context.setAttribute("sessionCounter", ++sessNum);
        return sessNum;
    }

    /**
     * What the expiration time should be for the current request
     * @return
     */
    public static long getExpirationTime() {
        Calendar now = Calendar.getInstance();
        return now.getTimeInMillis() + TIMEOUT;
    }

    public static void deleteCookie(HttpServletResponse response, Cookie toDelete) {
        Cookie cookie = new Cookie(toDelete.getName(), toDelete.getValue());
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }
}
