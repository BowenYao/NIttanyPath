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
import java.util.Set;

public class examServlet extends HttpServlet {
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        NittanyUser user = NittanyServer.authenticate(request.getSession().getId());
        String[] query = request.getQueryString().split("&");
        String courseId = query[0];
        int section = Integer.parseInt(query[1]);
        int examNum = Integer.parseInt(query[2]);
        if (user == null) {
            response.sendRedirect("/login");
            return;
        }
        NittanyCourse[] taughtCourses = user.getTaughtCourses();
        NittanyCourse course = null;
        if (taughtCourses != null) {
            for (NittanyCourse taughtCourse : taughtCourses) {
                if (courseId.equals(taughtCourse.getId()) && taughtCourse.getSection() == section) {
                    course = taughtCourse;
                    break;
                }
            }
        }
        if(course == null){
            response.sendRedirect("/home");
            return;
        }
        Document examDefault = Jsoup.parse(new File("src/main/webapp/examDefault.html"),null);
        Element navBarElement = examDefault.body().getElementById("course-navbar");
        navBarElement.html("<i class=\"fa fa-book\"></i> " + course.getId());
        navBarElement.attr("href", "/course?" + course.getId());
        examDefault.body().getElementById("exam-navbar").html("Exam " + examNum);
        examDefault.body().getElementById("exam-header").html("Exam " + examNum + " Grades");
        try {
            buildExamGrades(course, examNum, examDefault.body().getElementById("exam-display"));
        }catch(SQLException e){
            e.printStackTrace();
        }
        examDefault.body().getElementById("exam-form").attr("action","exam?"+courseId+"&"+section+"&"+examNum);
        response.getOutputStream().print(examDefault.html());
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
        int examNum = Integer.parseInt(query[2]);
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
        Set<String> emails = parameterMap.keySet();
        try {
            DBFunctions.batchUpdateExams(parameterMap, course, examNum);
        }catch(SQLException e){
            e.printStackTrace();
            response.sendRedirect("/home");
        }
        response.sendRedirect("/exam?" + courseId + "&"+section + "&" + examNum);
    }

    private void buildExamGrades(NittanyCourse course, int examNum, Element container) throws SQLException{
        ResultSet examInfo = DBFunctions.getExamStudentGrades(course,examNum);
        if(examInfo.first()){
            do{
                String email = examInfo.getString("student_email");
                double grade = examInfo.getDouble("grade");
                container.append("<tr id = \"new-row\"></tr>");
                Element row = container.getElementById("new-row");
                row.append("<td>" + email + "@nittany.edu</td>");
                row.append("<td><input type =\"number\" value = " + grade + " min =0.0 max = 100.0 step = 0.01 name =\""+email+"\"></td>");
                row.removeAttr("id");
            }while(examInfo.next());
            container.append("<input type = \"submit\" value = \"Submit Grades\"/>");
        }
    }
}
