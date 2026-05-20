package computation;

import timekeeping.Timekeeping;

public class GrossPayCalculator {
    private double grossPay;
    private double overtimepay;
    private double hourrate;

    public GrossPayCalculator(){
        this.grossPay = 0.0;
        this.overtimepay = 0.0;
        this.hourrate = 0.0;
    }

    public void calculategrosspay(String status, double baseRate, Timekeeping timekeeping){

        // check if employee is parttime (no work, no pay logic)
        if (status.equalsIgnoreCase("Part-time")) {
            this.hourrate = (baseRate / 22.0) /8.0;

            this.overtimepay = timekeeping.getTotalOvertime() * this.hourrate * 1.25;
            double weekendPay = timekeeping.getTotalWeekendHours() * this.hourrate * 1.30;
            double weekendOtPay = timekeeping.getTotalWeekendOvertime() * this.hourrate * 1.69;

            this.grossPay = (timekeeping.getTotalHours() * this.hourrate) + this.overtimepay + weekendPay + weekendOtPay;
        } else {
            // Uses the standard 22-day work month and 8-hour work day logic
            this.hourrate = (baseRate / 22.0) / 8.0;

            // Calculate standard OT
            this.overtimepay = timekeeping.getTotalOvertime() * this.hourrate * 1.25;

            // NEW: Calculate Weekend Pay (130%) and Weekend OT (169%)
            double weekendPay = timekeeping.getTotalWeekendHours() * this.hourrate * 1.30;
            double weekendOtPay = timekeeping.getTotalWeekendOvertime() * this.hourrate * 1.69;

            // Calculate semi-monthly cutoff (Monthly Base Rate / 2)
            double cutoffSalary = baseRate / 2.0;

            // Add all the premium pays to the base cutoff salary
            this.grossPay = cutoffSalary + this.overtimepay + weekendPay + weekendOtPay;
        }
    }

    public double getGrossPay(){ return grossPay; }
    public double getOvertimepay(){ return overtimepay; }
    public double getHourrate(){ return hourrate; }
}