package com.example.foodlink;

import com.google.firebase.firestore.DocumentSnapshot;

public class AdminListing {

    public String listingId;

    // Core listing fields (match food_listings document)
    public String foodName;            // food_name
    public String category;            // category
    public String description;         // description
    public String additionalDetails;   // additional_details
    public String expiryDate;          // expiry_date
    public String startTime;           // start_time
    public String endTime;             // end_time
    public Long quantity;              // quantity
    public String status;              // status
    public Long createdAt;             // created_at

    // Seller fields
    public String sellerId;            // seller_id
    public String sellerName;          // seller_name
    public String sellerEmail;         // seller_email

    // Reservation fields (stored inside food_listings doc)
    public String reservedBy;          // reserved_by
    public String reservedCharity;     // reserved_charity
    public String pickupDate;          // pickup_date

    public AdminListing() {}

    public static AdminListing fromDoc(DocumentSnapshot doc) {
        AdminListing l = new AdminListing();
        l.listingId = doc.getId();

        // Match Firestore field names exactly
        l.foodName = doc.getString("food_name");
        l.category = doc.getString("category");
        l.description = doc.getString("description");
        l.additionalDetails = doc.getString("additional_details");
        l.expiryDate = doc.getString("expiry_date");
        l.startTime = doc.getString("start_time");
        l.endTime = doc.getString("end_time");

        l.quantity = doc.getLong("quantity");
        l.status = doc.getString("status");
        l.createdAt = doc.getLong("created_at");

        l.sellerId = doc.getString("seller_id");
        l.sellerName = doc.getString("seller_name");
        l.sellerEmail = doc.getString("seller_email");

        l.reservedBy = doc.getString("reserved_by");
        l.reservedCharity = doc.getString("reserved_charity");
        l.pickupDate = doc.getString("pickup_date");

        return l;
    }
}
