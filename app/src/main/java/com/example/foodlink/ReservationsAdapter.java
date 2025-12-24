package com.example.foodlink;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;
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

        Log.d("ReservationsDebug", "Position: " + position +
                " | Food: " + reservation.getFoodItem() +
                " | Status: " + reservation.getStatus() +
                " | Status lower: " + reservation.getStatus().toLowerCase());

        // Set data
        holder.tvFoodItem.setText(reservation.getFoodItem());
        holder.tvRestaurant.setText(reservation.getRestaurant());
        holder.tvQuantity.setText(reservation.getQuantity());
        holder.tvPickupTime.setText("Pickup: " + reservation.getPickupTime());
        holder.tvExpiryDate.setText("Expires: " + reservation.getExpiryDate());
        holder.tvStatus.setText(reservation.getStatus());

        // Set status background
        int statusBgResId = R.drawable.bg_status_upcoming;
        String status = reservation.getStatus().toLowerCase();

        switch (status) {
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
                statusBgResId = R.drawable.bg_status_cancelled;
                break;
            default:
                statusBgResId = R.drawable.bg_status_upcoming;
                break;
        }
        holder.tvStatus.setBackgroundResource(statusBgResId);

        // Load food image
        if (holder.ivFoodImage != null && holder.tvNoImage != null) {
            String imageBase64 = reservation.getImageBase64();
            if (imageBase64 != null && !imageBase64.isEmpty()) {
                loadFoodImage(imageBase64, holder.ivFoodImage, holder.tvNoImage, reservation.getFoodItem());
            } else {
                holder.ivFoodImage.setImageResource(R.drawable.ic_food);
                holder.tvNoImage.setVisibility(View.VISIBLE);
            }
        }

        // Set button visibility based on status - SIMPLIFIED
        Log.d("ReservationsDebug", "Setting visibility for status: " + status);

        // First, hide buttons directly
        if ("completed".equals(status)) {
            Log.d("ReservationsDebug", "HIDING buttons for completed status");

            // Hide buttons individually
            if (holder.btnConfirmPickup != null) {
                holder.btnConfirmPickup.setVisibility(View.GONE);
            }
            if (holder.btnCancelReservation != null) {
                holder.btnCancelReservation.setVisibility(View.GONE);
            }

            // Hide the layout too
            if (holder.llActionButtons != null) {
                holder.llActionButtons.setVisibility(View.GONE);
            }

            // Hide completed actions
            if (holder.llCompletedActions != null) {
                holder.llCompletedActions.setVisibility(View.GONE);
            }
        } else {
            Log.d("ReservationsDebug", "SHOWING buttons for status: " + status);

            // Show buttons
            if (holder.btnConfirmPickup != null) {
                holder.btnConfirmPickup.setVisibility(View.VISIBLE);
            }
            if (holder.btnCancelReservation != null) {
                holder.btnCancelReservation.setVisibility(View.VISIBLE);
            }

            // Show the layout
            if (holder.llActionButtons != null) {
                holder.llActionButtons.setVisibility(View.VISIBLE);
            }

            // Handle other statuses
            if ("cancelled".equals(status) || "expired".equals(status)) {
                if (holder.llActionButtons != null) {
                    holder.llActionButtons.setVisibility(View.GONE);
                }
                if (holder.llCompletedActions != null) {
                    holder.llCompletedActions.setVisibility(View.VISIBLE);
                }
            } else {
                if (holder.llActionButtons != null) {
                    holder.llActionButtons.setVisibility(View.VISIBLE);
                }
                if (holder.llCompletedActions != null) {
                    holder.llCompletedActions.setVisibility(View.GONE);
                }
            }
        }

        // Set click listeners
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
        ImageView ivFoodImage;
        TextView tvNoImage;

        ViewHolder(@NonNull View itemView) {
            super(itemView);

            // Initialize all views with debugging
            Log.d("ViewHolderDebug", "Initializing ViewHolder...");

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
            btnConfirmPickup = itemView.findViewById(R.id.btnConfirmPickup);
            btnCancelReservation = itemView.findViewById(R.id.btnCancelReservation);
            btnViewDetails = itemView.findViewById(R.id.btnViewDetails);
            btnReorder = itemView.findViewById(R.id.btnReorder);

            // Layout views
            llActionButtons = itemView.findViewById(R.id.llActionButtons);
            llCompletedActions = itemView.findViewById(R.id.llCompletedActions);

            // Log which views were found
            Log.d("ViewHolderDebug", "btnConfirmPickup found: " + (btnConfirmPickup != null));
            Log.d("ViewHolderDebug", "btnCancelReservation found: " + (btnCancelReservation != null));
            Log.d("ViewHolderDebug", "llActionButtons found: " + (llActionButtons != null));
            Log.d("ViewHolderDebug", "llCompletedActions found: " + (llCompletedActions != null));
        }
    }
}