// ProfileActivity.java
package com.example.foodlink;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.example.foodlink.Profile;
import com.example.foodlink.SessionManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class ProfileActivity extends AppCompatActivity {

    private TextView tvProfileName, tvUserType, tvEmail, tvPhone, tvAddress;
    private TextView tvFoodSaved, tvDonationsMade, tvCo2Reduced, tvCharitiesHelped;

    private BottomNavigationView bottomNavigation;
    private Profile userProfile;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Hide default action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        setContentView(R.layout.activity_profile);

        sessionManager = new SessionManager(this);
        initViews();
        setupClickListeners();
        setupBottomNavigation();
        loadProfileData();
    }

    private void initViews() {
        // Back button
//        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // Profile info views
        tvProfileName = findViewById(R.id.tvProfileName);
        tvUserType = findViewById(R.id.tvUserType);
        tvEmail = findViewById(R.id.tvEmail);
        tvPhone = findViewById(R.id.tvPhone);
        tvAddress = findViewById(R.id.tvAddress);

        // Impact metrics views
        tvFoodSaved = findViewById(R.id.tvFoodSaved);
        tvDonationsMade = findViewById(R.id.tvDonationsMade);
        tvCo2Reduced = findViewById(R.id.tvCo2Reduced);
        tvCharitiesHelped = findViewById(R.id.tvCharitiesHelped);

        bottomNavigation = findViewById(R.id.bottomNavigation);

    }

    private void setupClickListeners() {
        // Edit Profile
        findViewById(R.id.btnEditProfile).setOnClickListener(v -> {
            navigateToEditProfile();
        });

        // Notification Settings
        findViewById(R.id.btnNotificationSettings).setOnClickListener(v -> {
            navigateToNotificationSettings();
        });

        // Privacy & Security
        findViewById(R.id.btnPrivacySecurity).setOnClickListener(v -> {
            navigateToPrivacySecurity();
        });

        // Help & Support
        findViewById(R.id.btnHelpSupport).setOnClickListener(v -> {
            navigateToHelpSupport();
        });

        // Logout
        findViewById(R.id.btnLogout).setOnClickListener(v -> {
            showLogoutConfirmation();
        });
    }

    private void setupBottomNavigation() {
        // Clear existing menu items
        bottomNavigation.getMenu().clear();

        // Add only the items you want for seller
        bottomNavigation.getMenu().add(Menu.NONE, R.id.nav_dashboard, 1, "Dashboard")
                .setIcon(R.drawable.ic_dashboard);

        bottomNavigation.getMenu().add(Menu.NONE, R.id.nav_add, 2, "Add")
                .setIcon(R.drawable.ic_add);

        bottomNavigation.getMenu().add(Menu.NONE, R.id.nav_profile, 3, "Profile")
                .setIcon(R.drawable.ic_profile);

        bottomNavigation.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();

                if (itemId == R.id.nav_dashboard) {
                    navigateToDashboard();
                    return true;
                }
                else if (itemId == R.id.nav_add) {
                    navigateToAddListing();
                    return true;
                }
                else if (itemId == R.id.nav_profile) {
                    return true;
                }
                return false;
            }
        });

        // Set dashboard as selected
        bottomNavigation.setSelectedItemId(R.id.nav_profile);
    }

    private void loadProfileData() {
        // For demo, create a sample profile
        // In real app, fetch from database/API

        userProfile = new Profile();
        userProfile.setUserId("seller_001");
        userProfile.setName("Green Leaf Restaurant");
        userProfile.setEmail("1@1");
        userProfile.setPhone("+1 234 567 8900");
        userProfile.setAddress("123 Eco Street, Sustainable City");
        userProfile.setUserType("seller");

        // Set sustainability metrics
        userProfile.setFoodSavedKg(127);
        userProfile.setDonationsMade(42);
        userProfile.setCo2ReducedKg(185);
        userProfile.setCharitiesHelped(8);

        // Update UI with profile data
        updateProfileUI();
    }

    private void updateProfileUI() {
        if (userProfile != null) {
            tvProfileName.setText(userProfile.getName());
            tvUserType.setText("Food Seller");
            tvEmail.setText(userProfile.getEmail());
            tvPhone.setText(userProfile.getPhone());
            tvAddress.setText(userProfile.getAddress());

            // Update impact metrics
            tvFoodSaved.setText(userProfile.getFoodSavedKg() + "kg");
            tvDonationsMade.setText(String.valueOf(userProfile.getDonationsMade()));
            tvCo2Reduced.setText(userProfile.getCo2ReducedKg() + "kg");
            tvCharitiesHelped.setText(String.valueOf(userProfile.getCharitiesHelped()));
        }
    }

    private void navigateToEditProfile() {
        Toast.makeText(this, "Edit Profile clicked", Toast.LENGTH_SHORT).show();
         Intent intent = new Intent(this, EditProfileActivity.class);
         startActivity(intent);
//         intent.putExtra("profile", userProfile);
        // startActivityForResult(intent, EDIT_PROFILE_REQUEST);
    }

    private void navigateToNotificationSettings() {
        Toast.makeText(this, "Notification Settings clicked", Toast.LENGTH_SHORT).show();
        // Intent intent = new Intent(this, NotificationSettingsActivity.class);
        // startActivity(intent);
    }

    private void navigateToPrivacySecurity() {
        Toast.makeText(this, "Privacy & Security clicked", Toast.LENGTH_SHORT).show();
        // Intent intent = new Intent(this, PrivacySecurityActivity.class);
        // startActivity(intent);
    }

    private void navigateToHelpSupport() {
        Toast.makeText(this, "Help & Support clicked", Toast.LENGTH_SHORT).show();
        // Intent intent = new Intent(this, HelpSupportActivity.class);
        // startActivity(intent);
    }

    private void showLogoutConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Logout", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        logout();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void navigateToDashboard() {
        Toast.makeText(this, "Navigate to Dashboard", Toast.LENGTH_SHORT).show();
        bottomNavigation.setSelectedItemId(R.id.nav_add);
        Intent intent = new Intent(this, SellerDashboardActivity.class);
        startActivity(intent);
        finish();
    }
    private void navigateToAddListing() {
        Toast.makeText(this, "Navigate to Add Listing", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, AddNewFoodListingActivity.class);
        startActivity(intent);
        finish();
    }

    private void logout() {
        // Clear session
//        sessionManager.logout();

        // Navigate to login screen
        Intent intent = new Intent(this, MainActivity.class);
//        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();

        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
    }

    // Handle result from EditProfileActivity
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // if (requestCode == EDIT_PROFILE_REQUEST && resultCode == RESULT_OK) {
        //     // Profile updated, refresh data
        //     loadProfileData();
        // }
    }
}