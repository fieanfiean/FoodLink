package com.example.foodlink;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
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
            case "expired":
                statusBgResId = R.drawable.bg_status_cancelled; // Use cancelled style for expired
                break;
            default:
                statusBgResId = R.drawable.bg_status_upcoming;
                break;
        }
        holder.tvStatus.setBackgroundResource(statusBgResId);

        // Load food image - CORRECTED: Moved outside click listener
        if (holder.ivFoodImage != null && holder.tvNoImage != null) {
            String imageBase64 = reservation.getImageBase64();
            if (imageBase64 != null && !imageBase64.isEmpty()) {
                loadFoodImage(imageBase64, holder.ivFoodImage, holder.tvNoImage, reservation.getFoodItem());
            } else {
                holder.ivFoodImage.setImageResource(R.drawable.ic_food);
                holder.tvNoImage.setVisibility(View.VISIBLE);
            }
        }

        // Set button visibility based on status - SAFELY
        if (holder.llActionButtons != null && holder.llCompletedActions != null) {
            if ("completed".equals(reservation.getStatus()) || "cancelled".equals(reservation.getStatus()) || "expired".equals(reservation.getStatus())) {
                holder.llActionButtons.setVisibility(View.GONE);
                holder.llCompletedActions.setVisibility(View.VISIBLE);
            } else {
                holder.llActionButtons.setVisibility(View.VISIBLE);
                holder.llCompletedActions.setVisibility(View.GONE);
            }
        }

        // Set click listeners - SAFELY
        if (holder.btnConfirmPickup != null) {
            holder.btnConfirmPickup.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onConfirmPickupClick(reservation);
                }
            });
        }

        if (holder.btnCancelReservation != null) {
            holder.btnCancelReservation.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onCancelReservationClick(reservation);
                }
            });
        }

        if (holder.btnViewDetails != null) {
            holder.btnViewDetails.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onViewDetailsClick(reservation);
                }
            });
        }

        if (holder.btnReorder != null) {
            holder.btnReorder.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onReorderClick(reservation);
                }
            });
        }
    }

    private void loadFoodImage(String imageBase64, ImageView imageView, TextView tvNoImage, String foodName) {
        if (imageBase64 != null && !imageBase64.trim().isEmpty() && !imageBase64.equals("null")) {
            try {
                // Clean the Base64 string
                String cleanBase64 = imageBase64.trim();
                if (cleanBase64.contains("base64,")) {
                    cleanBase64 = cleanBase64.substring(cleanBase64.indexOf("base64,") + 7);
                }

                // Decode Base64
                byte[] decodedBytes = Base64.decode(cleanBase64, Base64.DEFAULT);

                if (decodedBytes != null && decodedBytes.length > 0) {
                    Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);

                    if (bitmap != null) {
                        imageView.setImageBitmap(bitmap);
                        tvNoImage.setVisibility(View.GONE);
                        return;
                    }
                }
            } catch (Exception e) {
                // Error loading image
            }
        }

        // If image loading failed, show default
        imageView.setImageResource(R.drawable.ic_food);
        tvNoImage.setVisibility(View.VISIBLE);
    }

    @Override
    public int getItemCount() {
        return reservations.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvStatus, tvFoodItem, tvRestaurant, tvQuantity, tvPickupTime, tvExpiryDate;
        Button btnConfirmPickup, btnCancelReservation, btnViewDetails, btnReorder;
        LinearLayout llActionButtons, llCompletedActions;
        ImageView ivFoodImage;  // Added
        TextView tvNoImage;     // Added

        ViewHolder(@NonNull View itemView) {
            super(itemView);

            // Initialize all views with null checks
            try {
                // Image views
                ivFoodImage = itemView.findViewById(R.id.ivFoodImage);
                tvNoImage = itemView.findViewById(R.id.tvNoImage);

                // Status and text views
                tvStatus = itemView.findViewById(R.id.tvStatus);
                tvFoodItem = itemView.findViewById(R.id.tvFoodItem);
                tvRestaurant = itemView.findViewById(R.id.tvRestaurant);
                tvQuantity = itemView.findViewById(R.id.tvQuantity);
                tvPickupTime = itemView.findViewById(R.id.tvPickupTime);
                tvExpiryDate = itemView.findViewById(R.id.tvExpiryDate);

                // Button views
//                btnConfirmPickup = itemView.findViewById(R.id.btnConfirmPickup);
                btnCancelReservation = itemView.findViewById(R.id.btnCancelReservation);
                btnViewDetails = itemView.findViewById(R.id.btnViewDetails);
//                btnReorder = itemView.findViewById(R.id.btnReorder);

                // Layout views
//                llActionButtons = itemView.findViewById(R.id.llActionButtons);
//                llCompletedActions = itemView.findViewById(R.id.llCompletedActions);
            } catch (Exception e) {
                // Handle missing views gracefully
            }
        }
    }
}