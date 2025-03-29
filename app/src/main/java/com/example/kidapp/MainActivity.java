package com.example.kidapp;


import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    private CardView cardNumbers, cardReading, cardShapes, cardVocab, cardAnalysis, cardSettings;
    private View frontNumbers, backNumbers;
    private View frontReading, backReading;
    private View frontShapes, backShapes;
    private View frontVocab, backVocab;
    private View frontAnalysis, backAnalysis;
    private View frontSettings, backSettings;

    private FlipAnimationUtil numbersFlipAnimation;
    private FlipAnimationUtil readingFlipAnimation;
    private FlipAnimationUtil shapesFlipAnimation;
    private FlipAnimationUtil vocabFlipAnimation;
    private FlipAnimationUtil analysisFlipAnimation;
    private FlipAnimationUtil settingsFlipAnimation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        initViews();
        setupCardFlips();
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }


    private void initViews() {
        // Khởi tạo các CardView
        cardNumbers = findViewById(R.id.cardNumbers);
        cardReading = findViewById(R.id.cardReading);
        cardShapes = findViewById(R.id.cardShapes);
        cardVocab = findViewById(R.id.cardVocab);
        cardAnalysis = findViewById(R.id.cardAnalysis);
        cardSettings = findViewById(R.id.cardSettings);

        // Inflate layouts cho mặt sau của thẻ
        inflateFrontBackViews();

        // Setup các animations
        setupAnimations();
    }

    private void inflateFrontBackViews() {
        // Numbers Card
        frontNumbers = getLayoutInflater().inflate(R.layout.card_front_numbers, cardNumbers, false);
        backNumbers = getLayoutInflater().inflate(R.layout.card_back_numbers, cardNumbers, false);

        // Làm tương tự cho các thẻ khác
        frontReading = getLayoutInflater().inflate(R.layout.card_front_reading, cardReading, false);
        backReading = getLayoutInflater().inflate(R.layout.card_back_reading, cardReading, false);

        frontShapes = getLayoutInflater().inflate(R.layout.card_front_shapes, cardShapes, false);
        backShapes = getLayoutInflater().inflate(R.layout.card_back_shapes, cardShapes, false);

        frontVocab = getLayoutInflater().inflate(R.layout.card_front_vocab, cardVocab, false);
        backVocab = getLayoutInflater().inflate(R.layout.card_back_vocab, cardVocab, false);

        frontAnalysis = getLayoutInflater().inflate(R.layout.card_front_analysis, cardAnalysis, false);
        backAnalysis = getLayoutInflater().inflate(R.layout.card_back_analysis, cardAnalysis, false);

        frontSettings = getLayoutInflater().inflate(R.layout.card_front_settings, cardSettings, false);
        backSettings = getLayoutInflater().inflate(R.layout.card_back_settings, cardSettings, false);

        // Thêm views vào CardView
        cardNumbers.removeAllViews();
        cardNumbers.addView(frontNumbers);
        cardNumbers.addView(backNumbers);

        // Làm tương tự cho các thẻ khác
        cardReading.removeAllViews();
        cardReading.addView(frontReading);
        cardReading.addView(backReading);

        cardShapes.removeAllViews();
        cardShapes.addView(frontShapes);
        cardShapes.addView(backShapes);

        cardVocab.removeAllViews();
        cardVocab.addView(frontVocab);
        cardVocab.addView(backVocab);

        cardAnalysis.removeAllViews();
        cardAnalysis.addView(frontAnalysis);
        cardAnalysis.addView(backAnalysis);

        cardSettings.removeAllViews();
        cardSettings.addView(frontSettings);
        cardSettings.addView(backSettings);
    }

    private void setupAnimations() {
        // Khởi tạo animations cho từng thẻ
        numbersFlipAnimation = new FlipAnimationUtil(this, frontNumbers, backNumbers);
        readingFlipAnimation = new FlipAnimationUtil(this, frontReading, backReading);
        shapesFlipAnimation = new FlipAnimationUtil(this, frontShapes, backShapes);
        vocabFlipAnimation = new FlipAnimationUtil(this, frontVocab, backVocab);
        analysisFlipAnimation = new FlipAnimationUtil(this, frontAnalysis, backAnalysis);
        settingsFlipAnimation = new FlipAnimationUtil(this, frontSettings, backSettings);
    }

    private void setupCardFlips() {
        // Set OnClickListener cho từng thẻ
        cardNumbers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                numbersFlipAnimation.cancelAutoFlip(); // Hủy lật tự động nếu đang chờ
                numbersFlipAnimation.flipCard();

                // Nếu đã lật sang mặt sau, setup nút "Start Learning"
                if (!numbersFlipAnimation.isFront()) {
                    ImageView btnStart = backNumbers.findViewById(R.id.Imv_numbers);
                    btnStart.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            // Khởi chạy NumbersActivity ở đây
                            // Intent intent = new Intent(MainActivity.this, NumbersActivity.class);
                            // startActivity(intent);
                        }
                    });


                }
            }
        });

        // Làm tương tự cho các thẻ khác
        cardReading.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Sửa thành readingFlipAnimation
                shapesFlipAnimation.cancelAutoFlip();
                readingFlipAnimation.flipCard();

                // Tìm nút Start trong backReading (mặt sau của card Reading)
                ImageView btnStart = backReading.findViewById(R.id.Imv_reading); // Thay R.id.Imv_reading bằng ID thực tế
                btnStart.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(MainActivity.this, ReadingActivity.class);
                        startActivity(intent);
                    }
                });
            }
        });

        cardShapes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shapesFlipAnimation.cancelAutoFlip(); // Hủy lật tự động nếu đang chờ
                shapesFlipAnimation.flipCard();
            }
        });

        cardVocab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                vocabFlipAnimation.cancelAutoFlip(); // Hủy lật tự động nếu đang chờ
                vocabFlipAnimation.flipCard();
            }
        });

        cardAnalysis.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                analysisFlipAnimation.cancelAutoFlip(); // Hủy lật tự động nếu đang chờ
                analysisFlipAnimation.flipCard();
            }
        });

        cardSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                settingsFlipAnimation.cancelAutoFlip(); // Hủy lật tự động nếu đang chờ
                settingsFlipAnimation.flipCard();
            }
        });
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        numbersFlipAnimation.cancelAutoFlip();
        readingFlipAnimation.cancelAutoFlip();
        shapesFlipAnimation.cancelAutoFlip();
        vocabFlipAnimation.cancelAutoFlip();
        analysisFlipAnimation.cancelAutoFlip();
        settingsFlipAnimation.cancelAutoFlip();
        // Hủy tất cả animation util khác...
    }

}