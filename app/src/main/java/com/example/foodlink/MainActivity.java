package com.example.foodlink;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private EditText etEmail, etPassword;
    private Button btnSignIn;
    private TextView tvForgotPassword, tvSignUp;
    private CardView cardSeller, cardCharity;
    private String selectedUserType = ""; // Store selected user type

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnSignIn = findViewById(R.id.btnSignIn);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        tvSignUp = findViewById(R.id.tvSignUp);
        cardSeller = findViewById(R.id.cardSeller);
        cardCharity = findViewById(R.id.cardCharity);

        mAuth = FirebaseAuth.getInstance();

        // Set initial state (no selection)
        resetCardSelection();

        // Set click listeners for user type selection
        cardSeller.setOnClickListener(v -> {
            selectUserType("seller");
        });

        cardCharity.setOnClickListener(v -> {
            selectUserType("charity");
        });

        // Set click listeners for buttons
        btnSignIn.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            // Validate user type selection
            if (selectedUserType.isEmpty()) {
                Toast.makeText(this, "Please select user type", Toast.LENGTH_SHORT).show();
                return;
            }

            // Validate email
            if (email.isEmpty()) {
                etEmail.setError("Email is required");
                return;
            }

            if (!isValidEmail(email)) {
                etEmail.setError("Please enter a valid email address");
                return;
            }

            // Validate password
            if (password.isEmpty()) {
                etPassword.setError("Password is required");
                return;
            }

            if (password.length() < 6) {
                etPassword.setError("Password must be at least 6 characters");
                return;
            }

            // Handle sign in logic
            performLogin(email, password, selectedUserType);
        });

        // Make Forgot Password clickable
        tvForgotPassword.setOnClickListener(v -> {
            // Handle forgot password
            Toast.makeText(this, "Forgot Password clicked", Toast.LENGTH_SHORT).show();
            // You can navigate to forgot password activity here

//            Intent intent = new Intent(this, ForgotPasswordActivity.class);
//            overridePendingTransition(R.anim.slide_in_bottom, R.anim.slide_out_top);
        });

        // Make Sign Up text clickable (Partially)
        tvSignUp.setOnClickListener(v -> {
            // Handle sign up
            Toast.makeText(this, "Sign Up clicked", Toast.LENGTH_SHORT).show();
            // You can navigate to sign up activity here
            Intent intent = new Intent(MainActivity.this, SignUpActivity.class);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            startActivity(intent);
        });

        // Alternative: Make only "Sign Up" part clickable using SpannableString
        makeSignUpClickable();
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

    private boolean isValidEmail(String email) {
        // Android's built-in email pattern matching
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private void selectUserType(String type) {
        // Reset both cards to normal state
        resetCardSelection();

        // Set selected card state
        if (type.equals("seller")) {
            cardSeller.setCardBackgroundColor(Color.parseColor("#E3F2FD"));
//            cardSeller.setStrokeColor(Color.parseColor("#2196F3"));
//            cardSeller.setStrokeWidth(4);
            cardSeller.setCardElevation(8);
            selectedUserType = "seller";
        } else if (type.equals("charity")) {
            cardCharity.setCardBackgroundColor(Color.parseColor("#E3F2FD"));
//            cardCharity.setStrokeColor(Color.parseColor("#2196F3"));
//            cardCharity.setStrokeWidth(4);
            cardCharity.setCardElevation(8);
            selectedUserType = "charity";
        }
    }

    private void resetCardSelection() {
        // Reset Seller card
        cardSeller.setCardBackgroundColor(Color.parseColor("#F5F5F5"));
//        cardSeller.setStrokeColor(Color.parseColor("#E0E0E0"));
//        cardSeller.setStrokeWidth(1);
        cardSeller.setCardElevation(4);

        // Reset Charity card
        cardCharity.setCardBackgroundColor(Color.parseColor("#F5F5F5"));
//        cardCharity.setStrokeColor(Color.parseColor("#E0E0E0"));
//        cardCharity.setStrokeWidth(1);
        cardCharity.setCardElevation(4);

        selectedUserType = "";
    }

    private void performLogin(String email, String password, String userType) {
        // Show loading indicator (optional)
        // ProgressDialog progressDialog = new ProgressDialog(this);
        // progressDialog.setMessage("Logging in...");
        // progressDialog.show();

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        // progressDialog.dismiss(); // If using ProgressDialog

                        if (task.isSuccessful()) {
                            // Sign in success
                            Log.d(TAG, "signInWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();

                            // Check if email is verified (important for security)
                            if (user != null && user.isEmailVerified()) {
                                // User is verified, navigate to appropriate dashboard
                                navigateToDashboard(userType);
                            } else if (user != null) {
                                // Email not verified
                                Toast.makeText(MainActivity.this,
                                        "Please verify your email first. Check your inbox.",
                                        Toast.LENGTH_LONG).show();

                                // Optionally resend verification email
                                user.sendEmailVerification()
                                        .addOnCompleteListener(emailTask -> {
                                            if (emailTask.isSuccessful()) {
                                                Toast.makeText(MainActivity.this,
                                                        "Verification email sent!",
                                                        Toast.LENGTH_SHORT).show();
                                            }
                                        });

                                // Sign out until email is verified
                                mAuth.signOut();
                            }
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithEmail:failure", task.getException());

                            // User-friendly error messages
                            String errorMessage = "Login failed";
                            if (task.getException() != null) {
                                String error = task.getException().getMessage();
                                if (error.contains("invalid credential") || error.contains("password is invalid")) {
                                    errorMessage = "Invalid email or password";
                                } else if (error.contains("no user record")) {
                                    errorMessage = "No account found with this email";
                                } else if (error.contains("badly formatted")) {
                                    errorMessage = "Invalid email format";
                                } else {
                                    errorMessage = error;
                                }
                            }

                            Toast.makeText(MainActivity.this, errorMessage, // ✅ Fixed: Use MainActivity.this
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void navigateToDashboard(String userType) {
        Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show();

        // TODO: Check user type in Firestore to verify they're logging in as the correct type

        if (userType.equals("seller")) {
            Intent intent = new Intent(this, SellerDashboardActivity.class);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            startActivity(intent);
            finish();
        } else if (userType.equals("charity")) {
            Intent intent = new Intent(this, CharityDashboardActivity.class);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            startActivity(intent);
            finish();
        }
    }

    private void makeSignUpClickable() {
        // Optional: Make only "Sign Up" part clickable using SpannableString
        // This is more advanced but provides better UX
        String fullText = "Don't have an account? Sign Up";
        // We're already handling the click in the tvSignUp.setOnClickListener
        // This method is for if you want only "Sign Up" to be clickable
    }
}