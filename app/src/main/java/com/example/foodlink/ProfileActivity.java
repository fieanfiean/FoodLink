package com.example.foodlink;

import static android.content.ContentValues.TAG;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.foodlink.Profile;
import com.example.foodlink.SessionManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {

    private TextView tvProfileName, tvUserType, tvEmail, tvPhone, tvAddress;
//    private TextView tvFoodSaved, tvDonationsMade, tvCo2Reduced, tvCharitiesHelped;
    private ImageView imageViewProfile; // Variable name should match usage

    private BottomNavigationView bottomNavigation;
    private Profile userProfile;
    private SessionManager sessionManager;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Hide default action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        setContentView(R.layout.activity_profile);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        sessionManager = new SessionManager(this);
        initViews();
        setupClickListeners();
        setupBottomNavigation();
        loadProfileDataFromFirestore();
        setupBackPressHandler();
        checkAndSyncEmailVerification();
    }

    private void initViews() {
        // Profile info views
        tvProfileName = findViewById(R.id.tvProfileName);
        tvUserType = findViewById(R.id.tvUserType);
        tvEmail = findViewById(R.id.tvEmail);
        tvPhone = findViewById(R.id.tvPhone);
        tvAddress = findViewById(R.id.tvAddress);

        // Impact metrics views
//        tvFoodSaved = findViewById(R.id.tvFoodSaved);
//        tvDonationsMade = findViewById(R.id.tvDonationsMade);
//        tvCo2Reduced = findViewById(R.id.tvCo2Reduced);
//        tvCharitiesHelped = findViewById(R.id.tvCharitiesHelped);

        bottomNavigation = findViewById(R.id.bottomNavigation);

        // CORRECT WAY TO GET THE ImageView from inside the CardView
        CardView profileImageCard = findViewById(R.id.ivProfileImage); // This is the CardView
        imageViewProfile = profileImageCard.findViewById(R.id.imageViewProfile); // This is the ImageView inside

        // Debug log to verify
        Log.d("ProfileActivity", "ImageView initialized: " + (imageViewProfile != null));
    }

    private void setupClickListeners() {
        // Edit Profile
        findViewById(R.id.btnEditProfile).setOnClickListener(v -> {
            navigateToEditProfile();
        });

        // Notification Settings
//        findViewById(R.id.btnNotificationSettings).setOnClickListener(v -> {
//            navigateToNotificationSettings();
//        });

        // Privacy & Security
        findViewById(R.id.btnPrivacySecurity).setOnClickListener(v -> {
            navigateToPrivacySecurity();
        });

        // Help & Support
        findViewById(R.id.btnHelpSupport).setOnClickListener(v -> {
            navigateToHelpSupport();
        });

        // Logout
        findViewById(R.id.btnLogout).setOnClickListener(v -> {
            showLogoutConfirmation();
        });
    }

    private void setupBottomNavigation() {
        // Clear existing menu items
        bottomNavigation.getMenu().clear();

        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();
        db.collection("users").document(userId).get().addOnSuccessListener(documentSnapshot -> {
            if(documentSnapshot.exists()){
                String userType = documentSnapshot.getString("user_type");

                // Clear menu again inside the callback (just to be safe)
                bottomNavigation.getMenu().clear();

                if(userType != null && userType.equals("seller")){
                    // Add only the items you want for seller
                    bottomNavigation.getMenu().add(Menu.NONE, R.id.nav_dashboard, 1, "Dashboard")
                            .setIcon(R.drawable.ic_dashboard);

                    bottomNavigation.getMenu().add(Menu.NONE, R.id.nav_add, 2, "Add")
                            .setIcon(R.drawable.ic_add);

                    bottomNavigation.getMenu().add(Menu.NONE, R.id.nav_profile, 3, "Profile")
                            .setIcon(R.drawable.ic_profile);

                    bottomNavigation.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
                        @Override
                        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                            int itemId = item.getItemId();

                            if (itemId == R.id.nav_dashboard) {
                                navigateToDashboard(userType);
                                return true;
                            } else if (itemId == R.id.nav_add) {
                                navigateToAddListing();
                                return true;
                            } else if (itemId == R.id.nav_profile) {
                                return true;
                            }
                            return false;
                        }
                    });
                } else if(userType != null && userType.equals("charity")){
                    // Add only the items you want for charity
                    bottomNavigation.getMenu().add(Menu.NONE, R.id.nav_dashboard, 1, "Dashboard")
                            .setIcon(R.drawable.ic_dashboard);

                    bottomNavigation.getMenu().add(Menu.NONE, R.id.nav_reservations, 2, "Reservation")
                            .setIcon(R.drawable.ic_reservation);

                    bottomNavigation.getMenu().add(Menu.NONE, R.id.nav_profile, 3, "Profile")
                            .setIcon(R.drawable.ic_profile);

                    bottomNavigation.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
                        @Override
                        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                            int itemId = item.getItemId();

                            if (itemId == R.id.nav_dashboard) {
                                navigateToDashboard(userType);
                                return true;
                            } else if (itemId == R.id.nav_reservations) {
                                navigateToReservation();
                                return true;
                            } else if (itemId == R.id.nav_profile) {
                                return true;
                            }
                            return false;
                        }
                    });
                } else {
                    // Default/fallback navigation
                    setupDefaultBottomNavigation();
                }

                // IMPORTANT: Set profile as selected AFTER adding menu items
                // This should be inside the success callback
                bottomNavigation.setSelectedItemId(R.id.nav_profile);

            } else {
                // Document doesn't exist, setup default navigation
                setupDefaultBottomNavigation();
                bottomNavigation.setSelectedItemId(R.id.nav_profile);
            }
        }).addOnFailureListener(e -> {
            // Handle failure
            Toast.makeText(this, "Error loading user data", Toast.LENGTH_SHORT).show();
            setupDefaultBottomNavigation();
            bottomNavigation.setSelectedItemId(R.id.nav_profile);
        });
    }

    private void setupDefaultBottomNavigation() {
        bottomNavigation.getMenu().clear();

        bottomNavigation.getMenu().add(Menu.NONE, R.id.nav_dashboard, 1, "Dashboard")
                .setIcon(R.drawable.ic_dashboard);

        bottomNavigation.getMenu().add(Menu.NONE, R.id.nav_profile, 2, "Profile")
                .setIcon(R.drawable.ic_profile);

        bottomNavigation.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();

                if (itemId == R.id.nav_dashboard) {
                    // Navigate to appropriate dashboard based on stored user type
                    String defaultType = sessionManager.getUserType(); // Or get from SharedPreferences
                    navigateToDashboard(defaultType != null ? defaultType : "user");
                    return true;
                } else if (itemId == R.id.nav_profile) {
                    return true;
                }
                return false;
            }
        });
    }

    private void loadProfileDataFromFirestore() {
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            // User is not logged in, handle this case
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            // Optionally navigate to login screen
            return;
        }

        String userId = currentUser.getUid();

        // Fetch user document from Firestore "users" collection
        db.collection("users").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Document exists, create Profile object from data
                        userProfile = new Profile();

                        // Set basic profile info
                        userProfile.setUserId(userId);

                        // Get user's name - check different possible field names
                        String name = documentSnapshot.getString("business_name");
                        if (name == null || name.isEmpty()) {
                            name = documentSnapshot.getString("full_name");
                        }
                        if (name == null || name.isEmpty()) {
                            name = documentSnapshot.getString("name");
                        }
                        if (name == null || name.isEmpty()) {
                            name = currentUser.getEmail(); // fallback to email
                        }
                        userProfile.setName(name != null ? name : "User");

                        // Get email - from Firebase Auth or Firestore
                        String email = documentSnapshot.getString("email");
                        if (email == null || email.isEmpty()) {
                            email = currentUser.getEmail();
                        }
                        userProfile.setEmail(email != null ? email : "No email");

                        // Get other fields
                        userProfile.setPhone(documentSnapshot.getString("phone"));
                        userProfile.setAddress(documentSnapshot.getString("address"));

                        // Get user type and set appropriate display text
                        String userType = documentSnapshot.getString("user_type");
                        if ("seller".equals(userType)) {
                            userProfile.setUserType("Food Seller");
                        } else if ("charity".equals(userType)) {
                            userProfile.setUserType("Charity Organization");
                        } else {
                            userProfile.setUserType("User");
                        }

                        // Get sustainability metrics - handle possible number types
                        Object foodSavedObj = documentSnapshot.get("food_saved_kg");
                        if (foodSavedObj != null) {
                            if (foodSavedObj instanceof Long) {
                                userProfile.setFoodSavedKg(((Long) foodSavedObj).intValue());
                            } else if (foodSavedObj instanceof Double) {
                                userProfile.setFoodSavedKg(((Double) foodSavedObj).intValue());
                            } else if (foodSavedObj instanceof Integer) {
                                userProfile.setFoodSavedKg((Integer) foodSavedObj);
                            }
                        }

                        // Similarly for other metrics
                        Object donationsObj = documentSnapshot.get("donations_made");
                        if (donationsObj != null) {
                            if (donationsObj instanceof Long) {
                                userProfile.setDonationsMade(((Long) donationsObj).intValue());
                            } else if (donationsObj instanceof Integer) {
                                userProfile.setDonationsMade((Integer) donationsObj);
                            }
                        }

                        // Get CO2 reduced
                        Object co2Obj = documentSnapshot.get("co2_reduced_kg");
                        if (co2Obj != null) {
                            if (co2Obj instanceof Long) {
                                userProfile.setCo2ReducedKg(((Long) co2Obj).intValue());
                            } else if (co2Obj instanceof Double) {
                                userProfile.setCo2ReducedKg(((Double) co2Obj).intValue());
                            } else if (co2Obj instanceof Integer) {
                                userProfile.setCo2ReducedKg((Integer) co2Obj);
                            }
                        }

                        // Get charities helped
                        Object charitiesObj = documentSnapshot.get("charities_helped");
                        if (charitiesObj != null) {
                            if (charitiesObj instanceof Long) {
                                userProfile.setCharitiesHelped(((Long) charitiesObj).intValue());
                            } else if (charitiesObj instanceof Integer) {
                                userProfile.setCharitiesHelped((Integer) charitiesObj);
                            }
                        }

                        // LOAD BASE64 PROFILE IMAGE
                        String profileImageBase64 = documentSnapshot.getString("profile_image_base64");
                        Boolean hasProfileImage = documentSnapshot.getBoolean("has_profile_image");

                        if (profileImageBase64 != null && !profileImageBase64.isEmpty() &&
                                hasProfileImage != null && hasProfileImage) {
                            loadBase64Image(profileImageBase64);
                        } else {
                            // Set default image if no profile image exists
                            if (imageViewProfile != null) {
                                imageViewProfile.setImageResource(R.drawable.ic_profile);
                            }
                        }

                        // Update UI with real data
                        updateProfileUI();

                    } else {
                        // Document doesn't exist, use default data or create new
                        Toast.makeText(ProfileActivity.this,
                                "User profile not found", Toast.LENGTH_SHORT).show();
                        createDefaultProfile(currentUser);
                    }
                })
                .addOnFailureListener(e -> {
                    // Handle error
                    Toast.makeText(ProfileActivity.this,
                            "Error loading profile: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e("ProfileActivity", "Error loading profile: " + e.getMessage());

                    // Fallback to default data
                    createDefaultProfile(currentUser);
                });
    }

    private void checkAndSyncEmailVerification() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            user.reload().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    String currentAuthEmail = user.getEmail();

                    // Check Firestore for pending verification
                    db.collection("users").document(user.getUid())
                            .get()
                            .addOnSuccessListener(documentSnapshot -> {
                                if (documentSnapshot.exists()) {
                                    String storedEmail = documentSnapshot.getString("email");
                                    String pendingEmail = documentSnapshot.getString("auth_email_pending");

                                    // If Firebase Auth email is different from Firestore
                                    // and matches pending email, update Firestore
                                    if (pendingEmail != null &&
                                            pendingEmail.equals(currentAuthEmail) &&
                                            !currentAuthEmail.equals(storedEmail)) {

                                        // Update Firestore to match Auth email
                                        Map<String, Object> updateData = new HashMap<>();
                                        updateData.put("email", currentAuthEmail);
                                        updateData.put("auth_email_pending", FieldValue.delete());
                                        updateData.put("auth_email_status", "verified");
                                        updateData.put("auth_email_verified_at", System.currentTimeMillis());

                                        db.collection("users").document(user.getUid())
                                                .update(updateData)
                                                .addOnSuccessListener(aVoid -> {
                                                    Log.d(TAG, "Firestore email synced with Auth: " + currentAuthEmail);
                                                    // Refresh UI
                                                    loadProfileDataFromFirestore();
                                                });
                                    }
                                }
                            });
                }
            });
        }
    }

    // ADD THIS METHOD: Load Base64 image and display it
    private void loadBase64Image(String base64String) {
        if (base64String == null || base64String.isEmpty() || imageViewProfile == null) {
            return;
        }

        try {
            Log.d("ProfileActivity", "Loading Base64 image, length: " + base64String.length());

            // Clean the Base64 string (remove data URI prefix if present)
            String cleanBase64 = base64String.trim();
            if (cleanBase64.contains("base64,")) {
                cleanBase64 = cleanBase64.substring(cleanBase64.indexOf("base64,") + 7);
                Log.d("ProfileActivity", "Removed data URI prefix");
            }

            // Decode Base64 to byte array
            byte[] decodedBytes = Base64.decode(cleanBase64, Base64.DEFAULT);

            if (decodedBytes != null && decodedBytes.length > 0) {
                Log.d("ProfileActivity", "Decoded bytes: " + decodedBytes.length);

                // Convert to Bitmap
                Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);

                if (bitmap != null) {
                    // Set the bitmap to ImageView
                    imageViewProfile.setImageBitmap(bitmap);
                    Log.d("ProfileActivity", "Profile image loaded from Base64: " +
                            bitmap.getWidth() + "x" + bitmap.getHeight());
                } else {
                    Log.e("ProfileActivity", "Failed to decode bitmap from Base64");
                    imageViewProfile.setImageResource(R.drawable.ic_profile);
                }
            } else {
                Log.e("ProfileActivity", "Decoded bytes are null or empty");
                imageViewProfile.setImageResource(R.drawable.ic_profile);
            }
        } catch (IllegalArgumentException e) {
            Log.e("ProfileActivity", "Invalid Base64 format: " + e.getMessage());
            imageViewProfile.setImageResource(R.drawable.ic_profile);
        } catch (Exception e) {
            Log.e("ProfileActivity", "Error loading Base64 image: " + e.getMessage());
            imageViewProfile.setImageResource(R.drawable.ic_profile);
        }
    }

    private void createDefaultProfile(FirebaseUser currentUser) {
        // Create a basic profile from Firebase Auth data
        userProfile = new Profile();
        userProfile.setUserId(currentUser.getUid());
        userProfile.setName(currentUser.getDisplayName() != null ?
                currentUser.getDisplayName() : "User");
        userProfile.setEmail(currentUser.getEmail());
        userProfile.setUserType("User");

        // Set default/empty values for other fields
        userProfile.setPhone("Not set");
        userProfile.setAddress("Not set");
        userProfile.setFoodSavedKg(0);
        userProfile.setDonationsMade(0);
        userProfile.setCo2ReducedKg(0);
        userProfile.setCharitiesHelped(0);

        // Set default image
        if (imageViewProfile != null) {
            imageViewProfile.setImageResource(R.drawable.ic_profile);
        }

        updateProfileUI();
    }

    // Update onResume to refresh profile image when returning from EditProfile
    @Override
    protected void onResume() {
        super.onResume();
        // Refresh profile data when activity resumes
        if (mAuth.getCurrentUser() != null) {
            loadProfileDataFromFirestore();
        }
    }

    private void updateProfileUI() {
        if (userProfile != null) {
            // Set profile information
            tvProfileName.setText(userProfile.getName());
            tvUserType.setText(userProfile.getUserType());
            tvEmail.setText(userProfile.getEmail());
            tvPhone.setText(userProfile.getPhone() != null ?
                    userProfile.getPhone() : "Not set");
            tvAddress.setText(userProfile.getAddress() != null ?
                    userProfile.getAddress() : "Not set");

            // Set impact metrics
//            tvFoodSaved.setText(userProfile.getFoodSavedKg() + "kg");
//            tvDonationsMade.setText(String.valueOf(userProfile.getDonationsMade()));
//            tvCo2Reduced.setText(userProfile.getCo2ReducedKg() + "kg");
//            tvCharitiesHelped.setText(String.valueOf(userProfile.getCharitiesHelped()));
        }
    }

    private void navigateToEditProfile() {
        Intent intent = new Intent(this, EditProfileActivity.class);
        startActivityForResult(intent, 100); // Request code 100 for edit profile
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    private void navigateToNotificationSettings() {
        Toast.makeText(this, "Notification Settings clicked", Toast.LENGTH_SHORT).show();
        // Intent intent = new Intent(this, NotificationSettingsActivity.class);
        // startActivity(intent);
    }

    private void navigateToPrivacySecurity() {
        Toast.makeText(this, "Privacy & Security clicked", Toast.LENGTH_SHORT).show();
         Intent intent = new Intent(this, PrivacySecurityActivity.class);
         startActivity(intent);
         finish();
    }

    private void navigateToHelpSupport() {
        Toast.makeText(this, "Help & Support clicked", Toast.LENGTH_SHORT).show();
         Intent intent = new Intent(this, HelpSupportActivity.class);
         startActivity(intent);
         finish();
    }

    private void showLogoutConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Logout", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        logout();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void navigateToDashboard(String userType) {
        if(userType.equals("seller")){
            Intent intent = new Intent(this, SellerDashboardActivity.class);
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
            startActivity(intent);
            finish();
        } else if (userType.equals("charity")) {
            Intent intent = new Intent(this, CharityDashboardActivity.class);
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
            startActivity(intent);
            finish();
        }
    }

    private void navigateToAddListing() {
        Intent intent = new Intent(this, AddNewFoodListingActivity.class);
        startActivity(intent);
        finish();
    }

    private void navigateToReservation() {
        Intent intent = new Intent(this, ReservationsActivity.class);
        startActivity(intent);
        finish();
    }

    private void logout() {
        // Navigate to login screen
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();

        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
    }

    private void setupBackPressHandler() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                FirebaseUser currentUser = mAuth.getCurrentUser();

                if (currentUser == null) {
                    // User is not logged in, handle this case
//                    Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
                    // Optionally navigate to login screen
                    return;
                }

                String userId = currentUser.getUid();
                db.collection("users").document(userId).get().addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String userType = documentSnapshot.getString("user_type");
                        if(userType.equals("seller")){
                            navigateToDashboard(userType);
                            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                        }
                        else if(userType.equals("charity")){
                            navigateToDashboard(userType);
                            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                        }
                    }
                });
            }
        });
    }

    // Update onActivityResult to refresh after editing
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 100 && resultCode == RESULT_OK) {
            // Refresh profile data after editing
            loadProfileDataFromFirestore();
            Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
        }
    }
}