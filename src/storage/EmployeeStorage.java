package storage;

import java.io.*;
import java.util.*;

public class EmployeeStorage {
    private final String FILE_PATH = "resources/employees.txt";

    public void saveEmployee(String lblID, String lblName, String lblStatus, String lblRate) {
        File file = new File(FILE_PATH);
        try {

            if (file.getParentFile() != null && !file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }


            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, true))) {
                // Stores data in the format: EMP-ID,Name,Status,Rate
                String record = String.format("%s,%s,%s,%s", lblID, lblName, lblStatus, lblRate);
                writer.write(record);
                writer.newLine();
                writer.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}