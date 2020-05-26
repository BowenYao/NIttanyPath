import java.io.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;

public class DBFunctions {
    static final String dbURL = "jdbc:mysql://localhost/nittanypath?user=root&password="+Secret.databasePW; //used a separate secret class to protect my password
    static final File studentsTA = new File("src/main/Students_TA.csv"),
            professors = new File("src/main/Professors.csv"),
            postsComments=new File("src/main/Posts_Comments.csv");
    static final String[] dropOrder = {"comments","post","exam_grades","homeworks","ta_teaching_teams","prof_teaching_teams"};
    static class Table{
        String headings;
        ArrayList<String> rows;
        Table(String headings,ArrayList<String>rows){
            this.headings = headings;
            this.rows=rows;
        }
    }
    public static void main(String[] args) throws SQLException,IOException{
        cleanInstall();
    }
    static void updatePassword(NittanyUser user, String newPassword) throws SQLException{
        Connection connection = DriverManager.getConnection(dbURL);
        Statement statement = connection.createStatement();
        String email = user.getEmail();
        String table = "";
        if(user.getClass().isInstance(new NittanyStudent())){
            table = "students";
        }else if (user.getClass().isInstance(new NittanyProfessor())){
            table = "professors";
        }
        System.out.println("UPDATE "+ table + " SET password =\""+newPassword+"\" WHERE email = \""+email + "\"");
        statement.executeUpdate("UPDATE "+ table + " SET password =\""+newPassword+"\" WHERE email = \""+email + "\"");
        statement.close();
        connection.close();
    }
    static ResultSet getPostComments(String courseId,int postNum)throws SQLException{
        Connection connection = DriverManager.getConnection(dbURL);
        Statement statement = connection.createStatement();
        ResultSet results = statement.executeQuery("SELECT comment_info,student_email,comment_no FROM comments WHERE course_id = \""+courseId + "\" AND post_no =" + postNum);
        return results;
    }
    static ResultSet getCoursePosts(NittanyCourse course)throws SQLException{
        Connection connection = DriverManager.getConnection(dbURL);
        Statement statement = connection.createStatement();
        ResultSet results = statement.executeQuery("SELECT post_info,student_email,post_no FROM posts WHERE course_id = \""+course.getId()+"\"");
        return results;
    }
    static ResultSet getCourseHomework(NittanyCourse course) throws SQLException{
        Connection connection = DriverManager.getConnection(dbURL);
        Statement statement = connection.createStatement();
        ResultSet results = statement.executeQuery("SELECT * FROM homeworks WHERE course_id = \""+course.getId() + "\" AND sec_no = \"" + course.getSection() +"\"");
        return  results;
    }
    static ResultSet getCourseExams(NittanyCourse course)throws SQLException{
        Connection connection = DriverManager.getConnection(dbURL);
        Statement statement = connection.createStatement();
        ResultSet results = statement.executeQuery("SELECT * FROM exams WHERE course_id = \""+course.getId() + "\" AND sec_no = \"" + course.getSection() +"\"");
        return results;
    }
    static double getHWGradeAvg(NittanyCourse course,int hwNum) throws SQLException{
        Connection connection = DriverManager.getConnection(dbURL);
        Statement statement = connection.createStatement();
        ResultSet results = statement.executeQuery("SELECT AVG(grade) FROM homework_grades WHERE course_id = \"" + course.getId() + "\" AND sec_no = " + course.getSection() +" AND hw_no = " + hwNum);
        results.first();
        return results.getDouble(1);
    }
    static double getExamGradeAvg(NittanyCourse course,int examNum)throws  SQLException{
        Connection connection = DriverManager.getConnection(dbURL);
        Statement statement = connection.createStatement();
        ResultSet results = statement.executeQuery("SELECT AVG(grade) FROM exam_grades WHERE course_id = \"" + course.getId() + "\" AND sec_no = " + course.getSection() +" AND exam_no = " + examNum);
        results.first();
        return results.getDouble(1);
    }
    static ResultSet getStudentHomeworkGrades(NittanyCourse course,NittanyStudent student) throws SQLException{
        Connection connection = DriverManager.getConnection(dbURL);
        Statement statement = connection.createStatement();
        ResultSet results = statement.executeQuery("SELECT homeworks.hw_no,grade,hw_details FROM homeworks,homework_grades WHERE student_email = \""+ student.getEmail() + "\" AND " +
                "homeworks.course_id = homework_grades.course_id AND homeworks.sec_no = homework_grades.sec_no AND homeworks.hw_no = homework_grades.hw_no "+
                "AND homeworks.course_id = \"" + course.getId() + "\" AND homeworks.sec_no = "+ course.getSection());
        return results;
    }
    static ResultSet getHWStudentGrades(NittanyCourse course, int hwNum) throws SQLException{
        Connection connection = DriverManager.getConnection(dbURL);
        Statement statement = connection.createStatement();
        ResultSet results = statement.executeQuery("SELECT * from homework_grades WHERE course_id = \"" + course.getId() + "\" AND sec_no = "+ course.getSection() + " AND hw_no =" +hwNum);
        return results;
    }
    static ResultSet getExamStudentGrades(NittanyCourse course,int examNum) throws SQLException{
        Connection connection = DriverManager.getConnection(dbURL);
        Statement statement = connection.createStatement();
        ResultSet results = statement.executeQuery("SELECT * from exam_grades WHERE course_id = \"" + course.getId() + "\" AND sec_no = "+ course.getSection() + " AND exam_no =" +examNum);
        return results;
    }
    static ResultSet getStudentExamGrades(NittanyCourse course,NittanyStudent student)throws SQLException{
        Connection connection = DriverManager.getConnection(dbURL);
        Statement statement = connection.createStatement();
        ResultSet results = statement.executeQuery("SELECT exams.exam_no,grade,exam_details FROM exams,exam_grades WHERE student_email = \"" + student.getEmail() + "\" AND " +
                "exams.course_id = exam_grades.course_id AND exams.sec_no = exam_grades.sec_no AND exams.exam_no = exam_grades.exam_no AND" +
                " exams.course_id = \"" + course.getId() + "\" AND exams.sec_no = " + course.getSection());
        return results;
    }
    static ResultSet getCoursePageInfo(NittanyCourse course) throws SQLException{
        Connection connection = DriverManager.getConnection(dbURL);
        Statement statement = connection.createStatement();
        ResultSet results = statement.executeQuery("SELECT email,name,office_address FROM professors,prof_teaching_teams WHERE professors.email = prof_teaching_teams.prof_email AND teaching_team_id = \"" +course.getTeachingTeamId()+"\"");
        return results;
    }
    static ResultSet getStudentCourses(String studentEmail) throws SQLException{
        Connection connection = DriverManager.getConnection(dbURL);
        Statement statement = connection.createStatement();
        ResultSet results = statement.executeQuery("SELECT enrolls.course_id,enrolls.sec_no,course_name,course_description,late_drop_deadline,limits,sections.teaching_team_id FROM enrolls,courses,sections " +
                "WHERE student_email = \"" +studentEmail + "\" AND enrolls.course_id = courses.course_id AND enrolls.course_id = sections.course_id AND enrolls.sec_no = sections.sec_no");
        return results;
    }
    static ResultSet getStudentTaCourses(String studentEmail) throws SQLException{
        Connection connection = DriverManager.getConnection(dbURL);
        Statement statement = connection.createStatement();
        ResultSet results = statement.executeQuery("SELECT course_name, course_description, sections.course_id,sec_no,limits,late_drop_deadline,sections.teaching_team_id FROM ta_teaching_teams,sections,courses " +
                "WHERE student_email = \"" + studentEmail + "\" AND sections.teaching_team_id = ta_teaching_teams.teaching_team_id AND courses.course_id = sections.course_id");
        return results;
    }
    static ResultSet getProfessorCourses(String professorEmail) throws  SQLException{
        Connection connection = DriverManager.getConnection(dbURL);
        Statement statement = connection.createStatement();
        ResultSet results = statement.executeQuery("SELECT course_name, course_description, sections.course_id,sec_no,limits,late_drop_deadline,sections.teaching_team_id FROM prof_teaching_teams,sections,courses "+
        "WHERE prof_email = \"" + professorEmail + "\" AND sections.teaching_team_id = prof_teaching_teams.teaching_team_id AND courses.course_id = sections.course_id");
        return results;
    }
    static void makePost(String courseId,String postContent,String email) throws SQLException{
        Connection connection = DriverManager.getConnection(dbURL);
        Statement statement = connection.createStatement();
        ResultSet results = statement.executeQuery("SELECT * FROM posts WHERE course_id = \"" + courseId+"\"");
        int count = countResults(results);
        statement.executeUpdate("INSERT INTO posts(course_id,post_no,student_email,post_info) VALUES(\""+courseId+"\","+(count+1)+",\""+email+"\",\""+postContent+"\")");
        results.close();
        statement.close();
        connection.close();
    }
    static void makeComment(String courseId,String commentContent,int postNum,String email) throws SQLException{
        Connection connection = DriverManager.getConnection(dbURL);
        Statement statement=connection.createStatement();
        ResultSet results = statement.executeQuery("SELECT * FROM comments WHERE course_id = \"" + courseId + "\" AND post_no = " + postNum);
        int count = countResults(results);
        statement.executeUpdate("INSERT INTO comments(course_id,post_no,comment_no,student_email,comment_info) VALUES(\""+courseId +"\","+postNum+","+(count+1)+",\""+email+"\",\""+commentContent+"\")");
        results.close();
        statement.close();
        connection.close();
    }
    static void addHW(String courseId, int section,String details) throws  SQLException{
        Connection connection = DriverManager.getConnection(dbURL);
        Statement statement = connection.createStatement();
        ResultSet homeworks = statement.executeQuery("SELECT * FROM homeworks WHERE course_id = \"" + courseId+"\" AND sec_no = " + section);
        int count = countResults(homeworks);
        statement.execute("INSERT INTO homeworks(course_id,sec_no,hw_no,hw_details) VALUES (\""+courseId+"\","+section+","+(count+1)+",\""+details+"\")");
        ResultSet students = statement.executeQuery("SELECT student_email FROM enrolls WHERE course_id = \"" + courseId+"\" AND sec_no =" +section);
        if(students.first()){
            String insertSql = "INSERT INTO homework_grades(student_email,course_id,sec_no,hw_no,grade) VALUES(\""+students.getString("student_email")+"\",\""+courseId+"\","+section + ","+(count+1)+",NULL)";
            while(students.next()){
                insertSql+=",(\""+students.getString("student_email")+"\",\""+courseId+"\","+section + ","+(count+1)+",NULL)";
            }
            statement.execute(insertSql);
        }
    }
    static void addExam(String courseId, int section,String details) throws  SQLException{
        Connection connection = DriverManager.getConnection(dbURL);
        Statement statement = connection.createStatement();
        ResultSet exams = statement.executeQuery("SELECT * FROM exams WHERE course_id = \"" + courseId+"\" AND sec_no = " + section);
        int count = countResults(exams);
        statement.execute("INSERT INTO exams(course_id,sec_no,exam_no,exam_details) VALUES (\""+courseId+"\","+section+","+(count+1)+",\""+details+"\")");
        ResultSet students = statement.executeQuery("SELECT student_email FROM enrolls WHERE course_id = \"" + courseId+"\" AND sec_no =" +section);
        if(students.first()){
            String insertSql = "INSERT INTO exam_grades(student_email,course_id,sec_no,exam_no,grade) VALUES(\""+students.getString("student_email")+"\",\""+courseId+"\","+section + ","+(count+1)+",NULL)";
            while(students.next()){
                insertSql+=",(\""+students.getString("student_email")+"\",\""+courseId+"\","+section + ","+(count+1)+",NULL)";
            }
            statement.execute(insertSql);
        }
    }
    static void batchUpdateExams(Map<String,String[]> parameterMap,NittanyCourse course,int examNum) throws SQLException{
        Connection connection = DriverManager.getConnection(dbURL);
        Statement statement = connection.createStatement();
        for(String parameter:parameterMap.keySet()){
            String[] gradeStr = parameterMap.get(parameter);
            if(gradeStr!=null && !parameterMap.get(parameter)[0].isEmpty()) {
                double grade = Double.parseDouble(parameterMap.get(parameter)[0]);
                statement.executeUpdate("UPDATE exam_grades SET grade =" + grade + " WHERE course_id = \"" + course.getId() + "\" AND sec_no =" + course.getSection() + " AND exam_no = " + examNum + " AND student_email = \""+parameter+"\"");
            }
        }
        statement.close();
        connection.close();
    }
    static void batchUpdateHW(Map<String,String[]> parameterMap, NittanyCourse course, int hwNum)throws SQLException{
        Connection connection = DriverManager.getConnection(dbURL);
        Statement statement = connection.createStatement();
        for(String parameter:parameterMap.keySet()){
            String[] gradeStr = parameterMap.get(parameter);
            System.out.println(parameter + "|" + gradeStr[0]);
            if(gradeStr!=null && !gradeStr[0].isEmpty()) {
                double grade = Double.parseDouble(gradeStr[0]);
                System.out.println(grade);
                statement.executeUpdate("UPDATE homework_grades SET grade =" + grade + " WHERE course_id = \"" +course.getId() + "\" AND sec_no =" + course.getSection() + " AND hw_no = " + hwNum + " AND student_email = \""+parameter+"\"");
            }
        }
        statement.close();
        connection.close();
    }
    static void dropClass(NittanyStudent student, String courseId) throws SQLException{
        Connection connection = DriverManager.getConnection(dbURL);
        Statement statement = connection.createStatement();
        statement.execute("DELETE FROM enrolls WHERE course_id =\"" + courseId +"\" AND student_email = \""+student.getEmail()+"\"");
        statement.execute("DELETE FROM posts WHERE course_id = \"" + courseId + "\" AND student_email = \"" + student.getEmail()+"\"");
        statement.execute("DELETE FROM comments WHERE course_id = \"" + courseId + "\" AND student_email =\"" + student.getEmail()+"\"");
    }
    public static String[] customSplit(String string){
        //had to create a custom split to split database entries by commas ignoring those in strings
        ArrayList<String> strings = new ArrayList<>();
        boolean escape = false;
        String buildString = "";
        for(int x = 0; x < string.length();x++){
            char c = string.charAt(x);
            if(c == '\"')
                escape = !escape;
            else if(c==',' && !escape){
                buildString=buildString.trim();
                if(buildString.isEmpty())
                    buildString="NULL";
                strings.add(buildString);
                buildString="";
            }else
                buildString+=c;
        }
        if(buildString.isEmpty())
            buildString="NULL";
        strings.add(buildString);
        return strings.toArray(new String[0]);
    }
    public static int countResults(ResultSet resultSet) throws SQLException{
        int count = 0;
        if(resultSet.first()){
            do {
                count++;
            }while(resultSet.next());
        }
        return count;
    }
    public static void cleanInstall() throws SQLException{
        clearTables();
        createTables();
        parseStudentInfo();
        parseProfessorInfo();
        parsePostInfo();
    }

