package edu.berkeley.cs.cs162;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
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
}