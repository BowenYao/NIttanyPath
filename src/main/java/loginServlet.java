import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.sql.*;

@WebServlet(urlPatterns={"/login"},loadOnStartup=1)
public class loginServlet extends HttpServlet {
    //handles navigation to login page
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String sessionID = request.getSession().getId();
        NittanyUser user = NittanyServer.authenticate(sessionID);
        if(user!= null){
            response.sendRedirect("/home");
            return;
        }
        RequestDispatcher dispatch = request.getRequestDispatcher("/login.html");
        System.out.println(request.toString());
        dispatch.forward(request,response);
    }
    //handles Post request to login page and checks if username and pw are in database
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException,IOException{
        String email = request.getParameter("email");
        //Removes email @ if it's included
        if(email.contains("@"))
            email = email.substring(0,email.indexOf('@'));
        int password = request.getParameter("password").hashCode();
        NittanyUser user = null;
        try{
            user = NittanyServer.login(email,password+"");
            if(user!=null) {
                NittanyServer.addUser(user, request.getSession().getId());
                response.sendRedirect("/home");
            }else{
               // RequestDispatcher dispatch = request.getRequestDispatcher("/login.html");
                Document login = Jsoup.parse(new File("src/main/webapp/login.html"),null);
                Document alertDocument = Jsoup.parse(new File("src/main/webapp/alertDefault.html"),null);
                Element alert = alertDocument.body().getElementById("alert");
                Element alertHeader = alert.getElementById("alert-header");
                Element alertBody = alert.getElementById("alert-body");
                alertHeader.html("Login failed!");
                alertBody.remove();
                alert.appendTo(login.head());
                response.getOutputStream().print(login.html());
               // dispatch.include(request,response);
            }
        }catch(SQLException e){
            e.printStackTrace();
        }
    }
}
