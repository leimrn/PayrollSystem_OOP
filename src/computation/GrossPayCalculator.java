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

    // UPDATED: Now accepts status and baseRate directly from the UI/Table
    public void calculategrosspay(String status, double baseRate, Timekeeping timekeeping){

        // check if employee is parttime (no work, no pay logic)
        if (status.equalsIgnoreCase("Part-time")) {
            this.hourrate = baseRate; // BaseRate is their hourly rate
            this.overtimepay = timekeeping.getTotalOvertime() * this.hourrate * 1.25;
            this.grossPay = (timekeeping.getTotalHours() * this.hourrate) + this.overtimepay;
        } else {
            // Uses the standard 22-day work month and 8-hour work day logic [cite: 1868]
            this.hourrate = (baseRate / 22.0) / 8.0;

            // Calculate OT using total overtime hours multiplied by hourly rate and 1.25 premium [cite: 1869]
            this.overtimepay = timekeeping.getTotalOvertime() * this.hourrate * 1.25;

            // Calculate semi-monthly cutoff (Monthly Base Rate / 2)
            double cutoffSalary = baseRate / 2.0;
            this.grossPay = cutoffSalary + this.overtimepay;
        }
    }

    public double getGrossPay(){
        return grossPay;
    }

    public double getOvertimepay(){
        return overtimepay;
    }

    public double getHourrate(){
        return hourrate;
    }
}