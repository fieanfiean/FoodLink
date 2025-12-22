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

public class AdminReservationsAdapter extends RecyclerView.Adapter<AdminReservationsAdapter.ResVH> {

    private final List<AdminReservation> reservations;
    private final FirebaseFirestore db;
    private final String listingsCollection;

    public AdminReservationsAdapter(List<AdminReservation> reservations, FirebaseFirestore db, String listingsCollection) {
        this.reservations = reservations;
        this.db = db;
        this.listingsCollection = listingsCollection;
    }

    @NonNull
    @Override
    public ResVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_reservation, parent, false);
        return new ResVH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ResVH h, int position) {
        AdminReservation r = reservations.get(position);

        String charity = safe(r.reservedCharity);
        String seller = safe(r.sellerName);
        String pickupDate = safe(r.pickupDate);
        String qty = (r.quantity == null) ? "-" : String.valueOf(r.quantity);

        h.tvTitle.setText("Reservation");
        h.tvDetails.setText(
                "Charity: " + charity +
                        " | Seller: " + seller +
                        " | Pickup: " + pickupDate +
                        " | Qty: " + qty
        );
        h.tvStatus.setText("Status: " + safe(r.status));

        h.btnCancel.setOnClickListener(v -> confirmCancel(v.getContext(), r, position));
    }

    private void confirmCancel(Context ctx, AdminReservation r, int position) {
        new AlertDialog.Builder(ctx)
                .setTitle("Cancel Reservation?")
                .setMessage("This will set the listing back to AVAILABLE and clear reservation fields.")
                .setPositiveButton("Cancel Reservation", (d, w) -> cancelReservation(ctx, r, position))
                .setNegativeButton("Close", null)
                .show();
    }

    private void cancelReservation(Context ctx, AdminReservation r, int position) {

        String listingDocId = r.reservationId;

        Map<String, Object> updates = new HashMap<>();
        updates.put("status", "available");
        updates.put("reserved_by", "");
        updates.put("reserved_charity", "");
        updates.put("pickup_charity", "");
        updates.put("pick_up_by", "");
        updates.put("pickup_date", "");
        updates.put("updated_at", FieldValue.serverTimestamp());

        db.collection(listingsCollection).document(listingDocId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    reservations.remove(position);
                    notifyItemRemoved(position);
                    Toast.makeText(ctx, "Reservation cancelled", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(ctx, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    @Override
    public int getItemCount() {
        return reservations.size();
    }

    static class ResVH extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDetails, tvStatus;
        Button btnCancel;

        public ResVH(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvReservationTitle);
            tvDetails = itemView.findViewById(R.id.tvReservationDetails);
            tvStatus = itemView.findViewById(R.id.tvReservationStatus);
            btnCancel = itemView.findViewById(R.id.btnCancelReservation);
        }
    }

    private String safe(String s) {
        return (s == null || s.trim().isEmpty()) ? "-" : s;
    }
}
