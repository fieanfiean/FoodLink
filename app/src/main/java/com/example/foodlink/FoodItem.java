package com.example.foodlink;

import java.io.Serializable;

public class FoodItem implements Serializable {
    private String id;
    private String food_name;  // Changed from 'name' to match Firestore
    private String quantity;
    private String category;
    private String expiry_date;  // Changed from 'expiryDate' to match Firestore
    private String start_time;   // Changed from 'startTime' to match Firestore
    private String end_time;     // Changed from 'endTime' to match Firestore
    private String description;  // Changed from 'additionalDetails' to match Firestore
    private String status;
    private String image_base64; // Changed from 'imageUrl' to match Firestore
    private String sellerId;
    private String sellerName;
    private long created_at;     // Added to match Firestore

    // Empty constructor for Firestore
    public FoodItem() {
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFoodName() {
        return food_name;
    }

    public void setFoodName(String food_name) {
        this.food_name = food_name;
    }

    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getExpiryDate() {
        return expiry_date;
    }

    public void setExpiryDate(String expiry_date) {
        this.expiry_date = expiry_date;
    }

    public String getStartTime() {
        return start_time;
    }

    public void setStartTime(String start_time) {
        this.start_time = start_time;
    }

    public String getEndTime() {
        return end_time;
    }

    public void setEndTime(String end_time) {
        this.end_time = end_time;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getImageBase64() {
        return image_base64;
    }

    public void setImageBase64(String image_base64) {
        this.image_base64 = image_base64;
    }

    public String getSellerId() {
        return sellerId;
    }

    public void setSellerId(String sellerId) {
        this.sellerId = sellerId;
    }

    public String getSellerName() {
        return sellerName;
    }

    public void setSellerName(String sellerName) {
        this.sellerName = sellerName;
    }

    public long getCreatedAt() {
        return created_at;
    }

    public void setCreatedAt(long created_at) {
        this.created_at = created_at;
    }
}