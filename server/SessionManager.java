package server;

import identifiers.CookieVal;
import identifiers.FormData;
import identifiers.FormData.Location;
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

public class SessionManager {
    public static String DEFAULT_MESSAGE = "Hello, user!";
    public static String COOKIE_NAME = "CS5300PROJECT1SESSION";
    public static int TIMEOUT = 1000 * 60 * 30;
    public static int COOKIE_TIMEOUT = TIMEOUT/1000;
    private static boolean DEBUG=true;
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
//            if(ourCookie != null) {
//                SessionTable table = SessionTable.getInstance();
//                if(DEBUG) System.out.println("Cookie was found and here it is (" + ourCookie.getValue() + ")");
//                CookieVal cookieVal = CookieVal.getCookieVal(ourCookie.getValue());
//                SessionTable.Entry entry = table.get(cookieVal.getSid(), cookieVal.getSvn().getChangeCount());
//                //if(DEBUG) System.out.println("SessionTable Cookie version (" + entry.message + ")");
//            }
        }
        //No cookie found, so make a new one
        if(ourCookie == null){
        	if(DEBUG) System.out.println("ourCookie is null");
            ourCookie = newSession(context);
        }
        ourCookie.setMaxAge(COOKIE_TIMEOUT);
        response.addCookie(ourCookie);
        return ourCookie;
    }

    public static Cookie newSession(ServletContext context) {
        return writeRequest(context, DEFAULT_MESSAGE, new SID(getNewSessNum(context), RpcServer.getInstance().getIPPLocal()), 
        		new SVN(-1, IPP.getNullIpp(), IPP.getNullIpp())); 
    }

    public static Cookie writeRequest(ServletContext context, String newData, SID sid, SVN svn) {
        FormData data = FormManager.getInstance().getData();
        data.setNewUpdated(true);
        SessionTable table = SessionTable.getInstance();
        long discardTime = getExpirationTime();
        int newChangeCount = svn.getChangeCount()+1;
        SessionTable.Entry entry = new SessionTable.Entry(newChangeCount, newData, discardTime);
        table.put(sid, entry);
        data.setMessage(newData);
        data.setExpiration(discardTime);

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
        IPP newIppPrimary = ippLocal;
        IPP newIppBackup = null;
        for(IPP ipp : members) {
            if(ipp.equals(ippLocal) || ipp.isNull())
                continue;
            if(RpcMessageCall.SessionWrite(ipp, sid, newChangeCount, newData, discardTime)) {
                newIppBackup = ipp;
                newSvn = new SVN(newChangeCount, newIppPrimary, newIppBackup);
                HashSet<IPP> set = new HashSet<IPP>();
                set.add(ippPrimary);
                set.add(ippBackup);
                set.remove(ippLocal);
                set.remove(ipp);
                set.remove(IPP.getNullIpp());
                if(!set.isEmpty())
                    RpcMessageCall.SessionDelete(set, sid, newChangeCount);
                break;
            }
        }
        if(newSvn == null) {
            newIppBackup = IPP.getNullIpp();
            newSvn = new SVN(newChangeCount, newIppPrimary, newIppBackup);
        }
        data.setIppPrimary(newIppPrimary);
        data.setIppBackup(newIppBackup);
        return new Cookie(COOKIE_NAME, new CookieVal(sid, newSvn).toString());
    }
    
    public static boolean readRequest(HttpServletResponse response, SID sid, SVN svn) {
        FormData data = FormManager.getInstance().getData();
        IPP ippPrimary = svn.getIppPrime();
        IPP ippBackup = svn.getIppBackup();
        IPP ippLocal = RpcServer.getInstance().getIPPLocal();
        long discardTime = getExpirationTime();
        if(ippPrimary.isNull() && ippBackup.isNull()) {
            //Messed up cookie
            return false; 
        } else if(ippBackup.isNull() && !ippPrimary.equals(ippLocal)) {
          //Trigger self repair 3.4
          ReadResult read = RpcMessageCall.SessionRead(ippPrimary, sid, svn.getChangeCount());
          if(read != null) {
              SessionTable.getInstance().put(sid, new Entry(svn.getChangeCount(), read.getData(), read.getDiscardTime()));
              ippBackup = ippLocal;
              SID newSid = new SID(sid.getSessNum(), RpcServer.getInstance().getIPPLocal());
              SVN newSvn = new SVN(svn.getChangeCount(), ippPrimary, ippBackup);
              data.setLoc(Location.ippPrimary);
              data.setMessage(read.getData());
              data.setExpiration(read.getDiscardTime());
              response.addCookie(new Cookie(COOKIE_NAME, new CookieVal(newSid, newSvn).toString()));
              return true;
          }
          return false;
        }
        ArrayList<IPP> ipps = new ArrayList<IPP>();
        ipps.add(ippPrimary);
        ipps.add(ippBackup);
        for(IPP ipp : ipps) {
            if(ipp.isNull())
                continue;
            if(ipp.equals(ippLocal)) {
                Entry entry = SessionTable.getInstance().get(sid, svn.getChangeCount());
                if(entry != null) {
                    if(ipp.equals(ippPrimary)) {
                        data.setLoc(Location.ippPrimary);
                        data.setIppPrimary(ippPrimary);
                    } else {
                        data.setLoc(Location.ippBackup);
                        data.setIppBackup(ippBackup);
                    }
                    data.setMessage(entry.message);
                    entry.expiration = discardTime;
                    data.setExpiration(discardTime);
                    return true;
                }
            } else {
                ReadResult read = RpcMessageCall.SessionRead(ipp, sid, svn.getChangeCount());
                if(read != null) {
                    if(ipp.equals(ippPrimary)) {
                        data.setLoc(Location.ippPrimary);
                        data.setIppPrimary(ippPrimary);
                    } else {
                        data.setLoc(Location.ippBackup);
                        data.setIppBackup(ippBackup);
                    }
                    data.setMessage(read.getData());
                    data.setExpiration(read.getDiscardTime());
                    return true;
                }
            }
        }
        return false;
    }
    
