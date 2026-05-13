package gui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

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
    private JTable tblEmployee; // Existing variable
    private JTable tblRequests;
    private JButton approveButton; // Existing variable
    private JButton declineButton; // Existing variable
    private JButton btnApprove;
    private JButton btnDecline;

    private DefaultTableModel employeeModel;
    private DefaultTableModel requestModel;

    public AdminView() {
        // 1. Initialize Table Models
        employeeModel = new DefaultTableModel(new String[]{"ID", "Name", "Status", "Rate"}, 0);
        requestModel = new DefaultTableModel(new String[]{"Employee", "Type", "Days", "Status"}, 0);

        // 2. FOOLPROOF LINKING: Attach model to ANY table variable the Designer might be using
        if (tblEmployees != null) tblEmployees.setModel(employeeModel);
        if (tblEmployee != null) tblEmployee.setModel(employeeModel);
        if (tblRequests != null) tblRequests.setModel(requestModel);

        // --- SIDEBAR NAVIGATION ---
        if (btnAddEmployee != null) btnAddEmployee.addActionListener(e -> showPanel(addEmployeeCard));
        if (btnManage != null) btnManage.addActionListener(e -> showPanel(manageCard));
        if (btnRequests != null) btnRequests.addActionListener(e -> showPanel(requestsCard));

        // --- ADD EMPLOYEE LOGIC ---
        if (saveEmployeeButton != null) {
            saveEmployeeButton.addActionListener(e -> {
                String id = txtID.getText().trim();
                String name = txtName.getText().trim();
                String status = (cbStatus != null) ? (String) cbStatus.getSelectedItem() : "Regular";
                String rateText = txtRate.getText().trim();

                if (id.isEmpty() || name.isEmpty() || rateText.isEmpty()) {
                    JOptionPane.showMessageDialog(adminPanel, "All fields are required!", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                try {
                    Double.parseDouble(rateText);
                    // Adds the row to the model, which immediately updates whichever table is visible
                    employeeModel.addRow(new Object[]{id, name, status, rateText});

                    // Clear the fields
                    txtID.setText("");
                    txtName.setText("");
                    txtRate.setText("");
                    JOptionPane.showMessageDialog(adminPanel, "Employee " + name + " added.");
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(adminPanel, "Invalid rate.");
                }
            });
        }

        // --- MANAGE EMPLOYEES LOGIC (DELETE) ---
        if (btnDelete != null) {
            btnDelete.addActionListener(e -> {
                int selectedRow = -1;

                // Safely check whichever table is currently active in the UI
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

        // --- LEAVE REQUESTS LOGIC ---
        if (btnApprove != null) btnApprove.addActionListener(e -> handleRequestStatus("Approved"));
        if (approveButton != null) approveButton.addActionListener(e -> handleRequestStatus("Approved"));

        if (btnDecline != null) btnDecline.addActionListener(e -> handleRequestStatus("Declined"));
        if (declineButton != null) declineButton.addActionListener(e -> handleRequestStatus("Declined"));

        // --- LOGOUT ---
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

        // Dummy data for testing the Requests table
        requestModel.addRow(new Object[]{"Phainon Khaslana", "Vacation", "5", "Pending"});

        // Default View
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