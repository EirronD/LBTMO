package com.lbtmo.ticketingsystem;

public class Barangay {
    private String id;
    private String barangayName;

    // Constructor
    public Barangay(String id, String barangayName) {
        this.id = id;
        this.barangayName = barangayName;
    }

    // Getter and Setter for id
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    // Getter and Setter for barangayName
    public String getBarangayName() {
        return barangayName;
    }

    public void setBarangayName(String barangayName) {
        this.barangayName = barangayName;
    }

    // Optional: Override toString() to show barangay name in Spinner
    @Override
    public String toString() {
        return barangayName;
    }
}
