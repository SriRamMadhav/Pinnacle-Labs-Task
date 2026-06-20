package com.ecommerce.gui;

import com.ecommerce.model.Product;
import com.ecommerce.service.CatalogService;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class CatalogPanel extends JPanel {
    private final CatalogService catalogService;
    private final ProductCard.AddToCartListener addToCartListener;

    private JTextField txtSearch;
    private JComboBox<String> cbCategory;
    private JPanel gridPanel;
    private JScrollPane scrollPane;
    private final List<ProductCard> activeCards;

    public CatalogPanel(CatalogService catalogService, ProductCard.AddToCartListener addToCartListener) {
        this.catalogService = catalogService;
        this.addToCartListener = addToCartListener;
        this.activeCards = new ArrayList<>();

        setLayout(new BorderLayout(15, 15));
        setBackground(GuiStyles.COLOR_BACKGROUND);
        setBorder(GuiStyles.getPadding(20, 20, 20, 20));

        // Create UI components
        initComponents();
        
        // Initial load
        refreshCatalog();
    }

    private void initComponents() {
        // Top Filter Bar Panel
        JPanel filterBar = new JPanel(new BorderLayout(15, 15));
        filterBar.setOpaque(false);

        // Search Panel (Left)
        JPanel searchPanel = new JPanel(new BorderLayout(8, 0));
        searchPanel.setOpaque(false);
        
        JLabel lblSearch = new JLabel("Search:");
        lblSearch.setFont(GuiStyles.FONT_BODY_BOLD);
        lblSearch.setForeground(GuiStyles.COLOR_TEXT_MAIN);
        searchPanel.add(lblSearch, BorderLayout.WEST);

        txtSearch = new JTextField(20);
        txtSearch.setFont(GuiStyles.FONT_BODY);
        txtSearch.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(GuiStyles.COLOR_BORDER, 1),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { refreshCatalog(); }
            @Override public void removeUpdate(DocumentEvent e) { refreshCatalog(); }
            @Override public void changedUpdate(DocumentEvent e) { refreshCatalog(); }
        });
        searchPanel.add(txtSearch, BorderLayout.CENTER);
        filterBar.add(searchPanel, BorderLayout.CENTER);

        // Category Filter Panel (Right)
        JPanel catPanel = new JPanel(new BorderLayout(8, 0));
        catPanel.setOpaque(false);

        JLabel lblCat = new JLabel("Category:");
        lblCat.setFont(GuiStyles.FONT_BODY_BOLD);
        lblCat.setForeground(GuiStyles.COLOR_TEXT_MAIN);
        catPanel.add(lblCat, BorderLayout.WEST);

        cbCategory = new JComboBox<>();
        cbCategory.setFont(GuiStyles.FONT_BODY);
        cbCategory.setBackground(Color.WHITE);
        updateCategoriesCombo();
        cbCategory.addActionListener(e -> refreshCatalog());
        catPanel.add(cbCategory, BorderLayout.CENTER);
        
        filterBar.add(catPanel, BorderLayout.EAST);

        add(filterBar, BorderLayout.NORTH);

        // Grid Panel for Products
        gridPanel = new JPanel();
        gridPanel.setOpaque(false);
        // Custom wrap layout or FlowLayout with left alignment
        gridPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 20, 20));

        scrollPane = new JScrollPane(gridPanel);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        add(scrollPane, BorderLayout.CENTER);
    }

    public void updateCategoriesCombo() {
        cbCategory.removeAllItems();
        for (String cat : catalogService.getCategories()) {
            cbCategory.addItem(cat);
        }
    }

    public void refreshCatalog() {
        gridPanel.removeAll();
        activeCards.clear();

        String query = txtSearch.getText();
        String category = (String) cbCategory.getSelectedItem();

        List<Product> products = catalogService.searchAndFilter(query, category);

        if (products.isEmpty()) {
            JPanel emptyPanel = new JPanel(new GridBagLayout());
            emptyPanel.setOpaque(false);
            JLabel lblEmpty = new JLabel("No products found matching the criteria.");
            lblEmpty.setFont(GuiStyles.FONT_SUBTITLE);
            lblEmpty.setForeground(GuiStyles.COLOR_TEXT_MUTED);
            emptyPanel.add(lblEmpty);
            gridPanel.setLayout(new BorderLayout());
            gridPanel.add(emptyPanel, BorderLayout.CENTER);
        } else {
            // Re-assert FlowLayout for items
            gridPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 20, 20));
            for (Product p : products) {
                ProductCard card = new ProductCard(p, addToCartListener);
                gridPanel.add(card);
                activeCards.add(card);
            }
        }

        gridPanel.revalidate();
        gridPanel.repaint();
    }

    /**
     * Refreshes stock displays across all visible cards
     */
    public void updateStockDisplays() {
        for (ProductCard card : activeCards) {
            card.updateStockLabel();
        }
        gridPanel.repaint();
    }
}
