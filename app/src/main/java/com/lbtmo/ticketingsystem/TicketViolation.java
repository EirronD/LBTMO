package com.lbtmo.ticketingsystem;

import androidx.annotation.NonNull;

public class TicketViolation {
    private String code;
    private String title;

    public TicketViolation(String code, String title) {
        this.code = code;
        this.title = title;
    }

    public String getCode() {
        return code;
    }

    public String getTitle() {
        return title;
    }

    @NonNull
    @Override
    public String toString() {
        return code + " " + title; // Ensures it shows properly in AutoCompleteTextView
    }
}
