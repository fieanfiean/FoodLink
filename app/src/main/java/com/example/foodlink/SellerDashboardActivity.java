package com.example.foodlink;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class SellerDashboardActivity extends AppCompatActivity {

    private static final String TAG = "SellerDashboard";
    private TextView tvRestaurantName;
    private TextView tvActiveCount, tvReservedCount, tvCompletedCount, tvTotalImpact;
    private CardView cardEmptyState;
    private BottomNavigationView bottomNavigation;
    private LinearLayout llListingsContainer;

    // Firestore
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private ListenerRegistration activeListener, reservedListener, completedListener;

    // Current user
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seller_dashboard);

        try {
            // Initialize Firebase
            db = FirebaseFirestore.getInstance();
            mAuth = FirebaseAuth.getInstance();

            // Get current user
            FirebaseUser currentUser = mAuth.getCurrentUser();
            if (currentUser == null) {
                // User not logged in, go to login
                Toast.makeText(this, "Please login again", Toast.LENGTH_SHORT).show();
                navigateToLogin();
                return;
            }
            currentUserId = currentUser.getUid();

            // Initialize views
            initViews();
            setupClickListeners();
            setupBottomNavigation();

            // Load user data
            loadUserData();

            // Start listening for real-time updates
            setupRealTimeListeners();

        } catch (Exception e) {
            Toast.makeText(this, "Error in dashboard: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
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
        llListingsContainer = findViewById(R.id.llListingsContainer);

        // Initially hide the container since it's empty
        llListingsContainer.setVisibility(View.GONE);
    }

    private void loadUserData() {
        // Fetch user data to get restaurant/business name
        db.collection("users").document(currentUserId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String restaurantName = documentSnapshot.getString("business_name");
                        if (restaurantName == null || restaurantName.isEmpty()) {
                            restaurantName = documentSnapshot.getString("full_name");
                        }
                        if (restaurantName == null || restaurantName.isEmpty()) {
                            restaurantName = "My Restaurant";
                        }
                        tvRestaurantName.setText(restaurantName);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading user data: " + e.getMessage());
                    tvRestaurantName.setText("My Restaurant");
                });
    }

    private void setupRealTimeListeners() {
        // ========== ACTIVE LISTINGS COUNT & DISPLAY ==========
        Query activeQuery = db.collection("food_listings")
                .whereEqualTo("seller_id", currentUserId)
                .whereEqualTo("status", "available");

        activeListener = activeQuery.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot querySnapshot,
                                @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    Log.w(TAG, "Active listings listen failed.", error);
                    Toast.makeText(SellerDashboardActivity.this,
                            "Error loading listings: " + error.getMessage(),
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                if (querySnapshot != null) {
                    int count = querySnapshot.size();
                    tvActiveCount.setText(String.valueOf(count));

                    // Update active listings display
                    updateActiveListings(querySnapshot);

                    // Update empty state
                    updateEmptyState(count);

                    Log.d(TAG, "Active listings updated: " + count);
                }
            }
        });

        // ========== RESERVED LISTINGS COUNT ==========
        Query reservedQuery = db.collection("food_listings")
                .whereEqualTo("seller_id", currentUserId)
                .whereEqualTo("status", "reserved");

        reservedListener = reservedQuery.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot querySnapshot,
                                @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    Log.w(TAG, "Reserved listings listen failed.", error);
                    return;
                }

                int count = querySnapshot != null ? querySnapshot.size() : 0;
                tvReservedCount.setText(String.valueOf(count));
                Log.d(TAG, "Reserved listings updated: " + count);
            }
        });

        // ========== COMPLETED LISTINGS COUNT ==========
        Query completedQuery = db.collection("food_listings")
                .whereEqualTo("seller_id", currentUserId)
                .whereEqualTo("status", "completed");

        completedListener = completedQuery.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot querySnapshot,
                                @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    Log.w(TAG, "Completed listings listen failed.", error);
                    return;
                }

                int count = querySnapshot != null ? querySnapshot.size() : 0;
                tvCompletedCount.setText(String.valueOf(count));

                // Update total impact (example: 5kg per completed listing)
