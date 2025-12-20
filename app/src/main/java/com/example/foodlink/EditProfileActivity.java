package com.example.foodlink;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import android.util.Base64;
import java.io.ByteArrayOutputStream;

public class EditProfileActivity extends AppCompatActivity {

    // UI Components
    private ImageView imageViewProfile;
    private MaterialButton btnUploadPhoto;
    private TextInputEditText etBusinessName;
    private TextInputEditText etEmail;
    private TextInputEditText etPhone;
    private TextInputEditText etAddress;
    private MaterialButton btnCancel;
    private MaterialButton btnSaveChanges;

    // Firebase
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    // Constants
    private static final int PICK_IMAGE_REQUEST = 1;
    private static final String TAG = "EditProfileActivity";

    // User data
    private String originalBusinessName = "";
    private String originalEmail = "";
    private String originalPhone = "";
    private String originalAddress = "";

    private String imageBase64 = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        try {
            initViews();
            loadUserDataFromFirestore();
            setupListeners();
            setupBackPressHandler();
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate: " + e.getMessage(), e);
            Toast.makeText(this, "Error loading edit profile", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void initViews() {
        try {
            View backButton = findViewById(R.id.btnBack);
            if (backButton != null) {
                backButton.setOnClickListener(v -> navigateToProfile());
            }

            View cardView = findViewById(R.id.ivProfileImage);
            if (cardView != null) {
                imageViewProfile = cardView.findViewById(R.id.imageViewProfile);
            }

            if (imageViewProfile == null) {
                imageViewProfile = findViewById(R.id.imageViewProfile);
            }

            btnUploadPhoto = findViewById(R.id.btnUploadPhoto);
            etBusinessName = findViewById(R.id.etBusinessName);
            etEmail = findViewById(R.id.etEmail);
            etPhone = findViewById(R.id.etPhone);
            etAddress = findViewById(R.id.etAddress);
            btnCancel = findViewById(R.id.btnCancel);
            btnSaveChanges = findViewById(R.id.btnSaveChanges);

        } catch (Exception e) {
            Log.e(TAG, "Error in initViews: " + e.getMessage(), e);
            throw new RuntimeException("Failed to initialize views", e);
        }
    }

    private void loadUserDataFromFirestore() {
        String userId = currentUser.getUid();

        db.collection("users").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String businessName = documentSnapshot.getString("business_name");
                        if (businessName == null || businessName.isEmpty()) {
                            businessName = documentSnapshot.getString("full_name");
                        }
                        if (businessName == null || businessName.isEmpty()) {
                            businessName = documentSnapshot.getString("name");
                        }

                        // ALWAYS show current Firebase Auth email in the field
                        String currentAuthEmail = currentUser.getEmail();
                        originalEmail = currentAuthEmail != null ? currentAuthEmail : "";

                        // But load the stored email to detect changes
                        String storedEmail = documentSnapshot.getString("email");

                        String phone = documentSnapshot.getString("phone");
                        String address = documentSnapshot.getString("address");

                        // Store original values
                        originalBusinessName = businessName != null ? businessName : "";
                        originalPhone = phone != null ? phone : "";
                        originalAddress = address != null ? address : "";

                        // Populate UI
                        etBusinessName.setText(originalBusinessName);
                        etEmail.setText(originalEmail); // Show Auth email, not stored email
                        etPhone.setText(originalPhone);
                        etAddress.setText(originalAddress);

                        String profileImageBase64 = documentSnapshot.getString("profile_image_base64");
                        boolean hasProfileImage = Boolean.TRUE.equals(documentSnapshot.getBoolean("has_profile_image"));

                        // Store original values
                        originalBusinessName = businessName != null ? businessName : "";
                        originalPhone = phone != null ? phone : "";
                        originalAddress = address != null ? address : "";

                        // Populate UI
                        etBusinessName.setText(originalBusinessName);
                        etEmail.setText(originalEmail); // Show Auth email, not stored email
                        etPhone.setText(originalPhone);
                        etAddress.setText(originalAddress);

                        // ===== NEW CODE: DISPLAY PROFILE IMAGE IF EXISTS =====
                        if (hasProfileImage && profileImageBase64 != null && !profileImageBase64.isEmpty()) {
                            // Store the loaded image in instance variable
                            imageBase64 = profileImageBase64;

                            // Convert Base64 to Bitmap and display
                            Bitmap profileBitmap = convertBase64ToBitmap(profileImageBase64);
                            if (profileBitmap != null && imageViewProfile != null) {
                                imageViewProfile.setImageBitmap(profileBitmap);
                                Toast.makeText(this, "Profile image loaded", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            // Set default profile image
                            if (imageViewProfile != null) {
                                imageViewProfile.setImageResource(R.drawable.ic_profile);
                            }
                        }

                    } else {
                        originalEmail = currentUser.getEmail() != null ? currentUser.getEmail() : "";
                        etEmail.setText(originalEmail);
                        Toast.makeText(this, "Profile not found, creating new one", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading profile: " + e.getMessage());
                    Toast.makeText(this, "Error loading profile", Toast.LENGTH_SHORT).show();
                });
    }

    private Bitmap convertBase64ToBitmap(String base64String) {
        try {
            // Decode Base64 to byte array
            byte[] decodedBytes = Base64.decode(base64String, Base64.DEFAULT);

            // Convert byte array to Bitmap
            android.graphics.BitmapFactory.Options options = new android.graphics.BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;

            return android.graphics.BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length, options);

        } catch (Exception e) {
            Log.e(TAG, "Error converting Base64 to Bitmap: " + e.getMessage());
            return null;
        }
    }
    private void setupListeners() {
        try {
            if (btnUploadPhoto != null) {
                btnUploadPhoto.setOnClickListener(v -> openImageChooser());
            }

            if (btnCancel != null) {
                btnCancel.setOnClickListener(v -> {
                    if (hasUnsavedChanges()) {
                        showUnsavedChangesDialog();
                    } else {
                        finish();
                    }
                });
            }

            if (btnSaveChanges != null) {
                btnSaveChanges.setOnClickListener(v -> saveProfileChanges());
            }

            setupFormValidation();

        } catch (Exception e) {
            Log.e(TAG, "Error in setupListeners: " + e.getMessage(), e);
            Toast.makeText(this, "Error setting up listeners", Toast.LENGTH_SHORT).show();
        }
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
        validateForm();
    }

    private void validateForm() {
        String businessName = etBusinessName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String address = etAddress.getText().toString().trim();

        boolean isValid = !businessName.isEmpty() &&
                !email.isEmpty() &&
                !phone.isEmpty() &&
                !address.isEmpty() &&
                android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();

        if (btnSaveChanges != null) {
            btnSaveChanges.setEnabled(isValid);
            btnSaveChanges.setAlpha(isValid ? 1.0f : 0.5f);
        }
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
                if (imageViewProfile != null) {
                    imageViewProfile.setImageBitmap(bitmap);
                    imageBase64 = convertBitmapToBase64(bitmap);

                    if (imageBase64 != null && imageBase64.length() > 1000000) {
                        Toast.makeText(this, "Image is too large. Please select a smaller image.", Toast.LENGTH_LONG).show();
                        imageBase64 = null;
                        imageViewProfile.setImageResource(R.drawable.ic_profile);
                    } else if (imageBase64 != null) {
                        Toast.makeText(this, "Profile image updated", Toast.LENGTH_SHORT).show();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private String convertBitmapToBase64(Bitmap bitmap) {
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            boolean isCompressed = bitmap.compress(Bitmap.CompressFormat.JPEG, 70, byteArrayOutputStream);

            if (!isCompressed) {
                Log.e(TAG, "Failed to compress bitmap");
                return null;
            }

            byte[] byteArray = byteArrayOutputStream.toByteArray();
            String base64 = Base64.encodeToString(byteArray, Base64.DEFAULT);
            byteArrayOutputStream.close();
            return base64;

        } catch (Exception e) {
            Log.e(TAG, "Error converting bitmap to Base64: " + e.getMessage());
            return null;
        }
    }

    // ==================== MAIN PROFILE SAVE LOGIC ====================
    private void saveProfileChanges() {
        String businessName = etBusinessName.getText().toString().trim();
        final String newEmail = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String address = etAddress.getText().toString().trim();

        // Validate
        if (businessName.isEmpty() || newEmail.isEmpty() || phone.isEmpty() || address.isEmpty()) {
            Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(newEmail).matches()) {
            Toast.makeText(this, "Please enter a valid email address", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show loading
        if (btnSaveChanges != null) {
            btnSaveChanges.setEnabled(false);
            btnSaveChanges.setText("Saving...");
        }

        // Check if email changed
        boolean emailChanged = !newEmail.equals(currentUser.getEmail());

        // ALWAYS UPDATE FIRESTORE FIRST with the new email
        updateFirestoreEmailImmediately(newEmail, businessName, phone, address);

        if (emailChanged) {
            // Email changed - ask for password to update Firebase Auth
            showPasswordDialogForAuthUpdate(newEmail);
        } else {
            // No email change in Auth, just complete
            Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK);
            navigateToProfile();
        }
    }

    private void updateFirestoreEmailImmediately(String newEmail, String businessName,
                                                 String phone, String address) {
        Map<String, Object> userData = new HashMap<>();
        userData.put("business_name", businessName);
        userData.put("full_name", businessName);
        userData.put("email", newEmail); // Update Firestore immediately
        userData.put("phone", phone);
        userData.put("address", address);
        userData.put("updated_at", System.currentTimeMillis());

        if (imageBase64 != null && !imageBase64.isEmpty()) {
            userData.put("profile_image_base64", imageBase64);
            userData.put("has_profile_image", true);
        }

        db.collection("users").document(currentUser.getUid())
                .set(userData, com.google.firebase.firestore.SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Firestore email updated to: " + newEmail);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to update Firestore: " + e.getMessage());
                });
    }

    private void showPasswordDialogForAuthUpdate(final String newEmail) {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("Update Login Email");
        builder.setMessage("To update your login email, please enter your current password:");

        final TextInputEditText input = new TextInputEditText(this);
        input.setInputType(android.text.InputType.TYPE_CLASS_TEXT |
                android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
        input.setHint("Current Password");

        TextInputLayout inputLayout = new TextInputLayout(this);
        inputLayout.addView(input);
        builder.setView(inputLayout);

        builder.setPositiveButton("Update Login", (dialog, which) -> {
            String password = input.getText().toString().trim();
            if (password.isEmpty()) {
                Toast.makeText(this, "Password cannot be empty", Toast.LENGTH_SHORT).show();
                showPasswordDialogForAuthUpdate(newEmail);
            } else {
                updateFirebaseAuthEmail(newEmail, password);
            }
        });

        builder.setNegativeButton("Skip Login Update", (dialog, which) -> {
            // User chooses not to update Firebase Auth
            Toast.makeText(this,
                    "Profile updated. Login email remains: " + currentUser.getEmail(),
                    Toast.LENGTH_LONG).show();
            setResult(RESULT_OK);
            navigateToProfile();
        });

        builder.setNeutralButton("Cancel", (dialog, which) -> {
            resetSaveButton();
        });

        builder.create().show();
    }

    private void updateFirebaseAuthEmail(String newEmail, String password) {
        // 1. Reauthenticate first
        AuthCredential credential = EmailAuthProvider.getCredential(currentUser.getEmail(), password);

        currentUser.reauthenticate(credential)
                .addOnCompleteListener(reauthTask -> {
                    if (reauthTask.isSuccessful()) {
                        // 2. Send verification email using verifyBeforeUpdateEmail
                        currentUser.verifyBeforeUpdateEmail(newEmail)
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        // Success! Verification email sent
                                        Toast.makeText(this,
                                                "Verification email sent to " + newEmail +
                                                        ". Please check your inbox.",
                                                Toast.LENGTH_LONG).show();

                                        // Update Firestore to mark verification pending
                                        updateFirestoreForPendingVerification(newEmail);

                                        setResult(RESULT_OK);
                                        navigateToProfile();
                                    } else {
                                        // Failed to send verification
                                        String error = getErrorMessage(task.getException());
                                        Toast.makeText(this,
                                                "Could not update login email: " + error,
                                                Toast.LENGTH_LONG).show();
                                        resetSaveButton();
                                    }
                                });
                    } else {
                        // Reauthentication failed
                        Toast.makeText(this,
                                "Incorrect password. Please try again.",
                                Toast.LENGTH_SHORT).show();
                        showPasswordDialogForAuthUpdate(newEmail);
                    }
                });
    }

    private void updateFirestoreForPendingVerification(String pendingEmail) {
        Map<String, Object> updateData = new HashMap<>();
        updateData.put("auth_email_pending", pendingEmail);
        updateData.put("auth_email_status", "verification_sent");
        updateData.put("auth_email_sent_at", System.currentTimeMillis());

        db.collection("users").document(currentUser.getUid())
                .update(updateData)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Firestore updated for pending email verification");
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to update Firestore for pending verification: " + e.getMessage());
                });
    }

    private String getErrorMessage(Exception exception) {
        if (exception == null) return "Unknown error";
        String errorMessage = exception.getMessage();
        if (errorMessage == null) return "Unknown error";

        if (errorMessage.contains("ERROR_INVALID_EMAIL")) return "Invalid email address";
        if (errorMessage.contains("ERROR_EMAIL_ALREADY_IN_USE")) return "Email already in use";
        if (errorMessage.contains("ERROR_REQUIRES_RECENT_LOGIN")) return "Session expired - please login again";
        if (errorMessage.contains("ERROR_WRONG_PASSWORD")) return "Incorrect password";
        if (errorMessage.contains("ERROR_USER_NOT_FOUND")) return "Account not found";
        if (errorMessage.contains("ERROR_USER_DISABLED")) return "Account disabled";
        if (errorMessage.contains("ERROR_OPERATION_NOT_ALLOWED")) return "Email changes not allowed";
        if (errorMessage.contains("ERROR_TOO_MANY_REQUESTS")) return "Too many attempts - try later";

        return errorMessage;
    }

    private void resetSaveButton() {
        if (btnSaveChanges != null) {
            btnSaveChanges.setEnabled(true);
            btnSaveChanges.setText("Save Changes");
        }
    }

    private boolean hasUnsavedChanges() {
        String currentBusinessName = etBusinessName.getText().toString().trim();
        String currentEmail = etEmail.getText().toString().trim();
        String currentPhone = etPhone.getText().toString().trim();
        String currentAddress = etAddress.getText().toString().trim();

        return !currentBusinessName.equals(originalBusinessName) ||
                !currentEmail.equals(originalEmail) ||
                !currentPhone.equals(originalPhone) ||
                !currentAddress.equals(originalAddress);
    }

    private void showUnsavedChangesDialog() {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("Unsaved Changes");
        builder.setMessage("You have unsaved changes. Are you sure you want to discard them?");
        builder.setPositiveButton("Discard", (dialog, which) -> finish());
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.create().show();
    }

    private void setupBackPressHandler() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (hasUnsavedChanges()) {
                    showUnsavedChangesDialog();
                } else {
                    navigateToProfile();
                    overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                }
            }
        });
    }

    private void navigateToProfile() {
        Intent intent = new Intent(this, ProfileActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        finish();
    }
}