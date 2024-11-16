package com.lbtmo.ticketingsystem;

public class AnnouncementModel {
    private String title; // Title of the announcement
    private String content; // Content of the announcement
    private String dateTime; // Date and time of the announcement

    // Constructor
    public AnnouncementModel(String title, String content, String dateTime) {
        this.title = title;
        this.content = content;
        this.dateTime = dateTime;
    }

    // Getters
    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public String getDateTime() {
        return dateTime;
    }

    @Override
    public String toString() {
        return "AnnouncementModel{" +
                "title='" + title + '\'' +
                ", content='" + content + '\'' +
                ", dateTime='" + dateTime + '\'' +
                '}';
    }
}
