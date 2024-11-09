package com.example.ticketingsystem;

public class ViolationListFetch {
    private String code;
    private String title;
    private boolean checked;

    public ViolationListFetch(String code, String title) {
        this.code = code;
        this.title = title;
        this.checked = false; // Default value
    }

    public String getCode() {
        return code;
    }

    public String getTitle() {
        return title;
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }
}
