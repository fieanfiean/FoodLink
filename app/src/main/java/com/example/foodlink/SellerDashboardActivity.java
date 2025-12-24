package com.example.foodlink;

import static android.widget.Toast.LENGTH_SHORT;

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

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

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
    private ListenerRegistration allListingsListener, totalQuantityListener;

    // Current user
    private String currentUserId;

    private ActivityResultLauncher<Intent> editListingLauncher;

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
                Toast.makeText(this, "Please login again", LENGTH_SHORT).show();
                navigateToLogin();
                return;
            }
            currentUserId = currentUser.getUid();

            // Initialize views
            initViews();
            setupClickListeners();
            setupBottomNavigation();
            setupBackPressHandler();
            setupActivityResultLauncher();

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

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.d(TAG, "onNewIntent() called");

        if (intent != null && intent.getBooleanExtra("refresh_listings", false)) {
            Log.d(TAG, "Refresh requested, forcing reload");

            // Force refresh by removing and re-adding listeners
            removeListeners();
            if (currentUserId != null) {
                setupRealTimeListeners();
            }

            String newListingId = intent.getStringExtra("new_listing_id");
            if (newListingId != null) {
                Toast.makeText(this, "New listing added!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void loadUserData() {
        db.collection("users").document(currentUserId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String restaurantName = documentSnapshot.getString("business_name");
                        if (restaurantName == null || restaurantName.isEmpty()) {
                            restaurantName = documentSnapshot.getString("full_name");
                        }
                        if (restaurantName == null || restaurantName.isEmpty()) {
                            restaurantName = documentSnapshot.getString("name");
                        }
                        if (restaurantName == null || restaurantName.isEmpty()) {
                            restaurantName = "My Restaurant";
                        }
                        tvRestaurantName.setText(restaurantName);
                        Log.d(TAG, "Restaurant name loaded: " + restaurantName);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading user data: " + e.getMessage());
                    tvRestaurantName.setText("My Restaurant");
                });
    }

    private void setupRealTimeListeners() {
        Log.d(TAG, "=== SETTING UP REAL-TIME LISTENERS ===");
        Log.d(TAG, "Current user ID: " + currentUserId);

        // ========== ALL LISTINGS (ACTIVE, RESERVED, COMPLETED) ==========
        // Remove orderBy since created_at is stored as a number, not a Timestamp
        Query allListingsQuery = db.collection("food_listings")
                .whereEqualTo("seller_id", currentUserId);
        // Remove: .orderBy("created_at", Query.Direction.DESCENDING);

        allListingsListener = allListingsQuery.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot querySnapshot,
                                @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    Log.w(TAG, "All listings listen failed.", error);
                    Toast.makeText(SellerDashboardActivity.this,
                            "Error loading listings: " + error.getMessage(),
                            LENGTH_SHORT).show();
                    return;
                }

                if (querySnapshot != null) {
                    // Convert to list for sorting
                    List<DocumentSnapshot> documents = new ArrayList<>(querySnapshot.getDocuments());

                    // Sort manually by created_at (descending - newest first)
                    Collections.sort(documents, (doc1, doc2) -> {
                        Long time1 = getCreatedAtAsLong(doc1);
                        Long time2 = getCreatedAtAsLong(doc2);
                        return time2.compareTo(time1); // Descending order
                    });

                    int totalCount = documents.size();

                    // Calculate counts by status
                    int activeCount = 0;
                    int reservedCount = 0;
                    int completedCount = 0;

                    for (DocumentSnapshot doc : documents) {
                        String status = doc.getString("status");
                        if (status != null) {
                            switch (status.toLowerCase()) {
                                case "available":
                                    activeCount++;
                                    break;
                                case "reserved":
                                    reservedCount++;
                                    break;
                                case "completed":
                                    completedCount++;
                                    break;
                            }
                        }
                    }

                    // Update all count displays
                    tvActiveCount.setText(String.valueOf(activeCount));
                    tvReservedCount.setText(String.valueOf(reservedCount));
                    tvCompletedCount.setText(String.valueOf(completedCount));

                    // Update the listings display with ALL listings (sorted)
                    updateAllListings(documents);

                    // Update empty state
                    updateEmptyState(totalCount);

                    Log.d(TAG, "All listings updated - Total: " + totalCount +
                            " (Active: " + activeCount +
                            ", Reserved: " + reservedCount +
                            ", Completed: " + completedCount + ")");
                }
            }
        });

        // ========== TOTAL QUANTITY FOR SELLER ==========
        Query totalQuantityQuery = db.collection("food_listings")
                .whereEqualTo("seller_id", currentUserId);

        totalQuantityListener = totalQuantityQuery.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot querySnapshot,
                                @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    Log.w(TAG, "Total quantity listen failed.", error);
                    return;
                }

                int totalQuantity = 0;
                if (querySnapshot != null && !querySnapshot.isEmpty()) {
                    for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                        try {
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
                                }
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error getting quantity: " + e.getMessage());
                        }
                    }
                    Log.d(TAG, "Total quantity calculated: " + totalQuantity);
                }

                // Update UI with total quantity sum
                tvTotalImpact.setText(totalQuantity + "kg");
            }
        });
    }

    private Long getCreatedAtAsLong(DocumentSnapshot document) {
        try {
            Object createdAtObj = document.get("created_at");
            if (createdAtObj == null) return 0L;

            if (createdAtObj instanceof Long) {
                return (Long) createdAtObj;
            } else if (createdAtObj instanceof Double) {
                return ((Double) createdAtObj).longValue();
            } else if (createdAtObj instanceof Integer) {
                return ((Integer) createdAtObj).longValue();
            } else if (createdAtObj instanceof String) {
                try {
                    return Long.parseLong((String) createdAtObj);
                } catch (NumberFormatException e) {
                    return 0L;
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting created_at: " + e.getMessage());
        }
        return 0L;
    }

    private void updateAllListings(List<DocumentSnapshot> documents) {
        Log.d(TAG, "=== UPDATE ALL LISTINGS CALLED ===");
        Log.d(TAG, "Number of all listings: " + documents.size());

        // Clear all existing views in the container
        llListingsContainer.removeAllViews();

        if (documents.isEmpty()) {
            Log.d(TAG, "No listings found");
            llListingsContainer.setVisibility(View.GONE);
            return;
        }

        // Show the container since we have listings
        llListingsContainer.setVisibility(View.VISIBLE);

        // For each listing, create and add a CardView
        for (DocumentSnapshot document : documents) {
            try {
                // Get the listing data
                String foodName = document.getString("food_name");
                String quantity = getQuantityAsString(document);
                String category = document.getString("category");
                String expiryDate = document.getString("expiry_date");
                String startTime = document.getString("start_time");
                String endTime = document.getString("end_time");
                String status = document.getString("status");
                String documentId = document.getId();
                String imageBase64 = document.getString("image_base64");

                Log.d(TAG, "Creating card for: " + foodName + " (ID: " + documentId + ", Status: " + status + ")");

                if (foodName == null || foodName.isEmpty()) {
                    Log.w(TAG, "Skipping listing with no food name");
                    continue;
                }

                // Create a CardView for this listing
                CardView listingCard = createListingCard(
                        foodName, quantity, category, expiryDate,
                        startTime, endTime, status, documentId, imageBase64
                );

                // Add the CardView to the container
                if (listingCard != null) {
                    llListingsContainer.addView(listingCard);
                    Log.d(TAG, "Card added for: " + foodName + " (Status: " + status + ")");
                }

            } catch (Exception e) {
                Log.e(TAG, "Error creating listing card: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private String getQuantityAsString(DocumentSnapshot document) {
        try {
            Object quantityObj = document.get("quantity");
            if (quantityObj == null) return "N/A";

            if (quantityObj instanceof Long) {
                return String.valueOf((Long) quantityObj);
            } else if (quantityObj instanceof Double) {
                return String.valueOf(((Double) quantityObj).intValue());
            } else if (quantityObj instanceof Integer) {
                return String.valueOf((Integer) quantityObj);
            } else if (quantityObj instanceof String) {
                return (String) quantityObj;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting quantity: " + e.getMessage());
        }
        return "N/A";
    }

    private CardView createListingCard(String foodName, String quantity, String category,
                                       String expiryDate, String startTime, String endTime,
                                       String status, String documentId, String imageBase64) {
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

            // Set data from listing
            tvFoodName.setText(foodName != null ? foodName : "Food Item");

            // Format status with proper casing
            if (status != null && !status.isEmpty()) {
                String formattedStatus = status.substring(0, 1).toUpperCase() +
                        status.substring(1).toLowerCase();
                tvStatus.setText(formattedStatus);
            } else {
                tvStatus.setText("Available");
            }

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

            // Load image if available
            loadFoodImage(imageBase64, ivFoodImage, tvNoImage, foodName);

            // Set status color and button text based on status
            String currentStatus = status != null ? status.toLowerCase() : "available";
            switch (currentStatus) {
                case "available":
                    tvStatus.setTextColor(ContextCompat.getColor(this, R.color.green_500));
                    tvStatus.setBackgroundResource(R.drawable.status_available_bg);
                    btnAction.setText("Delete");
                    btnAction.setBackgroundTintList(ColorStateList.valueOf(
                            ContextCompat.getColor(this, R.color.green_500)));
                    btnAction.setOnClickListener(v -> showDeleteConfirmation(documentId));
                    break;
                case "reserved":
                    tvStatus.setTextColor(ContextCompat.getColor(this, R.color.orange_500));
                    tvStatus.setBackgroundResource(R.drawable.status_reserved_bg);
                    btnAction.setText("Delete");
                    btnAction.setBackgroundTintList(ColorStateList.valueOf(
                            ContextCompat.getColor(this, R.color.green_500)));
                    btnAction.setOnClickListener(v -> showDeleteConfirmation(documentId));
                    break;
                case "completed":
                    tvStatus.setTextColor(ContextCompat.getColor(this, R.color.blue_500));
                    tvStatus.setBackgroundResource(R.drawable.status_complete_bg);
//                    btnAction.setText("Completed");
//                    btnAction.setBackgroundTintList(ColorStateList.valueOf(
//                            ContextCompat.getColor(this, R.color.blue_500)));
                    btnAction.setEnabled(false);
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

            // Set delete button (or action button) click listener
            btnAction.setOnClickListener(view -> {
                if (currentStatus.equals("available") || currentStatus.equals("reserved")) {
                    // For available/reserved listings, the button changes status
                    // This is already handled in the switch statement above
                } else {
                    // For other statuses or additional actions
                    showDeleteConfirmation(documentId);
                }
            });

            // Set click listener for the whole card
            cardView.setOnClickListener(v -> viewListingDetails(documentId));

            return cardView;

        } catch (Exception e) {
            Log.e(TAG, "Error inflating listing card: " + e.getMessage());
            return null;
        }
    }

    private void loadFoodImage(String imageBase64, ImageView imageView, TextView tvNoImage, String foodName) {
        if (imageBase64 != null && !imageBase64.trim().isEmpty() && !imageBase64.equals("null")) {
            try {
                Log.d(TAG, "Loading image for: " + foodName);

                // Clean the Base64 string
                String cleanBase64 = imageBase64.trim();
                if (cleanBase64.contains("base64,")) {
                    cleanBase64 = cleanBase64.substring(cleanBase64.indexOf("base64,") + 7);
                }

                // Decode Base64
                byte[] decodedBytes = Base64.decode(cleanBase64, Base64.DEFAULT);

                if (decodedBytes != null && decodedBytes.length > 0) {
                    Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);

                    if (bitmap != null) {
                        imageView.setImageBitmap(bitmap);
                        tvNoImage.setVisibility(View.GONE);
                        Log.d(TAG, "✓ Image loaded for: " + foodName);
                        return;
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "✗ Error loading image for " + foodName + ": " + e.getMessage());
            }
        }

        // If image loading failed, show default
        imageView.setImageResource(R.drawable.ic_food);
        tvNoImage.setVisibility(View.VISIBLE);
        Log.d(TAG, "Showing default image for: " + foodName);
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
                    Toast.makeText(this, "Status updated to " + newStatus, LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to update status: " + e.getMessage(),
                            LENGTH_SHORT).show();
                    Log.e(TAG, "Error updating status: " + e.getMessage());
                });
    }

    private void editListing(String documentId) {
        Log.d(TAG, "Opening edit page for listing: " + documentId);

        try {
            Intent intent = new Intent(this, EditFoodListingActivity.class);
            intent.putExtra("listing_id", documentId);

            // Use the launcher instead of deprecated startActivityForResult
            editListingLauncher.launch(intent);

            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);

        } catch (Exception e) {
            Log.e(TAG, "Error opening edit activity: " + e.getMessage());
            Toast.makeText(this, "Error opening edit page", Toast.LENGTH_SHORT).show();
        }
    }

    private void showDeleteConfirmation(String documentId) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Listing")
                .setMessage("Are you sure you want to delete this listing? This action cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> deleteListing(documentId))
                .setNegativeButton("Cancel", null)
                .setCancelable(true)
                .show();
    }

    private void deleteListing(String listingId) {
        db.collection("food_listings").document(listingId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Listing deleted successfully", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error deleting listing: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error deleting listing: " + e.getMessage());
                });
    }

    private void viewListingDetails(String documentId) {
        Toast.makeText(this, "View details: " + documentId, LENGTH_SHORT).show();
        // TODO: Implement details view
    }

    private void updateEmptyState(int totalCount) {
        if (totalCount == 0) {
            cardEmptyState.setVisibility(View.VISIBLE);
        } else {
            cardEmptyState.setVisibility(View.GONE);
        }
    }

    private void setupClickListeners() {
        // Add New Listing Button
        findViewById(R.id.btnAddListing).setOnClickListener(v -> {
            navigateToAddListing();
        });

        // View All Listings
        // findViewById(R.id.tvViewAll).setOnClickListener(v -> {
        //     navigateToAllListings();
        // });

        // Notification icon
        // findViewById(R.id.ivNotification).setOnClickListener(v -> {
        //     navigateToNotifications();
        // });
    }

    private void setupBackPressHandler() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                moveTaskToBack(true);
            }
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
        // Don't finish - user might want to come back
    }

    private void navigateToAllListings() {
        Toast.makeText(this, "Navigate to All Listings", LENGTH_SHORT).show();
    }

    private void navigateToNotifications() {
        Toast.makeText(this, "Navigate to Notifications", LENGTH_SHORT).show();
        Intent intent = new Intent(this, NotificationsActivity.class);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        startActivity(intent);
        finish();
    }

    private void navigateToProfile() {
        Intent intent = new Intent(this, ProfileActivity.class);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        startActivity(intent);
        finish();
    }

    private void navigateToLogin() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart() called");

        // Reconnect listeners when activity becomes visible
        if (currentUserId != null && mAuth.getCurrentUser() != null) {
            Log.d(TAG, "User authenticated, setting up listeners");
            removeListeners();
            setupRealTimeListeners();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop() called");
        removeListeners();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        removeListeners();
    }

    private void removeListeners() {
        Log.d(TAG, "Removing listeners");

        if (allListingsListener != null) {
            allListingsListener.remove();
            allListingsListener = null;
        }
        if (totalQuantityListener != null) {
            totalQuantityListener.remove();
            totalQuantityListener = null;
        }

        Log.d(TAG, "All listeners removed");
    }

    private void setupActivityResultLauncher() {
        editListingLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == RESULT_OK) {
                            Intent data = result.getData();
                            if (data != null) {
                                boolean listingUpdated = data.getBooleanExtra("listing_updated", false);
                                boolean listingDeleted = data.getBooleanExtra("listing_deleted", false);

                                if (listingDeleted) {
                                    Toast.makeText(SellerDashboardActivity.this,
                                            "Listing deleted successfully", Toast.LENGTH_SHORT).show();
                                } else if (listingUpdated) {
                                    Toast.makeText(SellerDashboardActivity.this,
                                            "Listing updated successfully", Toast.LENGTH_SHORT).show();
                                }

                                // Refresh the listings
                                removeListeners();
                                setupRealTimeListeners();
                            }
                        }
                    }
                }
        );
    }
}