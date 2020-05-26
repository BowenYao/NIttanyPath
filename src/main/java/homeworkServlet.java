import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

public class homeworkServlet extends HttpServlet {
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        NittanyUser user = NittanyServer.authenticate(request.getSession().getId());
        String[] query = request.getQueryString().split("&");
        String courseId = query[0];
        int section = Integer.parseInt(query[1]);
        int hwNum = Integer.parseInt(query[2]);
        if (user == null) {
            response.sendRedirect("/login");
            return;
        }
        NittanyCourse[] taughtCourses = user.getTaughtCourses();
        NittanyCourse course = null;
        if (taughtCourses != null) {
            for (NittanyCourse taughtCourse : taughtCourses) {
                if (courseId.equals(taughtCourse.getId())&& taughtCourse.getSection()==section) {
                    course = taughtCourse;
                    break;
                }
            }
        }
        if(course == null){
            response.sendRedirect("/home");
            return;
        }
        Document homeworkDefault = Jsoup.parse(new File("src/main/webapp/homeworkDefault.html"),null);
        Element navBarElement = homeworkDefault.body().getElementById("course-navbar");
        navBarElement.html("<i class=\"fa fa-book\"></i> " + course.getId());
        navBarElement.attr("href", "/course?" + course.getId());
        homeworkDefault.body().getElementById("hw-navbar").html("Homework " + hwNum);
        homeworkDefault.body().getElementById("homework-header").html("Homework " + hwNum + " Grades");
        try {
            buildHomeworkGrades(course, hwNum, homeworkDefault.body().getElementById("homework-display"));
        }catch(SQLException e){
            e.printStackTrace();
        }
        homeworkDefault.body().getElementById("homework-form").attr("action","homework?"+courseId+"&"+section+"&"+hwNum);
        response.getOutputStream().print(homeworkDefault.html());
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        NittanyUser user = NittanyServer.authenticate(request.getSession().getId());
        if(user==null){
            response.sendRedirect("/login");
            return;
        }
        String[] query = request.getQueryString().split("&");
        String courseId = query[0];
        int section = Integer.parseInt(query[1]);
        int hwNum = Integer.parseInt(query[2]);
        NittanyCourse[] taughtCourses = user.getTaughtCourses();
        NittanyCourse course = null;
        if (taughtCourses != null) {
            for (NittanyCourse taughtCourse : taughtCourses) {
                if (courseId.equals(taughtCourse.getId())&& taughtCourse.getSection()==section) {
                    course = taughtCourse;
                    break;
                }
            }
        }
        if(course == null){
            response.sendRedirect("/home");
            return;
        }
        Map<String,String[]> parameterMap = request.getParameterMap();
        try {
            DBFunctions.batchUpdateHW(parameterMap, course, hwNum);
        }catch(SQLException e){
            e.printStackTrace();
            response.sendRedirect("/home");
        }
        response.sendRedirect("/homework?" + courseId + "&"+section + "&" + hwNum);
    }

    private void buildHomeworkGrades(NittanyCourse course, int hwNum, Element container) throws SQLException{
        ResultSet homeworkInfo = DBFunctions.getHWStudentGrades(course,hwNum);
        if(homeworkInfo.first()){
            do{
                String email = homeworkInfo.getString("student_email");
                double grade = homeworkInfo.getDouble("grade");
                container.append("<tr id = \"new-row\"></tr>");
                Element row = container.getElementById("new-row");
                row.append("<td>" + email + "@nittany.edu</td>");
                row.append("<td><input type =\"number\" value = " + grade + " min =0.0 max = 100.0 step = 0.01 name =\""+email+"\"></td>");
                row.removeAttr("id");
            }while(homeworkInfo.next());
            container.append("<input type = \"submit\" value = \"Submit Grades\"/>");

        }
    }
}