    public static void createTables()throws SQLException{
        Connection connect = DriverManager.getConnection(dbURL);
        //When deciding how long to make my string variables I looked around and found this https://stackoverflow.com/questions/20958/list-of-standard-lengths-for-database-fields
        // As such I decided on VARCHAR(255) with the exception of the emails which are all have exactly a 6 character prefix before the @
        //IMPORTANT NOTES: Homework and exam number stored as double for now even though it seems like they're all integers with .0s Consider using DECIMAL
        //Grades are also all doubles for now. Also considering DECIMAL depending on precision
        Statement statement = connect.createStatement();
        statement.execute("CREATE TABLE students(email CHAR(6),password VARCHAR(255),name VARCHAR(255),age INT,gender BOOLEAN,major VARCHAR(255)," +
                "street VARCHAR(255),zipcode INT,PRIMARY KEY(email))");
        statement.execute("CREATE TABLE zipcodes(zipcode INT,city VARCHAR(255),state VARCHAR(255),PRIMARY KEY(zipcode))");
        statement.execute("CREATE TABLE professors(email CHAR(6),password VARCHAR(255),name VARCHAR(255),age INT,gender BOOLEAN, office_address VARCHAR(255)," +
                "department VARCHAR(255),title VARCHAR(255), PRIMARY KEY(email))");
        statement.execute("CREATE TABLE departments(dept_id CHAR(5),dept_name VARCHAR(255),dept_head VARCHAR(255),PRIMARY KEY(dept_id))");
        statement.execute("CREATE TABLE courses(course_id CHAR(15),course_name VARCHAR(255),course_description VARCHAR(255),late_drop_deadline DATE,PRIMARY KEY(course_id))");
        statement.execute("CREATE TABLE sections(course_id CHAR(15),sec_no INT,limits INT,teaching_team_id DOUBLE,PRIMARY KEY(course_id,sec_no)," +
                "FOREIGN KEY (course_id) REFERENCES courses(course_id))");
        statement.execute("CREATE TABLE enrolls(student_email CHAR(6),course_id CHAR(15),sec_no INT,PRIMARY KEY(student_email,course_id)," +
                "FOREIGN KEY(student_email) REFERENCES students(email),FOREIGN KEY(course_id,sec_no) REFERENCES sections(course_id,sec_no));");
        statement.execute("CREATE TABLE prof_teaching_teams(prof_email CHAR(6),teaching_team_id DOUBLE,PRIMARY KEY(prof_email,teaching_team_id),FOREIGN KEY(prof_email) REFERENCES professors(email))");
        statement.execute("CREATE TABLE ta_teaching_teams(student_email CHAR(6),teaching_team_id DOUBLE,PRIMARY KEY(student_email,teaching_team_id)," +
                "FOREIGN KEY(student_email) REFERENCES students(email))");
        statement.execute("CREATE TABLE homeworks(course_id CHAR(15), sec_no INT, hw_no INT, hw_details VARCHAR(255),PRIMARY KEY(course_id,sec_no,hw_no)," +
                "FOREIGN KEY(course_id,sec_no) REFERENCES sections (course_id,sec_no))");
        statement.execute("CREATE TABLE homework_grades(student_email CHAR(8),course_id CHAR(15), sec_no INT, hw_no INT, grade DOUBLE, PRIMARY KEY(student_email,course_id,sec_no,hw_no), " +
                "FOREIGN KEY(course_id,sec_no, hw_no) REFERENCES homeworks(course_id,sec_no,hw_no), FOREIGN KEY(student_email,course_id) REFERENCES enrolls(student_email,course_id) ON DELETE CASCADE)");
        statement.execute("CREATE TABLE exams(course_id CHAR(15), sec_no INT,exam_no INT,exam_details VARCHAR(255), PRIMARY KEY(course_id,sec_no,exam_no)," +
                "FOREIGN KEY(course_id,sec_no) REFERENCES sections (course_id,sec_no))");
        statement.execute("CREATE TABLE exam_grades(student_email CHAR(8),course_id CHAR(15),sec_no INT,exam_no INT, grade DOUBLE,PRIMARY KEY(student_email,course_id,sec_no,exam_no)," +
                "FOREIGN KEY(course_id,sec_no,exam_no) REFERENCES exams(course_id,sec_no,exam_no), FOREIGN KEY(student_email,course_id) REFERENCES enrolls(student_email,course_id) ON DELETE CASCADE)");
        statement.execute("CREATE TABLE posts(course_id CHAR(15), post_no INT, student_email CHAR(6),post_info VARCHAR(255),PRIMARY KEY (course_id,post_no))");
        statement.execute("CREATE TABLE comments(course_id CHAR(15),post_no INT, comment_no INT, student_email CHAR(6),comment_info VARCHAR(255),PRIMARY KEY(course_id,post_no,comment_no)," +
                "FOREIGN KEY(course_id,post_no) REFERENCES posts(course_id,post_no) ON DELETE CASCADE)");
        statement.executeBatch();
        statement.close();
        connect.close();
    }

