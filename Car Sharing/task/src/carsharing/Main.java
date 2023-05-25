package carsharing;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class Main {
    public static void main(String[] args) {
        // Get the database file name from command-line arguments or use a default name
        String databaseFileName = "carsharing";
        if (args.length > 0 && args[0].equals("-databaseFileName")) {
            databaseFileName = args[1];
        }

        // Construct the JDBC URL for connecting to the H2 database
        String jdbcUrl = "jdbc:h2:./src/carsharing/db/" + databaseFileName;

        try (Connection connection = DriverManager.getConnection(jdbcUrl);
             Statement statement = connection.createStatement()) {

            // Enable auto-commit mode
            connection.setAutoCommit(true);

            // Create the COMPANY table
            String createTableSql = "CREATE TABLE COMPANY (ID INT, NAME VARCHAR)";
            statement.executeUpdate(createTableSql);

            System.out.println("COMPANY table created successfully!");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
