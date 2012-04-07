package identifiers;

/**
 * Represents the value of a cookie
 * (containing a sid, and svn)
 * and provides methods to convert between
 * this object and a string
 * @author jonathanpullano
 */
public class CookieVal {
    private SID sid;
    private SVN svn;

    public CookieVal(SID sid, SVN svn) {
        this.sid = sid;
        this.svn = svn;
    }

    public SID getSid() {
        return sid;
    }

    public void setSid(SID sid) {
        this.sid = sid;
    }

    public SVN getSvn() {
        return svn;
    }

    public void setSvn(SVN svn) {
        this.svn = svn;
    }

    @Override
    public String toString() {
        return sid.toString() + "_" + svn.toString();
    }

    public static CookieVal getCookieVal(String cookieString) {
        String[] split = cookieString.split("_");
        String sid = split[0] + "_" + split[1];
        String svn = split[2] + "_" + split[3] + "_" + split[4];
        return new CookieVal(SID.getSID(sid), SVN.getSVN(svn));
    }
}
