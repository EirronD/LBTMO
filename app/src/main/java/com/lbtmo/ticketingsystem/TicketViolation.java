package com.lbtmo.ticketingsystem;

import androidx.annotation.NonNull;
import java.io.Serializable;

public class TicketViolation implements Serializable{
    private String code;
    private String title;
    private String id;

    public TicketViolation(String id, String code, String title) {
        this.id = id;
        this.code = code;
        this.title = title;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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
