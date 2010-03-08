package guestbook;

import java.io.IOException;
import javax.servlet.http.*;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;

public class GuestbookServlet extends HttpServlet {
    
    UserService userService=null;
    public void doGet(HttpServletRequest req, HttpServletResponse resp)
              throws IOException {
        UserService userService = getUserService();
        User user = userService.getCurrentUser();

        if (user != null) {
            resp.setContentType("text/plain");
            resp.getWriter().println("Hello, " + user.getNickname());
        } else {
            resp.sendRedirect(userService.createLoginURL(req.getRequestURI()));
        }
    }
    public UserService getUserService () {
        if (userService==null) userService=UserServiceFactory.getUserService();
        return userService;
    }
    public void setUserService (UserService u) {
        this.userService=u;
    }
}

