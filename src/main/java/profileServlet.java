import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

@WebServlet(urlPatterns = {"profile","my-profile"})
public class profileServlet extends HttpServlet {
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        String sessionId = request.getSession().getId();
        NittanyUser user = NittanyServer.authenticate(sessionId);
        if(user==null){
            response.sendRedirect("/login");
            return;
        }
        response.getOutputStream().print(buildProfile(user));
    }
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException{
        String sessionId = request.getSession().getId();
        NittanyUser user = NittanyServer.authenticate(sessionId);
        if(user==null){
            response.sendRedirect("/login");
            return;
        }
        String email = user.getEmail();
        String password = request.getParameter("oldPassword").hashCode()+"";
        String newPassword = request.getParameter("newPassword").hashCode() + "";
        boolean updateSuccess = false;
        try {
            NittanyUser login = NittanyServer.login(email,password);
            if(login!=null && login.equals(user)){
                updateSuccess = true;
                DBFunctions.updatePassword(user,newPassword);
            }
        }catch(SQLException e){
            e.printStackTrace();
            updateSuccess = false;
        }
        Document alertDocument = Jsoup.parse(new File("src/main/webapp/alertDefault.html"),null);
        Element alert = alertDocument.body().getElementById("alert");
        Element alertHeader = alert.getElementById("alert-header");
        Element alertBody = alert.getElementById("alert-body");
        if(updateSuccess){
            alertHeader.html("Password updated sucessfully!");
            alertBody.remove();
            Elements redElements = alertDocument.body().getElementsByAttributeValueContaining("class","w3-red");
            System.out.println("BEFORE: \n" +redElements.html());
            for(Element redElement:redElements) {
                redElement.attr("class", redElement.attr("class").replace("red", "green"));
            }
            System.out.println("AFTER:\n"+redElements.html());
        }else{
            alertHeader.html("Password incorrect!");
            alertBody.html("Password not updated");
        }
        Document profileHtml = Jsoup.parse(buildProfile(user));
        alert.appendTo(profileHtml.head());
        System.out.println(alert.html());
        response.getOutputStream().print(profileHtml.html());
    }
    public static String buildProfile(NittanyUser user) throws IOException{
        Document document = Jsoup.parse(new File("src/main/webapp/profile.html"),null);
        Element profileContainer = document.body().getElementById("profile-info");
        profileContainer.append("<h4>" + user.getName()+"</h4>");
        profileContainer.append("<h4>Email: " + user.getEmail() + "@nittany.edu</h4>");
        profileContainer.append("<h4>Age: " + user.getAge() + " years old</h4>");
        if(user.gender)
            profileContainer.append("<h4>Gender: Male <i class = \"fa fa-male\"></i></h4>");
        else
            profileContainer.append("<h4>Gender: Female <i class = \"fa fa-female\"></i></h4>");
        if(user.getClass().isInstance(new NittanyStudent())){
            NittanyStudent student = (NittanyStudent) user;
            profileContainer.append("<h4>Home Address:</h4>" +student.getAddressHtml());
        }else if(user.getClass().isInstance(new NittanyProfessor())){
            NittanyProfessor professor = (NittanyProfessor) user;
            profileContainer.append("<h4>" + professor.getTitle() + " of the " + professor.getDepartment() + " department</h4>");
            profileContainer.append("<h4>Office: " + professor.getOfficeAddress()+"</h4>");
        }
        return document.html();
    }
}
