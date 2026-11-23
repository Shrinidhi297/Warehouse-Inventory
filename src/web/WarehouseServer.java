package web;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import exception.InsufficientStockException;
import model.*;
import service.*;
import storage.FileHandler;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Runs the localhost website using the existing inventory services. */
public final class WarehouseServer {
    private final ProductService products;
    private final SupplierService suppliers;
    private final LoginService accounts;
    private final StockService stock;
    private final byte[] styles;
    private final Map<String, Session> sessions = new HashMap<>();

    /** Keeps the signed-in identity and the next feedback message. */
    private static final class Session {
        private final User user;
        private final String token = UUID.randomUUID().toString();
        private String notice = "";

        private Session(User user) {
            this.user = user;
        }
    }

    /** Initializes a separate CSV folder for the website. */
    private WarehouseServer(Path data) throws IOException {
        FileHandler files = new FileHandler(data);
        suppliers = new SupplierService(files);
        products = new ProductService(files);
        accounts = new LoginService(files);
        stock = new StockService(products, files);
        styles = Files.readAllBytes(Path.of("website", "style.css"));
    }

    /** Starts a loopback-only HTTP server. */
    public static void main(String[] args) throws IOException {
        int port = args.length > 0 ? Integer.parseInt(args[0]) : 8080;
        Path data = args.length > 1 ? Path.of(args[1]) : Path.of("web-data");
        WarehouseServer warehouse = new WarehouseServer(data);
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", port), 0);
        server.createContext("/", warehouse::handle);
        server.start();
        System.out.println("Warehouse Inventory is ready at http://127.0.0.1:" + server.getAddress().getPort());
    }

    /** Routes requests and keeps each request's reads and writes together. */
    private synchronized void handle(HttpExchange exchange) throws IOException {
        try (exchange) {
            String path = exchange.getRequestURI().getPath();
            String method = exchange.getRequestMethod();
            if (!method.equals("GET") && !method.equals("POST")) {
                exchange.getResponseHeaders().set("Allow", "GET, POST");
                send(exchange, 405, "text/plain", "Method not allowed.".getBytes(StandardCharsets.UTF_8));
                return;
            }
            if (path.equals("/style.css") && method.equals("GET")) {
                send(exchange, 200, "text/css; charset=utf-8", styles);
                return;
            }
            String id = sessionId(exchange);
            Session session = sessions.get(id);
            if (path.equals("/login") && method.equals("POST")) {
                if (session == null) {
                    signIn(exchange, parse(readForm(exchange)));
                } else {
                    redirect(exchange, "/products");
                }
                return;
            }
            if (session == null) {
                if (path.equals("/") || path.equals("/login")) {
                    html(exchange, 200, Views.login(""));
                } else {
                    redirect(exchange, "/login");
                }
                return;
            }
            if (path.equals("/") || path.equals("/login")) {
                redirect(exchange, "/products");
                return;
            }
            String page = path.substring(1);
            if (!Views.links(session.user).containsKey(page) && !page.equals("logout")) {
                html(exchange, 404, Views.layout(session.user, page, "", "<h1>Page unavailable</h1>"));
                return;
            }
            if (method.equals("POST")) {
                handleAction(exchange, page, id, session);
            } else if (page.equals("logout")) {
                redirect(exchange, "/products");
            } else {
                render(exchange, 200, page, session, parse(exchange.getRequestURI().getRawQuery()), "");
            }
        }
    }

    /** Checks credentials and issues a fresh session cookie on success. */
    private void signIn(HttpExchange exchange, Map<String, String> form) throws IOException {
        User user = accounts.login(value(form, "username").strip(), value(form, "password"));
        if (user == null) {
            html(exchange, 401, Views.login("Check your username and password."));
            return;
        }
        String id = UUID.randomUUID().toString();
        sessions.put(id, new Session(user));
        exchange.getResponseHeaders().add("Set-Cookie", "warehouse=" + id + "; Path=/; HttpOnly; SameSite=Strict");
        redirect(exchange, "/products");
    }

    /** Validates and performs a write authorized for the current role. */
    private void handleAction(HttpExchange exchange, String page, String id, Session session) throws IOException {
        Map<String, String> form = parse(readForm(exchange));
        if (!session.token.equals(value(form, "token"))) {
            html(exchange, 403, Views.layout(session.user, page, "", "<h1>Reload the page and try again</h1>"));
            return;
        }
        if (page.equals("logout")) {
            sessions.remove(id);
            exchange.getResponseHeaders().add("Set-Cookie", "warehouse=; Path=/; Max-Age=0; HttpOnly; SameSite=Strict");
            redirect(exchange, "/login");
            return;
        }
        String action = value(form, "action");
        try {
            if (session.user instanceof Admin && page.equals("products")) {
                saveProduct(form, action);
            } else if (session.user instanceof Admin && page.equals("suppliers")) {
                saveSupplier(form, action);
            } else if (session.user instanceof Admin && page.equals("users")) {
                saveUser(form, action, session.user);
            } else if (session.user instanceof Staff && (page.equals("stock-in") || page.equals("stock-out"))) {
                moveStock(form, page, session.user);
            } else {
                html(exchange, 403, Views.layout(session.user, page, "", "<h1>This action is unavailable</h1>"));
                return;
            }
            session.notice = switch (page) {
                case "stock-in" -> "Stock added.";
                case "stock-out" -> "Stock removed.";
                default -> action.equals("delete") ? "Record deleted." : "Changes saved.";
            };
            redirect(exchange, "/" + page);
        } catch (NumberFormatException error) {
            render(exchange, 422, page, session, form, "Enter a valid price and whole numbers for quantities.");
        } catch (IllegalArgumentException | InsufficientStockException error) {
            render(exchange, 422, page, session, form, error.getMessage());
        } catch (IOException error) {
            render(exchange, 500, page, session, form, "The change could not be saved. Check the data folder.");
        }
    }

