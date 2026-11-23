package web;

import model.*;
import service.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

/** Renders the website's accessible HTML forms and inventory tables. */
final class Views {
    private final ProductService products;
    private final SupplierService suppliers;
    private final LoginService accounts;
    private final StockService stock;
    private final User user;
    private final String token;

    Views(ProductService products, SupplierService suppliers, LoginService accounts, StockService stock,
          User user, String token) {
        this.products = products;
        this.suppliers = suppliers;
        this.accounts = accounts;
        this.stock = stock;
        this.user = user;
        this.token = token;
    }

    /** Translates the model's role menus into page links. */
    static Map<String, String> links(User user) {
        Map<String, String> links = new LinkedHashMap<>();
        for (String title : user.getMenuOptions()) {
            String path = title.toLowerCase(java.util.Locale.ROOT).replace(' ', '-');
            links.put(path, title.equals("History") ? "Transaction history" : title);
        }
        return links;
    }

    /** Creates the sign-in page with the available demo accounts. */
    static String login(String error) {
        return document("Sign in", """
                <main class="login-shell">
                  <section class="login-story">
                    <div class="brand"><span class="brand-mark" aria-hidden="true">W</span>Warehouse Inventory</div>
                    <div class="story-copy"><h1>Warehouse<br> inventory</h1>
                    <p>Products, suppliers, and stock movements in one place.</p></div>
                    <div class="warehouse-art" aria-hidden="true">
                      <i></i><i></i><i></i><i></i><i></i><i></i>
                    </div>
                  </section>
                  <section class="login-form">
                    <h2>Sign in</h2>
                    <p>Use your warehouse account to continue.</p>
                """ + feedback(error, true) + """
                    <form method="post" action="/login">
                      <label>Username<input name="username" autocomplete="username" required autofocus></label>
                      <label>Password<input type="password" name="password" autocomplete="current-password" required></label>
                      <button class="primary" type="submit">Sign in</button>
                    </form>
                    <div class="demo">
                      <h3>Demo accounts</h3>
                      <div><strong>Admin</strong><code>admin / admin123</code></div>
                      <div><strong>Staff</strong><code>staff / staff123</code></div>
                    </div>
                  </section>
                </main>
                """);
    }

    /** Wraps a page in the role navigation and signed-in header. */
    static String layout(User user, String active, String signOut, String body) {
        StringBuilder nav = new StringBuilder();
        for (var link : links(user).entrySet()) {
            nav.append("<a href=\"/").append(link.getKey()).append("\"");
            if (link.getKey().equals(active)) {
                nav.append(" aria-current=\"page\"");
            }
            nav.append(">").append(escape(link.getValue())).append("</a>");
        }
        return document(links(user).getOrDefault(active, "Warehouse Inventory"), """
                <div class="workspace">
                  <aside class="sidebar">
                    <div class="brand"><span class="brand-mark" aria-hidden="true">W</span><span>Warehouse<br>Inventory</span></div>
                    <nav aria-label="Inventory">%s</nav>
                  </aside>
                  <div class="main-area">
                    <header class="topbar"><span class="identity"><b>%s</b><span>%s</span></span>%s</header>
                    <main>%s</main>
                  </div>
                </div>
                """.formatted(nav, escape(user.getUsername()), escape(user.getRole()), signOut, body));
    }

    /** Adds the local stylesheet and responsive viewport to a document. */
    private static String document(String title, String body) {
        return """
                <!doctype html>
                <html lang="en"><head><meta charset="utf-8">
                <meta name="viewport" content="width=device-width, initial-scale=1">
                <title>%s · Warehouse Inventory</title><link rel="stylesheet" href="/style.css">
                </head><body>%s</body></html>
                """.formatted(escape(title), body);
    }

    /** Builds the sign-out form. */
    String signOut() {
        return form("logout", "") + "<button class=\"secondary\" type=\"submit\">Sign out</button></form>";
    }

    /** Builds the main content for the current page. */
    String page(String page, Map<String, String> values, String error) {
        return switch (page) {
            case "products" -> products(values, error);
            case "suppliers" -> suppliers(values, error);
            case "users" -> users(values, error);
            case "stock-in", "stock-out" -> stock(page, values, error);
            case "low-stock" -> lowStock();
            case "history" -> history();
            default -> "<h1>Page unavailable</h1>";
        };
    }

