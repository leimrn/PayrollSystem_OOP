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
            // If ID somehow isn't in CSV
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
                        String name = lblName.getText();
                        String status = lblStatus.getText();
                        double rate = Double.parseDouble(lblRate.getText());
                        String period = "May 1 - May 15, 2026"; // Current cut-off

                        // Dummy Timekeeping
                        timekeeping.Timekeeping timeData = new timekeeping.Timekeeping();
                        timeData.addDailyRecord(id, "May 1", "08:00 AM", "05:00 PM");
                        timeData.addDailyRecord(id, "May 2", "08:00 AM", "07:00 PM");
                        timeData.calculateHours();

                        // midterm math
                        computation.GrossPayCalculator grossCalc = new computation.GrossPayCalculator();
                        grossCalc.calculategrosspay(status, rate, timeData);

                        computation.DeductionsCalculator dedCalc = new computation.DeductionsCalculator();
                        dedCalc.calculateAllDeductions(status, rate, timeData, grossCalc.getGrossPay());

                        double netPay = grossCalc.getGrossPay() -
                                (dedCalc.getSssContribution() + dedCalc.getPhilhealthContribution() +
                                        dedCalc.getPagibigContribution() + dedCalc.getAbsenceDeduction() +
                                        dedCalc.getUndertimeDeduction());
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
                                        "Absences/Late:    ₱ %.2f\n" +
                                        "Withholding Tax:  ₱ %.2f\n\n" +
                                        "========================================\n" +
                                        "NET PAY:          ₱ %.2f\n" +
                                        "========================================",
                                id, name, status, period,
                                rate, (timeData.getTotalHours() / 8.0), grossCalc.getOvertimepay(), grossCalc.getGrossPay(),
                                dedCalc.getSssContribution(), dedCalc.getPhilhealthContribution(), dedCalc.getPagibigContribution(),
                                (dedCalc.getAbsenceDeduction() + dedCalc.getUndertimeDeduction()), dedCalc.getWithholdingTax(),
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
                    JOptionPane.showMessageDialog(employeePanel, "Please enter the day before clocking in.");
                    return;
                }
                String currentTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("hh:mm a"));
                txtTimeIn.setText(currentTime);
                JOptionPane.showMessageDialog(employeePanel, "Successfully Clocked In at " + currentTime);
            });
        }

        if (btnClockOut != null) {
            btnClockOut.addActionListener(e -> {
                if (txtDay.getText().trim().isEmpty()) {
                    JOptionPane.showMessageDialog(employeePanel, "Please enter the day before clocking out.");
                    return;
                }
                if (txtTimeIn.getText().trim().isEmpty()) {
                    JOptionPane.showMessageDialog(employeePanel, "You must Clock In first before you can Clock Out.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                String currentTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("hh:mm a"));
                txtTimeOut.setText(currentTime);
                JOptionPane.showMessageDialog(employeePanel, "Successfully Clocked Out at " + currentTime + ".\nAttendance logged for " + cbMonth.getSelectedItem() + " " + txtDay.getText());
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