package ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.ArrayList;
import javax.swing.*;
import model.User;

/** Provides a shared layout for the two role windows. */
class Dashboard extends JFrame {
    private final JTabbedPane tabs = new JTabbedPane();
    private final ArrayList<Runnable> refreshActions = new ArrayList<>();

    /** Builds the dashboard header and navigation. */
    Dashboard(User user, Runnable signOut) {
        super("Warehouse Inventory — " + user.getRole());
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        JPanel background = Theme.backdrop(new BorderLayout(0, 16));
        background.setBorder(BorderFactory.createEmptyBorder(20, 22, 22, 22));
        setContentPane(background);
        JPanel header = Theme.glass(new BorderLayout());
        header.setBorder(BorderFactory.createEmptyBorder(16, 20, 19, 20));
        JPanel identity = new JPanel(new java.awt.GridLayout(0, 1, 0, 4));
        identity.setOpaque(false);
        JLabel title = new JLabel("Warehouse Inventory");
        title.setFont(title.getFont().deriveFont(java.awt.Font.BOLD, 25f));
        identity.add(title);
        identity.add(new JLabel(user.getRole() + " Dashboard  /  " + user.getUsername()));
        header.add(identity, BorderLayout.WEST);
        JPanel actions = new JPanel(new java.awt.GridBagLayout());
        actions.setOpaque(false);
        JButton button = Theme.button("Sign Out", false);
        button.addActionListener(event -> {
            dispose();
            signOut.run();
        });
        actions.add(button);
        header.add(actions, BorderLayout.EAST);
        background.add(header, BorderLayout.NORTH);
        JPanel navigation = Theme.glass(new BorderLayout());
        navigation.setBorder(BorderFactory.createEmptyBorder(12, 12, 15, 12));
        Theme.styleTabs(tabs);
        navigation.add(tabs, BorderLayout.CENTER);
        background.add(navigation, BorderLayout.CENTER);
        tabs.addChangeListener(event -> {
            int selected = tabs.getSelectedIndex();
            if (selected >= 0) {
                refreshActions.get(selected).run();
            }
        });
        setMinimumSize(new Dimension(760, 480));
        setSize(1000, 620);
        setLocationRelativeTo(null);
    }

    /** Adds a tab and refreshes its data whenever selected. */
    void addTab(String title, JPanel panel, Runnable refresh) {
        refreshActions.add(refresh);
        tabs.addTab(title, panel);
    }
}
