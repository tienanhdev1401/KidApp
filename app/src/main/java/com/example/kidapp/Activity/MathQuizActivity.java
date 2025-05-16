package com.example.kidapp.Activity;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import com.example.kidapp.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class MathQuizActivity extends AppCompatActivity implements View.OnClickListener {

    // UI Components
    private TextView progressCounter, operationTextView, questionTitle, chooseAnswerText;
    private ImageView number1ImageView, number2ImageView;
    private ImageView option1ImageView, option2ImageView, option3ImageView, option4ImageView;
    private CardView option1Card, option2Card, option3Card, option4Card;
    private CardView questionTitleCard;
    private Button dontKnowButton;
    private ImageView closeButton;
    private View mainCard;

    // Game variables
    private int currentProblem = 1;
    private int totalProblems = 30;
    private int correctAnswer;
    private int correctOptionIndex;
    private int number1, number2;
    private boolean isAddition;
    private Random random = new Random();
    private boolean canSelect = true; // To prevent multiple selection

    // Resource IDs for number images (you'll need to create these drawable resources)
    private final int[] numberDrawables = {
            R.drawable.number0,
            R.drawable.number1,
            R.drawable.number2,
            R.drawable.number3,
            R.drawable.number4,
            R.drawable.number5,
            R.drawable.number6,
            R.drawable.number7,
            R.drawable.number8,
            R.drawable.number9,
            R.drawable.number10
    };

    // For sound effects
    private MediaPlayer correctSound;
    private MediaPlayer incorrectSound;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_math_quiz);

        initViews();
        setupListeners();
        initSounds();
        updateProgressCounter();
        animateIntro();
        generateNewProblem();
    }

    private void initViews() {
        // Initialize TextViews
        progressCounter = findViewById(R.id.progressCounter);
        operationTextView = findViewById(R.id.operationTextView);
        questionTitle = findViewById(R.id.questionTitle);
        chooseAnswerText = findViewById(R.id.chooseAnswerText);

        // Initialize ImageViews for numbers
        number1ImageView = findViewById(R.id.number1ImageView);
        number2ImageView = findViewById(R.id.number2ImageView);

        // Initialize card containers
        mainCard = findViewById(R.id.mainCard);
        questionTitleCard = findViewById(R.id.questionTitleCard);

        // Initialize option cards
        option1Card = findViewById(R.id.option1Card);
        option2Card = findViewById(R.id.option2Card);
        option3Card = findViewById(R.id.option3Card);
        option4Card = findViewById(R.id.option4Card);
        
        // Set initial background colors for option cards
        resetCardColors();

        // Initialize option ImageViews
        option1ImageView = findViewById(R.id.option1ImageView);
        option2ImageView = findViewById(R.id.option2ImageView);
        option3ImageView = findViewById(R.id.option3ImageView);
        option4ImageView = findViewById(R.id.option4ImageView);

        // Initialize buttons
        dontKnowButton = findViewById(R.id.dontKnowButton);
        closeButton = findViewById(R.id.closeButton);
    }

    private void setupListeners() {
        option1Card.setOnClickListener(this);
        option2Card.setOnClickListener(this);
        option3Card.setOnClickListener(this);
        option4Card.setOnClickListener(this);
        dontKnowButton.setOnClickListener(this);
        closeButton.setOnClickListener(v -> finish());
    }

    private void initSounds() {
        // Initialize sound effects (you'll need to add these raw resources)
        try {
            correctSound = MediaPlayer.create(this, R.raw.correct_sound);
            incorrectSound = MediaPlayer.create(this, R.raw.incorrect_sound);
        } catch (Exception e) {
            // Handle missing sound resources gracefully
            e.printStackTrace();
        }
    }

    private void animateIntro() {
        // Animate question title card
        questionTitleCard.setScaleX(0.8f);
        questionTitleCard.setScaleY(0.8f);
        questionTitleCard.setAlpha(0f);
        
        questionTitleCard.animate()
                .scaleX(1f)
                .scaleY(1f)
                .alpha(1f)
                .setDuration(400)
                .setInterpolator(new DecelerateInterpolator())
                .start();
        
        // Animate main problem card
        mainCard.setScaleX(0.8f);
        mainCard.setScaleY(0.8f);
        mainCard.setAlpha(0f);
        
        mainCard.animate()
                .scaleX(1f)
                .scaleY(1f)
                .alpha(1f)
                .setDuration(500)
                .setStartDelay(200)
                .setInterpolator(new DecelerateInterpolator())
                .start();

        // Animate option cards sequentially
        CardView[] optionCards = {option1Card, option2Card, option3Card, option4Card};
        for (int i = 0; i < optionCards.length; i++) {
            CardView card = optionCards[i];
            card.setTranslationY(100f);
            card.setAlpha(0f);
            
            card.animate()
                    .translationY(0f)
                    .alpha(1f)
                    .setDuration(400)
                    .setStartDelay(500 + (i * 100))
                    .start();
        }
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();

        if (!canSelect) return; // Prevent multiple selections

        if (id == R.id.option1Card || id == R.id.option2Card ||
                id == R.id.option3Card || id == R.id.option4Card) {

            canSelect = false; // Disable selection temporarily
            handleOptionSelection(id);
        } else if (id == R.id.dontKnowButton) {
            showCorrectAnswer();
            
            // Short delay before next problem
            new Handler().postDelayed(this::generateNewProblem, 1500);
        }
    }

    private void handleOptionSelection(int id) {
        int selectedOption = -1;
        CardView selectedCard = null;

        if (id == R.id.option1Card) {
            selectedOption = 0;
            selectedCard = option1Card;
        } else if (id == R.id.option2Card) {
            selectedOption = 1;
            selectedCard = option2Card;
        } else if (id == R.id.option3Card) {
            selectedOption = 2;
            selectedCard = option3Card;
        } else if (id == R.id.option4Card) {
            selectedOption = 3;
            selectedCard = option4Card;
        }

        if (selectedCard != null) {
            if (selectedOption == correctOptionIndex) {
                // Correct answer animation
                playCorrectAnimation(selectedCard);
                
                // Play correct sound
                if (correctSound != null) {
                    correctSound.start();
                }
                
                // Increment problem counter and update UI
                currentProblem++;
                updateProgressCounter();

                // Move to next problem after a short delay
                new Handler().postDelayed(() -> {
                    if (currentProblem <= totalProblems) {
                        generateNewProblem();
                    } else {
                        finishGame();
                    }
                    canSelect = true; // Re-enable selection for next problem
                }, 1000);
            } else {
                // Incorrect answer animation
                playIncorrectAnimation(selectedCard);
                
                // Play incorrect sound
                if (incorrectSound != null) {
                    incorrectSound.start();
                }
                
                // Re-enable selection after a short delay
                new Handler().postDelayed(() -> canSelect = true, 500);
            }
        }
    }

    private void playCorrectAnimation(CardView cardView) {
        // Scale up-down animation
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(cardView, "scaleX", 1f, 1.2f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(cardView, "scaleY", 1f, 1.2f, 1f);
        
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(scaleX, scaleY);
        animatorSet.setDuration(500);
        animatorSet.start();
        
        // Show success message with cheerful icon
        Toast.makeText(this, "👍 Chính xác!", Toast.LENGTH_SHORT).show();
    }

    private void playIncorrectAnimation(CardView cardView) {
        // Shake animation
        cardView.startAnimation(AnimationUtils.loadAnimation(this, R.anim.shake_animation));
        
        // Show encouraging message
        Toast.makeText(this, "❤️ Chưa đúng, hãy thử lại!", Toast.LENGTH_SHORT).show();
    }

    private void updateProgressCounter() {
        progressCounter.setText(currentProblem + "/" + totalProblems);
    }

    private void generateNewProblem() {
        // Reset UI state
        resetCardColors();
        canSelect = true;
        
        // Animate question change
        animateQuestionChange();
        
        // Generate random numbers between 0 and 10
        number1 = random.nextInt(11);
        number2 = random.nextInt(11);

        // Randomly choose between addition and subtraction
        isAddition = random.nextBoolean();

        // For subtraction, ensure the result is non-negative (number1 >= number2)
        if (!isAddition && number1 < number2) {
            int temp = number1;
            number1 = number2;
            number2 = temp;
        }

        // Calculate the correct answer
        correctAnswer = isAddition ? number1 + number2 : number1 - number2;

        // Update question title
        questionTitle.setText(isAddition ? "Bạn cộng được bao nhiêu?" : "Bạn trừ được bao nhiêu?");
        
        // Change question card color based on operation
        questionTitleCard.setCardBackgroundColor(ContextCompat.getColor(this, 
                isAddition ? R.color.addition_color : R.color.subtraction_color));

        // Set operation symbol
        operationTextView.setText(isAddition ? "+" : "-");
        operationTextView.setTextColor(ContextCompat.getColor(this, 
                isAddition ? R.color.addition_color : R.color.subtraction_color));

        // Set the number images
        setNumberImage(number1ImageView, number1);
        setNumberImage(number2ImageView, number2);

        // Generate and set answer options
        setAnswerOptions();
    }

    private void animateQuestionChange() {
        // Fade out/in animation for problem elements
        View[] problemViews = {number1ImageView, number2ImageView, operationTextView};
        
        // Animate question title card
        questionTitleCard.animate()
                .alpha(0.7f)
                .scaleX(0.95f)
                .scaleY(0.95f)
                .setDuration(200)
                .withEndAction(() -> {
                    questionTitleCard.animate()
                            .alpha(1f)
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(300)
                            .start();
                })
                .start();
        
        for (View view : problemViews) {
            view.animate()
                .alpha(0f)
                .setDuration(200)
                .withEndAction(() -> {
                    view.animate()
                        .alpha(1f)
                        .setDuration(300)
                        .start();
                })
                .start();
        }
    }

    private void resetCardColors() {
        option1Card.setCardBackgroundColor(Color.parseColor("#CCFFFF"));
        option2Card.setCardBackgroundColor(Color.parseColor("#FFCCFF"));
        option3Card.setCardBackgroundColor(Color.parseColor("#FFCCCC"));
        option4Card.setCardBackgroundColor(Color.parseColor("#FFFFCC"));
    }

    private void setNumberImage(ImageView imageView, int number) {
        if (number <= 10) {
            imageView.setImageResource(numberDrawables[number]);
        } else {
            // Tạo layout mới cho các số 2 chữ số
            LinearLayout container = new LinearLayout(this);
            container.setOrientation(LinearLayout.HORIZONTAL);
            container.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));
            
            // Căn giữa theo chiều dọc và ngang
            container.setGravity(android.view.Gravity.CENTER);
            
            // Tách các chữ số
            String numberStr = String.valueOf(number);
            
            // Kích thước cơ bản và giảm khoảng cách giữa các chữ số
            int digitSize = 90;
            
            // Sử dụng margins âm để kéo các chữ số gần nhau hơn
            int digitMargin = -25; // Margin âm để các số gần nhau hơn
            
            for (int i = 0; i < numberStr.length(); i++) {
                int digit = Character.getNumericValue(numberStr.charAt(i));
                
                // Tạo ImageView mới cho mỗi chữ số
                ImageView digitImage = new ImageView(this);
                
                // Thiết lập LayoutParams với margin
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        digitSize, digitSize);
                
                // Áp dụng margin trái cho tất cả các chữ số trừ chữ số đầu tiên
                if (i > 0) {
                    params.leftMargin = digitMargin;
                }
                
                digitImage.setLayoutParams(params);
                digitImage.setScaleType(ImageView.ScaleType.FIT_CENTER);
                digitImage.setImageResource(numberDrawables[digit]);
                
                // Thêm vào container
                container.addView(digitImage);
            }
            
            // Chuyển LinearLayout thành Bitmap và hiển thị
            Bitmap bitmap = createBitmapFromView(container);
            imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            imageView.setImageBitmap(bitmap);
        }
    }

    private Bitmap createBitmapFromView(View view) {
        // Đặt kích thước của view
        int widthSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        int heightSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        view.measure(widthSpec, heightSpec);

        // Lấy kích thước sau khi tính toán
        int width = view.getMeasuredWidth();
        int height = view.getMeasuredHeight();
        
        // Đảm bảo bitmap không quá nhỏ
        width = Math.max(width, 120);
        height = Math.max(height, 100);

        // Tạo bitmap với kích thước đã tính toán
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        
        // Vẽ view lên canvas ở chính giữa
        int left = (width - view.getMeasuredWidth()) / 2;
        int top = (height - view.getMeasuredHeight()) / 2;
        view.layout(left, top, left + view.getMeasuredWidth(), top + view.getMeasuredHeight());
        view.draw(canvas);
        
        return bitmap;
    }

    private void setAnswerOptions() {
        List<Integer> options = new ArrayList<>();
        options.add(correctAnswer); // Thêm đáp án đúng

        // Tạo các đáp án sai khác nhau và có độ khó hợp lý
        while (options.size() < 4) {
            int deviation = random.nextInt(5) + 1; // Độ lệch từ 1-5 so với đáp án đúng
            boolean addDeviation = random.nextBoolean(); // Thêm hoặc bớt độ lệch

            int wrongAnswer;
            if (addDeviation) {
                wrongAnswer = correctAnswer + deviation;
            } else {
                wrongAnswer = correctAnswer - deviation;
            }

            // Đảm bảo đáp án sai không âm và không trùng
            if (wrongAnswer >= 0 && wrongAnswer != correctAnswer && !options.contains(wrongAnswer)) {
                options.add(wrongAnswer);
            }

            // Nếu sau nhiều lần thử vẫn không đủ 4 option, tạo số ngẫu nhiên
            if (options.size() < 4 && options.size() > 1 && random.nextFloat() > 0.7f) {
                wrongAnswer = random.nextInt(21); // Số từ 0-20
                if (wrongAnswer != correctAnswer && !options.contains(wrongAnswer)) {
                    options.add(wrongAnswer);
                }
            }
        }

        // Đảm bảo luôn có đủ 4 lựa chọn
        while (options.size() < 4) {
            int wrongAnswer = random.nextInt(21);
            if (!options.contains(wrongAnswer)) {
                options.add(wrongAnswer);
            }
        }

        Collections.shuffle(options);
        correctOptionIndex = options.indexOf(correctAnswer);

        // Hiển thị các lựa chọn
        setNumberImage(option1ImageView, options.get(0));
        setNumberImage(option2ImageView, options.get(1));
        setNumberImage(option3ImageView, options.get(2));
        setNumberImage(option4ImageView, options.get(3));
    }

    private void showCorrectAnswer() {
        // Highlight the correct answer
        CardView correctCard = null;
        switch (correctOptionIndex) {
            case 0:
                correctCard = option1Card;
                break;
            case 1:
                correctCard = option2Card;
                break;
            case 2:
                correctCard = option3Card;
                break;
            case 3:
                correctCard = option4Card;
                break;
        }

        if (correctCard != null) {
            // Highlight correct answer with pulsing animation
            ObjectAnimator scaleX = ObjectAnimator.ofFloat(correctCard, "scaleX", 1f, 1.1f, 1f);
            ObjectAnimator scaleY = ObjectAnimator.ofFloat(correctCard, "scaleY", 1f, 1.1f, 1f);
            
            AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.playTogether(scaleX, scaleY);
            animatorSet.setDuration(500);
            animatorSet.start();
            
            // Temporarily change card color to indicate correct answer
            correctCard.setCardBackgroundColor(ContextCompat.getColor(this, R.color.correct_answer));
        }

        // Show message with the correct answer
        Toast.makeText(this, "✅ Câu trả lời đúng là: " + correctAnswer, Toast.LENGTH_SHORT).show();
    }

    private void finishGame() {
        // Play celebration sound and animation
        try {
            MediaPlayer celebrationSound = MediaPlayer.create(this, R.raw.celebration_sound);
            if (celebrationSound != null) {
                celebrationSound.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // Show congratulatory message
        Toast.makeText(this, "🎉 Chúc mừng! Bạn đã hoàn thành tất cả các bài tập. 🎉", Toast.LENGTH_LONG).show();
        
        // Finish activity after a delay
        new Handler().postDelayed(this::finish, 3000);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Release media player resources
        if (correctSound != null) {
            correctSound.release();
        }
        if (incorrectSound != null) {
            incorrectSound.release();
        }
    }
}