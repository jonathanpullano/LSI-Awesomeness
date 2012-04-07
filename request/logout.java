package request;

import identifiers.CookieVal;

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

@WebServlet("/logout")
public class logout extends HttpServlet {

	private static final long serialVersionUID = 1L;
	
	@Override
    public void doGet(HttpServletRequest request,
                      HttpServletResponse response)
        throws ServletException, IOException {
        ServletContext context = getServletContext();
        Cookie cookie = SessionManager.getCookie(context, request, response);

        //Erase the session
        CookieVal cookieVal = CookieVal.getCookieVal(cookie.getValue());
        SessionManager.logout(cookieVal);

        //Print the logout page
        PrintWriter out = response.getWriter();
        out.println("Bye!");
    }
}
