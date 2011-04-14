package edu.berkeley.cs.cs162;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;


public class DBHandler {
    private static Connection conn;
    static {
        conn = null;
        Properties connectionProps = new Properties();
        connectionProps.put("user", "group24");
        connectionProps.put("password", "dkhjjjprtd");
        try {
            conn = DriverManager.
                getConnection("jdbc:" + "mysql" + "://" + "ec2-50-17-180-71.compute-1.amazonaws.com" +
                              ":" + 3306 + "/" + "group24", connectionProps);
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
    }
    
    public static void addUser(String username, String salt, String hashedPassword) throws SQLException {
    	Statement stmt = conn.createStatement();
    	stmt.executeQuery("INSERT into  users (username, salt, encrypted_password) VALUES + (" + username + "," + salt +"," + hashedPassword + ")");
    	
    }
    
    public static String getSalt(String username) throws SQLException {
    	Statement stmt = conn.createStatement();
    	ResultSet rs = stmt.executeQuery("SELECT * FROM users where username = " + username);
    	rs.next();
    	String salt = rs.getString("salt");
    	return salt;
    	
    }
    
    public static void removeFromGroup(String uname, String gname) {
    	
    }
    
    public static String getHashedPassword(String uname) throws SQLException {
    	Statement stmt = conn.createStatement();
    	ResultSet rs = stmt.executeQuery("SELECT * FROM users where username = " + uname);
    	rs.next();
    	String hashedPassword = rs.getString("encrypted_password");
    	return hashedPassword;
    	
    }
}