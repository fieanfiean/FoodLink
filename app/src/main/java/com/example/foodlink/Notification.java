package com.example.foodlink;

import java.util.Date;

public class Notification {
    private String id;
    private String userId; // Recipient user ID
    private String title;
    private String message;
    private String type; // "new_listing", "reservation", "pickup_confirmed", "reservation_cancelled"
    private String listingId; // Related food listing ID
    private String senderId; // User who triggered the notification
    private boolean isRead;
    private Date timestamp;

    // Constructors
    public Notification() {}

    public Notification(String userId, String title, String message, String type,
                        String listingId, String senderId) {
        this.userId = userId;
        this.title = title;
        this.message = message;
        this.type = type;
        this.listingId = listingId;
        this.senderId = senderId;
        this.isRead = false;
        this.timestamp = new Date();
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getListingId() { return listingId; }
    public void setListingId(String listingId) { this.listingId = listingId; }

    public String getSenderId() { return senderId; }
    public void setSenderId(String senderId) { this.senderId = senderId; }

    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { isRead = read; }

    public Date getTimestamp() { return timestamp; }
    public void setTimestamp(Date timestamp) { this.timestamp = timestamp; }
}
