package database;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class DatabaseManager {

    // Database connection
    public static Connection connect() {
        try {
            return DriverManager.getConnection("jdbc:mysql://localhost:3306/bank", "root", "C3ntr14WAB");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Authenticate user
    public static boolean authenticateUser(String username, String password) {
        String query = "SELECT password FROM users WHERE username = ?";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                String storedPassword = rs.getString("password");
                return storedPassword.equals(password);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Get user balance
    public static BigDecimal getBalance(String username) {
        String query = "SELECT balance FROM users WHERE username = ?";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getBigDecimal("balance");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return BigDecimal.ZERO;
    }

    // Update user balance
    public static boolean updateBalance(String username, BigDecimal amount) {
        String query = "UPDATE users SET balance = balance + ? WHERE username = ?";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setBigDecimal(1, amount);
            pstmt.setString(2, username);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Check if user exists
    public static boolean userExists(String username) {
        String query = "SELECT 1 FROM users WHERE username = ?";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    //register new user
    public static boolean registerUser(String username, String password, BigDecimal initialBalance) {
        String query = "INSERT INTO users (username, password, balance) VALUES (?, ?, ?)";

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, username);
            pstmt.setString(2, password);
            pstmt.setBigDecimal(3, initialBalance);

            return pstmt.executeUpdate() > 0; // Returns true if a row was inserted
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }


    // Transfer money between users
    public static boolean transferMoney(String sender, String recipient, BigDecimal amount) {
        try (Connection conn = connect()) {
            conn.setAutoCommit(false);

            String senderQuery = "UPDATE users SET balance = balance - ? WHERE username = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(senderQuery)) {
                pstmt.setBigDecimal(1, amount);
                pstmt.setString(2, sender);
                if (pstmt.executeUpdate() == 0) {
                    conn.rollback();
                    return false;
                }
            }

            String recipientQuery = "UPDATE users SET balance = balance + ? WHERE username = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(recipientQuery)) {
                pstmt.setBigDecimal(1, amount);
                pstmt.setString(2, recipient);
                if (pstmt.executeUpdate() == 0) {
                    conn.rollback();
                    return false;
                }
            }

            conn.commit();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Request a loan
    public static boolean requestLoan(String username, BigDecimal amount) {
        String query = "INSERT INTO loan_requests (username, amount, status) VALUES (?, ?, ?)";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, username);
            pstmt.setBigDecimal(2, amount);
            pstmt.setString(3, "Pending");
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Fetch all pending loan requests
    public static Map<String, BigDecimal> getLoanRequests() {
        Map<String, BigDecimal> requests = new HashMap<>();
        String query = "SELECT username, amount FROM loan_requests WHERE status = 'Pending'";

        try (Connection conn = connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                requests.put(rs.getString("username"), rs.getBigDecimal("amount"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return requests;
    }

    // Approve a loan request
    public static boolean approveLoan(String username, BigDecimal amount) {
        String updateBalanceSQL = "UPDATE users SET balance = balance + ? WHERE username = ?";
        String deleteLoanSQL = "DELETE FROM loan_requests WHERE username = ? AND amount = ?";

        try (Connection conn = connect();
             PreparedStatement updateBalanceStmt = conn.prepareStatement(updateBalanceSQL);
             PreparedStatement deleteLoanStmt = conn.prepareStatement(deleteLoanSQL)) {

            conn.setAutoCommit(false); // Start transaction

            // Step 1: Update User Balance
            updateBalanceStmt.setBigDecimal(1, amount);
            updateBalanceStmt.setString(2, username);
            int balanceUpdated = updateBalanceStmt.executeUpdate();

            // Step 2: Remove Loan Request
            deleteLoanStmt.setString(1, username);
            deleteLoanStmt.setBigDecimal(2, amount);
            int loanDeleted = deleteLoanStmt.executeUpdate();

            if (balanceUpdated > 0 && loanDeleted > 0) {
                conn.commit(); // Commit transaction
                return true;
            } else {
                conn.rollback(); // Rollback in case of failure
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


    // Reject a loan request
    public static boolean rejectLoan(String username, BigDecimal amount) {
        String query = "UPDATE loan_requests SET status = 'Rejected' WHERE username = ? AND amount = ? AND status = 'Pending'";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, username);
            pstmt.setBigDecimal(2, amount);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Get all users
    public static List<String> getAllUsers() {
        List<String> users = new ArrayList<>();
        String query = "SELECT username FROM users";

        try (Connection conn = connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                users.add(rs.getString("username"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }

    // Delete a user
    public static boolean deleteUser(String username) {
        String query = "DELETE FROM users WHERE username = ?";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, username);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
