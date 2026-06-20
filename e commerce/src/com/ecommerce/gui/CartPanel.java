package com.ecommerce.gui;

import com.ecommerce.model.CartItem;
import com.ecommerce.model.ShoppingCart;
import com.ecommerce.gui.GuiStyles.ModernButton;
import com.ecommerce.gui.GuiStyles.RoundedPanel;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class CartPanel extends JPanel {
    private final ShoppingCart cart;
    private final CartUpdateListener updateListener;
    private final CheckoutTrigger checkoutTrigger;

    private JPanel itemsListPanel;
    private JLabel lblSubtotalVal;
    private JLabel lblTaxVal;
    private JLabel lblDiscountVal;
    private JLabel lblTotalVal;
    private JTextField txtCoupon;
    private JLabel lblCouponFeedback;
    private JScrollPane scrollPane;

    public interface CartUpdateListener {
        void onCartChanged();
    }

    public interface CheckoutTrigger {
        void onCheckoutTriggered();
    }

    public CartPanel(ShoppingCart cart, CartUpdateListener updateListener, CheckoutTrigger checkoutTrigger) {
        this.cart = cart;
        this.updateListener = updateListener;
        this.checkoutTrigger = checkoutTrigger;

        setLayout(new BorderLayout(20, 20));
        setBackground(GuiStyles.COLOR_BACKGROUND);
        setBorder(GuiStyles.getPadding(20, 20, 20, 20));

        initComponents();
        refreshCart();
    }

    private void initComponents() {
        // Center: Items List (Scrollable)
        itemsListPanel = new JPanel();
        itemsListPanel.setLayout(new BoxLayout(itemsListPanel, BoxLayout.Y_AXIS));
        itemsListPanel.setBackground(GuiStyles.COLOR_BACKGROUND);

        scrollPane = new JScrollPane(itemsListPanel);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.getVerticalScrollBar().setUnitIncrement(12);

        add(scrollPane, BorderLayout.CENTER);

        // East: Order Summary Card
        RoundedPanel summaryCard = new RoundedPanel(16);
        summaryCard.setLayout(new BoxLayout(summaryCard, BoxLayout.Y_AXIS));
        summaryCard.setBorder(GuiStyles.getPadding(20, 20, 20, 20));
        summaryCard.setPreferredSize(new Dimension(300, 0));
        summaryCard.setBackground(GuiStyles.COLOR_CARD_BG);

        // Header
        JLabel lblSummaryTitle = new JLabel("Order Summary");
        lblSummaryTitle.setFont(GuiStyles.FONT_SUBTITLE);
        lblSummaryTitle.setForeground(GuiStyles.COLOR_TEXT_MAIN);
        lblSummaryTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        summaryCard.add(lblSummaryTitle);
        summaryCard.add(Box.createRigidArea(new Dimension(0, 20)));

        // Details Panel
        JPanel detailsPanel = new JPanel(new GridLayout(4, 2, 0, 12));
        detailsPanel.setOpaque(false);
        detailsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblSubtotal = new JLabel("Subtotal");
        lblSubtotal.setFont(GuiStyles.FONT_BODY);
        lblSubtotal.setForeground(GuiStyles.COLOR_TEXT_MUTED);
        lblSubtotalVal = new JLabel("$0.00");
        lblSubtotalVal.setFont(GuiStyles.FONT_BODY_BOLD);
        lblSubtotalVal.setForeground(GuiStyles.COLOR_TEXT_MAIN);
        lblSubtotalVal.setHorizontalAlignment(SwingConstants.RIGHT);

        JLabel lblTax = new JLabel("Sales Tax (8%)");
        lblTax.setFont(GuiStyles.FONT_BODY);
        lblTax.setForeground(GuiStyles.COLOR_TEXT_MUTED);
        lblTaxVal = new JLabel("$0.00");
        lblTaxVal.setFont(GuiStyles.FONT_BODY_BOLD);
        lblTaxVal.setForeground(GuiStyles.COLOR_TEXT_MAIN);
        lblTaxVal.setHorizontalAlignment(SwingConstants.RIGHT);

        JLabel lblDiscount = new JLabel("Discount");
        lblDiscount.setFont(GuiStyles.FONT_BODY);
        lblDiscount.setForeground(GuiStyles.COLOR_TEXT_MUTED);
        lblDiscountVal = new JLabel("-$0.00");
        lblDiscountVal.setFont(GuiStyles.FONT_BODY_BOLD);
        lblDiscountVal.setForeground(GuiStyles.COLOR_SUCCESS);
        lblDiscountVal.setHorizontalAlignment(SwingConstants.RIGHT);

        JLabel lblTotal = new JLabel("Total");
        lblTotal.setFont(GuiStyles.FONT_BODY_BOLD);
        lblTotal.setForeground(GuiStyles.COLOR_TEXT_MAIN);
        lblTotalVal = new JLabel("$0.00");
        lblTotalVal.setFont(GuiStyles.FONT_SUBTITLE);
        lblTotalVal.setForeground(GuiStyles.COLOR_PRIMARY);
        lblTotalVal.setHorizontalAlignment(SwingConstants.RIGHT);

        detailsPanel.add(lblSubtotal);
        detailsPanel.add(lblSubtotalVal);
        detailsPanel.add(lblTax);
        detailsPanel.add(lblTaxVal);
        detailsPanel.add(lblDiscount);
        detailsPanel.add(lblDiscountVal);
        detailsPanel.add(lblTotal);
        detailsPanel.add(lblTotalVal);

        summaryCard.add(detailsPanel);
        summaryCard.add(Box.createRigidArea(new Dimension(0, 20)));

        // Separator line
        JSeparator sep = new JSeparator();
        sep.setMaximumSize(new Dimension(Short.MAX_VALUE, 1));
        sep.setForeground(GuiStyles.COLOR_BORDER);
        sep.setAlignmentX(Component.LEFT_ALIGNMENT);
        summaryCard.add(sep);
        summaryCard.add(Box.createRigidArea(new Dimension(0, 15)));

        // Coupon Section
        JLabel lblCoupon = new JLabel("Promo Code");
        lblCoupon.setFont(GuiStyles.FONT_SMALL_BOLD);
        lblCoupon.setForeground(GuiStyles.COLOR_TEXT_MAIN);
        lblCoupon.setAlignmentX(Component.LEFT_ALIGNMENT);
        summaryCard.add(lblCoupon);
        summaryCard.add(Box.createRigidArea(new Dimension(0, 8)));

        JPanel couponInputPanel = new JPanel(new BorderLayout(8, 0));
        couponInputPanel.setOpaque(false);
        couponInputPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        txtCoupon = new JTextField();
        txtCoupon.setFont(GuiStyles.FONT_BODY);
        txtCoupon.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(GuiStyles.COLOR_BORDER),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
        couponInputPanel.add(txtCoupon, BorderLayout.CENTER);

        ModernButton btnApplyCoupon = new ModernButton("Apply");
        btnApplyCoupon.setPreferredSize(new Dimension(70, 32));
        btnApplyCoupon.addActionListener(e -> applyPromoCode());
        couponInputPanel.add(btnApplyCoupon, BorderLayout.EAST);

        summaryCard.add(couponInputPanel);

        lblCouponFeedback = new JLabel(" ");
        lblCouponFeedback.setFont(GuiStyles.FONT_SMALL);
        lblCouponFeedback.setAlignmentX(Component.LEFT_ALIGNMENT);
        summaryCard.add(Box.createRigidArea(new Dimension(0, 4)));
        summaryCard.add(lblCouponFeedback);

        summaryCard.add(Box.createVerticalGlue());

        // Checkout Button
        ModernButton btnCheckout = new ModernButton("Proceed to Checkout");
        btnCheckout.setPreferredSize(new Dimension(0, 44));
        btnCheckout.setMaximumSize(new Dimension(Short.MAX_VALUE, 44));
        btnCheckout.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnCheckout.addActionListener(e -> {
            if (cart.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Your cart is empty. Add products before checking out.", 
                                              "Empty Cart", JOptionPane.WARNING_MESSAGE);
            } else if (checkoutTrigger != null) {
                checkoutTrigger.onCheckoutTriggered();
            }
        });
        summaryCard.add(btnCheckout);

        add(summaryCard, BorderLayout.EAST);
    }

    private void applyPromoCode() {
        String code = txtCoupon.getText();
        if (code == null || code.trim().isEmpty()) {
            lblCouponFeedback.setText("Enter a promo code.");
            lblCouponFeedback.setForeground(GuiStyles.COLOR_DANGER);
            return;
        }

        boolean success = cart.applyCoupon(code);
        if (success) {
            lblCouponFeedback.setText("Coupon applied: " + Math.round(cart.getDiscountPercentage() * 100) + "% Off!");
            lblCouponFeedback.setForeground(GuiStyles.COLOR_SUCCESS);
            txtCoupon.setText("");
            updateTotals();
            if (updateListener != null) {
                updateListener.onCartChanged();
            }
        } else {
            lblCouponFeedback.setText("Invalid coupon code.");
            lblCouponFeedback.setForeground(GuiStyles.COLOR_DANGER);
        }
    }

    private void updateTotals() {
        lblSubtotalVal.setText(String.format("$%.2f", cart.getSubtotal()));
        lblTaxVal.setText(String.format("$%.2f", cart.getTaxAmount()));
        lblDiscountVal.setText(String.format("-$%.2f", cart.getDiscountAmount()));
        lblTotalVal.setText(String.format("$%.2f", cart.getTotal()));
    }

    public void refreshCart() {
        itemsListPanel.removeAll();
        lblCouponFeedback.setText(" ");

        if (cart.isEmpty()) {
            JPanel emptyPanel = new JPanel(new GridBagLayout());
            emptyPanel.setOpaque(false);
            
            JLabel lblEmpty = new JLabel("Your shopping cart is empty");
            lblEmpty.setFont(GuiStyles.FONT_SUBTITLE);
            lblEmpty.setForeground(GuiStyles.COLOR_TEXT_MUTED);
            
            emptyPanel.add(lblEmpty);
            itemsListPanel.setLayout(new BorderLayout());
            itemsListPanel.add(emptyPanel, BorderLayout.CENTER);
        } else {
            itemsListPanel.setLayout(new BoxLayout(itemsListPanel, BoxLayout.Y_AXIS));
            
            // Render rows of cart items
            for (CartItem item : new ArrayList<>(cart.getItems())) {
                itemsListPanel.add(createCartItemRow(item));
                itemsListPanel.add(Box.createRigidArea(new Dimension(0, 10))); // spacer
            }
        }

        updateTotals();
        itemsListPanel.revalidate();
        itemsListPanel.repaint();
    }

    private JPanel createCartItemRow(CartItem item) {
        RoundedPanel row = new RoundedPanel(12);
        row.setLayout(new GridBagLayout());
        row.setBorder(GuiStyles.getPadding(12, 15, 12, 15));
        row.setBackground(GuiStyles.COLOR_CARD_BG);
        row.setMaximumSize(new Dimension(Short.MAX_VALUE, 80));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Column 1: Details (Name & Category)
        JPanel detailsPanel = new JPanel(new GridLayout(2, 1, 0, 2));
        detailsPanel.setOpaque(false);
        JLabel lblName = new JLabel(item.getProduct().getName());
        lblName.setFont(GuiStyles.FONT_BODY_BOLD);
        lblName.setForeground(GuiStyles.COLOR_TEXT_MAIN);
        
        JLabel lblCat = new JLabel(item.getProduct().getCategory());
        lblCat.setFont(GuiStyles.FONT_SMALL);
        lblCat.setForeground(GuiStyles.COLOR_TEXT_MUTED);
        
        detailsPanel.add(lblName);
        detailsPanel.add(lblCat);

        gbc.gridx = 0;
        gbc.weightx = 0.5;
        row.add(detailsPanel, gbc);

        // Column 2: Unit Price
        JLabel lblPrice = new JLabel(String.format("$%.2f", item.getProduct().getPrice()));
        lblPrice.setFont(GuiStyles.FONT_BODY);
        lblPrice.setForeground(GuiStyles.COLOR_TEXT_MAIN);
        
        gbc.gridx = 1;
        gbc.weightx = 0.15;
        row.add(lblPrice, gbc);

        // Column 3: Quantity Controls
        JPanel qtyPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
        qtyPanel.setOpaque(false);

        ModernButton btnMinus = new ModernButton("-");
        btnMinus.setPreferredSize(new Dimension(28, 28));
        btnMinus.setColors(GuiStyles.COLOR_BACKGROUND, GuiStyles.COLOR_BORDER, GuiStyles.COLOR_TEXT_MAIN);
        btnMinus.addActionListener(e -> {
            int currentQty = item.getQuantity();
            if (cart.updateQuantity(item.getProduct().getId(), currentQty - 1)) {
                refreshCart();
                if (updateListener != null) updateListener.onCartChanged();
            }
        });

        JLabel lblQty = new JLabel(String.valueOf(item.getQuantity()), SwingConstants.CENTER);
        lblQty.setFont(GuiStyles.FONT_BODY_BOLD);
        lblQty.setPreferredSize(new Dimension(30, 28));

        ModernButton btnPlus = new ModernButton("+");
        btnPlus.setPreferredSize(new Dimension(28, 28));
        btnPlus.setColors(GuiStyles.COLOR_BACKGROUND, GuiStyles.COLOR_BORDER, GuiStyles.COLOR_TEXT_MAIN);
        btnPlus.addActionListener(e -> {
            int currentQty = item.getQuantity();
            if (cart.updateQuantity(item.getProduct().getId(), currentQty + 1)) {
                refreshCart();
                if (updateListener != null) updateListener.onCartChanged();
            } else {
                JOptionPane.showMessageDialog(this, "Cannot add more items. Insufficient stock limit of " + 
                                              item.getProduct().getStockQuantity() + " reached.", 
                                              "Out of Stock", JOptionPane.WARNING_MESSAGE);
            }
        });

        qtyPanel.add(btnMinus);
        qtyPanel.add(lblQty);
        qtyPanel.add(btnPlus);

        gbc.gridx = 2;
        gbc.weightx = 0.2;
        row.add(qtyPanel, gbc);

        // Column 4: Subtotal
        JLabel lblSub = new JLabel(String.format("$%.2f", item.getSubtotal()));
        lblSub.setFont(GuiStyles.FONT_BODY_BOLD);
        lblSub.setForeground(GuiStyles.COLOR_TEXT_MAIN);
        lblSub.setHorizontalAlignment(SwingConstants.RIGHT);

        gbc.gridx = 3;
        gbc.weightx = 0.15;
        row.add(lblSub, gbc);

        // Column 5: Remove Button
        ModernButton btnRemove = new ModernButton("Remove");
        btnRemove.setPreferredSize(new Dimension(75, 28));
        btnRemove.setColors(GuiStyles.COLOR_BACKGROUND, GuiStyles.COLOR_BORDER, GuiStyles.COLOR_DANGER);
        btnRemove.setFont(GuiStyles.FONT_SMALL_BOLD);
        btnRemove.addActionListener(e -> {
            cart.removeItem(item.getProduct().getId());
            refreshCart();
            if (updateListener != null) updateListener.onCartChanged();
        });

        gbc.gridx = 4;
        gbc.weightx = 0.1;
        gbc.insets = new Insets(0, 10, 0, 0);
        row.add(btnRemove, gbc);

        return row;
    }
}
