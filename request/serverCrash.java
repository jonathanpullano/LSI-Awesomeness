package request;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/serverCrash")
public class serverCrash extends HttpServlet {
    private static final long serialVersionUID = 1L;
    
    @Override
    public void doGet(HttpServletRequest request,
                      HttpServletResponse response) {
        System.err.println("SHE CAN'T TAKE MUCH MORE OF THIS CAPTAIN!");
        System.exit(1);
    }
}
