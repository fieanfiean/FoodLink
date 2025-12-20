// Profile.java
package com.example.foodlink;

public class Profile {
    private String userId;
    private String name;
    private String email;
    private String phone;
    private String address;
    private String userType; // "seller" or "charity"
    private String profileImageUrl;

    // Sustainability metrics
    private int foodSavedKg;
    private int donationsMade;
    private int co2ReducedKg;
    private int charitiesHelped;

    // Constructors
    public Profile() {}

    public Profile(String userId, String name, String email, String userType) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.userType = userType;
    }

    // Getters and Setters
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getUserType() { return userType; }
    public void setUserType(String userType) { this.userType = userType; }

    public String getProfileImageUrl() { return profileImageUrl; }
    public void setProfileImageUrl(String profileImageUrl) { this.profileImageUrl = profileImageUrl; }

    public int getFoodSavedKg() { return foodSavedKg; }
    public void setFoodSavedKg(int foodSavedKg) { this.foodSavedKg = foodSavedKg; }

    public int getDonationsMade() { return donationsMade; }
    public void setDonationsMade(int donationsMade) { this.donationsMade = donationsMade; }

    public int getCo2ReducedKg() { return co2ReducedKg; }
    public void setCo2ReducedKg(int co2ReducedKg) { this.co2ReducedKg = co2ReducedKg; }

    public int getCharitiesHelped() { return charitiesHelped; }
    public void setCharitiesHelped(int charitiesHelped) { this.charitiesHelped = charitiesHelped; }

}