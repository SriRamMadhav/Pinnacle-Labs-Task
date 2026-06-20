package gui;

import models.Book;
import models.Member;
import models.Transaction;
import services.LibraryService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class TransactionPanel extends JPanel {
    private final LibraryService libraryService;
    private final JTable table;
    private final DefaultTableModel tableModel;
    private final JTextField txtSearch;

    public TransactionPanel(LibraryService libraryService) {
        this.libraryService = libraryService;

        // Initialize Table first to prevent compiler errors due to early capture in action listeners
        String[] columnNames = {"Tx ID", "Book Title", "Member Name", "Issue Date", "Due Date", "Return Date", "Status"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(tableModel);
        Theme.styleTable(table);

        setLayout(new BorderLayout(20, 20));
        setBackground(Theme.CONTENT_BG);
        setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));

        // --- Header Panel ---
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Theme.CONTENT_BG);
        
        JLabel titleLabel = new JLabel("Book Checkout & Returns");
        titleLabel.setFont(Theme.FONT_TITLE);
        titleLabel.setForeground(Theme.TEXT_MAIN);
        JLabel subtitleLabel = new JLabel("Issue books to members, process returns, and track transaction logs");
        subtitleLabel.setFont(Theme.FONT_SUBTITLE);
        subtitleLabel.setForeground(Theme.TEXT_MUTED);
        
        headerPanel.add(titleLabel, BorderLayout.NORTH);
        headerPanel.add(subtitleLabel, BorderLayout.SOUTH);
        add(headerPanel, BorderLayout.NORTH);

        // --- Controls Panel (Search & Actions) ---
        JPanel controlsPanel = new JPanel(new BorderLayout(15, 0));
        controlsPanel.setBackground(Theme.CONTENT_BG);

        // Search panel
        JPanel searchPanel = new JPanel(new BorderLayout(5, 0));
        searchPanel.setBackground(Theme.CONTENT_BG);
        JLabel lblSearch = new JLabel("Search: ");
        lblSearch.setFont(Theme.FONT_BOLD);
        lblSearch.setForeground(Theme.TEXT_MAIN);
        txtSearch = new JTextField();
        txtSearch.setFont(Theme.FONT_BODY);
        txtSearch.setBorder(Theme.BORDER_INPUT);
        txtSearch.setPreferredSize(new Dimension(250, 32));
        txtSearch.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent e) {
                refreshTable(txtSearch.getText());
            }
        });
        searchPanel.add(lblSearch, BorderLayout.WEST);
        searchPanel.add(txtSearch, BorderLayout.CENTER);

        // Actions panel
        JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionsPanel.setBackground(Theme.CONTENT_BG);

        JButton btnIssue = Theme.createStyledButton("Issue Book", Theme.PRIMARY, Color.WHITE);
        JButton btnReturn = Theme.createStyledButton("Return Book", Theme.SUCCESS, Color.WHITE);

        btnIssue.addActionListener(e -> showIssueDialog());
        btnReturn.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Please select an active checkout transaction to return.", "No Transaction Selected", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String txId = table.getValueAt(selectedRow, 0).toString();
            Transaction tx = libraryService.findTransactionById(txId);
            if (tx != null) {
                if ("RETURNED".equals(tx.getStatus())) {
                    JOptionPane.showMessageDialog(this, "This book has already been returned.", "Already Returned", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                
                int confirm = JOptionPane.showConfirmDialog(this,
                        "Process return for transaction " + txId + "?",
                        "Confirm Return", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
                if (confirm == JOptionPane.YES_OPTION) {
                    try {
                        libraryService.returnBook(txId);
                        JOptionPane.showMessageDialog(this, "Book returned successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                        refreshTable(txtSearch.getText());
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });

        actionsPanel.add(btnIssue);
        actionsPanel.add(btnReturn);

        controlsPanel.add(searchPanel, BorderLayout.WEST);
        controlsPanel.add(actionsPanel, BorderLayout.EAST);

        // --- Table Panel ---
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(Theme.CARD_BG);
        tablePanel.setBorder(Theme.BORDER_CARD);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(Theme.BORDER, 1));
        tablePanel.add(scrollPane, BorderLayout.CENTER);

        // Assemble Main Layout
        JPanel centerPanel = new JPanel(new BorderLayout(0, 15));
        centerPanel.setBackground(Theme.CONTENT_BG);
        centerPanel.add(controlsPanel, BorderLayout.NORTH);
        centerPanel.add(tablePanel, BorderLayout.CENTER);

        add(centerPanel, BorderLayout.CENTER);

        // Initial table load
        refreshTable("");
    }

    public final void refreshTable(String query) {
        tableModel.setRowCount(0);
        List<Transaction> transactions = libraryService.searchTransactions(query);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        
        for (Transaction t : transactions) {
            Book book = libraryService.findBookById(t.getBookId());
            Member member = libraryService.findMemberById(t.getMemberId());
            
            String bookTitle = (book != null) ? book.getTitle() : t.getBookId() + " (Deleted)";
            String memberName = (member != null) ? member.getName() : t.getMemberId() + " (Deleted)";
            String returnDateStr = (t.getReturnDate() == null) ? "-" : t.getReturnDate().format(formatter);
            
            tableModel.addRow(new Object[]{
                    t.getId(),
                    bookTitle,
                    memberName,
                    t.getIssueDate().format(formatter),
                    t.getDueDate().format(formatter),
                    returnDateStr,
                    t.getStatus()
            });
        }
    }

    private void showIssueDialog() {
        List<Book> books = libraryService.getBooks();
        List<Member> members = libraryService.getMembers();

        if (books.isEmpty()) {
            JOptionPane.showMessageDialog(this, "There are no books registered in the system.", "No Books Available", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (members.isEmpty()) {
            JOptionPane.showMessageDialog(this, "There are no members registered in the system.", "No Members Available", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Issue Book to Member", true);
        dialog.setLayout(new BorderLayout());
        dialog.setResizable(false);

        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(Theme.CARD_BG);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.weightx = 1.0;

        // ComboBoxes
        JLabel lblBook = new JLabel("Select Book:");
        lblBook.setFont(Theme.FONT_BOLD);
        JComboBox<BookComboItem> comboBooks = new JComboBox<>();
        comboBooks.setBorder(Theme.BORDER_INPUT);
        for (Book b : books) {
            comboBooks.addItem(new BookComboItem(b));
        }

        JLabel lblMember = new JLabel("Select Member:");
        lblMember.setFont(Theme.FONT_BOLD);
        JComboBox<MemberComboItem> comboMembers = new JComboBox<>();
        comboMembers.setBorder(Theme.BORDER_INPUT);
        for (Member m : members) {
            comboMembers.addItem(new MemberComboItem(m));
        }

        JLabel lblDuration = new JLabel("Loan Duration (Days):");
        lblDuration.setFont(Theme.FONT_BOLD);
        JSpinner spinDuration = new JSpinner(new SpinnerNumberModel(14, 1, 90, 1));
        spinDuration.setBorder(Theme.BORDER_INPUT);

        // Add to layout
        gbc.gridx = 0; gbc.gridy = 0;
        mainPanel.add(lblBook, gbc);
        gbc.gridx = 1;
        mainPanel.add(comboBooks, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        mainPanel.add(lblMember, gbc);
        gbc.gridx = 1;
        mainPanel.add(comboMembers, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        mainPanel.add(lblDuration, gbc);
        gbc.gridx = 1;
        mainPanel.add(spinDuration, gbc);

        // Action Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnPanel.setBackground(Theme.CARD_BG);
        btnPanel.setBorder(new EmptyBorder(0, 0, 15, 20));

        JButton btnSubmit = Theme.createStyledButton("Issue Checkout", Theme.PRIMARY, Color.WHITE);
        JButton btnCancel = Theme.createStyledButton("Cancel", Theme.SIDEBAR_BG, Color.WHITE);

        btnCancel.addActionListener(e -> dialog.dispose());
        btnSubmit.addActionListener(e -> {
            BookComboItem bookItem = (BookComboItem) comboBooks.getSelectedItem();
            MemberComboItem memberItem = (MemberComboItem) comboMembers.getSelectedItem();
            int loanDays = (Integer) spinDuration.getValue();

            if (bookItem == null || memberItem == null) {
                JOptionPane.showMessageDialog(dialog, "Please select both a book and a member.", "Selection Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                libraryService.issueBook(bookItem.book.getId(), memberItem.member.getId(), loanDays);
                JOptionPane.showMessageDialog(dialog, "Book checked out successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
                refreshTable(txtSearch.getText());
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, ex.getMessage(), "Checkout Failed", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnPanel.add(btnCancel);
        btnPanel.add(btnSubmit);

        dialog.add(mainPanel, BorderLayout.CENTER);
        dialog.add(btnPanel, BorderLayout.SOUTH);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    // Helper wrapper classes for ComboBox items to display cleanly
    private static class BookComboItem {
        final Book book;

        BookComboItem(Book book) {
            this.book = book;
        }

        @Override
        public String toString() {
            return "[" + book.getId() + "] " + book.getTitle() + " (" + book.getAvailableCopies() + "/" + book.getTotalCopies() + " available)";
        }
    }

    private static class MemberComboItem {
        final Member member;

        MemberComboItem(Member member) {
            this.member = member;
        }

        @Override
        public String toString() {
            return "[" + member.getId() + "] " + member.getName();
        }
    }
}
