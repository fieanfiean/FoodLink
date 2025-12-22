package com.example.foodlink;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminListingsAdapter extends RecyclerView.Adapter<AdminListingsAdapter.ListingVH> {

    private final List<AdminListing> listings;
    private final FirebaseFirestore db;
    private final String listingsCollection;

    public AdminListingsAdapter(List<AdminListing> listings, FirebaseFirestore db, String listingsCollection) {
        this.listings = listings;
        this.db = db;
        this.listingsCollection = listingsCollection;
    }

    @NonNull
    @Override
    public ListingVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_listing, parent, false);
        return new ListingVH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ListingVH h, int position) {
        AdminListing l = listings.get(position);

        h.tvFoodName.setText(
                (l.foodName != null && !l.foodName.trim().isEmpty())
                        ? l.foodName
                        : "Unknown food"
        );

        String qty = (l.quantity == null) ? "-" : String.valueOf(l.quantity);
        String details =
                "Category: " + safe(l.category) +
                        " | Qty: " + qty +
                        " | Status: " + safe(l.status) +
                        "\nExpiry: " + safe(l.expiryDate) +
                        " | Window: " + safe(l.startTime) + " - " + safe(l.endTime);

        h.tvDetails.setText(details);

        String created = (l.createdAt == null) ? "-" : String.valueOf(l.createdAt);
        h.tvOwner.setText(
                "Seller: " + safe(l.sellerName) + " | Created: " + created
        );

        h.btnView.setOnClickListener(v -> showDetailsDialog(v.getContext(), l));

        h.btnSetStatus.setOnClickListener(v -> showStatusPicker(v.getContext(), l, position));

        h.btnDelete.setOnClickListener(v -> confirmDelete(v.getContext(), l, position));
    }

    @Override
    public int getItemCount() {
        return listings.size();
    }

    private void showDetailsDialog(Context ctx, AdminListing l) {
        StringBuilder sb = new StringBuilder();
        sb.append("Food: ").append(safe(l.foodName)).append("\n");
        sb.append("Category: ").append(safe(l.category)).append("\n");
        sb.append("Description: ").append(safe(l.description)).append("\n");
        sb.append("Additional: ").append(safe(l.additionalDetails)).append("\n\n");

        sb.append("Quantity: ").append(l.quantity == null ? "-" : l.quantity).append("\n");
        sb.append("Status: ").append(safe(l.status)).append("\n");
        sb.append("Expiry: ").append(safe(l.expiryDate)).append("\n");
        sb.append("Start: ").append(safe(l.startTime)).append("\n");
        sb.append("End: ").append(safe(l.endTime)).append("\n\n");

        sb.append("Seller Name: ").append(safe(l.sellerName)).append("\n");
        sb.append("Seller Email: ").append(safe(l.sellerEmail)).append("\n");
        sb.append("Seller ID: ").append(safe(l.sellerId)).append("\n\n");

        sb.append("Reserved By: ").append(safe(l.reservedBy)).append("\n");
        sb.append("Reserved Charity: ").append(safe(l.reservedCharity)).append("\n");
        sb.append("Pickup Date: ").append(safe(l.pickupDate)).append("\n");

        new AlertDialog.Builder(ctx)
                .setTitle("Listing Details")
                .setMessage(sb.toString())
                .setPositiveButton("OK", null)
                .show();
    }

    private void showStatusPicker(Context ctx, AdminListing l, int position) {
        String[] statuses = new String[]{"available", "reserved", "picked_up", "expired"};

        new AlertDialog.Builder(ctx)
                .setTitle("Set Listing Status")
                .setItems(statuses, (dialog, which) -> {
                    String newStatus = statuses[which];

                    Map<String, Object> updates = new HashMap<>();
                    updates.put("status", newStatus);
                    updates.put("updated_at", FieldValue.serverTimestamp());

                    // If admin sets to available, clear reservation fields
                    if ("available".equalsIgnoreCase(newStatus)) {
                        updates.put("reserved_by", "");
                        updates.put("reserved_charity", "");
                        updates.put("pickup_charity", "");
                        updates.put("pick_up_by", "");
                        updates.put("pickup_date", "");
                    }

                    db.collection(listingsCollection).document(l.listingId)
                            .update(updates)
                            .addOnSuccessListener(aVoid -> {
                                l.status = newStatus;

                                if ("available".equalsIgnoreCase(newStatus)) {
                                    l.reservedBy = "";
                                    l.reservedCharity = "";
                                    l.pickupDate = "";
                                }

                                notifyItemChanged(position);
                                Toast.makeText(ctx, "Status updated to " + newStatus, Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(ctx, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                            );
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void confirmDelete(Context ctx, AdminListing l, int position) {
        new AlertDialog.Builder(ctx)
                .setTitle("Delete Listing?")
                .setMessage("This will permanently delete the listing:\n" + safe(l.foodName))
                .setPositiveButton("Delete", (d, w) -> {
                    db.collection(listingsCollection).document(l.listingId)
                            .delete()
                            .addOnSuccessListener(aVoid -> {
                                listings.remove(position);
                                notifyItemRemoved(position);
                                Toast.makeText(ctx, "Listing deleted", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(ctx, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                            );
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    static class ListingVH extends RecyclerView.ViewHolder {
        TextView tvFoodName, tvDetails, tvOwner;
        Button btnView, btnSetStatus, btnDelete;

        public ListingVH(@NonNull View itemView) {
            super(itemView);
            tvFoodName = itemView.findViewById(R.id.tvListingFoodName);
            tvDetails = itemView.findViewById(R.id.tvListingDetails);
            tvOwner = itemView.findViewById(R.id.tvListingOwner);

            btnView = itemView.findViewById(R.id.btnListingView);
            btnSetStatus = itemView.findViewById(R.id.btnListingSetStatus);
            btnDelete = itemView.findViewById(R.id.btnListingDelete);
        }
    }

    private String safe(String s) {
        return (s == null || s.trim().isEmpty()) ? "-" : s;
    }
}