    /** Displays search results and the admin product form when requested. */
    private String products(Map<String, String> values, String error) {
        String query = values.getOrDefault("q", "");
        boolean admin = user instanceof Admin;
        ArrayList<Product> matches = products.search(query);
        StringBuilder rows = new StringBuilder();
        for (Product item : matches) {
            Supplier supplier = suppliers.find(item.getSupplierId());
            rows.append("<tr>").append(cells(item.getId(), item.getName(), item.getCategory()))
                    .append(quantity(item)).append(cells("Rs " + item.getPrice().toPlainString(),
                            item.getReorderLevel(), supplier == null ? item.getSupplierId() : supplier.getName()));
            if (admin) {
                rows.append(actions("products", item.getId(), "id", true));
            }
            rows.append("</tr>");
        }
        String toolbar = """
                <form class="search" method="get" action="/products">
                  <label class="sr-only" for="search">Search by name or category</label>
                  <input id="search" type="search" name="q" placeholder="Search by name or category" value="%s">
                  <button class="secondary" type="submit">Search</button>
                  %s
                </form>
                """.formatted(escape(query), query.isBlank() ? "" : "<a href=\"/products\">Clear search</a>");
        return heading("Products", "Find products and review their current stock.",
                admin ? addLink("products", "Add product") : "")
                + feedback(error, true) + (admin ? productForm(values) : "")
                + table("Product catalogue", matches.size(), toolbar, rows.toString(), "No products match.",
                admin ? new String[]{"Product ID", "Name", "Category", "Quantity", "Price", "Reorder level", "Supplier", "Actions"}
                      : new String[]{"Product ID", "Name", "Category", "Quantity", "Price", "Reorder level", "Supplier"});
    }

    /** Builds the add or edit product form. */
    private String productForm(Map<String, String> values) {
        Product item = products.find(values.getOrDefault("edit", values.get("id")));
        boolean editing = values.containsKey("edit") || "edit".equals(values.get("action"));
        if (!showForm(values) || editing && item == null) {
            return "";
        }
        String supplier = values.getOrDefault("supplier", editing ? item.getSupplierId() : "");
        StringBuilder options = new StringBuilder("<option value=\"\">Select a supplier</option>");
        for (Supplier option : suppliers.list()) {
            options.append(option(option.getId(), option.getName(), supplier));
        }
        return editor("products", editing, "product",
                input("Product ID", "id", "text", values.getOrDefault("id", editing ? item.getId() : ""), editing)
                + input("Name", "name", "text", values.getOrDefault("name", editing ? item.getName() : ""), false)
                + input("Category", "category", "text", values.getOrDefault("category", editing ? item.getCategory() : ""), false)
                + input("Quantity", "quantity", "number", values.getOrDefault("quantity", editing ? "" + item.getQuantity() : "0"), false)
                + input("Price (Rs)", "price", "number", values.getOrDefault("price", editing ? item.getPrice().toPlainString() : ""), false)
                + input("Reorder level", "reorder", "number", values.getOrDefault("reorder", editing ? "" + item.getReorderLevel() : "0"), false)
                + select("Supplier", "supplier", options.toString()));
    }

    /** Lists suppliers and displays their add or edit form. */
    private String suppliers(Map<String, String> values, String error) {
        StringBuilder rows = new StringBuilder();
        var items = suppliers.list();
        for (Supplier item : items) {
            rows.append("<tr>").append(cells(item.getId(), item.getName(), item.getPhone(), item.getCity()))
                    .append(actions("suppliers", item.getId(), "id", false)).append("</tr>");
        }
        String editor = "";
        boolean editing = values.containsKey("edit") || "edit".equals(values.get("action"));
        Supplier item = suppliers.find(values.getOrDefault("edit", values.get("id")));
        if (showForm(values) && (!editing || item != null)) {
            editor = editor("suppliers", editing, "supplier",
                    input("Supplier ID", "id", "text", values.getOrDefault("id", editing ? item.getId() : ""), editing)
                    + input("Name", "name", "text", values.getOrDefault("name", editing ? item.getName() : ""), false)
                    + input("Phone", "phone", "tel", values.getOrDefault("phone", editing ? item.getPhone() : ""), false)
                    + input("City", "city", "text", values.getOrDefault("city", editing ? item.getCity() : ""), false));
        }
        return heading("Suppliers", "Manage supplier contact details.", addLink("suppliers", "Add supplier"))
                + feedback(error, true) + editor + table("Supplier directory", items.size(), "", rows.toString(),
                "No suppliers yet.", "Supplier ID", "Name", "Phone", "City", "Actions");
    }

