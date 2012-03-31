package request;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import rpc.RpcServer;

public class ServletInitializer extends HttpServlet {
    private static final long serialVersionUID = 6190255655842856682L;

    public void init() throws ServletException
    {
        System.out.println("Server Initialized :D");
        
        //Forces server creation, so callIDs may be generated
        RpcServer.getInstance();
        
        //TODO: Add server ID to DB
    }
}
