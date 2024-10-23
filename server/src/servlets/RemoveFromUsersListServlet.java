package servlets;

import chat.users.UserManager;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import utils.ServletUtils;
import utils.SessionUtils;

import java.io.IOException;

@WebServlet("/removeuser")
public class RemoveFromUsersListServlet extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // Get the user manager instance
        UserManager userManager = ServletUtils.getUserManager(getServletContext());

        // Retrieve the username from the session
        String username = SessionUtils.getUsername(request);

        if (username != null) {
            System.out.println("Removing user: " + username);

            // Remove the user from the user set
            userManager.removeUser(username);

            // Clear the session
            request.getSession().invalidate();

            // Respond with success status
            response.setStatus(HttpServletResponse.SC_OK);
        } else {
            // User is not logged in, return an error status
            System.out.println("No user found in session.");
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }
    }
}
