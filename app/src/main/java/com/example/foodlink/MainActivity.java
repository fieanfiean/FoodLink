package com.example.foodlink;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import com.google.android.material.card.MaterialCardView;

public class MainActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnSignIn;
    private TextView tvForgotPassword, tvSignUp;
    private CardView cardSeller, cardCharity;
    private String selectedUserType = ""; // Store selected user type

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize views
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnSignIn = findViewById(R.id.btnSignIn);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        tvSignUp = findViewById(R.id.tvSignUp);
        cardSeller = findViewById(R.id.cardSeller);
        cardCharity = findViewById(R.id.cardCharity);

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
            String email = etEmail.getText().toString();
            String password = etPassword.getText().toString();

            // Validate user type selection
            if (selectedUserType.isEmpty()) {
                Toast.makeText(this, "Please select user type", Toast.LENGTH_SHORT).show();
                return;
            }

            // Validate email and password
            if (email.isEmpty()) {
                etEmail.setError("Email is required");
                return;
            }

            if (password.isEmpty()) {
                etPassword.setError("Password is required");
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
        // Implement your login logic here
        Toast.makeText(this,
                "Login as " + userType + " with email: " + email,
                Toast.LENGTH_SHORT).show();

        // Example: You would typically call an API here
        // For now, just show a toast
        if(userType.equals("seller") ) {
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