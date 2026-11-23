# Warehouse Inventory

A local warehouse inventory website for a college Object-Oriented Programming project. The Java 17 server renders HTML and uses CSS for the interface. Inventory and accounts are stored in local CSV files.

## Run the project

Install JDK 17 or newer. On Windows, double-click `run.cmd` or run `.\run.cmd` from the project folder. The launcher looks for a JDK in `JAVA_HOME`, the standard Oracle JDK installation folder, or `PATH`. Open [http://127.0.0.1:8080](http://127.0.0.1:8080) in your browser. Stop the server with Ctrl+C. To use a different port, pass it to the launcher, for example `.\run.cmd 8081`.

In an IDE, open the project folder, mark `src` as the Java source root, and run `web.WarehouseServer` with the project folder as the working directory. On macOS or Linux, use this command from the project folder.

```sh
mkdir -p out && javac --release 17 -encoding UTF-8 -d out src/*/*.java && java -cp out web.WarehouseServer
```

The first run creates `web-data/` with five sample products and five suppliers. Changes persist between launches. The previous Swing demo's `data/` folder is independent. Demo passwords are stored as plain text in `users.csv`, and the server listens only on the local machine. No npm packages, framework, or database installation is required.

## Demo logins

| Role | Username | Password | Actions |
| --- | --- | --- | --- |
| Admin | `admin` | `admin123` | Manage products, suppliers, and users. View low stock and history. |
| Staff | `staff` | `staff123` | Search products, record stock in or out, and view low stock and history. |

Try withdrawing 21 units of Rice on the first run. The page shows **Only 20 items available in stock.**

## OOP concepts

| Concept | Where to find it |
| --- | --- |
| Encapsulation | `Product` and `Supplier` have private fields and validating setters. `Product.setQuantity()` rejects negative quantities. |
| Inheritance | `Admin` and `Staff` extend the abstract `User` class. |
| Polymorphism | Both subclasses override `getRole()` and `getMenuOptions()`. `Views` calls those methods through a `User` reference. |
| Interface | `StockOperation` declares `stockIn()` and `stockOut()`. `StockService` implements it. |
| Custom exception | `StockService` throws `InsufficientStockException`. `WarehouseServer` catches it and displays its message. |
| Collections | Services keep lists in `ArrayList`. `LoginService` uses a `HashMap` keyed by username. |
| Constructors and overloading | Model constructors validate their initial values. `FileHandler` overloads its constructor, and `LoginService.login()` accepts either a `String` or a `char[]` password. |
| `static` and `final` | `WarehouseServer.main()` is static, services use static final filename constants, and `Transaction` keeps its recorded fields final. |

## Class diagram

```text
Admin -------- extends -------> User (abstract) <------- extends -------- Staff
Browser <---------- HTTP -------------> WarehouseServer -----> Views ----> User
                                             |
                                             +---- LoginService -----> User
                                             +---- ProductService ---> Product
                                             +---- SupplierService --> Supplier
                                             +---- StockService -----> Transaction

StockService --- implements ---> StockOperation (interface)
StockService --- throws -------> InsufficientStockException
Services ------- read/write ---> FileHandler
```
