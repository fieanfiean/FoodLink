package com.example.foodlink;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class EditFoodListingActivity extends AppCompatActivity {

    private static final String TAG = "EditFoodListing";
    private static final int PICK_IMAGE_REQUEST = 1;

    // Views
    private TextInputEditText etFoodName, etQuantity, etExpiryDate, etStartTime, etEndTime, etAdditionalDetails;
    private com.google.android.material.textfield.MaterialAutoCompleteTextView actvCategory;
    private TextView tvCurrentStatus;
    private CardView cardStatus;
    private ImageView ivFoodImage;
    private LinearLayout llDefaultImage;
    private MaterialButton btnUploadImage, btnSave, btnCancel, btnDelete;
    private com.google.android.material.floatingactionbutton.FloatingActionButton fabRemoveImage;

    // Firebase
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    // Data
    private String listingId;
    private String currentUserId;
    private String currentImageBase64;
    private boolean imageChanged = false;
    private final Calendar calendar = Calendar.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_food_listing);

        try {
            // Get listing ID from intent
            Intent intent = getIntent();
            if (intent == null || !intent.hasExtra("listing_id")) {
                Toast.makeText(this, "No listing selected", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            listingId = intent.getStringExtra("listing_id");
            Log.d(TAG, "Editing listing ID: " + listingId);

            // Initialize Firebase
            db = FirebaseFirestore.getInstance();
            mAuth = FirebaseAuth.getInstance();

            // Get current user
            if (mAuth.getCurrentUser() == null) {
                Toast.makeText(this, "Please login again", Toast.LENGTH_SHORT).show();
                navigateToLogin();
                return;
            }
            currentUserId = mAuth.getCurrentUser().getUid();

            // Initialize views
            initViews();
            setupClickListeners();
            setupCategoryDropdown();
            setupDateAndTimePickers();
            loadListingData();
            setupBackPressHandler();

        } catch (Exception e) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            Log.e(TAG, "Error in onCreate: " + e.getMessage());
            finish();
        }
    }

    @SuppressLint("WrongViewCast")
    private void initViews() {
        // Text inputs
        etFoodName = findViewById(R.id.etFoodName);
        etQuantity = findViewById(R.id.etQuantity);
        etExpiryDate = findViewById(R.id.etExpiryDate);
        etStartTime = findViewById(R.id.etStartTime);
        etEndTime = findViewById(R.id.etEndTime);
        etAdditionalDetails = findViewById(R.id.etAdditionalDetails);
        actvCategory = findViewById(R.id.actvCategory);

        // Status
        tvCurrentStatus = findViewById(R.id.tvCurrentStatus);
        cardStatus = findViewById(R.id.cardStatus);

        // Image views
        ivFoodImage = findViewById(R.id.ivFoodImage);
        llDefaultImage = findViewById(R.id.llDefaultImage);
        fabRemoveImage = findViewById(R.id.fabRemoveImage);

        // Buttons
        btnUploadImage = findViewById(R.id.btnUploadImage);
        btnSave = findViewById(R.id.btnSave);
        btnCancel = findViewById(R.id.btnCancel);
//        btnDelete = findViewById(R.id.btnDelete);

        // Back button
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }

    private void setupClickListeners() {
        // Upload Image
        findViewById(R.id.cardImageUpload).setOnClickListener(v -> openImageChooser());
        btnUploadImage.setOnClickListener(v -> openImageChooser());

        // Remove Image
        fabRemoveImage.setOnClickListener(v -> removeImage());

        // Save Changes
        btnSave.setOnClickListener(v -> saveChanges());

        // Cancel
        btnCancel.setOnClickListener(v -> finish());

        // Delete Listing
//        btnDelete.setOnClickListener(v -> showDeleteConfirmation());
    }

    private void setupCategoryDropdown() {
        List<String> categories = new ArrayList<>();
        categories.add("Vegetables");
        categories.add("Fruits");
        categories.add("Bakery");
        categories.add("Dairy");
        categories.add("Meat");
        categories.add("Seafood");
        categories.add("Ready Meals");
        categories.add("Beverages");
        categories.add("Other");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, R.layout.dropdown_menu_item, categories);
        actvCategory.setAdapter(adapter);
    }

    private void setupDateAndTimePickers() {
        // Expiry Date Picker
        etExpiryDate.setOnClickListener(v -> showDatePicker());

        // Start Time Picker
        etStartTime.setOnClickListener(v -> showTimePicker(etStartTime));

        // End Time Picker
        etEndTime.setOnClickListener(v -> showTimePicker(etEndTime));
    }

    private void showDatePicker() {
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    calendar.set(selectedYear, selectedMonth, selectedDay);
                    String dateStr = String.format(Locale.getDefault(),
                            "%02d/%02d/%04d", selectedDay, selectedMonth + 1, selectedYear);
                    etExpiryDate.setText(dateStr);
                },
                year, month, day
        );

        // Set min date to today
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        datePickerDialog.show();
    }

    private void showTimePicker(TextInputEditText editText) {
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(
                this,
                (view, selectedHour, selectedMinute) -> {
                    String timeStr = String.format(Locale.getDefault(),
                            "%02d:%02d", selectedHour, selectedMinute);
                    editText.setText(timeStr);
                },
                hour, minute, true
        );
        timePickerDialog.show();
    }

    private void loadListingData() {
        if (listingId == null || listingId.isEmpty()) {
            Toast.makeText(this, "Invalid listing ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        db.collection("food_listings").document(listingId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        populateFields(documentSnapshot);
                    } else {
                        Toast.makeText(this, "Listing not found", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error loading listing: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error loading listing: " + e.getMessage());
                    finish();
                });
    }

    private void populateFields(DocumentSnapshot document) {
        try {
            // Basic information
            etFoodName.setText(document.getString("food_name"));

            // FIX: Handle quantity properly (it's Double in Firestore)
            Object quantityObj = document.get("quantity");
            if (quantityObj != null) {
                if (quantityObj instanceof Double) {
                    // If it's Double, remove decimal if it's .0
                    Double quantityDouble = (Double) quantityObj;
                    if (quantityDouble % 1 == 0) {
                        // It's a whole number (e.g., 5.0)
                        etQuantity.setText(String.valueOf(quantityDouble.intValue()));
                    } else {
                        // It has decimal places
                        etQuantity.setText(String.valueOf(quantityDouble));
                    }
                } else if (quantityObj instanceof Long) {
                    etQuantity.setText(String.valueOf((Long) quantityObj));
                } else if (quantityObj instanceof Integer) {
                    etQuantity.setText(String.valueOf((Integer) quantityObj));
                } else if (quantityObj instanceof String) {
                    etQuantity.setText((String) quantityObj);
                }
            }

            // Category
            String category = document.getString("category");
            if (category != null) {
                actvCategory.setText(category, false);
            }

            // Dates and times
            etExpiryDate.setText(document.getString("expiry_date"));
            etStartTime.setText(document.getString("start_time"));
            etEndTime.setText(document.getString("end_time"));

            // Additional details
            String details = document.getString("additional_details");
            if (details != null && !details.isEmpty()) {
                etAdditionalDetails.setText(details);
            }

            // Status
            String status = document.getString("status");
            if (status != null) {
                tvCurrentStatus.setText(status);
                updateStatusCardColor(status);
            }

            // Image
            String imageBase64 = document.getString("image_base64");
            if (imageBase64 != null && !imageBase64.isEmpty() && !imageBase64.equals("null")) {
                currentImageBase64 = imageBase64;
                loadImageFromBase64(imageBase64);
            }

            Log.d(TAG, "Listing data loaded successfully");

        } catch (Exception e) {
            Log.e(TAG, "Error populating fields: " + e.getMessage());
        }
    }

    private void updateStatusCardColor(String status) {
        int backgroundColor;
        int textColor;

        switch (status.toLowerCase()) {
            case "available":
                backgroundColor = getColor(R.color.green_light);
                textColor = getColor(R.color.green_500);
                break;
            case "reserved":
                backgroundColor = getColor(R.color.orange_light);
                textColor = getColor(R.color.orange_500);
                break;
            case "completed":
                backgroundColor = getColor(R.color.blue_light);
                textColor = getColor(R.color.blue_500);
                break;
            default:
                backgroundColor = getColor(R.color.green_light);
                textColor = getColor(R.color.green_500);
        }

        cardStatus.setCardBackgroundColor(backgroundColor);
        tvCurrentStatus.setTextColor(textColor);
    }

    private void loadImageFromBase64(String base64String) {
        try {
            if (base64String.contains("base64,")) {
                base64String = base64String.substring(base64String.indexOf("base64,") + 7);
            }

            byte[] decodedBytes = Base64.decode(base64String, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);

            if (bitmap != null) {
                ivFoodImage.setImageBitmap(bitmap);
                ivFoodImage.setVisibility(View.VISIBLE);
                llDefaultImage.setVisibility(View.GONE);
                fabRemoveImage.setVisibility(View.VISIBLE);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error loading image: " + e.getMessage());
        }
    }

    private void openImageChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                // Resize bitmap to avoid large files
                bitmap = resizeBitmap(bitmap, 800);

                // Convert to Base64
                currentImageBase64 = bitmapToBase64(bitmap);
                imageChanged = true;

                // Update UI
                ivFoodImage.setImageBitmap(bitmap);
                ivFoodImage.setVisibility(View.VISIBLE);
                llDefaultImage.setVisibility(View.GONE);
                fabRemoveImage.setVisibility(View.VISIBLE);

            } catch (IOException e) {
                Toast.makeText(this, "Error loading image", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Error loading image: " + e.getMessage());
            }
        }
    }

    private Bitmap resizeBitmap(Bitmap bitmap, int maxSize) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        float ratio = (float) width / height;

        if (width > height && width > maxSize) {
            width = maxSize;
            height = (int) (width / ratio);
        } else if (height > maxSize) {
            height = maxSize;
            width = (int) (height * ratio);
        }

        return Bitmap.createScaledBitmap(bitmap, width, height, true);
    }

    private String bitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return "data:image/jpeg;base64," + Base64.encodeToString(byteArray, Base64.DEFAULT);
    }

    private void removeImage() {
        currentImageBase64 = null;
        imageChanged = true;

        ivFoodImage.setVisibility(View.GONE);
        llDefaultImage.setVisibility(View.VISIBLE);
        fabRemoveImage.setVisibility(View.GONE);

        Toast.makeText(this, "Image removed", Toast.LENGTH_SHORT).show();
    }

    private void saveChanges() {
        // Validate inputs
        if (!validateInputs()) {
            return;
        }

        // Get updated values
        String foodName = etFoodName.getText().toString().trim();
        String quantityStr = etQuantity.getText().toString().trim();
        String category = actvCategory.getText().toString().trim();
        String expiryDate = etExpiryDate.getText().toString().trim();
        String startTime = etStartTime.getText().toString().trim();
        String endTime = etEndTime.getText().toString().trim();
        String additionalDetails = etAdditionalDetails.getText().toString().trim();

        // Parse quantity
        int quantity;
        try {
            quantity = Integer.parseInt(quantityStr);
            if (quantity <= 0) {
                Toast.makeText(this, "Quantity must be greater than 0", Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Please enter a valid quantity", Toast.LENGTH_SHORT).show();
            return;
        }

        // Prepare update data
        Map<String, Object> updates = new HashMap<>();
        updates.put("food_name", foodName);
        updates.put("quantity", quantity);
        updates.put("category", category);
        updates.put("expiry_date", expiryDate);
        updates.put("start_time", startTime);
        updates.put("end_time", endTime);
        updates.put("additional_details", additionalDetails);
        updates.put("updated_at", new Date());

        // Only update image if changed
        if (imageChanged) {
            updates.put("image_base64", currentImageBase64);
        }

        // Show loading
        btnSave.setText("Saving...");
        btnSave.setEnabled(false);

        // Update in Firestore
        db.collection("food_listings").document(listingId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Listing updated successfully", Toast.LENGTH_SHORT).show();

                    // Return to dashboard with refresh flag
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("listing_updated", true);
                    resultIntent.putExtra("listing_id", listingId);
                    setResult(RESULT_OK, resultIntent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error updating listing: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error updating listing: " + e.getMessage());

                    // Reset button
                    btnSave.setText("Save Changes");
                    btnSave.setEnabled(true);
                });
    }

    private boolean validateInputs() {
        if (etFoodName.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Please enter food name", Toast.LENGTH_SHORT).show();
            etFoodName.requestFocus();
            return false;
        }

        if (etQuantity.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Please enter quantity", Toast.LENGTH_SHORT).show();
            etQuantity.requestFocus();
            return false;
        }

        if (actvCategory.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Please select a category", Toast.LENGTH_SHORT).show();
            actvCategory.requestFocus();
            return false;
        }

        if (etExpiryDate.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Please select expiry date", Toast.LENGTH_SHORT).show();
            etExpiryDate.requestFocus();
            return false;
        }

        if (etStartTime.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Please select start time", Toast.LENGTH_SHORT).show();
            etStartTime.requestFocus();
            return false;
        }

        if (etEndTime.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Please select end time", Toast.LENGTH_SHORT).show();
            etEndTime.requestFocus();
            return false;
        }

        return true;
    }



    private void setupBackPressHandler() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Intent intent = new Intent(EditFoodListingActivity.this, SellerDashboardActivity.class);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                startActivity(intent);
                finish();
            }
        });
    }

    private void navigateToLogin() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}