package com.example.foodlink;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class AddNewFoodListingActivity extends AppCompatActivity {

    // UI Components
    private ImageView btnBack;
    private TextInputEditText etFoodName;
    private TextInputEditText etQuantity;
    private AutoCompleteTextView actvCategory;
    private TextInputEditText etExpiryDate;
    private TextInputEditText etStartTime;
    private TextInputEditText etEndTime;
    private TextInputEditText etAdditionalDetails;
    private TextInputLayout tilStartTime;
    private TextInputLayout tilEndTime;
    private MaterialButton btnUploadImage;
    private MaterialButton btnCancel;
    private MaterialButton btnPublish;
    private View cardImageUpload;
    private View llDefaultImage;
    private ImageView ivFoodImage;
    private FloatingActionButton fabRemoveImage;

    // Constants
    private static final int PICK_IMAGE_REQUEST = 1;
    private Calendar calendar;
    private Calendar startTimeCalendar;
    private Calendar endTimeCalendar;
    private SimpleDateFormat dateFormatter;
    private SimpleDateFormat timeFormatter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_food_listing);

        // Initialize calendars and formatters
        calendar = Calendar.getInstance();
        startTimeCalendar = Calendar.getInstance();
        endTimeCalendar = Calendar.getInstance();
        dateFormatter = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        timeFormatter = new SimpleDateFormat("HH:mm", Locale.getDefault());

        // Initialize UI Components
        initViews();

        // Setup category dropdown
        setupCategoryDropdown();

        // Setup listeners
        setupListeners();

        // Setup toolbar
        setupToolbar();

        // Setup form validation
        setupFormValidation();
    }

    private void initViews() {
        // Toolbar and navigation
        btnBack = findViewById(R.id.btnBack);

        // Form fields
        etFoodName = findViewById(R.id.etFoodName);
        etQuantity = findViewById(R.id.etQuantity);
        actvCategory = findViewById(R.id.actvCategory);
        etExpiryDate = findViewById(R.id.etExpiryDate);
        etStartTime = findViewById(R.id.etStartTime);
        etEndTime = findViewById(R.id.etEndTime);
        etAdditionalDetails = findViewById(R.id.etAdditionalDetails);

        // TextInputLayouts for error display
        tilStartTime = findViewById(R.id.tilStartTime);
        tilEndTime = findViewById(R.id.tilEndTime);

        // Image upload components
        cardImageUpload = findViewById(R.id.cardImageUpload);
        llDefaultImage = findViewById(R.id.llDefaultImage);
        ivFoodImage = findViewById(R.id.ivFoodImage);
        fabRemoveImage = findViewById(R.id.fabRemoveImage);
        btnUploadImage = findViewById(R.id.btnUploadImage);

        // Action buttons
        btnCancel = findViewById(R.id.btnCancel);
        btnPublish = findViewById(R.id.btnPublish);
    }

    private void setupCategoryDropdown() {
        // Define categories
        String[] categories = {"All","Vegetables", "Fruits", "Bakery", "Prepared Food",
                "Dairy", "Other"};

        // Create adapter for dropdown
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, categories);
        actvCategory.setAdapter(adapter);

        // Set default value
        actvCategory.setText("All", false);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
    }

    private void setupListeners() {
        // Back button
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        // Expiry date picker
        etExpiryDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePicker();
            }
        });

        // Time pickers
        etStartTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showStartTimePicker();
            }
        });

        etEndTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showEndTimePicker();
            }
        });

        // Image upload click
        cardImageUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openImageChooser();
            }
        });

        btnUploadImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openImageChooser();
            }
        });

        // Remove image button
        fabRemoveImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeSelectedImage();
            }
        });

        // Cancel button
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (hasUnsavedChanges()) {
                    showUnsavedChangesDialog();
                } else {
                    finish();
                }
            }
        });

        // Publish button
        btnPublish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                publishListing();
            }
        });
    }

    private void setupFormValidation() {
        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validateForm();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        };

        etFoodName.addTextChangedListener(textWatcher);
        etQuantity.addTextChangedListener(textWatcher);
        etExpiryDate.addTextChangedListener(textWatcher);
