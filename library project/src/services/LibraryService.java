package services;

import models.Book;
import models.Member;
import models.Transaction;
import storage.FileStorage;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class LibraryService {
    private final List<Book> books;
    private final List<Member> members;
    private final List<Transaction> transactions;
    private final FileStorage storage;

    public LibraryService() {
        this.storage = new FileStorage();
        this.books = storage.loadBooks();
        this.members = storage.loadMembers();
        this.transactions = storage.loadTransactions();
    }

    // =========================================================================
    // ID GENERATORS
    // =========================================================================

    public synchronized String generateBookId() {
        int maxNum = 1000;
        for (Book book : books) {
            String id = book.getId();
            if (id.startsWith("B-")) {
                try {
                    int num = Integer.parseInt(id.substring(2));
                    if (num > maxNum) {
                        maxNum = num;
                    }
                } catch (NumberFormatException ignored) {}
            }
        }
        return "B-" + (maxNum + 1);
    }

    public synchronized String generateMemberId() {
        int maxNum = 1000;
        for (Member member : members) {
            String id = member.getId();
            if (id.startsWith("M-")) {
                try {
                    int num = Integer.parseInt(id.substring(2));
                    if (num > maxNum) {
                        maxNum = num;
                    }
                } catch (NumberFormatException ignored) {}
            }
        }
        return "M-" + (maxNum + 1);
    }

    public synchronized String generateTransactionId() {
        int maxNum = 1000;
        for (Transaction transaction : transactions) {
            String id = transaction.getId();
            if (id.startsWith("T-")) {
                try {
                    int num = Integer.parseInt(id.substring(2));
                    if (num > maxNum) {
                        maxNum = num;
                    }
                } catch (NumberFormatException ignored) {}
            }
        }
        return "T-" + (maxNum + 1);
    }

    // =========================================================================
    // BOOK CRUD
    // =========================================================================

    public List<Book> getBooks() {
        return new ArrayList<>(books);
    }

    public Book findBookById(String id) {
        for (Book book : books) {
            if (book.getId().equalsIgnoreCase(id)) {
                return book;
            }
        }
        return null;
    }

    public void addBook(Book book) throws IllegalArgumentException {
        if (findBookById(book.getId()) != null) {
            throw new IllegalArgumentException("Book with ID " + book.getId() + " already exists.");
        }
        books.add(book);
        storage.saveBooks(books);
    }

    public void updateBook(String id, String title, String author, String genre, int totalCopies) throws Exception {
        Book book = findBookById(id);
        if (book == null) {
            throw new IllegalArgumentException("Book not found.");
        }

        // Calculate currently borrowed copies
        int borrowedCopies = book.getTotalCopies() - book.getAvailableCopies();
        if (totalCopies < borrowedCopies) {
            throw new IllegalArgumentException("Total copies cannot be less than currently borrowed copies (" + borrowedCopies + ").");
        }

        book.setTitle(title);
        book.setAuthor(author);
        book.setGenre(genre);
        book.setTotalCopies(totalCopies);
        book.setAvailableCopies(totalCopies - borrowedCopies);

        storage.saveBooks(books);
    }

    public void deleteBook(String id) throws Exception {
        Book book = findBookById(id);
        if (book == null) {
            throw new IllegalArgumentException("Book not found.");
        }

        // Check if there are active transactions for this book
        boolean hasActiveBorrows = transactions.stream()
                .anyMatch(t -> t.getBookId().equalsIgnoreCase(id) && !"RETURNED".equals(t.getStatus()));
        if (hasActiveBorrows) {
            throw new IllegalStateException("Cannot delete book. There are active borrows for this book.");
        }

        books.remove(book);
        storage.saveBooks(books);
    }

    public List<Book> searchBooks(String query) {
        if (query == null || query.trim().isEmpty()) {
            return getBooks();
        }
        String lowerQuery = query.toLowerCase();
        return books.stream()
                .filter(b -> b.getId().toLowerCase().contains(lowerQuery) ||
                             b.getTitle().toLowerCase().contains(lowerQuery) ||
                             b.getAuthor().toLowerCase().contains(lowerQuery) ||
                             b.getGenre().toLowerCase().contains(lowerQuery))
                .collect(Collectors.toList());
    }

    // =========================================================================
    // MEMBER CRUD
    // =========================================================================

    public List<Member> getMembers() {
        return new ArrayList<>(members);
    }

    public Member findMemberById(String id) {
        for (Member member : members) {
            if (member.getId().equalsIgnoreCase(id)) {
                return member;
            }
        }
        return null;
    }

    public void addMember(Member member) throws IllegalArgumentException {
        if (findMemberById(member.getId()) != null) {
            throw new IllegalArgumentException("Member with ID " + member.getId() + " already exists.");
        }
        members.add(member);
        storage.saveMembers(members);
    }

    public void updateMember(String id, String name, String email, String phone) throws IllegalArgumentException {
        Member member = findMemberById(id);
        if (member == null) {
            throw new IllegalArgumentException("Member not found.");
        }
        member.setName(name);
        member.setEmail(email);
        member.setPhone(phone);
        storage.saveMembers(members);
    }

    public void deleteMember(String id) throws Exception {
        Member member = findMemberById(id);
        if (member == null) {
            throw new IllegalArgumentException("Member not found.");
        }

        // Check if there are active transactions for this member
        boolean hasActiveBorrows = transactions.stream()
                .anyMatch(t -> t.getMemberId().equalsIgnoreCase(id) && !"RETURNED".equals(t.getStatus()));
        if (hasActiveBorrows) {
            throw new IllegalStateException("Cannot delete member. This member currently has borrowed books.");
        }

        members.remove(member);
        storage.saveMembers(members);
    }

    public List<Member> searchMembers(String query) {
        if (query == null || query.trim().isEmpty()) {
            return getMembers();
        }
        String lowerQuery = query.toLowerCase();
        return members.stream()
                .filter(m -> m.getId().toLowerCase().contains(lowerQuery) ||
                             m.getName().toLowerCase().contains(lowerQuery) ||
                             m.getEmail().toLowerCase().contains(lowerQuery) ||
                             m.getPhone().toLowerCase().contains(lowerQuery))
                .collect(Collectors.toList());
    }

    // =========================================================================
    // TRANSACTION LOGIC
    // =========================================================================

    public List<Transaction> getTransactions() {
        return new ArrayList<>(transactions);
    }

    public Transaction findTransactionById(String id) {
        for (Transaction t : transactions) {
            if (t.getId().equalsIgnoreCase(id)) {
                return t;
            }
        }
        return null;
    }

    public void issueBook(String bookId, String memberId, int loanDays) throws Exception {
        Book book = findBookById(bookId);
        if (book == null) {
            throw new IllegalArgumentException("Book with ID " + bookId + " does not exist.");
        }

        Member member = findMemberById(memberId);
        if (member == null) {
            throw new IllegalArgumentException("Member with ID " + memberId + " does not exist.");
        }

        if (book.getAvailableCopies() <= 0) {
            throw new IllegalStateException("No copies of '" + book.getTitle() + "' are currently available.");
        }

        // Perform book borrow
        if (book.borrowCopy()) {
            String txId = generateTransactionId();
            LocalDate issueDate = LocalDate.now();
            LocalDate dueDate = issueDate.plusDays(loanDays);
            Transaction tx = new Transaction(txId, bookId, memberId, issueDate, dueDate, null, "ISSUED");
            transactions.add(tx);

            // Save both collections
            storage.saveBooks(books);
            storage.saveTransactions(transactions);
        } else {
            throw new IllegalStateException("Failed to issue book.");
        }
    }

    public void returnBook(String transactionId) throws Exception {
        Transaction tx = findTransactionById(transactionId);
        if (tx == null) {
            throw new IllegalArgumentException("Transaction with ID " + transactionId + " not found.");
        }

        if ("RETURNED".equals(tx.getStatus())) {
            throw new IllegalStateException("This book has already been returned.");
        }

        Book book = findBookById(tx.getBookId());
        if (book != null) {
            book.returnCopy();
        }

        tx.setReturnDate(LocalDate.now());
        tx.setStatus("RETURNED");

        // Save both collections
        storage.saveBooks(books);
        storage.saveTransactions(transactions);
    }

    public List<Transaction> searchTransactions(String query) {
        if (query == null || query.trim().isEmpty()) {
            return getTransactions();
        }
        String lowerQuery = query.toLowerCase();
        return transactions.stream()
                .filter(t -> {
                    Book book = findBookById(t.getBookId());
                    Member member = findMemberById(t.getMemberId());
                    String title = (book == null) ? "" : book.getTitle().toLowerCase();
                    String memberName = (member == null) ? "" : member.getName().toLowerCase();
                    return t.getId().toLowerCase().contains(lowerQuery) ||
                           t.getBookId().toLowerCase().contains(lowerQuery) ||
                           t.getMemberId().toLowerCase().contains(lowerQuery) ||
                           title.contains(lowerQuery) ||
                           memberName.contains(lowerQuery) ||
                           t.getStatus().toLowerCase().contains(lowerQuery);
                })
                .collect(Collectors.toList());
    }

    // =========================================================================
    // STATS & REPORT GENERATION
    // =========================================================================

    public int getTotalBooksCount() {
        return books.size();
    }

    public int getTotalBookCopiesCount() {
        return books.stream().mapToInt(Book::getTotalCopies).sum();
    }

    public int getTotalMembersCount() {
        return members.size();
    }

    public int getActiveBorrowsCount() {
        return (int) transactions.stream()
                .filter(t -> !"RETURNED".equals(t.getStatus()))
                .count();
    }

    public int getOverdueCount() {
        return (int) transactions.stream()
                .filter(t -> "OVERDUE".equals(t.getStatus()) ||
                        ("ISSUED".equals(t.getStatus()) && LocalDate.now().isAfter(t.getDueDate())))
                .count();
    }

    public List<Transaction> getOverdueTransactions() {
        return transactions.stream()
                .filter(t -> "OVERDUE".equals(t.getStatus()) ||
                        ("ISSUED".equals(t.getStatus()) && LocalDate.now().isAfter(t.getDueDate())))
                .collect(Collectors.toList());
    }

    public Map<String, Integer> getPopularBooks() {
        Map<String, Integer> counts = new HashMap<>();
        for (Transaction t : transactions) {
            Book b = findBookById(t.getBookId());
            if (b != null) {
                counts.put(b.getTitle(), counts.getOrDefault(b.getTitle(), 0) + 1);
            }
        }
        return counts;
    }
}
