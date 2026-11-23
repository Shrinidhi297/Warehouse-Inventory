package service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import model.Admin;
import model.Staff;
import model.User;
import storage.FileHandler;

/** Authenticates users and maintains the account list. */
public class LoginService {
    private static final String FILE = "users.csv";
    private final FileHandler files;
    private HashMap<String, User> users = new HashMap<>();

    /** Loads accounts or creates the demo logins. */
    public LoginService(FileHandler files) throws IOException {
        this.files = files;
        if (!files.exists(FILE)) {
            files.write(FILE, List.of(new String[] {"admin", "admin123", "Admin"},
                    new String[] {"staff", "staff123", "Staff"}));
        }
        for (String[] row : files.read(FILE)) {
            users.put(row[0], createUser(row[0], row[1], row[2]));
        }
    }

    /** Returns the account when the credentials match. */
    public User login(String username, String password) {
        User user = users.get(username);
        if (user != null && user.getPassword().equals(password)) {
            return user;
        }
        return null;
    }

    /** Authenticates with the character array supplied by a Swing password field. */
    public User login(String username, char[] password) {
        return login(username, new String(password));
    }

    /** Returns accounts sorted by username. */
    public ArrayList<User> list() {
        ArrayList<String> names = new ArrayList<>(users.keySet());
        names.sort(String::compareTo);
        ArrayList<User> result = new ArrayList<>();
        for (String name : names) {
            result.add(users.get(name));
        }
        return result;
    }

    /** Adds or replaces an account after checking whether the username exists. */
    public void saveUser(String username, String password, String role, boolean editing) throws IOException {
        if (users.containsKey(username) != editing) {
            throw new IllegalArgumentException("Choose a new username or select an existing account to edit.");
        }
        HashMap<String, User> updated = new HashMap<>(users);
        updated.put(username, createUser(username, password, role));
        save(updated);
    }

    /** Removes an account. */
    public void delete(String username) throws IOException {
        HashMap<String, User> updated = new HashMap<>(users);
        updated.remove(username);
        save(updated);
    }

    /** Creates the subclass that matches the saved role. */
    private User createUser(String username, String password, String role) {
        return switch (role) {
            case "Admin" -> new Admin(username, password);
            case "Staff" -> new Staff(username, password);
            default -> throw new IllegalArgumentException("Unknown user role.");
        };
    }

    /** Saves the account list and keeps at least one administrator. */
    private void save(HashMap<String, User> updated) throws IOException {
        boolean hasAdmin = false;
        ArrayList<String[]> rows = new ArrayList<>();
        for (User user : updated.values()) {
            hasAdmin |= user instanceof Admin;
            rows.add(new String[] {user.getUsername(), user.getPassword(), user.getRole()});
        }
        if (!hasAdmin) {
            throw new IllegalArgumentException("Keep at least one admin account.");
        }
        files.write(FILE, rows);
        users = updated;
    }
}
