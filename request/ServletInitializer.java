package request;

import identifiers.IPP;

import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import rpc.RpcServer;
import rpc.message.RpcMessageCall;
import rpc.message.RpcMessageCall.ReadResult;

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

        //TODO: Add server IPP to DB

        ArrayList<IPP> ipps = new ArrayList<IPP>();
        ipps.add(local);
        System.out.println("got here3");
        ReadResult reply = RpcMessageCall.SessionRead(ipps, 1, 1);
        System.out.println(reply.getData());
        //TODO: Test on AWS
        SimpleDB db = new SimpleDB();
        db.createDomain(SimpleDB.MEMBER_LIST_DOMAIN);
        db.putMember(SimpleDB.MEMBER_LIST_DOMAIN, local);
    }
}
