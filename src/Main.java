import gui.LoginView;
import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        // Simple syntax to launch the system
        SwingUtilities.invokeLater(() -> {
            new LoginView().setVisible(true);
        });
    }
}