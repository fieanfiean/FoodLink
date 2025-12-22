package com.example.foodlink;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.UserVH> {

    private final List<AdminUser> users;
    private final FirebaseFirestore db;
    private final String usersCollection;

    public UsersAdapter(List<AdminUser> users, FirebaseFirestore db, String usersCollection) {
        this.users = users;
        this.db = db;
        this.usersCollection = usersCollection;
    }

    @NonNull
    @Override
    public UserVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_user, parent, false);
        return new UserVH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull UserVH h, int position) {
        AdminUser u = users.get(position);

        String name = (u.fullName != null && !u.fullName.trim().isEmpty()) ? u.fullName : "Unknown";
        String email = (u.email != null && !u.email.trim().isEmpty()) ? u.email : "-";

        boolean isActive = (u.isActive == null) ? true : u.isActive;

        h.tvName.setText(name);
        h.tvEmail.setText(email);
        h.tvRoleStatus.setText("Type: " + safe(u.userType) + " | Active: " + (isActive ? "true" : "false"));

        h.btnToggle.setText(isActive ? "Suspend" : "Activate");

        h.btnToggle.setOnClickListener(v -> {
            boolean newActive = !isActive;

            db.collection(usersCollection).document(u.uid)
                    .update("is_active", newActive)
                    .addOnSuccessListener(aVoid -> {
                        u.isActive = newActive;
                        notifyItemChanged(position);
                        Toast.makeText(v.getContext(),
                                "Updated is_active to " + newActive,
                                Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(v.getContext(),
                                    "Failed: " + e.getMessage(),
                                    Toast.LENGTH_SHORT).show()
                    );
        });
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    static class UserVH extends RecyclerView.ViewHolder {
        TextView tvName, tvEmail, tvRoleStatus;
        Button btnToggle;

        public UserVH(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvUserName);
            tvEmail = itemView.findViewById(R.id.tvUserEmail);
            tvRoleStatus = itemView.findViewById(R.id.tvUserRoleStatus);
            btnToggle = itemView.findViewById(R.id.btnToggleStatus);
        }
    }

    private String safe(String s) {
        return s == null ? "-" : s;
    }
}
