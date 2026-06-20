package com.ecommerce.gui;

import com.ecommerce.model.CartItem;
import com.ecommerce.model.Order;
import com.ecommerce.gui.GuiStyles.ModernButton;
import com.ecommerce.gui.GuiStyles.RoundedPanel;

import javax.swing.*;
import java.awt.*;

public class ReceiptDialog extends JDialog {

    public ReceiptDialog(JFrame parent, Order order) {
        super(parent, "Order Confirmation Receipt", true);
        
        setSize(420, 600);
        setLocationRelativeTo(parent);
        setResizable(false);
        setLayout(new BorderLayout());

        // Custom Main Content Panel
        JPanel contentPanel = new JPanel(new BorderLayout(15, 15));
        contentPanel.setBackground(GuiStyles.COLOR_BACKGROUND);
        contentPanel.setBorder(GuiStyles.getPadding(20, 20, 20, 20));

        // Invoice Receipt Paper Card
        RoundedPanel paper = new RoundedPanel(16);
        paper.setLayout(new BoxLayout(paper, BoxLayout.Y_AXIS));
        paper.setBorder(GuiStyles.getPadding(25, 25, 25, 25));
        paper.setBackground(Color.WHITE);

        // Header - Company branding
        JLabel lblStore = new JLabel("E-SHOPPING CORP");
        lblStore.setFont(GuiStyles.FONT_BODY_BOLD);
        lblStore.setForeground(GuiStyles.COLOR_PRIMARY);
        lblStore.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel lblReceipt = new JLabel("Payment Receipt");
        lblReceipt.setFont(GuiStyles.FONT_TITLE);
        lblReceipt.setForeground(GuiStyles.COLOR_TEXT_MAIN);
        lblReceipt.setAlignmentX(Component.CENTER_ALIGNMENT);

        paper.add(lblStore);
        paper.add(Box.createRigidArea(new Dimension(0, 5)));
        paper.add(lblReceipt);
        paper.add(Box.createRigidArea(new Dimension(0, 15)));

        // Dashed Separator
        paper.add(createDashedDivider());
        paper.add(Box.createRigidArea(new Dimension(0, 15)));

        // Order metadata
        JPanel metaPanel = new JPanel(new GridLayout(4, 2, 0, 8));
        metaPanel.setOpaque(false);
        metaPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        addMetaField(metaPanel, "Order ID:", order.getOrderId());
        addMetaField(metaPanel, "Date/Time:", order.getFormattedTimestamp());
        addMetaField(metaPanel, "Customer:", order.getCustomerName());
        addMetaField(metaPanel, "Contact:", order.getCustomerPhone());

        paper.add(metaPanel);
        paper.add(Box.createRigidArea(new Dimension(0, 15)));

        // Dashed Separator
        paper.add(createDashedDivider());
        paper.add(Box.createRigidArea(new Dimension(0, 15)));

        // Items list header
        JPanel listHeader = new JPanel(new BorderLayout());
        listHeader.setOpaque(false);
        listHeader.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel lblHeaderItem = new JLabel("Item description");
        lblHeaderItem.setFont(GuiStyles.FONT_SMALL_BOLD);
        lblHeaderItem.setForeground(GuiStyles.COLOR_TEXT_MUTED);
        JLabel lblHeaderTotal = new JLabel("Total");
        lblHeaderTotal.setFont(GuiStyles.FONT_SMALL_BOLD);
        lblHeaderTotal.setForeground(GuiStyles.COLOR_TEXT_MUTED);
        listHeader.add(lblHeaderItem, BorderLayout.WEST);
        listHeader.add(lblHeaderTotal, BorderLayout.EAST);
        paper.add(listHeader);
        paper.add(Box.createRigidArea(new Dimension(0, 8)));

        // Scrollable list items
        JPanel itemsContainer = new JPanel();
        itemsContainer.setLayout(new BoxLayout(itemsContainer, BoxLayout.Y_AXIS));
        itemsContainer.setOpaque(false);

        for (CartItem item : order.getItems()) {
            JPanel itemRow = new JPanel(new BorderLayout());
            itemRow.setOpaque(false);
            itemRow.setMaximumSize(new Dimension(Short.MAX_VALUE, 24));
            
            JLabel lblItemName = new JLabel(item.getProduct().getName() + " x" + item.getQuantity());
            lblItemName.setFont(GuiStyles.FONT_BODY);
            lblItemName.setForeground(GuiStyles.COLOR_TEXT_MAIN);
            
            JLabel lblItemSubtotal = new JLabel(String.format("$%.2f", item.getSubtotal()));
            lblItemSubtotal.setFont(GuiStyles.FONT_BODY);
            lblItemSubtotal.setForeground(GuiStyles.COLOR_TEXT_MAIN);
            
            itemRow.add(lblItemName, BorderLayout.WEST);
            itemRow.add(lblItemSubtotal, BorderLayout.EAST);
            itemsContainer.add(itemRow);
            itemsContainer.add(Box.createRigidArea(new Dimension(0, 4)));
        }

        JScrollPane itemsScroll = new JScrollPane(itemsContainer);
        itemsScroll.setBorder(null);
        itemsScroll.setOpaque(false);
        itemsScroll.getViewport().setOpaque(false);
        itemsScroll.setAlignmentX(Component.CENTER_ALIGNMENT);
        paper.add(itemsScroll);
        paper.add(Box.createRigidArea(new Dimension(0, 10)));

        // Dashed Separator
        paper.add(createDashedDivider());
        paper.add(Box.createRigidArea(new Dimension(0, 15)));

        // Pricing summary
        JPanel billingPanel = new JPanel(new GridLayout(4, 2, 0, 6));
        billingPanel.setOpaque(false);
        billingPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        addSummaryField(billingPanel, "Subtotal:", String.format("$%.2f", order.getSubtotal()), false);
        addSummaryField(billingPanel, "Sales Tax (8%):", String.format("$%.2f", order.getTax()), false);
        addSummaryField(billingPanel, "Discount:", String.format("-$%.2f", order.getDiscount()), true);
        addSummaryField(billingPanel, "Grand Total:", String.format("$%.2f", order.getTotal()), false);

        paper.add(billingPanel);
        paper.add(Box.createRigidArea(new Dimension(0, 15)));

        // Thank you note
        JLabel lblThankYou = new JLabel("Thank you for shopping with us!");
        lblThankYou.setFont(GuiStyles.FONT_SMALL_BOLD);
        lblThankYou.setForeground(GuiStyles.COLOR_PRIMARY);
        lblThankYou.setAlignmentX(Component.CENTER_ALIGNMENT);
        paper.add(lblThankYou);

        contentPanel.add(paper, BorderLayout.CENTER);

        // Close Action Button
        ModernButton btnClose = new ModernButton("Done");
        btnClose.setPreferredSize(new Dimension(0, 44));
        btnClose.addActionListener(e -> dispose());
        contentPanel.add(btnClose, BorderLayout.SOUTH);

        add(contentPanel, BorderLayout.CENTER);
    }

