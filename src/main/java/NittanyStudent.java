import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;

public class NittanyStudent extends NittanyUser {
    private String homeState, homeCity, homeZipcode,homeStreet,major;
    private NittanyCourse[] enrolledCourses;
    public NittanyStudent(){
        super();
        this.homeState = "";
        this.homeCity = "";
        this.homeZipcode = "";
        this.homeStreet = "";
        this.major = "";
        this.enrolledCourses = null;
    }
    public NittanyStudent(String name, String email, NittanyCourse[] taughtCourses,int age,boolean gender, String homeState,String homeCity, String homeZipcode, String homeStreet,String major, NittanyCourse[] enrolledCourses){
        super(name,email,taughtCourses,age, gender);
        this.homeState = homeState;
        this.homeCity = homeCity;
        this.homeZipcode = homeZipcode;
        this.homeStreet = homeStreet;
        this.major = major;
        this.enrolledCourses = enrolledCourses;
    }
    public NittanyStudent(ResultSet studentInfo) throws SQLException {
        studentInfo.first();
        this.name = studentInfo.getString("name");
        this.email = studentInfo.getString("email");
        this.gender = studentInfo.getBoolean("gender");
        this.age = studentInfo.getInt("age");
        this.homeState = studentInfo.getString("state");
        this.homeCity = studentInfo.getString("city");
        this.homeZipcode = studentInfo.getString("zipcode");
        this.homeStreet = studentInfo.getString("street");
        this.major = studentInfo.getString("major");
        this.enrolledCourses = getCourseInfo(email);
        this.taughtCourses = getTaughtCourseInfo(email);

    }
    public static NittanyCourse[] getCourseInfo(String email) throws SQLException{
        ResultSet courses = DBFunctions.getStudentCourses(email);
        return resultSetToCourseInfo(courses);
    }
    public static NittanyCourse[] getTaughtCourseInfo(String email) throws SQLException{
        ResultSet courses = DBFunctions.getStudentTaCourses(email);
        return resultSetToCourseInfo(courses);
    }
    public String getAddressHtml(){return "<h4>" +homeStreet+ "</h4><h4>" + homeCity + ", " + homeState + " " + homeZipcode + "<h4>";}
    public String getAddress(){return homeState + "\n" +homeCity + ", " + homeState + " " + homeZipcode;}
    public String getHomeState(){return homeState;}
    public String getHomeCity(){return homeCity;}
    public String getHomeZipcode(){return homeZipcode;}
    public String getMajor(){return major;}
    public NittanyCourse[] getEnrolledCourses(){return enrolledCourses;}
}
