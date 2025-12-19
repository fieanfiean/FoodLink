package com.example.foodlink;

import static android.content.ContentValues.TAG;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class SignUpActivity extends AppCompatActivity {

    private CardView cardSeller, cardCharity;
    private ImageView ivSeller, ivCharity;
    private TextView tvNameLabel, tvBackText, tvSignIn;
    private EditText etName, etEmail, etPhone, etAddress, etPassword, etConfirmPassword;
    private Button btnCreateAccount;
    private String selectedRole = ""; // "seller" or "charity"
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        try {
            FirebaseApp.initializeApp(this);
            mAuth = FirebaseAuth.getInstance();
        } catch (Exception e) {
            Toast.makeText(this, "Firebase initialization failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
            // You might want to proceed without Firebase or exit gracefully
            finish();
            return;
        }

        // Initialize views
        initViews();
        setupClickListeners();
        setupTextWatchers();
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            currentUser.reload();
        }
    }

    private void initViews() {
        cardSeller = findViewById(R.id.cardSeller);
        cardCharity = findViewById(R.id.cardCharity);
        ivSeller = findViewById(R.id.ivSeller);
        ivCharity = findViewById(R.id.ivCharity);
        tvNameLabel = findViewById(R.id.tvNameLabel);
        tvBackText = findViewById(R.id.tvBackText);
        tvSignIn = findViewById(R.id.tvSignIn);
        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);
        etAddress = findViewById(R.id.etAddress);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnCreateAccount = findViewById(R.id.btnCreateAccount);
    }

    private void setupClickListeners() {
        // Back to login
        findViewById(R.id.btnBack).setOnClickListener(v -> {
            finish(); // Go back to login activity
        });

        tvBackText.setOnClickListener(v -> finish());
        tvSignIn.setOnClickListener(v -> finish());

        // Role selection
        cardSeller.setOnClickListener(v -> selectRole("seller"));
        cardCharity.setOnClickListener(v -> selectRole("charity"));

        // Create account button
        btnCreateAccount.setOnClickListener(v -> createAccount());
    }

    private void selectRole(String role) {
        selectedRole = role;

        // Reset both cards
        cardSeller.setSelected(false);
        cardCharity.setSelected(false);
        ivSeller.setColorFilter(Color.parseColor("#666666"));
        ivCharity.setColorFilter(Color.parseColor("#666666"));

        // Select the chosen card
        if (role.equals("seller")) {
            cardSeller.setSelected(true);
            ivSeller.setColorFilter(Color.parseColor("#4CAF50"));
            tvNameLabel.setText("Business Name *");
            etName.setHint("e.g., Green Leaf Restaurant");
        } else if (role.equals("charity")) {
            cardCharity.setSelected(true);
            ivCharity.setColorFilter(Color.parseColor("#4CAF50"));
            tvNameLabel.setText("Organization Name *");
            etName.setHint("e.g., Hope Community Kitchen");
        }

//        updateCreateButtonState();
    }

    private void setupTextWatchers() {
        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
//                updateCreateButtonState();
            }
        };

        etName.addTextChangedListener(textWatcher);
        etEmail.addTextChangedListener(textWatcher);
        etPhone.addTextChangedListener(textWatcher);
        etAddress.addTextChangedListener(textWatcher);
        etPassword.addTextChangedListener(textWatcher);
        etConfirmPassword.addTextChangedListener(textWatcher);
    }

