package model;

/** Represents a warehouse staff account. */
public class Staff extends User {
    /** Creates a staff account. */
    public Staff(String username, String password) {
        super(username, password);
    }

    /** Returns the role name. */
    @Override
    public String getRole() { return "Staff"; }

    /** Returns the tabs this role can open. */
    @Override
    public String[] getMenuOptions() {
        return new String[] {"Products", "Stock In", "Stock Out", "Low Stock", "History"};
    }
}
