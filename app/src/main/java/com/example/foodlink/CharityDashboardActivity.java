package com.example.foodlink;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FieldValue;
import java.util.HashMap;
import java.util.Map;

public class CharityDashboardActivity extends AppCompatActivity {

    // UI Components
    private ImageView ivNotification;
//    private ImageView ivProfile;
    private TextView tvCharityName;
    private MaterialButton btnBrowseFood;
    private MaterialButton btnMyReservations;
    private TextInputEditText etSearch;
    private TextInputLayout filterDropdownLayout;
    private AutoCompleteTextView categoryAutoComplete;
    private TextView tvListingCount;
    private LinearLayout llListingsContainer;
    private View cardEmptyState;

    // Data
    private List<FoodListing> foodListings = new ArrayList<>();
    private List<FoodListing> filteredListings = new ArrayList<>();
    private List<String> categories = Arrays.asList(
            "All",
            "Vegetables",
            "Fruits",
            "Bakery",
            "Prepared Food",
            "Dairy",
            "Other"
    );

    // Firebase
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private ListenerRegistration listingsListener;
    private String currentUserId;

    private String charityName;

    private BottomNavigationView bottomNavigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_charity_dashboard);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            navigateToLogin();
            return;
        }
        currentUserId = currentUser.getUid();

        // Initialize UI Components
        initViews();

        // Setup exposed dropdown menu
        setupExposedDropdownMenu();

        // Setup listeners
        setupListeners();

        setupBottomNavigation();
        setupBackPressHandler();
        setupToolbar();

        // Load user data
        loadUserData();

        // Load food listings from Firebase
        setupFoodListingsListener();
    }

    private void initViews() {
        // Toolbar components
        ivNotification = findViewById(R.id.ivNotification);
//        ivProfile = findViewById(R.id.ivProfile);

        // Welcome section
        tvCharityName = findViewById(R.id.tvCharityName);

        // Action buttons
        btnBrowseFood = findViewById(R.id.btnBrowseFood);
        btnMyReservations = findViewById(R.id.btnMyReservations);

        // Search and filter
        etSearch = findViewById(R.id.etSearch);
        filterDropdownLayout = findViewById(R.id.filterDropdownLayout);
        categoryAutoComplete = findViewById(R.id.categoryAutoComplete);

        // Listings
        tvListingCount = findViewById(R.id.tvListingCount);
        llListingsContainer = findViewById(R.id.llListingsContainer);
        cardEmptyState = findViewById(R.id.cardEmptyState);

        bottomNavigation = findViewById(R.id.bottomNavigation);
    }

    private void loadUserData() {
        db.collection("users").document(currentUserId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        charityName = documentSnapshot.getString("charity_name");
                        if (charityName == null || charityName.isEmpty()) {
                            charityName = documentSnapshot.getString("organization_name");
                        }
                        if (charityName == null || charityName.isEmpty()) {
                            charityName = documentSnapshot.getString("full_name");
                        }
                        if (charityName == null || charityName.isEmpty()) {
                            charityName = documentSnapshot.getString("name");
                        }
                        if (charityName == null || charityName.isEmpty()) {
                            charityName = "Hope Community Kitchen";
                        }
                        tvCharityName.setText(charityName); // Set UI text
                    }
                })
                .addOnFailureListener(e -> {
                    tvCharityName.setText("Hope Community Kitchen");
                });
    }

    private void setupFoodListingsListener() {
        // CORRECTED: Use just "available" string, not Arrays.asList()
        Query query = db.collection("food_listings")
                .whereEqualTo("status", "available")  // Fixed this line
                .whereGreaterThanOrEqualTo("expiry_date", getCurrentDate())
                .orderBy("expiry_date");

        listingsListener = query.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot querySnapshot,
                                @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    Toast.makeText(CharityDashboardActivity.this,
                            "Error loading listings: " + error.getMessage(),
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                if (querySnapshot != null) {
                    foodListings.clear();

                    for (com.google.firebase.firestore.DocumentSnapshot document : querySnapshot.getDocuments()) {
                        try {
                            String foodName = document.getString("food_name");
                            String category = document.getString("category");
                            Object quantityObj = document.get("quantity");
                            String sellerName = document.getString("seller_name");
                            String startTime = document.getString("start_time");
                            String endTime = document.getString("end_time");
                            String expiryDate = document.getString("expiry_date");
                            String status = document.getString("status");
                            String sellerId = document.getString("seller_id");
                            String documentId = document.getId();
                            String imageBase64 = document.getString("image_base64"); // Get Base64 image

                            // Get quantity as string
                            String quantity = "N/A";
                            if (quantityObj != null) {
                                if (quantityObj instanceof Long) {
                                    quantity = ((Long) quantityObj).intValue() + " kg";
                                } else if (quantityObj instanceof Double) {
                                    quantity = ((Double) quantityObj).intValue() + " kg";
                                } else if (quantityObj instanceof Integer) {
                                    quantity = (Integer) quantityObj + " kg";
                                } else if (quantityObj instanceof String) {
                                    quantity = (String) quantityObj;
                                }
                            }

                            // Get restaurant name from seller data or use seller name
                            String restaurant = sellerName != null ? sellerName : "Restaurant";

                            // Get pickup time
                            String pickupTime = "N/A";
                            if (startTime != null && endTime != null) {
                                pickupTime = formatTime(startTime) + "-" + formatTime(endTime);
                            }

                            // Map category to icon
                            int iconResource = getIconForCategory(category);

                            FoodListing listing = new FoodListing(
                                    foodName,
                                    category,
                                    quantity,
                                    restaurant,
                                    pickupTime,
                                    expiryDate,
                                    iconResource,
                                    status,
                                    sellerId,
                                    documentId,
                                    imageBase64  // Store Base64 image
                            );

                            foodListings.add(listing);

                        } catch (Exception e) {
                            // Skip problematic listings
                        }
                    }

                    // Initially show all listings
                    filteredListings.clear();
                    filteredListings.addAll(foodListings);

                    // Update UI
                    updateUI();
                }
            }
        });
    }

    private String getCurrentDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return sdf.format(new Date());
    }

    private String formatTime(String time) {
        try {
            // Assuming time is in HH:mm format or similar
            if (time.contains(":")) {
                String[] parts = time.split(":");
                if (parts.length >= 2) {
                    int hour = Integer.parseInt(parts[0]);
                    String minute = parts[1];

                    // Convert to 12-hour format
                    String amPm = hour >= 12 ? "PM" : "AM";
                    hour = hour % 12;
                    hour = hour == 0 ? 12 : hour;

                    return String.format(Locale.getDefault(), "%d:%s %s", hour, minute, amPm);
                }
            }
            return time;
        } catch (Exception e) {
            return time;
        }
    }

    private int getIconForCategory(String category) {
        if (category == null) return R.drawable.ic_food;

        switch (category.toLowerCase()) {
            case "vegetables":
                return R.drawable.ic_vegetables;
            case "fruits":
                return R.drawable.ic_fruits;
            case "bakery":
                return R.drawable.ic_bakery;
            case "prepared food":
                return R.drawable.ic_meal;
            case "dairy":
                return R.drawable.ic_dairy;
            default:
                return R.drawable.ic_food;
        }
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
    }

    private void setupListeners() {
        // Notification icon
        ivNotification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToNotification();
            }
        });

        // Profile icon
