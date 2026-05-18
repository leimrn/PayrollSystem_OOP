package timekeeping;

public class DailyRecord {
    private String employeeId;
    private String date;
    private String timeIn;
    private String timeOut;

    public DailyRecord(String employeeId, String date, String timeIn, String timeOut) {
        this.employeeId = employeeId;
        this.date = date;
        this.timeIn = timeIn;
        this.timeOut = timeOut;
    }

    public String getEmployeeId() { return employeeId; }
    public String getDate() { return date; }
    public String getTimeIn() { return timeIn; }
    public String getTimeOut() { return timeOut; }
}