    /** Lists user accounts and displays their add or edit form. */
    private String users(Map<String, String> values, String error) {
        StringBuilder rows = new StringBuilder();
        var items = accounts.list();
        String wanted = values.getOrDefault("edit", values.get("username"));
        User selected = null;
        for (User item : items) {
            rows.append("<tr>").append(cells(item.getUsername(), item.getRole()))
                    .append(actions("users", item.getUsername(), "username",
                            !item.getUsername().equals(user.getUsername()))).append("</tr>");
            if (item.getUsername().equals(wanted)) {
                selected = item;
            }
        }
        String editor = "";
        boolean editing = values.containsKey("edit") || "edit".equals(values.get("action"));
        if (showForm(values) && (!editing || selected != null)) {
            String role = values.getOrDefault("role", editing ? selected.getRole() : "Staff");
            boolean own = editing && selected.getUsername().equals(user.getUsername());
            editor = editor("users", editing, "user",
                    input("Username", "username", "text", wanted == null ? "" : wanted, editing)
                    + input("Password", "password", "password", "", false)
                    + (own ? input("Role", "role", "text", user.getRole(), true)
                    : select("Role", "role", option("Staff", "Staff", role) + option("Admin", "Admin", role))));
        }
        return heading("Users", "Manage warehouse access and account roles.", addLink("users", "Add user"))
                + feedback(error, true) + editor + table("User accounts", items.size(), "", rows.toString(),
                "No users yet.", "Username", "Role", "Actions");
    }

    /** Builds a stock movement form with availability listed for every product. */
    private String stock(String page, Map<String, String> values, String error) {
        boolean incoming = page.equals("stock-in");
        String title = incoming ? "Stock in" : "Stock out";
        String label = incoming ? "Record stock in" : "Record stock out";
        var items = products.list();
        StringBuilder options = new StringBuilder("<option value=\"\">Select a product</option>");
        for (Product item : items) {
            options.append(option(item.getId(), item.getName() + " · " + item.getId() + " · "
                    + item.getQuantity() + " available", values.getOrDefault("id", "")));
        }
        String content = items.isEmpty() ? "<p>There are no products available.</p>"
                : form(page, "move") + select("Product", "id", options.toString())
                + input("Quantity", "quantity", "number", values.getOrDefault("quantity", ""), false)
                + "<button class=\"primary\" type=\"submit\">" + label + "</button></form>";
        return heading(title, incoming ? "Add a received quantity to a product."
                : "Withdraw a quantity from the available stock.", "") + feedback(error, true)
                + "<section class=\"card movement\"><h2>" + (incoming ? "Receive stock" : "Withdraw stock")
                + "</h2>" + content + "</section>";
    }

    /** Lists products at or below the reorder level. */
    private String lowStock() {
        var items = products.lowStock();
        StringBuilder rows = new StringBuilder();
        for (Product item : items) {
            rows.append("<tr>").append(cells(item.getId(), item.getName(), item.getCategory()))
                    .append(quantity(item)).append(cells(item.getReorderLevel())).append("</tr>");
        }
        return heading("Low stock", "Review products at or below their reorder level.", "")
                + table("Reorder list", items.size(), "", rows.toString(), "All products are above their reorder levels.",
                "Product ID", "Name", "Category", "Quantity", "Reorder level");
    }

    /** Lists recorded stock movements in their original order. */
    private String history() {
        var items = stock.history();
        StringBuilder rows = new StringBuilder();
        for (Transaction item : items) {
            rows.append("<tr>").append(cells(item.getDate(), item.getProduct()))
                    .append("<td><span class=\"status\">").append(escape(item.getType())).append("</span></td>")
                    .append(cells(item.getQuantity(), item.getUsername())).append("</tr>");
        }
        return heading("Transaction history", "Review recorded stock receipts and withdrawals.", "")
                + table("Stock movements", items.size(), "", rows.toString(), "No stock movements yet.",
                "Date", "Product", "Type", "Quantity", "User");
    }

    /** Shows a named stock state alongside the numeric quantity. */
    private static String quantity(Product item) {
        return "<td class=\"quantity\">" + item.getQuantity()
                + (item.isLowStock() ? "<span class=\"status low\">Low</span>" : "") + "</td>";
    }

