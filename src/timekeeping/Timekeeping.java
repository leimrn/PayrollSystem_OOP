package timekeeping;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Locale;

public class Timekeeping {
    private double totalHours, totalOvertime, totalUndertime;
    private double totalWeekendHours, totalWeekendOvertime;
    private int totalAbsences, totalLeaves;
    private ArrayList<DailyRecord> timesheet;

    public Timekeeping() {
        this.timesheet = new ArrayList<>();
        this.totalHours = 0.0;
        this.totalOvertime = 0.0;
        this.totalUndertime = 0.0;
        this.totalWeekendHours = 0.0;
        this.totalWeekendOvertime = 0.0;
        this.totalAbsences = 0;
        this.totalLeaves = 0;
    }

    public void addDailyRecord(String empId, String date, String timeIn, String timeOut) {
        timesheet.add(new DailyRecord(empId, date, timeIn, timeOut));
    }

    public void setTotalAbsences(int absences) {
        this.totalAbsences = absences;
    }

    public void countAbsences(String currentMonth, int startDay, int endDay, int year) {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMMM d yyyy", Locale.ENGLISH);
        int missingCount = 0;

        for (int day = startDay; day <= endDay; day++) {
            String targetDate = currentMonth + " " + day; // e.g., "May 16"
            boolean foundInTimesheet = false;

            // Check if this specific day exists in your ArrayList
            for (DailyRecord record : timesheet) {
                if (record.getDate().equalsIgnoreCase(targetDate)) {
                    foundInTimesheet = true;
                    break;
                }
            }

            // Logic: If NOT found in CSV AND it's a weekday = Absence
            if (!foundInTimesheet) {
                try {
                    LocalDate dateObj = LocalDate.parse(targetDate + " " + year, dateFormatter);
                    DayOfWeek dayOfWeek = dateObj.getDayOfWeek();

                    // Only count if it's Monday to Friday
                    if (dayOfWeek != DayOfWeek.SATURDAY && dayOfWeek != DayOfWeek.SUNDAY) {
                        missingCount++;
                    }
                } catch (Exception e) {

                }
            }
        }
        this.totalAbsences = missingCount;
    }

    public double getTotalHours() { return totalHours; }
    public double getTotalOvertime() { return totalOvertime; }
    public double getTotalUndertime() { return totalUndertime; }
    public double getTotalWeekendHours() { return totalWeekendHours; }
    public double getTotalWeekendOvertime() { return totalWeekendOvertime; }
    public int getTotalAbsences() { return totalAbsences; }
    public int getTotalLeaves() { return totalLeaves; }

    public void calculateHours() {
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH);
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMMM d yyyy", Locale.ENGLISH);

        for (DailyRecord record : timesheet) {
            if (record.getTimeIn().equalsIgnoreCase("Leave")) {
                totalLeaves++;
                totalHours += 8.0;
                continue;
            }

            if (record.getTimeIn().equalsIgnoreCase("Absent") || record.getTimeIn().isEmpty()) {
                totalAbsences++;
                continue;
            }

            try {
                // 1. Time Parsing
                LocalTime in = LocalTime.parse(record.getTimeIn().toUpperCase(), timeFormatter);
                LocalTime out = LocalTime.parse(record.getTimeOut().toUpperCase(), timeFormatter);

                long rawMinutes = Duration.between(in, out).toMinutes();
                if (rawMinutes < 0) rawMinutes += 1440; // Night shift handling

                if (rawMinutes == 0) {
                    totalUndertime += 8.0;
                    continue;
                }

                // 2. Strict Break Logic (Gross Minutes to Net Minutes)
                long netMinutes = rawMinutes;
                if (rawMinutes > 480) {          // Over 8 hours
                    netMinutes -= 60;            // 1 Hour Break
                } else if (rawMinutes >= 240) {  // Between 4 and 8 hours
                    netMinutes -= 30;            // 30 Minute Break
                }

                double netHours = netMinutes / 60.0;

                // 3. Weekend Detection
                boolean isWeekend = false;
                try {
                    LocalDate date = LocalDate.parse(record.getDate() + " 2026", dateFormatter);
                    DayOfWeek day = date.getDayOfWeek();
                    if (day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY) {
                        isWeekend = true;
                    }
                } catch (Exception dateEx) {
                    // Fallback if UI sends a weird date
                }

                // 4. Distribution of Hours
                if (isWeekend) {
                    if (netHours <= 8.0) {
                        totalWeekendHours += netHours;
                    } else {
                        totalWeekendHours += 8.0;
                        totalWeekendOvertime += (netHours - 8.0);
                    }
                } else {
                    if (netHours < 8.0) {
                        totalHours += netHours;
                        totalUndertime += (8.0 - netHours);
                    } else {
                        totalHours += 8.0;
                        totalOvertime += (netHours - 8.0);
                    }
                }

            } catch (Exception e) {
                totalUndertime += 8.0; // Failsafe for unparseable time inputs
            }
        }
    }
}