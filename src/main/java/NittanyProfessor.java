import java.sql.ResultSet;
import java.sql.SQLException;

public class NittanyProfessor extends NittanyUser {
    private String officeAddress, department, title;
    public NittanyProfessor(){
        super();
        this.officeAddress = "";
        this.department = "";
        this.title = "";
    }
    public NittanyProfessor(String name, String email, NittanyCourse[] taughtCourses,int age,boolean gender,String officeAddress, String department, String title){
        super(name,email,taughtCourses,age,gender);
        this.officeAddress = officeAddress;
        this.department = department;
        this.title = title;
    }
    public NittanyProfessor(ResultSet professorInfo) throws SQLException{
        professorInfo.first();
        this.name = professorInfo.getString("name");
        this.email = professorInfo.getString("email");
        this.taughtCourses = getTaughtCourseInfo(email);
        this.age = professorInfo.getInt("age");
        this.gender = professorInfo.getBoolean("gender");
        this.officeAddress = professorInfo.getString("office_address");
        this.department = professorInfo.getString("department");
        this.title = professorInfo.getString("title");
    }
    public static NittanyCourse[] getTaughtCourseInfo(String email) throws SQLException {
        ResultSet courses = DBFunctions.getProfessorCourses(email);
        return resultSetToCourseInfo(courses);
    }
    public String getOfficeAddress(){return officeAddress;}
    public String getDepartment(){return department;}
    public String getTitle(){return title;}
}