    private void addMetaField(JPanel panel, String label, String value) {
        JLabel lblField = new JLabel(label);
        lblField.setFont(GuiStyles.FONT_SMALL_BOLD);
        lblField.setForeground(GuiStyles.COLOR_TEXT_MUTED);
        
        JLabel lblValue = new JLabel(value);
        lblValue.setFont(GuiStyles.FONT_SMALL);
        lblValue.setForeground(GuiStyles.COLOR_TEXT_MAIN);
        lblValue.setHorizontalAlignment(SwingConstants.RIGHT);

        panel.add(lblField);
        panel.add(lblValue);
    }

    private void addSummaryField(JPanel panel, String label, String value, boolean isSuccessColor) {
        JLabel lblField = new JLabel(label);
        lblField.setFont(label.startsWith("Grand") ? GuiStyles.FONT_BODY_BOLD : GuiStyles.FONT_BODY);
        lblField.setForeground(GuiStyles.COLOR_TEXT_MAIN);

        JLabel lblValue = new JLabel(value);
        lblValue.setFont(label.startsWith("Grand") ? GuiStyles.FONT_SUBTITLE : GuiStyles.FONT_BODY_BOLD);
        lblValue.setForeground(isSuccessColor ? GuiStyles.COLOR_SUCCESS : 
                               (label.startsWith("Grand") ? GuiStyles.COLOR_PRIMARY : GuiStyles.COLOR_TEXT_MAIN));
        lblValue.setHorizontalAlignment(SwingConstants.RIGHT);

        panel.add(lblField);
        panel.add(lblValue);
    }

    private JComponent createDashedDivider() {
        return new JComponent() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setColor(GuiStyles.COLOR_BORDER);
                float[] dashed = {6f, 4f};
                g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1.0f, dashed, 0.0f));
                g2.drawLine(0, getHeight() / 2, getWidth(), getHeight() / 2);
            }

            @Override
            public Dimension getPreferredSize() {
                return new Dimension(0, 10);
            }

            @Override
            public Dimension getMaximumSize() {
                return new Dimension(Short.MAX_VALUE, 10);
            }
        };
    }
}
