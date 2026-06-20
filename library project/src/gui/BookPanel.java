package gui;

import models.Book;
import services.LibraryService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.List;

public class BookPanel extends JPanel {
    private final LibraryService libraryService;
    private final JTable table;
    private final DefaultTableModel tableModel;
    private final JTextField txtSearch;

    public BookPanel(LibraryService libraryService) {
        this.libraryService = libraryService;

        // Initialize Table first to prevent compiler errors due to early capture in action listeners
        String[] columnNames = {"Book ID", "Title", "Author", "Genre", "Total Copies", "Available Copies"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 4 || columnIndex == 5) {
                    return Integer.class;
                }
                return String.class;
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
        
        JLabel titleLabel = new JLabel("Manage Books");
        titleLabel.setFont(Theme.FONT_TITLE);
        titleLabel.setForeground(Theme.TEXT_MAIN);
        JLabel subtitleLabel = new JLabel("Add, edit, remove, and search books in the library catalog");
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
        txtSearch.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                refreshTable(txtSearch.getText());
            }
        });
        searchPanel.add(lblSearch, BorderLayout.WEST);
        searchPanel.add(txtSearch, BorderLayout.CENTER);

        // Actions panel
        JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionsPanel.setBackground(Theme.CONTENT_BG);

        JButton btnAdd = Theme.createStyledButton("+ Add Book", Theme.SUCCESS, Color.WHITE);
        JButton btnEdit = Theme.createStyledButton("Edit Book", Theme.PRIMARY, Color.WHITE);
        JButton btnDelete = Theme.createStyledButton("Delete Book", Theme.DANGER, Color.WHITE);

        btnAdd.addActionListener(e -> showBookDialog(null));
        btnEdit.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Please select a book to edit.", "No Book Selected", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String bookId = table.getValueAt(selectedRow, 0).toString();
            Book book = libraryService.findBookById(bookId);
            if (book != null) {
                showBookDialog(book);
            }
        });
        btnDelete.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Please select a book to delete.", "No Book Selected", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String bookId = table.getValueAt(selectedRow, 0).toString();
            Book book = libraryService.findBookById(bookId);
            if (book != null) {
                int confirm = JOptionPane.showConfirmDialog(this,
                        "Are you sure you want to delete book '" + book.getTitle() + "'?",
                        "Confirm Deletion", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                if (confirm == JOptionPane.YES_OPTION) {
                    try {
                        libraryService.deleteBook(bookId);
                        JOptionPane.showMessageDialog(this, "Book deleted successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                        refreshTable(txtSearch.getText());
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });

        actionsPanel.add(btnAdd);
        actionsPanel.add(btnEdit);
        actionsPanel.add(btnDelete);

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
        List<Book> books = libraryService.searchBooks(query);
        for (Book b : books) {
            tableModel.addRow(new Object[]{
                    b.getId(),
                    b.getTitle(),
                    b.getAuthor(),
                    b.getGenre(),
                    b.getTotalCopies(),
                    b.getAvailableCopies()
            });
        }
    }

    private void showBookDialog(Book existingBook) {
        boolean isEditMode = (existingBook != null);
        String titleText = isEditMode ? "Edit Book details" : "Add New Book";
        
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), titleText, true);
        dialog.setLayout(new BorderLayout());
        dialog.setResizable(false);

        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(Theme.CARD_BG);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.weightx = 1.0;

        // Form Fields
        JLabel lblId = new JLabel("Book ID:");
        lblId.setFont(Theme.FONT_BOLD);
        JTextField txtId = new JTextField(isEditMode ? existingBook.getId() : libraryService.generateBookId());
        txtId.setEnabled(false); // ID is generated/managed
        txtId.setBorder(Theme.BORDER_INPUT);

        JLabel lblTitle = new JLabel("Title:");
        lblTitle.setFont(Theme.FONT_BOLD);
        JTextField txtTitle = new JTextField(isEditMode ? existingBook.getTitle() : "");
        txtTitle.setBorder(Theme.BORDER_INPUT);

        JLabel lblAuthor = new JLabel("Author:");
        lblAuthor.setFont(Theme.FONT_BOLD);
        JTextField txtAuthor = new JTextField(isEditMode ? existingBook.getAuthor() : "");
        txtAuthor.setBorder(Theme.BORDER_INPUT);

        JLabel lblGenre = new JLabel("Genre:");
        lblGenre.setFont(Theme.FONT_BOLD);
        JTextField txtGenre = new JTextField(isEditMode ? existingBook.getGenre() : "");
        txtGenre.setBorder(Theme.BORDER_INPUT);

        JLabel lblTotalCopies = new JLabel("Total Copies:");
        lblTotalCopies.setFont(Theme.FONT_BOLD);
        JSpinner spinTotalCopies = new JSpinner(new SpinnerNumberModel(
                isEditMode ? existingBook.getTotalCopies() : 1,
                1, 9999, 1
        ));
        spinTotalCopies.setBorder(Theme.BORDER_INPUT);

        // Add to dialog grid
        gbc.gridx = 0; gbc.gridy = 0;
        mainPanel.add(lblId, gbc);
        gbc.gridx = 1;
        mainPanel.add(txtId, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        mainPanel.add(lblTitle, gbc);
        gbc.gridx = 1;
        mainPanel.add(txtTitle, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        mainPanel.add(lblAuthor, gbc);
        gbc.gridx = 1;
        mainPanel.add(txtAuthor, gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        mainPanel.add(lblGenre, gbc);
        gbc.gridx = 1;
        mainPanel.add(txtGenre, gbc);

        gbc.gridx = 0; gbc.gridy = 4;
        mainPanel.add(lblTotalCopies, gbc);
        gbc.gridx = 1;
        mainPanel.add(spinTotalCopies, gbc);

        // Button actions
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnPanel.setBackground(Theme.CARD_BG);
        btnPanel.setBorder(new EmptyBorder(0, 0, 15, 20));

        JButton btnSave = Theme.createStyledButton(isEditMode ? "Save Changes" : "Add Book", Theme.SUCCESS, Color.WHITE);
        JButton btnCancel = Theme.createStyledButton("Cancel", Theme.SIDEBAR_BG, Color.WHITE);

        btnCancel.addActionListener(e -> dialog.dispose());
        btnSave.addActionListener(e -> {
            String bookTitle = txtTitle.getText().trim();
            String author = txtAuthor.getText().trim();
            String genre = txtGenre.getText().trim();
            int totalCopies = (Integer) spinTotalCopies.getValue();

            if (bookTitle.isEmpty() || author.isEmpty() || genre.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "All fields are required.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                if (isEditMode) {
                    libraryService.updateBook(existingBook.getId(), bookTitle, author, genre, totalCopies);
                    JOptionPane.showMessageDialog(dialog, "Book updated successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    Book newBook = new Book(txtId.getText(), bookTitle, author, genre, totalCopies, totalCopies);
                    libraryService.addBook(newBook);
                    JOptionPane.showMessageDialog(dialog, "Book added successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                }
                dialog.dispose();
                refreshTable(txtSearch.getText());
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnPanel.add(btnCancel);
        btnPanel.add(btnSave);

        dialog.add(mainPanel, BorderLayout.CENTER);
        dialog.add(btnPanel, BorderLayout.SOUTH);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }
}
