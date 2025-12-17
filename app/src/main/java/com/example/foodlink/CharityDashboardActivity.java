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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CharityDashboardActivity extends AppCompatActivity {

    // UI Components
    private ImageView ivNotification;
    private ImageView ivProfile;
    private TextView tvCharityName;
    private MaterialButton btnBrowseFood;
    private MaterialButton btnMyReservations;
    private TextInputEditText etSearch;
    private TextInputLayout filterDropdownLayout;
    private AutoCompleteTextView categoryAutoComplete;
    private TextView tvListingCount;
    private View llListingsContainer;
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

    private BottomNavigationView bottomNavigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_charity_dashboard);

        // Initialize UI Components
        initViews();

        // Setup exposed dropdown menu
        setupExposedDropdownMenu();

        // Load sample data
        loadSampleData();

        // Setup listeners
        setupListeners();
        setupBottomNavigation();

        // Setup toolbar
        setupToolbar();

        // Update listings count
        updateListingsCount();
    }

    private void initViews() {
        // Toolbar components
        ivNotification = findViewById(R.id.ivNotification);
        ivProfile = findViewById(R.id.ivProfile);

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

    private void loadSampleData() {
        // Clear existing data
        foodListings.clear();

        // Add sample food listings
        foodListings.add(new FoodListing(
                "Fresh Vegetable Salad Mix",
                "Vegetables",
                "5 kg",
                "Green Leaf Restaurant",
                "18:00-20:00",
                "2025-12-17",
                R.drawable.ic_vegetables
        ));

        foodListings.add(new FoodListing(
                "Assorted Pastries",
                "Bakery",
                "25 pieces",
                "Sunshine Bakery",
                "09:00-17:00",
                "2025-12-18",
                R.drawable.ic_bakery
        ));

        foodListings.add(new FoodListing(
                "Mixed Fruit Basket",
                "Fruits",
                "3 kg",
                "Fresh Farm Market",
                "14:00-18:00",
                "2025-12-16",
                R.drawable.ic_fruits
        ));

        foodListings.add(new FoodListing(
                "Prepared Meals Pack",
                "Prepared Food",
                "12 portions",
                "Community Kitchen",
                "16:00-19:00",
                "2025-12-19",
                R.drawable.ic_meal
        ));

        foodListings.add(new FoodListing(
                "Milk and Cheese",
                "Dairy",
                "8 liters",
                "Dairy Delight",
                "10:00-15:00",
                "2025-12-20",
                R.drawable.ic_dairy
        ));

        // Initially show all listings
        filteredListings.addAll(foodListings);
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
//                Toast.makeText(CharityDashboardActivity.this, "Notifications", Toast.LENGTH_SHORT).show();
                navigateToNotification();
            }
        });

        // Profile icon
        ivProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(CharityDashboardActivity.this, "Profile", Toast.LENGTH_SHORT).show();
                navigateToProfile();
            }
        });

        // Browse Food button
        btnBrowseFood.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(CharityDashboardActivity.this, "Browse Food", Toast.LENGTH_SHORT).show();
                navigateToBrowseFood();
            }
        });

        // My Reservations button
        btnMyReservations.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(CharityDashboardActivity.this, "My Reservations", Toast.LENGTH_SHORT).show();
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

        // Reserve buttons
        findViewById(R.id.btnReserve1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reserveFoodListing(0);
            }
        });

        findViewById(R.id.btnReserve2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reserveFoodListing(1);
            }
        });
    }

    private void navigateToReservation() {
        Intent intent = new Intent(this, ReservationsActivity.class);
        startActivity(intent);
    }

    private void navigateToBrowseFood() {

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

    private void setupBottomNavigation() {
        // Clear existing menu items
        bottomNavigation.getMenu().clear();

        // Add only the items you want for seller
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
                String selectedCategory = categories.get(position);
                filterListings(); // Call the filter method
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

            // Check if listing is not expired
            boolean isNotExpired = true; // In real app, check expiry date

            if (matchesSearch && matchesCategory && isNotExpired) {
                filteredListings.add(listing);
            }
        }

        updateUI();
    }

    private void updateUI() {
        // Show/hide empty state
        if (filteredListings.isEmpty()) {
            cardEmptyState.setVisibility(View.VISIBLE);
            llListingsContainer.setVisibility(View.GONE);
        } else {
            cardEmptyState.setVisibility(View.GONE);
            llListingsContainer.setVisibility(View.VISIBLE);
        }

        updateListingsCount();
    }

    private void updateListingsCount() {
        int count = filteredListings.size();
        String countText = count + " listing" + (count != 1 ? "s" : "");
        tvListingCount.setText(countText);
    }

    private void reserveFoodListing(int position) {
        if (position < filteredListings.size()) {
            FoodListing listing = filteredListings.get(position);

            Toast.makeText(this,
                    "Reserving: " + listing.getName() + " from " + listing.getRestaurant(),
                    Toast.LENGTH_SHORT).show();
        }
    }

    // FoodListing model class
    public static class FoodListing {
        private String name;
        private String category;
        private String quantity;
        private String restaurant;
        private String pickupTime;
        private String expiryDate;
        private int iconResource;

        public FoodListing(String name, String category, String quantity,
                           String restaurant, String pickupTime, String expiryDate, int iconResource) {
            this.name = name;
            this.category = category;
            this.quantity = quantity;
            this.restaurant = restaurant;
            this.pickupTime = pickupTime;
            this.expiryDate = expiryDate;
            this.iconResource = iconResource;
        }

        // Getters
        public String getName() { return name; }
        public String getCategory() { return category; }
        public String getQuantity() { return quantity; }
        public String getRestaurant() { return restaurant; }
        public String getPickupTime() { return pickupTime; }
        public String getExpiryDate() { return expiryDate; }
        public int getIconResource() { return iconResource; }
    }
}