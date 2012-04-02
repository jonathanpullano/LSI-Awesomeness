package request;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import server.SessionManager;
import server.SessionTable;

@WebServlet("/logout")
public class Logout extends HttpServlet {
    @Override
    public void doGet(HttpServletRequest request,
                      HttpServletResponse response)
        throws ServletException, IOException {
        ServletContext context = getServletContext();
        Cookie cookie = SessionManager.getCookie(context, request, response);

        //Erase the session
        SessionTable table = SessionTable.getSessionTable(getServletContext());
        table.destroySession(new Integer(cookie.getValue().split(":")[0]));
        table.commit(context);

        //Print the logout page
        PrintWriter out = response.getWriter();
        out.println("Bye!");
    }
}