    public static void clearTables()throws SQLException{
        Connection connect = DriverManager.getConnection(dbURL);
        Statement statement = connect.createStatement();
        ResultSet tables = statement.executeQuery("show tables");
        tables.first();
        Statement dropTables = connect.createStatement();
        ArrayList<String> overflow = new ArrayList<>();
        do{
            try {
                dropTables.execute("DROP TABLE " + tables.getString(1));
            }catch(java.sql.SQLException sql){
                System.out.println(sql.getLocalizedMessage());
                if(sql.getLocalizedMessage().matches("Cannot drop table '\\w*' referenced by a foreign key constraint(.*)")){
                    overflow.add(tables.getString(1));
                    System.out.println("Overflow: " + tables.getString(1));
                }else
                    throw sql;
            }
        }while(tables.next());
        int count = 0;
        SQLException latest = null;
        while(!overflow.isEmpty() ||count >= 3){
            ArrayList<String> remove = new ArrayList<>();
            for(String table:overflow){
                boolean success = true;
                try{
                    dropTables.execute("DROP TABLE " + table);
                }catch(java.sql.SQLException sql) {
                    success = false;
                    latest = sql;
                }
                if(success)
                    remove.add(table);
            }
            overflow.removeAll(remove);
            if(!remove.isEmpty())
                count++;
            remove.clear();

        }
        if(!overflow.isEmpty())
            throw latest;
        tables.close();
        statement.close();
        dropTables.close();
        connect.close();
    }