//        ivProfile.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                navigateToProfile();
//            }
//        });

        // Browse Food button
        btnBrowseFood.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Already on browse food page
            }
        });

        // My Reservations button
        btnMyReservations.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToReservation();
            }
        });

        // Search functionality
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterListings();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void navigateToReservation() {
        Intent intent = new Intent(this, ReservationsActivity.class);
        startActivity(intent);
    }

    private void navigateToNotification() {
        Intent intent = new Intent(this, NotificationsActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    private void navigateToProfile() {
        Intent intent = new Intent(this, ProfileActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    private void navigateToLogin() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
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
        // Clear existing menu items
        bottomNavigation.getMenu().clear();

        // Add only the items you want for charity
        bottomNavigation.getMenu().add(Menu.NONE, R.id.nav_dashboard, 1, "BrowseFood")
                .setIcon(R.drawable.ic_dashboard);

        bottomNavigation.getMenu().add(Menu.NONE, R.id.nav_reservations, 2, "Reservation")
                .setIcon(R.drawable.ic_reservation);

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
                else if (itemId == R.id.nav_reservations) {
                    navigateToReservation();
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

    private void setupExposedDropdownMenu() {
        // Create adapter for dropdown
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                R.layout.dropdown_menu_item,
                categories
        );

        categoryAutoComplete.setAdapter(adapter);

        // Handle item selection
        categoryAutoComplete.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                filterListings();
            }
        });

        // Set initial selection
        categoryAutoComplete.setText("All", false);
    }

    private void filterListings() {
        filteredListings.clear();

        String searchQuery = etSearch.getText().toString().toLowerCase().trim();
        String selectedCategory = categoryAutoComplete.getText().toString();

        for (FoodListing listing : foodListings) {
            // Check if listing matches search query
            boolean matchesSearch = searchQuery.isEmpty() ||
                    listing.getName().toLowerCase().contains(searchQuery) ||
                    listing.getRestaurant().toLowerCase().contains(searchQuery);

            // Check if listing matches selected category
            boolean matchesCategory = selectedCategory.equals("All") ||
                    selectedCategory.equals(listing.getCategory());

            // Check if listing is available
            boolean isAvailable = "available".equals(listing.getStatus());

            if (matchesSearch && matchesCategory && isAvailable) {
                filteredListings.add(listing);
            }
        }

        updateUI();
    }

    private void updateUI() {
        // Clear existing views
        llListingsContainer.removeAllViews();

        // Show/hide empty state
        if (filteredListings.isEmpty()) {
            cardEmptyState.setVisibility(View.VISIBLE);
            llListingsContainer.setVisibility(View.GONE);
        } else {
            cardEmptyState.setVisibility(View.GONE);
            llListingsContainer.setVisibility(View.VISIBLE);

            // Add listing cards dynamically
            for (int i = 0; i < filteredListings.size(); i++) {
                FoodListing listing = filteredListings.get(i);
                CardView listingCard = createListingCardView(listing, i);
                if (listingCard != null) {
                    llListingsContainer.addView(listingCard);

                    // Add margin between cards
                    LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) listingCard.getLayoutParams();
                    params.bottomMargin = 16;
                    listingCard.setLayoutParams(params);
                }
            }
        }

        updateListingsCount();
    }

    private CardView createListingCardView(FoodListing listing, int position) {
        try {
            LayoutInflater inflater = LayoutInflater.from(this);
            CardView cardView = (CardView) inflater.inflate(
                    R.layout.charity_food_listing_item, llListingsContainer, false);

            // Get references to views
            ImageView ivFoodImage = cardView.findViewById(R.id.ivFoodImage);
            ImageView ivFoodIcon = cardView.findViewById(R.id.ivFoodIcon);
            TextView tvNoImage = cardView.findViewById(R.id.tvNoImage);
            TextView tvFoodName = cardView.findViewById(R.id.tvFoodName);
            TextView tvCategoryQuantity = cardView.findViewById(R.id.tvCategoryQuantity);
            TextView tvRestaurant = cardView.findViewById(R.id.tvRestaurant);
            TextView tvPickupTime = cardView.findViewById(R.id.tvPickupTime);
            TextView tvExpiryDate = cardView.findViewById(R.id.tvExpiryDate);
            MaterialButton btnViewDetails = cardView.findViewById(R.id.btnViewDetails); // NEW
            MaterialButton btnReserve = cardView.findViewById(R.id.btnReserve);

            // Set text data
            tvFoodName.setText(listing.getName());
            tvCategoryQuantity.setText(listing.getCategory() + " • " + listing.getQuantity());
            tvRestaurant.setText(listing.getRestaurant());
            tvPickupTime.setText("Pickup: " + listing.getPickupTime());
            tvExpiryDate.setText("Expires: " + listing.getExpiryDate());

            // Load food image from Base64
            if (ivFoodImage != null && ivFoodIcon != null) {
                loadFoodImage(listing.getImageBase64(), ivFoodImage, ivFoodIcon,
                        tvNoImage, listing.getName(), listing.getCategory());
            }

            // Set view details button click listener
            btnViewDetails.setTag(position);
            btnViewDetails.setOnClickListener(v -> {
                int pos = (int) v.getTag();
                viewFoodListingDetails(pos);
            });

            // Set reserve button click listener
            btnReserve.setTag(position);
            btnReserve.setOnClickListener(v -> {
                int pos = (int) v.getTag();
                reserveFoodListing(pos);
            });

            return cardView;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // Add this new method
    private void viewFoodListingDetails(int position) {
        if (position < filteredListings.size()) {
            FoodListing listing = filteredListings.get(position);

            // TEMPORARY: Remove Base64 from the listing to make sure
            // This is a test to confirm it's the Base64 causing the issue
            FoodListing listingWithoutImage = new FoodListing(
                    listing.getName(),
                    listing.getCategory(),
                    listing.getQuantity(),
                    listing.getRestaurant(),
                    listing.getPickupTime(),
                    listing.getExpiryDate(),
                    listing.getIconResource(),
                    listing.getStatus(),
                    listing.getSellerId(),
                    listing.getDocumentId()
                    // Don't pass imageBase64 in constructor
            );

            Intent intent = new Intent(this, FoodDetailsActivity.class);
            // PASS ONLY THE ID
            intent.putExtra("food_id", listingWithoutImage.getDocumentId());
            startActivity(intent);
        }
    }

    private void loadFoodImage(String imageBase64, ImageView imageView, ImageView iconView,
                               TextView tvNoImage, String foodName, String category) {
        // Check if views are null
        if (imageView == null || iconView == null) {
            Log.e("CharityDashboard", "Image views are null for: " + foodName);
            return;
        }

        if (imageBase64 != null && !imageBase64.trim().isEmpty() && !imageBase64.equals("null")) {
            try {
                Log.d("CharityDashboard", "Loading image for: " + foodName);

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
                        // Show the loaded image, hide the fallback icon
                        imageView.setImageBitmap(bitmap);
                        imageView.setVisibility(View.VISIBLE);
                        iconView.setVisibility(View.GONE);
                        if (tvNoImage != null) {
                            tvNoImage.setVisibility(View.GONE);
                        }
                        Log.d("CharityDashboard", "✓ Image loaded for: " + foodName);
                        return;
                    }
                }
            } catch (Exception e) {
                Log.e("CharityDashboard", "✗ Error loading image for " + foodName + ": " + e.getMessage());
            }
        }

        // If image loading failed or no image, show fallback icon
        imageView.setVisibility(View.GONE);
        iconView.setVisibility(View.VISIBLE);

        // Set category-based icon
        int iconResource = getIconForCategory(category);
        iconView.setImageResource(iconResource);

        if (tvNoImage != null) {
            tvNoImage.setVisibility(View.VISIBLE);
        }
        Log.d("CharityDashboard", "Showing fallback icon for: " + foodName);
    }

    private void updateListingsCount() {
        int count = filteredListings.size();
        String countText = count + " listing" + (count != 1 ? "s" : "");
        tvListingCount.setText(countText);
    }

    private void reserveFoodListing(int position) {
        if (position < filteredListings.size()) {
            FoodListing listing = filteredListings.get(position);
            String currentUserUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

            // Create a Map with the fields to update
            Map<String, Object> updateData = new HashMap<>();
            updateData.put("status", "reserved");
            updateData.put("reserved_by", currentUserUid);
            updateData.put("reserved_charity", charityName);
            updateData.put("updated_at", FieldValue.serverTimestamp()); // Optional but good practice

            // Update the document in Firestore
            db.collection("food_listings").document(listing.getDocumentId())
                    .update(updateData)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            // Navigate to reservations page after successful update
                            Intent intent = new Intent(CharityDashboardActivity.this, ReservationsActivity.class);
                            intent.putExtra("listing_id", listing.getDocumentId());
                            intent.putExtra("food_name", listing.getName());
                            intent.putExtra("restaurant", listing.getRestaurant());
                            intent.putExtra("quantity", listing.getQuantity());
                            intent.putExtra("pickup_time", listing.getPickupTime());
                            intent.putExtra("expiry_date", listing.getExpiryDate());
                            intent.putExtra("seller_id", listing.getSellerId());
                            startActivity(intent);

                            Toast.makeText(CharityDashboardActivity.this,
                                    "Successfully reserved: " + listing.getName(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(CharityDashboardActivity.this,
                                    "Failed to reserve: " + e.getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Reconnect listeners
        if (currentUserId != null && mAuth.getCurrentUser() != null) {
            setupFoodListingsListener();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Remove listeners to prevent memory leaks
        if (listingsListener != null) {
            listingsListener.remove();
            listingsListener = null;
        }
    }

    // Updated FoodListing model class with image support
    public static class FoodListing {
        private String name;
        private String category;
        private String quantity;
        private String restaurant;
        private String pickupTime;
        private String expiryDate;
        private int iconResource;
        private String status;
        private String sellerId;
        private String documentId;
        private String imageBase64;

        public FoodListing(String name, String category, String quantity,
                           String restaurant, String pickupTime, String expiryDate,
                           int iconResource, String status, String sellerId, String documentId) {
            this(name, category, quantity, restaurant, pickupTime, expiryDate,
                    iconResource, status, sellerId, documentId, null);
        }

        public FoodListing(String name, String category, String quantity,
                           String restaurant, String pickupTime, String expiryDate,
                           int iconResource, String status, String sellerId,
                           String documentId, String imageBase64) {
            this.name = name;
            this.category = category;
            this.quantity = quantity;
            this.restaurant = restaurant;
            this.pickupTime = pickupTime;
            this.expiryDate = expiryDate;
            this.iconResource = iconResource;
            this.status = status;
            this.sellerId = sellerId;
            this.documentId = documentId;
            this.imageBase64 = imageBase64;
        }

        // Getters
        public String getName() { return name; }
        public String getCategory() { return category; }
        public String getQuantity() { return quantity; }
        public String getRestaurant() { return restaurant; }
        public String getPickupTime() { return pickupTime; }
        public String getExpiryDate() { return expiryDate; }
        public int getIconResource() { return iconResource; }
        public String getStatus() { return status; }
        public String getSellerId() { return sellerId; }
        public String getDocumentId() { return documentId; }
        public String getImageBase64() { return imageBase64; }
    }
}