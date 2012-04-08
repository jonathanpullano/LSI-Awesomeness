package request;

import identifiers.CookieVal;
import identifiers.FormData;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import server.FormManager;
import server.SessionManager;

@WebServlet("/form")
public class Form extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
    @Override
    public void doGet(HttpServletRequest request,
                      HttpServletResponse response)
        throws ServletException, IOException {
        FormManager.getInstance().newRequest();
        Cookie cookie = SessionManager.getCookie(getServletContext(), request, response);
        
        CookieVal cookieVal = CookieVal.getCookieVal(cookie.getValue());
        boolean found = SessionManager.readRequest(response, cookieVal.getSid(), cookieVal.getSvn());
        request.setAttribute("data", FormManager.getInstance().getData());

        RequestDispatcher dispatcher = null;
        if(!found) {
            SessionManager.deleteCookie(response, cookie);
            dispatcher = request.getRequestDispatcher("/WEB-INF/error.jsp");
        } else
            dispatcher = request.getRequestDispatcher("/WEB-INF/form.jsp");
        dispatcher.forward(request, response);
        FormManager.getInstance().endRequest();
    }
}