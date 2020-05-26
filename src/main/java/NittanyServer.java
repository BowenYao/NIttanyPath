import javax.servlet.RequestDispatcher;
import java.sql.*;
import java.util.LinkedHashMap;
import java.util.Map;

public class NittanyServer {
    private static final int cacheCapacity = 1000000;
    private static final LinkedHashMap<String,NittanyUser> sessionIDs = new LinkedHashMap<String,NittanyUser>(cacheCapacity){
        @Override
        protected boolean removeEldestEntry(Map.Entry eldest) {
            return size()>cacheCapacity;
        }
    };
    public static NittanyUser login(String email,String password) throws SQLException {
        Connection connection = DriverManager.getConnection(DBFunctions.dbURL);
        Statement studentSelect = connection.createStatement();
        studentSelect.execute("SELECT * FROM students,zipcodes WHERE email="+"\""+email+"\"AND password=" + password + " AND students.zipcode = zipcodes.zipcode");
        ResultSet studentResults = studentSelect.getResultSet();
        if(studentResults.first()){
            return new NittanyStudent(studentResults);
        }else{
            studentResults.close();
            studentSelect.close();
            Statement professorSelect = connection.createStatement();
            professorSelect.execute("SELECT * FROM professors WHERE email="+"\"" + email+"\"AND password="+password);
            ResultSet professorResults = professorSelect.getResultSet();
            if(professorResults.first()){
                return new NittanyProfessor(professorResults);
            }
        }
        return null;
    }
    public static NittanyUser authenticate(String sessionID){
        if(sessionIDs.containsKey(sessionID)){
            System.out.println("getting " + sessionIDs.get(sessionID).getName() + " out");
            return sessionIDs.get(sessionID);
        }
        return null;
    }
    public static void addUser(NittanyUser user,String sessionId){
        System.out.println("putting " + user.getName() + " in");
        sessionIDs.put(sessionId,user);
    }
    public static void updateSession(String sessionId,String password) throws SQLException{
        NittanyUser user = sessionIDs.get(sessionId);
        NittanyUser updatedUser = login(user.getEmail(),password);
        if(updatedUser!=null){
            sessionIDs.replace(sessionId,updatedUser);
        }
    }
    public static void removeSessionID(String sessionID){
        sessionIDs.remove(sessionID);
    }
}
