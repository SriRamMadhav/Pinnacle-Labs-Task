package com.ecommerce.gui;

import com.ecommerce.model.Product;
import com.ecommerce.gui.GuiStyles.ModernButton;
import com.ecommerce.gui.GuiStyles.RoundedPanel;

import javax.swing.*;
import java.awt.*;

public class ProductCard extends RoundedPanel {
    private final Product product;
    private final ModernButton btnAddToCart;
    private final JLabel lblStockStatus;

    public interface AddToCartListener {
        void onAdd(Product product);
    }

    public ProductCard(Product product, AddToCartListener listener) {
        super(16); // 16px corner radius
        this.product = product;
        
        setLayout(new BorderLayout(10, 10));
        setBorder(GuiStyles.getPadding(15, 15, 15, 15));
        setPreferredSize(new Dimension(220, 260));

        // Add to Cart Button (Bottom) - Initialize first to prevent NullPointerException in updateStockLabel
        btnAddToCart = new ModernButton("Add to Cart");
        btnAddToCart.setPreferredSize(new Dimension(0, 36));
        btnAddToCart.setEnabled(product.getStockQuantity() > 0);
        btnAddToCart.addActionListener(e -> {
            if (listener != null) {
                listener.onAdd(product);
            }
        });

        // Category & Title Area (Top)
        JPanel topPanel = new JPanel();
        topPanel.setOpaque(false);
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));

        JLabel lblCategory = new JLabel(product.getCategory().toUpperCase());
        lblCategory.setFont(GuiStyles.FONT_SMALL_BOLD);
        lblCategory.setForeground(GuiStyles.COLOR_PRIMARY);
        lblCategory.setAlignmentX(Component.LEFT_ALIGNMENT);
        topPanel.add(lblCategory);
        topPanel.add(Box.createRigidArea(new Dimension(0, 4)));

        JLabel lblTitle = new JLabel(product.getName());
        lblTitle.setFont(GuiStyles.FONT_BODY_BOLD);
        lblTitle.setForeground(GuiStyles.COLOR_TEXT_MAIN);
        lblTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        topPanel.add(lblTitle);
        
        add(topPanel, BorderLayout.NORTH);

        // Description & Price (Center)
        JPanel centerPanel = new JPanel();
        centerPanel.setOpaque(false);
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));

        // Multi-line description using HTML
        JLabel lblDesc = new JLabel("<html><body style='width: 160px;'>" + product.getDescription() + "</body></html>");
        lblDesc.setFont(GuiStyles.FONT_SMALL);
        lblDesc.setForeground(GuiStyles.COLOR_TEXT_MUTED);
        lblDesc.setAlignmentX(Component.LEFT_ALIGNMENT);
        centerPanel.add(lblDesc);
        
        centerPanel.add(Box.createVerticalGlue());

        // Price & Stock
        JPanel priceStockPanel = new JPanel(new BorderLayout());
        priceStockPanel.setOpaque(false);
        priceStockPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblPrice = new JLabel(String.format("$%.2f", product.getPrice()));
        lblPrice.setFont(GuiStyles.FONT_SUBTITLE);
        lblPrice.setForeground(GuiStyles.COLOR_TEXT_MAIN);
        priceStockPanel.add(lblPrice, BorderLayout.WEST);

        lblStockStatus = new JLabel();
        lblStockStatus.setFont(GuiStyles.FONT_SMALL_BOLD);
        updateStockLabel();
        priceStockPanel.add(lblStockStatus, BorderLayout.EAST);

        centerPanel.add(Box.createRigidArea(new Dimension(0, 8)));
        centerPanel.add(priceStockPanel);

        add(centerPanel, BorderLayout.CENTER);
        add(btnAddToCart, BorderLayout.SOUTH);

        // Hover effect on the card
        addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                setBorderColor(GuiStyles.COLOR_PRIMARY);
                repaint();
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                setBorderColor(GuiStyles.COLOR_BORDER);
                repaint();
            }
        });
    }

    public void updateStockLabel() {
        int qty = product.getStockQuantity();
        if (qty <= 0) {
            lblStockStatus.setText("Out of Stock");
            lblStockStatus.setForeground(GuiStyles.COLOR_DANGER);
            btnAddToCart.setEnabled(false);
            btnAddToCart.setText("Out of Stock");
        } else if (qty <= 3) {
            lblStockStatus.setText("Only " + qty + " left!");
            lblStockStatus.setForeground(GuiStyles.COLOR_WARNING);
            btnAddToCart.setEnabled(true);
            btnAddToCart.setText("Add to Cart");
        } else {
            lblStockStatus.setText("In Stock (" + qty + ")");
            lblStockStatus.setForeground(GuiStyles.COLOR_SUCCESS);
            btnAddToCart.setEnabled(true);
            btnAddToCart.setText("Add to Cart");
        }
    }
}
