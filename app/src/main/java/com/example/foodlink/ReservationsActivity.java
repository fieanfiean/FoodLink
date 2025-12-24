package com.example.foodlink;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
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

public class ReservationsActivity extends AppCompatActivity {

    // UI Components
//    private ImageView ivBack;
    private ImageView ivNotification;
    private TextInputEditText etSearch;
    private TextInputLayout statusFilterLayout;
    private AutoCompleteTextView statusAutoComplete;
    private TextView tvReservationsCount;
    private RecyclerView rvReservations;
    private View llEmptyState;

    // Adapter
    private ReservationsAdapter reservationsAdapter;

    // Data
    private List<Reservation> reservations = new ArrayList<>();
    private List<Reservation> filteredReservations = new ArrayList<>();
    private List<String> statusFilters = Arrays.asList(
            "All",
            "Upcoming",
            "Pending",
            "Completed",
            "Cancelled",
            "Expired"
    );

    // Firebase
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private ListenerRegistration reservationsListener;
    private String currentUserId;
    private String charityName;

    private BottomNavigationView bottomNavigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reservations);

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

        // Setup toolbar
        setupToolbar();

        // Setup status filter dropdown
        setupStatusFilter();

        // Load charity name and setup Firestore listener
        loadCharityNameAndSetupListener();

        // Setup listeners
        setupListeners();

        // Setup recycler view
        setupRecyclerView();

        setupBackPressHandler();
        setupBottomNavigation();
    }

    private void initViews() {
        // Toolbar components
//        ivBack = findViewById(R.id.ivBack);
        ivNotification = findViewById(R.id.ivNotification);

        // Search and filter
        etSearch = findViewById(R.id.etSearch);
        statusFilterLayout = findViewById(R.id.statusFilterLayout);
        statusAutoComplete = findViewById(R.id.statusAutoComplete);

        // Count
        tvReservationsCount = findViewById(R.id.tvReservationsCount);

        // List
        rvReservations = findViewById(R.id.rvReservations);
        llEmptyState = findViewById(R.id.llEmptyState);

        bottomNavigation = findViewById(R.id.bottomNavigation);
    }

    private void loadCharityNameAndSetupListener() {
        // Load charity name from Firestore
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
                            charityName = "Charity Organization";
                        }

                        // Setup Firestore listener after getting charity name
                        setupReservationsListener();
                    } else {
                        charityName = "Charity Organization";
                        setupReservationsListener();
                    }
                })
                .addOnFailureListener(e -> {
                    charityName = "Charity Organization";
                    setupReservationsListener();
                });
    }

    private void setupReservationsListener() {
        // Query to fetch food listings reserved by this charity
        Query query = db.collection("food_listings")
                .whereEqualTo("reserved_by", currentUserId);

        reservationsListener = query.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot querySnapshot,
                                @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    Toast.makeText(ReservationsActivity.this,
                            "Error loading reservations: " + error.getMessage(),
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                if (querySnapshot != null && !querySnapshot.isEmpty()) {
                    reservations.clear();

                    for (com.google.firebase.firestore.DocumentSnapshot document : querySnapshot.getDocuments()) {
                        try {
                            String documentId = document.getId();
                            String foodName = document.getString("food_name");
                            String category = document.getString("category");
                            Object quantityObj = document.get("quantity");
                            String sellerName = document.getString("seller_name");
                            String startTime = document.getString("start_time");
                            String endTime = document.getString("end_time");
                            String expiryDate = document.getString("expiry_date");
                            String status = document.getString("status");
                            String sellerId = document.getString("seller_id");
                            String reservedBy = document.getString("reserved_by");
                            String reservedCharity = document.getString("reserved_charity");
                            String imageBase64 = document.getString("image_base64");

                            // Get pickup address
                            String pickupAddress = document.getString("pickup_address");
                            if (pickupAddress == null) {
                                pickupAddress = document.getString("address");
                            }

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

                            // Get restaurant name
                            String restaurant = sellerName != null ? sellerName : "Restaurant";

                            // Get pickup time
                            String pickupTime = "N/A";
                            if (startTime != null && endTime != null) {
                                pickupTime = formatTime(startTime) + "-" + formatTime(endTime);
                            }

                            // Determine status based on current status and expiry date
                            String reservationStatus = determineReservationStatus(expiryDate, status);

                            // Create reservation object
                            Reservation reservation = new Reservation(
                                    documentId,
                                    foodName,
                                    restaurant,
                                    quantity,
                                    pickupTime,
                                    expiryDate,
                                    reservationStatus,
                                    category,
                                    sellerId,
                                    reservedBy,
                                    reservedCharity,
                                    pickupAddress,
                                    imageBase64
                            );

                            reservations.add(reservation);

                        } catch (Exception e) {
                            // Skip problematic reservations
                        }
                    }

                    // Initially show all reservations
                    filteredReservations.clear();
                    filteredReservations.addAll(reservations);

                    // Update UI
                    updateUI();
                } else {
                    // No reservations found
                    reservations.clear();
                    filteredReservations.clear();
                    updateUI();
                }
            }
        });
    }

    private String determineReservationStatus(String expiryDate, String originalStatus) {
        try {
            // If status is already completed or cancelled, keep it
            if ("completed".equals(originalStatus) || "cancelled".equals(originalStatus)) {
                return originalStatus;
            }

            // Check if expired
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date expiry = sdf.parse(expiryDate);
            Date today = new Date();

            // Remove time part for comparison
            today = sdf.parse(sdf.format(today));

            if (expiry.before(today)) {
                return "Expired";
            }

            // If not expired, return the original status or default to "Upcoming"
            if (originalStatus != null && !originalStatus.isEmpty()) {
                return originalStatus;
            }
            return "Upcoming";

        } catch (Exception e) {
            return "Upcoming";
        }
    }

    private String formatTime(String time) {
        try {
            if (time == null || time.isEmpty()) {
                return "N/A";
            }

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

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
    }

    private void setupStatusFilter() {
        // Create adapter for dropdown
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                R.layout.dropdown_menu_item,
                statusFilters
        );

        statusAutoComplete.setAdapter(adapter);

        // Handle item selection
        statusAutoComplete.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                filterReservations();
            }
        });

        // Set initial selection
        statusAutoComplete.setText("All", false);
    }

    private void setupRecyclerView() {
        reservationsAdapter = new ReservationsAdapter(filteredReservations, new ReservationsAdapter.OnReservationClickListener() {
            @Override
            public void onConfirmPickupClick(Reservation reservation) {
                confirmPickup(reservation);
            }

            @Override
            public void onCancelReservationClick(Reservation reservation) {
                cancelReservation(reservation);
            }

            @Override
            public void onViewDetailsClick(Reservation reservation) {
                viewReservationDetails(reservation);
            }

            @Override
            public void onReorderClick(Reservation reservation) {
                reorderReservation(reservation);
            }
        });

        rvReservations.setLayoutManager(new LinearLayoutManager(this));
        rvReservations.setAdapter(reservationsAdapter);
    }

    private void setupListeners() {
        // Back button
//        ivBack.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                onBackPressed();
//            }
//        });

        // Notification icon
        ivNotification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToNotification();
            }
        });

        // Search functionality
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterReservations();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Browse Food button in empty state
        if (llEmptyState != null) {
            View browseFoodButton = llEmptyState.findViewById(R.id.btnBrowseFood);
            if (browseFoodButton != null) {
                browseFoodButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        navigateToDashboard();
                    }
                });
            }
        }
    }

    private void navigateToNotification() {
        Intent intent = new Intent(this, NotificationsActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    private void navigateToLogin() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
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
                    navigateToDashboard();
                    return true;
                } else if (itemId == R.id.nav_reservations) {
                    // Already on reservations page
                    return true;
                } else if (itemId == R.id.nav_profile) {
                    navigateToProfile();
                    return true;
                }
                return false;
            }
        });

        // Set reservations as selected
        bottomNavigation.setSelectedItemId(R.id.nav_reservations);
    }

    private void navigateToProfile() {
        Intent intent = new Intent(this, ProfileActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        finish();
    }

    private void navigateToDashboard() {
        Intent intent = new Intent(this, CharityDashboardActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        finish();
    }

    private void setupBackPressHandler() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                navigateToDashboard();
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                finish();
            }
        });
    }

    private void filterReservations() {
        filteredReservations.clear();

        String searchQuery = etSearch.getText().toString().toLowerCase().trim();
        String selectedStatus = statusAutoComplete.getText().toString();

        for (Reservation reservation : reservations) {
            // Check if reservation matches search query
            boolean matchesSearch = searchQuery.isEmpty() ||
                    reservation.getFoodItem().toLowerCase().contains(searchQuery) ||
                    reservation.getRestaurant().toLowerCase().contains(searchQuery);

            // Check if reservation matches selected status
            boolean matchesStatus = selectedStatus.equals("All") ||
                    selectedStatus.equalsIgnoreCase(reservation.getStatus());

            if (matchesSearch && matchesStatus) {
                filteredReservations.add(reservation);
            }
        }

        updateUI();
    }

    private void updateUI() {
        if (filteredReservations.isEmpty()) {
            rvReservations.setVisibility(View.GONE);
            llEmptyState.setVisibility(View.VISIBLE);
        } else {
            rvReservations.setVisibility(View.VISIBLE);
            llEmptyState.setVisibility(View.GONE);

            // Update adapter
            if (reservationsAdapter != null) {
                reservationsAdapter.updateReservations(filteredReservations);
            }
        }

        updateReservationsCount();
    }

    private void updateReservationsCount() {
        int count = filteredReservations.size();
        String countText = count + " reservation" + (count != 1 ? "s" : "");
        tvReservationsCount.setText(countText);
    }

    private void confirmPickup(Reservation reservation) {
        // Update status to "completed" in Firestore
        db.collection("food_listings").document(reservation.getId())
                .update("status", "completed")
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this,
                            "Pickup confirmed for: " + reservation.getFoodItem(),
                            Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this,
                            "Failed to confirm pickup: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void cancelReservation(Reservation reservation) {
        // Update status back to "available" in Firestore
        db.collection("food_listings").document(reservation.getId())
                .update("status", "available", "reserved_by", null, "reserved_charity", null)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this,
                            "Reservation cancelled: " + reservation.getFoodItem(),
                            Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this,
                            "Failed to cancel reservation: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void viewReservationDetails(Reservation reservation) {
        Intent intent = new Intent(this, ReservationsAdapter.class);
        intent.putExtra("reservation_id", reservation.getId());
        intent.putExtra("food_name", reservation.getFoodItem());
        intent.putExtra("restaurant", reservation.getRestaurant());
        intent.putExtra("quantity", reservation.getQuantity());
        intent.putExtra("pickup_time", reservation.getPickupTime());
        intent.putExtra("expiry_date", reservation.getExpiryDate());
        intent.putExtra("status", reservation.getStatus());
        intent.putExtra("category", reservation.getCategory());
        intent.putExtra("pickup_address", reservation.getPickupAddress());
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    private void reorderReservation(Reservation reservation) {
        Toast.makeText(this,
                "Reordering: " + reservation.getFoodItem(),
                Toast.LENGTH_SHORT).show();
        // TODO: Implement reorder logic
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Reconnect listener
        if (currentUserId != null && mAuth.getCurrentUser() != null) {
            if (reservationsListener == null) {
                setupReservationsListener();
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Remove listener to prevent memory leaks
        if (reservationsListener != null) {
            reservationsListener.remove();
            reservationsListener = null;
        }
    }

    // Enhanced Reservation model class
    public static class Reservation {
        private String id;
        private String foodItem;
        private String restaurant;
        private String quantity;
        private String pickupTime;
        private String expiryDate;
        private String status;
        private String category;
        private String sellerId;
        private String reservedBy;
        private String reservedCharity;
        private String pickupAddress;
        private String imageBase64;

        public Reservation(String id, String foodItem, String restaurant, String quantity,
                           String pickupTime, String expiryDate, String status,
                           String category, String sellerId, String reservedBy,
                           String reservedCharity, String pickupAddress, String imageBase64) {
            this.id = id;
            this.foodItem = foodItem;
            this.restaurant = restaurant;
            this.quantity = quantity;
            this.pickupTime = pickupTime;
            this.expiryDate = expiryDate;
            this.status = status;
            this.category = category;
            this.sellerId = sellerId;
            this.reservedBy = reservedBy;
            this.reservedCharity = reservedCharity;
            this.pickupAddress = pickupAddress;
            this.imageBase64 = imageBase64;
        }

        // Simplified constructor for backward compatibility
        public Reservation(String id, String foodItem, String restaurant, String quantity,
                           String pickupTime, String expiryDate, String status, String imageBase64) {
            this(id, foodItem, restaurant, quantity, pickupTime, expiryDate, status, imageBase64,
                    "", "", "", "", "");
        }

        // Getters
        public String getId() { return id; }
        public String getFoodItem() { return foodItem; }
        public String getRestaurant() { return restaurant; }
        public String getQuantity() { return quantity; }
        public String getPickupTime() { return pickupTime; }
        public String getExpiryDate() { return expiryDate; }
        public String getStatus() { return status; }
        public String getCategory() { return category; }
        public String getSellerId() { return sellerId; }
        public String getReservedBy() { return reservedBy; }
        public String getReservedCharity() { return reservedCharity; }
        public String getPickupAddress() { return pickupAddress; }
        public String getImageBase64() { return imageBase64; }

    }
}