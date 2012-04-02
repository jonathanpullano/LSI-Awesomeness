package request;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import request.Form.FormData;
import server.SessionManager;
import server.SessionTable;

@WebServlet("/replace")
public class Replace extends HttpServlet {
    @Override
    public void doGet(HttpServletRequest request,
                      HttpServletResponse response)
        throws ServletException, IOException {
        ServletContext context = getServletContext();
        Cookie cookie = SessionManager.getCookie(context, request, response);

        //Updates the session with the new text
        String newText = (String) request.getParameter("NewText");
        //Prevent the newText from being too long, to keep the session small.
        if(newText != null)
            newText = newText.substring(0, Math.min(newText.length(), 512));
        SessionTable table = SessionTable.getSessionTable(getServletContext());
        SessionTable.Entry entry = table.get(new Integer(cookie.getValue().split(":")[0]));
        entry.expiration = SessionManager.getExpirationTime();
        entry.message = newText;
        entry.version++;
        table.commit(context);
        request.setAttribute("data", new FormData(entry.message,entry.expiration));

        //Redirect to form.jsp
        RequestDispatcher dispatcher =
        request.getRequestDispatcher("/WEB-INF/form.jsp");
        dispatcher.forward(request, response);
    }
}
