package com.example.foodlink;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ReservationsActivity extends AppCompatActivity {

    // UI Components
    private ImageView ivBack;
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
            "Cancelled"
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reservations);

        // Initialize UI Components
        initViews();

        // Setup toolbar
        setupToolbar();

        // Setup status filter dropdown
        setupStatusFilter();

        // Load sample data
        loadSampleData();

        // Setup listeners
        setupListeners();

        // Setup recycler view
        setupRecyclerView();

        // Update count
        updateReservationsCount();
    }

    private void initViews() {
        // Toolbar components
        ivBack = findViewById(R.id.ivBack);
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
                String selectedStatus = statusFilters.get(position);
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

    private void loadSampleData() {
        reservations.clear();

        // Add sample reservations
        reservations.add(new Reservation(
                "RES001",
                "Assorted Fresh Bread",
                "Sunrise Bakery",
                "20 loaves",
                "19:00-21:00",
                "2025-12-17",
                "Upcoming"
        ));

        reservations.add(new Reservation(
                "RES002",
                "Fresh Vegetable Salad Mix",
                "Green Leaf Restaurant",
                "5 kg",
                "18:00-20:00",
                "2025-12-17",
                "Pending"
        ));

        reservations.add(new Reservation(
                "RES003",
                "Mixed Fruit Basket",
                "Fresh Farm Market",
                "3 kg",
                "14:00-16:00",
                "2025-12-16",
                "Completed"
        ));

        reservations.add(new Reservation(
                "RES004",
                "Prepared Meals Pack",
                "Community Kitchen",
                "12 portions",
                "16:00-18:00",
                "2025-12-18",
                "Cancelled"
        ));

        reservations.add(new Reservation(
                "RES005",
                "Milk and Cheese",
                "Dairy Delight",
                "8 liters",
                "10:00-12:00",
                "2025-12-19",
                "Upcoming"
        ));

        // Initially show all reservations
        filteredReservations.addAll(reservations);
    }

    private void setupListeners() {
        // Back button
        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        // Notification icon
        ivNotification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to notifications
                Toast.makeText(ReservationsActivity.this, "Notifications", Toast.LENGTH_SHORT).show();
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
                        // Navigate back to browse food
                        finish();
//                        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                    }
                });
            }
        }
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
                    selectedStatus.equals(reservation.getStatus());

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
        Toast.makeText(this,
                "Confirming pickup for: " + reservation.getFoodItem(),
                Toast.LENGTH_SHORT).show();
        // TODO: Implement actual confirmation logic
    }

    private void cancelReservation(Reservation reservation) {
        Toast.makeText(this,
                "Cancelling reservation: " + reservation.getFoodItem(),
                Toast.LENGTH_SHORT).show();
        // TODO: Implement actual cancellation logic
    }

    private void viewReservationDetails(Reservation reservation) {
        Toast.makeText(this,
                "Viewing details for: " + reservation.getFoodItem(),
                Toast.LENGTH_SHORT).show();
        // TODO: Navigate to reservation details screen
    }

    private void reorderReservation(Reservation reservation) {
        Toast.makeText(this,
                "Reordering: " + reservation.getFoodItem(),
                Toast.LENGTH_SHORT).show();
        // TODO: Implement reorder logic
    }

//    @Override
//    public void onBackPressed() {
//        super.onBackPressed();
//        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
//    }

    // Reservation model class
    public static class Reservation {
        private String id;
        private String foodItem;
        private String restaurant;
        private String quantity;
        private String pickupTime;
        private String expiryDate;
        private String status;

        public Reservation(String id, String foodItem, String restaurant, String quantity,
                           String pickupTime, String expiryDate, String status) {
            this.id = id;
            this.foodItem = foodItem;
            this.restaurant = restaurant;
            this.quantity = quantity;
            this.pickupTime = pickupTime;
            this.expiryDate = expiryDate;
            this.status = status;
        }

        // Getters
        public String getId() { return id; }
        public String getFoodItem() { return foodItem; }
        public String getRestaurant() { return restaurant; }
        public String getQuantity() { return quantity; }
        public String getPickupTime() { return pickupTime; }
        public String getExpiryDate() { return expiryDate; }
        public String getStatus() { return status; }
    }
}