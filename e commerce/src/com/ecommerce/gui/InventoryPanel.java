package com.ecommerce.gui;

import com.ecommerce.model.Product;
import com.ecommerce.service.CatalogService;
import com.ecommerce.gui.GuiStyles.ModernButton;
import com.ecommerce.gui.GuiStyles.RoundedPanel;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class InventoryPanel extends JPanel {
    private final CatalogService catalogService;
    private final Runnable onCatalogUpdated;
    private JPanel inventoryListPanel;

    public InventoryPanel(CatalogService catalogService, Runnable onCatalogUpdated) {
        this.catalogService = catalogService;
        this.onCatalogUpdated = onCatalogUpdated;

        setLayout(new BorderLayout(15, 15));
        setBackground(GuiStyles.COLOR_BACKGROUND);
        setBorder(GuiStyles.getPadding(20, 20, 20, 20));

        initComponents();
        refreshInventory();
    }

    private void initComponents() {
        // Header Panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JLabel lblTitle = new JLabel("Product Inventory Administration");
        lblTitle.setFont(GuiStyles.FONT_TITLE);
        lblTitle.setForeground(GuiStyles.COLOR_TEXT_MAIN);
        headerPanel.add(lblTitle, BorderLayout.WEST);

        ModernButton btnAddProduct = new ModernButton("Add New Product");
        btnAddProduct.setPreferredSize(new Dimension(160, 36));
        btnAddProduct.addActionListener(e -> showAddProductDialog());
        headerPanel.add(btnAddProduct, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);

        // List Panel
        inventoryListPanel = new JPanel();
        inventoryListPanel.setLayout(new BoxLayout(inventoryListPanel, BoxLayout.Y_AXIS));
        inventoryListPanel.setBackground(GuiStyles.COLOR_BACKGROUND);

        JScrollPane scrollPane = new JScrollPane(inventoryListPanel);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.getVerticalScrollBar().setUnitIncrement(14);
        add(scrollPane, BorderLayout.CENTER);
    }

    public void refreshInventory() {
        inventoryListPanel.removeAll();

        List<Product> products = catalogService.getAllProducts();

        if (products.isEmpty()) {
            JPanel emptyPanel = new JPanel(new GridBagLayout());
            emptyPanel.setOpaque(false);
            JLabel lblEmpty = new JLabel("No products in inventory.");
            lblEmpty.setFont(GuiStyles.FONT_SUBTITLE);
            lblEmpty.setForeground(GuiStyles.COLOR_TEXT_MUTED);
            emptyPanel.add(lblEmpty);
            inventoryListPanel.setLayout(new BorderLayout());
            inventoryListPanel.add(emptyPanel, BorderLayout.CENTER);
        } else {
            inventoryListPanel.setLayout(new BoxLayout(inventoryListPanel, BoxLayout.Y_AXIS));
            for (Product p : products) {
                inventoryListPanel.add(createInventoryRow(p));
                inventoryListPanel.add(Box.createRigidArea(new Dimension(0, 10)));
            }
        }

        inventoryListPanel.revalidate();
        inventoryListPanel.repaint();
    }

    private JPanel createInventoryRow(Product p) {
        RoundedPanel row = new RoundedPanel(12);
        row.setLayout(new GridBagLayout());
        row.setBorder(GuiStyles.getPadding(12, 15, 12, 15));
        row.setBackground(GuiStyles.COLOR_CARD_BG);
        row.setMaximumSize(new Dimension(Short.MAX_VALUE, 75));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Col 1: ID, Name, Category
        JPanel infoPanel = new JPanel(new GridLayout(3, 1, 0, 1));
        infoPanel.setOpaque(false);
        
        JLabel lblId = new JLabel("ID: " + p.getId());
        lblId.setFont(GuiStyles.FONT_SMALL_BOLD);
        lblId.setForeground(GuiStyles.COLOR_TEXT_MUTED);
        
        JLabel lblName = new JLabel(p.getName());
        lblName.setFont(GuiStyles.FONT_BODY_BOLD);
        lblName.setForeground(GuiStyles.COLOR_TEXT_MAIN);

        JLabel lblCat = new JLabel(p.getCategory());
        lblCat.setFont(GuiStyles.FONT_SMALL);
        lblCat.setForeground(GuiStyles.COLOR_PRIMARY);

        infoPanel.add(lblId);
        infoPanel.add(lblName);
        infoPanel.add(lblCat);

        gbc.gridx = 0;
        gbc.weightx = 0.4;
        row.add(infoPanel, gbc);

        // Col 2: Price
        JLabel lblPrice = new JLabel(String.format("$%.2f", p.getPrice()));
        lblPrice.setFont(GuiStyles.FONT_SUBTITLE);
        lblPrice.setForeground(GuiStyles.COLOR_TEXT_MAIN);

        gbc.gridx = 1;
        gbc.weightx = 0.15;
        row.add(lblPrice, gbc);

        // Col 3: Stock Status Badge
        JLabel lblStock = new JLabel();
        lblStock.setFont(GuiStyles.FONT_BODY_BOLD);
        int qty = p.getStockQuantity();
        if (qty <= 0) {
            lblStock.setText("Out of Stock");
            lblStock.setForeground(GuiStyles.COLOR_DANGER);
        } else if (qty <= 3) {
            lblStock.setText("Low Stock (" + qty + ")");
            lblStock.setForeground(GuiStyles.COLOR_WARNING);
        } else {
            lblStock.setText("Stock: " + qty);
            lblStock.setForeground(GuiStyles.COLOR_SUCCESS);
        }

        gbc.gridx = 2;
        gbc.weightx = 0.15;
        row.add(lblStock, gbc);

        // Col 4: Actions Panel (Restock, Edit, Delete)
        JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actionsPanel.setOpaque(false);

        ModernButton btnRestock = new ModernButton("Restock");
        btnRestock.setPreferredSize(new Dimension(80, 28));
        btnRestock.setColors(GuiStyles.COLOR_BACKGROUND, GuiStyles.COLOR_BORDER, GuiStyles.COLOR_PRIMARY);
        btnRestock.setFont(GuiStyles.FONT_SMALL_BOLD);
        btnRestock.addActionListener(e -> showRestockDialog(p));

        ModernButton btnEdit = new ModernButton("Edit");
        btnEdit.setPreferredSize(new Dimension(65, 28));
        btnEdit.setColors(GuiStyles.COLOR_BACKGROUND, GuiStyles.COLOR_BORDER, GuiStyles.COLOR_TEXT_MAIN);
        btnEdit.setFont(GuiStyles.FONT_SMALL_BOLD);
        btnEdit.addActionListener(e -> showEditProductDialog(p));

        ModernButton btnDelete = new ModernButton("Delete");
        btnDelete.setPreferredSize(new Dimension(75, 28));
        btnDelete.setColors(GuiStyles.COLOR_BACKGROUND, GuiStyles.COLOR_BORDER, GuiStyles.COLOR_DANGER);
        btnDelete.setFont(GuiStyles.FONT_SMALL_BOLD);
        btnDelete.addActionListener(e -> deleteProductConfirm(p));

        actionsPanel.add(btnRestock);
        actionsPanel.add(btnEdit);
        actionsPanel.add(btnDelete);

        gbc.gridx = 3;
        gbc.weightx = 0.3;
        row.add(actionsPanel, gbc);

        return row;
    }

    private void showRestockDialog(Product p) {
        String qtyStr = JOptionPane.showInputDialog(this, "Enter restock quantity for " + p.getName() + ":", "Restock Product", JOptionPane.QUESTION_MESSAGE);
        if (qtyStr == null) return;

        try {
            int qty = Integer.parseInt(qtyStr.trim());
            if (qty <= 0) {
                JOptionPane.showMessageDialog(this, "Quantity must be a positive integer.", "Invalid Input", JOptionPane.ERROR_MESSAGE);
                return;
            }
            catalogService.restockProduct(p.getId(), qty);
            refreshInventory();
            onCatalogUpdated.run();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter a valid numeric integer value.", "Invalid Input", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteProductConfirm(Product p) {
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete product: " + p.getName() + "?", "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            if (catalogService.deleteProduct(p.getId())) {
                refreshInventory();
                onCatalogUpdated.run();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to delete product.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void showAddProductDialog() {
        JDialog dialog = new JDialog((JFrame) SwingUtilities.getWindowAncestor(this), "Add New Product", true);
        dialog.setSize(400, 500);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel formPanel = new JPanel(new GridLayout(7, 2, 10, 15));
        formPanel.setBorder(GuiStyles.getPadding(20, 20, 20, 20));
        formPanel.setBackground(Color.WHITE);

        JTextField txtId = new JTextField();
        JTextField txtName = new JTextField();
        JTextField txtDesc = new JTextField();
        JTextField txtPrice = new JTextField();
        JTextField txtCategory = new JTextField();
        JTextField txtStock = new JTextField();

        addFormField(formPanel, "Product ID:", txtId);
        addFormField(formPanel, "Product Name:", txtName);
        addFormField(formPanel, "Description:", txtDesc);
        addFormField(formPanel, "Unit Price ($):", txtPrice);
        addFormField(formPanel, "Category:", txtCategory);
        addFormField(formPanel, "Initial Stock:", txtStock);

        dialog.add(formPanel, BorderLayout.CENTER);

        ModernButton btnSave = new ModernButton("Save Product");
        btnSave.setPreferredSize(new Dimension(0, 44));
        btnSave.addActionListener(e -> {
            try {
                String id = txtId.getText().trim();
                String name = txtName.getText().trim();
                String desc = txtDesc.getText().trim();
                double price = Double.parseDouble(txtPrice.getText().trim());
                String cat = txtCategory.getText().trim();
                int stock = Integer.parseInt(txtStock.getText().trim());

                if (id.isEmpty() || name.isEmpty() || cat.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "All fields (except description) are required.", "Error", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                if (price < 0 || stock < 0) {
                    JOptionPane.showMessageDialog(dialog, "Price and Stock must be non-negative values.", "Error", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                Product newProduct = new Product(id, name, desc, price, cat, stock, id.toLowerCase());
                if (catalogService.addNewProduct(newProduct)) {
                    dialog.dispose();
                    refreshInventory();
                    onCatalogUpdated.run();
                } else {
                    JOptionPane.showMessageDialog(dialog, "Product ID already exists.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Please enter correct numeric values for Price and Stock.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBorder(GuiStyles.getPadding(10, 20, 20, 20));
        bottomPanel.setOpaque(false);
        bottomPanel.add(btnSave, BorderLayout.CENTER);
        dialog.add(bottomPanel, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }

    private void showEditProductDialog(Product p) {
        JDialog dialog = new JDialog((JFrame) SwingUtilities.getWindowAncestor(this), "Edit Product: " + p.getId(), true);
        dialog.setSize(400, 450);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel formPanel = new JPanel(new GridLayout(5, 2, 10, 15));
        formPanel.setBorder(GuiStyles.getPadding(20, 20, 20, 20));
        formPanel.setBackground(Color.WHITE);

        JTextField txtName = new JTextField(p.getName());
        JTextField txtDesc = new JTextField(p.getDescription());
        JTextField txtPrice = new JTextField(String.valueOf(p.getPrice()));
        JTextField txtCategory = new JTextField(p.getCategory());

        addFormField(formPanel, "Product Name:", txtName);
        addFormField(formPanel, "Description:", txtDesc);
        addFormField(formPanel, "Unit Price ($):", txtPrice);
        addFormField(formPanel, "Category:", txtCategory);

        dialog.add(formPanel, BorderLayout.CENTER);

        ModernButton btnSave = new ModernButton("Save Changes");
        btnSave.setPreferredSize(new Dimension(0, 44));
        btnSave.addActionListener(e -> {
            try {
                String name = txtName.getText().trim();
                String desc = txtDesc.getText().trim();
                double price = Double.parseDouble(txtPrice.getText().trim());
                String cat = txtCategory.getText().trim();

                if (name.isEmpty() || cat.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "Product Name and Category are required.", "Error", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                if (price < 0) {
                    JOptionPane.showMessageDialog(dialog, "Price must be non-negative.", "Error", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                Product updated = new Product(p.getId(), name, desc, price, cat, p.getStockQuantity(), p.getImageName());
                if (catalogService.updateProduct(updated)) {
                    dialog.dispose();
                    refreshInventory();
                    onCatalogUpdated.run();
                } else {
                    JOptionPane.showMessageDialog(dialog, "Failed to update product details.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Please enter correct decimal price format.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBorder(GuiStyles.getPadding(10, 20, 20, 20));
        bottomPanel.setOpaque(false);
        bottomPanel.add(btnSave, BorderLayout.CENTER);
        dialog.add(bottomPanel, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }

    private void addFormField(JPanel panel, String labelText, JTextField field) {
        JLabel label = new JLabel(labelText);
        label.setFont(GuiStyles.FONT_BODY_BOLD);
        label.setForeground(GuiStyles.COLOR_TEXT_MAIN);
        panel.add(label);

        field.setFont(GuiStyles.FONT_BODY);
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(GuiStyles.COLOR_BORDER),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
        panel.add(field);
    }
}
