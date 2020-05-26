

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Date;

@WebServlet(urlPatterns = {"dropClass"})
public class dropClassServlet extends HttpServlet {
public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
    String sessionId = request.getSession().getId();
    NittanyUser user = NittanyServer.authenticate(sessionId);
    if(user==null){
        response.sendRedirect("/login");
        return;
    }
    String courseId = request.getParameter("course-id");
    String password = request.getParameter("password").hashCode()+"";
    if(user.getClass().isInstance(new NittanyStudent())){
        NittanyStudent student = (NittanyStudent) user;
        for(NittanyCourse course:student.getEnrolledCourses())
            if (course.getId().equals(courseId) && course.getLateDropDeadline().before(new Date())) {
                try {
                    System.out.println(student.email + " | " + password);
                    NittanyUser login = NittanyServer.login(student.email, password);
                    if (login != null && login.equals(user)) {
                        DBFunctions.dropClass(student, courseId);
                        NittanyServer.updateSession(sessionId, password);
                        response.sendRedirect("/home");
                    } else {
                        response.sendRedirect("/course?" + courseId);
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                    response.sendRedirect("/home");
                }
                break;
            }
    }else{
        response.sendRedirect("/home");
    }
}
}
