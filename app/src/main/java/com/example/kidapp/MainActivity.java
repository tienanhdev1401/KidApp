package com.example.kidapp;


import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;

public class MainActivity extends AppCompatActivity {

    private CardView cardNumbers, cardReading, cardShapes, cardVocab, cardAnalysis, cardSettings, cardGame, cardListen, cardMaths, cardAnimals;
    private View frontNumbers, backNumbers;
    private View frontReading, backReading;
    private View frontShapes, backShapes;
    private View frontVocab, backVocab;
    private View frontAnalysis, backAnalysis;
    private View frontSettings, backSettings;
    private View frontAnimals, backAnimals;
    private View frontMaths, backMaths;
    private View frontListen, backListen;
    private View frontGame, backGame;

    private ImageView profileButton;
    private FlipAnimationUtil numbersFlipAnimation;
    private FlipAnimationUtil readingFlipAnimation;
    private FlipAnimationUtil shapesFlipAnimation;
    private FlipAnimationUtil vocabFlipAnimation;
    private FlipAnimationUtil analysisFlipAnimation;
    private FlipAnimationUtil settingsFlipAnimation;
    private FlipAnimationUtil animalsFlipAnimation;
    private FlipAnimationUtil mathsFlipAnimation;
    private FlipAnimationUtil listenFlipAnimation;
    private FlipAnimationUtil gameFlipAnimation;

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
        cardAnimals = findViewById(R.id.cardAnimals);
        cardMaths = findViewById(R.id.cardMaths);
        cardListen = findViewById(R.id.cardListen);
        cardGame = findViewById(R.id.cardGame);

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

        frontAnimals = getLayoutInflater().inflate(R.layout.card_front_animals, cardAnimals, false);
        backAnimals = getLayoutInflater().inflate(R.layout.card_back_animals, cardAnimals, false);

        frontMaths = getLayoutInflater().inflate(R.layout.card_front_maths, cardMaths, false);
        backMaths = getLayoutInflater().inflate(R.layout.card_back_maths, cardMaths, false);

        frontListen = getLayoutInflater().inflate(R.layout.card_front_listen, cardListen, false);
        backListen = getLayoutInflater().inflate(R.layout.card_back_listen, cardListen, false);

        frontGame = getLayoutInflater().inflate(R.layout.card_front_game, cardGame, false);
        backGame = getLayoutInflater().inflate(R.layout.card_back_game, cardGame, false);

        // Khởi tạo các animation util

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

        cardAnimals.removeAllViews();
        cardAnimals.addView(frontAnimals);
        cardAnimals.addView(backAnimals);

        cardMaths.removeAllViews();
        cardMaths.addView(frontMaths);
        cardMaths.addView(backMaths);

        cardListen.removeAllViews();
        cardListen.addView(frontListen);
        cardListen.addView(backListen);

