package com.example.foodlink;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import android.content.SharedPreferences;
import java.util.HashMap;
import java.util.Map;

import java.util.Locale;

public class FoodDetailsActivity extends AppCompatActivity {

    private Toolbar toolbar;
//    private ImageView btnBack;
    private ImageView ivFoodImage;
    private View llNoImage;
    private TextView tvFoodName;
    private TextView tvQuantity;
    private TextView tvCategory;
    private TextView tvExpiryDate;
    private TextView tvStartTime;
    private TextView tvEndTime;
    private TextView tvAdditionalDetails;
    private TextView tvCurrentStatus;
    private MaterialButton btnCancel;
    private MaterialButton btnReserve;

    private FirebaseFirestore db;
    private String foodId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_food_listing);

        Log.d("FoodDetails", "=== FoodDetailsActivity onCreate ===");

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Get food ID from intent
        if (getIntent() != null) {
            Bundle extras = getIntent().getExtras();
            if (extras != null) {
                Log.d("FoodDetails", "Intent extras keys: " + extras.keySet());
                for (String key : extras.keySet()) {
                    Object value = extras.get(key);
                    if (value != null) {
                        Log.d("FoodDetails", "Key: " + key + ", Type: " + value.getClass().getSimpleName() +
                                ", Size: " + (value instanceof String ? ((String)value).length() : "N/A"));
                    }
                }
            }

            if (getIntent().hasExtra("food_id")) {
                foodId = getIntent().getStringExtra("food_id");
                Log.d("FoodDetails", "Received food ID: " + foodId);
            } else {
                Log.e("FoodDetails", "No food_id in intent!");
                Toast.makeText(this, "Error: No food ID provided", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
        } else {
            Log.e("FoodDetails", "Intent is null!");
            finish();
            return;
        }

        initViews();
        setupToolbar();
        setupListeners();
        setupBackPressHandler();

        // Fetch food details from Firestore
        fetchFoodDetails();
    }

    private void initViews() {
        // Toolbar and back button
        toolbar = findViewById(R.id.toolbar);
//        btnBack = findViewById(R.id.btnBack);

        // Food image
        ivFoodImage = findViewById(R.id.ivFoodImage);
        llNoImage = findViewById(R.id.llNoImage);

        // Text views for food details
        tvFoodName = findViewById(R.id.tvFoodName);
        tvQuantity = findViewById(R.id.tvQuantity);
        tvCategory = findViewById(R.id.tvCategory);
        tvExpiryDate = findViewById(R.id.tvExpiryDate);
        tvStartTime = findViewById(R.id.tvStartTime);
        tvEndTime = findViewById(R.id.tvEndTime);
        tvAdditionalDetails = findViewById(R.id.tvAdditionalDetails);
        tvCurrentStatus = findViewById(R.id.tvCurrentStatus);

        // Action button
        btnCancel = findViewById(R.id.btnCancel);
        btnReserve = findViewById(R.id.btnReserve);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
    }

    private void fetchFoodDetails() {
        db.collection("food_listings")
                .document(foodId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        populateData(documentSnapshot);
                    } else {
                        Toast.makeText(this, "Food listing not found", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error loading food details: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    private void populateData(DocumentSnapshot document) {
        // Set food name
        String foodName = document.getString("food_name");
        tvFoodName.setText(foodName != null ? foodName : "No Name Provided");

        // FIXED: Handle quantity field (it might be a number, not a string)
        Object quantityObj = document.get("quantity");
        String quantityText = "Not specified";
        if (quantityObj != null) {
            if (quantityObj instanceof Long) {
                quantityText = ((Long) quantityObj) + " kg";
            } else if (quantityObj instanceof Integer) {
                quantityText = ((Integer) quantityObj) + " kg";
            } else if (quantityObj instanceof Double) {
                quantityText = String.format(Locale.getDefault(), "%.1f kg", (Double) quantityObj);
            } else if (quantityObj instanceof String) {
                quantityText = (String) quantityObj;
            } else {
                quantityText = quantityObj.toString() + " kg";
            }
        }
        tvQuantity.setText(quantityText);

        // Set category
        String category = document.getString("category");
        tvCategory.setText(category != null ? category : "Not categorized");

        // Set expiry date
        String expiryDate = document.getString("expiry_date");
        tvExpiryDate.setText(expiryDate != null ? expiryDate : "Not specified");

        // Set pickup times
        String startTime = document.getString("start_time");
        tvStartTime.setText(startTime != null ? startTime : "Not specified");

        String endTime = document.getString("end_time");
        tvEndTime.setText(endTime != null ? endTime : "Not specified");

        // Set additional details (called 'description' in your Firestore)
        String description = document.getString("description");
        tvAdditionalDetails.setText(description != null ? description : "No additional details provided.");

        // Set status (you might need to add this field to Firestore)
        String status = document.getString("status");
        if (status != null && !status.isEmpty()) {
            tvCurrentStatus.setText(status);
            updateStatusUI(status);
        } else {
            tvCurrentStatus.setText("Available");
            updateStatusUI("available");
        }

        // Load image from Base64
        String imageBase64 = document.getString("image_base64");
        loadFoodImage(imageBase64);
    }

    private void loadFoodImage(String imageBase64) {
        if (imageBase64 != null && !imageBase64.isEmpty()) {
            // Hide "no image" layout
            llNoImage.setVisibility(View.GONE);
            ivFoodImage.setVisibility(View.VISIBLE);

            try {
                // Decode Base64 string to bitmap
                byte[] decodedString = android.util.Base64.decode(imageBase64, android.util.Base64.DEFAULT);
                android.graphics.Bitmap decodedByte = android.graphics.BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                ivFoodImage.setImageBitmap(decodedByte);
            } catch (Exception e) {
                // If Base64 decoding fails, show placeholder
                llNoImage.setVisibility(View.VISIBLE);
                ivFoodImage.setVisibility(View.GONE);
                e.printStackTrace();
            }
        } else {
            // Show "no image" layout
            llNoImage.setVisibility(View.VISIBLE);
            ivFoodImage.setVisibility(View.GONE);
        }
    }

    private void updateStatusUI(String status) {
        // Same as before, using your color resources
        if (status.equalsIgnoreCase("available")) {
            tvCurrentStatus.setTextColor(getResources().getColor(R.color.status_active));
        } else if (status.equalsIgnoreCase("reserved")) {
            tvCurrentStatus.setTextColor(getResources().getColor(R.color.status_warning));
            btnReserve.setEnabled(false);
            btnReserve.setText("Already Reserved");
            btnReserve.setBackgroundColor(getResources().getColor(R.color.grey_500));
        } else if (status.equalsIgnoreCase("collected")) {
            tvCurrentStatus.setTextColor(getResources().getColor(R.color.status_info));
            btnReserve.setEnabled(false);
            btnReserve.setText("Already Collected");
            btnReserve.setBackgroundColor(getResources().getColor(R.color.grey_500));
        } else if (status.equalsIgnoreCase("expired")) {
            tvCurrentStatus.setTextColor(getResources().getColor(R.color.status_expired));
            btnReserve.setEnabled(false);
            btnReserve.setText("Expired");
            btnReserve.setBackgroundColor(getResources().getColor(R.color.grey_500));
        }
    }

    private void setupListeners() {
        // Back button click
//        btnBack.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                onBackPressed();
//            }
//        });

        // Reserve button click
        btnReserve.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reserveFoodItem();
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                navigateToDashboard();
            }
        });
    }

    private void reserveFoodItem() {
        // Get current user ID
        String currentUserUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Get charity name (you might need to get this from Firestore or SharedPreferences)
        // For example, if you have it stored in SharedPreferences:
        SharedPreferences prefs = getSharedPreferences("user_data", MODE_PRIVATE);
        String charityName = prefs.getString("charity_name", "Unknown Charity");

        // OR if you have it as a field in your FoodDetailsActivity class, use that

        // Create a Map with the fields to update (same as CharityDashboard)
        Map<String, Object> updateData = new HashMap<>();
        updateData.put("status", "reserved");
        updateData.put("reserved_by", currentUserUid);
        updateData.put("reserved_charity", charityName);
        updateData.put("updated_at", FieldValue.serverTimestamp());

        // Update the document in Firestore
        db.collection("food_listings")
                .document(foodId)
                .update(updateData)
                .addOnSuccessListener(aVoid -> {
                    // Update UI
                    tvCurrentStatus.setText("Reserved");
                    updateStatusUI("reserved");

                    // Option 1: Navigate to reservations page (same as CharityDashboard)
                    // You'll need to get additional data first
                    navigateToReservations();

                    Toast.makeText(this, "Food item reserved successfully!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to reserve: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    // New method to navigate to reservations
    private void navigateToReservations() {
        // First, get the current food details to pass to ReservationsActivity
        db.collection("food_listings")
                .document(foodId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Intent intent = new Intent(FoodDetailsActivity.this, ReservationsActivity.class);

                        // Pass all the data you need (matching CharityDashboard)
                        intent.putExtra("listing_id", foodId);
                        intent.putExtra("food_name", documentSnapshot.getString("food_name"));
                        intent.putExtra("restaurant", documentSnapshot.getString("restaurant_name")); // Adjust field name if needed
                        Object quantityObj = documentSnapshot.get("quantity");
                        String quantityStr = "Not specified";
                        if (quantityObj != null) {
                            if (quantityObj instanceof Long) {
                                quantityStr = ((Long) quantityObj) + " kg";
                            } else if (quantityObj instanceof Integer) {
                                quantityStr = ((Integer) quantityObj) + " kg";
                            } else if (quantityObj instanceof Double) {
                                quantityStr = String.format(Locale.getDefault(), "%.1f kg", (Double) quantityObj);
                            } else if (quantityObj instanceof String) {
                                quantityStr = (String) quantityObj;
                            } else {
                                quantityStr = quantityObj.toString() + " kg";
                            }
                        }
                        intent.putExtra("quantity", quantityStr);
                        intent.putExtra("pickup_time", documentSnapshot.getString("pickup_time")); // Adjust field name if needed
                        intent.putExtra("expiry_date", documentSnapshot.getString("expiry_date"));
                        intent.putExtra("seller_id", documentSnapshot.getString("seller_id"));

                        startActivity(intent);
                        finish(); // Close FoodDetailsActivity
                    }
                })
                .addOnFailureListener(e -> {
                    // If we can't get the data, just go to ReservationsActivity without extras
                    Intent intent = new Intent(FoodDetailsActivity.this, ReservationsActivity.class);
                    startActivity(intent);
                    finish();
                });
    }

    private void setupBackPressHandler() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                navigateToDashboard();
            }
        });
    }

    private void navigateToDashboard() {
        Intent intent = new Intent(this, CharityDashboardActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        finish();
    }

}