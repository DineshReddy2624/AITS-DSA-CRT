// DBConnection.java
package application;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    // IMPORTANT: Replace with your actual MySQL database URL, username, and password.
    // Ensure the 'project' database exists in your MySQL server.
    private static final String URL = "jdbc:mysql://localhost:3306/project?useSSL=false&allowPublicKeyRetrieval=true";
    private static final String USER = "root";
    private static final String PASSWORD = "DineshK@2624"; // REPLACE WITH YOUR ACTUAL MYSQL PASSWORD. This is also the admin password for "Dinesh Reddy".

    /**
     * Establishes and returns a database connection.
     * This method attempts to connect to the MySQL database using the predefined URL, user, and password.
     * If the connection fails, it prints detailed error information to the standard error stream.
     *
     * @return A java.sql.Connection object if the connection is successful, or null if the connection fails due to an SQLException.
     */
    public static Connection getConnection() {
        Connection connection = null;
        try {
            // Registering the JDBC driver explicitly is generally not needed for modern JDBC 4.0+ compliant drivers,
            // as they are automatically registered when they are found on the classpath.
            // However, keeping it does no harm and can prevent issues with older configurations.
            // Class.forName("com.mysql.cj.jdbc.Driver"); // Uncomment if you face issues with driver auto-registration

            // Attempt to establish a connection to the database
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
            // System.out.println("Database connection established successfully."); // For debugging
        } catch (SQLException e) {
            // Log detailed SQL exception information if the connection fails
            System.err.println("Database connection failed!");
            System.err.println("SQL State: " + e.getSQLState()); // SQLState provides a standard error code
            System.err.println("Error Code: " + e.getErrorCode()); // Vendor-specific error code
            System.err.println("Message: " + e.getMessage()); // Detailed error message
            // In a production application, you would use a proper logging framework (e.g., Log4j, SLF4J)
            // and potentially throw a custom exception or handle the UI to inform the user.
        }
        return connection;
    }
}
