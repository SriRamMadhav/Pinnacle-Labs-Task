package models;

public class Book {
    private String id;
    private String title;
    private String author;
    private String genre;
    private int totalCopies;
    private int availableCopies;

    public Book(String id, String title, String author, String genre, int totalCopies, int availableCopies) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.genre = genre;
        this.totalCopies = totalCopies;
        this.availableCopies = availableCopies;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public int getTotalCopies() {
        return totalCopies;
    }

    public void setTotalCopies(int totalCopies) {
        this.totalCopies = totalCopies;
    }

    public int getAvailableCopies() {
        return availableCopies;
    }

    public void setAvailableCopies(int availableCopies) {
        this.availableCopies = availableCopies;
    }

    // Business Logic
    public boolean borrowCopy() {
        if (availableCopies > 0) {
            availableCopies--;
            return true;
        }
        return false;
    }

    public void returnCopy() {
        if (availableCopies < totalCopies) {
            availableCopies++;
        }
    }

    // Serialization helper to write to TSV file
    public String toTSV() {
        return id + "\t" + title + "\t" + author + "\t" + genre + "\t" + totalCopies + "\t" + availableCopies;
    }

    // Deserialization helper
    public static Book fromTSV(String tsvLine) {
        String[] parts = tsvLine.split("\t");
        if (parts.length < 6) {
            throw new IllegalArgumentException("Invalid TSV line for Book: " + tsvLine);
        }
        String id = parts[0];
        String title = parts[1];
        String author = parts[2];
        String genre = parts[3];
        int totalCopies = Integer.parseInt(parts[4]);
        int availableCopies = Integer.parseInt(parts[5]);
        return new Book(id, title, author, genre, totalCopies, availableCopies);
    }
}
