package storage;

import models.Book;
import models.Member;
import models.Transaction;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class FileStorage {
    private static final String DATA_DIR = "data";
    private static final String BOOKS_FILE = DATA_DIR + "/books.txt";
    private static final String MEMBERS_FILE = DATA_DIR + "/members.txt";
    private static final String TRANSACTIONS_FILE = DATA_DIR + "/transactions.txt";

    public FileStorage() {
        createDataDirectoryIfNeeded();
    }

    private void createDataDirectoryIfNeeded() {
        File dir = new File(DATA_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    public List<Book> loadBooks() {
        List<Book> books = new ArrayList<>();
        File file = new File(BOOKS_FILE);
        if (!file.exists()) return books;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                try {
                    books.add(Book.fromTSV(line));
                } catch (Exception e) {
                    System.err.println("Error parsing book: " + line + ". " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("Failed to load books: " + e.getMessage());
        }
        return books;
    }

    public void saveBooks(List<Book> books) {
        createDataDirectoryIfNeeded();
        try (PrintWriter writer = new PrintWriter(new FileWriter(BOOKS_FILE))) {
            for (Book book : books) {
                writer.println(book.toTSV());
            }
        } catch (IOException e) {
            System.err.println("Failed to save books: " + e.getMessage());
        }
    }

    public List<Member> loadMembers() {
        List<Member> members = new ArrayList<>();
        File file = new File(MEMBERS_FILE);
        if (!file.exists()) return members;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                try {
                    members.add(Member.fromTSV(line));
                } catch (Exception e) {
                    System.err.println("Error parsing member: " + line + ". " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("Failed to load members: " + e.getMessage());
        }
        return members;
    }

    public void saveMembers(List<Member> members) {
        createDataDirectoryIfNeeded();
        try (PrintWriter writer = new PrintWriter(new FileWriter(MEMBERS_FILE))) {
            for (Member member : members) {
                writer.println(member.toTSV());
            }
        } catch (IOException e) {
            System.err.println("Failed to save members: " + e.getMessage());
        }
    }

    public List<Transaction> loadTransactions() {
        List<Transaction> transactions = new ArrayList<>();
        File file = new File(TRANSACTIONS_FILE);
        if (!file.exists()) return transactions;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                try {
                    transactions.add(Transaction.fromTSV(line));
                } catch (Exception e) {
                    System.err.println("Error parsing transaction: " + line + ". " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("Failed to load transactions: " + e.getMessage());
        }
        return transactions;
    }

    public void saveTransactions(List<Transaction> transactions) {
        createDataDirectoryIfNeeded();
        try (PrintWriter writer = new PrintWriter(new FileWriter(TRANSACTIONS_FILE))) {
            for (Transaction t : transactions) {
                writer.println(t.toTSV());
            }
        } catch (IOException e) {
            System.err.println("Failed to save transactions: " + e.getMessage());
        }
    }
}
