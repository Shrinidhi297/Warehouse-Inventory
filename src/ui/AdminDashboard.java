package ui;

import model.User;
import service.LoginService;
import service.ProductService;
import service.StockService;
import service.SupplierService;

/** Displays the admin role's navigation. */
public class AdminDashboard extends Dashboard {
    /** Builds tabs from the signed-in user's menu. */
    public AdminDashboard(User user, ProductService products, SupplierService suppliers,
            LoginService login, StockService stock, Runnable signOut) {
        super(user, signOut);
        for (String option : user.getMenuOptions()) {
            switch (option) {
                case "Products" -> {
                    ProductPanel panel = new ProductPanel(products, suppliers, true);
                    addTab(option, panel, panel::refresh);
                }
                case "Suppliers" -> {
                    SupplierPanel panel = new SupplierPanel(suppliers);
                    addTab(option, panel, panel::refresh);
                }
                case "Users" -> {
                    UserPanel panel = new UserPanel(login, user.getUsername());
                    addTab(option, panel, panel::refresh);
                }
                case "Low Stock" -> {
                    LowStockPanel panel = new LowStockPanel(products);
                    addTab(option, panel, panel::refresh);
                }
                case "History" -> {
                    HistoryPanel panel = new HistoryPanel(stock);
                    addTab(option, panel, panel::refresh);
                }
                default -> throw new IllegalArgumentException("Unknown menu option: " + option);
            }
        }
    }
}
