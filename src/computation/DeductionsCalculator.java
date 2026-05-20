package computation;

import timekeeping.Timekeeping;

public class DeductionsCalculator {
    private double undertimeDeduction;
    private double absenceDeduction;
    private double sssContribution;
    private double philhealthContribution;
    private double pagibigContribution;
    private double withholdingTax;

    public DeductionsCalculator() {
        this.undertimeDeduction = 0.0;
        this.absenceDeduction = 0.0;
        this.sssContribution = 0.0;
        this.philhealthContribution = 0.0;
        this.pagibigContribution = 0.0;
        this.withholdingTax = 0.0;
    }

    // UPDATED: Now accepts status and rate directly instead of Employee object
    private void computeAttendanceDeductions(String status, Timekeeping timeData, double hourlyrate) {
        this.undertimeDeduction = timeData.getTotalUndertime() * hourlyrate;

        String type = status.toLowerCase();
        int daysToDeduct = timeData.getTotalAbsences();

        // Regular and Probationary employees typically have leave benefits [cite: 1540, 1587]
        boolean hasBenefits = type.equals("regular") || type.equals("probationary");

        if (!hasBenefits) {
            // Contractual/Part-time: deduct for both absences and leaves [cite: 1515, 1516]
            daysToDeduct += timeData.getTotalLeaves();
        }
        double calculatedDeduction = daysToDeduct * 8.0 * hourlyrate;
        this.absenceDeduction = daysToDeduct * 8.0 * hourlyrate;
    }


    private void computeGovernmentContributions(double baseSalary) {
        // Pag-IBIG: Capped at 10,000 base with 2% rate [cite: 1581]
        double pagibigBase = Math.min(baseSalary, 10000.0);
        this.pagibigContribution = pagibigBase * 0.02;

        // PhilHealth: 2.5% rate with 10k minimum and 100k cap [cite: 1580]
        double philhealthBase = baseSalary;
        if (philhealthBase < 10000.0) philhealthBase = 10000.0;
        else if (philhealthBase > 100000.0) philhealthBase = 100000.0;
        this.philhealthContribution = philhealthBase * 0.025;

        // SSS: 5% rate with 5k minimum and 35k cap [cite: 1579, 1580]
        double sssBase = baseSalary;
        if (sssBase < 5000.0) sssBase = 5000.0;
        else if (sssBase > 35000.0) sssBase = 35000.0;
        this.sssContribution = sssBase * 0.05;
    }

    private void computeTax(double grossPay) {
        double totalGovContributions = this.sssContribution + this.philhealthContribution + this.pagibigContribution;
        double taxableIncome = grossPay - totalGovContributions - this.absenceDeduction - this.undertimeDeduction;

        // Tax threshold: 20,833 monthly [cite: 1583]
        if (taxableIncome > 20833.0) {
            this.withholdingTax = (taxableIncome - 20833.0) * 0.20;
        } else {
            this.withholdingTax = 0.0;
        }
    }

    // UPDATED: Main entry method now uses String status and double baseRate
    public void calculateAllDeductions(String status, double baseRate, Timekeeping timeData, double grossPay) {
        double hourlyrate;
        double monthlyIncomeBase;

        // Standard hourly rate calculation for everyone
        hourlyrate = (baseRate / 22.0) / 8.0;

        if (status.equalsIgnoreCase("Part-time")) {
            monthlyIncomeBase = grossPay * 2;
            // REMOVED: this.absenceDeduction = 0.0;
            // We now call the attendance logic for everyone:
            computeAttendanceDeductions(status, timeData, hourlyrate);
        } else {
            monthlyIncomeBase = baseRate;
            computeAttendanceDeductions(status, timeData, hourlyrate);
        }

        computeGovernmentContributions(monthlyIncomeBase);
        computeTax(grossPay);
    }

    // Getters remain the same...
    public double getSssContribution() {
        return sssContribution;
    }

    public double getPhilhealthContribution() {
        return philhealthContribution;
    }

    public double getPagibigContribution() {
        return pagibigContribution;
    }

    public double getWithholdingTax() {
        return withholdingTax;
    }

    public double getAbsenceDeduction() {
        return absenceDeduction;
    }

    public double getUndertimeDeduction() {
        return undertimeDeduction;
    }
}