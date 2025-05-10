package com.example.kidapp.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.example.kidapp.R;
import com.example.kidapp.Utils.FlipAnimationUtil;
import com.example.kidapp.ViewModel.UserViewModel;
import com.example.kidapp.models.User;
import com.google.android.material.navigation.NavigationView;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

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
    private DrawerLayout drawer;
    private ImageButton menuButton;
    private NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        initViews();
        setupCardFlips();
        drawer = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // Tạo toggle button cho navigation drawer
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        // Xử lý nút menu để mở navigation drawer
        menuButton = findViewById(R.id.menuButton);
        menuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawer.openDrawer(GravityCompat.START);
            }
        });
        // Hiển thị fragment mặc định khi ứng dụng khởi động

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        UserViewModel userViewModel = new ViewModelProvider(this).get(UserViewModel.class);
        userViewModel.getAllUsers().observe(this, users -> {
            if (users != null && !users.isEmpty()) {
                for (User user : users) {
                    Log.d("USER", "User: " + user.getName()+" "+user.getEmail()+" "+user.getPassword());
                }
            } else {
                Log.d("USER", "No users found.");
            }
        });



    }

    private void initViews() {
        // Initialize CardViews
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

        // Initialize front and back views
        inflateFrontBackViews();

        // Setup animations
        setupAnimations();
    }

    private void inflateFrontBackViews() {
        // Numbers Card
        frontNumbers = getLayoutInflater().inflate(R.layout.card_front_numbers, null);
        backNumbers = getLayoutInflater().inflate(R.layout.card_back_numbers, null);

        // Reading Card
        frontReading = getLayoutInflater().inflate(R.layout.card_front_reading, null);
        backReading = getLayoutInflater().inflate(R.layout.card_back_reading, null);

        // Shapes Card
        frontShapes = getLayoutInflater().inflate(R.layout.card_front_shapes, null);
        backShapes = getLayoutInflater().inflate(R.layout.card_back_shapes, null);

        // Vocab Card
        frontVocab = getLayoutInflater().inflate(R.layout.card_front_vocab, null);
        backVocab = getLayoutInflater().inflate(R.layout.card_back_vocab, null);

        // Analysis Card
        frontAnalysis = getLayoutInflater().inflate(R.layout.card_front_analysis, null);
        backAnalysis = getLayoutInflater().inflate(R.layout.card_back_analysis, null);

        // Settings Card
        frontSettings = getLayoutInflater().inflate(R.layout.card_front_settings, null);
        backSettings = getLayoutInflater().inflate(R.layout.card_back_settings, null);

        // Animals Card
        frontAnimals = getLayoutInflater().inflate(R.layout.card_front_animals, null);
        backAnimals = getLayoutInflater().inflate(R.layout.card_back_animals, null);

        // Maths Card
        frontMaths = getLayoutInflater().inflate(R.layout.card_front_maths, null);
        backMaths = getLayoutInflater().inflate(R.layout.card_back_maths, null);

        // Listen Card
        frontListen = getLayoutInflater().inflate(R.layout.card_front_listen, null);
        backListen = getLayoutInflater().inflate(R.layout.card_back_listen, null);

        // Game Card
        frontGame = getLayoutInflater().inflate(R.layout.card_front_game, null);
        backGame = getLayoutInflater().inflate(R.layout.card_back_game, null);

        // Add views to CardViews
        cardNumbers.removeAllViews();
        cardNumbers.addView(frontNumbers);
        cardNumbers.addView(backNumbers);

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
        // Initialize flip animations for all cards
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
    }

    private void setupCardFlips() {
        // Set up card flip animations and actions
        setupNumbersCard();
        setupReadingCard();
        setupShapesCard();
        setupVocabCard();
        setupAnalysisCard();
        setupSettingsCard();
        setupAnimalsCard();
        setupMathsCard();
        setupListenCard();
        setupGameCard();

        // Set up profile button click
        profileButton = findViewById(R.id.profileButton);
        profileButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
            startActivity(intent);
        });
    }

    private void setupNumbersCard() {
        cardNumbers.setOnClickListener(v -> {
            numbersFlipAnimation.cancelAutoFlip();
            numbersFlipAnimation.flipCard();

            if (!numbersFlipAnimation.isFront()) {
                ImageView btnStart = backNumbers.findViewById(R.id.Imv_letter);
                btnStart.setOnClickListener(view -> {
                    Intent intent = new Intent(MainActivity.this, NumberLearnActivity.class);
                    startActivity(intent);
                });
            }
        });
    }

    private void setupReadingCard() {
        cardReading.setOnClickListener(v -> {
            readingFlipAnimation.cancelAutoFlip();
            readingFlipAnimation.flipCard();

            if (!readingFlipAnimation.isFront()) {
                ImageView btnStart = backReading.findViewById(R.id.Imv_reading);
                btnStart.setOnClickListener(view -> {
                    Intent intent = new Intent(MainActivity.this, ReadingActivity.class);
                    startActivity(intent);
                });
            }
        });
    }

    private void setupShapesCard() {
        cardShapes.setOnClickListener(v -> {
            shapesFlipAnimation.cancelAutoFlip();
            shapesFlipAnimation.flipCard();

            if (!shapesFlipAnimation.isFront()) {
                ImageView gameButton = backShapes.findViewById(R.id.Imv_sharps);
                gameButton.setOnClickListener(view -> {
                    Intent intent = new Intent(MainActivity.this, ShapesGameActivity.class);
                    startActivity(intent);
                });
            }
        });
    }

    private void setupVocabCard() {
        cardVocab.setOnClickListener(v -> {
            vocabFlipAnimation.cancelAutoFlip();
            vocabFlipAnimation.flipCard();

            if (!vocabFlipAnimation.isFront()) {
                ImageView btnLetter = backVocab.findViewById(R.id.Imv_letter);
                btnLetter.setOnClickListener(view -> {
                    Intent intent = new Intent(MainActivity.this, LetterActivity.class);
                    startActivity(intent);
                });
            }
        });
    }

    private void setupAnalysisCard() {
        cardAnalysis.setOnClickListener(v -> {
            analysisFlipAnimation.cancelAutoFlip();
            analysisFlipAnimation.flipCard();
        });
    }

    private void setupSettingsCard() {
        cardSettings.setOnClickListener(v -> {
            settingsFlipAnimation.cancelAutoFlip();
            settingsFlipAnimation.flipCard();
        });
    }

    private void setupAnimalsCard() {
        cardAnimals.setOnClickListener(v -> {
            animalsFlipAnimation.cancelAutoFlip();
            animalsFlipAnimation.flipCard();

            if (!animalsFlipAnimation.isFront()) {
                ImageView gifView = backAnimals.findViewById(R.id.Imv_animals);
                Glide.with(MainActivity.this)
                        .asGif()
                        .load(R.drawable.panda)
                        .into(gifView);

                gifView.setOnClickListener(view -> {
                    Intent intent = new Intent(MainActivity.this, AnimalsActivity.class);
                    startActivity(intent);
                });
            }
        });
    }

    private void setupMathsCard() {
        cardMaths.setOnClickListener(v -> {
            mathsFlipAnimation.cancelAutoFlip();
            mathsFlipAnimation.flipCard();

            if (!mathsFlipAnimation.isFront()) {
                ImageView btnMaths = backMaths.findViewById(R.id.Imv_maths);
                btnMaths.setOnClickListener(view -> {
                    Intent intent = new Intent(MainActivity.this, MathQuizActivity.class);
                    startActivity(intent);
                });
            }
        });
    }

    private void setupListenCard() {
        cardListen.setOnClickListener(v -> {
            listenFlipAnimation.cancelAutoFlip();
            listenFlipAnimation.flipCard();

            if (!listenFlipAnimation.isFront()) {
                ImageView gifView = backListen.findViewById(R.id.Imv_listen);
                Glide.with(MainActivity.this)
                        .asGif()
                        .load(R.drawable.story)
                        .into(gifView);

                gifView.setOnClickListener(view -> showListenOptionsDialog());
            }
        });
    }

    private void setupGameCard() {
        cardGame.setOnClickListener(v -> {
            gameFlipAnimation.cancelAutoFlip();
            gameFlipAnimation.flipCard();

            if (!gameFlipAnimation.isFront()) {
                ImageView gifView = backGame.findViewById(R.id.Imv_game);
                gifView.setOnClickListener(view -> ShowGameOptionsDialog());
            }
        });
    }

    private void ShowGameOptionsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.CustomDialogTheme);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_games_options, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();

        // Custom animation
        if (dialog.getWindow() != null) {
            dialog.getWindow().setWindowAnimations(R.style.DialogAnimation);
        }

        // Set up puzzle game button
        ImageView btnPuzzle = dialogView.findViewById(R.id.Imv_puzzel);
        Glide.with(MainActivity.this)
                .asGif()
                .load(R.drawable.puzzel_game)
                .into(btnPuzzle);

        btnPuzzle.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, GameXepHinhActivity.class));
            dialog.dismiss();
        });

        // Set up memory card game button
        ImageView btnCard = dialogView.findViewById(R.id.Imv_card);
        btnCard.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, FlipCardLevelListActivity.class));
            dialog.dismiss();
        });

        // Set up word guess game button
        ImageView btnDoanAnh = dialogView.findViewById(R.id.Imv_DoanAnh);
        Glide.with(MainActivity.this)
                .asGif()
                .load(R.drawable.puzzel_game)
                .into(btnDoanAnh);

        btnDoanAnh.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, GuessWordLevelListActivity.class));
            dialog.dismiss();
        });

        dialog.show();
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

        // Set up music button
        ImageView btnMusic = dialogView.findViewById(R.id.Imv_music);
        Glide.with(MainActivity.this)
                .asGif()
                .load(R.drawable.story)
                .into(btnMusic);

        btnMusic.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, MusicActivity.class));
            dialog.dismiss();
        });

        // Set up story button
        ImageView btnStory = dialogView.findViewById(R.id.Imv_story);
        Glide.with(MainActivity.this)
                .asGif()
                .load(R.drawable.story2)
                .into(btnStory);

        btnStory.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, StoryActivity.class));
            dialog.dismiss();
        });

        dialog.show();
    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();

        // Xử lý các mục menu không có submenu bằng if-else thay cho switch-case
        if (itemId == R.id.nav_home) {
            // Đang ở trang chủ, có thể không cần chuyển trang
        } else if (itemId == R.id.nav_number_learning) {
            startActivity(new Intent(MainActivity.this, NumberLearnActivity.class));
        } else if (itemId == R.id.nav_shape_learning) {
            startActivity(new Intent(MainActivity.this, ShapesGameActivity.class));
        } else if (itemId == R.id.nav_word_learning) {
            startActivity(new Intent(MainActivity.this, LetterActivity.class));
        } else if (itemId == R.id.nav_animal_learning) {
            startActivity(new Intent(MainActivity.this, AnimalsActivity.class));
        } else if (itemId == R.id.nav_math_learning) {
            startActivity(new Intent(MainActivity.this, MathQuizActivity.class));
        } else if (itemId == R.id.nav_music) {
            startActivity(new Intent(MainActivity.this, MusicActivity.class));
        } else if (itemId == R.id.nav_story) {
            startActivity(new Intent(MainActivity.this, StoryActivity.class));
        } else if (itemId == R.id.nav_puzzle_game) {
            startActivity(new Intent(MainActivity.this, GameXepHinhActivity.class));
        } else if (itemId == R.id.nav_card_flipping_game) {
            startActivity(new Intent(MainActivity.this, GameLatTheActivity.class));
        } else if (itemId == R.id.nav_word_guessing_game) {
            startActivity(new Intent(MainActivity.this, GameDoanChuActivity.class));
        } else if (itemId == R.id.nav_profile) {
            startActivity(new Intent(MainActivity.this, ProfileActivity.class));
        }

        // Đóng Drawer sau khi xử lý

        return false;
    }



    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Cancel all flip animations
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
    }
}