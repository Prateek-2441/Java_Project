import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.List;
import java.util.ArrayList;

public class BeautyShopGUI extends JFrame {

    private CardLayout cardLayout;
    private JPanel mainPanel;

    private List<Product> products = new ArrayList<>();
    private final String productFile = "product.txt";
    private final String orderFile = "orders.txt";

    // User Panel
    private JTable productTable, cartTable;
    private JTextField quantityField, mobileField, addressField;
    private DefaultTableModel productModel, cartModel;
    private List<CartItem> cartItems = new ArrayList<>();

    // Admin Panel
    private JTable adminTable;
    private DefaultTableModel adminModel;

    public BeautyShopGUI() {
        setTitle("💄 Beauty Shop");
        setSize(900, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);
        add(mainPanel);

        mainPanel.add(createLoginPanel(), "Login");
        mainPanel.add(createUserPanel(), "User");
        mainPanel.add(createAdminPanel(), "Admin");

        loadProducts();
    }

    private JPanel createLoginPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(245, 230, 255));
        GridBagConstraints gbc = new GridBagConstraints();

        JLabel title = new JLabel("🛍️ Welcome to Beauty Shop");
        title.setFont(new Font("Serif", Font.BOLD, 28));
        title.setForeground(new Color(118, 93, 173));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2; gbc.insets = new Insets(20, 0, 20, 0);
        panel.add(title, gbc);

        gbc.gridwidth = 1;
        gbc.insets = new Insets(10, 10, 10, 10);

        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Username:"), gbc);
        gbc.gridx = 1;
        JTextField userField = new JTextField(15);
        panel.add(userField, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1;
        JPasswordField passField = new JPasswordField(15);
        panel.add(passField, gbc);

        JButton loginBtn = new JButton("Login");
        loginBtn.setBackground(new Color(118, 93, 173));
        loginBtn.setForeground(Color.WHITE);
        loginBtn.setFont(new Font("SansSerif", Font.BOLD, 14));
        loginBtn.setFocusPainted(false);

        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        panel.add(loginBtn, gbc);

        loginBtn.addActionListener(e -> {
            String user = userField.getText().trim();
            String pass = new String(passField.getPassword());

            if (user.equals("admin") && pass.equals("admin123")) {
                cardLayout.show(mainPanel, "Admin");
            } else if (!user.isEmpty()) {
                setTitle("💄 Beauty Shop - Welcome, " + user);
                cardLayout.show(mainPanel, "User");
            } else {
                JOptionPane.showMessageDialog(this, "Invalid login.");
            }
        });

        return panel;
    }

    private JPanel createUserPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        JLabel title = new JLabel("🛒 Beauty Shop - Shop Now", JLabel.CENTER);
        title.setFont(new Font("Serif", Font.BOLD, 24));
        title.setForeground(new Color(118, 93, 173));
        panel.add(title, BorderLayout.NORTH);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        panel.add(splitPane, BorderLayout.CENTER);

        // Left: Products
        JPanel productPanel = new JPanel(new BorderLayout());
        productPanel.setBorder(BorderFactory.createTitledBorder("🛍️ Products"));
        productModel = new DefaultTableModel(new Object[]{"ID", "Name", "Price", "Stock"}, 0);
        productTable = new JTable(productModel);
        productTable.setRowHeight(25);
        productPanel.add(new JScrollPane(productTable), BorderLayout.CENTER);

        JPanel inputPanel = new JPanel();
        inputPanel.add(new JLabel("Quantity:"));
        quantityField = new JTextField(5);
        inputPanel.add(quantityField);
        JButton addBtn = new JButton("Add to Cart");
        addBtn.addActionListener(e -> addToCart());
        inputPanel.add(addBtn);
        productPanel.add(inputPanel, BorderLayout.SOUTH);

        splitPane.setLeftComponent(productPanel);

        // Right: Cart
        JPanel cartPanel = new JPanel(new BorderLayout());
        cartPanel.setBorder(BorderFactory.createTitledBorder("🛒 Your Cart"));
        cartModel = new DefaultTableModel(new Object[]{"Product", "Qty", "Price"}, 0);
        cartTable = new JTable(cartModel);
        cartTable.setRowHeight(25);
        cartPanel.add(new JScrollPane(cartTable), BorderLayout.CENTER);

        JPanel checkoutPanel = new JPanel(new GridLayout(3, 2, 10, 10));
        checkoutPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        checkoutPanel.add(new JLabel("Mobile:"));
        mobileField = new JTextField();
        checkoutPanel.add(mobileField);

        checkoutPanel.add(new JLabel("Address:"));
        addressField = new JTextField();
        checkoutPanel.add(addressField);

        JButton checkoutBtn = new JButton("Checkout");
        checkoutBtn.addActionListener(e -> checkout());
        checkoutPanel.add(new JLabel());
        checkoutPanel.add(checkoutBtn);
        cartPanel.add(checkoutPanel, BorderLayout.SOUTH);

        splitPane.setRightComponent(cartPanel);
        splitPane.setDividerLocation(450);

        return panel;
    }

    private void addToCart() {
        int row = productTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select a product.");
            return;
        }

        String qtyStr = quantityField.getText().trim();
        if (!qtyStr.matches("\\d+")) {
            JOptionPane.showMessageDialog(this, "Enter valid quantity.");
            return;
        }

        int qty = Integer.parseInt(qtyStr);
        Product p = products.get(row);
        if (p.getStock() < qty) {
            JOptionPane.showMessageDialog(this, "Not enough stock.");
            return;
        }

        p.setStock(p.getStock() - qty);
        cartItems.add(new CartItem(p, qty));
        cartModel.addRow(new Object[]{p.getName(), qty, p.getPrice() * qty});

        quantityField.setText("");
        refreshProductTable();
    }

    private void checkout() {
        if (cartItems.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Cart is empty.");
            return;
        }

        String mobile = mobileField.getText().trim();
        String address = addressField.getText().trim();

        if (!mobile.matches("\\d{10}")) {
            JOptionPane.showMessageDialog(this, "Invalid mobile number.");
            return;
        }

        if (address.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter address.");
            return;
        }

        double total = 0;
        StringBuilder sb = new StringBuilder();
        sb.append("Mobile: ").append(mobile).append("\n");
        sb.append("Address: ").append(address).append("\n");

        for (CartItem item : cartItems) {
            double itemTotal = item.getQuantity() * item.getProduct().getPrice();
            sb.append(item.getProduct().getName()).append(" x ")
              .append(item.getQuantity()).append(" = $").append(String.format("%.2f", itemTotal)).append("\n");
            total += itemTotal;
        }

        sb.append("Total = $").append(String.format("%.2f", total)).append("\n");
        sb.append("---------------------------\n");

        try (PrintWriter pw = new PrintWriter(new FileWriter(orderFile, true))) {
            pw.write(sb.toString());
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Failed to save order.");
        }

        cartItems.clear();
        cartModel.setRowCount(0);
        mobileField.setText("");
        addressField.setText("");
        saveProducts();
        loadProducts();

        JOptionPane.showMessageDialog(this, "✅ Order placed successfully!");
    }

    private JPanel createAdminPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JLabel title = new JLabel("🛠️ Admin Panel - Product Management", JLabel.CENTER);
        title.setFont(new Font("Serif", Font.BOLD, 22));
        panel.add(title, BorderLayout.NORTH);

        adminModel = new DefaultTableModel(new Object[]{"ID", "Name", "Price", "Stock"}, 0);
        adminTable = new JTable(adminModel);
        adminTable.setRowHeight(25);
        panel.add(new JScrollPane(adminTable), BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel();
        JButton updateBtn = new JButton("Update Stock");
        JButton saveBtn = new JButton("Save");
        JButton logoutBtn = new JButton("Logout");

        updateBtn.addActionListener(e -> updateStockAdmin());
        saveBtn.addActionListener(e -> saveProducts());
        logoutBtn.addActionListener(e -> {
            saveProducts();
            cardLayout.show(mainPanel, "Login");
        });

        bottomPanel.add(updateBtn);
        bottomPanel.add(saveBtn);
        bottomPanel.add(logoutBtn);
        panel.add(bottomPanel, BorderLayout.SOUTH);

        return panel;
    }

    private void updateStockAdmin() {
        int row = adminTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select a product.");
            return;
        }

        String newStock = JOptionPane.showInputDialog("Enter new stock quantity:");
        if (newStock == null || !newStock.matches("\\d+")) {
            JOptionPane.showMessageDialog(this, "Invalid stock.");
            return;
        }

        int stock = Integer.parseInt(newStock);
        products.get(row).setStock(stock);
        adminModel.setValueAt(stock, row, 3);
        productModel.setValueAt(stock, row, 3);
    }

    private void loadProducts() {
        products.clear();
        productModel.setRowCount(0);
        adminModel.setRowCount(0);

        try (BufferedReader br = new BufferedReader(new FileReader(productFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] p = line.split(",");
                if (p.length == 4) {
                    Product prod = new Product(p[0], p[1], Double.parseDouble(p[2]), Integer.parseInt(p[3]));
                    products.add(prod);
                    productModel.addRow(new Object[]{prod.getId(), prod.getName(), prod.getPrice(), prod.getStock()});
                    adminModel.addRow(new Object[]{prod.getId(), prod.getName(), prod.getPrice(), prod.getStock()});
                }
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error loading product.txt");
        }
    }

    private void saveProducts() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(productFile))) {
            for (Product p : products) {
                pw.println(p.getId() + "," + p.getName() + "," + p.getPrice() + "," + p.getStock());
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error saving products.");
        }
    }

    private void refreshProductTable() {
        productModel.setRowCount(0);
        for (Product p : products) {
            productModel.addRow(new Object[]{p.getId(), p.getName(), p.getPrice(), p.getStock()});
        }
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (Exception ignored) {
        }
        SwingUtilities.invokeLater(() -> new BeautyShopGUI().setVisible(true));
    }

    class Product {
        private String id, name;
        private double price;
        private int stock;

        public Product(String id, String name, double price, int stock) {
            this.id = id;
            this.name = name;
            this.price = price;
            this.stock = stock;
        }

        public String getId() { return id; }
        public String getName() { return name; }
        public double getPrice() { return price; }
        public int getStock() { return stock; }
        public void setStock(int s) { this.stock = s; }
    }

    class CartItem {
        private Product product;
        private int quantity;

        public CartItem(Product p, int q) {
            this.product = p;
            this.quantity = q;
        }

        public Product getProduct() { return product; }
        public int getQuantity() { return quantity; }
    }
}