import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;

public class NittanyUser {
    protected String name, email;
    protected NittanyCourse[] taughtCourses;
    protected int age;
    protected boolean gender;
    public NittanyUser(){
        this.name = "Mark Smith";
        this.email = "abc123";
        this.age = 18;
        this.gender = true;
    }
    public NittanyUser(String name, String email, NittanyCourse[] taughtCourses,int age,boolean gender){
        this.name = name;
        this.email = email;
        this.taughtCourses = taughtCourses;
        this.age = age;
        this.gender = gender;
    }
    protected static NittanyCourse[] resultSetToCourseInfo(ResultSet courses) throws SQLException{
        if(courses.first()){
            ArrayList<NittanyCourse> output = new ArrayList<>();
            do{
                String courseName = courses.getString("course_name");
                String courseId = courses.getString("course_id");
                String courseDescription = courses.getString("course_description");
                int sectionNumber = courses.getInt("sec_no");
                int sectionLimit = courses.getInt("limits");
                int teachingTeamID = courses.getInt("teaching_team_id");
                Date lateDropDeadline = courses.getDate("late_drop_deadline");
                output.add(new NittanyCourse(courseName,courseId,courseDescription,sectionNumber,sectionLimit,teachingTeamID,lateDropDeadline));
            }while(courses.next());
            return output.toArray(new NittanyCourse[0]);
        }
        return null;
    }
    @Override
    public boolean equals(Object o){
        NittanyUser user;
        try{
            user = (NittanyUser) o;
            return user.email.equals(email);
        }catch(ClassCastException cce){
            return false;
        }
    }
    public String getName(){return name;}
    public String getEmail(){return email;}
    public NittanyCourse[] getTaughtCourses(){return taughtCourses;}
    public int getAge(){return age;}
    public boolean getGender(){return gender;}
}
