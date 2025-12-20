package com.example.foodlink;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class PrivacySecurityActivity extends AppCompatActivity {

    private TextView tvEmailVerifiedStatus, tvPasswordStrengthStatus, tv2faStatus;
    private EditText etCurrentPassword, etNewPassword, etConfirmPassword;
    private Button btnChangePassword, btnDeleteAccount;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Hide default action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        setContentView(R.layout.activity_privacy_security);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUser = mAuth.getCurrentUser();

        initViews();
        setupClickListeners();
        loadSecurityStatus();
        setupBackPressHandler();
    }

    private void initViews() {
        // Security status views
        tvEmailVerifiedStatus = findViewById(R.id.tvEmailVerifiedStatus);
        tvPasswordStrengthStatus = findViewById(R.id.tvPasswordStrengthStatus);
//        tv2faStatus = findViewById(R.id.tv2faStatus);

        // Change password views
        etCurrentPassword = findViewById(R.id.etCurrentPassword);
        etNewPassword = findViewById(R.id.etNewPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnChangePassword = findViewById(R.id.btnChangePassword);
        btnDeleteAccount = findViewById(R.id.btnDeleteAccount);
    }

    private void setupClickListeners() {
        // Change Password button
        btnChangePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changePassword();
            }
        });

        // Delete Account button
        btnDeleteAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDeleteAccountConfirmation();
            }
        });

        // Back button
        findViewById(R.id.btnBack).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    private void loadSecurityStatus() {
        if (currentUser != null) {
            // Check email verification status
            boolean isEmailVerified = currentUser.isEmailVerified();
            tvEmailVerifiedStatus.setText(isEmailVerified ? "Email Verified" : "Email Not Verified");
            tvEmailVerifiedStatus.setTextColor(isEmailVerified ?
                    getResources().getColor(R.color.status_active) : getResources().getColor(R.color.status_expired));

            // Load password strength from Firestore
            loadPasswordStrengthFromFirestore();

            // 2FA status
//            tv2faStatus.setText("2FA Disabled");
//            tv2faStatus.setTextColor(getResources().getColor(R.color.status_expired));
        }
    }

    private void loadPasswordStrengthFromFirestore() {
        if (currentUser != null) {
            db.collection("users").document(currentUser.getUid())
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            // Check for the new field names from SignUpActivity
                            String strengthDescription = documentSnapshot.getString("password_strength_description");
                            Integer strengthRating = documentSnapshot.getLong("password_strength_rating") != null ?
                                    documentSnapshot.getLong("password_strength_rating").intValue() : null;

                            // Fallback to old field name if new ones don't exist
                            if (strengthDescription == null) {
                                strengthDescription = "Strong Password"; // Default
                            }

                            if (strengthRating == null) {
                                strengthRating = 4; // Default to "Strong"
                            }

                            // Display password strength
                            String displayText = getPasswordStrengthDisplayText(strengthDescription, strengthRating);
                            tvPasswordStrengthStatus.setText(displayText);

                            // Set color based on strength rating
                            int color = getPasswordStrengthColor(strengthRating);
                            tvPasswordStrengthStatus.setTextColor(color);
                        } else {
                            // Document doesn't exist, set default
                            tvPasswordStrengthStatus.setText("Password Strength: Unknown");
                            tvPasswordStrengthStatus.setTextColor(getResources().getColor(R.color.status_expired));
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e("Firestore", "Error loading password strength: " + e.getMessage());
                        tvPasswordStrengthStatus.setText("Password Strength: Error loading");
                        tvPasswordStrengthStatus.setTextColor(getResources().getColor(R.color.status_expired));
                    });
        }
    }

    private String getPasswordStrengthDisplayText(String description, int rating) {
        String ratingText;
        switch (rating) {
            case 1:
                ratingText = "Very Weak";
                break;
            case 2:
                ratingText = "Weak";
                break;
            case 3:
                ratingText = "Fair";
                break;
            case 4:
                ratingText = "Good";
                break;
            case 5:
                ratingText = "Strong";
                break;
            default:
                ratingText = "Unknown";
        }
        return "Password: " + ratingText;
    }

    private int getPasswordStrengthColor(int rating) {
        switch (rating) {
            case 1: // Very Weak
                return getResources().getColor(R.color.password_very_weak);
            case 2: // Weak
                return getResources().getColor(R.color.password_weak); // Red
            case 3: // Fair
                return getResources().getColor(R.color.password_fair); // Orange/Yellow
            case 4: // Good
                return getResources().getColor(R.color.password_good); // Orange/Yellow
            case 5: // Strong
                return getResources().getColor(R.color.password_strong); // Green
            default:
                return getResources().getColor(R.color.password_very_strong);
        }
    }

    private void changePassword() {
        String currentPassword = etCurrentPassword.getText().toString().trim();
        String newPassword = etNewPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        // Validation
        if (currentPassword.isEmpty()) {
            etCurrentPassword.setError("Enter current password");
            etCurrentPassword.requestFocus();
            return;
        }

        if (newPassword.isEmpty()) {
            etNewPassword.setError("Enter new password");
            etNewPassword.requestFocus();
            return;
        }

        if (newPassword.length() < 8) {
            etNewPassword.setError("Password must be at least 8 characters");
            etNewPassword.requestFocus();
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            etConfirmPassword.setError("Passwords do not match");
            etConfirmPassword.requestFocus();
            return;
        }

        // Check password strength
        int passwordStrengthLevel = checkPasswordStrength(newPassword);
        String passwordStrengthDescription = getPasswordStrengthDescription(passwordStrengthLevel);
        int passwordStrengthRating = getPasswordStrengthRating(passwordStrengthLevel);

        // Show warning for weak passwords (rating 1-2)
        if (passwordStrengthRating <= 2) {
            showWeakPasswordWarning(passwordStrengthDescription, newPassword);
            return;
        }

        // Show re-authentication dialog
        showReauthDialog(currentPassword, newPassword, passwordStrengthLevel,
                passwordStrengthDescription, passwordStrengthRating);
    }

    /**
     * Method to check password strength
     * Returns a strength level from 0 to 4
     * 0: Very Weak, 1: Weak, 2: Fair, 3: Good, 4: Strong
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

        // Cap the score at 4 for consistency
        return Math.min(strengthScore, 4);
    }

    /**
     * Returns a descriptive string for the password strength level
     */
    private String getPasswordStrengthDescription(int strengthLevel) {
        switch (strengthLevel) {
            case 0:
            case 1:
                return "very_weak";
            case 2:
                return "weak";
            case 3:
                return "fair";
            case 4:
                return "strong";
            case 5:
                return "very_strong";
            default:
                return "very_weak";
        }
    }

    /**
     * Returns a numeric rating from 1-5 for password strength
     */
    private int getPasswordStrengthRating(int strengthLevel) {
        return Math.min(strengthLevel + 1, 5);
    }

    private void showWeakPasswordWarning(String strengthDescription, String newPassword) {
        String message;
        switch (strengthDescription) {
            case "very_weak":
                message = "Your password is very weak. Please include:\n" +
                        "• At least 8 characters\n" +
                        "• Uppercase letters\n" +
                        "• Lowercase letters\n" +
                        "• Numbers\n" +
                        "• Special characters";
                break;
            case "weak":
                message = "Your password is weak. For better security, add:\n" +
                        "• Uppercase letters\n" +
                        "• Numbers\n" +
                        "• Special characters";
                break;
            default:
                message = "Your password needs improvement for better security.";
        }

        new AlertDialog.Builder(this)
                .setTitle("Weak Password")
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show();
    }

    private void showReauthDialog(String currentPassword, final String newPassword,
                                  final int strengthLevel, final String strengthDescription,
                                  final int strengthRating) {
        new AlertDialog.Builder(this)
                .setTitle("Change Password")
                .setMessage("For security, please sign in again to change your password.")
                .setPositiveButton("Continue", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        updatePasswordInFirebase(newPassword, strengthLevel,
                                strengthDescription, strengthRating);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void updatePasswordInFirebase(String newPassword, int strengthLevel,
                                          String strengthDescription, int strengthRating) {
        if (currentUser != null) {
            // Show progress dialog
            AlertDialog progressDialog = new AlertDialog.Builder(this)
                    .setTitle("Changing Password")
                    .setMessage("Please wait...")
                    .setCancelable(false)
                    .create();
            progressDialog.show();

            currentUser.updatePassword(newPassword)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(Task<Void> task) {
                            if (task.isSuccessful()) {
                                // Update password strength in Firestore
                                updatePasswordStrengthInFirestore(strengthLevel, strengthDescription,
                                        strengthRating, progressDialog);
                            } else {
                                progressDialog.dismiss();
                                Toast.makeText(PrivacySecurityActivity.this,
                                        "Failed to update password: " + task.getException().getMessage(),
                                        Toast.LENGTH_SHORT).show();
                                Log.e("PasswordUpdate", "Error: " + task.getException().getMessage());
                            }
                        }
                    });
        }
    }

    private void updatePasswordStrengthInFirestore(int strengthLevel, String strengthDescription,
                                                   int strengthRating, AlertDialog progressDialog) {
        if (currentUser != null) {
            Map<String, Object> updates = new HashMap<>();
            updates.put("password_strength_level", strengthLevel);
            updates.put("password_strength_description", strengthDescription);
            updates.put("password_strength_rating", strengthRating);
            updates.put("password_last_updated", System.currentTimeMillis());

            db.collection("users").document(currentUser.getUid())
                    .update(updates)
                    .addOnSuccessListener(aVoid -> {
                        progressDialog.dismiss();
                        showPasswordChangeSuccessDialog();
                        Log.d("PasswordUpdate", "Password strength updated in Firestore");
                    })
                    .addOnFailureListener(e -> {
                        progressDialog.dismiss();
                        // Still show success since password was changed, but log the Firestore error
                        Log.e("PasswordUpdate", "Failed to update password strength in Firestore: " + e.getMessage());
                        showPasswordChangeSuccessDialog();
                    });
        }
    }

    private void showPasswordChangeSuccessDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Password Changed")
                .setMessage("Your password has been updated successfully.\n\n" +
                        "Password strength has been updated.")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Clear password fields
                        etCurrentPassword.setText("");
                        etNewPassword.setText("");
                        etConfirmPassword.setText("");

                        // Reload password strength display
                        loadPasswordStrengthFromFirestore();

                        // Navigate back to ProfileActivity
                        navigateToLogin();
                    }
                })
                .setCancelable(false)
                .show();
    }

    private void showDeleteAccountConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Account")
                .setMessage("Are you sure you want to permanently delete your account and all data? This action cannot be undone.")
                .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        showFinalDeleteConfirmation();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showFinalDeleteConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle("⚠️ Final Warning")
                .setMessage("This will permanently delete:\n\n• Your account\n• All food listings\n• Donation history\n• All personal data\n\nThis action CANNOT be undone!")
                .setPositiveButton("Permanently Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteUserAccount();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteUserAccount() {
        if (currentUser != null) {
            String userId = currentUser.getUid();

            // Show progress dialog
            AlertDialog progressDialog = new AlertDialog.Builder(this)
                    .setTitle("Deleting Account")
                    .setMessage("Please wait...")
                    .setCancelable(false)
                    .create();
            progressDialog.show();

            // 1. Delete all food listings from food_listing collection
            deleteAllUserListings(userId, progressDialog);
        }
    }

    private void deleteAllUserListings(String userId, AlertDialog progressDialog) {
        Log.d("FirestoreDebug", "Deleting from food_listings collection for user: " + userId);

        // Query the food_listings collection where seller_id equals user's ID
        db.collection("food_listings")  // Collection name is "food_listings" (with 's')
                .whereEqualTo("seller_id", userId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    Log.d("FirestoreDebug", "Found " + querySnapshot.size() + " documents in food_listings");

                    if (!querySnapshot.isEmpty()) {
                        WriteBatch batch = db.batch();
                        int deletedCount = 0;

                        // Delete each document that matches the user's seller_id
                        for (DocumentSnapshot document : querySnapshot) {
                            String documentId = document.getId();
                            String sellerId = document.getString("seller_id");

                            Log.d("FirestoreDebug", "Document ID: " + documentId +
                                    ", Seller ID: " + sellerId +
                                    ", Matches current user? " + userId.equals(sellerId));

                            // Double-check it's the right user (should already be filtered by query)
                            if (userId.equals(sellerId)) {
                                batch.delete(document.getReference());
                                deletedCount++;
                                Log.d("FirestoreDebug", "Marked for deletion: " + documentId);
                            }
                        }

                        if (deletedCount > 0) {
                            int finalDeletedCount = deletedCount;
                            batch.commit()
                                    .addOnSuccessListener(aVoid -> {
                                        Log.d("FirestoreDebug", "Successfully deleted " + finalDeletedCount + " food listings");
                                        Toast.makeText(this, "Deleted " + finalDeletedCount + " food listings",
                                                Toast.LENGTH_SHORT).show();
                                        deleteUserDocument(userId, progressDialog);
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e("FirestoreDebug", "Batch commit failed: " + e.getMessage());
                                        progressDialog.dismiss();
                                        Toast.makeText(this, "Failed to delete listings: " + e.getMessage(),
                                                Toast.LENGTH_SHORT).show();
                                    });
                        } else {
                            Log.d("FirestoreDebug", "No matching documents found after verification");
                            deleteUserDocument(userId, progressDialog);
                        }
                    } else {
                        Log.d("FirestoreDebug", "No food listings found for this user");
                        // No listings to delete, proceed to delete user document
                        deleteUserDocument(userId, progressDialog);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("FirestoreDebug", "Error querying food_listings: " + e.getMessage());
                    progressDialog.dismiss();
                    Toast.makeText(this, "Error accessing food listings: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void deleteUserDocument(String userId, AlertDialog progressDialog) {
        db.collection("users").document(userId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d("FirestoreDebug", "User document deleted");
                    deleteFirebaseAuthAccount(progressDialog);
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "Failed to delete user data: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void deleteFirebaseAuthAccount(AlertDialog progressDialog) {
        currentUser.delete()
                .addOnSuccessListener(aVoid -> {
                    progressDialog.dismiss();
                    showAccountDeleteSuccessDialog();
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "Failed to delete account: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void showAccountDeleteSuccessDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Account Deleted")
                .setMessage("Your account and all associated food listings have been permanently deleted.")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Navigate to login screen
                        mAuth.signOut();
                        navigateToLogin();
                    }
                })
                .setCancelable(false)
                .show();
    }

    private void setupBackPressHandler() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                navigateToProfile();
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
            }
        });
    }

    private void navigateToProfile(){
        Intent intent = new Intent(this, ProfileActivity.class);
        startActivity(intent);
        finish();
    }

    private void navigateToLogin(){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}