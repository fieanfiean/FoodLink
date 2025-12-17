// SessionManager.java
package com.example.foodlink;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private static final String PREF_NAME = "FoodLinkPref";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_USER_TYPE = "userType";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_RESTAURANT_NAME = "restaurantName";

    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private Context context;

    // Constructor
    public SessionManager(Context context) {
        this.context = context;
        pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = pref.edit();
    }

    // Create login session
    public void createLoginSession(String userType, String email, String restaurantName) {
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putString(KEY_USER_TYPE, userType);
        editor.putString(KEY_EMAIL, email);
        editor.putString(KEY_RESTAURANT_NAME, restaurantName);
        editor.commit();
    }

    // Check login status
    public boolean isLoggedIn() {
        return pref.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    // Get user type
    public String getUserType() {
        return pref.getString(KEY_USER_TYPE, "");
    }

    // Get email
    public String getEmail() {
        return pref.getString(KEY_EMAIL, "");
    }

    // Get restaurant name
    public String getRestaurantName() {
        return pref.getString(KEY_RESTAURANT_NAME, "Green Leaf Restaurant");
    }

    // Logout - clear session
    public void logout() {
        editor.clear();
        editor.commit();
    }

    // Additional getters if needed
    public SharedPreferences getPref() {
        return pref;
    }

    public Context getContext() {
        return context;
    }
}