    /** Adds, replaces or deletes one product. */
    private void saveProduct(Map<String, String> form, String action) throws IOException {
        String id = value(form, "id").strip();
        if (action.equals("delete")) {
            products.delete(id);
            return;
        }
        Supplier supplier = suppliers.find(value(form, "supplier"));
        if (supplier == null) {
            throw new IllegalArgumentException("Select a supplier.");
        }
        Product product = new Product(id, value(form, "name").strip(), value(form, "category").strip(),
                Integer.parseInt(value(form, "quantity")), new BigDecimal(value(form, "price")),
                Integer.parseInt(value(form, "reorder")), supplier.getId());
        if (action.equals("edit")) {
            products.edit(product);
        } else {
            products.add(product);
        }
    }

    /** Adds or replaces a supplier. */
    private void saveSupplier(Map<String, String> form, String action) throws IOException {
        Supplier supplier = new Supplier(value(form, "id").strip(), value(form, "name").strip(),
                value(form, "phone").strip(), value(form, "city").strip());
        if (action.equals("edit")) {
            suppliers.edit(supplier);
        } else {
            suppliers.add(supplier);
        }
    }

    /** Maintains accounts while protecting the active admin's role. */
    private void saveUser(Map<String, String> form, String action, User current) throws IOException {
        String username = value(form, "username").strip();
        String role = value(form, "role");
        if (action.equals("delete")) {
            if (username.equals(current.getUsername())) {
                throw new IllegalArgumentException("You cannot delete your signed-in account.");
            }
            accounts.delete(username);
        } else {
            if (username.equals(current.getUsername()) && !role.equals(current.getRole())) {
                throw new IllegalArgumentException("You cannot change your signed-in role.");
            }
            accounts.saveUser(username, value(form, "password"), role, action.equals("edit"));
        }
        sessions.values().removeIf(session -> session.user.getUsername().equals(username)
                && !session.user.getUsername().equals(current.getUsername()));
    }

    /** Records a receipt or withdrawal for the signed-in staff member. */
    private void moveStock(Map<String, String> form, String page, User user)
            throws IOException, InsufficientStockException {
        int quantity = Integer.parseInt(value(form, "quantity"));
        String id = value(form, "id");
        if (page.equals("stock-in")) {
            stock.stockIn(id, quantity, user.getUsername());
        } else {
            stock.stockOut(id, quantity, user.getUsername());
        }
    }

    /** Renders the selected inventory page. */
    private void render(HttpExchange exchange, int status, String page, Session session,
            Map<String, String> values, String error) throws IOException {
        Views views = new Views(products, suppliers, accounts, stock, session.user, session.token);
        String body = views.page(page, values, error);
        String notice = session.notice;
        session.notice = "";
        html(exchange, status, Views.layout(session.user, page, views.signOut(), Views.feedback(notice, false) + body));
    }

    /** Reads form data with a small limit appropriate for inventory fields. */
    private static String readForm(HttpExchange exchange) throws IOException {
        return new String(exchange.getRequestBody().readNBytes(8192), StandardCharsets.UTF_8);
    }

    /** Reads the sign-in cookie if present. */
    private static String sessionId(HttpExchange exchange) {
        String cookies = exchange.getRequestHeaders().getFirst("Cookie");
        if (cookies != null) {
            for (String cookie : cookies.split(";")) {
                String part = cookie.strip();
                if (part.startsWith("warehouse=")) {
                    return part.substring("warehouse=".length());
                }
            }
        }
        return "";
    }

    /** Decodes a query string or a browser form. */
    private static Map<String, String> parse(String raw) {
        Map<String, String> fields = new HashMap<>();
        if (raw != null) {
            for (String pair : raw.split("&")) {
                String[] parts = pair.split("=", 2);
                if (parts.length == 2) {
                    fields.put(URLDecoder.decode(parts[0], StandardCharsets.UTF_8),
                            URLDecoder.decode(parts[1], StandardCharsets.UTF_8));
                }
            }
        }
        return fields;
    }

    /** Gets a form value with an empty default. */
    private static String value(Map<String, String> fields, String key) {
        return fields.getOrDefault(key, "");
    }

    /** Sends an HTML response in UTF-8. */
    private static void html(HttpExchange exchange, int status, String body) throws IOException {
        send(exchange, status, "text/html; charset=utf-8", body.getBytes(StandardCharsets.UTF_8));
    }

    /** Writes a response with content type and cache policy. */
    private static void send(HttpExchange exchange, int status, String type, byte[] body) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", type);
        exchange.getResponseHeaders().set("Cache-Control", "no-store");
        exchange.getResponseHeaders().set("X-Content-Type-Options", "nosniff");
        exchange.sendResponseHeaders(status, body.length);
        exchange.getResponseBody().write(body);
    }

    /** Redirects after writes to avoid repeating form submissions. */
    private static void redirect(HttpExchange exchange, String location) throws IOException {
        exchange.getResponseHeaders().set("Location", location);
        exchange.sendResponseHeaders(303, -1);
    }
}
