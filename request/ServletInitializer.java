package request;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import rpc.RpcServer;
import server.SessionTable;
import server.SimpleDB;

public class ServletInitializer extends HttpServlet {
    private static final long serialVersionUID = 6190255655842856682L;

    @Override
    public void init() throws ServletException
    {
        System.out.println("Server Initialized :D");

        //Start the RPC server
        RpcServer.getInstance().start();
        
        //Start the garbage collector
        SessionTable.getInstance().start();

        //TODO: Test on AWS
        SimpleDB db = SimpleDB.getInstance();
        
        /**********************************************/
        /*****/ db.deleteDomain(SimpleDB.MEMBER_LIST_DOMAIN); //DELETE THIS LINE!!!!!!!!
        /**********************************************/
        
        db.createDomain(SimpleDB.MEMBER_LIST_DOMAIN);
        db.start();
    }
}
