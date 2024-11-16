package com.lbtmo.ticketingsystem;

public class Street {
    private String streetName;
    private String streetId;
    private String barangayId;

    // Default constructor required for calls to DataSnapshot.getValue(Street.class)
    public Street() {}

    public Street(String streetName, String streetId, String barangayId) {
        this.streetName = streetName;
        this.streetId = streetId;
        this.barangayId = barangayId;
    }

    public String getStreetName() {
        return streetName;
    }

    public void setStreetName(String streetName) {
        this.streetName = streetName;
    }

    public String getStreetId() {
        return streetId;
    }

    public void setStreetId(String streetId) {
        this.streetId = streetId;
    }

    public String getBarangayId() {
        return barangayId;
    }

    public void setBarangayId(String barangayId) {
        this.barangayId = barangayId;
    }

    @Override
    public String toString() {
        return streetName;  // Display the street name in the spinner
    }
}

