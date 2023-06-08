package carsharing;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class CarSharingApp {
    private static final String DATABASE_FILE_PATH = "./src/carsharing/db/";

    private Connection connection;

    public static void main(String[] args) {
        // Get the database file name from command-line arguments or use a default name
        String databaseFileName = "carsharing";
        if (args.length > 0 && args[0].equals("-databaseFileName")) {
            databaseFileName = args[1];
        }

        CarSharingApp app = new CarSharingApp();
        app.run(databaseFileName);
    }

    private void run(String databaseFileName) {
        try {
            // Construct the JDBC URL for connecting to the H2 database
            String jdbcUrl = "jdbc:h2:" + DATABASE_FILE_PATH + databaseFileName;

            // Connect to the database
            connection = DriverManager.getConnection(jdbcUrl);

            // Enable auto-commit mode
            connection.setAutoCommit(true);

            // Create the COMPANY table if it doesn't exist
            createCompanyTable();

            // Start the main menu loop
            boolean exit = false;
            while (!exit) {
                printMainMenu();

                int choice = readIntInput();

                switch (choice) {
                    case 1 -> managerMenu();
                    case 0 -> exit = true;
                    default -> System.out.println("Invalid choice. Please try again.");
                }
            }

            // Close the database connection
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void createCompanyTable() throws SQLException {
        try (Statement statement = connection.createStatement()) {
            String createTableSql = "CREATE TABLE IF NOT EXISTS COMPANY (ID INT PRIMARY KEY AUTO_INCREMENT, NAME VARCHAR NOT NULL UNIQUE)";
            statement.executeUpdate(createTableSql);
        }
    }

    private void managerMenu() {
        boolean back = false;
        while (!back) {
            printManagerMenu();

            int choice = readIntInput();

            switch (choice) {
                case 1 -> showCompanyList();
                case 2 -> createCompany();
                case 0 -> back = true;
                default -> System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    private void showCompanyList() {
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT NAME FROM COMPANY ORDER BY ID")) {

            List<String> companies = new ArrayList<>();
            while (resultSet.next()) {
                companies.add(resultSet.getString("NAME"));
            }

            if (companies.isEmpty()) {
                System.out.println("The company list is empty!");
            } else {
                System.out.println("Company list:");
                for (int i = 0; i < companies.size(); i++) {
                    System.out.println((i + 1) + ". " + companies.get(i));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void createCompany() {
        String companyName = readStringInput();

        try (PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO COMPANY (NAME) VALUES (?)")) {
            preparedStatement.setString(1, companyName);
            preparedStatement.executeUpdate();
            System.out.println("The company was created!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private int readIntInput() {
        Scanner scanner = new Scanner(System.in);
        int choice = -1;
        while (choice == -1) {
            try {
                System.out.print("Enter your choice: ");
                choice = scanner.nextInt();
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
                scanner.nextLine(); // Consume the invalid input
            }
        }
        return choice;
    }

    private String readStringInput() {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter the company name: ");
        return scanner.nextLine();
    }

    private void printMainMenu() {
        System.out.println("1. Log in as a manager");
        System.out.println("0. Exit");
    }

    private void printManagerMenu() {
        System.out.println("1. Company list");
        System.out.println("2. Create a company");
        System.out.println("0. Back");
    }
}