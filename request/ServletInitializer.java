package request;

import identifiers.IPP;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import rpc.RpcServer;
import server.SimpleDB;

public class ServletInitializer extends HttpServlet {
    private static final long serialVersionUID = 6190255655842856682L;

    @Override
    public void init() throws ServletException
    {
        System.out.println("Server Initialized :D");

        RpcServer server = RpcServer.getInstance();
        //Start the RPC server
        server.start();
        IPP local = server.getIPPLocal();

        //TODO: Test on AWS
        SimpleDB db = SimpleDB.getInstance();
        db.createDomain(SimpleDB.MEMBER_LIST_DOMAIN);
        
        //TODO FIX ME to do periodic refresh... so spawn thread etc...
        //db.putMember(SimpleDB.MEMBER_LIST_DOMAIN, local);
    }
}
