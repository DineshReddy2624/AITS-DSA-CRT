package application;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
public class DBConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/project?useSSL=false&allowPublicKeyRetrieval=true";
    private static final String USER = "root";
    private static final String PASSWORD = "DineshK@2624"; // REPLACE WITH YOUR ACTUAL MYSQL PASSWORD

    /**
     * Establishes and returns a database connection.
     * @return A java.sql.Connection object, or null if the connection fails.
     */
    public static Connection getConnection() {
        Connection connection = null;
        try {
            // Loading the JDBC driver is generally not needed for modern JDBC 4.0+ compliant drivers,
            // as they are automatically registered. However, it's harmless to keep.
            // Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (SQLException e) {
            System.err.println("Database connection failed!");
            System.err.println("SQL State: " + e.getSQLState());
            System.err.println("Error Code: " + e.getErrorCode());
            System.err.println("Message: " + e.getMessage());
            // In a real application, you might log this error more formally
            // and show a user-friendly message through the UI.
        }
        return connection;
    }
}
