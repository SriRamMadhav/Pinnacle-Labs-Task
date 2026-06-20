package com.ecommerce.gui;

import com.ecommerce.model.Order;
import com.ecommerce.model.Product;
import com.ecommerce.model.ShoppingCart;
import com.ecommerce.service.CatalogService;
import com.ecommerce.service.OrderService;
import com.ecommerce.gui.GuiStyles.ModernButton;
import com.ecommerce.gui.GuiStyles.RoundedPanel;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class MainFrame extends JFrame {
    private final CatalogService catalogService;
    private final OrderService orderService;
    private final ShoppingCart cart;

    private CardLayout cardLayout;
    private JPanel cardsPanel;

    // Sidebar navigation buttons
    private SidebarButton btnShopTab;
    private SidebarButton btnCartTab;
    private SidebarButton btnHistoryTab;
    private SidebarButton btnInventoryTab;
    private final List<SidebarButton> sidebarButtons = new ArrayList<>();

    // Panels
    private CatalogPanel catalogPanel;
    private CartPanel cartPanel;
    private OrderHistoryPanel historyPanel;
    private InventoryPanel inventoryPanel;

    public MainFrame(CatalogService catalogService, OrderService orderService) {
        this.catalogService = catalogService;
        this.orderService = orderService;
        this.cart = new ShoppingCart();

        setTitle("E-Commerce Shopping Cart System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 680);
        setMinimumSize(new Dimension(850, 580));
        setLocationRelativeTo(null);

        // Core Layout
        setLayout(new BorderLayout());

        // 1. Sidebar Panel (Left)
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(GuiStyles.COLOR_SIDEBAR_BG);
        sidebar.setPreferredSize(new Dimension(220, 0));
        sidebar.setBorder(GuiStyles.getPadding(25, 15, 25, 15));

        // Branding
        JLabel lblLogo = new JLabel("E-SHOPPING");
        lblLogo.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblLogo.setForeground(Color.WHITE);
        lblLogo.setAlignmentX(Component.CENTER_ALIGNMENT);
        sidebar.add(lblLogo);

        JLabel lblLogoSub = new JLabel("Cart System");
        lblLogoSub.setFont(GuiStyles.FONT_SMALL);
        lblLogoSub.setForeground(GuiStyles.COLOR_PRIMARY);
        lblLogoSub.setAlignmentX(Component.CENTER_ALIGNMENT);
        sidebar.add(lblLogoSub);

        sidebar.add(Box.createRigidArea(new Dimension(0, 40)));

        // Navigation Tab Buttons
        btnShopTab = new SidebarButton("Shop Catalog", "SHOP");
        btnCartTab = new SidebarButton("Shopping Cart", "CART");
        btnHistoryTab = new SidebarButton("Order History", "HISTORY");
        btnInventoryTab = new SidebarButton("Admin Inventory", "INVENTORY");

        sidebar.add(btnShopTab);
        sidebar.add(Box.createRigidArea(new Dimension(0, 10)));
        sidebar.add(btnCartTab);
        sidebar.add(Box.createRigidArea(new Dimension(0, 10)));
        sidebar.add(btnHistoryTab);
        sidebar.add(Box.createRigidArea(new Dimension(0, 10)));
        sidebar.add(btnInventoryTab);

        sidebar.add(Box.createVerticalGlue());

        // Footer version info
        JLabel lblVer = new JLabel("Version 1.0.0");
        lblVer.setFont(GuiStyles.FONT_SMALL);
        lblVer.setForeground(GuiStyles.COLOR_SIDEBAR_TEXT);
        lblVer.setAlignmentX(Component.CENTER_ALIGNMENT);
        sidebar.add(lblVer);

        add(sidebar, BorderLayout.WEST);

        // 2. Right Main Layout (CardLayout)
        cardLayout = new CardLayout();
        cardsPanel = new JPanel(cardLayout);
        cardsPanel.setBackground(GuiStyles.COLOR_BACKGROUND);

        // Instantiating panels
        catalogPanel = new CatalogPanel(catalogService, this::addToCartCallback);
        cartPanel = new CartPanel(cart, this::cartUpdatedCallback, this::triggerCheckoutCallback);
        historyPanel = new OrderHistoryPanel(this, orderService);
        inventoryPanel = new InventoryPanel(catalogService, this::catalogUpdatedCallback);

        cardsPanel.add(catalogPanel, "SHOP");
        cardsPanel.add(cartPanel, "CART");
        cardsPanel.add(historyPanel, "HISTORY");
        cardsPanel.add(inventoryPanel, "INVENTORY");

        add(cardsPanel, BorderLayout.CENTER);

        // Set Default active tab
        setActiveTab("SHOP");
        updateCartBadgeCount();
    }

    private void setActiveTab(String cardName) {
        cardLayout.show(cardsPanel, cardName);
        for (SidebarButton btn : sidebarButtons) {
            btn.setActive(btn.getCardName().equals(cardName));
        }
    }

    private void addToCartCallback(Product product) {
        boolean success = cart.addItem(product, 1);
        if (success) {
            updateCartBadgeCount();
            cartPanel.refreshCart();
            // Show custom micro-notification in bottom right or overlay rather than blocking alert
            // But since Swing standard, a quiet status toast is best. Or a non-blocking dialog.
            // Let's increment badge count and refresh catalog item count to feel dynamic.
            catalogPanel.refreshCatalog();
        } else {
            JOptionPane.showMessageDialog(this, 
                "Cannot add more of " + product.getName() + ".\nAvailable stock: " + product.getStockQuantity(), 
                "Out of Stock", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void cartUpdatedCallback() {
        updateCartBadgeCount();
        catalogPanel.updateStockDisplays();
    }

    private void catalogUpdatedCallback() {
        catalogPanel.updateCategoriesCombo();
        catalogPanel.refreshCatalog();
    }

    private void updateCartBadgeCount() {
        int count = cart.getTotalCount();
        if (count > 0) {
            btnCartTab.setText("Shopping Cart (" + count + ")");
        } else {
            btnCartTab.setText("Shopping Cart");
        }
    }

    private void triggerCheckoutCallback() {
        // Show Checkout details form dialog
        CheckoutFormDialog dialog = new CheckoutFormDialog(this);
        dialog.setVisible(true);
    }

    // Custom Sidebar Button with visual styling states
    private class SidebarButton extends JButton {
        private final String cardName;
        private boolean active = false;

        public SidebarButton(String text, String cardName) {
            super(text);
            this.cardName = cardName;
            sidebarButtons.add(this);

            setContentAreaFilled(false);
            setFocusPainted(false);
            setBorderPainted(false);
            setFont(GuiStyles.FONT_BODY_BOLD);
            setForeground(GuiStyles.COLOR_SIDEBAR_TEXT);
            setHorizontalAlignment(SwingConstants.LEFT);
            setBorder(GuiStyles.getPadding(10, 15, 10, 15));
            setCursor(new Cursor(Cursor.HAND_CURSOR));

            addActionListener(e -> setActiveTab(cardName));
            
            addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseEntered(java.awt.event.MouseEvent e) {
                    if (!active) {
                        setForeground(Color.WHITE);
                    }
                }

                @Override
                public void mouseExited(java.awt.event.MouseEvent e) {
                    if (!active) {
                        setForeground(GuiStyles.COLOR_SIDEBAR_TEXT);
                    }
                }
            });
        }

        public String getCardName() {
            return cardName;
        }

        public void setActive(boolean active) {
            this.active = active;
            setForeground(active ? Color.WHITE : GuiStyles.COLOR_SIDEBAR_TEXT);
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            if (active) {
                GuiStyles.applyAntialiasing(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setColor(GuiStyles.COLOR_SIDEBAR_ACTIVE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
            }
            super.paintComponent(g);
        }
    }

    // Checkout Form modal
    private class CheckoutFormDialog extends JDialog {
        private final JTextField txtName;
        private final JTextField txtEmail;
        private final JTextField txtPhone;

        public CheckoutFormDialog(JFrame parent) {
            super(parent, "Secure Checkout", true);
            setSize(380, 400);
            setLocationRelativeTo(parent);
            setResizable(false);
            setLayout(new BorderLayout());

            JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
            mainPanel.setBackground(Color.WHITE);
            mainPanel.setBorder(GuiStyles.getPadding(20, 20, 20, 20));

            // Form Title
            JLabel lblTitle = new JLabel("Checkout Information");
            lblTitle.setFont(GuiStyles.FONT_SUBTITLE);
            lblTitle.setForeground(GuiStyles.COLOR_TEXT_MAIN);
            mainPanel.add(lblTitle, BorderLayout.NORTH);

            // Fields container
            JPanel fieldsPanel = new JPanel(new GridLayout(3, 1, 0, 15));
            fieldsPanel.setOpaque(false);

            txtName = createField(fieldsPanel, "Full Name *:");
            txtEmail = createField(fieldsPanel, "Email Address:");
            txtPhone = createField(fieldsPanel, "Phone Number *:");

            mainPanel.add(fieldsPanel, BorderLayout.CENTER);

            // Checkout action buttons
            JPanel bottomPanel = new JPanel(new GridLayout(1, 2, 10, 0));
            bottomPanel.setOpaque(false);

            ModernButton btnCancel = new ModernButton("Cancel");
            btnCancel.setColors(GuiStyles.COLOR_BACKGROUND, GuiStyles.COLOR_BORDER, GuiStyles.COLOR_TEXT_MUTED);
            btnCancel.addActionListener(e -> dispose());

            ModernButton btnPay = new ModernButton("Place Order ($" + String.format("%.2f", cart.getTotal()) + ")");
            btnPay.addActionListener(e -> processCheckoutPayment());

            bottomPanel.add(btnCancel);
            bottomPanel.add(btnPay);

            mainPanel.add(bottomPanel, BorderLayout.SOUTH);
            add(mainPanel, BorderLayout.CENTER);
        }

        private JTextField createField(JPanel parent, String labelText) {
            JPanel wrapper = new JPanel(new BorderLayout(5, 5));
            wrapper.setOpaque(false);

            JLabel label = new JLabel(labelText);
            label.setFont(GuiStyles.FONT_BODY_BOLD);
            label.setForeground(GuiStyles.COLOR_TEXT_MAIN);
            wrapper.add(label, BorderLayout.NORTH);

            JTextField field = new JTextField();
            field.setFont(GuiStyles.FONT_BODY);
            field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(GuiStyles.COLOR_BORDER),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)
            ));
            wrapper.add(field, BorderLayout.CENTER);

            parent.add(wrapper);
            return field;
        }

        private void processCheckoutPayment() {
            String name = txtName.getText().trim();
            String email = txtEmail.getText().trim();
            String phone = txtPhone.getText().trim();

            if (name.isEmpty() || phone.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter your name and phone number.", 
                                              "Required Fields", JOptionPane.WARNING_MESSAGE);
                return;
            }

            try {
                // Checkout order through order service
                Order order = orderService.checkout(cart, name, email, phone);
                
                dispose(); // Close form modal
                
                // Show Invoice receipt modal
                ReceiptDialog receipt = new ReceiptDialog(MainFrame.this, order);
                receipt.setVisible(true);

                // Update app states
                updateCartBadgeCount();
                cartPanel.refreshCart();
                historyPanel.refreshHistory();
                inventoryPanel.refreshInventory();
                catalogPanel.refreshCatalog();
                
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Checkout Failed:\n" + ex.getMessage(), 
                                              "Checkout Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
