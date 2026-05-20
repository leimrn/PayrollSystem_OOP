package storage;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class RequestStorage {
    //Location of the employees request
    private static final String FILE_PATH = "resources/requests.csv";

    // Called when an Employee submits a request
    public void saveRequest(String employeeName, String type, String days, String status) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH, true))) {
            File file = new File(FILE_PATH);
            if (file.length() == 0) {
                writer.write("Employee,Type,Days,Status");
                writer.newLine();
            }
            writer.write(String.format("%s,%s,%s,%s", employeeName, type, days, status));
            writer.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Called when the Admin opens the "Leave Requests" tab
    public List<Object[]> loadRequests() {
        List<Object[]> requests = new ArrayList<>();
        File file = new File(FILE_PATH);
        if (!file.exists()) return requests;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            boolean isHeader = true;
            while ((line = reader.readLine()) != null) {
                if (isHeader) { isHeader = false; continue; }
                requests.add(line.split(","));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return requests;
    }

    // Updates the CSV when the Admin approves/declines
    public void updateAllRequests(List<Object[]> allRequests) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH, false))) {
            writer.write("Employee,Type,Days,Status");
            writer.newLine();
            for (Object[] row : allRequests) {
                writer.write(String.format("%s,%s,%s,%s", row[0], row[1], row[2], row[3]));
                writer.newLine();
            }
        } catch (IOException e) { e.printStackTrace(); }
    }
}