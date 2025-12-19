package com.example.foodlink;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import android.util.Base64;  // For Base64 encoding

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
    private static final String TAG = "AddNewFoodListing";
    private Calendar calendar;
    private Calendar startTimeCalendar;
    private Calendar endTimeCalendar;
    private SimpleDateFormat dateFormatter;
    private SimpleDateFormat timeFormatter;

    private BottomNavigationView bottomNavigation;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private FirebaseStorage storage;
    private StorageReference storageRef;
    private Uri selectedImageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_food_listing);

        // Initialize
        calendar = Calendar.getInstance();
        startTimeCalendar = Calendar.getInstance();
        endTimeCalendar = Calendar.getInstance();
        dateFormatter = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        timeFormatter = new SimpleDateFormat("HH:mm", Locale.getDefault());
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();
        selectedImageUri = null;

        // Initialize UI Components
        initViews();

        // Setup category dropdown
        setupCategoryDropdown();

        // Setup listeners
        setupListeners();

        // Setup BottomNavigation
        setupBottomNavigation();

        // Setup toolbar
        setupToolbar();

        // Setup form validation
        setupFormValidation();

        setupBackPressHandler();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        etFoodName = findViewById(R.id.etFoodName);
        etQuantity = findViewById(R.id.etQuantity);
        actvCategory = findViewById(R.id.actvCategory);
        etExpiryDate = findViewById(R.id.etExpiryDate);
        etStartTime = findViewById(R.id.etStartTime);
        etEndTime = findViewById(R.id.etEndTime);
        etAdditionalDetails = findViewById(R.id.etAdditionalDetails);
        tilStartTime = findViewById(R.id.tilStartTime);
        tilEndTime = findViewById(R.id.tilEndTime);
        cardImageUpload = findViewById(R.id.cardImageUpload);
        llDefaultImage = findViewById(R.id.llDefaultImage);
        ivFoodImage = findViewById(R.id.ivFoodImage);
        fabRemoveImage = findViewById(R.id.fabRemoveImage);
        btnUploadImage = findViewById(R.id.btnUploadImage);
        btnCancel = findViewById(R.id.btnCancel);
        btnPublish = findViewById(R.id.btnPublish);
        bottomNavigation = findViewById(R.id.bottomNavigation);
    }

    private void setupCategoryDropdown() {
        String[] categories = {"All", "Vegetables", "Fruits", "Bakery", "Prepared Food", "Dairy", "Other"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, categories);
        actvCategory.setAdapter(adapter);
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
        btnBack.setOnClickListener(v -> navigateToDashboard());

        etExpiryDate.setOnClickListener(v -> showDatePicker());

        etStartTime.setOnClickListener(v -> showStartTimePicker());

        etEndTime.setOnClickListener(v -> showEndTimePicker());

        cardImageUpload.setOnClickListener(v -> openImageChooser());

        btnUploadImage.setOnClickListener(v -> openImageChooser());

        fabRemoveImage.setOnClickListener(v -> removeSelectedImage());

        btnCancel.setOnClickListener(v -> {
            if (hasUnsavedChanges()) {
                showUnsavedChangesDialog();
            } else {
                finish();
            }
        });

        btnPublish.setOnClickListener(v -> publishListing());
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
                    navigateToDashboard();
                    return true;
                } else if (itemId == R.id.nav_add) {
                    return true;
                } else if (itemId == R.id.nav_profile) {
                    navigateToProfile();
                    return true;
                }
                return false;
            }
        });
        bottomNavigation.setSelectedItemId(R.id.nav_add);
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

        actvCategory.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                validateForm();
            }
        });
        validateForm();
    }

    private void showStartTimePicker() {
        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        startTimeCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        startTimeCalendar.set(Calendar.MINUTE, minute);
                        etStartTime.setText(timeFormatter.format(startTimeCalendar.getTime()));

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
                true
        );
        timePickerDialog.setTitle("Select Start Time");
        timePickerDialog.show();
    }

    private void showEndTimePicker() {
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
                true
        );
        timePickerDialog.setTitle("Select End Time");
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
        if (etStartTime.getText().toString().trim().isEmpty() ||
                etEndTime.getText().toString().trim().isEmpty()) {
            isValid = false;
        }

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
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
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
            selectedImageUri = data.getData();
            Log.d(TAG, "Image URI: " + selectedImageUri);

            try {
                String mimeType = getContentResolver().getType(selectedImageUri);
                Log.d(TAG, "MIME Type: " + mimeType);

                Cursor cursor = getContentResolver().query(selectedImageUri,
                        null, null, null, null);
                if (cursor != null && cursor.moveToFirst()) {
                    int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);
                    int displayNameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);

                    if (sizeIndex != -1) {
                        long size = cursor.getLong(sizeIndex);
                        Log.d(TAG, "File size: " + (size / 1024) + "KB");
                    }
                    if (displayNameIndex != -1) {
                        String fileName = cursor.getString(displayNameIndex);
                        Log.d(TAG, "File name: " + fileName);
                    }
                    cursor.close();
                }

                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImageUri);
                Log.d(TAG, "Bitmap loaded: " + bitmap.getWidth() + "x" + bitmap.getHeight());
                Toast.makeText(this,
                        "Image: " + bitmap.getWidth() + "x" + bitmap.getHeight(),
                        Toast.LENGTH_SHORT).show();

                llDefaultImage.setVisibility(View.GONE);
                ivFoodImage.setVisibility(View.VISIBLE);
                fabRemoveImage.setVisibility(View.VISIBLE);
                ivFoodImage.setImageBitmap(bitmap);

            } catch (IOException e) {
                e.printStackTrace();
                Log.e(TAG, "Failed to load image: " + e.getMessage());
                Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == PICK_IMAGE_REQUEST) {
            Log.d(TAG, "No image selected or result not OK");
            Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show();
        }
    }

    private void uploadImageToFirebaseStorage(Uri imageUri, String listingId, OnImageUploadListener listener) {
        if (imageUri == null) {
            listener.onImageUploadComplete(null, null);
            return;
        }

        Log.d(TAG, "Uploading image for listing: " + listingId);

        try {
            String mimeType = getContentResolver().getType(imageUri);
            String fileExtension = ".jpg";

            if (mimeType != null) {
                if (mimeType.equals("image/png")) {
                    fileExtension = ".png";
                } else if (mimeType.equals("image/jpeg")) {
                    fileExtension = ".jpg";
                }
            }

            String filename = "listing_" + listingId + "_" + System.currentTimeMillis() + fileExtension;
            StorageReference imageRef = storageRef.child("food_listings/" + filename);

            InputStream stream = getContentResolver().openInputStream(imageUri);

            if (stream == null) {
                Log.e(TAG, "Failed to open input stream for image");
                listener.onImageUploadComplete(null, null);
                return;
            }

            UploadTask uploadTask = imageRef.putStream(stream);

            uploadTask.addOnSuccessListener(taskSnapshot -> {
                imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    String downloadUrl = uri.toString();
                    Log.d(TAG, "Image uploaded successfully. URL: " + downloadUrl);
                    listener.onImageUploadComplete(downloadUrl, filename);
                }).addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to get download URL: " + e.getMessage());
                    listener.onImageUploadComplete(null, null);
                });
            }).addOnFailureListener(e -> {
                Log.e(TAG, "Image upload failed: " + e.getMessage());
                listener.onImageUploadComplete(null, null);
            }).addOnProgressListener(taskSnapshot -> {
                double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                Log.d(TAG, "Upload progress: " + progress + "%");
            });

        } catch (Exception e) {
            Log.e(TAG, "Error preparing image upload: " + e.getMessage());
            listener.onImageUploadComplete(null, null);
        }
    }

    private void removeSelectedImage() {
        llDefaultImage.setVisibility(View.VISIBLE);
        ivFoodImage.setVisibility(View.GONE);
        fabRemoveImage.setVisibility(View.GONE);
        ivFoodImage.setImageBitmap(null);
        selectedImageUri = null;
    }

    private void publishListing() {
        Log.d(TAG, "=== PUBLISH LISTING STARTED ===");

        // Get values from form
        String foodName = etFoodName.getText().toString().trim();
        String quantity = etQuantity.getText().toString().trim();
        String category = actvCategory.getText().toString().trim();
        String expiryDate = etExpiryDate.getText().toString().trim();
        String startTime = etStartTime.getText().toString().trim();
        String endTime = etEndTime.getText().toString().trim();
        String additionalDetails = etAdditionalDetails.getText().toString().trim();

        // Validate required fields
        if (foodName.isEmpty() || quantity.isEmpty() || category.isEmpty() ||
                expiryDate.isEmpty() || startTime.isEmpty() || endTime.isEmpty()) {
            Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate time order
        try {
            String formattedStartTime = startTime;
            String formattedEndTime = endTime;

            if (startTime.length() == 4 && !startTime.contains(":")) {
                formattedStartTime = startTime.substring(0, 2) + ":" + startTime.substring(2);
            }
            if (endTime.length() == 4 && !endTime.contains(":")) {
                formattedEndTime = endTime.substring(0, 2) + ":" + endTime.substring(2);
            }

            String[] startParts = formattedStartTime.split(":");
            String[] endParts = formattedEndTime.split(":");

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

        // Check user authentication
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show loading dialog
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Publishing listing...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        // Step 1: Convert image to Base64 (if selected)
        String imageBase64 = null;
        if (selectedImageUri != null) {
            try {
                InputStream inputStream = getContentResolver().openInputStream(selectedImageUri);
                byte[] bytes = new byte[inputStream.available()];
                inputStream.read(bytes);
                inputStream.close();

                // Encode to Base64 string
                imageBase64 = Base64.encodeToString(bytes, Base64.DEFAULT);

                // Check size (Firestore limit is 1MB per field)
                if (imageBase64.length() > 1000000) { // ~0.75MB original image
                    Toast.makeText(this, "Image is too large for Firestore. Please use a smaller image.", Toast.LENGTH_LONG).show();
                    progressDialog.dismiss();
                    return;
                }
            } catch (Exception e) {
                Log.e(TAG, "Error converting image to Base64: " + e.getMessage());
                // Continue without the image
            }
        }

        // Generate listing ID
        String listingId = "FDL" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        // Call method to save everything (including Base64 string) to Firestore
        saveFoodListingToFirestore(progressDialog, listingId, imageBase64, currentUser);
    }

    private void saveFoodListingToFirestore(ProgressDialog progressDialog, String listingId,
                                            String imageBase64, FirebaseUser currentUser) {
        // Get user data from Firestore
        db.collection("users")
                .document(currentUser.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    String sellerName = "";

                    if (documentSnapshot.exists()) {
                        String userType = documentSnapshot.getString("user_type");

                        if ("seller".equals(userType)) {
                            sellerName = documentSnapshot.getString("business_name");
                        } else if ("charity".equals(userType)) {
                            sellerName = documentSnapshot.getString("charity_name");
                        }

                        if (sellerName == null || sellerName.isEmpty()) {
                            sellerName = documentSnapshot.getString("full_name");
                        }
                    }

                    if (sellerName == null || sellerName.isEmpty()) {
                        String email = currentUser.getEmail();
                        sellerName = email != null ? email.split("@")[0] : "Seller";
                        Log.w(TAG, "Using email fallback for seller name");
                    }

                    // Create FoodListing object
                    FoodListing foodListing = new FoodListing();
                    foodListing.setFoodName(etFoodName.getText().toString().trim());
                    foodListing.setQuantity(etQuantity.getText().toString().trim());
                    foodListing.setCategory(actvCategory.getText().toString().trim());
                    foodListing.setExpiryDate(etExpiryDate.getText().toString().trim());
                    foodListing.setStartTime(etStartTime.getText().toString().trim());
                    foodListing.setEndTime(etEndTime.getText().toString().trim());
                    foodListing.setAdditionalDetails(etAdditionalDetails.getText().toString().trim());
                    foodListing.setStatus("available");

                    // Set user info
                    foodListing.setSellerId(currentUser.getUid());
                    foodListing.setSellerEmail(currentUser.getEmail());
                    foodListing.setSellerName(sellerName);

                    // Set Base64 image string if available
                    if (imageBase64 != null) {
                        foodListing.setImageBase64(imageBase64);  // You need to add this method to FoodListing class
                    }

                    // Set other fields
                    foodListing.setListingId(listingId);
                    foodListing.setPickupDate(new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Calendar.getInstance().getTime()));
                    foodListing.setPickUpBy("");
                    foodListing.setPickupCharity("");
                    foodListing.setReservedBy("");
                    foodListing.setCreatedAt(System.currentTimeMillis());

                    // Save to Firestore
                    Map<String, Object> listingData = foodListing.toFirestoreMap();

                    db.collection("food_listings")
                            .add(listingData)
                            .addOnSuccessListener(documentReference -> {
                                progressDialog.dismiss();
                                Log.d(TAG, "Listing saved successfully with ID: " + documentReference.getId());
                                Toast.makeText(AddNewFoodListingActivity.this,
                                        "Food listing published successfully!\nID: " + listingId,
                                        Toast.LENGTH_LONG).show();

                                // Navigate to dashboard
                                Intent intent = new Intent(AddNewFoodListingActivity.this, SellerDashboardActivity.class);
                                startActivity(intent);
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                progressDialog.dismiss();
                                Log.e(TAG, "Error saving listing: " + e.getMessage());
                                Toast.makeText(AddNewFoodListingActivity.this,
                                        "Failed to save listing: " + e.getMessage(),
                                        Toast.LENGTH_LONG).show();
                            });
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Log.e(TAG, "Error fetching user data: " + e.getMessage());
                    Toast.makeText(AddNewFoodListingActivity.this,
                            "Failed to load user profile: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
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
        builder.setPositiveButton("Discard", (dialog, which) -> navigateToDashboard());
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.create().show();
    }

    private void navigateToDashboard() {
        Toast.makeText(this, "Navigate to Dashboard", Toast.LENGTH_SHORT).show();
        bottomNavigation.setSelectedItemId(R.id.nav_add);
        Intent intent = new Intent(this, SellerDashboardActivity.class);
        startActivity(intent);
        finish();
    }

    private void navigateToProfile() {
        Toast.makeText(this, "Navigate to Profile", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, ProfileActivity.class);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        startActivity(intent);
        finish();
    }

    private void setupBackPressHandler() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (hasUnsavedChanges()) {
                    showUnsavedChangesDialog();
                } else {
                    navigateToDashboard();
                    overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                }
            }
        });
    }

    // FoodListing model class
    public static class FoodListing {
        private String foodName;
        private String quantity;
        private String category;
        private String expiryDate;
        private String startTime;
        private String endTime;
        private String additionalDetails;
        private String status = "available";
        private String listingId;
        private String sellerId;
        private String sellerName;
        private String sellerEmail;
        private String pickupDate;
        private String pickUpBy;
        private String pickupCharity;
        private String reservedBy;
        private long createdAt;
        private String imageUrl;

        private String imageBase64;


        public String getFoodName() { return foodName; }
        public void setFoodName(String foodName) { this.foodName = foodName; }

        public String getQuantity() { return quantity; }
        public void setQuantity(String quantity) { this.quantity = quantity; }

        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }

        public String getExpiryDate() { return expiryDate; }
        public void setExpiryDate(String expiryDate) { this.expiryDate = expiryDate; }

        public String getStartTime() { return startTime; }
        public void setStartTime(String startTime) {
            if (startTime != null && startTime.length() == 4) {
                this.startTime = startTime.substring(0, 2) + ":" + startTime.substring(2);
            } else {
                this.startTime = startTime;
            }
        }

        public String getEndTime() { return endTime; }
        public void setEndTime(String endTime) {
            if (endTime != null && !endTime.contains(":")) {
                if (endTime.length() == 4) {
                    this.endTime = endTime.substring(0, 2) + ":" + endTime.substring(2);
                } else {
                    this.endTime = endTime;
                }
            } else {
                this.endTime = endTime;
            }
        }

        public String getAdditionalDetails() { return additionalDetails; }
        public void setAdditionalDetails(String additionalDetails) { this.additionalDetails = additionalDetails; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public String getListingId() { return listingId; }
        public void setListingId(String listingId) { this.listingId = listingId; }

        public String getSellerId() { return sellerId; }
        public void setSellerId(String sellerId) { this.sellerId = sellerId; }

        public String getSellerName() { return sellerName; }
        public void setSellerName(String sellerName) { this.sellerName = sellerName; }

        public String getSellerEmail() { return sellerEmail; }
        public void setSellerEmail(String sellerEmail) { this.sellerEmail = sellerEmail; }

        public String getPickupDate() { return pickupDate; }
        public void setPickupDate(String pickupDate) { this.pickupDate = pickupDate; }

        public String getPickUpBy() { return pickUpBy; }
        public void setPickUpBy(String pickUpBy) { this.pickUpBy = pickUpBy; }

        public String getPickupCharity() { return pickupCharity; }
        public void setPickupCharity(String pickupCharity) { this.pickupCharity = pickupCharity; }

        public String getReservedBy() { return reservedBy; }
        public void setReservedBy(String reservedBy) { this.reservedBy = reservedBy; }

        public long getCreatedAt() { return createdAt; }
        public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

        public String getImageUrl() { return imageUrl; }
        public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

        public String getImageBase64() { return imageBase64; }
        public void setImageBase64(String imageBase64) { this.imageBase64 = imageBase64; }

        // Update toFirestoreMap() method to include Base64
        public Map<String, Object> toFirestoreMap() {
            Map<String, Object> map = new HashMap<>();
            map.put("food_name", foodName);
            map.put("quantity", quantity);
            map.put("category", category);
            map.put("expiry_date", expiryDate);
            map.put("start_time", startTime);
            map.put("end_time", endTime);
            map.put("description", additionalDetails);
            map.put("status", status);
            map.put("listing_id", listingId);
            map.put("seller_id", sellerId);
            map.put("seller_name", sellerName);
            map.put("seller_email", sellerEmail);
            map.put("pickup_date", pickupDate);
            map.put("pick_up_by", pickUpBy);
            map.put("pickup_charity", pickupCharity);
            map.put("reserved_by", reservedBy);
            map.put("created_at", createdAt);

            // Store Base64 string if available
            if (imageBase64 != null && !imageBase64.isEmpty()) {
                map.put("image_base64", imageBase64);
            }

            // You can keep imageUrl for future if you switch to URLs
            if (imageUrl != null && !imageUrl.isEmpty()) {
                map.put("image_url", imageUrl);
            }

            return map;
        }
    }

    // Interface for image upload callback
    interface OnImageUploadListener {
        void onImageUploadComplete(String downloadUrl, String filename);
    }
}