    public static void parseStudentInfo() throws SQLException{
        //reads student information from csv and runs INSERT sql commands
        Table studentTable = readCSV(studentsTA);
        ArrayList<String> rows = studentTable.rows;
        String studentSQL = "INSERT INTO students(email,password,name,age,gender,major,street,zipcode) VALUES ",
        enrollSQL = "INSERT INTO enrolls(student_email,course_id,sec_no) VALUES ",
        zipcodeSQL = "INSERT INTO zipcodes(zipcode,city,state) VALUES ",
        courseSQL = "INSERT INTO courses(course_id,course_name, course_description) VALUES",
        sectionSQL = "INSERT INTO sections(course_id,sec_no,limits,teaching_team_id) VALUES ",
        teachingTeamSQL = "INSERT INTO ta_teaching_teams(student_email,teaching_team_id) VALUES ",
        homeworkSQL = "INSERT INTO homeworks(course_id,sec_no,hw_no,hw_details) VALUES ",
        homeworkGradeSQL = "INSERT INTO homework_grades(student_email,course_id,sec_no,hw_no,grade) VALUES",
        examSQL = "INSERT INTO exams(course_id,sec_no,exam_no,exam_details) VALUES ",
        examGradeSQL = "INSERT INTO exam_grades(student_email,course_id,sec_no,exam_no,grade) VALUES";
        HashSet<String> uniqueZipcodes = new HashSet<>(),uniqueCourses = new HashSet<>(), uniqueSections = new HashSet<>(),uniqueHomework = new HashSet<>(), uniqueExams = new HashSet<>();
        for(String row:rows){
            String[] tokens =customSplit(row);
            if(tokens.length!=45) {
                System.out.println("Something's wrong: " + tokens.length + "|" + row);
                return;
            }
            String name = tokens[0].trim(); String email = tokens[1].trim().substring(0,tokens[1].trim().indexOf('@')); String age = tokens[2].trim();
            String zipcode = tokens[3].trim(); String phone = tokens[4].trim(); String gender = tokens[5].trim();
            String city = tokens[6].trim(); String state = tokens[7].trim(); String password = tokens[8].trim();
            String street = tokens[9].trim(); String major = tokens[10].trim(); String course1 = tokens[11].trim();
            String course1Name = tokens[12].trim(); String course1Details = tokens[13].trim(); String course1Section = tokens[14].trim();
            String course1SectionLimit = tokens[15].trim(); String course1HW_no = tokens[16].trim(); String course1HW_details = tokens[17].trim();
            String course1HW_grade = tokens[18].trim(); String course1Exam_no = tokens[19].trim(); String course1ExamDetails = tokens[20].trim();
            String course1ExamGrade = tokens[21].trim(); String course2 = tokens[22].trim(); String course2Name = tokens[23].trim();
            String course2Details = tokens[24].trim(); String course2Section = tokens[25].trim(); String course2SectionLimit = tokens[26].trim();
            String course2HW_no = tokens[27].trim(); String course2HW_details = tokens[28].trim(); String course2HW_grade =tokens[29].trim();
            String course2Exam_no = tokens[30].trim(); String course2ExamDetails = tokens[31].trim(); String course2ExamGrade = tokens[32].trim();
            String course3 = tokens[33].trim(); String course3Name = tokens[34].trim(); String course3Details = tokens[35].trim();
            String course3Section = tokens[36].trim(); String course3SectionLimit = tokens[37].trim(); String course3HW_no = tokens[38].trim();
            String course3HW_details = tokens[39].trim(); String course3HW_grade = tokens[40].trim(); String course3Exam_no = tokens[41].trim();
            String course3ExamDetails = tokens[42].trim(); String course3ExamGrade = tokens[43].trim(); String teaching_team_id = tokens[44].trim();
            //building sql for students
            if(gender.equals("M"))
                gender = "true";
            else if(gender.equals("F"))
                gender = "FALSE";
            studentSQL+="(\""+ email+"\",\""+password.hashCode()+"\",\"" + name +"\"," + age +"," + gender + ",\"" + major+"\",\""+street+"\","+zipcode+"),";
            //building sql for enrolls
            if(!course1.equals("NULL"))
                enrollSQL+="(\""+email+"\",\""+course1+"\",\"" +course1Section+"\"),";
            if(!course2.equals("NULL"))
                enrollSQL+= "(\""+email+"\",\""+course2+"\",\""+course2Section+"\"),";
            if(!course3.equals("NULL"))
                enrollSQL+="(\""+email+"\",\""+course3+"\",\""+course3Section+"\"),";
            //adding unique zipcodes to set
            uniqueZipcodes.add("("+ zipcode+",\""+city+"\",\""+state+"\")");
            //adding unique courses to set
            if(!course1.equals("NULL"))
                uniqueCourses.add("(\""+course1+"\",\"" +course1Name +  "\",\""+ course1Details+"\")");
            if(!course2.equals("NULL"))
                uniqueCourses.add("(\""+course2+"\",\""+course2Name +  "\",\""+ course2Details+"\")");
            if(!course3.equals("NULL"))
                uniqueCourses.add("(\""+ course3+"\",\""+course3Name +  "\",\""+ course3Details+"\")");
            //adding unique sections to set
            if(!course1.equals("NULL"))
                uniqueSections.add("\""+course1+"\"," + course1Section+","+course1SectionLimit+"");
            if(!course2.equals("NULL"))
                uniqueSections.add("\""+course2+"\","+ course2Section+","+course2SectionLimit);
            if(!course3.equals("NULL"))
                uniqueSections.add("\""+ course3+"\","+ course3Section +","+course3SectionLimit);
            //building sql for ta_teaching_teams
            if(!teaching_team_id.equals("NULL"))
                teachingTeamSQL+="(\""+email+"\","+teaching_team_id+"),";
            //adding unique homeworks to set
            if(!course1HW_no.equals("NULL")) {
                uniqueHomework.add("(\"" + course1 + "\"," + course1Section + "," + course1HW_no + ",\"" + course1HW_details + "\")");
                homeworkGradeSQL+="(\""+email + "\",\""+course1+"\"," + course1Section +"," + course1HW_no+"," + course1HW_grade+"),";
            }
            if(!course2HW_no.equals("NULL")) {
                uniqueHomework.add("(\"" + course2 + "\"," + course2Section + "," + course2HW_no + ",\"" + course2HW_details + "\")");
                homeworkGradeSQL+="(\""+email + "\",\""+course2+"\"," + course2Section +"," + course2HW_no+"," + course2HW_grade+"),";
            }
            if(!course3HW_no.equals("NULL")) {
                uniqueHomework.add("(\"" + course3 + "\"," + course3Section + "," + course3HW_no + ",\"" + course3HW_details + "\")");
                homeworkGradeSQL+="(\""+email + "\",\""+course3+"\"," + course3Section +"," + course3HW_no+"," + course3HW_grade+"),";
            }
            //building sql for exams
            if(!course1.equals("NULL") && !course1Exam_no.equals("NULL")) {
                uniqueExams.add("(\""+course1 +"\"," + course1Section + "," + course1Exam_no + ",\"" + course1ExamDetails + "\")");
                examGradeSQL += "(\"" + email + "\",\"" + course1 + "\"," + course1Section + "," + course1Exam_no + "," + course1ExamGrade + "),";
            }
            if(!course2.equals("NULL") && !course2Exam_no.equals("NULL")) {
                uniqueExams.add("(\""+course2 +"\"," + course2Section + "," + course2Exam_no + ",\"" + course2ExamDetails + "\")");
                examGradeSQL += "(\"" + email + "\",\"" + course2 + "\"," + course2Section + "," + course2Exam_no + "," + course2ExamGrade + "),";
            }
            if(!course3.equals("NULL") && !course3Exam_no.equals("NULL")) {
                uniqueExams.add("(\""+course3 +"\"," + course3Section + "," + course3Exam_no + ",\"" + course3ExamDetails + "\")");
                examGradeSQL += "(\"" + email + "\",\"" + course3 + "\"," + course3Section + "," + course3Exam_no + "," + course3ExamGrade + "),";
            }
        }
        //building sql for zipcodes
        for(String zipcode:uniqueZipcodes){
            zipcodeSQL+=zipcode+",";
        }
        //building sql for courses
        for(String course:uniqueCourses){
            courseSQL += course + ",";
        }
        //building sql for sections
        for(String section:uniqueSections){
            sectionSQL+="(" + section+",NULL),"; //Easier to leave teaching team blank for now and update later when parsing teachers since data is unclear
        }
        //building sql for homeworks
        for(String homework:uniqueHomework){
            homeworkSQL+=homework+",";
        }
        for(String exam:uniqueExams){
            examSQL+=exam+",";
        }
        //trimming last ','
        studentSQL= studentSQL.substring(0,studentSQL.length()-1);
        enrollSQL = enrollSQL.substring(0,enrollSQL.length()-1);
        teachingTeamSQL = teachingTeamSQL.substring(0,teachingTeamSQL.length()-1);
        examSQL = examSQL.substring(0,examSQL.length()-1);
        examGradeSQL = examGradeSQL.substring(0,examGradeSQL.length()-1);
        zipcodeSQL = zipcodeSQL.substring(0,zipcodeSQL.length()-1);
        courseSQL = courseSQL.substring(0,courseSQL.length()-1);
        sectionSQL = sectionSQL.substring(0,sectionSQL.length()-1);
        homeworkSQL = homeworkSQL.substring(0,homeworkSQL.length()-1);
        homeworkGradeSQL= homeworkGradeSQL.substring(0,homeworkGradeSQL.length()-1);
        //Insert all values
        Connection connection = DriverManager.getConnection(dbURL);
        Statement insertStudents = connection.createStatement();
        insertStudents.execute(studentSQL);
        insertStudents.close();

        Statement insertZipcodes = connection.createStatement();
        insertZipcodes.execute(zipcodeSQL);
        insertZipcodes.close();

        System.out.println(courseSQL);
        Statement insertCourses = connection.createStatement();
        insertCourses.execute(courseSQL);
        insertCourses.close();

        Statement insertSections = connection.createStatement();
        insertSections.execute(sectionSQL);
        insertSections.close();

        Statement insertEnrolls = connection.createStatement();
        insertEnrolls.execute(enrollSQL);
        insertEnrolls.close();

        Statement insertTATeachingTeams = connection.createStatement();
        insertTATeachingTeams.execute(teachingTeamSQL);
        insertTATeachingTeams.close();

        Statement insertHomeworks =connection.createStatement();
        insertHomeworks.execute(homeworkSQL);
        insertHomeworks.close();

        Statement insertHomeworkGrades = connection.createStatement();
        insertHomeworkGrades.execute(homeworkGradeSQL);
        insertHomeworkGrades.close();

        Statement insertExams = connection.createStatement();
        insertExams.execute(examSQL);
        insertExams.close();

        Statement insertExamGrades = connection.createStatement();
        insertExamGrades.execute(examGradeSQL);
        insertExamGrades.close();
        connection.close();
    }

