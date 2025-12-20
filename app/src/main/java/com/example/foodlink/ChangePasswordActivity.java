package com.example.foodlink;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class ChangePasswordActivity extends AppCompatActivity {

    private TextInputEditText etCurrentPassword;
    private TextInputEditText etNewPassword;
    private TextInputEditText etConfirmPassword;
    private MaterialButton btnSavePassword;
    private TextView tvPasswordStrength;
    private ProgressBar pbPasswordStrength;
    private TextInputLayout tilNewPassword;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        setupListeners();
        setupPasswordStrengthWatcher();
    }

    private void initViews() {
        etCurrentPassword = findViewById(R.id.etCurrentPassword);
        etNewPassword = findViewById(R.id.etNewPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnSavePassword = findViewById(R.id.btnSavePassword);
        tvPasswordStrength = findViewById(R.id.tvPasswordStrength);
        pbPasswordStrength = findViewById(R.id.pbPasswordStrength);
        tilNewPassword = findViewById(R.id.tilNewPassword);
    }

    private void setupListeners() {
        btnSavePassword.setOnClickListener(v -> changePassword());

        // Back button if you have one
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }

    private void setupPasswordStrengthWatcher() {
        etNewPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updatePasswordStrengthDisplay(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    /**
     * Updates the password strength UI in real-time
     */
    private void updatePasswordStrengthDisplay(String password) {
        if (password.isEmpty()) {
            tvPasswordStrength.setText("");
            pbPasswordStrength.setProgress(0);
            pbPasswordStrength.setVisibility(View.GONE);
            return;
        }

        pbPasswordStrength.setVisibility(View.VISIBLE);
        int strengthLevel = checkPasswordStrength(password);
        String strengthDescription = getPasswordStrengthDescription(strengthLevel);
        int progress = (strengthLevel + 1) * 20; // Convert to 0-100 scale

        pbPasswordStrength.setProgress(progress);
        tvPasswordStrength.setText("Password Strength: " + strengthDescription);

        // Set color based on strength
        int color;
        switch (strengthLevel) {
            case 0:
            case 1:
                color = getResources().getColor(R.color.password_very_weak); // Red
                break;
            case 2:
                color = getResources().getColor(R.color.password_weak); // Orange
                break;
            case 3:
                color = getResources().getColor(R.color.password_fair); // Yellow
                break;
            case 4:
                color = getResources().getColor(R.color.password_strong); // Green
                break;
            case 5:
                color = getResources().getColor(R.color.password_very_strong); // Dark Green
                break;
            default:
                color = getResources().getColor(R.color.password_very_weak);
        }
        tvPasswordStrength.setTextColor(color);
        // Note: For ProgressBar color, you might need to create a custom progress bar
        // or use a library that supports color changes
    }

    /**
     * Check password strength (same as SignUpActivity)
     */
    private int checkPasswordStrength(String password) {
        int strengthScore = 0;

        // Check password length
        if (password.length() >= 8) {
            strengthScore++;
        }

        // Check for uppercase letters
        if (Pattern.compile("[A-Z]").matcher(password).find()) {
            strengthScore++;
        }

        // Check for lowercase letters
        if (Pattern.compile("[a-z]").matcher(password).find()) {
            strengthScore++;
        }

        // Check for numbers
        if (Pattern.compile("[0-9]").matcher(password).find()) {
            strengthScore++;
        }

        // Check for special characters
        if (Pattern.compile("[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?]").matcher(password).find()) {
            strengthScore++;
        }

        // Bonus: Check for length > 12
        if (password.length() >= 12) {
            strengthScore++;
        }

        // Cap the score at 5 for consistency
        return Math.min(strengthScore, 5);
    }

    /**
     * Returns descriptive string for password strength
     */
    private String getPasswordStrengthDescription(int strengthLevel) {
        switch (strengthLevel) {
            case 0:
                return "Very Weak";
            case 1:
                return "Weak";
            case 2:
                return "Fair";
            case 3:
                return "Good";
            case 4:
                return "Strong";
            case 5:
                return "Very Strong";
            default:
                return "Very Weak";
        }
    }

    /**
     * Returns numeric rating 1-6
     */
    private int getPasswordStrengthRating(int strengthLevel) {
        return strengthLevel + 1; // 1-6
    }

    private void changePassword() {
        String currentPassword = etCurrentPassword.getText().toString();
        String newPassword = etNewPassword.getText().toString();
        String confirmPassword = etConfirmPassword.getText().toString();

        // Validate inputs
        if (currentPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            etConfirmPassword.setError("New passwords don't match");
            return;
        }

        if (newPassword.length() < 6) {
            etNewPassword.setError("Password must be at least 6 characters");
            return;
        }

        // Check if new password is same as current
        if (newPassword.equals(currentPassword)) {
            etNewPassword.setError("New password must be different from current password");
            return;
        }

        // Check password strength
        int passwordStrengthLevel = checkPasswordStrength(newPassword);
        if (passwordStrengthLevel <= 1) { // Very Weak or Weak
            Toast.makeText(this,
                    "Your password is weak. Consider adding uppercase letters, numbers, or special characters.",
                    Toast.LENGTH_LONG).show();
            // You can decide to block weak passwords or just warn
            // return; // Uncomment to block weak passwords
        }

        // Show loading
        btnSavePassword.setEnabled(false);
        btnSavePassword.setText("Updating...");

        // Re-authenticate user before changing password
        AuthCredential credential = EmailAuthProvider.getCredential(
                currentUser.getEmail(), currentPassword);

        currentUser.reauthenticate(credential)
                .addOnCompleteListener(reauthTask -> {
                    if (reauthTask.isSuccessful()) {
                        // User re-authenticated, now update password
                        updatePasswordInFirebase(newPassword, passwordStrengthLevel);
                    } else {
                        // Re-authentication failed
                        btnSavePassword.setEnabled(true);
                        btnSavePassword.setText("Save Password");
                        etCurrentPassword.setError("Current password is incorrect");
                        Toast.makeText(ChangePasswordActivity.this,
                                "Authentication failed. Please check your current password.",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updatePasswordInFirebase(String newPassword, int passwordStrengthLevel) {
        // Get password strength data
        String passwordStrengthDescription = getPasswordStrengthDescription(passwordStrengthLevel);
        int passwordStrengthRating = getPasswordStrengthRating(passwordStrengthLevel);

        // Update password in Firebase Auth
        currentUser.updatePassword(newPassword)
                .addOnCompleteListener(updateTask -> {
                    if (updateTask.isSuccessful()) {
                        // Password updated in Auth, now update Firestore with strength data
                        updatePasswordStrengthInFirestore(passwordStrengthLevel,
                                passwordStrengthDescription, passwordStrengthRating);
                    } else {
                        // Password update failed
                        btnSavePassword.setEnabled(true);
                        btnSavePassword.setText("Save Password");

                        String errorMessage = "Failed to update password";
                        if (updateTask.getException() != null) {
                            errorMessage = updateTask.getException().getMessage();
                            if (errorMessage.contains("requires recent authentication")) {
                                errorMessage = "Session expired. Please login again.";
                                // You might want to redirect to login here
                            }
                        }

                        Toast.makeText(ChangePasswordActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void updatePasswordStrengthInFirestore(int strengthLevel,
                                                   String description, int rating) {

        Map<String, Object> updates = new HashMap<>();
        updates.put("password_strength_level", strengthLevel);
        updates.put("password_strength_description", description);
        updates.put("password_strength_rating", rating);
        updates.put("password_last_updated", System.currentTimeMillis());

        db.collection("users")
                .document(currentUser.getUid())
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    // Successfully updated in Firestore
                    btnSavePassword.setEnabled(true);
                    btnSavePassword.setText("Save Password");

                    Toast.makeText(ChangePasswordActivity.this,
                            "Password updated successfully!", Toast.LENGTH_SHORT).show();

                    // Optional: Log out user everywhere for security
                    // mAuth.signOut();
                    // startActivity(new Intent(this, LoginActivity.class));

                    finish();
                })
                .addOnFailureListener(e -> {
                    // Password changed in Auth but Firestore update failed
                    btnSavePassword.setEnabled(true);
                    btnSavePassword.setText("Save Password");

                    Toast.makeText(ChangePasswordActivity.this,
                            "Password changed but failed to update security information. Please check your profile.",
                            Toast.LENGTH_LONG).show();

                    finish(); // Still finish since password was changed in Auth
                });
    }
}