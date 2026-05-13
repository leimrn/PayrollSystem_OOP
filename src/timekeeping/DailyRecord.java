package timekeeping;

public class DailyRecord { // An object to act as a helper to illustrate more use of OOP
    private String timeIn;
    private String timeOut;

    // The constructor for the private strings. All private variables need a constructor and a getter.
    public DailyRecord(String timeIn, String timeOut) {
        this.timeIn = timeIn;
        this.timeOut = timeOut;
    }

    // The getters
    public String getTimeIn() {
        return timeIn;
    }

    public String getTimeOut() {
        return timeOut;
    }
}
