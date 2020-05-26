import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;

public class addAssignmentServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        NittanyUser user = NittanyServer.authenticate(request.getSession().getId());
        String courseId = request.getParameter("course-id");
        String inputType = request.getParameter("input-type");
        int section = Integer.parseInt(request.getParameter("section"));
        try{if(inputType.equals("homework")){
            String details = request.getParameter("hw-details");
            DBFunctions.addHW(courseId,section,details);
        }else if (inputType.equals("exam")){
            String details = request.getParameter("exam-details");
            DBFunctions.addExam(courseId,section,details);
        }}catch(SQLException e){
            e.printStackTrace();
        }
        response.sendRedirect("/course?"+courseId);
    }
}
