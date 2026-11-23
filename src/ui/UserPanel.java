package ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.io.IOException;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import model.User;
import service.LoginService;

/** Lets admins list, add, edit and delete user accounts. */
class UserPanel extends JPanel {
    private final LoginService login;
    private final String signedInUsername;
    private final DefaultTableModel rows = Forms.tableModel("Username", "Role");
    private final JTable table = Forms.table(rows);

    /** Builds the account list and management controls. */
    UserPanel(LoginService login, String signedInUsername) {
        super(new BorderLayout(8, 8));
        this.login = login;
        this.signedInUsername = signedInUsername;
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton add = Theme.button("Add", true);
        add.addActionListener(event -> showForm(null, "Staff"));
        JButton edit = Theme.button("Edit", false);
        edit.addActionListener(event -> {
            int row = table.getSelectedRow();
            if (row < 0) {
                Forms.error(this, "Select a user first.");
            } else {
                showForm(Forms.selectedId(table), (String) table.getValueAt(row, 1));
            }
        });
        JButton delete = Theme.button("Delete", false);
        delete.addActionListener(event -> deleteUser());
        actions.add(add);
        actions.add(edit);
        actions.add(delete);
        add(actions, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
        refresh();
    }

    /** Reloads usernames and roles from the account list. */
    void refresh() {
        rows.setRowCount(0);
        for (User user : login.list()) {
            rows.addRow(new Object[] {user.getUsername(), user.getRole()});
        }
    }

    /** Opens the account form without displaying the current password. */
    private void showForm(String current, String currentRole) {
        JTextField username = new JTextField(current == null ? "" : current, 16);
        username.setEditable(current == null);
        JPasswordField password = new JPasswordField(16);
        JComboBox<String> role = new JComboBox<>(new String[] {"Admin", "Staff"});
        role.setSelectedItem(currentRole);
        role.setEnabled(!signedInUsername.equals(current));
        JPanel form = Forms.form(new String[] {"Username", "Password", "Role"}, username, password, role);
        String title = current == null ? "Add User" : "Edit User";
        while (JOptionPane.showConfirmDialog(this, form, title, JOptionPane.OK_CANCEL_OPTION)
                == JOptionPane.OK_OPTION) {
            char[] entered = password.getPassword();
            try {
                login.saveUser(username.getText().strip(), new String(entered), (String) role.getSelectedItem(),
                        current != null);
                refresh();
                Forms.success(this, "User saved.");
                return;
            } catch (IOException | IllegalArgumentException error) {
                Forms.error(this, error.getMessage());
            } finally {
                java.util.Arrays.fill(entered, '\0');
            }
        }
    }

    /** Confirms and deletes an account other than the signed-in account. */
    private void deleteUser() {
        String username = Forms.selectedId(table);
        if (username == null) {
            Forms.error(this, "Select a user first.");
            return;
        }
        if (signedInUsername.equals(username)) {
            Forms.error(this, "You cannot delete the account you are signed in with.");
            return;
        }
        if (JOptionPane.showConfirmDialog(this, "Delete " + username + "?", "Delete User",
                JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            login.delete(username);
            refresh();
            Forms.success(this, "User deleted.");
        } catch (IOException | IllegalArgumentException error) {
            Forms.error(this, error.getMessage());
        }
    }
}
