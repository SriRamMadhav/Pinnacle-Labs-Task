package gui;

import models.Book;
import models.Member;
import models.Transaction;
import services.LibraryService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class DashboardPanel extends JPanel {
    private final LibraryService libraryService;

    // GUI Components that need updates
    private JLabel totalBooksVal;
    private JLabel totalMembersVal;
    private JLabel activeBorrowsVal;
    private JLabel overdueVal;
    private JTable overdueTable;
    private DefaultTableModel overdueTableModel;

    public DashboardPanel(LibraryService libraryService, MainFrame mainFrame) {
        this.libraryService = libraryService;
        
        setLayout(new BorderLayout(20, 20));
        setBackground(Theme.CONTENT_BG);
        setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));

        // --- Top Header ---
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Theme.CONTENT_BG);
        JLabel titleLabel = new JLabel("Granthalaya Dashboard");
        titleLabel.setFont(Theme.FONT_TITLE);
        titleLabel.setForeground(Theme.TEXT_MAIN);
        JLabel subtitleLabel = new JLabel("Real-time summary of national library operations and records");
        subtitleLabel.setFont(Theme.FONT_SUBTITLE);
        subtitleLabel.setForeground(Theme.TEXT_MUTED);
        headerPanel.add(titleLabel, BorderLayout.NORTH);
        headerPanel.add(subtitleLabel, BorderLayout.SOUTH);
        add(headerPanel, BorderLayout.NORTH);

        // --- Stats Grid Panel (4 Cards) ---
        JPanel statsPanel = new JPanel(new GridLayout(1, 4, 15, 0));
        statsPanel.setBackground(Theme.CONTENT_BG);

        JPanel card1 = createStatCard("TOTAL BOOKS", totalBooksVal = new JLabel("0"), "books registered", Theme.PRIMARY);
        JPanel card2 = createStatCard("TOTAL MEMBERS", totalMembersVal = new JLabel("0"), "active library patrons", Theme.SUCCESS);
        JPanel card3 = createStatCard("ACTIVE BORROWS", activeBorrowsVal = new JLabel("0"), "currently checked out", Theme.WARNING);
        JPanel card4 = createStatCard("OVERDUE BORROWS", overdueVal = new JLabel("0"), "require immediate return", Theme.DANGER);

        statsPanel.add(card1);
        statsPanel.add(card2);
        statsPanel.add(card3);
        statsPanel.add(card4);

        // --- Main Content Split Section ---
        JPanel contentGrid = new JPanel(new BorderLayout(20, 20));
        contentGrid.setBackground(Theme.CONTENT_BG);

        // Left Component: Overdue alerts table
        JPanel overdueAlertPanel = new JPanel(new BorderLayout(10, 10));
        overdueAlertPanel.setBackground(Theme.CARD_BG);
        overdueAlertPanel.setBorder(Theme.BORDER_CARD);

        JLabel alertTitle = new JLabel("Overdue & Pending Returns Alert");
        alertTitle.setFont(Theme.FONT_HEADER);
        alertTitle.setForeground(Theme.DANGER);
        overdueAlertPanel.add(alertTitle, BorderLayout.NORTH);

        String[] colNames = {"Tx ID", "Book", "Borrower", "Due Date"};
        overdueTableModel = new DefaultTableModel(colNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        overdueTable = new JTable(overdueTableModel);
        Theme.styleTable(overdueTable);
        
        JScrollPane scrollPane = new JScrollPane(overdueTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(Theme.BORDER, 1));
        overdueAlertPanel.add(scrollPane, BorderLayout.CENTER);

        // Right Component: Quick Actions
        JPanel quickActionsPanel = new JPanel(new GridBagLayout());
        quickActionsPanel.setBackground(Theme.CARD_BG);
        quickActionsPanel.setBorder(Theme.BORDER_CARD);
        quickActionsPanel.setPreferredSize(new Dimension(280, 0));

        JLabel actionsTitle = new JLabel("Quick Shortcuts");
        actionsTitle.setFont(Theme.FONT_HEADER);
        actionsTitle.setForeground(Theme.TEXT_MAIN);

        JLabel fineLabel = new JLabel("Overdue Fine: \u20B95 / Day");
        fineLabel.setFont(Theme.FONT_SUBTITLE);
        fineLabel.setForeground(Theme.DANGER);

        JButton btnIssue = Theme.createStyledButton("Issue / Return Books", Theme.PRIMARY, Color.WHITE);
        JButton btnAddBook = Theme.createStyledButton("Add New Book", Theme.SIDEBAR_BG, Color.WHITE);
        JButton btnAddMember = Theme.createStyledButton("Add New Member", Theme.SIDEBAR_BG, Color.WHITE);

        btnIssue.addActionListener(e -> mainFrame.showPanel("Transactions"));
        btnAddBook.addActionListener(e -> {
            mainFrame.showPanel("Books");
        });
        btnAddMember.addActionListener(e -> mainFrame.showPanel("Members"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.weightx = 1.0;

        gbc.gridy = 0;
        quickActionsPanel.add(actionsTitle, gbc);
        gbc.gridy = 1;
        gbc.insets = new Insets(2, 8, 12, 8);
        quickActionsPanel.add(fineLabel, gbc);
        gbc.gridy = 2;
        gbc.insets = new Insets(10, 8, 8, 8);
        quickActionsPanel.add(btnIssue, gbc);
        gbc.gridy = 3;
        gbc.insets = new Insets(8, 8, 8, 8);
        quickActionsPanel.add(btnAddBook, gbc);
        gbc.gridy = 4;
        quickActionsPanel.add(btnAddMember, gbc);

        // Spacer to push items to the top
        gbc.gridy = 5;
        gbc.weighty = 1.0;
        quickActionsPanel.add(new JLabel(""), gbc);

        contentGrid.add(overdueAlertPanel, BorderLayout.CENTER);
        contentGrid.add(quickActionsPanel, BorderLayout.EAST);

        // Add everything to layout
        JPanel centerWrapper = new JPanel(new BorderLayout(0, 20));
        centerWrapper.setBackground(Theme.CONTENT_BG);
        centerWrapper.add(statsPanel, BorderLayout.NORTH);
        centerWrapper.add(contentGrid, BorderLayout.CENTER);

        add(centerWrapper, BorderLayout.CENTER);

        // Initial Load
        refreshData();
    }

    private JPanel createStatCard(String labelStr, JLabel valLabel, String subtextStr, Color indicatorColor) {
        JPanel card = new JPanel(new BorderLayout(5, 5));
        card.setBackground(Theme.CARD_BG);
        card.setBorder(Theme.BORDER_CARD);

        // Left indicator line
        JPanel indicator = new JPanel();
        indicator.setBackground(indicatorColor);
        indicator.setPreferredSize(new Dimension(4, 0));

        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setBackground(Theme.CARD_BG);

        JLabel label = new JLabel(labelStr);
        label.setFont(Theme.FONT_CARD_LBL);
        label.setForeground(Theme.TEXT_MUTED);

        valLabel.setFont(Theme.FONT_CARD_VAL);
        valLabel.setForeground(Theme.TEXT_MAIN);

        JLabel subtext = new JLabel(subtextStr);
        subtext.setFont(Theme.FONT_SUBTITLE);
        subtext.setForeground(Theme.TEXT_MUTED);

        textPanel.add(label);
        textPanel.add(Box.createVerticalStrut(5));
        textPanel.add(valLabel);
        textPanel.add(Box.createVerticalStrut(5));
        textPanel.add(subtext);

        card.add(indicator, BorderLayout.WEST);
        card.add(textPanel, BorderLayout.CENTER);

        return card;
    }

    public final void refreshData() {
        totalBooksVal.setText(String.valueOf(libraryService.getTotalBooksCount()));
        totalMembersVal.setText(String.valueOf(libraryService.getTotalMembersCount()));
        activeBorrowsVal.setText(String.valueOf(libraryService.getActiveBorrowsCount()));
        overdueVal.setText(String.valueOf(libraryService.getOverdueCount()));

        // Populate Overdue table
        overdueTableModel.setRowCount(0);
        List<Transaction> overdueTxs = libraryService.getOverdueTransactions();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        for (Transaction t : overdueTxs) {
            Book book = libraryService.findBookById(t.getBookId());
            Member member = libraryService.findMemberById(t.getMemberId());
            String bookTitle = (book != null) ? book.getTitle() : t.getBookId();
            String memberName = (member != null) ? member.getName() : t.getMemberId();
            
            overdueTableModel.addRow(new Object[]{
                    t.getId(),
                    bookTitle,
                    memberName,
                    t.getDueDate().format(formatter)
            });
        }
    }
}
