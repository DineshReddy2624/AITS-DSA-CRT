package application;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    // Database credentials for MySQL
    // Ensure your MySQL server is running and database 'project' exists.
    private static final String URL = "jdbc:mysql://localhost:3306/project?useSSL=false&allowPublicKeyRetrieval=true";
    private static final String USER = "root";
    private static final String PASSWORD = "DineshK@2624"; // REPLACE WITH YOUR ACTUAL MYSQL PASSWORD

    public static Connection getConnection() {
        Connection connection = null;
        try {
            // Load the MySQL JDBC driver (optional for newer JDBC versions but good practice)
            // Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (SQLException e) {
            System.err.println("Database connection failed!");
            System.err.println("SQL State: " + e.getSQLState());
            System.err.println("Error Code: " + e.getErrorCode());
            System.err.println("Message: " + e.getMessage());
            // Consider showing a more user-friendly error message in a real application
        }
        return connection;
    }
}
