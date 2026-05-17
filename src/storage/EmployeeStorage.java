package storage;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class EmployeeStorage {
    private static final String FILE_PATH = "resources/employees.csv";

    public void saveEmployee(String id, String name, String status, String rate) {
        File file = new File(FILE_PATH);
        boolean isNewFile = !file.exists();

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, true))) {
            if (isNewFile) {
                writer.write("ID,Name,Status,Rate");
                writer.newLine();
            }

            name = sanitizeForCSV(name);
            status = sanitizeForCSV(status);

            String csvLine = String.format("%s,%s,%s,%s", id, name, status, rate);
            writer.write(csvLine);
            writer.newLine();

        } catch (IOException e) {
            System.err.println("Error saving employee to CSV: " + e.getMessage());
        }
    }

    public List<Object[]> loadEmployees() {
        List<Object[]> employeeList = new ArrayList<>();
        File file = new File(FILE_PATH);

        if (!file.exists()) {
            return employeeList;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            boolean isHeader = true;

            while ((line = reader.readLine()) != null) {
                if (isHeader) {
                    isHeader = false;
                    continue;
                }

                String[] data = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");

                for (int i = 0; i < data.length; i++) {
                    data[i] = data[i].replace("\"", "").trim();
                }

                employeeList.add(data);
            }
        } catch (IOException e) {
            System.err.println("Error reading CSV file: " + e.getMessage());
        }

        return employeeList;
    }

    /**
     * ADD THIS METHOD: Overwrites the file with the full list from the table.
     * This fixes the "cannot find symbol" error.
     */
    public void updateAllEmployees(List<Object[]> allEmployees) {
        // Passing 'false' to FileWriter tells it to overwrite the file
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH, false))) {

            // 1. Write headers
            writer.write("ID,Name,Status,Rate");
            writer.newLine();

            // 2. Loop through the list and write each row
            for (Object[] row : allEmployees) {
                String id = row[0].toString();
                String name = sanitizeForCSV(row[1].toString());
                String status = sanitizeForCSV(row[2].toString());
                String rate = row[3].toString();

                writer.write(String.format("%s,%s,%s,%s", id, name, status, rate));
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error updating CSV file: " + e.getMessage());
        }
    }

    private String sanitizeForCSV(String input) {
        if (input != null && input.contains(",")) {
            return "\"" + input + "\"";
        }
        return input;
    }
}