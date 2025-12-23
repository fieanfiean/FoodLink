package com.example.foodlink;

import com.google.firebase.firestore.DocumentSnapshot;

public class AdminReservation {

    public String reservationId;
    public String reservedBy;
    public String reservedCharity;
    public String sellerId;
    public String sellerName;
    public String pickupDate;
    public Long quantity;
    public String status;

    public AdminReservation() {}

    public static AdminReservation fromDoc(DocumentSnapshot doc) {
        AdminReservation r = new AdminReservation();
        r.reservationId = doc.getId();

        r.reservedBy = doc.getString("reserved_by");
        r.reservedCharity = doc.getString("reserved_charity");
        r.sellerId = doc.getString("seller_id");
        r.sellerName = doc.getString("seller_name");
        r.pickupDate = doc.getString("pickup_date");
        r.quantity = doc.getLong("quantity");
        r.status = doc.getString("status");

        return r;
    }
}