        cardGame.removeAllViews();
        cardGame.addView(frontGame);
        cardGame.addView(backGame);


    }

    private void setupAnimations() {
        // Khởi tạo animations cho từng thẻ
        numbersFlipAnimation = new FlipAnimationUtil(this, frontNumbers, backNumbers);
        readingFlipAnimation = new FlipAnimationUtil(this, frontReading, backReading);
        shapesFlipAnimation = new FlipAnimationUtil(this, frontShapes, backShapes);
        vocabFlipAnimation = new FlipAnimationUtil(this, frontVocab, backVocab);
        analysisFlipAnimation = new FlipAnimationUtil(this, frontAnalysis, backAnalysis);
        settingsFlipAnimation = new FlipAnimationUtil(this, frontSettings, backSettings);
        animalsFlipAnimation = new FlipAnimationUtil(this, frontAnimals, backAnimals);
        mathsFlipAnimation = new FlipAnimationUtil(this, frontMaths, backMaths);
        listenFlipAnimation = new FlipAnimationUtil(this, frontListen, backListen);
        gameFlipAnimation = new FlipAnimationUtil(this, frontGame, backGame);
        // Khởi tạo animation util khác...
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
                    ImageView btnStart = backNumbers.findViewById(R.id.Imv_letter);
                    btnStart.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            // Khởi chạy NumbersActivity ở đây
                             Intent intent = new Intent(MainActivity.this, NumberLearnActivity.class);
                             startActivity(intent);
                        }
                    });
                }
            }
        });
        profileButton = findViewById(R.id.profileButton);
        profileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start ProfileActivity
                Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
                startActivity(intent);
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
                ImageView gameButton = backShapes.findViewById(R.id.Imv_sharps);
                gameButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Start ProfileActivity
                        Intent intent = new Intent(MainActivity.this, ShapesGameActivity.class);
                        startActivity(intent);
                    }
                });
            }
        });

        cardVocab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                vocabFlipAnimation.cancelAutoFlip(); // Hủy lật tự động nếu đang chờ
                vocabFlipAnimation.flipCard();
                ImageView btnLetter= backVocab.findViewById(R.id.Imv_letter);
                btnLetter.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Start ProfileActivity
                        Intent intent = new Intent(MainActivity.this, LetterActivity.class);
                        startActivity(intent);
                    }
                });
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
        cardAnimals.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                animalsFlipAnimation.cancelAutoFlip(); // Hủy lật tự động nếu đang chờ
                animalsFlipAnimation.flipCard();
                ImageView gifView = backAnimals.findViewById(R.id.Imv_animals);
                if(!animalsFlipAnimation.isFront() && backAnimals != null){
                    Glide.with(MainActivity.this)
                            .asGif()
                            .load(R.drawable.panda) // Thay bằng tên file GIF của bạn
                            .into(gifView);
                }
                gifView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Start ProfileActivity
                        Intent intent = new Intent(MainActivity.this, AnimalsActivity.class);
                        startActivity(intent);
                    }
                });


            }

        });

        cardMaths.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mathsFlipAnimation.cancelAutoFlip(); // Hủy lật tự động nếu đang chờ
                mathsFlipAnimation.flipCard();
            }
        });

        cardListen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listenFlipAnimation.cancelAutoFlip(); // Hủy lật tự động nếu đang chờ
                listenFlipAnimation.flipCard();
                ImageView gifView = backListen.findViewById(R.id.Imv_listen);

                if(!listenFlipAnimation.isFront()){
                    Glide.with(MainActivity.this)
                            .asGif()
                            .load(R.drawable.story) // Thay bằng tên file GIF của bạn
                            .into(gifView);
                }
                gifView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                       showListenOptionsDialog();
                    }
                });


            }
        });

        cardGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gameFlipAnimation.cancelAutoFlip(); // Hủy lật tự động nếu đang chờ
                gameFlipAnimation.flipCard();
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
        animalsFlipAnimation.cancelAutoFlip();
        mathsFlipAnimation.cancelAutoFlip();
        listenFlipAnimation.cancelAutoFlip();
        gameFlipAnimation.cancelAutoFlip();
        // Hủy tất cả animation util khác...
    }

    private void showListenOptionsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.CustomDialogTheme);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_listen_options, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();

        // Custom animation
        if (dialog.getWindow() != null) {
            dialog.getWindow().setWindowAnimations(R.style.DialogAnimation);
        }

        // Xử lý click Music
        ImageView btnMusic = dialogView.findViewById(R.id.Imv_music);
        Glide.with(MainActivity.this)
                .asGif()
                .load(R.drawable.story) // Thay bằng tên file GIF của bạn
                .into(btnMusic);
        btnMusic.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, MusicActivity.class));
            dialog.dismiss();
        });

        // Xử lý click Story
        ImageView btnStory = dialogView.findViewById(R.id.Imv_story);
        Glide.with(MainActivity.this)
                .asGif()
                .load(R.drawable.story) // Thay bằng tên file GIF của bạn
                .into(btnStory);
        btnStory.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, MusicActivity.class));
            dialog.dismiss();
        });

        dialog.show();
    }

}