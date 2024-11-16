package com.lbtmo.ticketingsystem;

public class Ticket {
    private String ticketId;
    private String ticketDescription;
    private String dateTime; // New field for date-time
    private String firstName; // New field for first name
    private String lastName; // New field for last name
    private String middleName; // New field for middle name
    private String plateLicense; // New field for plate license
    private String barangay; // New field for barangay
    private String street; // New field for street

    public Ticket(String ticketId, String ticketDescription, String dateTime, String firstName,
                  String lastName, String middleName, String plateLicense, String barangay,
                  String street) {
        this.ticketId = ticketId;
        this.ticketDescription = ticketDescription;
        this.dateTime = dateTime;
        this.firstName = firstName;
        this.lastName = lastName;
        this.middleName = middleName;
        this.plateLicense = plateLicense;
        this.barangay = barangay;
        this.street = street;
    }

    // Getters
    public String getTicketId() { return ticketId; }
    public String getTicketDescription() { return ticketDescription; }
    public String getDateTime() { return dateTime; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getMiddleName() { return middleName; }
    public String getPlateLicense() { return plateLicense; }
    public String getBarangay() { return barangay; }
    public String getStreet() { return street; }
}
