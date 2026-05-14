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
    private JButton btnApprove;
    private JButton btnDecline;

    private DefaultTableModel employeeModel;
    private DefaultTableModel requestModel;

    private storage.EmployeeStorage empStorage = new storage.EmployeeStorage();

    public AdminView() {
        employeeModel = new DefaultTableModel(new String[]{"ID", "Name", "Status", "Rate"}, 0);
        requestModel = new DefaultTableModel(new String[]{"Employee", "Type", "Days", "Status"}, 0);

        if (tblEmployees != null) tblEmployees.setModel(employeeModel);
        if (tblEmployee != null) tblEmployee.setModel(employeeModel);
        if (tblRequests != null) tblRequests.setModel(requestModel);

        if (btnAddEmployee != null) btnAddEmployee.addActionListener(e -> showPanel(addEmployeeCard));
        if (btnManage != null) btnManage.addActionListener(e -> showPanel(manageCard));
        if (btnRequests != null) btnRequests.addActionListener(e -> showPanel(requestsCard));
        if (btnComputePayroll != null) btnComputePayroll.addActionListener(e -> showPanel(computeCard));

        if (cbEmployeeSelect != null) cbEmployeeSelect.addItem("2025-001 - Cyrene Khaslana");
        if (cbPayPeriod != null) cbPayPeriod.addItem("May 1 - May 15, 2026");

        if (btnGeneratePayroll != null) {
            btnGeneratePayroll.addActionListener(e -> {
                if (txtAdminPayslipPreview != null) {
                    txtAdminPayslipPreview.setText(
                            "========================================\n" +
                                    "           EMPLOYEE PAYSLIP\n" +
                                    "========================================\n\n" +
                                    "Employee ID:      2025-001\n" +
                                    "Name:             Cyrene Khaslana\n" +
                                    "Status:           Probationary\n" +
                                    "Pay Period:       May 1 - May 15, 2026\n\n" +
                                    "----------------------------------------\n" +
                                    "EARNINGS\n" +
                                    "----------------------------------------\n" +
                                    "Base Rate:        ₱ 500.00 / day\n" +
                                    "Days Worked:      11 days\n" +
                                    "Gross Basic Pay:  ₱ 5,500.00\n" +
                                    "Overtime Pay:     ₱ 0.00\n\n" +
                                    "TOTAL EARNINGS:   ₱ 5,500.00\n\n" +
                                    "----------------------------------------\n" +
                                    "DEDUCTIONS\n" +
                                    "----------------------------------------\n" +
                                    "SSS:              ₱ 247.50\n" +
                                    "PhilHealth:       ₱ 137.50\n" +
                                    "Pag-IBIG:         ₱ 100.00\n" +
                                    "Late/Absences:    ₱ 0.00\n\n" +
                                    "TOTAL DEDUCTIONS: ₱ 485.00\n\n" +
                                    "========================================\n" +
                                    "NET PAY:          ₱ 5,015.00\n" +
                                    "========================================"
                    );
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

    private void handleRequestStatus(String status) {
        if (tblRequests != null) {
            int selectedRow = tblRequests.getSelectedRow();
            if (selectedRow != -1) {
                requestModel.setValueAt(status, selectedRow, 3);
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