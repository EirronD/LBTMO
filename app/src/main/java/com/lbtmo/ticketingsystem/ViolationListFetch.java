package com.lbtmo.ticketingsystem;

public class ViolationListFetch {
    private String code;
    private String title;
    private boolean checked;
    private String id;  // Added unique ID field

    // Constructor updated to include the unique ID
    public ViolationListFetch(String id, String code, String title) {
        this.id = id;      // Initialize the unique ID
        this.code = code;
        this.title = title;
        this.checked = false; // Default value for checked
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }
}
