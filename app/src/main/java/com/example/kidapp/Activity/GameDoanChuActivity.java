package com.example.kidapp.Activity;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.airbnb.lottie.LottieAnimationView;
import com.example.kidapp.R;
import com.example.kidapp.models.WordGuessLevel;
import com.example.kidapp.models.WordGuessStage;
import com.google.android.flexbox.FlexboxLayout;
import com.squareup.picasso.Picasso;
import java.util.ArrayList;
import java.util.List;

public class GameDoanChuActivity extends AppCompatActivity {

    private FlexboxLayout wordFrame;
    private FlexboxLayout wordCardsContainer;
    private Button btnCheck;
    private ImageView imageView;
    private int currentStageIndex = 0;
    private String currentCorrectPhrase;
    private List<String> selectedWords = new ArrayList<>();
    private List<WordGuessStage> stages;

    private static final int NORMAL_BG = R.drawable.textview_black_border;
    private static final int SELECTED_BG = R.drawable.textview_selected_bg;
    private static final int INFRAME_BG = R.drawable.textview_inframe_bg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_word_game);

        wordFrame = findViewById(R.id.wordFrame);
        wordCardsContainer = findViewById(R.id.wordCardsContainer);
        btnCheck = findViewById(R.id.btnCheck);
        imageView = findViewById(R.id.imageView);

        WordGuessLevel level = (WordGuessLevel) getIntent().getSerializableExtra("level");
        if (level == null || level.getStages() == null || level.getStages().isEmpty()) {
            Toast.makeText(this, "Không có dữ liệu màn chơi!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        stages = level.getStages();
        setupStage(currentStageIndex);

        ImageView btn_Back = findViewById(R.id.btnBack);
        btn_Back.setOnClickListener(v -> finish());
    }

    private void setupStage(int index) {
        wordFrame.removeAllViews();
        wordCardsContainer.removeAllViews();
        selectedWords.clear();

        TextView tvLevel = findViewById(R.id.tvLevel);
        tvLevel.setText("Màn " + (index + 1) + "/" + stages.size());

        WordGuessStage stage = stages.get(index);
        currentCorrectPhrase = stage.getAnswer();
        if (stage.getImageUrl() != null && stage.getImageUrl().startsWith("http")) {
            Picasso.get().load(stage.getImageUrl()).into(imageView);
        } else {
            imageView.setImageResource(R.drawable.no_image);
        }

        List<String> suggestion = stage.getSuggestion();
        for (int i = 0; i < suggestion.size(); i++) {
            addWordCard(suggestion.get(i), i);
        }

        btnCheck.setOnClickListener(v -> checkAnswer());
    }

    private void addWordCard(String word, int index) {
        TextView card = new TextView(this);
        card.setText(word);
        String uniqueTag = word + "_" + index;
        card.setTag(uniqueTag);
        card.setBackgroundResource(NORMAL_BG);
        card.setTextSize(30);
        card.setTextColor(Color.parseColor("#BDE1F7"));
        card.setGravity(Gravity.CENTER);
        card.setPadding(20, 20, 20, 20);
        FlexboxLayout.LayoutParams params = new FlexboxLayout.LayoutParams(
                FlexboxLayout.LayoutParams.WRAP_CONTENT,
                FlexboxLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(10, 10, 10, 10);
        card.setLayoutParams(params);
        card.setOnClickListener(v -> {
            String clickedTag = (String) v.getTag();
            TextView clickedView = (TextView) v;
            if (wordFrame.findViewWithTag(clickedTag) == null) {
                addWordToFrame(word, clickedTag);
                selectedWords.add(clickedTag);
                clickedView.setBackgroundResource(SELECTED_BG);
                clickedView.setTextColor(Color.WHITE);
            } else {
                removeWordFromFrame(clickedTag);
                selectedWords.remove(clickedTag);
                clickedView.setBackgroundResource(NORMAL_BG);
                clickedView.setTextColor(Color.parseColor("#BDE1F7"));
            }
        });
        wordCardsContainer.addView(card);
    }

    private void addWordToFrame(String word, String tag) {
        TextView wordView = new TextView(this);
        wordView.setText(word);
        wordView.setTag(tag);
        wordView.setBackgroundResource(INFRAME_BG);
        wordView.setTextSize(30);
        wordView.setTextColor(Color.WHITE);
        wordView.setGravity(Gravity.CENTER);
        wordView.setPadding(20, 20, 20, 20);
        FlexboxLayout.LayoutParams params = new FlexboxLayout.LayoutParams(
                FlexboxLayout.LayoutParams.WRAP_CONTENT,
                FlexboxLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(10, 10, 10, 10);
        wordView.setLayoutParams(params);
        wordView.setOnClickListener(v -> {
            String clickedTag = (String) v.getTag();
            TextView originalCard = wordCardsContainer.findViewWithTag(clickedTag);
            if (originalCard != null) {
                originalCard.setBackgroundResource(NORMAL_BG);
            }
            removeWordFromFrame(clickedTag);
            selectedWords.remove(clickedTag);
        });
        wordFrame.addView(wordView);
    }

    private void removeWordFromFrame(String tag) {
        View viewToRemove = wordFrame.findViewWithTag(tag);
        if (viewToRemove != null) {
            wordFrame.removeView(viewToRemove);
        }
    }

    private void checkAnswer() {
        StringBuilder userPhraseBuilder = new StringBuilder();
        for (String tag : selectedWords) {
            // Lấy ký tự đầu tiên trước dấu _
            String realChar = tag.split("_")[0];
            userPhraseBuilder.append(realChar);
        }
        String userPhrase = userPhraseBuilder.toString().trim();
        Log.d("GameDoanChuActivity", "User phrase: " + userPhrase);
        Log.d("GameDoanChuActivity", "Correct phrase: " + currentCorrectPhrase);
        if (userPhrase.equalsIgnoreCase(currentCorrectPhrase)) {
            showToast("Chính xác! Giỏi quá!");
            LottieAnimationView animationView = findViewById(R.id.animationView);
            animationView.setVisibility(View.VISIBLE);
            animationView.playAnimation();
            wordFrame.postDelayed(() -> {
                currentStageIndex++;
                if (currentStageIndex < stages.size()) {
                    setupStage(currentStageIndex);
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
}