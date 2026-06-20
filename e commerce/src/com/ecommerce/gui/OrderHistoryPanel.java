package com.ecommerce.gui;

import com.ecommerce.model.Order;
import com.ecommerce.service.OrderService;
import com.ecommerce.gui.GuiStyles.ModernButton;
import com.ecommerce.gui.GuiStyles.RoundedPanel;

import javax.swing.*;
import java.awt.*;
import java.util.Collections;
import java.util.List;

public class OrderHistoryPanel extends JPanel {
    private final OrderService orderService;
    private final JFrame parentFrame;
    private JPanel historyListPanel;

    public OrderHistoryPanel(JFrame parentFrame, OrderService orderService) {
        this.parentFrame = parentFrame;
        this.orderService = orderService;

        setLayout(new BorderLayout(15, 15));
        setBackground(GuiStyles.COLOR_BACKGROUND);
        setBorder(GuiStyles.getPadding(20, 20, 20, 20));

        // Header Title
        JLabel lblTitle = new JLabel("Order History Log");
        lblTitle.setFont(GuiStyles.FONT_TITLE);
        lblTitle.setForeground(GuiStyles.COLOR_TEXT_MAIN);
        add(lblTitle, BorderLayout.NORTH);

        // List Container
        historyListPanel = new JPanel();
        historyListPanel.setLayout(new BoxLayout(historyListPanel, BoxLayout.Y_AXIS));
        historyListPanel.setBackground(GuiStyles.COLOR_BACKGROUND);

        JScrollPane scrollPane = new JScrollPane(historyListPanel);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.getVerticalScrollBar().setUnitIncrement(14);
        add(scrollPane, BorderLayout.CENTER);

        refreshHistory();
    }

    public void refreshHistory() {
        historyListPanel.removeAll();

        List<Order> orders = orderService.getOrderHistory();
        // Sort newest first
        Collections.reverse(orders);

        if (orders.isEmpty()) {
            JPanel emptyPanel = new JPanel(new GridBagLayout());
            emptyPanel.setOpaque(false);

            JLabel lblEmpty = new JLabel("No order transactions logged yet.");
            lblEmpty.setFont(GuiStyles.FONT_SUBTITLE);
            lblEmpty.setForeground(GuiStyles.COLOR_TEXT_MUTED);

            emptyPanel.add(lblEmpty);
            historyListPanel.setLayout(new BorderLayout());
            historyListPanel.add(emptyPanel, BorderLayout.CENTER);
        } else {
            historyListPanel.setLayout(new BoxLayout(historyListPanel, BoxLayout.Y_AXIS));

            for (Order order : orders) {
                historyListPanel.add(createOrderRow(order));
                historyListPanel.add(Box.createRigidArea(new Dimension(0, 10)));
            }
        }

        historyListPanel.revalidate();
        historyListPanel.repaint();
    }

    private JPanel createOrderRow(Order order) {
        RoundedPanel row = new RoundedPanel(12);
        row.setLayout(new GridBagLayout());
        row.setBorder(GuiStyles.getPadding(15, 20, 15, 20));
        row.setBackground(GuiStyles.COLOR_CARD_BG);
        row.setMaximumSize(new Dimension(Short.MAX_VALUE, 75));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Col 1: Order ID & Time
        JPanel leftPanel = new JPanel(new GridLayout(2, 1, 0, 2));
        leftPanel.setOpaque(false);
        JLabel lblId = new JLabel(order.getOrderId());
        lblId.setFont(GuiStyles.FONT_BODY_BOLD);
        lblId.setForeground(GuiStyles.COLOR_TEXT_MAIN);

        JLabel lblTime = new JLabel(order.getFormattedTimestamp());
        lblTime.setFont(GuiStyles.FONT_SMALL);
        lblTime.setForeground(GuiStyles.COLOR_TEXT_MUTED);

        leftPanel.add(lblId);
        leftPanel.add(lblTime);

        gbc.gridx = 0;
        gbc.weightx = 0.35;
        row.add(leftPanel, gbc);

        // Col 2: Customer Details
        JPanel centerPanel = new JPanel(new GridLayout(2, 1, 0, 2));
        centerPanel.setOpaque(false);
        JLabel lblCustomerName = new JLabel(order.getCustomerName());
        lblCustomerName.setFont(GuiStyles.FONT_BODY);
        lblCustomerName.setForeground(GuiStyles.COLOR_TEXT_MAIN);

        JLabel lblEmail = new JLabel(order.getCustomerEmail());
        lblEmail.setFont(GuiStyles.FONT_SMALL);
        lblEmail.setForeground(GuiStyles.COLOR_TEXT_MUTED);

        centerPanel.add(lblCustomerName);
        centerPanel.add(lblEmail);

        gbc.gridx = 1;
        gbc.weightx = 0.35;
        row.add(centerPanel, gbc);

        // Col 3: Grand Total Amount
        JLabel lblTotal = new JLabel(String.format("$%.2f", order.getTotal()));
        lblTotal.setFont(GuiStyles.FONT_SUBTITLE);
        lblTotal.setForeground(GuiStyles.COLOR_PRIMARY);
        lblTotal.setHorizontalAlignment(SwingConstants.RIGHT);

        gbc.gridx = 2;
        gbc.weightx = 0.15;
        gbc.insets = new Insets(0, 0, 0, 15);
        row.add(lblTotal, gbc);

        // Col 4: View Receipt Button
        ModernButton btnReceipt = new ModernButton("Receipt");
        btnReceipt.setPreferredSize(new Dimension(85, 32));
        btnReceipt.setColors(GuiStyles.COLOR_BACKGROUND, GuiStyles.COLOR_BORDER, GuiStyles.COLOR_TEXT_MAIN);
        btnReceipt.setFont(GuiStyles.FONT_SMALL_BOLD);
        btnReceipt.addActionListener(e -> {
            ReceiptDialog dialog = new ReceiptDialog(parentFrame, order);
            dialog.setVisible(true);
        });

        gbc.gridx = 3;
        gbc.weightx = 0.15;
        gbc.insets = new Insets(0, 0, 0, 0);
        row.add(btnReceipt, gbc);

        return row;
    }
}
