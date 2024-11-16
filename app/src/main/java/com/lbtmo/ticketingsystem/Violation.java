package com.lbtmo.ticketingsystem;

public class Violation {
    private String offenseName;
    private String firstName;
    private String lastName;
    private String middleName;
    private String street;
    private String barangay;
    private String municipality;
    private String dateTime;
    private String status;

    public Violation(String offenseName, String firstName, String lastName, String middleName,
                     String street, String barangay, String municipality, String dateTime,String status) {
        this.offenseName = offenseName;
        this.firstName = firstName;
        this.lastName = lastName;
        this.middleName = middleName;
        this.street = street;
        this.barangay = barangay;
        this.municipality = municipality;
        this.dateTime = dateTime;
        this.status = status;
    }

    public String getOffenseName() {
        return offenseName;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public String getStreet() {
        return street;
    }

    public String getBarangay() {
        return barangay;
    }

    public String getMunicipality() {
        return municipality;
    }

    public String getDateTime() {
        return dateTime;
    }
    public String getStatus() {
        return status;
    }
}
