package com.example.foodlink;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class AdminDashboardActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigation;
    private RecyclerView recyclerView;

    private FirebaseFirestore db;

    private UsersAdapter usersAdapter;
    private AdminListingsAdapter listingsAdapter;
    private AdminReservationsAdapter reservationsAdapter;

    private final List<AdminUser> users = new ArrayList<>();
    private final List<AdminListing> listings = new ArrayList<>();
    private final List<AdminReservation> reservations = new ArrayList<>();

    private static final String COL_USERS = "users";
    private static final String COL_LISTINGS = "food_listings";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        // attach toolbar so menu (Logout) can appear
        Toolbar toolbar = findViewById(R.id.adminToolbar);
        setSupportActionBar(toolbar);

        db = FirebaseFirestore.getInstance();

        bottomNavigation = findViewById(R.id.bottomNavigationAdmin);
        recyclerView = findViewById(R.id.recyclerAdmin);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        usersAdapter = new UsersAdapter(users, db, COL_USERS);

        // pass db + collection so admin can delete / update status
        listingsAdapter = new AdminListingsAdapter(listings, db, COL_LISTINGS);

        // pass db + collection so admin can cancel reservation
        reservationsAdapter = new AdminReservationsAdapter(reservations, db, COL_LISTINGS);

        setupBottomNavigation();

        bottomNavigation.setSelectedItemId(R.id.nav_admin_users);
    }

    // Admin menu (Logout)
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_admin, menu);
        return true;
    }

    // Handle Logout click
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            FirebaseAuth.getInstance().signOut();

            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupBottomNavigation() {
        bottomNavigation.getMenu().clear();

        bottomNavigation.getMenu().add(Menu.NONE, R.id.nav_admin_users, 1, "Users")
                .setIcon(R.drawable.ic_profile);

        bottomNavigation.getMenu().add(Menu.NONE, R.id.nav_admin_listings, 2, "Listings")
                .setIcon(R.drawable.ic_dashboard);

        bottomNavigation.getMenu().add(Menu.NONE, R.id.nav_admin_reservations, 3, "Reservations")
                .setIcon(R.drawable.ic_reservation);

        bottomNavigation.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();

                if (id == R.id.nav_admin_users) {
                    showUsers();
                    return true;
                } else if (id == R.id.nav_admin_listings) {
                    showListings();
                    return true;
                } else if (id == R.id.nav_admin_reservations) {
                    showReservations();
                    return true;
                }
                return false;
            }
        });
    }

    private void showUsers() {
        recyclerView.setAdapter(usersAdapter);
        loadUsers();
    }

    private void showListings() {
        recyclerView.setAdapter(listingsAdapter);
        loadListings();
    }

    private void showReservations() {
        recyclerView.setAdapter(reservationsAdapter);
        loadReservations();
    }

    private void loadUsers() {
        users.clear();
        db.collection(COL_USERS)
                .orderBy("user_type", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(qs -> {
                    for (var doc : qs.getDocuments()) {
                        AdminUser u = AdminUser.fromDoc(doc);
                        if (u != null) users.add(u);
                    }
                    usersAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to load users", Toast.LENGTH_SHORT).show()
                );
    }

    private void loadListings() {
        listings.clear();
        db.collection(COL_LISTINGS)
                .orderBy("created_at", Query.Direction.DESCENDING)
                .limit(200)
                .get()
                .addOnSuccessListener(qs -> {
                    for (var doc : qs.getDocuments()) {
                        AdminListing l = AdminListing.fromDoc(doc);
                        if (l != null) listings.add(l);
                    }
                    listingsAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to load listings", Toast.LENGTH_SHORT).show()
                );
    }

    private void loadReservations() {
        reservations.clear();
        db.collection(COL_LISTINGS)
                .whereEqualTo("status", "reserved")
                .get()
                .addOnSuccessListener(qs -> {
                    for (var doc : qs.getDocuments()) {
                        AdminReservation r = AdminReservation.fromDoc(doc);
                        if (r != null) reservations.add(r);
                    }
                    reservationsAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to load reservations", Toast.LENGTH_SHORT).show()
                );
    }
}
