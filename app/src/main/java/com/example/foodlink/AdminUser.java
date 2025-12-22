package com.example.foodlink;

import com.google.firebase.firestore.DocumentSnapshot;

public class AdminUser {
    public String uid;
    public String email;
    public String fullName;
    public String userType;
    public Boolean isActive;

    public static AdminUser fromDoc(DocumentSnapshot doc) {
        AdminUser u = new AdminUser();
        u.uid = doc.getId();
        u.email = doc.getString("email");
        u.fullName = doc.getString("full_name");
        u.userType = doc.getString("user_type");
        u.isActive = doc.getBoolean("is_active");
        return u;
    }
}
