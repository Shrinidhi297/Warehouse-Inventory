package ui;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.Arrays;
import javax.swing.*;
import model.Admin;
import model.User;
import service.LoginService;
import service.ProductService;
import service.StockService;
import service.SupplierService;

/** Collects credentials and opens the matching role dashboard. */
public class LoginFrame extends JFrame {
    private final JTextField username = new JTextField(18);
    private final JPasswordField password = new JPasswordField(18);

    /** Builds the login form. */
    public LoginFrame(LoginService login, ProductService products, SupplierService suppliers, StockService stock) {
        super("Warehouse Inventory");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        JPanel background = Theme.backdrop(new BorderLayout());
        background.setBorder(BorderFactory.createEmptyBorder(46, 54, 46, 54));
        JPanel content = Theme.glass(new BorderLayout(0, 22));
        content.setBorder(BorderFactory.createEmptyBorder(28, 30, 34, 30));
        JLabel heading = new JLabel("Warehouse Inventory");
        heading.setFont(heading.getFont().deriveFont(java.awt.Font.BOLD, 28f));
        heading.setForeground(Theme.INK);
        content.add(heading, BorderLayout.NORTH);
        JPanel form = new JPanel(new GridLayout(0, 1, 0, 6));
        form.setOpaque(false);
        JLabel usernameLabel = new JLabel("Username");
        usernameLabel.setLabelFor(username);
        JLabel passwordLabel = new JLabel("Password");
        passwordLabel.setLabelFor(password);
        form.add(usernameLabel);
        form.add(username);
        form.add(passwordLabel);
        form.add(password);
        content.add(form, BorderLayout.CENTER);
        JButton signIn = Theme.button("Sign In", true);
        signIn.addActionListener(event -> signIn(login, products, suppliers, stock));
        content.add(signIn, BorderLayout.SOUTH);
        background.add(content, BorderLayout.CENTER);
        setContentPane(background);
        getRootPane().setDefaultButton(signIn);
        pack();
        setLocationRelativeTo(null);
    }

    /** Validates the credentials and switches to the appropriate window. */
    private void signIn(LoginService login, ProductService products, SupplierService suppliers, StockService stock) {
        char[] entered = password.getPassword();
        User user = login.login(username.getText().strip(), entered);
        Arrays.fill(entered, '\0');
        password.setText("");
        if (user == null) {
            JOptionPane.showMessageDialog(this, "Check your username and password.",
                    "Sign in failed", JOptionPane.ERROR_MESSAGE);
            return;
        }
        Runnable signOut = () -> setVisible(true);
        JFrame dashboard;
        if (user instanceof Admin) {
            dashboard = new AdminDashboard(user, products, suppliers, login, stock, signOut);
        } else {
            dashboard = new StaffDashboard(user, products, suppliers, stock, signOut);
        }
        setVisible(false);
        dashboard.setVisible(true);
    }
}
