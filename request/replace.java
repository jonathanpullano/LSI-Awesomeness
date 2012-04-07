package request;

import identifiers.CookieVal;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import server.FormManager;
import server.SessionManager;

@WebServlet("/replace")
public class replace extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
    @Override
    public void doGet(HttpServletRequest request,
                      HttpServletResponse response)
        throws ServletException, IOException {
        FormManager.getInstance().newRequest();
        ServletContext context = getServletContext();
        Cookie cookie = SessionManager.getCookie(context, request, response);
        CookieVal cookieVal = CookieVal.getCookieVal(cookie.getValue());

        //Updates the session with the new text
        String newText = (String) request.getParameter("NewText");
        //Prevent the newText from being too long, to keep the session small.
        if(newText != null)
            newText = newText.substring(0, Math.min(newText.length(), 512));
        Cookie newCookie = SessionManager.writeRequest(context, newText, cookieVal.getSid(), cookieVal.getSvn());
        response.addCookie(newCookie);

        //Redirect to form.jsp
        RequestDispatcher dispatcher =
        request.getRequestDispatcher("/WEB-INF/form.jsp");
        dispatcher.forward(request, response);
        FormManager.getInstance().endRequest();
    }
}