//                updateTotalImpact(count);
//                Log.d(TAG, "Completed listings updated: " + count);
            }
        });

        // ========== TOTAL QUANTITY FOR SELLER ==========
        Query totalQuantityQuery = db.collection("food_listings")
                .whereEqualTo("seller_id", currentUserId);

        completedListener = totalQuantityQuery.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot querySnapshot,
                                @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    Log.w(TAG, "Total quantity listen failed.", error);
                    return;
                }

                int totalQuantity = 0;
                if (querySnapshot != null && !querySnapshot.isEmpty()) {
                    // Loop through all seller's listings and sum quantities
                    for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                        try {
                            // Get quantity as number (handle multiple data types)
                            Object quantityObj = document.get("quantity");

                            if (quantityObj != null) {
                                if (quantityObj instanceof Long) {
                                    totalQuantity += ((Long) quantityObj).intValue();
                                } else if (quantityObj instanceof Double) {
                                    totalQuantity += ((Double) quantityObj).intValue();
                                } else if (quantityObj instanceof Integer) {
                                    totalQuantity += (Integer) quantityObj;
                                } else if (quantityObj instanceof String) {
                                    try {
                                        totalQuantity += Integer.parseInt((String) quantityObj);
                                    } catch (NumberFormatException e) {
                                        Log.w(TAG, "Could not parse quantity string: " + quantityObj);
                                    }
                                } else {
                                    Log.w(TAG, "Unknown quantity type: " + quantityObj.getClass().getName());
                                }
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error getting quantity from document: " + e.getMessage());
                        }
                    }

                    Log.d(TAG, "Total quantity calculated: " + totalQuantity +
                            " (from " + querySnapshot.size() + " listings)");
                } else {
                    // No listings found for this seller
                    Log.d(TAG, "No listings found for seller");
                }

                // Update UI with total quantity sum
                tvTotalImpact.setText(String.valueOf(totalQuantity) + "kg");

                // Update total impact with actual quantity values
