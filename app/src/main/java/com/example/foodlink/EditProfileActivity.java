package com.example.foodlink;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.io.IOException;

public class EditProfileActivity extends AppCompatActivity {

    // UI Components
    private ImageView btnBack;
    private ImageView imageViewProfile;
    private MaterialButton btnUploadPhoto;
    private TextInputEditText etBusinessName;
    private TextInputEditText etEmail;
    private TextInputEditText etPhone;
    private TextInputEditText etAddress;
    private TextView tvAccountType;
    private SwitchCompat switchEmailNotifications;
    private SwitchCompat switchSmsAlerts;
    private SwitchCompat switchPublicProfile;
    private MaterialButton btnChangePassword;
    private MaterialButton btnCancel;
    private MaterialButton btnSaveChanges;

    // Constants
    private static final int PICK_IMAGE_REQUEST = 1;

    // User data model (you can replace with your actual User class)
    private UserProfile userProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile); // Make sure your XML file is named activity_edit_profile.xml

        // Initialize UI Components
        initViews();

        // Load user data (you would replace this with actual data loading)
        loadUserData();

        // Setup listeners
        setupListeners();

        // Setup toolbar
        setupToolbar();
    }

    private void initViews() {
        // Toolbar and navigation
        btnBack = findViewById(R.id.btnBack);

        // Profile section
        imageViewProfile = findViewById(R.id.imageViewProfile);
        btnUploadPhoto = findViewById(R.id.btnUploadPhoto);

        // Organization information
        etBusinessName = findViewById(R.id.etBusinessName);
        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);
        etAddress = findViewById(R.id.etAddress);

        // Account type
        tvAccountType = findViewById(R.id.tvAccountType);

        // Additional settings
        switchEmailNotifications = findViewById(R.id.switchEmailNotifications);
        switchSmsAlerts = findViewById(R.id.switchSmsAlerts);
        switchPublicProfile = findViewById(R.id.switchPublicProfile);

        // Security
        btnChangePassword = findViewById(R.id.btnChangePassword);

        // Action buttons
        btnCancel = findViewById(R.id.btnCancel);
        btnSaveChanges = findViewById(R.id.btnSaveChanges);
    }

    private void loadUserData() {
        // TODO: Load actual user data from SharedPreferences, Database, or API
        // For now, using dummy data
        userProfile = new UserProfile();
        userProfile.setBusinessName("Green Leaf Restaurant");
        userProfile.setEmail("1@1");
        userProfile.setPhone("+1 234 567 8900");
        userProfile.setAddress("123 Eco Street, Sustainable City");
        userProfile.setAccountType("Food Seller");
        userProfile.setEmailNotifications(true);
        userProfile.setSmsAlerts(true);
        userProfile.setPublicProfile(true);

        // Populate UI with user data
        populateUserData();
    }

    private void populateUserData() {
        if (userProfile != null) {
            etBusinessName.setText(userProfile.getBusinessName());
            etEmail.setText(userProfile.getEmail());
            etPhone.setText(userProfile.getPhone());
            etAddress.setText(userProfile.getAddress());
            tvAccountType.setText(userProfile.getAccountType());
            switchEmailNotifications.setChecked(userProfile.isEmailNotifications());
            switchSmsAlerts.setChecked(userProfile.isSmsAlerts());
            switchPublicProfile.setChecked(userProfile.isPublicProfile());
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
        // Back button
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        // Upload photo button
        btnUploadPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openImageChooser();
            }
        });

        // Change password button
        btnChangePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to Change Password activity
                Intent intent = new Intent(EditProfileActivity.this, ChangePasswordActivity.class);
                startActivity(intent);
            }
        });

        // Cancel button
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Check if there are unsaved changes
                if (hasUnsavedChanges()) {
                    showUnsavedChangesDialog();
                } else {
                    finish();
                }
            }
        });

        // Save changes button
        btnSaveChanges.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveProfileChanges();
            }
        });

        // Form validation - Enable/Disable save button based on input validity
        setupFormValidation();
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

        etBusinessName.addTextChangedListener(textWatcher);
        etEmail.addTextChangedListener(textWatcher);
        etPhone.addTextChangedListener(textWatcher);
        etAddress.addTextChangedListener(textWatcher);

        // Also validate when switches change
        CompoundButton.OnCheckedChangeListener switchListener = new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                validateForm();
            }
        };

        switchEmailNotifications.setOnCheckedChangeListener(switchListener);
        switchSmsAlerts.setOnCheckedChangeListener(switchListener);
        switchPublicProfile.setOnCheckedChangeListener(switchListener);
    }

    private void validateForm() {
        boolean isValid = true;

        // Check required fields
        if (etBusinessName.getText().toString().trim().isEmpty()) {
            isValid = false;
        }

        if (etEmail.getText().toString().trim().isEmpty()) {
            isValid = false;
        } else if (!isValidEmail(etEmail.getText().toString().trim())) {
            isValid = false;
        }

        if (etPhone.getText().toString().trim().isEmpty()) {
            isValid = false;
        }

        if (etAddress.getText().toString().trim().isEmpty()) {
            isValid = false;
        }

//        // Enable/disable save button based on validation
//        btnSaveChanges.setEnabled(isValid);
//        btnSaveChanges.setAlpha(isValid ? 1.0f : 0.5f);
    }

    private boolean isValidEmail(String email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private void openImageChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Profile Image"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                imageViewProfile.setImageBitmap(bitmap);

                // TODO: Upload image to server and save URL
                Toast.makeText(this, "Profile image updated", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void saveProfileChanges() {
        // Get updated values from UI
        String businessName = etBusinessName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String address = etAddress.getText().toString().trim();
        boolean emailNotifications = switchEmailNotifications.isChecked();
        boolean smsAlerts = switchSmsAlerts.isChecked();
        boolean publicProfile = switchPublicProfile.isChecked();

        // Validate again (just in case)
        if (businessName.isEmpty() || email.isEmpty() || phone.isEmpty() || address.isEmpty()) {
            Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!isValidEmail(email)) {
            Toast.makeText(this, "Please enter a valid email address", Toast.LENGTH_SHORT).show();
            return;
        }

        // Update user profile object
        userProfile.setBusinessName(businessName);
        userProfile.setEmail(email);
        userProfile.setPhone(phone);
        userProfile.setAddress(address);
        userProfile.setEmailNotifications(emailNotifications);
        userProfile.setSmsAlerts(smsAlerts);
        userProfile.setPublicProfile(publicProfile);

        // TODO: Save to SharedPreferences, Database, or API
        // For now, just show a success message
        Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show();

        // Return to previous activity with result
        Intent returnIntent = new Intent();
        setResult(RESULT_OK, returnIntent);
        finish();
    }

    private boolean hasUnsavedChanges() {
        if (userProfile == null) return false;

        return !etBusinessName.getText().toString().equals(userProfile.getBusinessName()) ||
                !etEmail.getText().toString().equals(userProfile.getEmail()) ||
                !etPhone.getText().toString().equals(userProfile.getPhone()) ||
                !etAddress.getText().toString().equals(userProfile.getAddress()) ||
                switchEmailNotifications.isChecked() != userProfile.isEmailNotifications() ||
                switchSmsAlerts.isChecked() != userProfile.isSmsAlerts() ||
                switchPublicProfile.isChecked() != userProfile.isPublicProfile();
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

    // Simple UserProfile class (replace with your actual model)
    public static class UserProfile {
        private String businessName;
        private String email;
        private String phone;
        private String address;
        private String accountType;
        private boolean emailNotifications;
        private boolean smsAlerts;
        private boolean publicProfile;

        // Getters and setters
        public String getBusinessName() { return businessName; }
        public void setBusinessName(String businessName) { this.businessName = businessName; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }

        public String getAddress() { return address; }
        public void setAddress(String address) { this.address = address; }

        public String getAccountType() { return accountType; }
        public void setAccountType(String accountType) { this.accountType = accountType; }

        public boolean isEmailNotifications() { return emailNotifications; }
        public void setEmailNotifications(boolean emailNotifications) { this.emailNotifications = emailNotifications; }

        public boolean isSmsAlerts() { return smsAlerts; }
        public void setSmsAlerts(boolean smsAlerts) { this.smsAlerts = smsAlerts; }

        public boolean isPublicProfile() { return publicProfile; }
        public void setPublicProfile(boolean publicProfile) { this.publicProfile = publicProfile; }
    }
}