package servlets;

import java.io.IOException;

import constant.Constant;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import chat.users.UserManager;
import utils.ServletUtils;
import utils.SessionUtils;

    @WebServlet("/login")
public class LoginServlet extends HttpServlet {

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");

        // Get username from session
        String usernameFromSession = SessionUtils.getUsername(request);
        UserManager userManager = ServletUtils.getUserManager(getServletContext());

        if (usernameFromSession == null) {
            // User not logged in
            String usernameFromParameter = request.getParameter(Constant.USERNAME);

            if (usernameFromParameter == null || usernameFromParameter.isEmpty()) {
                // No username in session or parameter, redirect to signup
                response.sendRedirect("/signup/signup.html");
            } else {
                usernameFromParameter = usernameFromParameter.trim();

                // Synchronized block to avoid race conditions
                synchronized (this) {
                    if (userManager.isUserExists(usernameFromParameter)) {
                        // Username already exists, respond with error
                        response.setStatus(HttpServletResponse.SC_CONFLICT);  // 409 Conflict
                        response.getWriter().write("Username " + usernameFromParameter + " already exists. Please try a different username.");
                    } else {
                        // Add the new user and store in session
                        userManager.addUser(usernameFromParameter);
                        request.getSession(true).setAttribute(Constant.USERNAME, usernameFromParameter);

                        // Login success
                        response.setStatus(HttpServletResponse.SC_OK);  // 200 OK
                    }
                }
            }
        } else {
            // User already logged in, no need to log in again
            response.setStatus(HttpServletResponse.SC_OK);  // 200 OK
        }
    }

    // Override doGet method to handle GET requests
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    // Override doPost method to handle POST requests
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }
}
