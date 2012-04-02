package request;

import java.io.IOException;
import java.util.Date;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import server.SessionManager;

@WebServlet("/form")
public class Form extends HttpServlet {
    @Override
    public void doGet(HttpServletRequest request,
                      HttpServletResponse response)
        throws ServletException, IOException {
        SessionManager.sessionRequest(getServletContext(), request, response);

        //Redirect to form.jsp
        RequestDispatcher dispatcher =
        request.getRequestDispatcher("/WEB-INF/form.jsp");
        dispatcher.forward(request, response);
    }

    public static class FormData {
        private String message;
        private Date expiration;

        public FormData(String message, long expiration) {
            setMessage(message);
            setExpiration(expiration);
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public Date getExpiration() {
            return expiration;
        }

        public void setExpiration(long expiration) {
            this.expiration = new Date(expiration);
        }
    }
}