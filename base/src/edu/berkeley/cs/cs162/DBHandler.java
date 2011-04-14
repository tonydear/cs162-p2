package edu.berkeley.cs.cs162;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.util.ArrayList;
import java.util.List;
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
    
    public static void addToGroup(String uname, String gname) throws SQLException{
    	PreparedStatement pstmt = null;
    	try {
    		pstmt = conn.prepareStatement("INSERT INTO memberships (gname,username)" +
    		" VALUES (?,?)");
    		pstmt.setString(1, gname);
    		pstmt.setString(2, uname);
    		pstmt.executeUpdate();
    	} 
    	finally {
    		if(pstmt!=null) pstmt.close();
    	}
    }
    
    public static List<Message> readLog(String uname) throws SQLException{
    	List<Message> messages = new ArrayList<Message>();
    	PreparedStatement pstmt = conn.prepareStatement("SELECT sender, sqn, timestamp, destination, message" +
    	"FROM messages WHERE recipient = ?");
    	if(pstmt==null) return null;
    	ResultSet rs = pstmt.executeQuery();
    	while(rs.next()){
    		String sender = rs.getString("sender");
    		int sqn = rs.getInt("sqn");
    		Long timestamp = rs.getTime("timestamp").getTime();
    		String destination = rs.getString("destination");
    		String content = rs.getString("message");
    		Message msg = new Message(timestamp.toString(),sender,destination,content);
    		msg.setSQN(sqn);
    		messages.add(msg);
    	}
    	
    	return messages;
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