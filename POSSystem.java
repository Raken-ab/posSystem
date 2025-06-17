import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import javax.swing.filechooser.FileNameExtensionFilter;

public class POSSystem {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            new LoginFrame();
        });
    }

    // Add this utility method:
    public static void logEvent(String message) {
        try (PrintWriter pw = new PrintWriter(new FileWriter("session.log", true))) {
            pw.println(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + " - " + message);
        } catch (IOException e) {
        }
    }
}

class User {
    private String username;
    private String password;
    private String role;
    private String fullName;

    public User(String username, String password, String role, String fullName) {
        this.username = username;
        this.password = password;
        this.role = role;
        this.fullName = fullName;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getRole() {
        return role;
    }

    public String getFullName() {
        return fullName;
    }

    @Override
    public String toString() {
        return username + "|" + password + "|" + role + "|" + fullName;
    }

    public static User fromString(String line) {
        String[] parts = line.split("\\|");
        if (parts.length == 4) {
            return new User(parts[0], parts[1], parts[2], parts[3]);
        }
        return null;
    }
}

class Product {
    private String code;
    private String name;
    private double price;
    private int quantity;
    private String category;
    // New fields
    private String description;
    private int minStock;
    private String supplier;
    private double cost;

    public Product(String code, String name, double price, int quantity, String category) {
        this(code, name, price, quantity, category, "", 5, "", price * 0.6);
    }

    public Product(String code, String name, double price, int quantity, String category,
            String description, int minStock, String supplier, double cost) {
        this.code = code;
        this.name = name;
        this.price = price;
        this.quantity = quantity;
        this.category = category;
        this.description = description;
        this.minStock = minStock;
        this.supplier = supplier;
        this.cost = cost;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getMinStock() {
        return minStock;
    }

    public void setMinStock(int minStock) {
        this.minStock = minStock;
    }

    public String getSupplier() {
        return supplier;
    }

    public void setSupplier(String supplier) {
        this.supplier = supplier;
    }

    public double getCost() {
        return cost;
    }

    public void setCost(double cost) {
        this.cost = cost;
    }

    public boolean isLowStock() {
        return quantity <= minStock;
    }

    public double getProfit() {
        return price - cost;
    }

    public double getProfitMargin() {
        return ((price - cost) / price) * 100;
    }

    @Override
    public String toString() {
        return code + "|" + name + "|" + price + "|" + quantity + "|" + category + "|" +
                description + "|" + minStock + "|" + supplier + "|" + cost;
    }

    public static Product fromString(String line) {
        String[] parts = line.split("\\|");
        if (parts.length >= 5) {
            String description = parts.length > 5 ? parts[5] : "";
            int minStock = parts.length > 6 ? Integer.parseInt(parts[6]) : 5;
            String supplier = parts.length > 7 ? parts[7] : "";
            double cost = parts.length > 8 ? Double.parseDouble(parts[8]) : Double.parseDouble(parts[2]) * 0.6;
            return new Product(parts[0], parts[1], Double.parseDouble(parts[2]),
                    Integer.parseInt(parts[3]), parts[4], description, minStock, supplier, cost);
        }
        return null;
    }
}

class SaleItem {
    private Product product;
    private int quantity;
    private double discount;

    public SaleItem(Product product, int quantity) {
        this(product, quantity, 0.0);
    }

    public SaleItem(Product product, int quantity, double discount) {
        this.product = product;
        this.quantity = quantity;
        this.discount = discount;
    }

    public Product getProduct() {
        return product;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getDiscount() {
        return discount;
    }

    public void setDiscount(double discount) {
        this.discount = discount;
    }

    public double getSubtotal() {
        return product.getPrice() * quantity;
    }

    public double getDiscountAmount() {
        return getSubtotal() * (discount / 100);
    }

    public double getTotal() {
        return getSubtotal() - getDiscountAmount();
    }
}

class DataManager {
    private static final String USERS_FILE = "users.txt";
    private static final String PRODUCTS_FILE = "products.txt";
    private static final String SALES_FILE = "sales.txt";
    private static final String CONFIG_FILE = "config.txt";

    static {
        initializeDefaultData();
    }

