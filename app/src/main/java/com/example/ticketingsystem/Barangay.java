package com.example.ticketingsystem;

public class Barangay {
    private String barangayId;
    private String barangayName;

    public Barangay(String barangayId, String barangayName) {
        this.barangayId = barangayId;
        this.barangayName = barangayName;
    }

    public String getBarangayId() {
        return barangayId;
    }

    public String getBarangayName() {
        return barangayName;
    }

    @Override
    public String toString() {
        return barangayName; // Display name in spinner
    }
}
