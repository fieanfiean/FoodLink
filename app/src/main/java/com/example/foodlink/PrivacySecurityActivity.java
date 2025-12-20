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
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;
import com.google.firebase.firestore.DocumentSnapshot;

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

            // Check password strength
            tvPasswordStrengthStatus.setText("Strong Password");
            tvPasswordStrengthStatus.setTextColor(getResources().getColor(R.color.status_active));

            // 2FA status
//            tv2faStatus.setText("2FA Disabled");
//            tv2faStatus.setTextColor(getResources().getColor(R.color.status_expired));
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

        if (newPassword.length() < 8) { // Fixed: Changed from 6 to 8
            etNewPassword.setError("Password must be at least 8 characters");
            etNewPassword.requestFocus();
            return;
        }

//        if (!newPassword.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$")) {
//            etNewPassword.setError("Must include uppercase, lowercase, and numbers");
//            etNewPassword.requestFocus();
//            return;
//        }

        if (!newPassword.equals(confirmPassword)) {
            etConfirmPassword.setError("Passwords do not match");
            etConfirmPassword.requestFocus();
            return;
        }

        // Show re-authentication dialog
        showReauthDialog(currentPassword, newPassword);
    }

    private void showReauthDialog(String currentPassword, final String newPassword) {
        new AlertDialog.Builder(this)
                .setTitle("Change Password")
                .setMessage("For security, please sign in again to change your password.")
                .setPositiveButton("Continue", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        updatePasswordInFirebase(newPassword);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void updatePasswordInFirebase(String newPassword) {
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
                            progressDialog.dismiss();

                            if (task.isSuccessful()) {
                                // Show success dialog
                                showPasswordChangeSuccessDialog();
                                Log.d("PasswordUpdate", "Password updated successfully");
                            } else {
                                Toast.makeText(PrivacySecurityActivity.this,
                                        "Failed to update password: " + task.getException().getMessage(),
                                        Toast.LENGTH_SHORT).show();
                                Log.e("PasswordUpdate", "Error: " + task.getException().getMessage());
                            }
                        }
                    });
        }
    }

    private void showPasswordChangeSuccessDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Password Changed")
                .setMessage("Your password has been updated successfully.")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Clear password fields
                        etCurrentPassword.setText("");
                        etNewPassword.setText("");
                        etConfirmPassword.setText("");

                        // Navigate back to ProfileActivity
                        navigateToProfile();
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
                        Intent intent = new Intent(PrivacySecurityActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
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