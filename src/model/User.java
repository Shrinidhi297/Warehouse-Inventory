package model;

/** Holds the credentials and defines the behavior of a warehouse user. */
public abstract class User {
    private final String username;
    private String password;

    /** Creates a user with a username and password. */
    protected User(String username, String password) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Enter a username.");
        }
        this.username = username;
        setPassword(password);
    }

    /** Returns the username. */
    public String getUsername() { return username; }

    /** Returns the password for local demo storage. */
    public String getPassword() { return password; }

    /** Sets a nonblank password. */
    public void setPassword(String password) {
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("Enter a password.");
        }
        this.password = password;
    }

    /** Returns the role's display name. */
    public abstract String getRole();

    /** Returns the tabs available to the role. */
    public abstract String[] getMenuOptions();
}
