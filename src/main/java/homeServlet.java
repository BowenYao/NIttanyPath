import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

@WebServlet(urlPatterns = {"home"})
public class homeServlet extends HttpServlet{
    private static final String enrolledHeader ="<h2 class = \"w3-center\">Enrolled Courses</h2>";
    private static final String teachingHeader = "<h2 class = \"w3-center\">Teaching Courses</h2>";
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException{
        HttpSession session = request.getSession();
        String sessionID = session.getId();
        NittanyUser user = NittanyServer.authenticate(sessionID);
        if(user == null){
            response.sendRedirect("/login");
            return;
        }
        NittanyServer.addUser(user,sessionID);
        Document homePageHTMl = Jsoup.parse(new File("src/main/webapp/home.html"),null);
        Element courseDisplay =  homePageHTMl.body().getElementById("course-display");
        String email = user.getEmail();
        String name = user.getName();
        if(user.getClass().isInstance(new NittanyStudent())){
            NittanyStudent student = (NittanyStudent) user;
            NittanyCourse[] enrolledCourses = student.getEnrolledCourses();
            if(enrolledCourses!=null) {
                courseDisplay.append(enrolledHeader);
                try {
                    constructCourseBoxes(enrolledCourses, courseDisplay);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }else if(user.getClass().isInstance(new NittanyProfessor())){
        }
        NittanyCourse[] teachingCourses = user.getTaughtCourses();
        if(teachingCourses!=null) {
            courseDisplay.append(teachingHeader);
            try{
                constructCourseBoxes(teachingCourses,courseDisplay);
            }catch(SQLException e){
                e.printStackTrace();
            }
        }
        homePageHTMl.body().getElementById("welcome-message").html("Welcome " + name + "!");
        response.getOutputStream().print(homePageHTMl.toString());
    }
    public static void constructCourseBoxes(NittanyCourse[] courses,Element courseDisplay) throws IOException, SQLException {
        File defaultBox = new File("src/main/webapp/courseBoxDefault.html");
        Document document = Jsoup.parse(defaultBox,null);
        Element box = document.body().getElementById("course-box");
       // Element box = document.body().getElementById("course-box").getElementById("enrolled-courses");
        for(NittanyCourse course: courses){
            box.attr("href","/course?" + course.getId());
            box.getElementById("course-id").html(course.getId() + " Section " + course.getSection());
            box.getElementById("course-name").html(course.getName());
            box.getElementById("course-description").html(course.getDescription());
            box.clone().appendTo(courseDisplay);
        }
    }
}
