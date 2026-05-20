package storage;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class TimekeepingStorage {
    private static final String FILE_PATH = "resources/attendance.csv";

    // Call by the EmployeeView when they hit "Clock Out"
    public void saveRecord(String empId, String date, String timeIn, String timeOut) {
        File file = new File(FILE_PATH);
        boolean isNewFile = !file.exists();

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, true))) {
            if (isNewFile) {
                writer.write("EmployeeID,Date,TimeIn,TimeOut");
                writer.newLine();
            }
            // Writes: 2025-001,May 15,08:00 AM,05:00 PM
            writer.write(String.format("%s,%s,%s,%s", empId, date, timeIn, timeOut));
            writer.newLine();
        } catch (IOException e) {
            System.err.println("Error saving attendance: " + e.getMessage());
        }
    }

    // Call by AdminView and EmployeeView to calculate payroll
    public List<Object[]> loadRecordsForEmployee(String targetEmpId) {
        List<Object[]> employeeRecords = new ArrayList<>();
        File file = new File(FILE_PATH);

        if (!file.exists()) return employeeRecords;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            boolean isHeader = true;

            while ((line = reader.readLine()) != null) {
                if (isHeader) { isHeader = false; continue; }

                String[] data = line.split(",");

                if (data.length == 4 && data[0].equals(targetEmpId)) {
                    employeeRecords.add(new Object[]{data[0], data[1], data[2], data[3]});
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading attendance: " + e.getMessage());
        }
        return employeeRecords;
    }
}