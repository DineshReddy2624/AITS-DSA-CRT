package application;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Handles the database connection for the application.
 * Provides a static method to get a connection to the MySQL database.
 */
public class DBConnection {
    // JDBC URL for MySQL database.
    // 'project' is the database name.
    // 'useSSL=false' disables SSL encryption (for local development).
    // 'allowPublicKeyRetrieval=true' is needed for recent MySQL versions if using a simple password.
    private static final String URL = "jdbc:mysql://localhost:3306/project?useSSL=false&allowPublicKeyRetrieval=true";
    private static final String USER = "root";
    // IMPORTANT: Replace "DineshK@2624" with your actual MySQL root password.
    private static final String PASSWORD = "DineshK@2624"; // Use your actual MySQL root password here

    /**
     * Establishes and returns a database connection.
     *
     * @return A Connection object if successful, null otherwise.
     */
    public static Connection getConnection() {
        Connection connection = null;
        try {
            // Attempt to establish a connection to the database
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
            // System.out.println("Database connection established successfully!"); // For debugging
        } catch (SQLException e) {
            // Handle any SQL exceptions that occur during connection
            System.err.println("Database connection failed!");
            System.err.println("SQL State: " + e.getSQLState());
            System.err.println("Error Code: " + e.getErrorCode());
            System.err.println("Message: " + e.getMessage());
            // In a real application, you might want to throw a custom exception
            // or provide a more user-friendly error message.
        }
        return connection;
    }
}