    /** Renders a scrollable table with an explicit empty state. */
    private static String table(String title, int count, String toolbar, String rows, String empty, String... columns) {
        StringBuilder headings = new StringBuilder();
        for (String column : columns) {
            headings.append("<th scope=\"col\">").append(escape(column)).append("</th>");
        }
        return """
                <section class="card">
                  <div class="table-top"><h2>%s <span class="count">%d</span></h2>%s</div>
                  <div class="table-scroll" tabindex="0" role="region" aria-label="%s">
                    <table><thead><tr>%s</tr></thead><tbody>%s</tbody></table>
                  </div>
                </section>
                """.formatted(escape(title), count, toolbar, escape(title), headings, rows.isEmpty()
                ? "<tr><td class=\"empty\" colspan=\"" + columns.length + "\">" + escape(empty) + "</td></tr>" : rows);
    }

    /** Creates escaped cells for a row. */
    private static String cells(Object... values) {
        StringBuilder cells = new StringBuilder();
        for (Object value : values) {
            cells.append("<td>").append(escape(String.valueOf(value))).append("</td>");
        }
        return cells.toString();
    }

    /** Provides the available edit and delete controls for a row. */
    private String actions(String page, String id, String key, boolean delete) {
        String remove = delete ? "<details class=\"delete\"><summary>Delete</summary><div>"
                + "<p>Delete " + escape(id) + "?</p>" + form(page, "delete") + hidden(key, id)
                + "<button class=\"danger\" type=\"submit\">Confirm delete</button></form></div></details>" : "";
        return "<td><div class=\"row-actions\"><a href=\"/" + page + "?edit="
                + URLEncoder.encode(id, StandardCharsets.UTF_8) + "\">Edit</a>" + remove + "</div></td>";
    }

    /** Determines whether an add or edit form was requested. */
    private static boolean showForm(Map<String, String> values) {
        return values.containsKey("add") || values.containsKey("edit")
                || "add".equals(values.get("action")) || "edit".equals(values.get("action"));
    }

    /** Builds a titled editor with save and cancel controls. */
    private String editor(String page, boolean editing, String item, String fields) {
        String title = (editing ? "Edit " : "Add ") + item;
        return "<section class=\"card editor\"><h2>" + title + "</h2>" + form(page, editing ? "edit" : "add")
                + "<div class=\"fields\">" + fields + "</div><div class=\"form-actions\">"
                + "<button class=\"primary\" type=\"submit\">Save " + item + "</button>"
                + "<a class=\"secondary\" href=\"/" + page + "\">Cancel</a></div></form></section>";
    }

    /** Starts an authenticated form. */
    private String form(String page, String action) {
        return "<form method=\"post\" action=\"/" + page + "\">" + hidden("token", token) + hidden("action", action);
    }

    /** Renders a labeled input with HTML validation and safe values. */
    private static String input(String label, String name, String type, String value, boolean readOnly) {
        String limits = type.equals("number")
                ? (name.equals("price") ? " min=\"0\" step=\"any\"" : " min=\"0\" step=\"1\"")
                : " maxlength=\"100\"";
        return "<label>" + label + "<input name=\"" + name + "\" type=\"" + type + "\" value=\""
                + escape(value) + "\"" + limits + (readOnly ? " readonly" : " required") + "></label>";
    }

    /** Renders a required selection with its visible label. */
    private static String select(String label, String name, String choices) {
        return "<label>" + label + "<select name=\"" + name + "\" required>" + choices + "</select></label>";
    }

    /** Renders a selection option with an optional selected state. */
    private static String option(String value, String label, String current) {
        return "<option value=\"" + escape(value) + "\"" + (value.equals(current) ? " selected" : "")
                + ">" + escape(label) + "</option>";
    }

    /** Renders an escaped hidden field. */
    private static String hidden(String name, String value) {
        return "<input type=\"hidden\" name=\"" + name + "\" value=\"" + escape(value) + "\">";
    }

    /** Creates the top heading, description and optional action. */
    private static String heading(String title, String text, String action) {
        return "<div class=\"page-heading\"><div><h1>" + title + "</h1><p>" + text
                + "</p></div>" + action + "</div>";
    }

    /** Links to the requested add form. */
    private static String addLink(String page, String text) {
        return "<a class=\"primary\" href=\"/" + page + "?add=1\">" + text + "</a>";
    }

    /** Displays an accessible error or success message. */
    static String feedback(String message, boolean error) {
        if (message.isEmpty()) {
            return "";
        }
        return "<p class=\"feedback " + (error ? "error\" role=\"alert" : "success\" role=\"status")
                + "\">" + escape(message) + "</p>";
    }

    /** Escapes untrusted text for HTML text and quoted attributes. */
    private static String escape(String value) {
        return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;").replace("'", "&#39;");
    }
}
