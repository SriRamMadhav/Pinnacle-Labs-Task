package gui;

import services.LibraryService;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Map;

public class MainFrame extends JFrame {
    private final LibraryService libraryService;
    private final CardLayout cardLayout;
    private final JPanel containerPanel;
    
    // Panels
    private final DashboardPanel dashboardPanel;
    private final BookPanel bookPanel;
    private final MemberPanel memberPanel;
    private final TransactionPanel transactionPanel;

    // Sidebar navigation buttons
    private final Map<String, JButton> navButtons = new HashMap<>();
    private String activeCard = "Dashboard";

    public MainFrame(LibraryService libraryService) {
        this.libraryService = libraryService;
        
        setTitle("Bharat Granthalaya - Library Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 700);
        setMinimumSize(new Dimension(900, 600));
        setLocationRelativeTo(null); // Center on screen
        
        // Main split layout: Sidebar (West) + Card Container (Center)
        setLayout(new BorderLayout());

        // Create Container Panel (CardLayout)
        cardLayout = new CardLayout();
        containerPanel = new JPanel(cardLayout);
        containerPanel.setBackground(Theme.CONTENT_BG);

        // Initialize Child Panels
        dashboardPanel = new DashboardPanel(libraryService, this);
        bookPanel = new BookPanel(libraryService);
        memberPanel = new MemberPanel(libraryService);
        transactionPanel = new TransactionPanel(libraryService);

        containerPanel.add(dashboardPanel, "Dashboard");
        containerPanel.add(bookPanel, "Books");
        containerPanel.add(memberPanel, "Members");
        containerPanel.add(transactionPanel, "Transactions");

        // Create Sidebar
        JPanel sidebar = createSidebar();

        // Add to Frame
        add(sidebar, BorderLayout.WEST);
        add(containerPanel, BorderLayout.CENTER);

        // Highlight the initial nav button
        updateNavSelection();
    }

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BorderLayout());
        sidebar.setBackground(Theme.SIDEBAR_BG);
        sidebar.setPreferredSize(new Dimension(240, 0));

        // --- Logo Panel at the Top ---
        JPanel logoPanel = new JPanel(new BorderLayout());
        logoPanel.setBackground(Theme.SIDEBAR_ACTIVE);
        logoPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel titleLabel = new JLabel("Bharat Granthalaya");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(Color.WHITE);
        
        JLabel subtitleLabel = new JLabel("National Library Portal");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        subtitleLabel.setForeground(Theme.TEXT_MUTED);

        logoPanel.add(titleLabel, BorderLayout.NORTH);
        logoPanel.add(subtitleLabel, BorderLayout.SOUTH);
        sidebar.add(logoPanel, BorderLayout.NORTH);

        // --- Navigation Buttons Panel (Center) ---
        JPanel navPanel = new JPanel();
        navPanel.setLayout(new BoxLayout(navPanel, BoxLayout.Y_AXIS));
        navPanel.setBackground(Theme.SIDEBAR_BG);
        navPanel.setBorder(BorderFactory.createEmptyBorder(20, 10, 20, 10));

        navPanel.add(createNavButton("Dashboard", "Dashboard"));
        navPanel.add(Box.createVerticalStrut(10));
        navPanel.add(createNavButton("Books Catalog", "Books"));
        navPanel.add(Box.createVerticalStrut(10));
        navPanel.add(createNavButton("Members Database", "Members"));
        navPanel.add(Box.createVerticalStrut(10));
        navPanel.add(createNavButton("Checkouts & Returns", "Transactions"));

        sidebar.add(navPanel, BorderLayout.CENTER);

        // --- Footer Panel at the Bottom ---
        JPanel footerPanel = new JPanel(new BorderLayout());
        footerPanel.setBackground(Theme.SIDEBAR_BG);
        footerPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        JLabel lblVersion = new JLabel("v1.0.0 (Java 21)");
        lblVersion.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblVersion.setForeground(Theme.TEXT_MUTED);
        
        JLabel lblStatus = new JLabel("System: Online");
        lblStatus.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblStatus.setForeground(Theme.SUCCESS);

        footerPanel.add(lblVersion, BorderLayout.NORTH);
        footerPanel.add(lblStatus, BorderLayout.SOUTH);
        sidebar.add(footerPanel, BorderLayout.SOUTH);

        return sidebar;
    }

    private JButton createNavButton(String text, String cardName) {
        JButton btn = new JButton(text);
        btn.setFont(Theme.FONT_HEADER);
        btn.setForeground(Theme.TEXT_LIGHT);
        btn.setBackground(Theme.SIDEBAR_BG);
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setMaximumSize(new Dimension(220, 40));
        btn.setPreferredSize(new Dimension(220, 40));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(true);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 15));

        btn.addActionListener(e -> showPanel(cardName));

        // Hover Effect
        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (!cardName.equals(activeCard)) {
                    btn.setBackground(Theme.SIDEBAR_HOVER);
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (!cardName.equals(activeCard)) {
                    btn.setBackground(Theme.SIDEBAR_BG);
                }
            }
        });

        navButtons.put(cardName, btn);
        return btn;
    }

    public void showPanel(String cardName) {
        activeCard = cardName;
        cardLayout.show(containerPanel, cardName);
        updateNavSelection();

        // Refresh destination panel data dynamically on switch
        switch (cardName) {
            case "Dashboard":
                dashboardPanel.refreshData();
                break;
            case "Books":
                bookPanel.refreshTable("");
                break;
            case "Members":
                memberPanel.refreshTable("");
                break;
            case "Transactions":
                transactionPanel.refreshTable("");
                break;
        }
    }

    private void updateNavSelection() {
        for (Map.Entry<String, JButton> entry : navButtons.entrySet()) {
            String cardName = entry.getKey();
            JButton btn = entry.getValue();

            if (cardName.equals(activeCard)) {
                btn.setBackground(Theme.SIDEBAR_ACTIVE);
                btn.setForeground(Color.WHITE);
                btn.setFont(Theme.FONT_HEADER);
            } else {
                btn.setBackground(Theme.SIDEBAR_BG);
                btn.setForeground(Theme.TEXT_LIGHT);
                btn.setFont(Theme.FONT_HEADER);
            }
        }
    }
}
