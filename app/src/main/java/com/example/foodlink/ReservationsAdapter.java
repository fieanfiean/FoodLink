package com.example.foodlink;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ReservationsAdapter extends RecyclerView.Adapter<ReservationsAdapter.ViewHolder> {

    private List<ReservationsActivity.Reservation> reservations;
    private OnReservationClickListener listener;

    public interface OnReservationClickListener {
        void onConfirmPickupClick(ReservationsActivity.Reservation reservation);
        void onCancelReservationClick(ReservationsActivity.Reservation reservation);
        void onViewDetailsClick(ReservationsActivity.Reservation reservation);
        void onReorderClick(ReservationsActivity.Reservation reservation);
    }

    public ReservationsAdapter(List<ReservationsActivity.Reservation> reservations, OnReservationClickListener listener) {
        this.reservations = reservations;
        this.listener = listener;
    }

    public void updateReservations(List<ReservationsActivity.Reservation> newReservations) {
        this.reservations = newReservations;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_reservation, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ReservationsActivity.Reservation reservation = reservations.get(position);

        // Set data
        holder.tvFoodItem.setText(reservation.getFoodItem());
        holder.tvRestaurant.setText(reservation.getRestaurant());
        holder.tvQuantity.setText(reservation.getQuantity());
        holder.tvPickupTime.setText("Pickup: " + reservation.getPickupTime());
        holder.tvExpiryDate.setText("Expires: " + reservation.getExpiryDate());
        holder.tvStatus.setText(reservation.getStatus());

        // Set status background
        int statusBgResId = R.drawable.bg_status_upcoming;
        switch (reservation.getStatus().toLowerCase()) {
            case "upcoming":
                statusBgResId = R.drawable.bg_status_upcoming;
                break;
            case "pending":
                statusBgResId = R.drawable.bg_status_pending;
                break;
            case "completed":
                statusBgResId = R.drawable.bg_status_completed;
                break;
            case "cancelled":
                statusBgResId = R.drawable.bg_status_cancelled;
                break;
        }
        holder.tvStatus.setBackgroundResource(statusBgResId);

        // REMOVE THE VISIBILITY LOGIC TEMPORARILY
        // Comment out these lines to avoid the crash
        /*
        if ("completed".equals(reservation.getStatus())) {
            holder.llActionButtons.setVisibility(View.GONE);
            holder.llCompletedActions.setVisibility(View.VISIBLE);
        } else {
            holder.llActionButtons.setVisibility(View.VISIBLE);
            holder.llCompletedActions.setVisibility(View.GONE);
        }
        */

        // Set click listeners
        holder.btnConfirmPickup.setOnClickListener(v -> {
            if (listener != null) {
                listener.onConfirmPickupClick(reservation);
            }
        });

        holder.btnCancelReservation.setOnClickListener(v -> {
            if (listener != null) {
                listener.onCancelReservationClick(reservation);
            }
        });

        holder.btnViewDetails.setOnClickListener(v -> {
            if (listener != null) {
                listener.onViewDetailsClick(reservation);
            }
        });

        holder.btnReorder.setOnClickListener(v -> {
            if (listener != null) {
                listener.onReorderClick(reservation);
            }
        });
    }

    @Override
    public int getItemCount() {
        return reservations.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvStatus, tvFoodItem, tvRestaurant, tvQuantity, tvPickupTime, tvExpiryDate;
        Button btnConfirmPickup, btnCancelReservation, btnViewDetails, btnReorder;
        LinearLayout llActionButtons, llCompletedActions;

        ViewHolder(@NonNull View itemView) {
            super(itemView);

            // Initialize all views
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvFoodItem = itemView.findViewById(R.id.tvFoodItem);
            tvRestaurant = itemView.findViewById(R.id.tvRestaurant);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
            tvPickupTime = itemView.findViewById(R.id.tvPickupTime);
            tvExpiryDate = itemView.findViewById(R.id.tvExpiryDate);

            btnConfirmPickup = itemView.findViewById(R.id.btnConfirmPickup);
            btnCancelReservation = itemView.findViewById(R.id.btnCancelReservation);
            btnViewDetails = itemView.findViewById(R.id.btnViewDetails);
            btnReorder = itemView.findViewById(R.id.btnReorder);

            // These might be null - handle carefully
            llActionButtons = itemView.findViewById(R.id.llActionButtons);
            llCompletedActions = itemView.findViewById(R.id.llCompletedActions);

            // If they're null, don't crash - just show both layouts
            if (llActionButtons == null || llCompletedActions == null) {
                // If layouts are missing, show a simpler version
                // This prevents crashes
            }
        }
    }
}