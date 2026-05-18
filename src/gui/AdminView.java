package gui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.io.*;

public class AdminView {
    private JPanel adminPanel;
    private JPanel sidebarPanel;
    private JPanel contentArea;
    private JButton btnAddEmployee;
    private JButton btnManage;
    private JButton btnRequests;
    private JButton btnLogout;
    private JPanel addEmployeeCard;
    private JButton saveEmployeeButton;
    private JTextField txtID;
    private JTextField txtName;
    private JComboBox<String> cbStatus;
    private JTextField txtRate;
    private JPanel manageCard;
    private JTable tblEmployees;
    private JButton btnDelete;
    private JPanel requestsCard;
    private JTable tblEmployee;
    private JTable tblRequests;
    private JButton approveButton;
    private JButton declineButton;
    private JButton btnComputePayroll;
    private JPanel computeCard;
    private JComboBox<String> cbPayPeriod;
    private JComboBox<String> cbEmployeeSelect;
    private JButton btnGeneratePayroll;
    private JTextArea txtAdminPayslipPreview;
    private JButton btnEdit;
    private JComboBox cbMonth;
    private JButton btnApprove;
    private JButton btnDecline;

    private DefaultTableModel employeeModel;
    private DefaultTableModel requestModel;

    private storage.EmployeeStorage empStorage = new storage.EmployeeStorage();

    public AdminView() {
        employeeModel = new DefaultTableModel(new String[]{"ID", "Name", "Status", "Rate"}, 0);
        java.util.List<Object[]> savedEmployees = empStorage.loadEmployees();
        for (Object[] row : savedEmployees) {
            employeeModel.addRow(row);
        }
        if (tblEmployee != null) tblEmployee.setModel(employeeModel);
        if (btnAddEmployee != null) btnAddEmployee.addActionListener(e -> showPanel(addEmployeeCard));
        if (btnManage != null) btnManage.addActionListener(e -> showPanel(manageCard));

        requestModel = new DefaultTableModel(new String[]{"Employee", "Type", "Days", "Status"}, 0);
        storage.RequestStorage reqStorage = new storage.RequestStorage();
        java.util.List<Object[]> savedRequests = reqStorage.loadRequests();
        for (Object[] row : savedRequests) {
            requestModel.addRow(row);
        }
        if (tblRequests != null) tblRequests.setModel(requestModel);
        if (btnRequests != null) btnRequests.addActionListener(e -> showPanel(requestsCard));

        if (btnComputePayroll != null) btnComputePayroll.addActionListener(e -> showPanel(computeCard));

        if (cbEmployeeSelect != null) {
            cbEmployeeSelect.removeAllItems();
            // Loop through table model from csv
            for (int i = 0; i < employeeModel.getRowCount(); i++) {
                String id = employeeModel.getValueAt(i, 0).toString();
                String name = employeeModel.getValueAt(i, 1).toString();
                cbEmployeeSelect.addItem(id + " - " + name);
            }
        }

        if (cbMonth != null) {
            cbMonth.removeAllItems();
            String[] months = {"January", "February", "March", "April", "May", "June",
                    "July", "August", "September", "October", "November", "December"};
            for (String m : months) cbMonth.addItem(m);
        }

        if (cbPayPeriod != null) { // (Or cbCutoff if you renamed it)
            cbPayPeriod.removeAllItems();
            cbPayPeriod.addItem("1st Cut-off (1st - 15th)");
            cbPayPeriod.addItem("2nd Cut-off (16th - End of Month)");
        }

        if (btnGeneratePayroll != null) {
            btnGeneratePayroll.addActionListener(e -> {
                if (txtAdminPayslipPreview != null && cbEmployeeSelect.getSelectedItem() != null) {

                    // Extract ID from dropdown
                    String selectedItem = cbEmployeeSelect.getSelectedItem().toString();
                    String empID = selectedItem.split(" - ")[0];
                    String empName = selectedItem.split(" - ")[1];
                    String selectedMonth = cbMonth.getSelectedItem().toString();
                    String cutOff = cbPayPeriod.getSelectedItem().toString();
                    String fullPayPeriod = selectedMonth + " - " + cutOff;

                    // Find Status and Rate from the table
                    String status = "Regular";
                    double baseRate = 0.0;

                    for (int i = 0; i < employeeModel.getRowCount(); i++) {
                        if (employeeModel.getValueAt(i, 0).toString().equals(empID)) {
                            status = employeeModel.getValueAt(i, 2).toString();
                            baseRate = Double.parseDouble(employeeModel.getValueAt(i, 3).toString());
                            break;
                        }
                    }

                    // Setup Timekeeping (Add attendance csv eventually)
                    timekeeping.Timekeeping timeData = new timekeeping.Timekeeping();
                    timeData.addDailyRecord(empID, "May 1", "08:00 AM", "05:00 PM"); // Dummy 8 hours
                    timeData.addDailyRecord(empID, "May 2", "08:00 AM", "07:00 PM"); // Dummy OT
                    timeData.calculateHours();

                    // midterm math
                    computation.GrossPayCalculator grossCalc = new computation.GrossPayCalculator();
                    grossCalc.calculategrosspay(status, baseRate, timeData);

                    computation.DeductionsCalculator dedCalc = new computation.DeductionsCalculator();
                    dedCalc.calculateAllDeductions(status, baseRate, timeData, grossCalc.getGrossPay());

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
                            empID, empName, status, fullPayPeriod,
                            baseRate, (timeData.getTotalHours() / 8.0), grossCalc.getOvertimepay(), grossCalc.getGrossPay(),
                            dedCalc.getSssContribution(), dedCalc.getPhilhealthContribution(), dedCalc.getPagibigContribution(),
                            (dedCalc.getAbsenceDeduction() + dedCalc.getUndertimeDeduction()), dedCalc.getWithholdingTax(),
                            netPay
                    );

                    txtAdminPayslipPreview.setText(receipt);
                }
            });
        }

