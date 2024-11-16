package com.lbtmo.ticketingsystem;

public class ItemHorizontalRecyclerViewModel {
    private final int imageResId; // Resource ID for the image
    private final String title; // Title of the item
    private final String description; // Description of the item

    public ItemHorizontalRecyclerViewModel(int imageResId, String title, String description) {
        this.imageResId = imageResId;
        this.title = title;
        this.description = description; // Initialize description
    }

    public int getImageResId() {
        return imageResId;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() { // Add getter for description
        return description;
    }
}
