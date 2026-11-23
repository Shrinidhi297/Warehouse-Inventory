package ui;

import model.User;
import service.ProductService;
import service.StockService;
import service.SupplierService;

/** Displays the staff role's navigation. */
public class StaffDashboard extends Dashboard {
    /** Builds tabs from the signed-in user's menu. */
    public StaffDashboard(User user, ProductService products, SupplierService suppliers,
            StockService stock, Runnable signOut) {
        super(user, signOut);
        for (String option : user.getMenuOptions()) {
            switch (option) {
                case "Products" -> {
                    ProductPanel panel = new ProductPanel(products, suppliers, false);
                    addTab(option, panel, panel::refresh);
                }
                case "Stock In", "Stock Out" -> {
                    StockPanel panel = new StockPanel(products, stock, user.getUsername(), option.equals("Stock In"));
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
