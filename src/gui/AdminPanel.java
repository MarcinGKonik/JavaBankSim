package gui;

import database.DatabaseManager;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableCellEditor;
import java.awt.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class AdminPanel extends JFrame {
    private JTable loanTable, userTable;
    private DefaultTableModel loanTableModel, userTableModel;

    public AdminPanel() {
        setTitle("Admin Panel");
        setSize(800, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // **Loan Requests Panel (TOP)**
        JPanel loanRequestsPanel = new JPanel(new BorderLayout());
        loanRequestsPanel.setBorder(BorderFactory.createTitledBorder("Loan Requests"));

        loanTableModel = new DefaultTableModel(new String[]{"Username", "Amount", "Approve", "Reject"}, 0);
        loanTable = new JTable(loanTableModel);
        loanRequestsPanel.add(new JScrollPane(loanTable), BorderLayout.CENTER);
        add(loanRequestsPanel, BorderLayout.NORTH);

        // **User List Panel (BOTTOM)**
        JPanel usersPanel = new JPanel(new BorderLayout());
        usersPanel.setBorder(BorderFactory.createTitledBorder("Registered Users"));

        userTableModel = new DefaultTableModel(new String[]{"Username", "Delete"}, 0);
        userTable = new JTable(userTableModel);
        usersPanel.add(new JScrollPane(userTable), BorderLayout.CENTER);
        add(usersPanel, BorderLayout.CENTER);

        // **Load Data into Tables**
        loadLoanRequests();
        loadUsers();
    }

    // **Fetch Loan Requests from Database**
    private void loadLoanRequests() {
        loanTableModel.setRowCount(0); // Clear table before reloading
        Map<String, BigDecimal> requests = DatabaseManager.getLoanRequests();

        for (Map.Entry<String, BigDecimal> entry : requests.entrySet()) {
            String username = entry.getKey();
            BigDecimal amount = entry.getValue();
            loanTableModel.addRow(new Object[]{username, amount, "✅ Approve", "❌ Reject"});
        }

        loanTable.getColumnModel().getColumn(2).setCellRenderer(new ButtonRenderer());
        loanTable.getColumnModel().getColumn(2).setCellEditor(new LoanButtonEditor(new JCheckBox(), loanTableModel, true));

        loanTable.getColumnModel().getColumn(3).setCellRenderer(new ButtonRenderer());
        loanTable.getColumnModel().getColumn(3).setCellEditor(new LoanButtonEditor(new JCheckBox(), loanTableModel, false));
    }

    // **Fetch Users from Database**
    private void loadUsers() {
        userTableModel.setRowCount(0); // Clear table before reloading
        List<String> users = DatabaseManager.getAllUsers();

        for (String username : users) {
            userTableModel.addRow(new Object[]{username, "🗑️ Delete"});
        }

        userTable.getColumnModel().getColumn(1).setCellRenderer(new ButtonRenderer());
        userTable.getColumnModel().getColumn(1).setCellEditor(new DeleteUserButtonEditor(new JCheckBox(), userTableModel));
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new AdminPanel().setVisible(true));
    }
}

// **Custom Button Renderer for JTable**
class ButtonRenderer extends JButton implements TableCellRenderer {
    public ButtonRenderer() {
        setOpaque(true);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        setText((value == null) ? "" : value.toString());
        return this;
    }
}

// **Custom Button Editor for Loan Approval/Rejection**
class LoanButtonEditor extends DefaultCellEditor {
    private JButton button;
    private boolean isApprove;
    private String username;
    private BigDecimal amount;
    private DefaultTableModel model;
    private boolean clicked;

    public LoanButtonEditor(JCheckBox checkBox, DefaultTableModel model, boolean isApprove) {
        super(checkBox);
        this.model = model;
        this.isApprove = isApprove;
        button = new JButton();
        button.setOpaque(true);
        button.addActionListener(e -> {
            if (clicked) {
                int row = model.getRowCount() - 1;
                username = (String) model.getValueAt(row, 0);
                amount = (BigDecimal) model.getValueAt(row, 1);

                if (isApprove) {
                    if (DatabaseManager.approveLoan(username, amount)) {
                        JOptionPane.showMessageDialog(button, "Loan Approved for " + username);
                    }
                } else {
                    if (DatabaseManager.rejectLoan(username, amount)) {
                        JOptionPane.showMessageDialog(button, "Loan Rejected for " + username);
                    }
                }
                model.removeRow(row);
            }
            clicked = false;
            fireEditingStopped();
        });
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        clicked = true;
        button.setText((value == null) ? "" : value.toString());
        return button;
    }

    @Override
    public Object getCellEditorValue() {
        return button.getText();
    }
}

// **Custom Button Editor for User Deletion**
class DeleteUserButtonEditor extends DefaultCellEditor {
    private JButton button;
    private String username;
    private DefaultTableModel model;
    private boolean clicked;

    public DeleteUserButtonEditor(JCheckBox checkBox, DefaultTableModel model) {
        super(checkBox);
        this.model = model;
        button = new JButton();
        button.setOpaque(true);
        button.addActionListener(e -> {
            if (clicked) {
                int row = model.getRowCount() - 1;
                username = (String) model.getValueAt(row, 0);

                int confirm = JOptionPane.showConfirmDialog(
                        button, "Are you sure you want to delete user " + username + "?",
                        "Confirm Deletion", JOptionPane.YES_NO_OPTION
                );

                if (confirm == JOptionPane.YES_OPTION && DatabaseManager.deleteUser(username)) {
                    model.removeRow(row);
                    JOptionPane.showMessageDialog(button, "User " + username + " deleted!");
                }
            }
            clicked = false;
            fireEditingStopped();
        });
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        clicked = true;
        button.setText((value == null) ? "" : value.toString());
        return button;
    }

    @Override
    public Object getCellEditorValue() {
        return button.getText();
    }
}