//    private void updateCreateButtonState() {
//        boolean isFormValid = isFormValid();
//        btnCreateAccount.setEnabled(isFormValid);
//        btnCreateAccount.setAlpha(isFormValid ? 1.0f : 0.5f);
//    }

    private boolean isFormValid() {
        return !selectedRole.isEmpty() &&
                !etName.getText().toString().trim().isEmpty() &&
                !etEmail.getText().toString().trim().isEmpty() &&
                !etPhone.getText().toString().trim().isEmpty() &&
                !etAddress.getText().toString().trim().isEmpty() &&
                !etPassword.getText().toString().isEmpty() &&
                !etConfirmPassword.getText().toString().isEmpty();
    }

    private void createAccount() {
        // Get form data
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String address = etAddress.getText().toString().trim();
        String password = etPassword.getText().toString();
        String confirmPassword = etConfirmPassword.getText().toString();

        // Validate inputs
        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError("Passwords do not match");
            return;
        }

        if (password.length() < 6) {
            etPassword.setError("Minimum 6 characters required");
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Invalid email format");
            return;
        }

        // Show loading
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Creating account...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();

                        if (firebaseUser != null) {
                            // Create user data for Firestore
                            Map<String, Object> userData = new HashMap<>();

                            // Basic user information
                            userData.put("user_id", firebaseUser.getUid());
                            userData.put("email", email);
                            userData.put("user_type", selectedRole); // "seller" or "charity"
                            userData.put("full_name", name);
                            userData.put("phone", phone);
                            userData.put("address", address);

                            // Role-specific fields
                            if (selectedRole.equals("seller")) {
                                userData.put("business_name", name); // Using name as business name
                                userData.put("business_address", address);
                                userData.put("charity_name", ""); // Empty for sellers
                                userData.put("charity_registration", "");
                            } else if (selectedRole.equals("charity")) {
                                userData.put("charity_name", name); // Using name as charity name
                                userData.put("charity_address", address);
                                userData.put("business_name", ""); // Empty for charities
                                userData.put("business_registration", "");
                            }

                            // Additional metadata
//                            userData.put("is_email_verified", false);
                            userData.put("created_at", System.currentTimeMillis());
                            userData.put("last_login", System.currentTimeMillis());
                            userData.put("is_active", true);
                            userData.put("profile_image_url", "");

                            // Statistics (initialize to 0)
                            if (selectedRole.equals("seller")) {
                                userData.put("food_saved", 0);
                                userData.put("donation_made", 0);
                                userData.put("co2_reduced", 0.0);
                                userData.put("charities_helped",0);
                            }
//                            else if (selectedRole.equals("charity")) {
//                                userData.put("total_reservations", 0);
//                                userData.put("total_received", 0);
//                            }

                            // Save user data to Firestore 'users' collection
                            db.collection("user")
                                    .document(firebaseUser.getUid()) // Use Firebase UID as document ID
                                    .set(userData)
                                    .addOnSuccessListener(aVoid -> {
                                        // User data saved successfully
                                        Log.d(TAG, "User document written with ID: " + firebaseUser.getUid());

                                        // Send verification email
                                        firebaseUser.sendEmailVerification()
                                                .addOnCompleteListener(emailTask -> {
                                                    progressDialog.dismiss();

                                                    String message = emailTask.isSuccessful() ?
                                                            "Verification email sent to " + email +
                                                                    ". Please check your inbox." :
                                                            "Account created but failed to send verification email";

                                                    Toast.makeText(SignUpActivity.this,
                                                            message,
                                                            Toast.LENGTH_LONG).show();

                                                    // Show success message with role
                                                    String roleDisplay = selectedRole.equals("seller") ? "Seller" : "Charity";
                                                    Toast.makeText(SignUpActivity.this,
                                                            roleDisplay + " account created successfully!",
                                                            Toast.LENGTH_SHORT).show();

                                                    // Return to login page with email pre-filled
                                                    Intent resultIntent = new Intent();
                                                    resultIntent.putExtra("email", email);
                                                    setResult(RESULT_OK, resultIntent);
                                                    finish();
                                                });
                                    })
                                    .addOnFailureListener(e -> {
                                        progressDialog.dismiss();
                                        Log.w(TAG, "Error writing user document", e);

                                        // User created in Auth but failed to save to Firestore
                                        Toast.makeText(SignUpActivity.this,
                                                "Account created but failed to save profile data. Please login and update your profile.",
                                                Toast.LENGTH_LONG).show();

                                        // Still return to login since auth succeeded
                                        Intent resultIntent = new Intent();
                                        resultIntent.putExtra("email", email);
                                        setResult(RESULT_OK, resultIntent);
                                        finish();
                                    });
                        }
                    } else {
                        progressDialog.dismiss();
                        String errorMessage = "Registration failed";
                        if (task.getException() != null) {
                            String error = task.getException().getMessage();
                            if (error.contains("already in use")) {
                                errorMessage = "Email already registered";
                            } else if (error.contains("badly formatted")) {
                                errorMessage = "Invalid email format";
                            } else if (error.contains("weak password")) {
                                errorMessage = "Password is too weak";
                            } else {
                                errorMessage = error;
                            }
                        }
                        Toast.makeText(SignUpActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                    }
                });
    }
}