//    public static boolean readRequest(HttpServletResponse response, SID sid, SVN svn) {
//        FormData data = FormManager.getInstance().getData();
//        IPP ippPrimary = svn.getIppPrime();
//        IPP ippBackup = svn.getIppBackup();
//        IPP ippLocal = RpcServer.getInstance().getIPPLocal();
//        long discardTime = getExpirationTime();
//        
//        if(ippPrimary.equals(ippLocal)) {
//            //We are the primary server, so return the data
//            Entry entry = SessionTable.getInstance().get(sid, svn.getChangeCount());
//            if(entry != null) {
//                data.setLoc(Location.ippPrimary);
//                data.setIppPrimary(ippPrimary);
//                data.setMessage(entry.message);
//                entry.expiration = discardTime;
//                data.setExpiration(discardTime);
//                return true;
//            } else {
//                ReadResult read = RpcMessageCall.SessionRead(ippBackup, sid, svn.getChangeCount());
//                if(read != null) {
//                    data.setLoc(Location.ippBackup);
//                    data.setIppPrimary(ippBackup);
//                    data.setMessage(read.getData());
//                    data.setExpiration(read.getDiscardTime());
//                    return true;
//                }
//            }
//            return false;
//        } else if(ippBackup.isNull()) {
//            //Backup is null. Trigger self-repair case from 3.4
//            ReadResult read = RpcMessageCall.SessionRead(ippPrimary, sid, svn.getChangeCount());
//            if(read != null) {
//                SessionTable.getInstance().put(sid, new Entry(svn.getChangeCount(), read.getData(), read.getDiscardTime()));
//                ippBackup = ippLocal;
//                SID newSid = new SID(sid.getSessNum(), RpcServer.getInstance().getIPPLocal());
//                SVN newSvn = new SVN(svn.getChangeCount(), ippPrimary, ippBackup);
//                data.setLoc(Location.ippPrimary);
//                data.setMessage(read.getData());
//                data.setExpiration(read.getDiscardTime());
//                response.addCookie(new Cookie(COOKIE_NAME, new CookieVal(newSid, newSvn).toString()));
//                return true;
//            }
//            return false;
//        } else if(ippBackup.equals(ippLocal)) {
//            //We are the backup server, so return the data
//            Entry entry = SessionTable.getInstance().get(sid, svn.getChangeCount());
//            if(entry != null) {
//                data.setLoc(Location.ippBackup);
//                data.setIppPrimary(ippBackup);
//                data.setMessage(entry.message);
//                entry.expiration = discardTime;
//                data.setExpiration(discardTime);
//                return true;
//            } else {
//                ReadResult read = RpcMessageCall.SessionRead(ippPrimary, sid, svn.getChangeCount());
//                if(read != null) {
//                    data.setLoc(Location.ippPrimary);
//                    data.setIppPrimary(ippPrimary);
//                    data.setMessage(read.getData());
//                    data.setExpiration(read.getDiscardTime());
//                    return true;
//                }
//            }
//            return false;
//        }
//        ArrayList<IPP> ippList = new ArrayList<IPP>();
//        //Primary should never be null, and backup should be self-repaired
//        ippList.add(ippPrimary);
//        ippList.add(ippBackup);
//        ReadResult result = null;
//        if(!ippList.isEmpty()) {
//            result = RpcMessageCall.SessionRead(ippList, sid, svn.getChangeCount());
//            if(result.getServerID().equals(ippPrimary)) {
//                data.setLoc(Location.ippPrimary);
//                data.setIppPrimary(ippPrimary);
//            } else if(result.getServerID().equals(ippBackup)) {
//                data.setLoc(Location.ippBackup);
//                data.setIppBackup(ippBackup);
//            } else
//                System.err.println("Invalid Server");
//        }
//        if(result != null) {
//            data.setMessage(result.getData());
//            data.setExpiration(result.getDiscardTime());
//            return true;
//        }
//        return false;
//    }   

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

    public static void logout(CookieVal cookieVal) {
        SessionTable.getInstance().destroySession(cookieVal.getSid());
        HashSet<IPP> servers = new HashSet<IPP>();
        servers.add(cookieVal.getSvn().getIppBackup());
        servers.add(cookieVal.getSvn().getIppPrime());
        servers.remove(RpcServer.getInstance().getIPPLocal());
        servers.remove(IPP.getNullIpp());
        RpcMessageCall.SessionDelete(servers, cookieVal.getSid(), cookieVal.getSvn().getChangeCount());
    }
}
