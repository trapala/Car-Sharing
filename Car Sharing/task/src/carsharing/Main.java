package carsharing;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        // Get the database file name from command-line arguments or use a default name
        String databaseFileName = "carsharing";
        if (args.length > 0 && args[0].equals("-databaseFileName")) {
            databaseFileName = args[1];
        }

        CarSharingApp carSharingApp = new CarSharingApp();
        carSharingApp.run(databaseFileName);
    }
}

class CarSharingApp {
    private static final String DATABASE_FILE_PATH = "./src/carsharing/db/";
    private DatabaseManager databaseManager;
    private InputReader inputReader;
    private MenuPrinter menuPrinter;

    public void run(String databaseFileName) {
        try {
            // Construct the JDBC URL for connecting to the H2 database
            String jdbcUrl = "jdbc:h2:" + DATABASE_FILE_PATH + databaseFileName;

            // Connect to the database
            Connection connection = DriverManager.getConnection(jdbcUrl);

            // Enable auto-commit mode
            connection.setAutoCommit(true);

            // Create the COMPANY table if it doesn't exist
            databaseManager = new DatabaseManager(connection);
            databaseManager.createCompanyTable();

            // Initialize input reader and menu printer
            inputReader = new InputReader();
            menuPrinter = new MenuPrinter();

            // Start the main menu loop
            boolean exit = false;
            while (!exit) {
                menuPrinter.printMainMenu();

                int choice = inputReader.readIntInput();

                switch (choice) {
                    case 1 -> managerMenu();
                    case 0 -> exit = true;
                    default -> menuPrinter.printInvalidChoice();
                }
            }

            // Close the database connection
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void managerMenu() {
        boolean back = false;
        while (!back) {
            menuPrinter.printManagerMenu();

            int choice = inputReader.readIntInput();

            switch (choice) {
                case 1 -> showCompanyList();
                case 2 -> createCompany();
                case 0 -> back = true;
                default -> menuPrinter.printInvalidChoice();
            }
        }
    }

    private void showCompanyList() {
        List<Company> companies = databaseManager.getCompanyList();

        if (companies.isEmpty()) {
            System.out.println("The company list is empty!");
        } else {
            menuPrinter.printCompanyList(companies);
        }
    }

    private void createCompany() {
        String companyName = inputReader.readStringInput();

        if (databaseManager.insertCompany(companyName)) {
            System.out.println("The company was created!");
        } else {
            System.out.println("Failed to create the company.");
        }
    }
}

class Company {
    private int id;
    private String name;

    public Company(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

class DatabaseManager {
    private final Connection connection;

    public DatabaseManager(Connection connection) {
        this.connection = connection;
    }

    public void createCompanyTable() throws SQLException {
        try (Statement statement = connection.createStatement()) {
            String createTableSql = "CREATE TABLE IF NOT EXISTS COMPANY (ID INT PRIMARY KEY AUTO_INCREMENT, NAME VARCHAR NOT NULL UNIQUE)";
            statement.executeUpdate(createTableSql);
        }
    }

    public List<Company> getCompanyList() {
        List<Company> companies = new ArrayList<>();

        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT * FROM COMPANY ORDER BY ID")) {

            while (resultSet.next()) {
                int id = resultSet.getInt("ID");
                String name = resultSet.getString("NAME");
                Company company = new Company(id, name);
                companies.add(company);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return companies;
    }

    public boolean insertCompany(String companyName) {
        try (PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO COMPANY (NAME) VALUES (?)")) {
            preparedStatement.setString(1, companyName);
            int rowsAffected = preparedStatement.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }
}

class InputReader {
    public int readIntInput() {
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

    public String readStringInput() {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter the company name: ");
        return scanner.nextLine();
    }
}

class MenuPrinter {
    public void printMainMenu() {
        System.out.println("1. Log in as a manager");
        System.out.println("0. Exit");
    }

    public void printManagerMenu() {
        System.out.println("1. Company list");
        System.out.println("2. Create a company");
        System.out.println("0. Back");
    }

    public void printInvalidChoice() {
        System.out.println("Invalid choice. Please try again.");
    }

    public void printCompanyList(List<Company> companies) {
        System.out.println("Company list:");
        for (int i = 0; i < companies.size(); i++) {
            System.out.println((i + 1) + ". " + companies.get(i).getName());
        }
    }
}