//                updateTotalImpact(totalQuantity);
            }
        });
    }

    private void updateActiveListings(QuerySnapshot querySnapshot) {
        // Clear all existing views in the container
        llListingsContainer.removeAllViews();

        if (querySnapshot.isEmpty()) {
            // No active listings - container remains empty
            llListingsContainer.setVisibility(View.GONE);
            return;
        }

        // Show the container since we have listings
        llListingsContainer.setVisibility(View.VISIBLE);

        // For each active listing, create and add a CardView
        for (DocumentSnapshot document : querySnapshot.getDocuments()) {
            try {
                // Get the listing data
                String foodName = document.getString("food_name");
                Long quantityLong = document.getLong("quantity"); // Firestore stores numbers as Long
                String quantity = quantityLong != null ? String.valueOf(quantityLong) : "N/A";
                String category = document.getString("category");
                String expiryDate = document.getString("expiry_date");
                String startTime = document.getString("start_time");
                String endTime = document.getString("end_time");
                String status = document.getString("status");
                String listingId = document.getString("listing_id");
                String documentId = document.getId();

                String imageBase64 = document.getString("image_base64");

                // ADD DEBUG LOGGING
                Log.d(TAG, "========== DEBUG IMAGE DATA ==========");
                Log.d(TAG, "Food Name: " + foodName);

                if (imageBase64 == null) {
                    Log.d(TAG, "image_base64 is NULL");
                    // Check alternative field names
                    imageBase64 = document.getString("imageBase64");
                    Log.d(TAG, "Tried imageBase64: " + (imageBase64 != null ? "Found" : "NULL"));
                }

                if (imageBase64 != null) {
                    Log.d(TAG, "Image Base64 length: " + imageBase64.length());
                    Log.d(TAG, "First 50 chars: " + imageBase64.substring(0, Math.min(50, imageBase64.length())));

                    // Check if it's a valid Base64 string
                    if (imageBase64.startsWith("data:image")) {
                        Log.d(TAG, "Image has data URI prefix");
                    }

                    if (imageBase64.length() > 1000000) { // > 1MB
                        Log.w(TAG, "WARNING: Image is very large (" + imageBase64.length() + " chars)");
                    }
                }

                if (foodName == null || foodName.isEmpty()) {
                    continue; // Skip invalid listings
                }

                // Create a CardView for this listing
                CardView listingCard = createListingCard(
                        foodName, quantity, category, expiryDate,
                        startTime, endTime, status, documentId, listingId, imageBase64
                );

                // Add the CardView to the container
                if (listingCard != null) {
                    llListingsContainer.addView(listingCard);
                }

            } catch (Exception e) {
                Log.e(TAG, "Error creating listing card: " + e.getMessage());
            }
        }
    }

    private CardView createListingCard(String foodName, String quantity, String category,
                                       String expiryDate, String startTime, String endTime,
                                       String status, String documentId, String listingId, String imageBase64) {
        try {
            // Inflate the listing card layout
            LayoutInflater inflater = LayoutInflater.from(this);
            CardView cardView = (CardView) inflater.inflate(
                    R.layout.item_food_listing, llListingsContainer, false);

            // Get references to views inside the card
            TextView tvFoodName = cardView.findViewById(R.id.tvFoodName);
            TextView tvStatus = cardView.findViewById(R.id.tvStatus);
            TextView tvCategoryQuantity = cardView.findViewById(R.id.tvCategoryQuantity);
            TextView tvExpiry = cardView.findViewById(R.id.tvExpiry);
            TextView tvPickupTime = cardView.findViewById(R.id.tvPickupTime);
            MaterialButton btnEdit = cardView.findViewById(R.id.btnEdit);
            MaterialButton btnAction = cardView.findViewById(R.id.btnAction);

            ImageView ivFoodImage = cardView.findViewById(R.id.ivFoodImage);
            TextView tvNoImage = cardView.findViewById(R.id.tvNoImage);

            boolean imageLoaded = false;

            if (imageBase64 != null && !imageBase64.trim().isEmpty() && !imageBase64.equals("null")) {
                try {
                    Log.d(TAG, "Loading image for: " + foodName);

                    // Clean the Base64 string
                    String cleanBase64 = imageBase64.trim();

                    // Remove data URI prefix if present
                    if (cleanBase64.contains("base64,")) {
                        cleanBase64 = cleanBase64.substring(cleanBase64.indexOf("base64,") + 7);
                    }

                    // Decode Base64
                    byte[] decodedBytes = Base64.decode(cleanBase64, Base64.DEFAULT);

                    if (decodedBytes != null && decodedBytes.length > 0) {
                        Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);

                        if (bitmap != null) {
                            ivFoodImage.setImageBitmap(bitmap);
                            tvNoImage.setVisibility(View.VISIBLE);
                            imageLoaded = true;
                            Log.d(TAG, "✓ Image loaded successfully: " + bitmap.getWidth() + "x" + bitmap.getHeight());
                        } else {
                            Log.e(TAG, "✗ BitmapFactory returned null");
                        }
                    } else {
                        Log.e(TAG, "✗ Decoded bytes are null or empty");
                    }

                } catch (IllegalArgumentException e) {
                    Log.e(TAG, "✗ Invalid Base64 format: " + e.getMessage());
                } catch (Exception e) {
                    Log.e(TAG, "✗ Error loading image: " + e.getMessage());
                }
            }

            // If image loading failed, show default
            if (!imageLoaded) {
                ivFoodImage.setImageResource(R.drawable.ic_food);
                tvNoImage.setVisibility(View.GONE);
                Log.d(TAG, "Showing default image for: " + foodName);
            }

            // Set data from listing
            tvFoodName.setText(foodName != null ? foodName : "Food Item");
            tvStatus.setText(status != null ? status : "Available");

            // Format category and quantity
            String categoryText = category != null ? category : "Food";
            String quantityText = quantity != null ? quantity : "N/A";
            tvCategoryQuantity.setText(categoryText + " • " + quantityText + "kg");

            // Format expiry date
            tvExpiry.setText("Expiry: " + (expiryDate != null ? expiryDate : "N/A"));

            // Format pickup time
            String pickupStart = startTime != null ? startTime : "N/A";
            String pickupEnd = endTime != null ? endTime : "N/A";
            tvPickupTime.setText("Pickup: " + pickupStart + " - " + pickupEnd);

            // Set status color and button text based on status
            String currentStatus = status != null ? status.toLowerCase() : "available";
            switch (currentStatus) {
                case "available":
                    tvStatus.setTextColor(ContextCompat.getColor(this, R.color.green_500));
                    tvStatus.setBackgroundResource(R.drawable.status_available_bg);
                    btnAction.setText("Mark Reserved");
                    btnAction.setBackgroundTintList(ColorStateList.valueOf(
                            ContextCompat.getColor(this, R.color.green_500)));
                    btnAction.setOnClickListener(v -> updateListingStatus(documentId, "available"));
                    break;
                case "reserved":
                    tvStatus.setTextColor(ContextCompat.getColor(this, R.color.orange_500));
                    tvStatus.setBackgroundResource(R.drawable.status_reserved_bg);
                    btnAction.setText("Mark Complete");
                    btnAction.setBackgroundTintList(ColorStateList.valueOf(
                            ContextCompat.getColor(this, R.color.orange_500)));
                    btnAction.setOnClickListener(v -> updateListingStatus(documentId, "reserved"));
                    break;
                case "completed":
                    tvStatus.setTextColor(ContextCompat.getColor(this, R.color.blue_500));
                    tvStatus.setBackgroundResource(R.drawable.status_complete_bg);
                    btnAction.setText("Completed");
                    btnAction.setBackgroundTintList(ColorStateList.valueOf(
                            ContextCompat.getColor(this, R.color.blue_500)));
                    btnAction.setEnabled(false); // Disable button for completed
                    break;
                default:
                    tvStatus.setTextColor(ContextCompat.getColor(this, R.color.green_500));
                    tvStatus.setBackgroundResource(R.drawable.status_available_bg);
                    btnAction.setText("Mark Reserved");
                    btnAction.setBackgroundTintList(ColorStateList.valueOf(
                            ContextCompat.getColor(this, R.color.green_500)));
                    btnAction.setOnClickListener(v -> updateListingStatus(documentId, "available"));
            }

            // Set edit button click listener
            btnEdit.setOnClickListener(v -> editListing(documentId));

            // Set click listener for the whole card
            cardView.setOnClickListener(v -> viewListingDetails(documentId));

            return cardView;

        } catch (Exception e) {
            Log.e(TAG, "Error inflating listing card: " + e.getMessage());
            return null;
        }
    }

    private void viewFullImage(String imageUrl, String foodName) {
        Toast.makeText(this, "Viewing image for: " + foodName, Toast.LENGTH_SHORT).show();
        // You can implement a full-screen image viewer here
        // Intent intent = new Intent(this, FullScreenImageActivity.class);
        // intent.putExtra("image_url", imageUrl);
        // intent.putExtra("food_name", foodName);
        // startActivity(intent);
    }

    private void updateListingStatus(String documentId, String currentStatus) {
        String newStatus;
        switch (currentStatus.toLowerCase()) {
            case "available":
                newStatus = "reserved";
                break;
            case "reserved":
                newStatus = "completed";
                break;
            default:
                return;
        }

        // Update status in Firestore
        db.collection("food_listings").document(documentId)
                .update("status", newStatus)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Status updated to " + newStatus, Toast.LENGTH_SHORT).show();
                    // The listener will automatically refresh the UI
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to update status: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error updating status: " + e.getMessage());
                });
    }

    private void editListing(String documentId) {
        Toast.makeText(this, "Edit listing: " + documentId, Toast.LENGTH_SHORT).show();
        // TODO: Implement edit functionality
        // Intent intent = new Intent(this, EditFoodListingActivity.class);
        // intent.putExtra("document_id", documentId);
        // startActivity(intent);
    }

    private void viewListingDetails(String documentId) {
        Toast.makeText(this, "View details: " + documentId, Toast.LENGTH_SHORT).show();
        // TODO: Implement details view
        // Intent intent = new Intent(this, FoodListingDetailsActivity.class);
        // intent.putExtra("document_id", documentId);
        // startActivity(intent);
    }

    private void updateEmptyState(int activeCount) {
        if (activeCount == 0) {
            cardEmptyState.setVisibility(View.VISIBLE);
        } else {
            cardEmptyState.setVisibility(View.GONE);
        }
    }

    private void updateTotalImpact(int completedCount) {
        // Calculate impact: example 5kg per completed listing
        int totalKg = completedCount * 5;
        tvTotalImpact.setText(totalKg + "kg");
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
    }

    private void setupBottomNavigation() {
        bottomNavigation.getMenu().clear();

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
                    return true;
                } else if (itemId == R.id.nav_add) {
                    navigateToAddListing();
                    return true;
                } else if (itemId == R.id.nav_profile) {
                    navigateToProfile();
                    return true;
                }
                return false;
            }
        });

        bottomNavigation.setSelectedItemId(R.id.nav_dashboard);
    }

    // Navigation methods
    private void navigateToAddListing() {
        Intent intent = new Intent(this, AddNewFoodListingActivity.class);
        startActivity(intent);
        // Don't finish() - user might want to come back
    }

    private void navigateToAllListings() {
        Toast.makeText(this, "Navigate to All Listings", Toast.LENGTH_SHORT).show();
        // TODO: Implement all listings view
        // Intent intent = new Intent(this, AllListingsActivity.class);
        // startActivity(intent);
    }

    private void navigateToNotifications() {
        Toast.makeText(this, "Navigate to Notifications", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, NotificationsActivity.class);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        startActivity(intent);
    }

    private void navigateToProfile() {
        Intent intent = new Intent(this, ProfileActivity.class);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        startActivity(intent);
    }

    private void navigateToLogin() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Remove listeners to save resources
        removeListeners();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        // Re-attach listeners when activity comes back
        if (currentUserId != null) {
            setupRealTimeListeners();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        removeListeners();
    }

    private void removeListeners() {
        if (activeListener != null) {
            activeListener.remove();
        }
        if (reservedListener != null) {
            reservedListener.remove();
        }
        if (completedListener != null) {
            completedListener.remove();
        }
    }
}