package com.example.foodlink;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

public class SignUpActivity extends AppCompatActivity {

    private CardView cardSeller, cardCharity;
    private ImageView ivSeller, ivCharity;
    private TextView tvNameLabel, tvBackText, tvSignIn;
    private EditText etName, etEmail, etPhone, etAddress, etPassword, etConfirmPassword;
    private Button btnCreateAccount;
    private String selectedRole = ""; // "seller" or "charity"

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // Initialize views
        initViews();
        setupClickListeners();
        setupTextWatchers();
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

        // Validate password match
        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Passwords do not match!", Toast.LENGTH_SHORT).show();
            etConfirmPassword.setError("Passwords do not match");
            return;
        }

        // Validate password length
        if (password.length() < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
            etPassword.setError("Minimum 6 characters required");
            return;
        }

        // Validate email format (basic check)
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Please enter a valid email address", Toast.LENGTH_SHORT).show();
            etEmail.setError("Invalid email format");
            return;
        }

        // Here you would typically:
        // 1. Call your API to register the user
        // 2. Handle the response
        // 3. Navigate to the appropriate screen

        // For now, show success message and go back to login
        Toast.makeText(this,
                "Account created successfully for " +
                        (selectedRole.equals("seller") ? "Seller" : "Charity") + ": " + name,
                Toast.LENGTH_LONG).show();

        // Pass data back to login activity if needed
        Intent resultIntent = new Intent();
        resultIntent.putExtra("email", email);
        resultIntent.putExtra("role", selectedRole);
        setResult(RESULT_OK, resultIntent);

        finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);

    }
}