package com.example.kidapp.Activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.gridlayout.widget.GridLayout;
import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.example.kidapp.R;
import com.example.kidapp.models.FlipCard;
import com.example.kidapp.models.FlipCardLevel;
import com.example.kidapp.ViewModel.UserViewModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GameLatTheActivity extends AppCompatActivity {

    // UI Elements
    private TextView tvScore;
    private TextView tvTimer;
    private GridLayout gridCards;
    private Button btnRestart;

    // Game variables
    private int score = 0;
    private CountDownTimer timer;
    private long timeElapsed = 0;
    private List<CardItem> cardItems;
    private List<View> cardViews;
    private int firstCardPosition = -1;
    private int secondCardPosition = -1;
    private boolean isProcessing = false;
    private boolean gameActive = false;

    // Game data from level
    private FlipCardLevel currentLevel;

    private UserViewModel userViewModel;
    private String userEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_lat_the);

        // Get the level data from intent
        currentLevel = (FlipCardLevel) getIntent().getSerializableExtra("level");
        if (currentLevel == null || currentLevel.getCards() == null || currentLevel.getCards().isEmpty()) {
            Toast.makeText(this, "Level data is missing", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize UI elements
        tvScore = findViewById(R.id.tvScore);
        tvTimer = findViewById(R.id.tvTimer);
        gridCards = findViewById(R.id.gridCards);
        btnRestart = findViewById(R.id.btnRestart);

        // Set up button click listener
        btnRestart.setOnClickListener(v -> restartGame());

        // Initialize the game
        initializeGame();

        // Back button
        ImageView btnBack = findViewById(R.id.btn_Back);
        btnBack.setOnClickListener(v -> finish());

        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            userEmail = currentUser.getEmail();
        }
    }

    private void initializeGame() {
        // Reset variables
        score = 0;
        tvScore.setText("Điểm: " + score);
        timeElapsed = 0;
        tvTimer.setText("Thời gian: 00:00");
        firstCardPosition = -1;
        secondCardPosition = -1;
        isProcessing = false;
        gameActive = true;

        // Create and shuffle the card items
        cardItems = createCardItems();
        Collections.shuffle(cardItems);

        // Create the card views
        createCardViews();

        // Start the timer
        startTimer();
    }

    private List<CardItem> createCardItems() {
        List<CardItem> items = new ArrayList<>();
        List<FlipCard> flipCards = currentLevel.getCards();

        // Create pairs of card items
        for (int i = 0; i < flipCards.size(); i++) {
            FlipCard flipCard = flipCards.get(i);
            // Create two identical cards for each pair
            CardItem card1 = new CardItem(flipCard.getCardImageUrl(), flipCard.getCardText(), i);
            CardItem card2 = new CardItem(flipCard.getCardImageUrl(), flipCard.getCardText(), i);

            items.add(card1);
            items.add(card2);
        }

        return items;
    }

    private void createCardViews() {
        // Clear any existing views
        gridCards.removeAllViews();
        cardViews = new ArrayList<>();

        // Calculate number of columns based on card count
        int columns = (int) Math.ceil(Math.sqrt(cardItems.size()));
        gridCards.setColumnCount(columns);

        // Create card views for each card item
        for (int i = 0; i < cardItems.size(); i++) {
            CardView cardView = new CardView(this);
            cardView.setCardItem(cardItems.get(i));

            final int position = i;
            cardView.setOnClickListener(v -> onCardClick(position));

            // Add to grid
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = 0;
            params.height = 0;
            params.columnSpec = GridLayout.spec(i % columns, 1f);
            params.rowSpec = GridLayout.spec(i / columns, 1f);
            params.setMargins(8, 8, 8, 8);

            cardView.setLayoutParams(params);
            gridCards.addView(cardView);
            cardViews.add(cardView);
        }
    }

    private void onCardClick(int position) {
        // Ignore clicks if game is not active or processing other cards
        if (!gameActive || isProcessing) {
            return;
        }

        // Ignore click if card is already flipped or same card clicked twice
        CardView cardView = (CardView) cardViews.get(position);
        if (cardView.isFlipped() || position == firstCardPosition) {
            return;
        }

        // Flip the card
        cardView.flip();

        // Handle first or second card click
        if (firstCardPosition == -1) {
            // First card flipped
            firstCardPosition = position;
        } else {
            // Second card flipped
            secondCardPosition = position;

            // Check for match
            checkForMatch();
        }
    }

    private void checkForMatch() {
        isProcessing = true;

        // Get the card views
        CardView firstCardView = (CardView) cardViews.get(firstCardPosition);
        CardView secondCardView = (CardView) cardViews.get(secondCardPosition);

        // Get the card items
        CardItem firstCard = cardItems.get(firstCardPosition);
        CardItem secondCard = cardItems.get(secondCardPosition);

        // Check if the pair matches (same value)
        if (firstCard.getValue() == secondCard.getValue()) {
            // Match found
            score += 20;

            // Update score text
            tvScore.setText("Điểm: " + score);

            // Keep cards flipped
            new Handler().postDelayed(() -> {
                firstCardView.setMatched(true);
                secondCardView.setMatched(true);

                // Reset selection
                firstCardPosition = -1;
                secondCardPosition = -1;
                isProcessing = false;

                // Check if game is complete
                checkGameComplete();
            }, 500);
        } else {
            // No match found
            if (score >= 10) {
                score -= 10;
            } else {
                score = 0;
            }

            // Update score text
            tvScore.setText("Điểm: " + score);

            // Flip cards back after delay
            new Handler().postDelayed(() -> {
                firstCardView.flipBack();
                secondCardView.flipBack();

                // Reset selection
                firstCardPosition = -1;
                secondCardPosition = -1;
                isProcessing = false;
            }, 1000);
        }
    }

    private void checkGameComplete() {
        // Check if all cards are matched
        boolean allMatched = true;
        for (View view : cardViews) {
            CardView cardView = (CardView) view;
            if (!cardView.isMatched()) {
                allMatched = false;
                break;
            }
        }

        if (allMatched) {
            // Game complete
            gameActive = false;

            // Stop timer
            if (timer != null) {
                timer.cancel();
            }

            // Cập nhật tiến trình game cho user
            Log.d("GameLatTheActivity", "Cập nhật tiến trình game cho user: " + userEmail);
            Log.d("GameLatTheActivity", "Current level ID: " + currentLevel.getId());
            if (userEmail != null && currentLevel != null) {
                userViewModel.updateGameProgress(
                    userEmail,
                    "flipcard",
                    currentLevel.getId(),
                    score
                );
            }

            // Show congratulations message
            Toast.makeText(this, "Chúc mừng! Bạn đã hoàn thành trò chơi!", Toast.LENGTH_LONG).show();

            // Cập nhật achievements nếu đủ điều kiện
            if (userEmail != null) {
                userViewModel.getUserByEmail(userEmail).observe(this, user -> {
                    if (user != null) {
                        final int soManHoanThanh;
                        if (user.getGameProgress() != null && user.getGameProgress().containsKey("flipcard")) {
                            soManHoanThanh = user.getGameProgress().get("flipcard").getLevelReached();
                            Log.d("GameLatTheActivity", "Level reached: " + soManHoanThanh);
                        } else {
                            soManHoanThanh = 0;
                        }
                        final List<String> achievements;
                        if (user.getAchievements() == null) {
                            achievements = new ArrayList<>();
                        } else {
                            achievements = user.getAchievements();
                        }

                        FirebaseFirestore db = FirebaseFirestore.getInstance();
                        db.collection("achievement")
                                .get()
                                .addOnCompleteListener(task -> {
                                    final boolean[] changed = {false};
                                    final List<String> newAchievements = new ArrayList<>();
                                    if (task.isSuccessful()) {
                                        for (QueryDocumentSnapshot document : task.getResult()) {
                                            String id = document.getString("id");
                                            String name = document.getString("name");
                                            Long minLevelLong = document.getLong("minGameLevel");
                                            String imageUrl = document.getString("imageUrl");
                                            if (minLevelLong != null && id != null && id.startsWith("flipcard")) {
                                                int minLevel = minLevelLong.intValue();
                                                if (soManHoanThanh >= minLevel && !achievements.contains(id)) {
                                                    achievements.add(id);
                                                    newAchievements.add(name != null ? name : id);
                                                    changed[0] = true;
                                                    // Hiện animation và ảnh thành tựu
                                                    runOnUiThread(() -> {
                                                        LottieAnimationView achievementAnimationView = findViewById(R.id.achievementAnimationView);
                                                        ImageView achievementImageView = findViewById(R.id.achievementImageView);
                                                        if (achievementImageView != null) {
                                                            achievementImageView.setVisibility(View.GONE);
                                                        }
                                                        if (achievementAnimationView != null) {
                                                            achievementAnimationView.setAnimation(R.raw.achievement_animation);
                                                            achievementAnimationView.setVisibility(View.VISIBLE);
                                                            achievementAnimationView.playAnimation();

                                                            achievementAnimationView.addAnimatorListener(new android.animation.Animator.AnimatorListener() {
                                                                @Override
                                                                public void onAnimationStart(android.animation.Animator animation) {}

                                                                @Override
                                                                public void onAnimationEnd(android.animation.Animator animation) {
                                                                    achievementAnimationView.setVisibility(View.GONE);
                                                                    achievementImageView.setVisibility(View.GONE);
                                                                }

                                                                @Override
                                                                public void onAnimationCancel(android.animation.Animator animation) {}

                                                                @Override
                                                                public void onAnimationRepeat(android.animation.Animator animation) {}
                                                            });
                                                        }
                                                        if (achievementImageView != null && imageUrl != null && !imageUrl.isEmpty()) {
                                                            // Chỉ hiển thị ImageView nếu ảnh tải thành công
                                                            Picasso.get().load(imageUrl).into(achievementImageView, new com.squareup.picasso.Callback() {
                                                                @Override
                                                                public void onSuccess() {
                                                                    achievementImageView.setVisibility(View.VISIBLE);
                                                                    Log.d("Achievement", "Flip Card: Image loaded successfully.");
                                                                }

                                                                @Override
                                                                public void onError(Exception e) {
                                                                    Log.e("Achievement", "Flip Card: Error loading image: " + e.getMessage());
                                                                }
                                                            });
                                                        }
                                                    });
                                                }
                                            }
                                        }
                                        if (changed[0]) {
                                            userViewModel.updateAchievements(userEmail, achievements);
                                            for (String achv : newAchievements) {
                                                Toast.makeText(this, "Bạn vừa nhận được thành tựu: " + achv, Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    }
                                });
                    }
                });
            }
        }
    }

    private void startTimer() {
        // Cancel any existing timer
        if (timer != null) {
            timer.cancel();
        }

        // Create new timer (updates every second)
        timer = new CountDownTimer(Long.MAX_VALUE, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeElapsed += 1000;
                updateTimerText();
            }

            @Override
            public void onFinish() {
                // Not used
            }
        };

        // Start the timer
        timer.start();
    }

    private void updateTimerText() {
        // Convert time to minutes and seconds
        long seconds = (timeElapsed / 1000) % 60;
        long minutes = (timeElapsed / (1000 * 60)) % 60;

        // Format the time string
        String timeString = String.format("Thời gian: %02d:%02d", minutes, seconds);
        tvTimer.setText(timeString);
    }

    private void restartGame() {
        // Reset and restart the game
        initializeGame();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Pause the timer when activity is paused
        if (timer != null) {
            timer.cancel();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Clean up timer when activity is destroyed
        if (timer != null) {
            timer.cancel();
        }
    }

    // Inner class for card data
    private static class CardItem {
        private final String imageUrl;
        private final String text;
        private final int value;

        public CardItem(String imageUrl, String text, int value) {
            this.imageUrl = imageUrl;
            this.text = text;
            this.value = value;
        }

        public String getImageUrl() {
            return imageUrl;
        }

        public String getText() {
            return text;
        }

        public int getValue() {
            return value;
        }
    }

    // Custom CardView class to represent each card
    private class CardView extends androidx.cardview.widget.CardView {
        private CardItem cardItem;
        private boolean isFlipped = false;
        private boolean isMatched = false;

        private View frontView;
        private View backView;
        private ImageView ivCardImage;

        public CardView(GameLatTheActivity context) {
            super(context);
            init(context);
        }

        private void init(GameLatTheActivity context) {
            // Inflate card layout
            View cardLayout = context.getLayoutInflater().inflate(R.layout.card_item_activity, this, true);

            // Get front and back views
            frontView = cardLayout.findViewById(R.id.cardFront);
            backView = cardLayout.findViewById(R.id.cardBack);
            ivCardImage = frontView.findViewById(R.id.ivCardImage);

            // Initially show back (hidden) view
            frontView.setVisibility(View.GONE);
            backView.setVisibility(View.VISIBLE);

            // Set card appearance
            setRadius(16);
            setCardElevation(4);
            setContentPadding(8, 8, 8, 8);
        }

        public void setCardItem(CardItem item) {
            this.cardItem = item;

            // Set front view content
            ((TextView) frontView.findViewById(R.id.tvCardText)).setText(item.getText());

            // Load image from URL using Picasso
            Picasso.get()
                    .load(item.getImageUrl())
                    .placeholder(R.drawable.no_image)
                    .error(R.drawable.no_image)
                    .into(ivCardImage);
        }

        public void flip() {
            if (isMatched) {
                return;
            }

            // Flip animation
            frontView.setVisibility(View.VISIBLE);
            backView.setVisibility(View.GONE);
            isFlipped = true;
        }

        public void flipBack() {
            if (isMatched) {
                return;
            }

            // Flip back animation
            frontView.setVisibility(View.GONE);
            backView.setVisibility(View.VISIBLE);
            isFlipped = false;
        }

        public boolean isFlipped() {
            return isFlipped;
        }

        public void setMatched(boolean matched) {
            isMatched = matched;

            if (matched) {
                // Change appearance for matched cards
                setCardBackgroundColor(0x8000FF00);
            }
        }

        public boolean isMatched() {
            return isMatched;
        }
    }
}