        if (saveEmployeeButton != null) {
            saveEmployeeButton.addActionListener(e -> {
                String lblID = txtID.getText().trim();
                String lblName = txtName.getText().trim();
                String lblStatus = (cbStatus != null) ? (String) cbStatus.getSelectedItem() : "Regular";
                String lblRate = txtRate.getText().trim();

                if (lblID.isEmpty() || lblName.isEmpty() || lblRate.isEmpty()) {
                    JOptionPane.showMessageDialog(adminPanel, "All fields are required!", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                try {
                    Double.parseDouble(lblRate);

                    empStorage.saveEmployee(lblID, lblName, lblStatus, lblRate);

                    employeeModel.addRow(new Object[]{lblID, lblName, lblStatus, lblRate});

                    txtID.setText("");
                    txtName.setText("");
                    txtRate.setText("");

                    JOptionPane.showMessageDialog(adminPanel, "Employee " + lblName + " added and saved to file.");

                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(adminPanel, "Invalid rate. Please enter a number.");
                }
            });
        }

        if (btnEdit != null) {
            btnEdit.addActionListener(e -> {
                int selectedRow = -1;
                if (tblEmployees != null && tblEmployees.getSelectedRow() != -1) {
                    selectedRow = tblEmployees.getSelectedRow();
                } else if (tblEmployee != null && tblEmployee.getSelectedRow() != -1) {
                    selectedRow = tblEmployee.getSelectedRow();
                }

                if (selectedRow != -1) {
                    txtID.setText(employeeModel.getValueAt(selectedRow, 0).toString());
                    txtName.setText(employeeModel.getValueAt(selectedRow, 1).toString());
                    if (cbStatus != null) cbStatus.setSelectedItem(employeeModel.getValueAt(selectedRow, 2).toString());
                    txtRate.setText(employeeModel.getValueAt(selectedRow, 3).toString());

                    employeeModel.removeRow(selectedRow);
                    java.util.List<Object[]> updatedEmployees = new java.util.ArrayList<>();
                    for (int i = 0; i < employeeModel.getRowCount(); i++) {
                        updatedEmployees.add(new Object[]{
                                employeeModel.getValueAt(i, 0),
                                employeeModel.getValueAt(i, 1),
                                employeeModel.getValueAt(i, 2),
                                employeeModel.getValueAt(i, 3)
                        });
                    }
                    empStorage.updateAllEmployees(updatedEmployees);

                    showPanel(addEmployeeCard);
                    JOptionPane.showMessageDialog(adminPanel, "Details loaded. Update the fields and click Save.");
                } else {
                    JOptionPane.showMessageDialog(adminPanel, "Please select an employee to edit.");
                }
            });
        }

        if (btnDelete != null) {
            btnDelete.addActionListener(e -> {
                int selectedRow = -1;
                if (tblEmployees != null && tblEmployees.getSelectedRow() != -1) {
                    selectedRow = tblEmployees.getSelectedRow();
                } else if (tblEmployee != null && tblEmployee.getSelectedRow() != -1) {
                    selectedRow = tblEmployee.getSelectedRow();
                }

                if (selectedRow != -1) {
                    employeeModel.removeRow(selectedRow);

                    java.util.List<Object[]> updatedEmployees = new java.util.ArrayList<>();
                    for (int i = 0; i < employeeModel.getRowCount(); i++) {
                        updatedEmployees.add(new Object[]{
                                employeeModel.getValueAt(i, 0),
                                employeeModel.getValueAt(i, 1),
                                employeeModel.getValueAt(i, 2),
                                employeeModel.getValueAt(i, 3)
                        });
                    }

                    empStorage.updateAllEmployees(updatedEmployees);

                } else {
                    JOptionPane.showMessageDialog(adminPanel, "Please select an employee to delete.");
                }
            });
        }

        if (btnApprove != null) btnApprove.addActionListener(e -> handleRequestStatus("Approved"));
        if (approveButton != null) approveButton.addActionListener(e -> handleRequestStatus("Approved"));

        if (btnDecline != null) btnDecline.addActionListener(e -> handleRequestStatus("Declined"));
        if (declineButton != null) declineButton.addActionListener(e -> handleRequestStatus("Declined"));

        if (btnLogout != null) {
            btnLogout.addActionListener(e -> {
                int confirm = JOptionPane.showConfirmDialog(adminPanel, "Logout?", "Confirm", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(adminPanel);
                    if (frame != null) frame.dispose();
                    new LoginView().setVisible(true);
                }
            });
        }

        requestModel.addRow(new Object[]{"Phainon Khaslana", "Vacation", "5", "Pending"});
        showPanel(addEmployeeCard);
    }

    private void saveRequestsToFile() {
        java.util.List<Object[]> updatedData = new java.util.ArrayList<>();
        storage.RequestStorage storageHandler = new storage.RequestStorage();

        for (int i = 0; i < requestModel.getRowCount(); i++) {
            updatedData.add(new Object[]{
                    requestModel.getValueAt(i, 0),
                    requestModel.getValueAt(i, 1),
                    requestModel.getValueAt(i, 2),
                    requestModel.getValueAt(i, 3)
            });
        }
        storageHandler.updateAllRequests(updatedData);
    }

    private void handleRequestStatus(String status) {
        if (tblRequests != null) {
            int selectedRow = tblRequests.getSelectedRow();
            if (selectedRow != -1) {
                requestModel.setValueAt(status, selectedRow, 3);
                saveRequestsToFile();
                JOptionPane.showMessageDialog(adminPanel, "Request has been " + status + ".");
            } else {
                JOptionPane.showMessageDialog(adminPanel, "Please select a request first.");
            }
        }
    }

    private void showPanel(JPanel panelToShow) {
        if (addEmployeeCard != null) addEmployeeCard.setVisible(false);
        if (manageCard != null) manageCard.setVisible(false);
        if (requestsCard != null) requestsCard.setVisible(false);
        if (computeCard != null) computeCard.setVisible(false);

        if (panelToShow != null) {
            panelToShow.setVisible(true);
        }

        if (adminPanel != null) {
            adminPanel.revalidate();
            adminPanel.repaint();
        }
    }

    public JPanel getAdminPanel() {
        return adminPanel;
    }
}