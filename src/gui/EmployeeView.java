package gui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class EmployeeView {
    private JPanel employeePanel;
    private JPanel sidebarPanel;
    private JPanel contentArea;
    private JButton btnTimekeeping;
    private JButton btnProfile;
    private JButton btnApply;
    private JButton btnPayslip;
    private JButton btnViewRequests;
    private JButton btnLogout;
    private JPanel payslipCard;
    private JPanel timekeepingCard;
    private JPanel profileCard;
    private JPanel applyCard;
    private JPanel viewRequestsCard;
    private JComboBox<String> cbMonth;
    private JTextField txtDay;
    private JTextField txtTimeIn;
    private JTextField txtTimeOut;
    private JButton btnClockIn;
    private JButton btnClockOut;
    private JLabel lblID;
    private JLabel lblName;
    private JLabel lblStatus;
    private JLabel lblRate;
    private JLabel lblLeaves;
    private JComboBox<String> cbRequestType;
    private JTextField txtRequestDate;
    private JTextArea txtReason;
    private JButton submitButton;
    private JTextArea txtPayslip;
    private JButton btnSubmitRequest;
    private JTable tblMyRequests;

    private String employeeID;
    private DefaultTableModel myRequestsModel;

    public EmployeeView(String id) {
        this.employeeID = id;

        // Load profile data
        storage.EmployeeStorage empStorage = new storage.EmployeeStorage();
        java.util.List<Object[]> allEmps = empStorage.loadEmployees();

        boolean found = false;
        for (Object[] emp : allEmps) {
            //Check if the row is the employee selected
            if (emp[0].toString().equals(id)) {
                if (lblID != null) lblID.setText(emp[0].toString());
                if (lblName != null) lblName.setText(emp[1].toString());
                if (lblStatus != null) lblStatus.setText(emp[2].toString());
                if (lblRate != null) lblRate.setText(emp[3].toString());
                found = true;
                break;
            }
        }

        if (!found) {
            // If ID isn't in CSV
            if (lblID != null) lblID.setText(id);
            if (lblName != null) lblName.setText("Unknown");
        }

        if (btnTimekeeping != null) btnTimekeeping.addActionListener(e -> showCard("time"));
        if (btnProfile != null) btnProfile.addActionListener(e -> showCard("profile"));
        if (btnApply != null) btnApply.addActionListener(e -> showCard("apply"));

        if (btnPayslip != null) {
            btnPayslip.addActionListener(e -> {
                showCard("payslip");

                if (txtPayslip != null && lblID != null && lblRate != null) {
                    try {
                        //Gather employee information
                        String name = lblName.getText();
                        String status = lblStatus.getText();
                        double rate = Double.parseDouble(lblRate.getText());
                        // Get the current date and time
                        java.time.LocalDate today = java.time.LocalDate.now();
                        String month = today.format(java.time.format.DateTimeFormatter.ofPattern("MMMM"));
                        String year = String.valueOf(today.getYear());
                        int currentDay = today.getDayOfMonth();

                        // determine the cut-off period based on today's date
                        int startDay, endDay;
                        String period;
                        if (currentDay <= 15) {
                            startDay = 1;
                            endDay = 15;
                            period = month + " 1 - " + month + " 15, " + year;
                        } else {
                            // lengthOfMonth() knows if it's 28, 30, or 31 days!
                            startDay = 16;
                            endDay = today.lengthOfMonth();
                            period = month + " 16 - " + month + " " + endDay + ", " + year;
                        }

                        // REAL Timekeeping from CSV
                        timekeeping.Timekeeping timeData = new timekeeping.Timekeeping();
                        storage.TimekeepingStorage timeStorage = new storage.TimekeepingStorage();

                        // Load only the records for this specific employee
                        java.util.List<Object[]> myRecords = timeStorage.loadRecordsForEmployee(employeeID);
                        //Get employee requests from csv
                        storage.RequestStorage reqStorage = new storage.RequestStorage();
                        java.util.List<Object[]> allRequests = reqStorage.loadRequests();

                        int missingDays = 0;
                        //Checks every single day
                        for (int d = startDay; d <= endDay; d++) {
                            java.time.LocalDate dateToCheck = java.time.LocalDate.of(today.getYear(), today.getMonth(), d);
                            String targetDate = month + " " + d;
                            boolean foundInCSV = false;
                            Object[] csvRow = null;

                            // look for log in attendacne.csv
                            for (Object[] row : myRecords) {
                                if (row[1].toString().trim().equalsIgnoreCase(targetDate)) {
                                    foundInCSV = true;
                                    csvRow = row;
                                    break;
                                }
                            }

                            //Check request status for this date
                            boolean isLeaveApproved = false;
                            boolean isOtApproved = false;
                            for (Object[] req : allRequests) {

                                if (req[0].toString().equalsIgnoreCase(name) && req[2].toString().contains(targetDate)) {
                                    if (req[3].toString().equalsIgnoreCase("Approved")) {
                                        if (req[1].toString().equalsIgnoreCase("Leave")) isLeaveApproved = true;
                                        if (req[1].toString().equalsIgnoreCase("Overtime")) isOtApproved = true;
                                    }
                                }
                            }

                            //Handles missing days
                            if (foundInCSV) {
                                String tIn = csvRow[2].toString().trim();
                                String tOut = csvRow[3].toString().trim();

                                // If overtime is NOT approved, forcefully reset the exit time to 5:00 PM
                                if (!isOtApproved && tOut.contains("PM")) {
                                    // Simple logic: if it's after 5 PM, reset it to 5 PM
                                    int hour = Integer.parseInt(tOut.split(":")[0]);
                                    if (hour >= 5 && hour != 12) {
                                        tOut = "05:00 PM";
                                    }
                                }
                                timeData.addDailyRecord(id, targetDate, tIn, tOut);
                            }
                            else if (isLeaveApproved) {
                                // approved Leave = Paid. Add a standard 8-hour record.
                                timeData.addDailyRecord(id, targetDate, "08:00 AM", "05:00 PM");
                            }
                            else {
                                // no work + No approved leave + Weekday = Absence
                                boolean isWeekend = (dateToCheck.getDayOfWeek() == java.time.DayOfWeek.SATURDAY ||
                                        dateToCheck.getDayOfWeek() == java.time.DayOfWeek.SUNDAY);
                                if (!isWeekend) {
                                    missingDays++;
                                }
                            }
                        }

                        timeData.setTotalAbsences(missingDays);
                        timeData.calculateHours();

                        computation.GrossPayCalculator grossCalc = new computation.GrossPayCalculator();
                        grossCalc.calculategrosspay(status, rate, timeData);

                        computation.DeductionsCalculator dedCalc = new computation.DeductionsCalculator();
                        dedCalc.calculateAllDeductions(status, rate, timeData, grossCalc.getGrossPay());

                        double totalDeductions = dedCalc.getSssContribution() +
                                dedCalc.getPhilhealthContribution() +
                                dedCalc.getPagibigContribution() +
                                dedCalc.getAbsenceDeduction() +
                                dedCalc.getUndertimeDeduction() +
                                dedCalc.getWithholdingTax();

                        double netPay = grossCalc.getGrossPay() - totalDeductions;
                        if (netPay < 0) netPay = 0.0;

                        // Print
                        String receipt = String.format(
                                "========================================\n" +
                                        "           EMPLOYEE PAYSLIP\n" +
                                        "========================================\n\n" +
                                        "Employee ID:      %s\n" +
                                        "Name:             %s\n" +
                                        "Status:           %s\n" +
                                        "Pay Period:       %s\n\n" +
                                        "----------------------------------------\n" +
                                        "EARNINGS\n" +
                                        "----------------------------------------\n" +
                                        "Base Rate:        ₱ %.2f\n" +
                                        "Days Worked:      %.1f days\n" +
                                        "Overtime Pay:     ₱ %.2f\n" +
                                        "GROSS PAY:        ₱ %.2f\n\n" +
                                        "----------------------------------------\n" +
                                        "DEDUCTIONS\n" +
                                        "----------------------------------------\n" +
                                        "SSS:              ₱ %.2f\n" +
                                        "PhilHealth:       ₱ %.2f\n" +
                                        "Pag-IBIG:         ₱ %.2f\n" +
                                        "Absences:         ₱ %.2f\n" +
                                        "Late/Undertime:   ₱ %.2f\n" +
                                        "Withholding Tax:  ₱ %.2f\n\n" +
                                        "========================================\n" +
                                        "NET PAY:          ₱ %.2f\n" +
                                        "========================================",
                                id, name, status, period,
                                rate, (timeData.getTotalHours() / 8.0), grossCalc.getOvertimepay(), grossCalc.getGrossPay(),
                                dedCalc.getSssContribution(), dedCalc.getPhilhealthContribution(), dedCalc.getPagibigContribution(),
                                dedCalc.getAbsenceDeduction(), dedCalc.getUndertimeDeduction(), dedCalc.getWithholdingTax(),
                                netPay
                        );

                        txtPayslip.setText(receipt);
                    } catch (NumberFormatException ex) {
                        txtPayslip.setText("Error: Could not calculate payroll. Invalid rate data.");
                    }
                }
            });
        }

        if (btnViewRequests != null) btnViewRequests.addActionListener(e -> showCard("viewRequests"));

        myRequestsModel = new DefaultTableModel(new String[]{"Type", "Date/Days", "Status"}, 0);
        storage.RequestStorage reqStorage = new storage.RequestStorage();
        java.util.List<Object[]> allRequests = reqStorage.loadRequests();

        String currentEmpName = lblName != null ? lblName.getText() : "";
        for (Object[] row : allRequests) {
            if (row[0].toString().equals(currentEmpName)) {
                myRequestsModel.addRow(new Object[]{row[1], row[2], row[3]});
            }
        }
        if (tblMyRequests != null) tblMyRequests.setModel(myRequestsModel);

        if (btnLogout != null) {
            btnLogout.addActionListener(e -> {
                int confirm = JOptionPane.showConfirmDialog(
                        employeePanel,
                        "Are you sure you want to log out?",
                        "Confirm Logout",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE
                );

                if (confirm == JOptionPane.YES_OPTION) {
                    JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(employeePanel);
                    if (frame != null) frame.dispose();
                    new LoginView().setVisible(true);
                }
            });
        }

        if (cbMonth != null && cbMonth.getItemCount() == 0) {
            String[] months = {"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};
            cbMonth.setModel(new DefaultComboBoxModel<>(months));
        }

        if (btnClockIn != null) {
            btnClockIn.addActionListener(e -> {
                if (txtDay.getText().trim().isEmpty()) {
                    JOptionPane.showMessageDialog(employeePanel, "Please enter the day (e.g., 15) before clocking in.");
                    return;
                }

                // DEMO FIX: Read the text box first. If empty, use real system time.
                String timeToSave = txtTimeIn.getText().trim();
                if (timeToSave.isEmpty()) {
                    timeToSave = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("hh:mm a"));
                    txtTimeIn.setText(timeToSave);
                }

                JOptionPane.showMessageDialog(employeePanel, "Successfully Clocked In at " + timeToSave + ".\nDon't forget to Clock Out at the end of your shift!");
            });
        }

        if (btnClockOut != null) {
            btnClockOut.addActionListener(e -> {
                if (txtDay.getText().trim().isEmpty() || txtTimeIn.getText().trim().isEmpty()) {
                    JOptionPane.showMessageDialog(employeePanel, "You must Clock In first before you can Clock Out.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // DEMO FIX: Read the text box first. If empty, use real system time.
                String timeOut = txtTimeOut.getText().trim();
                if (timeOut.isEmpty()) {
                    timeOut = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("hh:mm a"));
                    txtTimeOut.setText(timeOut);
                }

                String timeIn = txtTimeIn.getText().trim();
                String fullDate = cbMonth.getSelectedItem().toString() + " " + txtDay.getText().trim();

                // SAVE TO CSV
                storage.TimekeepingStorage timeStorage = new storage.TimekeepingStorage();
                timeStorage.saveRecord(employeeID, fullDate, timeIn, timeOut);

                JOptionPane.showMessageDialog(employeePanel, "Successfully Clocked Out at " + timeOut + ".\nAttendance logged for " + fullDate);

                // Clear the boxes for the next day
                txtDay.setText("");
                txtTimeIn.setText("");
                txtTimeOut.setText("");
            });
        }

        if (cbRequestType != null && cbRequestType.getItemCount() == 0) {
            cbRequestType.setModel(new DefaultComboBoxModel<>(new String[]{"Leave", "Overtime"}));
        }

        java.awt.event.ActionListener submitAction = e -> {
            String type = cbRequestType != null && cbRequestType.getSelectedItem() != null ? (String) cbRequestType.getSelectedItem() : "";
            String date = txtRequestDate != null ? txtRequestDate.getText().trim() : "";
            String reason = txtReason != null ? txtReason.getText().trim() : "";
            String empName = lblName != null ? lblName.getText() : "Unknown Employee";

            if (date.isEmpty() || reason.isEmpty()) {
                JOptionPane.showMessageDialog(employeePanel, "Please fill out all fields.", "Missing Info", JOptionPane.WARNING_MESSAGE);
                return;
            }

            reqStorage.saveRequest(empName, type, date, "Pending");
            myRequestsModel.addRow(new Object[]{type, date, "Pending"});

            String msg = String.format("Request Submitted!\nType: %s\nDate: %s\nEmployee: %s", type, date, empName);
            JOptionPane.showMessageDialog(employeePanel, msg);

            if (txtRequestDate != null) txtRequestDate.setText("");
            if (txtReason != null) txtReason.setText("");
        };

        if (btnSubmitRequest != null) btnSubmitRequest.addActionListener(submitAction);
        if (submitButton != null) submitButton.addActionListener(submitAction);
    }

    private void showCard(String name) {
        if (timekeepingCard != null) timekeepingCard.setVisible(false);
        if (profileCard != null) profileCard.setVisible(false);
        if (applyCard != null) applyCard.setVisible(false);
        if (payslipCard != null) payslipCard.setVisible(false);
        if (viewRequestsCard != null) viewRequestsCard.setVisible(false);

        if (name.equals("time") && timekeepingCard != null) timekeepingCard.setVisible(true);
        if (name.equals("profile") && profileCard != null) profileCard.setVisible(true);
        if (name.equals("apply") && applyCard != null) applyCard.setVisible(true);
        if (name.equals("payslip") && payslipCard != null) payslipCard.setVisible(true);
        if (name.equals("viewRequests") && viewRequestsCard != null) viewRequestsCard.setVisible(true);

        if (employeePanel != null) {
            employeePanel.revalidate();
            employeePanel.repaint();
        }
    }

    public JPanel getEmployeePanel() {
        return employeePanel;
    }
}