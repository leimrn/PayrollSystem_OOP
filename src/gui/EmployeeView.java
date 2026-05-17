package gui;

import javax.swing.*;
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
    private JButton btnLogout;
    private JPanel payslipCard;
    private JPanel timekeepingCard;
    private JPanel profileCard;
    private JPanel applyCard;
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

    private String employeeID;

    public EmployeeView(String id) {
        this.employeeID = id;

        if (btnTimekeeping != null) btnTimekeeping.addActionListener(e -> showCard("time"));
        if (btnProfile != null) btnProfile.addActionListener(e -> showCard("profile"));
        if (btnApply != null) btnApply.addActionListener(e -> showCard("apply"));
        if (btnPayslip != null) btnPayslip.addActionListener(e -> showCard("payslip"));

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

        if (lblID != null) lblID.setText("EMP-33550336");
        if (lblName != null) lblName.setText("Cyrene Khaslana");
        if (lblStatus != null) lblStatus.setText("Probationary");
        if (lblRate != null) lblRate.setText("500.00");
        if (lblLeaves != null) lblLeaves.setText("15.0");

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

            storage.RequestStorage reqStorage = new storage.RequestStorage();
            reqStorage.saveRequest(empName, type, date, "Pending");

            //String msg = String.format("Request Submitted to Admin!\nType: %s\nDate: %s\nEmployee: %s", type, date, empName);
            //JOptionPane.showMessageDialog(employeePanel, msg);

            String msg = String.format("Request Submitted!\nType: %s\nDate: %s\nEmployee: Cyrene Khaslana", type, date);
            JOptionPane.showMessageDialog(employeePanel, msg);

            if (txtRequestDate != null) txtRequestDate.setText("");
            if (txtReason != null) txtReason.setText("");
        };

        if (btnSubmitRequest != null) {
            btnSubmitRequest.addActionListener(submitAction);
        }

        if (submitButton != null) {
            submitButton.addActionListener(submitAction);
        }
    }

    private void showCard(String name) {
        if (contentArea != null && contentArea.getLayout() instanceof CardLayout) {
            CardLayout cl = (CardLayout) contentArea.getLayout();
            cl.show(contentArea, name);
        }
    }

    public JPanel getEmployeePanel() {
        return employeePanel;
    }

    private void createUIComponents() {

    }
}
