package model;

/** Represents a warehouse admin account. */
public class Admin extends User {
    /** Creates a admin account. */
    public Admin(String username, String password) {
        super(username, password);
    }

    /** Returns the role name. */
    @Override
    public String getRole() { return "Admin"; }

    /** Returns the tabs this role can open. */
    @Override
    public String[] getMenuOptions() {
        return new String[] {"Products", "Suppliers", "Users", "Low Stock", "History"};
    }
}
