// NotificationsActivity.java
package com.example.foodlink;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import com.example.foodlink.Notification;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NotificationsActivity extends AppCompatActivity {

    private LinearLayout llNotificationsContainer;
    private CardView cardEmptyState;
    private TextView tvUnreadCount, tvMarkAll;
    private List<Notification> notifications = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        initViews();
        setupClickListeners();
        loadNotifications();
        setupBackPressHandler();
    }

    private void initViews() {
        llNotificationsContainer = findViewById(R.id.llNotificationsContainer);
        cardEmptyState = findViewById(R.id.cardEmptyState);
        tvUnreadCount = findViewById(R.id.tvUnreadCount);
        tvMarkAll = findViewById(R.id.tvMarkAll);

        findViewById(R.id.btnBack).setOnClickListener(v -> navigateToDashboard());
    }

    private void setupBackPressHandler() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                navigateToDashboard();
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
            }
        });
    }
    private void navigateToDashboard() {
        Toast.makeText(this, "Navigate to Dashboard", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, SellerDashboardActivity.class);
        startActivity(intent);
        finish();
    }

    private void setupClickListeners() {
        tvMarkAll.setOnClickListener(v -> markAllAsRead());
    }

    private void loadNotifications() {
        // For demo purposes, create sample notifications
        // In real app, you would fetch from database/API

        notifications.clear();

        // Sample notifications for seller
        notifications.add(new Notification(
                "seller_001", // userId
                "New Reservation",
                "Hope Community Kitchen reserved your \"Fresh Vegetable Salad Mix\"",
                "reservation",
                "listing_001",
                "charity_001"
        ));

        notifications.add(new Notification(
                "seller_001",
                "Pickup Confirmed",
                "Charity confirmed pickup of \"Assorted Fresh Bread\"",
                "pickup_confirmed",
                "listing_002",
                "charity_002"
        ));

        notifications.add(new Notification(
                "seller_001",
                "Reservation Cancelled",
                "Community Food Bank cancelled reservation for \"Prepared Meals\"",
                "reservation_cancelled",
                "listing_003",
                "charity_003"
        ));

        notifications.add(new Notification(
                "charity_001", // This would be for charity user
                "New Listing Available",
                "Green Leaf Restaurant posted new food items nearby",
                "new_listing",
                "listing_004",
                "seller_001"
        ));

        // Mark first two as unread for demo
        notifications.get(0).setRead(false);
        notifications.get(1).setRead(false);
        notifications.get(2).setRead(true);
        notifications.get(3).setRead(true);

        displayNotifications();
    }

    private void displayNotifications() {
        llNotificationsContainer.removeAllViews();

        int unreadCount = 0;

        for (Notification notification : notifications) {
            if (!notification.isRead()) {
                unreadCount++;
            }
            addNotificationView(notification);
        }

        // Update unread count
        tvUnreadCount.setText(unreadCount + " unread");

        // Show/hide empty state
        if (notifications.isEmpty()) {
            cardEmptyState.setVisibility(View.VISIBLE);
            tvMarkAll.setVisibility(View.GONE);
        } else {
            cardEmptyState.setVisibility(View.GONE);
            tvMarkAll.setVisibility(View.VISIBLE);
        }
    }

    private void addNotificationView(Notification notification) {
        View notificationView = LayoutInflater.from(this)
                .inflate(R.layout.item_notification, llNotificationsContainer, false);

        TextView tvTitle = notificationView.findViewById(R.id.tvNotificationTitle);
        TextView tvMessage = notificationView.findViewById(R.id.tvNotificationMessage);
        TextView tvTime = notificationView.findViewById(R.id.tvNotificationTime);
        View unreadIndicator = notificationView.findViewById(R.id.viewUnreadIndicator);

        // Set notification data
        tvTitle.setText(notification.getTitle());
        tvMessage.setText(notification.getMessage());

        // Format time (for demo, using static times)
        String timeAgo = getTimeAgo(notification.getTimestamp());
        tvTime.setText(timeAgo);

        // Show/hide unread indicator
        unreadIndicator.setVisibility(notification.isRead() ? View.GONE : View.VISIBLE);

        // Set different icon based on notification type
        // You would set different icons here based on notification.getType()

        // Set click listener
        notificationView.setOnClickListener(v -> {
            if (!notification.isRead()) {
                notification.setRead(true);
                markNotificationAsRead(notification);
                unreadIndicator.setVisibility(View.GONE);
                updateUnreadCount();
            }
            // Handle notification click (e.g., navigate to listing)
            onNotificationClicked(notification);
        });

        llNotificationsContainer.addView(notificationView);
    }

    private void markNotificationAsRead(Notification notification) {
        // In real app, update in database/API
        Toast.makeText(this, "Marked as read", Toast.LENGTH_SHORT).show();
    }

    private void markAllAsRead() {
        for (Notification notification : notifications) {
            notification.setRead(true);
        }
        displayNotifications();
        Toast.makeText(this, "All notifications marked as read", Toast.LENGTH_SHORT).show();
    }

    private void updateUnreadCount() {
        int unreadCount = 0;
        for (Notification notification : notifications) {
            if (!notification.isRead()) {
                unreadCount++;
            }
        }
        tvUnreadCount.setText(unreadCount + " unread");
    }

    private void onNotificationClicked(Notification notification) {
        // Handle notification click based on type
        switch (notification.getType()) {
            case "new_listing":
                // Navigate to listing details
                Toast.makeText(this, "View new listing", Toast.LENGTH_SHORT).show();
                break;
            case "reservation":
                // Navigate to reservation details
                Toast.makeText(this, "View reservation", Toast.LENGTH_SHORT).show();
                break;
            case "pickup_confirmed":
                // Navigate to completed listing
                Toast.makeText(this, "View pickup confirmation", Toast.LENGTH_SHORT).show();
                break;
            case "reservation_cancelled":
                // Navigate to listing
                Toast.makeText(this, "View cancelled reservation", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    private String getTimeAgo(Date date) {
        // Simple time ago calculation (for demo)
        long diff = new Date().getTime() - date.getTime();
        long seconds = diff / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;

        if (days > 0) {
            return days + " day" + (days > 1 ? "s" : "") + " ago";
        } else if (hours > 0) {
            return hours + " hour" + (hours > 1 ? "s" : "") + " ago";
        } else if (minutes > 0) {
            return minutes + " minute" + (minutes > 1 ? "s" : "") + " ago";
        } else {
            return "Just now";
        }
    }
}