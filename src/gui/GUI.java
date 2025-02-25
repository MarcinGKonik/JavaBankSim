package gui;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
import database.DatabaseManager;

public class GUI {

    // Method to show the login window for regular users
    public static void showLoginWindow() {
        JFrame loginFrame = new JFrame("Login");
        loginFrame.setSize(300, 200);
        loginFrame.setLayout(new GridLayout(4, 2));

        // Add UI elements for username and password input
        JLabel usernameLabel = new JLabel("Username:");
        JTextField usernameField = new JTextField();
        JLabel passwordLabel = new JLabel("Password:");
        JPasswordField passwordField = new JPasswordField();
        JButton loginButton = new JButton("Login");
        JButton registerButton = new JButton("Register");

        // **Login Button Functionality**
        loginButton.addActionListener(e -> {
            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());

            if (DatabaseManager.authenticateUser(username, password)) {
                showMainMenu(username); // Open main menu if login is successful
                loginFrame.dispose();
            } else {
                JOptionPane.showMessageDialog(null, "Invalid username or password.");
            }
        });

        // **Register Button Functionality**
        registerButton.addActionListener(e -> showRegisterWindow());

        // Add components to the frame
        loginFrame.add(usernameLabel);
        loginFrame.add(usernameField);
        loginFrame.add(passwordLabel);
        loginFrame.add(passwordField);
        loginFrame.add(loginButton);
        loginFrame.add(registerButton);

        // Set frame properties
        loginFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        loginFrame.setVisible(true);
    }

    // **🔹 Method to Show Registration Window**
    private static void showRegisterWindow() {
        JTextField usernameField = new JTextField();
        JPasswordField passwordField = new JPasswordField();
        JTextField balanceField = new JTextField();

        Object[] message = {
                "Username:", usernameField,
                "Password:", passwordField,
                "Initial Balance:", balanceField
        };

        int option = JOptionPane.showConfirmDialog(null, message, "Register New User", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());
            BigDecimal initialBalance;

            try {
                initialBalance = new BigDecimal(balanceField.getText());
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(null, "Invalid balance amount.");
                return;
            }

            if (username.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(null, "Username and password cannot be empty.");
                return;
            }

            if (DatabaseManager.registerUser(username, password, initialBalance)) {
                JOptionPane.showMessageDialog(null, "User registered successfully!");
            } else {
                JOptionPane.showMessageDialog(null, "Registration failed. Username may already exist.");
            }
        }
    }

    // **Method to Show the Main Menu**
    public static void showMainMenu(String username) {
        JFrame mainMenuFrame = new JFrame("Main Menu");
        mainMenuFrame.setSize(300, 250);
        mainMenuFrame.setLayout(new GridLayout(6, 1)); // Adjusted grid layout for more buttons

        JButton checkBalanceButton = new JButton("Check Balance");
        JButton depositButton = new JButton("Deposit Money");
        JButton sendMoneyButton = new JButton("Send Money");
        JButton loanRequestButton = new JButton("Request Loan");
        JButton withdrawButton = new JButton("Withdraw Money"); // New Withdraw Button

        // **Check Balance**
        checkBalanceButton.addActionListener(e -> {
            BigDecimal balance = DatabaseManager.getBalance(username);
            JOptionPane.showMessageDialog(null, "Your current balance is: $" + balance);
        });

        // **Deposit Money**
        depositButton.addActionListener(e -> {
            String amountStr = JOptionPane.showInputDialog("Enter amount to deposit:");
            try {
                BigDecimal amount = new BigDecimal(amountStr);
                if (DatabaseManager.updateBalance(username, amount)) {
                    JOptionPane.showMessageDialog(null, "Deposit successful!");
                } else {
                    JOptionPane.showMessageDialog(null, "Deposit failed.");
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(null, "Invalid amount.");
            }
        });

        // **Send Money**
        sendMoneyButton.addActionListener(e -> {
            String recipient = JOptionPane.showInputDialog("Enter recipient's username:");
            if (!DatabaseManager.userExists(recipient)) {
                JOptionPane.showMessageDialog(null, "Recipient not in the client database.");
                return;
            }

            String amountStr = JOptionPane.showInputDialog("Enter amount to send:");
            try {
                BigDecimal amount = new BigDecimal(amountStr);
                if (DatabaseManager.transferMoney(username, recipient, amount)) {
                    JOptionPane.showMessageDialog(null, "Transfer successful!");
                } else {
                    JOptionPane.showMessageDialog(null, "Transfer failed. Check funds or recipient.");
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(null, "Invalid amount.");
            }
        });

        // **Loan Request**
        loanRequestButton.addActionListener(e -> {
            String amountStr = JOptionPane.showInputDialog("Enter amount for loan request:");
            try {
                BigDecimal loanAmount = new BigDecimal(amountStr);
                boolean loanRequested = DatabaseManager.requestLoan(username, loanAmount);
                if (loanRequested) {
                    JOptionPane.showMessageDialog(null, "Loan request sent to admin for approval.");
                } else {
                    JOptionPane.showMessageDialog(null, "Failed to request loan. Please try again.");
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(null, "Invalid loan amount.");
            }
        });

        // **Withdraw Money**
        withdrawButton.addActionListener(e -> {
            String amountStr = JOptionPane.showInputDialog("Enter amount to withdraw:");
            try {
                BigDecimal amount = new BigDecimal(amountStr);
                BigDecimal currentBalance = DatabaseManager.getBalance(username);

                if (amount.compareTo(currentBalance) > 0) {
                    JOptionPane.showMessageDialog(null, "Insufficient balance.");
                    return;
                }

                // Withdraw by subtracting balance
                if (DatabaseManager.updateBalance(username, amount.negate())) {
                    JOptionPane.showMessageDialog(null, "Withdrawal successful!");
                } else {
                    JOptionPane.showMessageDialog(null, "Withdrawal failed.");
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(null, "Invalid amount.");
            }
        });

        // **Add Buttons to Main Menu**
        mainMenuFrame.add(checkBalanceButton);
        mainMenuFrame.add(depositButton);
        mainMenuFrame.add(sendMoneyButton);
        mainMenuFrame.add(loanRequestButton);
        mainMenuFrame.add(withdrawButton); // Add Withdraw button

        mainMenuFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainMenuFrame.setVisible(true);
    }
}