    public static void parseProfessorInfo()throws SQLException{
        //reads professor information and executes corresponding INSERT and UPDATE commands
        Table professorTable = readCSV(professors);
        ArrayList<String> rows = professorTable.rows;
        String professorSQL="INSERT INTO professors(email ,password ,name ,age,office_address,department,title) VALUES ";
        String departmentSQL="INSERT INTO departments(dept_id,dept_name,dept_head) VALUES";
        String teachingTeamSQL="INSERT INTO prof_teaching_teams(prof_email,teaching_team_id) VALUES";
        ArrayList<String> sectionUpdateSQLs = new ArrayList<>();
        for(String row:rows){
            String[] tokens = customSplit(row);
            String name = "\""+ tokens[0].trim() + "\"";
            String email = "\""+ tokens[1].trim().substring(0,tokens[1].trim().indexOf('@'))+"\"";
            String password = "\"" + tokens[2].trim().hashCode() + "\"";
            String age = tokens[3].trim();
            String gender = tokens[4].trim();
            String department = "\""+tokens[5].trim()+"\"";
            String office = "\"" +tokens[6].trim()+ "\"";
            String departmentName = "\"" + tokens[7].trim() + "\"";
            String title = "\"" + tokens[8].trim()+ "\"";
            String teaching_team_id = tokens[9].trim();
            String course = "\""+ tokens[10].trim() +"\"";
            String sectionUpdateSQL = "UPDATE sections SET teaching_team_id=";
            //building sql for professors
            professorSQL+="("+email+","+password+","+name+","+age+","+office+","+department+","+title+"),";
            //building sql for departments assuming there is exactly one head per department
            if(title.equals("\"Head\""))
                departmentSQL+="("+department+","+departmentName+","+name+"),";
            //building sql for prof_teaching_teams
            teachingTeamSQL+="("+email+","+teaching_team_id+"),";

            //building sql to update sections assuming each prof teaches one class
            sectionUpdateSQL += teaching_team_id+" WHERE course_id=" + course;
            sectionUpdateSQLs.add(sectionUpdateSQL);
        }
        professorSQL=professorSQL.substring(0,professorSQL.length()-1);
        departmentSQL = departmentSQL.substring(0,departmentSQL.length()-1);
        teachingTeamSQL = teachingTeamSQL.substring(0,teachingTeamSQL.length()-1);
        Connection connection = DriverManager.getConnection(dbURL);

        Statement professorStatement = connection.createStatement();
        professorStatement.execute(professorSQL);
        professorStatement.close();

        Statement departmentStatement = connection.createStatement();
        departmentStatement.execute(departmentSQL);
        departmentStatement.close();

        Statement teachingTeamStatement = connection.createStatement();
        teachingTeamStatement.execute(teachingTeamSQL);
        teachingTeamStatement.close();

        Statement sectionUpdateStatement = connection.createStatement();
        for(String updateCommand:sectionUpdateSQLs){
            sectionUpdateStatement.execute(updateCommand);
        }
        sectionUpdateStatement.executeBatch();
        sectionUpdateStatement.close();
        connection.close();
    }
    public static void parsePostInfo()throws SQLException{
        //reads postComment info and executes corresponding INSERT and UPDATE commands
        Table postTable = readCSV(postsComments);
        String postSQL = "INSERT INTO posts(course_id,post_no,student_email,post_info) VALUES ";
        String commentSQL = "INSERT INTO comments(course_id,post_no,comment_no,student_email,comment_info) VALUES";
        ArrayList<String> batchUpdates = new ArrayList<>();
        for(String row:postTable.rows){
            String[]tokens = customSplit(row);
            String course = "\""+ tokens[0].trim() + "\"";
            String drop_deadline = "\"" + tokens[1].trim() + "\"";
            String post = "\""+tokens[2].trim()+"\"";
            String postEmail = tokens[3].trim();
            String comment = "\"" +tokens[4].trim() + "\"";
            String commentEmail = tokens[5].trim();
            String updateSQL = "UPDATE courses SET late_drop_deadline=";
            //building sql for posts
            System.out.println(post);
            if(!post.equals("\"NULL\"")){
                postEmail= "\""+postEmail.substring(0,postEmail.indexOf('@'))+"\"";
                postSQL+="(" + course + ",1," +postEmail+","+post+ "),";
            }
            if(!comment.equals("\"NULL\"")){
                commentEmail="\""+commentEmail.substring(0,commentEmail.indexOf('@'))+"\"";
                commentSQL += "("+ course + ",1,1," + commentEmail+"," +comment+"),";
            }

            //building sql for comments

            //building sql to update courses

            updateSQL+="STR_TO_DATE(" + drop_deadline+ ",\"%m/%d/%y\") WHERE course_id=" + course;
            batchUpdates.add(updateSQL);
        }
        postSQL=postSQL.substring(0,postSQL.length()-1).replaceAll("\"NULL\"","NULL");
        commentSQL=commentSQL.substring(0,commentSQL.length()-1).replaceAll("\"NULL\"","NULL");
        Connection connection = DriverManager.getConnection(dbURL);
        Statement postStatement = connection.createStatement();
        postStatement.execute(postSQL);
        postStatement.close();

        Statement commentStatement = connection.createStatement();
        commentStatement.execute(commentSQL);
        commentStatement.close();

        Statement batchStatement = connection.createStatement();
        for(String update: batchUpdates){
            batchStatement.execute(update);
        }
        batchStatement.close();
        connection.close();
    }

    public static Table readCSV(File file){
        //reads CSV file and breaks it up into the headings string and the remaining rows as an arraylist
        BufferedReader reader =null;
        String headings ="";
        ArrayList<String> rows = new ArrayList<>();
        try {
            reader = new BufferedReader(new FileReader(file));
            headings = reader.readLine();
            String row = "";
            while((row = reader.readLine())!=null){
                rows.add(row);
            }
            reader.close();
        }catch(FileNotFoundException fnf){
            fnf.printStackTrace();
            return null;
        }catch(IOException io){
            io.printStackTrace();
            return null;
        }
        Table table = new Table(headings, rows);
        return table;
    }

}
