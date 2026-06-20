package models;

import java.time.LocalDate;

public class Transaction {
    private String id;
    private String bookId;
    private String memberId;
    private LocalDate issueDate;
    private LocalDate dueDate;
    private LocalDate returnDate; // Can be null if book is not returned yet
    private String status; // "ISSUED", "RETURNED", "OVERDUE"

    public Transaction(String id, String bookId, String memberId, LocalDate issueDate, LocalDate dueDate, LocalDate returnDate, String status) {
        this.id = id;
        this.bookId = bookId;
        this.memberId = memberId;
        this.issueDate = issueDate;
        this.dueDate = dueDate;
        this.returnDate = returnDate;
        this.status = status;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getBookId() {
        return bookId;
    }

    public void setBookId(String bookId) {
        this.bookId = bookId;
    }

    public String getMemberId() {
        return memberId;
    }

    public void setMemberId(String memberId) {
        this.memberId = memberId;
    }

    public LocalDate getIssueDate() {
        return issueDate;
    }

    public void setIssueDate(LocalDate issueDate) {
        this.issueDate = issueDate;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public LocalDate getReturnDate() {
        return returnDate;
    }

    public void setReturnDate(LocalDate returnDate) {
        this.returnDate = returnDate;
        if (returnDate != null) {
            this.status = "RETURNED";
        }
    }

    public String getStatus() {
        // Automatically check if overdue if not returned yet
        if ("ISSUED".equals(status) && LocalDate.now().isAfter(dueDate)) {
            return "OVERDUE";
        }
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    // Serialization helper to write to TSV file
    public String toTSV() {
        String returnStr = (returnDate == null) ? "null" : returnDate.toString();
        return id + "\t" + bookId + "\t" + memberId + "\t" + issueDate.toString() + "\t" + dueDate.toString() + "\t" + returnStr + "\t" + status;
    }

    // Deserialization helper
    public static Transaction fromTSV(String tsvLine) {
        String[] parts = tsvLine.split("\t");
        if (parts.length < 7) {
            throw new IllegalArgumentException("Invalid TSV line for Transaction: " + tsvLine);
        }
        String id = parts[0];
        String bookId = parts[1];
        String memberId = parts[2];
        LocalDate issueDate = LocalDate.parse(parts[3]);
        LocalDate dueDate = LocalDate.parse(parts[4]);
        LocalDate returnDate = parts[5].equals("null") ? null : LocalDate.parse(parts[5]);
        String status = parts[6];
        return new Transaction(id, bookId, memberId, issueDate, dueDate, returnDate, status);
    }
}
