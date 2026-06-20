package gui;

import models.Member;
import services.LibraryService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.List;

public class MemberPanel extends JPanel {
    private final LibraryService libraryService;
    private final JTable table;
    private final DefaultTableModel tableModel;
    private final JTextField txtSearch;

    public MemberPanel(LibraryService libraryService) {
        this.libraryService = libraryService;

        // Initialize Table first to prevent compiler errors due to early capture in action listeners
        String[] columnNames = {"Member ID", "Full Name", "Email Address", "Phone Number"};
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
        
        JLabel titleLabel = new JLabel("Manage Members");
        titleLabel.setFont(Theme.FONT_TITLE);
        titleLabel.setForeground(Theme.TEXT_MAIN);
        JLabel subtitleLabel = new JLabel("Add, edit, remove, and search members in the system database");
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

        JButton btnAdd = Theme.createStyledButton("+ Add Member", Theme.SUCCESS, Color.WHITE);
        JButton btnEdit = Theme.createStyledButton("Edit Member", Theme.PRIMARY, Color.WHITE);
        JButton btnDelete = Theme.createStyledButton("Delete Member", Theme.DANGER, Color.WHITE);

        btnAdd.addActionListener(e -> showMemberDialog(null));
        btnEdit.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Please select a member to edit.", "No Member Selected", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String memberId = table.getValueAt(selectedRow, 0).toString();
            Member member = libraryService.findMemberById(memberId);
            if (member != null) {
                showMemberDialog(member);
            }
        });
        btnDelete.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Please select a member to delete.", "No Member Selected", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String memberId = table.getValueAt(selectedRow, 0).toString();
            Member member = libraryService.findMemberById(memberId);
            if (member != null) {
                int confirm = JOptionPane.showConfirmDialog(this,
                        "Are you sure you want to delete member '" + member.getName() + "'?",
                        "Confirm Deletion", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                if (confirm == JOptionPane.YES_OPTION) {
                    try {
                        libraryService.deleteMember(memberId);
                        JOptionPane.showMessageDialog(this, "Member deleted successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
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
        List<Member> members = libraryService.searchMembers(query);
        for (Member m : members) {
            tableModel.addRow(new Object[]{
                    m.getId(),
                    m.getName(),
                    m.getEmail(),
                    m.getPhone()
            });
        }
    }

    private void showMemberDialog(Member existingMember) {
        boolean isEditMode = (existingMember != null);
        String titleText = isEditMode ? "Edit Member Details" : "Add New Member";

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
        JLabel lblId = new JLabel("Member ID:");
        lblId.setFont(Theme.FONT_BOLD);
        JTextField txtId = new JTextField(isEditMode ? existingMember.getId() : libraryService.generateMemberId());
        txtId.setEnabled(false); // ID is generated
        txtId.setBorder(Theme.BORDER_INPUT);

        JLabel lblName = new JLabel("Full Name:");
        lblName.setFont(Theme.FONT_BOLD);
        JTextField txtName = new JTextField(isEditMode ? existingMember.getName() : "");
        txtName.setBorder(Theme.BORDER_INPUT);

        JLabel lblEmail = new JLabel("Email:");
        lblEmail.setFont(Theme.FONT_BOLD);
        JTextField txtEmail = new JTextField(isEditMode ? existingMember.getEmail() : "");
        txtEmail.setBorder(Theme.BORDER_INPUT);

        JLabel lblPhone = new JLabel("Phone:");
        lblPhone.setFont(Theme.FONT_BOLD);
        JTextField txtPhone = new JTextField(isEditMode ? existingMember.getPhone() : "");
        txtPhone.setBorder(Theme.BORDER_INPUT);

        // Add to dialog grid
        gbc.gridx = 0; gbc.gridy = 0;
        mainPanel.add(lblId, gbc);
        gbc.gridx = 1;
        mainPanel.add(txtId, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        mainPanel.add(lblName, gbc);
        gbc.gridx = 1;
        mainPanel.add(txtName, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        mainPanel.add(lblEmail, gbc);
        gbc.gridx = 1;
        mainPanel.add(txtEmail, gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        mainPanel.add(lblPhone, gbc);
        gbc.gridx = 1;
        mainPanel.add(txtPhone, gbc);

        // Button actions
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnPanel.setBackground(Theme.CARD_BG);
        btnPanel.setBorder(new EmptyBorder(0, 0, 15, 20));

        JButton btnSave = Theme.createStyledButton(isEditMode ? "Save Changes" : "Add Member", Theme.SUCCESS, Color.WHITE);
        JButton btnCancel = Theme.createStyledButton("Cancel", Theme.SIDEBAR_BG, Color.WHITE);

        btnCancel.addActionListener(e -> dialog.dispose());
        btnSave.addActionListener(e -> {
            String name = txtName.getText().trim();
            String email = txtEmail.getText().trim();
            String phone = txtPhone.getText().trim();

            // Simple validation
            if (name.isEmpty() || email.isEmpty() || phone.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "All fields are required.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (!email.contains("@") || !email.contains(".")) {
                JOptionPane.showMessageDialog(dialog, "Please enter a valid email address.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (!phone.matches("\\+?\\d{7,15}")) {
                JOptionPane.showMessageDialog(dialog, "Please enter a valid phone number (digits only, 7-15 chars).", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                if (isEditMode) {
                    libraryService.updateMember(existingMember.getId(), name, email, phone);
                    JOptionPane.showMessageDialog(dialog, "Member updated successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    Member newMember = new Member(txtId.getText(), name, email, phone);
                    libraryService.addMember(newMember);
                    JOptionPane.showMessageDialog(dialog, "Member added successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
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