//        etPickupTime.addTextChangedListener(textWatcher);

        // Also validate when category changes
        actvCategory.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                validateForm();
            }
        });

        // Initial validation
        validateForm();
    }

    // Add these methods for time pickers
    private void showStartTimePicker() {
        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        startTimeCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        startTimeCalendar.set(Calendar.MINUTE, minute);
                        etStartTime.setText(timeFormatter.format(startTimeCalendar.getTime()));

                        // Auto-set end time to 2 hours later if not set
                        if (etEndTime.getText().toString().isEmpty()) {
                            endTimeCalendar.setTimeInMillis(startTimeCalendar.getTimeInMillis());
                            endTimeCalendar.add(Calendar.HOUR, 2);
                            etEndTime.setText(timeFormatter.format(endTimeCalendar.getTime()));
                        }

                        validateForm();
                    }
                },
                startTimeCalendar.get(Calendar.HOUR_OF_DAY),
                startTimeCalendar.get(Calendar.MINUTE),
                true // 24-hour format
        );

        // Set title
        timePickerDialog.setTitle("Select Start Time");
        timePickerDialog.show();
    }

    private void showEndTimePicker() {
        // Set minimum time to start time (if start time is set)
        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        endTimeCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        endTimeCalendar.set(Calendar.MINUTE, minute);
                        etEndTime.setText(timeFormatter.format(endTimeCalendar.getTime()));
                        validateForm();
                    }
                },
                endTimeCalendar.get(Calendar.HOUR_OF_DAY),
                endTimeCalendar.get(Calendar.MINUTE),
                true // 24-hour format
        );

        // Set title
        timePickerDialog.setTitle("Select End Time");

        // Set minimum time to start time if start time is already selected
        if (!etStartTime.getText().toString().isEmpty()) {
            timePickerDialog.updateTime(
                    startTimeCalendar.get(Calendar.HOUR_OF_DAY),
                    startTimeCalendar.get(Calendar.MINUTE)
            );
        }

        timePickerDialog.show();
    }

    private void validateForm() {
        boolean isValid = true;

        // Check required fields
        if (etFoodName.getText().toString().trim().isEmpty()) {
            isValid = false;
        }

        if (etQuantity.getText().toString().trim().isEmpty()) {
            isValid = false;
        }

        if (actvCategory.getText().toString().trim().isEmpty()) {
            isValid = false;
        }

        if (etExpiryDate.getText().toString().trim().isEmpty()) {
            isValid = false;
        }

        // Check both time fields
        if (etStartTime.getText().toString().trim().isEmpty() ||
                etEndTime.getText().toString().trim().isEmpty()) {
            isValid = false;
        }

        // Validate that end time is after start time
        if (!etStartTime.getText().toString().isEmpty() &&
                !etEndTime.getText().toString().isEmpty()) {
            try {
                Calendar start = Calendar.getInstance();
                Calendar end = Calendar.getInstance();

                String[] startParts = etStartTime.getText().toString().split(":");
                String[] endParts = etEndTime.getText().toString().split(":");

                if (startParts.length == 2 && endParts.length == 2) {
                    start.set(Calendar.HOUR_OF_DAY, Integer.parseInt(startParts[0]));
                    start.set(Calendar.MINUTE, Integer.parseInt(startParts[1]));

                    end.set(Calendar.HOUR_OF_DAY, Integer.parseInt(endParts[0]));
                    end.set(Calendar.MINUTE, Integer.parseInt(endParts[1]));

                    if (!end.after(start)) {
                        // Show error if end time is not after start time
                        if (tilEndTime != null) {
                            tilEndTime.setError("End time must be after start time");
                        }
                        isValid = false;
                    } else {
                        if (tilEndTime != null) {
                            tilEndTime.setError(null);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                if (tilEndTime != null) {
                    tilEndTime.setError("Invalid time format");
                }
                isValid = false;
            }
        }
//
//        // Enable/disable publish button based on validation
//        btnPublish.setEnabled(isValid);
//        btnPublish.setAlpha(isValid ? 1.0f : 0.5f);
    }

    private void showDatePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(android.widget.DatePicker view, int year,
                                          int month, int dayOfMonth) {
                        calendar.set(Calendar.YEAR, year);
                        calendar.set(Calendar.MONTH, month);
                        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        etExpiryDate.setText(dateFormatter.format(calendar.getTime()));
                    }
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        // Set minimum date to today
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);

        // Set maximum date to 1 year from now
        Calendar maxDate = Calendar.getInstance();
        maxDate.add(Calendar.YEAR, 1);
        datePickerDialog.getDatePicker().setMaxDate(maxDate.getTimeInMillis());

        datePickerDialog.show();
    }

    private void openImageChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Food Image"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);

                // Show the selected image
                llDefaultImage.setVisibility(View.GONE);
                ivFoodImage.setVisibility(View.VISIBLE);
                fabRemoveImage.setVisibility(View.VISIBLE);
                ivFoodImage.setImageBitmap(bitmap);

                Toast.makeText(this, "Image selected", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void removeSelectedImage() {
        llDefaultImage.setVisibility(View.VISIBLE);
        ivFoodImage.setVisibility(View.GONE);
        fabRemoveImage.setVisibility(View.GONE);
        ivFoodImage.setImageBitmap(null);
    }

    private void publishListing() {
        // Get values from form
        String foodName = etFoodName.getText().toString().trim();
        String quantity = etQuantity.getText().toString().trim();
        String category = actvCategory.getText().toString().trim();
        String expiryDate = etExpiryDate.getText().toString().trim();
        String startTime = etStartTime.getText().toString().trim();
        String endTime = etEndTime.getText().toString().trim();
        String pickupTimeWindow = startTime + " - " + endTime;
        String additionalDetails = etAdditionalDetails.getText().toString().trim();

        // Validate again (just in case)
        if (foodName.isEmpty() || quantity.isEmpty() || category.isEmpty() ||
                expiryDate.isEmpty() || startTime.isEmpty() || endTime.isEmpty()) {
            Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate time order
        try {
            String[] startParts = startTime.split(":");
            String[] endParts = endTime.split(":");

            if (startParts.length == 2 && endParts.length == 2) {
                int startHour = Integer.parseInt(startParts[0]);
                int startMinute = Integer.parseInt(startParts[1]);
                int endHour = Integer.parseInt(endParts[0]);
                int endMinute = Integer.parseInt(endParts[1]);

                if (endHour < startHour || (endHour == startHour && endMinute <= startMinute)) {
                    Toast.makeText(this, "End time must be after start time", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        } catch (Exception e) {
            Toast.makeText(this, "Invalid time format", Toast.LENGTH_SHORT).show();
            return;
        }

        // TODO: Validate date format
        // TODO: Validate time format

        // TODO: Create FoodListing object
        FoodListing foodListing = new FoodListing();
        foodListing.setFoodName(foodName);
        foodListing.setQuantity(quantity);
        foodListing.setCategory(category);
        foodListing.setExpiryDate(expiryDate);
        foodListing.setPickupTime(pickupTimeWindow);
        foodListing.setStartTime(startTime); // Store separately if needed
        foodListing.setEndTime(endTime);     // Store separately if needed
        foodListing.setAdditionalDetails(additionalDetails);
        foodListing.setStatus("Available");
        foodListing.setCreatedAt(System.currentTimeMillis());

        // TODO: Upload image to server if exists
        // TODO: Save to database/API

        // Show success message
        Toast.makeText(this, "Food listing published successfully!", Toast.LENGTH_SHORT).show();

        // Return to previous activity with result
        Intent returnIntent = new Intent();
        setResult(RESULT_OK, returnIntent);
        finish();
    }

    private boolean hasUnsavedChanges() {
        return !etFoodName.getText().toString().isEmpty() ||
                !etQuantity.getText().toString().isEmpty() ||
                !actvCategory.getText().toString().equals("Vegetables") ||
                !etExpiryDate.getText().toString().isEmpty() ||
                !etStartTime.getText().toString().isEmpty() ||
                !etEndTime.getText().toString().isEmpty() ||
                !etAdditionalDetails.getText().toString().isEmpty() ||
                ivFoodImage.getVisibility() == View.VISIBLE;
    }

    private void showUnsavedChangesDialog() {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("Unsaved Changes");
        builder.setMessage("You have unsaved changes. Are you sure you want to discard them?");
        builder.setPositiveButton("Discard", (dialog, which) -> finish());
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.create().show();
    }

//    @Override
//    public void onBackPressed() {
//        if (hasUnsavedChanges()) {
//            showUnsavedChangesDialog();
//        } else {
//            super.onBackPressed();
//        }
//    }

    // FoodListing model class
    public static class FoodListing {
        private String foodName;
        private String quantity;
        private String category;
        private String expiryDate;
        private String pickupTime;
        private String startTime;
        private String endTime;
        private String additionalDetails;
        private String status;
        private long createdAt;
        private String imageUrl;

        // Getters and setters
        public String getFoodName() { return foodName; }
        public void setFoodName(String foodName) { this.foodName = foodName; }

        public String getQuantity() { return quantity; }
        public void setQuantity(String quantity) { this.quantity = quantity; }

        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }

        public String getExpiryDate() { return expiryDate; }
        public void setExpiryDate(String expiryDate) { this.expiryDate = expiryDate; }

        public String getPickupTime() { return pickupTime; }
        public void setPickupTime(String pickupTime) { this.pickupTime = pickupTime; }

        public String getStartTime() { return startTime; }
        public void setStartTime(String startTime) { this.startTime = startTime; }

        public String getEndTime() { return endTime; }
        public void setEndTime(String endTime) { this.endTime = endTime;}

        public String getAdditionalDetails() { return additionalDetails; }
        public void setAdditionalDetails(String additionalDetails) { this.additionalDetails = additionalDetails; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public long getCreatedAt() { return createdAt; }
        public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

        public String getImageUrl() { return imageUrl; }
        public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    }
}