package com.example.foodlink;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class ChangePasswordActivity extends AppCompatActivity {

    private TextInputEditText etCurrentPassword;
    private TextInputEditText etNewPassword;
    private TextInputEditText etConfirmPassword;
    private MaterialButton btnSavePassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password); // Create this layout

//        initViews();
//        setupListeners();
    }

//    private void initViews() {
//        etCurrentPassword = findViewById(R.id.etCurrentPassword);
//        etNewPassword = findViewById(R.id.etNewPassword);
//        etConfirmPassword = findViewById(R.id.etConfirmPassword);
//        btnSavePassword = findViewById(R.id.btnSavePassword);
//    }
//
//    private void setupListeners() {
//        btnSavePassword.setOnClickListener(v -> changePassword());
//    }
//
//    private void changePassword() {
//        String currentPassword = etCurrentPassword.getText().toString();
//        String newPassword = etNewPassword.getText().toString();
//        String confirmPassword = etConfirmPassword.getText().toString();
//
//        if (currentPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
//            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        if (!newPassword.equals(confirmPassword)) {
//            Toast.makeText(this, "New passwords don't match", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        if (newPassword.length() < 6) {
//            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        // TODO: Implement actual password change logic (API call, database update, etc.)
//        Toast.makeText(this, "Password changed successfully", Toast.LENGTH_SHORT).show();
//        finish();
//    }
}