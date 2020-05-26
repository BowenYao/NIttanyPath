import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

@WebServlet(urlPatterns = {"/course?.*"})
public class courseServlet extends HttpServlet {
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        NittanyUser user = NittanyServer.authenticate(request.getSession().getId());
        if (user == null) {
            response.sendRedirect("/login");
            return;
        }
        String courseID = request.getQueryString();
        NittanyCourse[] taughtCourses = user.getTaughtCourses();
        NittanyCourse course = null;
        String coursePage = "";
        if (taughtCourses != null) {
            try {
                for (NittanyCourse taughtCourse : taughtCourses) {
                    if (courseID.equals(taughtCourse.getId())) {
                        course = taughtCourse;
                        coursePage = buildTeachingCoursePage(course, user);
                        break;
                    }
                }
            }catch(SQLException e){
                e.printStackTrace();
            }
        }
        if (course == null && user.getClass().isInstance(new NittanyStudent())) {
            NittanyStudent student = (NittanyStudent) user;
            try {
                for (NittanyCourse enrolledCourse : student.getEnrolledCourses()) {
                    System.out.println(enrolledCourse.getId() + " " + courseID);
                    if (courseID.equals(enrolledCourse.getId())) {
                        course = enrolledCourse;
                        coursePage = buildEnrolledCoursePage(course, student);
                        break;
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        if (course == null) {
            System.out.println("what?");
            response.sendRedirect("/home");
            return;
        }
       // System.out.println(coursePage);
        response.getOutputStream().print(coursePage.replaceAll("\\|\\|newline","\n"));
        System.out.println(coursePage.replaceAll("\\|\\|newline","\n"));
        // request.getRequestDispatcher("/courseDefault.html").forward(request,response);
    }
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,ServletException{
        NittanyUser user = NittanyServer.authenticate(request.getSession().getId());
        if (user == null) {
            response.sendRedirect("/login");
            return;
        }
        String courseId = request.getQueryString();
        String postContent = request.getParameter("post-content");
        String commentContent = request.getParameter("comment-content");
        String email = user.getEmail();
        if(postContent!=null) {
            try {
                DBFunctions.makePost(courseId, postContent, email);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }else if(commentContent!=null){
            int postNum = Integer.parseInt(request.getParameter("post-no"));
            try{
                DBFunctions.makeComment(courseId,commentContent,postNum,email);
            }catch(SQLException e){
                e.printStackTrace();
            }
        }
        response.sendRedirect("/course?"+courseId);
    }
    private Element buildCourseHeader(Document courseDefault, NittanyCourse course) throws SQLException{
        Element navBarElement = courseDefault.body().getElementById("course-navbar");
        navBarElement.html("<i class=\"fa fa-book\"></i> " + course.getId());
        navBarElement.attr("href", "/course?" + course.getId());
        Element courseInfo = courseDefault.body().getElementById("course-info-box");
        courseInfo.append("<h2>" + course.getId() + " Section " + course.getSection() + " </h2>");
        courseInfo.append("<h5>" + course.getName() + "</h5>");
        courseInfo.append("<h7>" + course.getDescription() + "</h7>");
        ResultSet profInfo = DBFunctions.getCoursePageInfo(course);
        profInfo.first();
        courseInfo.append("<h6>Professor: " + profInfo.getString("name") + "</h6>");
        courseInfo.append("<h6>" + profInfo.getString("email") + "@nittany.edu</h6>");
        courseInfo.append("<h6>Office: " + profInfo.getString("office_address") + "</h6>");
        return courseInfo;
    }
    private String buildTeachingCoursePage(NittanyCourse course, NittanyUser user)throws IOException, SQLException{
        Document courseDefault = Jsoup.parse(new File("src/main/webapp/courseDefault.html"),null);
        Element courseInfo = buildCourseHeader(courseDefault,course);
        ResultSet homeworkInfo = DBFunctions.getCourseHomework(course);
        buildTeachingHomeworks(homeworkInfo,courseDefault.body().getElementById("homework-display"),course);
        homeworkInfo.close();
        courseDefault.body().getElementById("add-homework-course-id").attr("value",course.getId());
        courseDefault.body().getElementById("add-homework-section").attr("value",course.getSection()+"");
        courseDefault.body().getElementById("add-homework-div").append("<h6 id = \"add-homework-label\"><i class = \"fa fa-plus\"></i> Add homework</h6>");


        ResultSet examInfo = DBFunctions.getCourseExams(course);
        buildTeachingExams(examInfo,courseDefault.body().getElementById("exam-display"),course);
        examInfo.close();
        courseDefault.body().getElementById("add-exam-course-id").attr("value",course.getId());
        courseDefault.body().getElementById("add-exam-section").attr("value",course.getSection()+"");
        courseDefault.body().getElementById("add-exam-div").append("<h6 id = \"add-exam-label\"><i class = \"fa fa-plus\"></i> Add exam</h6>");
        ResultSet postInfo = DBFunctions.getCoursePosts(course);
        buildPosts(postInfo,courseDefault, courseDefault.body().getElementById("forum-box"),course.getId());
        postInfo.close();

        return courseDefault.html();
    }

    private String buildEnrolledCoursePage(NittanyCourse course, NittanyStudent student) throws IOException, SQLException {
        Document courseDefault = Jsoup.parse(new File("src/main/webapp/courseDefault.html"), null);
        Element courseInfo = buildCourseHeader(courseDefault,course);
        courseInfo.append("<form action = \"drop\" method = \"POST\"><label for=\"password\">Password</label> <input type = \"password\" id = \"password\" name = \"password\">" +
                "<input type = \"hidden\" name = \"course-id\" value = \""+ course.getId() +"\">" +
                "<input type = \"submit\"  value = \"Drop Class\" class = \"w3-center\"></form><br>");
        ResultSet homeworkInfo = DBFunctions.getStudentHomeworkGrades(course, student);
        buildHomeworks(homeworkInfo, courseDefault.body().getElementById("homework-display"));
        homeworkInfo.close();
        ResultSet examInfo = DBFunctions.getStudentExamGrades(course, student);
        buildExams(examInfo, courseDefault.body().getElementById("exam-display"));
        ResultSet postInfo = DBFunctions.getCoursePosts(course);
        buildPosts(postInfo,courseDefault, courseDefault.body().getElementById("forum-box"),course.getId());
        postInfo.close();
        return courseDefault.html();
    }

    private void buildHomeworks(ResultSet homeworkInfo, Element container) throws IOException, SQLException {
        if(homeworkInfo.first()) {
            do {
                int homeworkNumber = homeworkInfo.getInt("hw_no");
                double grade = homeworkInfo.getDouble("grade");
                String details = homeworkInfo.getString("hw_details");
                container.append("<tr id = \"new-row\"></tr>");
                Element row = container.getElementById("new-row");
                row.append("<td>" + homeworkNumber + "</td>");
                row.append("<td>" + grade + "</td>");
                row.append("<td>" + details + "</td>");
                row.removeAttr("id");
            } while (homeworkInfo.next());
        }else{
            container.parent().append("<h4>No graded homework yet!</h4>");
            container.remove();
        }
    }
    private void buildTeachingHomeworks(ResultSet homeworkInfo, Element container,NittanyCourse course)throws SQLException{
        container.getElementById("grade-header").html("Grade (avg)");
        if(homeworkInfo.first()) {
            do {
                int homeworkNumber = homeworkInfo.getInt("hw_no");
                double gradeAvg = DBFunctions.getHWGradeAvg(course,homeworkNumber);
                gradeAvg = Math.round(gradeAvg*100)/100.00;
                String details = homeworkInfo.getString("hw_details");
                container.append("<tr id = \"new-row\"></tr>");
                Element row = container.getElementById("new-row");
                row.append("<td><a href = \"/homework?"+course.getId()+"&" +course.getSection() +"&"+ homeworkNumber+ "\" style = \"text-decoration: none\">" + homeworkNumber + "</a></td>");
                row.append("<td>" + gradeAvg + "</td>");
                row.append("<td>" + details + "</td>");
                row.removeAttr("id");
            } while (homeworkInfo.next());
        }else{
            container.parent().append("<h4>No homework yet!</h4>");
            container.remove();
        }


    }

    private void buildExams(ResultSet examInfo, Element container) throws SQLException {
        if (examInfo.first()) {
            do {
                int examNumber = examInfo.getInt("exam_no");
                double grade = examInfo.getDouble("grade");
                String details = examInfo.getString("exam_details");
                container.append("<tr id = \"new-row\"></tr>");
                Element row = container.getElementById("new-row");
                row.append("<td>" + examNumber + "</td>");
                row.append("<td>" + grade + "</td>");
                row.append("<td>" + details + "</td>");
                row.removeAttr("id");
            } while (examInfo.next());
        }else{
            container.parent().append("<h6>No graded exams yet!</h6>");
            container.remove();
        }

    }
    private void buildTeachingExams(ResultSet examInfo, Element container, NittanyCourse course) throws SQLException{
        container.getElementById("exam-grade-header").html("Grade (avg)");
        if (examInfo.first()) {
            do {
                int examNumber = examInfo.getInt("exam_no");
                double gradeAvg = DBFunctions.getExamGradeAvg(course,examNumber);
                gradeAvg = Math.round(gradeAvg*100)/100.0;
                String details = examInfo.getString("exam_details");
                container.append("<tr id = \"new-row\"></tr>");
                Element row = container.getElementById("new-row");
                row.append("<td><a href = \"/exam?"+course.getId()+"&" +course.getSection() +"&"+ examNumber+ "\" style = \"text-decoration: none\">" + examNumber + "</a></td>");
                row.append("<td>" + gradeAvg + "</td>");
                row.append("<td>" + details + "</td>");
                row.removeAttr("id");
            } while (examInfo.next());
        }else{
            container.parent().append("<h4>No graded exams yet!</h4>");
            container.remove();
        }
    }
    private void buildPosts(ResultSet postInfo,Document coursePage, Element container,String courseId) throws IOException, SQLException {
        if (postInfo.first()) {
            do {
                String postContent = postInfo.getString("post_info");
                String posterEmail = postInfo.getString("student_email");
                int postNum = postInfo.getInt("post_no");
                container.attr("id","post-box-"+postNum);
                Element defaultPost = Jsoup.parse(new File("src/main/webapp/postDefault.html"), null).body().getElementById("post-box").clone();
                defaultPost.getElementById("post-title").html("Post " + postNum + ": " + postContent);
                defaultPost.getElementById("poster").html(posterEmail + "@nittany.edu");
                defaultPost.appendTo(container.getElementById("post-display"));
                ResultSet commentInfo = DBFunctions.getPostComments(courseId,postNum);
                buildComments(commentInfo,defaultPost);
                defaultPost.getElementById("new-comment-display").attr("id","new-comment-display-"+postNum);
                defaultPost.getElementById("submit-post-no").attr("value",postNum+"");
                defaultPost.append("<h4 id = \"new-comment-label-"+postNum+"\"><i class = \"fa fa-reply\"></i> Reply to post</h4>");
                Element courseScript = coursePage.body().getElementById("courseScript");
                courseScript.html(courseScript.html() +"||newline" +
                        "document.getElementById(\"new-comment-label-"+postNum+"\").onclick=function(){||newline" +
                        "document.getElementById(\"new-comment-display-"+postNum+"\").removeAttribute(\"hidden\");||newline" +
                        "document.getElementById(\"new-comment-label-"+postNum+"\").setAttribute(\"hidden\",\"\");}||newline");
            } while (postInfo.next());
        } else {
            container.append("<h4 id= \"nothing-label\" class = \"w3-center\">Looks like there's nothing here!</h4>");
        }

    }
    private void buildComments(ResultSet commentInfo,Element container)throws IOException,SQLException{

        if(commentInfo.first()){
            Element comment = container.clone();
            do{
                String commentContent = commentInfo.getString("comment_info");
                String posterEmail = commentInfo.getString("student_email");
                int commentNum = commentInfo.getInt("comment_no");
                comment.getElementById("post-title").html("Comment " + commentNum + ": "+ commentContent);
                comment.getElementById("poster").html(posterEmail+"@nittany.psu.edu");
                comment.clone().appendTo(container);
            }while(commentInfo.next());
        }
    }
}