    private static void initializeDefaultData() {
        List<User> users = loadUsers();
        if (users.isEmpty()) {
            users.add(new User("admin", "admin123", "manager", "System Administrator"));
            users.add(new User("emp1", "emp123", "employee", "John Employee"));
            saveUsers(users);
        }

        List<Product> products = loadProducts();
        if (products.isEmpty()) {
            products.add(new Product("P001", "Coffee", 2.50, 100, "Beverages", "Premium roasted coffee", 10,
                    "Coffee Co.", 1.50));
            products.add(new Product("P002", "Sandwich", 5.99, 50, "Food", "Fresh sandwich with vegetables", 5,
                    "Food Supplier", 3.50));
            products.add(new Product("P003", "Water Bottle", 1.25, 200, "Beverages", "500ml purified water", 20,
                    "Water Co.", 0.75));
            products.add(
                    new Product("P004", "Chips", 1.99, 75, "Snacks", "Crispy potato chips", 10, "Snack Co.", 1.20));
            products.add(new Product("P005", "Energy Drink", 3.50, 60, "Beverages", "Energy boost drink", 8,
                    "Energy Co.", 2.00));
            saveProducts(products);
        }

        Map<String, String> config = loadConfig();
        if (config.isEmpty()) {
            config.put("tax_rate", "8.5");
            config.put("store_name", "Modern POS Store");
            config.put("store_address", "123 Main Street");
            config.put("store_phone", "(555) 123-4567");
            config.put("receipt_footer", "Thank you for your business!");
            saveConfig(config);
        }
    }

    public static List<User> loadUsers() {
        List<User> users = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(USERS_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                User user = User.fromString(line);
                if (user != null)
                    users.add(user);
            }
        } catch (IOException e) {
        }
        return users;
    }

    public static void saveUsers(List<User> users) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(USERS_FILE))) {
            for (User user : users) {
                pw.println(user.toString());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static List<Product> loadProducts() {
        List<Product> products = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(PRODUCTS_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                Product product = Product.fromString(line);
                if (product != null)
                    products.add(product);
            }
        } catch (IOException e) {
        }
        return products;
    }

    public static void saveProducts(List<Product> products) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(PRODUCTS_FILE))) {
            for (Product product : products) {
                pw.println(product.toString());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void saveSale(String saleData) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(SALES_FILE, true))) {
            pw.println(saleData);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static List<String> loadSales() {
        List<String> sales = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(SALES_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                sales.add(line);
            }
        } catch (IOException e) {
        }
        return sales;
    }

    public static Map<String, String> loadConfig() {
        Map<String, String> config = new HashMap<>();
        try (BufferedReader br = new BufferedReader(new FileReader(CONFIG_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("=", 2);
                if (parts.length == 2) {
                    config.put(parts[0], parts[1]);
                }
            }
        } catch (IOException e) {
        }
        return config;
    }

    public static void saveConfig(Map<String, String> config) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(CONFIG_FILE))) {
            for (Map.Entry<String, String> entry : config.entrySet()) {
                pw.println(entry.getKey() + "=" + entry.getValue());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

// Modern, appealing LoginFrame
class LoginFrame extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;

    public LoginFrame() {
        setTitle("POS System - Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 300);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel mainPanel = new JPanel();
        mainPanel.setBackground(new Color(245, 245, 245));
        mainPanel.setLayout(new GridBagLayout());

        JPanel card = new JPanel();
        card.setBackground(Color.WHITE);
        card.setLayout(new GridBagLayout());
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(41, 128, 185), 2),
                new EmptyBorder(20, 30, 20, 30)));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(14, 14, 14, 14);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel title = new JLabel("POS System Login", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(new Color(41, 128, 185));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        card.add(title, gbc);

        gbc.gridwidth = 1;
        gbc.gridy = 1;
        gbc.gridx = 0;
        JLabel userLabel = new JLabel("Username:");
        userLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        card.add(userLabel, gbc);
        gbc.gridx = 1;
        usernameField = new JTextField(15);
        usernameField.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        card.add(usernameField, gbc);

        gbc.gridy = 2;
        gbc.gridx = 0;
        JLabel passLabel = new JLabel("Password:");
        passLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        card.add(passLabel, gbc);
        gbc.gridx = 1;
        passwordField = new JPasswordField(15);
        passwordField.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        card.add(passwordField, gbc);

        gbc.gridy = 3;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        btnPanel.setOpaque(false);
        JButton loginBtn = new JButton("Login");
        JButton signupBtn = new JButton("Sign Up");
        styleButton(loginBtn, new Color(41, 128, 185));
        styleButton(signupBtn, new Color(46, 204, 113));
        btnPanel.add(loginBtn);
        btnPanel.add(signupBtn);
        card.add(btnPanel, gbc);

        loginBtn.addActionListener(e -> login());
        signupBtn.addActionListener(e -> openSignup());

        mainPanel.add(card);
        add(mainPanel);

        setVisible(true);
    }

    private void styleButton(JButton btn, Color color) {
        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setOpaque(true);
        btn.setBorderPainted(false);
        btn.setToolTipText(btn.getText());
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(btn.getBackground().darker());
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(color);
            }
        });
    }

    private void login() {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());
        List<User> users = DataManager.loadUsers();
        for (User user : users) {
            if (user.getUsername().equals(username) && user.getPassword().equals(password)) {
                POSSystem.logEvent("Login: " + username);
                new MainPOSFrame(user);
                dispose();
                return;
            }
        }
        JOptionPane.showMessageDialog(this, "Invalid credentials!", "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void openSignup() {
        new SignupFrame(this);
    }
}

