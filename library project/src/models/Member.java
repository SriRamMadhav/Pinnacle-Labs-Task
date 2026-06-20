package models;

public class Member {
    private String id;
    private String name;
    private String email;
    private String phone;

    public Member(String id, String name, String email, String phone) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phone = phone;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    // Serialization helper to write to TSV file
    public String toTSV() {
        return id + "\t" + name + "\t" + email + "\t" + phone;
    }

    // Deserialization helper
    public static Member fromTSV(String tsvLine) {
        String[] parts = tsvLine.split("\t");
        if (parts.length < 4) {
            throw new IllegalArgumentException("Invalid TSV line for Member: " + tsvLine);
        }
        return new Member(parts[0], parts[1], parts[2], parts[3]);
    }
}
