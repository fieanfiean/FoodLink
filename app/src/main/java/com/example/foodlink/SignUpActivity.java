package com.example.foodlink;

import static android.content.ContentValues.TAG;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
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
import java.util.regex.Pattern;

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
    }

    private void setupTextWatchers() {
        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                // You can add password strength indicator updates here if needed
            }
        };

        etName.addTextChangedListener(textWatcher);
        etEmail.addTextChangedListener(textWatcher);
        etPhone.addTextChangedListener(textWatcher);
        etAddress.addTextChangedListener(textWatcher);
        etPassword.addTextChangedListener(textWatcher);
        etConfirmPassword.addTextChangedListener(textWatcher);
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

    private boolean isFormValid() {
        return !selectedRole.isEmpty() &&
                !etName.getText().toString().trim().isEmpty() &&
                !etEmail.getText().toString().trim().isEmpty() &&
                !etPhone.getText().toString().trim().isEmpty() &&
                !etAddress.getText().toString().trim().isEmpty() &&
                !etPassword.getText().toString().isEmpty() &&
                !etConfirmPassword.getText().toString().isEmpty();
    }

    /**
     * Checks if an email is already registered in Firebase Auth
     * @param email The email to check
     * @param callback Callback to handle the result (true = exists, false = doesn't exist)
     */
    private void checkEmailExists(String email, EmailCheckCallback callback) {
        mAuth.fetchSignInMethodsForEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // If the list of sign-in methods is not empty, email exists
                        boolean exists = task.getResult().getSignInMethods() != null &&
                                !task.getResult().getSignInMethods().isEmpty();
                        callback.onResult(exists);
                    } else {
                        // If there's an error, we'll assume email doesn't exist to allow registration attempt
                        // But we'll log the error
                        Log.e(TAG, "Error checking email existence: " + task.getException());
                        callback.onResult(false);
                    }
                });
    }

    interface EmailCheckCallback {
        void onResult(boolean emailExists);
    }

    private void createAccount() {
        // Get form data
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String address = etAddress.getText().toString().trim();
        String password = etPassword.getText().toString();
        String confirmPassword = etConfirmPassword.getText().toString();

        // Validate role selection
        if (selectedRole.isEmpty()) {
            Toast.makeText(this, "Please select a role (Seller or Charity)", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate name
        if (name.isEmpty()) {
            etName.setError("Name is required");
            return;
        }

        // Validate email
        if (email.isEmpty()) {
            etEmail.setError("Email is required");
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Invalid email format");
            return;
        }

        // Validate phone
        if (phone.isEmpty()) {
            etPhone.setError("Phone number is required");
            return;
        }

        // Validate address
        if (address.isEmpty()) {
            etAddress.setError("Address is required");
            return;
        }

        // Validate password
        if (password.isEmpty()) {
            etPassword.setError("Password is required");
            return;
        }

        if (password.length() < 8) {
            etPassword.setError("Minimum 8 characters required");
            return;
        }

        // Validate confirm password
        if (confirmPassword.isEmpty()) {
            etConfirmPassword.setError("Please confirm your password");
            return;
        }

        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError("Passwords do not match");
            return;
        }

        // Check password strength
        int passwordStrengthLevel = checkPasswordStrength(password);
        String passwordStrengthDescription = getPasswordStrengthDescription(passwordStrengthLevel);
        int passwordStrengthRating = getPasswordStrengthRating(passwordStrengthLevel);

        // Show password strength feedback (optional)
        if (passwordStrengthRating <= 2) {
            Toast.makeText(this, "Your password is weak. Consider adding uppercase letters, numbers, or special characters.",
                    Toast.LENGTH_LONG).show();
            // You can proceed anyway or require stronger password
            // For now, we'll just warn but allow registration
        }

        // Show initial loading
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Checking email availability...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        // First check if email already exists
        checkEmailExists(email, new EmailCheckCallback() {
            @Override
            public void onResult(boolean emailExists) {
                if (emailExists) {
                    progressDialog.dismiss();
                    etEmail.setError("This email is already registered");
                    Toast.makeText(SignUpActivity.this,
                            "Email already registered. Please use a different email or try logging in.",
                            Toast.LENGTH_LONG).show();
                    return;
                }

                // Email doesn't exist, proceed with registration
                progressDialog.setMessage("Creating account...");

                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(SignUpActivity.this, task -> {
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

                                    // Password strength information
                                    userData.put("password_strength_level", passwordStrengthLevel);
                                    userData.put("password_strength_description", passwordStrengthDescription);
                                    userData.put("password_strength_rating", passwordStrengthRating);
                                    userData.put("password_last_updated", System.currentTimeMillis());

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

                                    // Save user data to Firestore 'users' collection
                                    db.collection("users")
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
        });
    }
}