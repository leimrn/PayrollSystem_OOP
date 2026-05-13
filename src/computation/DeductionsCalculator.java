package computation;
import model.Employee;
import timekeeping.Timekeeping;

public class DeductionsCalculator {

    // declare variables in private to hold the money deductions
    private double undertimeDeduction;
    private double absenceDeduction;
    private double sssContribution;
    private double philhealthContribution;
    private double pagibigContribution;
    private double withholdingTax;

    // Constructor
    public DeductionsCalculator() {
        // like with timekeeping. Ensure a clean slate by initializing to 0 at first
        this.undertimeDeduction = 0.0;
        this.absenceDeduction = 0.0;
        this.sssContribution = 0.0;
        this.philhealthContribution = 0.0;
        this.pagibigContribution = 0.0;
        this.withholdingTax = 0.0;
    }

    // Getters
    public double getUndertimeDeduction() {
        return undertimeDeduction;
    }

    public double getSssContribution() {
        return sssContribution;
    }

    public double getAbsenceDeduction() {
        return absenceDeduction;
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

    private void computeAttendanceDeductions(Employee employee, Timekeeping timeData, double hourlyrate) {
        // Undertime or late deduction that applies to everyone
        // Gets total undertime from timekeeping class and multiplies it that by the hourly rate and saves it to
        // undertimeDeduction variable.
        this.undertimeDeduction = timeData.getTotalUndertime() * hourlyrate;

        // Absence deduction
        // Grab the emptype string from Employee.java and convert to lowercase to play safe
        String type = employee.getEmptype().toLowerCase();

        // Everyone gets deducted for raw, unpaid "Absences" (including the missing days!)
        int daysToDeduct = timeData.getTotalAbsences();

        // If they are regular or probationary, they get benefits
        boolean hasBenefits = type.equals("regular") || type.equals("probationary");

        if (!hasBenefits) {
            // No benefits (Contractual/Part-time) = deduct for absences AND reject their "Leaves"
            daysToDeduct += timeData.getTotalLeaves();
        }
        // If they DO have benefits, we forgive the "Leave" and only deduct the raw unexcused Absences!

        // Final deduction math
        this.absenceDeduction = daysToDeduct * 8.0 * hourlyrate;
    }

    private void computeGovernmentContributions(double baseSalary) {
        // We did our best to research and get the rates

        // --- PagIbig ---
        // The government recently raised the Maximum Fund Salary (MFS) cap from 5,000 to 10,000.
        // Which means the maximum an employee will ever pay is 2% of ₱10,000, which is 200.00
        // Get the base to compute appropriately for the maximum and minimum an employee pays
        double pagibigBase = baseSalary;
        if (pagibigBase > 10000.0) {
            pagibigBase = 10000.0; // Cap it at the ceiling
        }
        // Whatever the base ends up being, multiply it by 2%
        this.pagibigContribution = pagibigBase * 0.02;

        // --- PhilHealth ---
        // The Universal Health Care Law bumped the premium rate up to 5.0% of the monthly basic salary.
        // This 5% is split evenly between the employer (2.5%) and the employee (2.5%).
        // We will use an if else to get the minimum and maximum range of the fee.
        double philhealthBase = baseSalary;
        if (philhealthBase < 10000.0) {
            philhealthBase = 10000.0; // PhilHealth requires a minimum flat fee. Bumping to 10k, ensures low
            // earners pay the right amount needed to keep their benefits
        } else if (philhealthBase > 100000.0) {
            philhealthBase = 100000.0; // 100k is the maximum cap PhilHealth placed. It's ceiling.
        }
        this.philhealthContribution = philhealthBase * 0.025;

        // --- SSS ---
        // As of 2025/2026, the SSS contribution rate is 15%. The employer pays 10%, and the employee pays 5%.
        // This uses the same if else statement that bumps and limits the base salary to the minimum and maximum of SSS.
        double sssBase = baseSalary;
        if (sssBase < 5000.0) {
            sssBase = 5000.0; // The minimum flat fee of SSS for low earners/part timers.
        } else if (sssBase > 35000.0) {
            sssBase = 35000.0; // This is their ceiling for high earners.
        }
        this.sssContribution = sssBase * 0.05;
    }

    private void computeTax(double grossPay) {
        // Find the taxable income by adding the computed contributions and getting rid of it along with the absences.
        // The government does not tax you on money you lost via absences or money already paid to PH, SSS, and PagIbig.
        double totalGovContributions = this.sssContribution + this.philhealthContribution + this.pagibigContribution;
        double taxableIncome = grossPay - totalGovContributions - this.absenceDeduction - this.undertimeDeduction;

        // The Ph standard is 20,833 minimum taxable threshold for monthly salaries
        // Under the Philippine TRAIN Law, anyone earning 250,000 or less per year is exempt from income taxes.
        // If you divide 250,000 by 12 months, you get roughly ₱20,833
        if (taxableIncome > 20833.0) {
            this.withholdingTax = (taxableIncome - 20833.0) * 0.20; // You are above the minimum threshold. The gov
            // takes your salary and subtracts it with 20833.
        } else {                                                    // 20% of that result is your final tax.
            // If they make below the threshold, they are exempt from taxes
            this.withholdingTax = 0.0;
        }
    }

    // The final method that takes all of the other methods and bridges to Main.java
    public void calculateAllDeductions(Employee employee, Timekeeping timeData, double grossPay) {
        double baseSalary = employee.getBaseRate();
        double hourlyrate;
        double monthlyIncomeBase;

        // Check for Part-timers
        if (employee.getEmptype().equalsIgnoreCase("Part-time")) {
            // Their baseSalary IS their hourly rate!
            hourlyrate = baseSalary;

            // estimate their monthly income (grosspay x 2) to figure out their PhilHealth/SSS brackets
            monthlyIncomeBase = grossPay * 2;

            // NO WORK, NO PAY: We do not penalize them for absences or undertime.
            // Gross Pay is already calculated based only on hours worked.
            this.absenceDeduction = 0.0;
            this.undertimeDeduction = 0.0;

        } else {
            // FOR REGULAR, PROBATIONARY, AND CONTRACTUAL
            hourlyrate = (baseSalary / 22.0) / 8.0;
            monthlyIncomeBase = baseSalary; // Use their fixed monthly salary for gov brackets

            // Run Attendance Deductions
            computeAttendanceDeductions(employee, timeData, hourlyrate);
        }

        // Run Government Contributions using the proper base
        computeGovernmentContributions(monthlyIncomeBase);

        // Run Tax Calculator
        computeTax(grossPay);
    }
}