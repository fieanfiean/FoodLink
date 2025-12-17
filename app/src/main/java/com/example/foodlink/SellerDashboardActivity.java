package com.example.foodlink;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class SellerDashboardActivity extends AppCompatActivity {

    private TextView tvRestaurantName;
    private TextView tvActiveCount, tvReservedCount, tvCompletedCount, tvTotalImpact;
    private CardView cardEmptyState;
    private BottomNavigationView bottomNavigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seller_dashboard);

        try {
            // Initialize views
            initViews();
            setupClickListeners();
            setupBottomNavigation();
            loadDashboardData();
        } catch (Exception e) {
            Toast.makeText(this, "Error in dashboard: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
            // Optional: Go back to login
            finish();
        }
    }

    private void initViews() {
        tvRestaurantName = findViewById(R.id.tvRestaurantName);
        tvActiveCount = findViewById(R.id.tvActiveCount);
        tvReservedCount = findViewById(R.id.tvReservedCount);
        tvCompletedCount = findViewById(R.id.tvCompletedCount);
        tvTotalImpact = findViewById(R.id.tvTotalImpact);
        cardEmptyState = findViewById(R.id.cardEmptyState);
        bottomNavigation = findViewById(R.id.bottomNavigation);

        // Get restaurant name from intent or shared preferences
        String restaurantName = getIntent().getStringExtra("restaurant_name");
        if (restaurantName != null) {
            tvRestaurantName.setText(restaurantName);
        }
    }

    private void setupClickListeners() {
        // Add New Listing Button
        findViewById(R.id.btnAddListing).setOnClickListener(v -> {
            navigateToAddListing();
        });

        // View All Listings
        findViewById(R.id.tvViewAll).setOnClickListener(v -> {
            navigateToAllListings();
        });

        // Notification icon
        findViewById(R.id.ivNotification).setOnClickListener(v -> {
            navigateToNotifications();
        });

        // Profile icon
        findViewById(R.id.ivProfile).setOnClickListener(v -> {
            navigateToProfile();
        });

//        // Edit Listing 1
//        findViewById(R.id.tvStatus1).getParent().findViewById(R.id.btnEdit1).setOnClickListener(v -> {
//            editListing(1);
//        });
//
//        // Mark Reserved for Listing 1
//        findViewById(R.id.tvStatus1).getParent().findViewById(R.id.btnReserve1).setOnClickListener(v -> {
//            markAsReserved(1);
//        });
//
//        // View Details for Listing 2
//        findViewById(R.id.tvStatus2).getParent().findViewById(R.id.btnDetails2).setOnClickListener(v -> {
//            viewListingDetails(2);
//        });
//
//        // Re-list for Listing 2
//        findViewById(R.id.tvStatus2).getParent().findViewById(R.id.btnRelist2).setOnClickListener(v -> {
//            reListListing(2);
//        });
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
                    // Already on dashboard
                    return true;
                }
                else if (itemId == R.id.nav_add) {
                    navigateToAddListing();
                    return true;
                }
                else if (itemId == R.id.nav_profile) {
                    navigateToProfile();
                    return true;
                }
                return false;
            }
        });

        // Set dashboard as selected
        bottomNavigation.setSelectedItemId(R.id.nav_dashboard);
    }

    private void loadDashboardData() {
        // Here you would fetch data from API/database
        // For now, set sample data

        // Sample data
        int activeListings = 2;
        int reservedListings = 0;
        int completedListings = 5;
        String totalImpact = "30kg";

        // Update UI
        tvActiveCount.setText(String.valueOf(activeListings));
        tvReservedCount.setText(String.valueOf(reservedListings));
        tvCompletedCount.setText(String.valueOf(completedListings));
        tvTotalImpact.setText(totalImpact);

        // Show/hide empty state
        if (activeListings == 0) {
            cardEmptyState.setVisibility(View.VISIBLE);
            findViewById(R.id.llListingsContainer).setVisibility(View.GONE);
        } else {
            cardEmptyState.setVisibility(View.GONE);
            findViewById(R.id.llListingsContainer).setVisibility(View.VISIBLE);
        }
    }

    // Navigation methods
    private void navigateToAddListing() {
        Toast.makeText(this, "Navigate to Add Listing", Toast.LENGTH_SHORT).show();
         Intent intent = new Intent(this, AddNewFoodListingActivity.class);
         startActivity(intent);
    }

    private void navigateToAllListings() {
        Toast.makeText(this, "Navigate to All Listings", Toast.LENGTH_SHORT).show();
        // Intent intent = new Intent(this, AllListingsActivity.class);
        // startActivity(intent);
    }

    private void navigateToNotifications() {
        Toast.makeText(this, "Navigate to Notifications", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, NotificationsActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    private void navigateToProfile() {
        Toast.makeText(this, "Navigate to Profile", Toast.LENGTH_SHORT).show();
         Intent intent = new Intent(this, ProfileActivity.class);
         startActivity(intent);
         overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    private void navigateToReservations() {
        Toast.makeText(this, "Navigate to Reservations", Toast.LENGTH_SHORT).show();
        // Intent intent = new Intent(this, ReservationsActivity.class);
        // startActivity(intent);
    }

    // Listing actions
    private void editListing(int listingId) {
        Toast.makeText(this, "Edit Listing " + listingId, Toast.LENGTH_SHORT).show();
    }

    private void markAsReserved(int listingId) {
        Toast.makeText(this, "Mark Listing " + listingId + " as Reserved", Toast.LENGTH_SHORT).show();
        // Update status in UI
        TextView statusView = findViewById(listingId == 1 ? R.id.tvStatus1 : R.id.tvStatus2);
        statusView.setText("Reserved");
        statusView.setBackgroundResource(R.drawable.status_reserved_bg);
        statusView.setTextColor(getResources().getColor(android.R.color.holo_orange_dark));
    }

    private void viewListingDetails(int listingId) {
        Toast.makeText(this, "View Details for Listing " + listingId, Toast.LENGTH_SHORT).show();
    }

    private void reListListing(int listingId) {
        Toast.makeText(this, "Re-list Listing " + listingId, Toast.LENGTH_SHORT).show();
        // Update status in UI
        TextView statusView = findViewById(listingId == 1 ? R.id.tvStatus1 : R.id.tvStatus2);
        statusView.setText("Available");
        statusView.setBackgroundResource(R.drawable.status_available_bg);
        statusView.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
    }

//    @Override
//    public void onBackPressed() {
//        // Exit app when back pressed from dashboard
//        finishAffinity();
//    }
}