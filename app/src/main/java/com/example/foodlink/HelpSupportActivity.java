package com.example.foodlink;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

public class HelpSupportActivity extends AppCompatActivity {

    private EditText etSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Hide default action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        setContentView(R.layout.activity_help_support);

        initViews();
        setupClickListeners();
        setupFAQItems();
        setupBackPressHandler();
    }

    private void initViews() {
        etSearch = findViewById(R.id.etSearch);

        // Back button
        findViewById(R.id.btnBack).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    private void setupClickListeners() {
        // Search button
//        findViewById(R.id.btnSearch).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                String searchQuery = etSearch.getText().toString().trim();
//                if (!searchQuery.isEmpty()) {
//                    searchFAQ(searchQuery);
//                } else {
//                    Toast.makeText(HelpSupportActivity.this,
//                            "Enter a search term", Toast.LENGTH_SHORT).show();
//                }
//            }
//        });
    }

    private void searchFAQ(String query) {
        // Implement search functionality
        Toast.makeText(this, "Searching for: " + query, Toast.LENGTH_SHORT).show();
        // In a real implementation, you would filter FAQ items
    }

    private void setupFAQItems() {
        LinearLayout faqContainer = findViewById(R.id.faqContainer);

        // Clear any existing views
        faqContainer.removeAllViews();

        // Define FAQ items
        String[] questions = {
                "How do I list surplus food as a seller?",
                "How do charities reserve food?",
                "What happens if a charity needs to cancel a reservation?",
                "How is food safety ensured?",
                "Is there a fee to use Plate for the Planet?",
                "How do I track my environmental impact?",
                "What types of food can be donated?",
                "How do pickup times work?"
        };

        String[] answers = {
                "Go to the 'Add' tab, fill in food details, set pickup time, and publish your listing.",
                "Charities can browse available listings and click 'Reserve' to claim food items.",
                "Charities can cancel up to 2 hours before pickup. Frequent cancellations may affect account status.",
                "All food must follow FDA guidelines. Sellers must provide temperature logs and proper packaging.",
                "No, Plate for the Planet is completely free for both sellers and charities.",
                "Your dashboard shows real-time metrics for food saved, CO2 reduced, and charities helped.",
                "Most non-perishable and properly stored perishable foods are accepted. See our guidelines for details.",
                "Sellers set pickup windows. Charities receive notifications and confirm pickup within the window."
        };

        // Create FAQ items
        for (int i = 0; i < questions.length; i++) {
            addFAQItem(faqContainer, questions[i], answers[i], i);
        }
    }

    private void addFAQItem(LinearLayout container, final String question,
                            final String answer, int index) {
        // Create FAQ card
        CardView faqCard = new CardView(this);
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        cardParams.setMargins(0, 0, 0, dpToPx(8));
        faqCard.setLayoutParams(cardParams);
        faqCard.setCardElevation(dpToPx(2));
        faqCard.setRadius(dpToPx(8));
        faqCard.setCardBackgroundColor(getResources().getColor(R.color.surface_white));
        faqCard.setContentPadding(dpToPx(16), dpToPx(16), dpToPx(16), dpToPx(16));

        // Create inner layout
        LinearLayout innerLayout = new LinearLayout(this);
        innerLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        innerLayout.setOrientation(LinearLayout.VERTICAL);

        // Question row with icon
        LinearLayout questionRow = new LinearLayout(this);
        questionRow.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        questionRow.setOrientation(LinearLayout.HORIZONTAL);
        questionRow.setGravity(android.view.Gravity.CENTER_VERTICAL);

        // FAQ icon
        TextView iconText = new TextView(this);
        iconText.setText("○");
        iconText.setTextSize(18);
        iconText.setTextColor(getResources().getColor(R.color.colorPrimary));
        iconText.setPadding(0, 0, dpToPx(12), 0);

        // Question text
        TextView questionText = new TextView(this);
        LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        textParams.weight = 1;
        questionText.setLayoutParams(textParams);
        questionText.setText(question);
        questionText.setTextSize(16);
        questionText.setTextColor(getResources().getColor(R.color.text_primary));

        // Expand/collapse icon
        final TextView expandIcon = new TextView(this);
        expandIcon.setText("▼");
        expandIcon.setTextSize(14);
        expandIcon.setTextColor(getResources().getColor(R.color.text_secondary));
        expandIcon.setPadding(dpToPx(8), 0, 0, 0);

        // Add views to question row
        questionRow.addView(iconText);
        questionRow.addView(questionText);
        questionRow.addView(expandIcon);

        // Add answer (initially hidden)
        final TextView answerText = new TextView(this);
        answerText.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        answerText.setText(answer);
        answerText.setTextSize(14);
        answerText.setTextColor(getResources().getColor(R.color.text_secondary));
        answerText.setVisibility(View.GONE);
        answerText.setPadding(dpToPx(28), dpToPx(12), 0, 0);

        // Add views to inner layout
        innerLayout.addView(questionRow);
        innerLayout.addView(answerText);
        faqCard.addView(innerLayout);

        // Add click listener to toggle answer
        faqCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (answerText.getVisibility() == View.VISIBLE) {
                    answerText.setVisibility(View.GONE);
                    expandIcon.setText("▼");
                } else {
                    answerText.setVisibility(View.VISIBLE);
                    expandIcon.setText("▲");
                }
            }
        });

        container.addView(faqCard);
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    private void setupBackPressHandler() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                navigateToProfile();
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
            }
        });
    }

    private void navigateToProfile(){
        Intent intent = new Intent(this, ProfileActivity.class);
        startActivity(intent);
        finish();
    }
}