class SignupFrame extends JFrame {
    private JTextField usernameField, fullNameField;
    private JPasswordField passwordField, confirmPasswordField;
    private JComboBox<String> roleCombo;
    private LoginFrame parent;

    public SignupFrame(LoginFrame parent) {
        this.parent = parent;
        setTitle("Sign Up - New User");
        setSize(400, 400);
        setLocationRelativeTo(parent);
        setResizable(false);

        initComponents();
        setVisible(true);
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        getContentPane().setBackground(Color.WHITE);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(new EmptyBorder(30, 30, 30, 30));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(new JLabel("Full Name:"), gbc);
        gbc.gridx = 1;
        fullNameField = new JTextField(15);
        formPanel.add(fullNameField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        formPanel.add(new JLabel("Username:"), gbc);
        gbc.gridx = 1;
        usernameField = new JTextField(15);
        formPanel.add(usernameField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        formPanel.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1;
        passwordField = new JPasswordField(15);
        formPanel.add(passwordField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        formPanel.add(new JLabel("Confirm Password:"), gbc);
        gbc.gridx = 1;
        confirmPasswordField = new JPasswordField(15);
        formPanel.add(confirmPasswordField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        formPanel.add(new JLabel("Role:"), gbc);
        gbc.gridx = 1;
        roleCombo = new JComboBox<>(new String[] { "employee", "manager" });
        formPanel.add(roleCombo, gbc);

        JPanel buttonPanel = new JPanel();
        JButton createBtn = new JButton("Create Account");
        JButton cancelBtn = new JButton("Cancel");

        createBtn.addActionListener(e -> createAccount());
        cancelBtn.addActionListener(e -> dispose());

        buttonPanel.add(createBtn);
        buttonPanel.add(cancelBtn);

        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        formPanel.add(buttonPanel, gbc);

        add(formPanel, BorderLayout.CENTER);
    }

    private void createAccount() {
        String fullName = fullNameField.getText().trim();
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        String confirmPassword = new String(confirmPasswordField.getPassword());
        String role = (String) roleCombo.getSelectedItem();

        if (fullName.isEmpty() || username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill all fields!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (!password.equals(confirmPassword)) {
            JOptionPane.showMessageDialog(this, "Passwords do not match!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (password.length() < 6) {
            JOptionPane.showMessageDialog(this, "Password must be at least 6 characters.", "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        List<User> users = DataManager.loadUsers();
        for (User user : users) {
            if (user.getUsername().equals(username)) {
                JOptionPane.showMessageDialog(this, "Username already exists!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        users.add(new User(username, password, role, fullName));
        DataManager.saveUsers(users);

        JOptionPane.showMessageDialog(this, "Account created successfully!", "Success",
                JOptionPane.INFORMATION_MESSAGE);
        dispose();
    }
}

class MainPOSFrame extends JFrame {
    private User currentUser;
    private List<Product> products;
    private List<SaleItem> cart;
    private JTable cartTable;
    private DefaultTableModel cartModel;
    private JLabel totalLabel;
    private JTextField productCodeField;
    private JPanel productListPanel;
    private Map<String, String> config;
    private JTextField searchField;
    private JComboBox<String> categoryCombo;

    public MainPOSFrame(User user) {
        this.currentUser = user;
        this.products = DataManager.loadProducts();
        this.cart = new ArrayList<>();
        this.config = DataManager.loadConfig();

        setTitle(config.getOrDefault("store_name", "POS System") + " - " + user.getFullName() + " (" + user.getRole()
                + ")");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);

        initComponents();
        setVisible(true);
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        JMenuBar menuBar = new JMenuBar();
        JMenu systemMenu = new JMenu("System");
        JMenuItem logoutItem = new JMenuItem("Logout");
        logoutItem.addActionListener(e -> logout());
        systemMenu.add(logoutItem);

        JMenu reportsMenu = new JMenu("Reports");
        JMenuItem lowStockItem = new JMenuItem("Low Stock Report");
        JMenuItem inventoryReportItem = new JMenuItem("Inventory Report");
        JMenuItem exportInventoryItem = new JMenuItem("Export Inventory to CSV");
        JMenuItem dailySalesItem = new JMenuItem("Daily Sales Report");
        JMenuItem profitReportItem = new JMenuItem("Profit Report");
        lowStockItem.addActionListener(e -> showLowStockReport());
        inventoryReportItem.addActionListener(e -> showInventoryReport());
        exportInventoryItem.addActionListener(e -> exportInventoryReport());
        dailySalesItem.addActionListener(e -> showDailySalesReport());
        profitReportItem.addActionListener(e -> showProfitReport());
        reportsMenu.add(lowStockItem);
        reportsMenu.add(inventoryReportItem);
        reportsMenu.add(exportInventoryItem);
        reportsMenu.add(dailySalesItem);
        reportsMenu.add(profitReportItem);

        if (currentUser.getRole().equals("manager")) {
            JMenu inventoryMenu = new JMenu("Inventory");
            JMenuItem manageProductsItem = new JMenuItem("Manage Products");
            manageProductsItem.addActionListener(e -> openInventoryManager());
            inventoryMenu.add(manageProductsItem);
            menuBar.add(inventoryMenu);
            menuBar.add(reportsMenu);
        }

        menuBar.add(systemMenu);
        setJMenuBar(menuBar);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(245, 245, 245));

        JPanel leftPanel = createProductPanel();
        JPanel rightPanel = createCartPanel();

        mainPanel.add(leftPanel, BorderLayout.WEST);
        mainPanel.add(rightPanel, BorderLayout.CENTER);

        add(mainPanel, BorderLayout.CENTER);
    }

    // --- New Reports ---
    private void showLowStockReport() {
        List<Product> lowStock = products.stream()
                .filter(Product::isLowStock)
                .collect(Collectors.toList());
        StringBuilder sb = new StringBuilder("Low Stock Products:\n\n");
        for (Product p : lowStock) {
            sb.append(String.format("%s (%s): %d left (Min: %d)\n", p.getName(), p.getCode(), p.getQuantity(),
                    p.getMinStock()));
        }
        if (lowStock.isEmpty())
            sb.append("All products are sufficiently stocked.");
        JOptionPane.showMessageDialog(this, sb.toString(), "Low Stock Report", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showInventoryReport() {
        StringBuilder sb = new StringBuilder("Inventory Report:\n\n");
        for (Product p : products) {
            sb.append(String.format("%s (%s): %d in stock, $%.2f each\n", p.getName(), p.getCode(), p.getQuantity(),
                    p.getPrice()));
        }
        JOptionPane.showMessageDialog(this, sb.toString(), "Inventory Report", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showDailySalesReport() {
        List<String> sales = DataManager.loadSales();
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        double total = 0;
        int count = 0;
        for (String sale : sales) {
            if (sale.startsWith(today)) {
                String[] parts = sale.split("\\|");
                if (parts.length > 2) {
                    total += Double.parseDouble(parts[2]);
                    count++;
                }
            }
        }
        JOptionPane.showMessageDialog(this, String.format("Today's Sales: %d\nTotal: $%.2f", count, total),
                "Daily Sales Report", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showProfitReport() {
        double totalProfit = 0;
        for (Product p : products) {
            totalProfit += p.getProfit() * p.getQuantity();
        }
        JOptionPane.showMessageDialog(this, String.format("Estimated Inventory Profit: $%.2f", totalProfit),
                "Profit Report", JOptionPane.INFORMATION_MESSAGE);
    }

    private JPanel createProductPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createTitledBorder("Product Selection"));
        panel.setPreferredSize(new Dimension(400, 0));

        JPanel codePanel = new JPanel(new FlowLayout());
        codePanel.add(new JLabel("Product Code:"));
        productCodeField = new JTextField(10);
        JButton addButton = new JButton("Add to Cart");
        addButton.setToolTipText("Add product by code");
        addButton.addActionListener(e -> addProductToCart());
        styleButton(addButton, new Color(41, 128, 185));
        codePanel.add(productCodeField);
        codePanel.add(addButton);

        // --- Search and Category Filter ---
        JPanel filterPanel = new JPanel(new FlowLayout());
        searchField = new JTextField(10);
        searchField.setToolTipText("Search by product name");
        filterPanel.add(new JLabel("Search:"));
        filterPanel.add(searchField);

        Set<String> categories = products.stream().map(Product::getCategory).collect(Collectors.toSet());
        categoryCombo = new JComboBox<>(categories.toArray(new String[0]));
        categoryCombo.insertItemAt("All", 0);
        categoryCombo.setSelectedIndex(0);
        categoryCombo.setToolTipText("Filter by category");
        filterPanel.add(new JLabel("Category:"));
        filterPanel.add(categoryCombo);

        searchField.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                refreshProductList();
            }
        });
        categoryCombo.addActionListener(e -> refreshProductList());

        productListPanel = new JPanel();
        productListPanel.setLayout(new BoxLayout(productListPanel, BoxLayout.Y_AXIS));
        refreshProductList();

        JScrollPane scrollPane = new JScrollPane(productListPanel);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        panel.add(codePanel, BorderLayout.NORTH);
        panel.add(filterPanel, BorderLayout.SOUTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private void refreshProductList() {
        String search = searchField.getText().toLowerCase();
        String category = (String) categoryCombo.getSelectedItem();
        productListPanel.removeAll();
        for (Product product : products) {
            boolean matchesSearch = search.isEmpty() || product.getName().toLowerCase().contains(search);
            boolean matchesCategory = category == null || category.equals("All")
                    || product.getCategory().equals(category);
            if (matchesSearch && matchesCategory) {
                productListPanel.add(createProductCard(product));
            }
        }
        productListPanel.revalidate();
        productListPanel.repaint();
    }

    private JPanel createProductCard(Product product) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
        card.setBackground(Color.WHITE);

        JPanel infoPanel = new JPanel(new GridLayout(3, 1));
        infoPanel.setBackground(Color.WHITE);
        JLabel nameLabel = new JLabel(product.getName());
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        if (product.isLowStock()) {
            nameLabel.setForeground(Color.RED);
            nameLabel.setToolTipText("Low stock!");
        }
        infoPanel.add(nameLabel);
        JLabel codeLabel = new JLabel("Code: " + product.getCode());
        codeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        infoPanel.add(codeLabel);
        JLabel priceLabel = new JLabel("Price: $" + String.format("%.2f", product.getPrice()));
        priceLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        infoPanel.add(priceLabel);

        JButton addBtn = new JButton("Add");
        addBtn.setPreferredSize(new Dimension(60, 30));
        addBtn.setToolTipText("Add this product to cart");
        addBtn.addActionListener(e -> addProductToCart(product));
        styleButton(addBtn, new Color(46, 204, 113));

        card.add(infoPanel, BorderLayout.CENTER);
        card.add(addBtn, BorderLayout.EAST);

        return card;
    }

    // --- Export Inventory Report to CSV ---
    private void exportInventoryReport() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Export Inventory Report");
        fileChooser.setFileFilter(new FileNameExtensionFilter("CSV Files", "csv"));
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            if (!file.getName().toLowerCase().endsWith(".csv")) {
                file = new File(file.getParentFile(), file.getName() + ".csv");
            }
            try (PrintWriter pw = new PrintWriter(new FileWriter(file))) {
                pw.println("Code,Name,Price,Quantity,Category,Description,MinStock,Supplier,Cost");
                for (Product p : products) {
                    pw.printf("%s,%s,%.2f,%d,%s,%s,%d,%s,%.2f%n",
                            p.getCode(), p.getName(), p.getPrice(), p.getQuantity(), p.getCategory(),
                            p.getDescription(), p.getMinStock(), p.getSupplier(), p.getCost());
                }
                JOptionPane.showMessageDialog(this, "Inventory report exported to " + file.getName());
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Export failed: " + e.getMessage());
            }
        }
    }

    private JPanel createCartPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createTitledBorder("Shopping Cart"));

        String[] columns = { "Product", "Price", "Qty", "Total", "Remove" };
        cartModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                // Only the quantity and remove button are editable
                return column == 2 || column == 4;
            }
        };
        cartTable = new JTable(cartModel) {
            @Override
            public Class<?> getColumnClass(int column) {
                if (column == 4)
                    return JButton.class;
                return super.getColumnClass(column);
            }
        };
        cartTable.setRowHeight(28);
        cartTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        cartTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));

        // Add a button renderer and editor for the "Remove" column
        cartTable.getColumn("Remove").setCellRenderer(new ButtonRenderer());
        cartTable.getColumn("Remove").setCellEditor(new ButtonEditor(new JCheckBox()));

        cartModel.addTableModelListener(new TableModelListener() {
            public void tableChanged(TableModelEvent e) {
                int row = e.getFirstRow();
                int col = e.getColumn();
                if (col == 2 && row >= 0 && row < cart.size()) {
                    try {
                        int newQty = Integer.parseInt(cartModel.getValueAt(row, 2).toString());
                        SaleItem item = cart.get(row);
                        int available = item.getProduct().getQuantity() + item.getQuantity();
                        if (newQty > 0 && newQty <= available) {
                            item.getProduct().setQuantity(available - newQty);
                            item.setQuantity(newQty);
                            updateCartDisplay();
                        } else {
                            JOptionPane.showMessageDialog(MainPOSFrame.this, "Invalid quantity!", "Error",
                                    JOptionPane.ERROR_MESSAGE);
                            updateCartDisplay();
                        }
                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(MainPOSFrame.this, "Invalid quantity format!", "Error",
                                JOptionPane.ERROR_MESSAGE);
                        updateCartDisplay();
                    }
                }
            }
        });

        JScrollPane cartScrollPane = new JScrollPane(cartTable);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBorder(new EmptyBorder(12, 12, 12, 12));

        totalLabel = new JLabel("Total: $0.00");
        totalLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        totalLabel.setForeground(new Color(41, 128, 185));

        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton clearBtn = new JButton("Clear Cart");
        JButton processBtn = new JButton("Process Sale");
        styleButton(clearBtn, new Color(231, 76, 60));
        styleButton(processBtn, new Color(41, 128, 185));
        clearBtn.addActionListener(e -> clearCart());
        processBtn.addActionListener(e -> processSale());
        buttonPanel.add(clearBtn);
        buttonPanel.add(processBtn);

        bottomPanel.add(totalLabel, BorderLayout.WEST);
        bottomPanel.add(buttonPanel, BorderLayout.EAST);

        panel.add(cartScrollPane, BorderLayout.CENTER);
        panel.add(bottomPanel, BorderLayout.SOUTH);

        return panel;
    }

    private void updateCartDisplay() {
        cartModel.setRowCount(0);
        double total = 0;
        for (int i = 0; i < cart.size(); i++) {
            SaleItem item = cart.get(i);
            Object[] row = {
                    item.getProduct().getName(),
                    String.format("$%.2f", item.getProduct().getPrice()),
                    item.getQuantity(),
                    String.format("$%.2f", item.getTotal()),
                    "Remove"
            };
            cartModel.addRow(row);
            total += item.getTotal();
        }
        totalLabel.setText("Total: $" + String.format("%.2f", total));
    }

    // ButtonRenderer for the "Remove" button in the cart table
    class ButtonRenderer extends JButton implements javax.swing.table.TableCellRenderer {
        public ButtonRenderer() {
            setOpaque(true);
            setForeground(Color.WHITE);
            setBackground(new Color(231, 76, 60)); // Red color
            setFont(new Font("Segoe UI", Font.BOLD, 12));
        }

        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            setText((value == null) ? "Remove" : value.toString());
            setBackground(new Color(231, 76, 60)); // Ensure red on every render
            return this;
        }
    }

    // ButtonEditor for the "Remove" button in the cart table
    class ButtonEditor extends DefaultCellEditor {
        private JButton button;
        private String label;
        private boolean isPushed;
        private int row;

        public ButtonEditor(JCheckBox checkBox) {
            super(checkBox);
            button = new JButton();
            button.setOpaque(true);
            button.setForeground(Color.WHITE);
            button.setBackground(new Color(231, 76, 60)); // Red color
            button.setFont(new Font("Segoe UI", Font.BOLD, 12));
            button.addActionListener(e -> fireEditingStopped());
        }

        public Component getTableCellEditorComponent(JTable table, Object value,
                boolean isSelected, int row, int column) {
            this.row = row;
            label = (value == null) ? "Remove" : value.toString();
            button.setText(label);
            button.setBackground(new Color(231, 76, 60)); // Ensure red on edit
            isPushed = true;
            return button;
        }

        public Object getCellEditorValue() {
            if (isPushed) {
                // Remove the item from cart
                MainPOSFrame frame = (MainPOSFrame) SwingUtilities.getWindowAncestor(button);
                if (frame != null && row >= 0 && row < frame.cart.size()) {
                    SaleItem item = frame.cart.get(row);
                    item.getProduct().setQuantity(item.getProduct().getQuantity() + item.getQuantity());
                    frame.cart.remove(row);
                    frame.updateCartDisplay();
                }
            }
            isPushed = false;
            return label;
        }
    }

    private void addProductToCart() {
        String code = productCodeField.getText().trim();
        if (code.isEmpty())
            return;

        Product product = findProductByCode(code);
        if (product != null) {
            addProductToCart(product);
            productCodeField.setText("");
        } else {
            JOptionPane.showMessageDialog(this, "Product not found!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void addProductToCart(Product product) {
        if (product.getQuantity() <= 0) {
            JOptionPane.showMessageDialog(this, "Product out of stock!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (product.isLowStock()) {
            JOptionPane.showMessageDialog(this, "Warning: This product is low in stock!", "Low Stock",
                    JOptionPane.WARNING_MESSAGE);
        }
        for (SaleItem item : cart) {
            if (item.getProduct().getCode().equals(product.getCode())) {
                int available = product.getQuantity() + item.getQuantity();
                if (item.getQuantity() < available) {
                    item.setQuantity(item.getQuantity() + 1);
                    product.setQuantity(product.getQuantity() - 1);
                    updateCartDisplay();
                    return;
                } else {
                    JOptionPane.showMessageDialog(this, "Not enough stock!", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }
        }
        cart.add(new SaleItem(product, 1));
        product.setQuantity(product.getQuantity() - 1);
        updateCartDisplay();
    }

    private void clearCart() {
        for (SaleItem item : cart) {
            item.getProduct().setQuantity(item.getProduct().getQuantity() + item.getQuantity());
        }
        cart.clear();
        updateCartDisplay();
    }

    private void processSale() {
        if (cart.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Cart is empty!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String timestamp = sdf.format(new Date());
        double total = cart.stream().mapToDouble(SaleItem::getTotal).sum();
        StringBuilder saleRecord = new StringBuilder();
        saleRecord.append(timestamp).append("|").append(currentUser.getUsername()).append("|").append(total);
        for (SaleItem item : cart) {
            saleRecord.append("|").append(item.getProduct().getCode())
                    .append(":").append(item.getQuantity());
        }
        DataManager.saveSale(saleRecord.toString());
        DataManager.saveProducts(products);
        this.products = DataManager.loadProducts();

        // Show receipt
        StringBuilder receipt = new StringBuilder();
        receipt.append("Receipt\n").append(config.getOrDefault("store_name", "POS")).append("\n")
                .append("Date: ").append(timestamp).append("\nCashier: ").append(currentUser.getFullName())
                .append("\n\n");
        for (SaleItem item : cart) {
            receipt.append(String.format("%s x%d - $%.2f\n", item.getProduct().getName(), item.getQuantity(),
                    item.getTotal()));
        }
        receipt.append("\nTotal: $").append(String.format("%.2f", total));
        JOptionPane.showMessageDialog(this, receipt.toString(), "Sale Receipt", JOptionPane.INFORMATION_MESSAGE);

        POSSystem.logEvent("Sale by " + currentUser.getUsername() + " for $" + String.format("%.2f", total));

        cart.clear();
        updateCartDisplay();
        refreshProducts();
    }

    private Product findProductByCode(String code) {
        return products.stream()
                .filter(p -> p.getCode().equalsIgnoreCase(code))
                .findFirst()
                .orElse(null);
    }

    private void openInventoryManager() {
        new InventoryManagerFrame(this, products);
    }

    private void logout() {
        dispose();
        new LoginFrame();
    }

    public void refreshProducts() {
        this.products = DataManager.loadProducts();
        productListPanel.removeAll();
        for (Product product : products) {
            JPanel productPanel = createProductCard(product);
            productListPanel.add(productPanel);
        }
        productListPanel.revalidate();
        productListPanel.repaint();
    }

    private void styleButton(JButton btn, Color color) {
        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setOpaque(true);
        btn.setBorderPainted(false);
        btn.setToolTipText(btn.getText());
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(btn.getBackground().darker());
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(color);
            }
        });
    }
}

class InventoryManagerFrame extends JFrame {
    private MainPOSFrame parent;
    private List<Product> products;
    private JTable productTable;
    private DefaultTableModel tableModel;

    public InventoryManagerFrame(MainPOSFrame parent, List<Product> products) {
        this.parent = parent;
        this.products = products;
        setTitle("Inventory Management");
        setSize(800, 600);
        setLocationRelativeTo(parent);
        initComponents();
        setVisible(true);
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        String[] columns = { "Code", "Name", "Price", "Quantity", "Category" };
        tableModel = new DefaultTableModel(columns, 0);
        productTable = new JTable(tableModel);
        productTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        productTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        loadTableData();
        JScrollPane scrollPane = new JScrollPane(productTable);

        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton addBtn = new JButton("Add Product");
        JButton editBtn = new JButton("Edit Product");
        JButton deleteBtn = new JButton("Delete Product");
        JButton closeBtn = new JButton("Close");

        styleButton(addBtn, new Color(41, 128, 185));
        styleButton(editBtn, new Color(46, 204, 113));
        styleButton(deleteBtn, new Color(231, 76, 60));
        styleButton(closeBtn, new Color(127, 140, 141));

        addBtn.addActionListener(e -> addProduct());
        editBtn.addActionListener(e -> editProduct());
        deleteBtn.addActionListener(e -> deleteProduct());
        closeBtn.addActionListener(e -> dispose());

        buttonPanel.add(addBtn);
        buttonPanel.add(editBtn);
        buttonPanel.add(deleteBtn);
        buttonPanel.add(closeBtn);

        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void loadTableData() {
        tableModel.setRowCount(0);
        for (Product product : products) {
            Object[] row = {
                    product.getCode(),
                    product.getName(),
                    String.format("%.2f", product.getPrice()),
                    product.getQuantity(),
                    product.getCategory()
            };
            tableModel.addRow(row);
        }
    }

    private void addProduct() {
        new ProductEditDialog(this, null);
    }

    private void editProduct() {
        int selectedRow = productTable.getSelectedRow();
        if (selectedRow >= 0) {
            Product product = products.get(selectedRow);
            new ProductEditDialog(this, product);
        } else {
            JOptionPane.showMessageDialog(this, "Please select a product to edit.", "No Selection",
                    JOptionPane.WARNING_MESSAGE);
        }
    }

    private void deleteProduct() {
        int selectedRow = productTable.getSelectedRow();
        if (selectedRow >= 0) {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to delete this product?",
                    "Confirm Delete", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                products.remove(selectedRow);
                DataManager.saveProducts(products);
                loadTableData();
                parent.refreshProducts();
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select a product to delete.", "No Selection",
                    JOptionPane.WARNING_MESSAGE);
        }
    }

    public void refreshTable() {
        loadTableData();
        parent.refreshProducts();
    }

    private void styleButton(JButton btn, Color color) {
        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setOpaque(true);
        btn.setBorderPainted(false);
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(btn.getBackground().darker());
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(color);
            }
        });
    }
}

class ProductEditDialog extends JDialog {
    private InventoryManagerFrame parent;
    private Product product;
    private JTextField codeField, nameField, priceField, quantityField, categoryField;

    public ProductEditDialog(InventoryManagerFrame parent, Product product) {
        super(parent, product == null ? "Add Product" : "Edit Product", true);
        this.parent = parent;
        this.product = product;
        setSize(400, 300);
        setLocationRelativeTo(parent);
        initComponents();
        setVisible(true);
    }

    private void initComponents() {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        add(new JLabel("Code:"), gbc);
        gbc.gridx = 1;
        codeField = new JTextField(15);
        add(codeField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        add(new JLabel("Name:"), gbc);
        gbc.gridx = 1;
        nameField = new JTextField(15);
        add(nameField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        add(new JLabel("Price:"), gbc);
        gbc.gridx = 1;
        priceField = new JTextField(15);
        add(priceField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        add(new JLabel("Quantity:"), gbc);
        gbc.gridx = 1;
        quantityField = new JTextField(15);
        add(quantityField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        add(new JLabel("Category:"), gbc);
        gbc.gridx = 1;
        categoryField = new JTextField(15);
        add(categoryField, gbc);

        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton saveBtn = new JButton("Save");
        JButton cancelBtn = new JButton("Cancel");
        saveBtn.addActionListener(e -> saveProduct());
        cancelBtn.addActionListener(e -> dispose());
        buttonPanel.add(saveBtn);
        buttonPanel.add(cancelBtn);

        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        add(buttonPanel, gbc);

        if (product != null) {
            codeField.setText(product.getCode());
            codeField.setEnabled(false);
            nameField.setText(product.getName());
            priceField.setText(String.valueOf(product.getPrice()));
            quantityField.setText(String.valueOf(product.getQuantity()));
            categoryField.setText(product.getCategory());
        }
    }

    private void saveProduct() {
        try {
            String code = codeField.getText().trim();
            String name = nameField.getText().trim();
            double price = Double.parseDouble(priceField.getText().trim());
            int quantity = Integer.parseInt(quantityField.getText().trim());
            String category = categoryField.getText().trim();

            if (code.isEmpty() || name.isEmpty() || category.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill all fields!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (price < 0 || quantity < 0) {
                JOptionPane.showMessageDialog(this, "Price and quantity must be non-negative!", "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            List<Product> products = DataManager.loadProducts();
            if (product == null) {
                for (Product p : products) {
                    if (p.getCode().equals(code)) {
                        JOptionPane.showMessageDialog(this, "Product code already exists!", "Error",
                                JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                }
                products.add(new Product(code, name, price, quantity, category));
            } else {
                product.setName(name);
                product.setPrice(price);
                product.setQuantity(quantity);
                product.setCategory(category);
            }
            DataManager.saveProducts(products);
            parent.refreshTable();
            dispose();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid price or quantity format!", "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}

class Test {
    public static void main(String[] args) {
        JFrame f = new JFrame();
        f.add(new JTextField(20));
        f.setSize(300, 100);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setVisible(true);
    }
}
