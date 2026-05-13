package timekeeping;

import java.time.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class Timekeeping {
    private double totalHours, totalOvertime, totalUndertime; // double as hours can have fractions
    private int totalAbsences, totalLeaves;                   //can be int as absences are counted in full days
    private ArrayList<DailyRecord> timesheet;

    // The constructor
    public Timekeeping() {
        this.timesheet = new ArrayList<>();

        // Set variables to 0 so ensure a clean slate
        this.totalHours = 0.0;
        this.totalOvertime = 0.0;
        this.totalUndertime = 0.0;
        this.totalAbsences = 0;
        this.totalLeaves = 0;
    }

    // Feeding the timesheet
    public void addDailyRecord(String timeIn, String timeOut) {
        DailyRecord record = new DailyRecord(timeIn, timeOut);
        timesheet.add(record);
    } // Whenever the main menu asks the user for their time in and out, the two strings go to this method
      // It stores the data in the DailyRecord helper object (an objectified Array)
      // and drops that into Arraylist

    // Getters
    public double getTotalHours() {
        return totalHours;
    }

    public double getTotalOvertime() {
        return totalOvertime;
    }

    public double getTotalUndertime() {
        return totalUndertime;
    }

    public int getTotalAbsences() {
        return totalAbsences;
    }

    public int getTotalLeaves() { return totalLeaves; }

    // Use a for each loop to look at every day in the timesheet in order to check for absences
    public void calculateHours() {
        for (DailyRecord record : timesheet) {

            // Is it a paid leave?
            if (record.getTimeIn().equalsIgnoreCase("Leave")) {
                totalLeaves++;
                continue; // Skip to next day
            }

            // Is it an unpaid absence?
            // Check absence syntax (if gettimein from DailyRecord contains nothing or "absent", then it is an absence
            if (record.getTimeIn().equalsIgnoreCase("Absent") || record.getTimeIn().isEmpty()) {
                totalAbsences++;
                continue; // this will tell java to skip the syntax below and move to the next day
            }

            // Time conversion syntax
            // Convert the strings to time objects
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("H:mm");
                LocalTime in = LocalTime.parse(record.getTimeIn(), formatter);
                LocalTime out = LocalTime.parse(record.getTimeOut(), formatter);

                // Calculate the total minutes, minus the 1-hour break
                long rawMinutes = Duration.between(in, out).toMinutes();

                // Failsafe for night shifts bringing the hours into negatives
                if (rawMinutes < 0) {
                    rawMinutes += 1440;
                }

                // Failsafe if i.e they input the same time in time out "In: 12:30" "Out: 12:30"
                if (rawMinutes == 0) {
                    totalUndertime += 8.0; // They missed their whole 8-hour shift for the day
                    continue;
                }

                long netMinutes = rawMinutes;
                if (rawMinutes > 240) {
                    netMinutes -= 60; // Here we subtract the 1-hour break
                }

                // Convert back to decimal hours
                double netHours = netMinutes / 60.0; // Divide with a .0 for the most accurate result

                // An If statement for if hours is exactly 8
                if (netHours == 8.0) {
                    totalHours += 8.0;
                }

                // If hours does not meet 8
                if (netHours < 8.0) {
                    totalHours += netHours;
                    totalUndertime += (8.0 - netHours);
                }

                // If hours is over 8
                if (netHours > 8.0) {
                    totalHours += 8.0;
                    totalOvertime += (netHours - 8.0);
                }
            } catch (Exception e) {
                // Safety net: if they type something unparseable like "24:22"
                totalUndertime += 8.0;
                continue; // This will tell java to skip the syntax below and move to the next day
            }
        }
    }
}
