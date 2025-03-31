package com.example.kidapp;

import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.flexbox.FlexboxLayout;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GameDoanChuActivity extends AppCompatActivity {

    private FlexboxLayout wordFrame;
    private FlexboxLayout wordCardsContainer;
    private Button btnCheck;
    private ImageView imageView;
    private int currentLevel = 0;
    private String currentCorrectPhrase;
    private List<String> selectedWords = new ArrayList<>();

    // Các màn chơi
    private final Level[] levels = {
            new Level("cà phê", new String[]{"cà", "phê", "nước", "sữa", "chào", "đen", "vàng"}, R.drawable.coffee),
            new Level("con mèo", new String[]{"con", "mèo", "chó", "gà", "vịt", "heo", "bò"}, R.drawable.cat),
            new Level("hoa hồng", new String[]{"hoa", "hồng", "lan", "cúc", "mai", "đào", "ly"}, R.drawable.rose),
            new Level("trường học", new String[]{"trường", "học", "lớp", "thầy", "cô", "bàn", "ghế"}, R.drawable.school),
            new Level("bánh mì", new String[]{"bánh", "mì", "kẹo", "sữa", "đường", "muối", "mắm"}, R.drawable.bread)
    };

    // Drawable resources
    private static final int NORMAL_BG = R.drawable.textview_black_border;
    private static final int SELECTED_BG = R.drawable.textview_selected_bg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_doan_chu);

        wordFrame = findViewById(R.id.wordFrame);
        wordCardsContainer = findViewById(R.id.wordCardsContainer);
        btnCheck = findViewById(R.id.btnCheck);
        imageView = findViewById(R.id.imageView);

        setupLevel(currentLevel);
        ImageButton btn_Back = findViewById(R.id.btn_Back);
        btn_Back.setOnClickListener(v -> finish());

    }

    private void setupLevel(int level) {
        // Clear previous views
        wordFrame.removeAllViews();
        wordCardsContainer.removeAllViews();
        selectedWords.clear();

        TextView tvLevel = findViewById(R.id.tvLevel);
        tvLevel.setText("Màn " + (level + 1) + "/" + levels.length); // Hiển thị dạng "Màn 1/5"

        // Set up new level
        Level currentLevelData = levels[level];
        currentCorrectPhrase = currentLevelData.correctPhrase;
        imageView.setImageResource(currentLevelData.imageRes);

        // Add word cards
        for (String word : currentLevelData.wordCards) {
            addWordCard(word);
        }

        btnCheck.setOnClickListener(v -> checkAnswer());
    }

    private void addWordCard(String word) {
        TextView card = new TextView(this);
        card.setText(word);
        card.setTag(word);

        // Styling ban đầu
        card.setBackgroundResource(NORMAL_BG);
        card.setTextSize(18);
        card.setTextColor(Color.BLACK);
        card.setGravity(Gravity.CENTER);
        card.setPadding(20, 20, 20, 20);

        FlexboxLayout.LayoutParams params = new FlexboxLayout.LayoutParams(
                FlexboxLayout.LayoutParams.WRAP_CONTENT,
                FlexboxLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(10, 10, 10, 10);
        card.setLayoutParams(params);

        card.setOnClickListener(v -> {
            String clickedWord = (String) v.getTag();
            TextView clickedView = (TextView) v;

            if (wordFrame.findViewWithTag(clickedWord) == null) {
                addWordToFrame(clickedWord);
                selectedWords.add(clickedWord);
                clickedView.setBackgroundResource(SELECTED_BG);
            } else {
                removeWordFromFrame(clickedWord);
                selectedWords.remove(clickedWord);
                clickedView.setBackgroundResource(NORMAL_BG);
            }
        });

        wordCardsContainer.addView(card);
    }

    private void addWordToFrame(String word) {
        TextView wordView = new TextView(this);
        wordView.setText(word);
        wordView.setTag(word);

        wordView.setBackgroundResource(NORMAL_BG);
        wordView.setTextSize(18);
        wordView.setTextColor(Color.BLACK);
        wordView.setGravity(Gravity.CENTER);
        wordView.setPadding(20, 20, 20, 20);

        FlexboxLayout.LayoutParams params = new FlexboxLayout.LayoutParams(
                FlexboxLayout.LayoutParams.WRAP_CONTENT,
                FlexboxLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(10, 10, 10, 10);
        wordView.setLayoutParams(params);

        wordView.setOnClickListener(v -> {
            TextView clickedView = (TextView) v;
            String clickedWord = (String) v.getTag();

            TextView originalCard = wordCardsContainer.findViewWithTag(clickedWord);
            if (originalCard != null) {
                originalCard.setBackgroundResource(NORMAL_BG);
            }

            removeWordFromFrame(clickedWord);
            selectedWords.remove(clickedWord);
        });

        wordFrame.addView(wordView);
    }

    private void removeWordFromFrame(String word) {
        View viewToRemove = wordFrame.findViewWithTag(word);
        if (viewToRemove != null) {
            wordFrame.removeView(viewToRemove);
        }
    }

    private void checkAnswer() {
        StringBuilder userPhraseBuilder = new StringBuilder();
        for (String word : selectedWords) {
            userPhraseBuilder.append(word).append(" ");
        }
        String userPhrase = userPhraseBuilder.toString().trim();

        if (userPhrase.equals(currentCorrectPhrase)) {
            showToast("Chính xác! Giỏi quá!");

            // Chuyển sang màn tiếp theo sau 1 giây
            wordFrame.postDelayed(() -> {
                currentLevel++;
                if (currentLevel < levels.length) {
                    setupLevel(currentLevel);
                } else {
                    showToast("Chúc mừng! Bạn đã hoàn thành tất cả màn chơi!");
                    btnCheck.setEnabled(false);
                }
            }, 1000);
        } else {
            showToast("Sai rồi! Hãy thử lại!");
        }
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    // Lớp lưu trữ thông tin mỗi màn chơi
    private static class Level {
        String correctPhrase;
        String[] wordCards;
        int imageRes;

        Level(String correctPhrase, String[] wordCards, int imageRes) {
            this.correctPhrase = correctPhrase;
            this.wordCards = wordCards;
            this.imageRes = imageRes;
        }
    }
}