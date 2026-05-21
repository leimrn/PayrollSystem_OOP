package gui;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.Color;
import java.awt.Font;

public class LoginView extends JFrame {
    private JPanel mainPanel;
    private JTextField textField1;
    private JPasswordField passwordField1;
    private JButton loginButton;

    public LoginView() {
        setContentPane(mainPanel);
        setTitle("Payroll System - Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(450, 350);
        setLocationRelativeTo(null);

        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String user = textField1.getText().trim();
                String pass = new String(passwordField1.getPassword()).trim();

                // Master Admin
                if (user.equals("admin") && pass.equals("admin123")) {
                    openDashboard(new AdminView().getAdminPanel(), "Admin Dashboard");
                    return; // Stop running code
                }

                // csv employee login
                boolean loggedIn = false;
                storage.EmployeeStorage empStorage = new storage.EmployeeStorage();
                java.util.List<Object[]> allEmps = empStorage.loadEmployees();

                for (Object[] emp : allEmps) {
                    String savedID = emp[0].toString();
                    String savedName = emp[1].toString();

                    //  Username = Name
                    //  Password = ID
                    if (user.equals(savedName) && pass.equals(savedID)) {
                        openDashboard(new EmployeeView(savedID).getEmployeePanel(), "Employee Dashboard");
                        loggedIn = true;
                        break;
                    }
                }

                // If no match was found in the CSV (and it wasn't the admin)
                if (!loggedIn) {
                    JOptionPane.showMessageDialog(null, "Invalid Credentials", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
    }

    private void openDashboard(JPanel panel, String title) {
        JFrame frame = new JFrame(title);
        frame.setContentPane(panel);
        frame.setSize(800, 600);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
        this.dispose();
    }

    public static void main(String[] args) {
        System.setProperty("sun.java2d.noddraw", "true");
        System.setProperty("sun.java2d.d3d", "false");
        System.setProperty("sun.java2d.opengl", "false");

        Color darkBg = new Color(30, 30, 30);
        Color surfaceBg = new Color(45, 45, 45);
        Color accentOrange = new Color(255, 140, 0);
        Color textWhite = new Color(220, 220, 220);

        UIManager.put("Panel.background", darkBg);
        UIManager.put("Label.foreground", textWhite);
        UIManager.put("TextField.background", surfaceBg);
        UIManager.put("TextField.foreground", textWhite);
        UIManager.put("PasswordField.background", surfaceBg);
        UIManager.put("PasswordField.foreground", textWhite);
        UIManager.put("Button.background", surfaceBg);
        UIManager.put("Button.foreground", accentOrange);
        UIManager.put("Label.font", new Font("SansSerif", Font.BOLD, 14));

        UIManager.put("OptionPane.background", darkBg);
        UIManager.put("OptionPane.messageForeground", textWhite);
        UIManager.put("Panel.background", darkBg);

        // Header Styling
        UIManager.put("TableHeader.background", surfaceBg);
        UIManager.put("TableHeader.foreground", accentOrange);

        // Table Styling
        UIManager.put("Table.background", darkBg);
        UIManager.put("Table.foreground", textWhite);
        UIManager.put("Table.gridColor", surfaceBg);
        UIManager.put("Table.selectionBackground", accentOrange);
        UIManager.put("Table.selectionForeground", darkBg);

        // The "White Void" Fix (Viewport)
        UIManager.put("Viewport.background", darkBg);

        SwingUtilities.invokeLater(() -> {
            new LoginView().setVisible(true);
        });
    }
}