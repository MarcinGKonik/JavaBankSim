import gui.GUI;
import gui.AdminPanel;
import database.DatabaseManager;
import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        try {
            // Ensure the database connection works before launching the GUI
            DatabaseManager.connect();

            // Ask if the user is an Admin or Regular User
            int choice = JOptionPane.showOptionDialog(
                    null,
                    "Select login type:",
                    "User Type Selection",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    new String[]{"Admin", "User"},
                    "User"
            );

            if (choice == 0) {
                // Admin login
                String adminPassword = JOptionPane.showInputDialog("Enter admin password:");
                if ("admin".equals(adminPassword)) {
                    SwingUtilities.invokeLater(() -> new AdminPanel().setVisible(true));
                } else {
                    JOptionPane.showMessageDialog(null, "Incorrect admin password.");
                }
            } else {
                // Open User Login
                GUI.showLoginWindow();
            }


        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error: Unable to connect to the database. Please check your database configuration.");
        }
    }
}
