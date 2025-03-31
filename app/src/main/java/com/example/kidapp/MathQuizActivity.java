package com.example.kidapp;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class MathQuizActivity extends AppCompatActivity implements View.OnClickListener {

    // UI Components
    private TextView progressCounter, operationTextView;
    private ImageView number1ImageView, number2ImageView;
    private ImageView option1ImageView, option2ImageView, option3ImageView, option4ImageView;
    private CardView option1Card, option2Card, option3Card, option4Card;
    private Button dontKnowButton;
    private ImageButton closeButton;

    // Game variables
    private int currentProblem = 1;
    private int totalProblems = 30;
    private int correctAnswer;
    private int correctOptionIndex;
    private int number1, number2;
    private boolean isAddition;
    private Random random = new Random();

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_math_quiz); // Make sure to rename your layout file to match

        initViews();
        setupListeners();
        updateProgressCounter();
        generateNewProblem();
    }

    private void initViews() {
        // Initialize TextViews
        progressCounter = findViewById(R.id.progressCounter);
        operationTextView = findViewById(R.id.operationTextView);

        // Initialize ImageViews for numbers
        number1ImageView = findViewById(R.id.number1ImageView);
        number2ImageView = findViewById(R.id.number2ImageView);

        // Initialize option cards
        option1Card = findViewById(R.id.option1Card);
        option2Card = findViewById(R.id.option2Card);
        option3Card = findViewById(R.id.option3Card);
        option4Card = findViewById(R.id.option4Card);

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

    @Override
    public void onClick(View view) {
        int id = view.getId();

        if (id == R.id.option1Card || id == R.id.option2Card ||
                id == R.id.option3Card || id == R.id.option4Card) {

            handleOptionSelection(id);
        } else if (id == R.id.dontKnowButton) {
            showCorrectAnswer();
            generateNewProblem();
        }
    }

    private void handleOptionSelection(int id) {
        int selectedOption = -1;

        if (id == R.id.option1Card) selectedOption = 0;
        else if (id == R.id.option2Card) selectedOption = 1;
        else if (id == R.id.option3Card) selectedOption = 2;
        else if (id == R.id.option4Card) selectedOption = 3;

        if (selectedOption == correctOptionIndex) {
            Toast.makeText(this, "Chính xác!", Toast.LENGTH_SHORT).show();
            currentProblem++;
            updateProgressCounter();

            if (currentProblem <= totalProblems) {
                generateNewProblem();
            } else {
                finishGame();
            }
        } else {
            Toast.makeText(this, "Chưa đúng, hãy thử lại!", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateProgressCounter() {
        progressCounter.setText(currentProblem + "/" + totalProblems);
    }

    private void generateNewProblem() {
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

        // Set operation symbol
        operationTextView.setText(isAddition ? "+" : "-");

        // Set the number images
        setNumberImage(number1ImageView, number1);
        setNumberImage(number2ImageView, number2);

        // Generate and set answer options
        setAnswerOptions();
    }

    private void setNumberImage(ImageView imageView, int number) {
        if (number <= 10) {
            imageView.setImageResource(numberDrawables[number]);
        } else {
            // Tách các chữ số
            String numberStr = String.valueOf(number);
            LinearLayout container = new LinearLayout(this);
            container.setOrientation(LinearLayout.HORIZONTAL);

            for (int i = 0; i < numberStr.length(); i++) {
                int digit = Character.getNumericValue(numberStr.charAt(i));
                ImageView digitImage = new ImageView(this);
                digitImage.setImageResource(numberDrawables[digit]);
                container.addView(digitImage);
            }

            // Chuyển LinearLayout thành Bitmap và hiển thị
            imageView.setImageBitmap(createBitmapFromView(container));
        }
    }

    private Bitmap createBitmapFromView(View view) {
        view.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        Bitmap bitmap = Bitmap.createBitmap(view.getMeasuredWidth(), view.getMeasuredHeight(),
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
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
        Toast.makeText(this, "Câu trả lời đúng là: " + correctAnswer, Toast.LENGTH_SHORT).show();
    }

    private void finishGame() {
        Toast.makeText(this, "Chúc mừng! Bạn đã hoàn thành tất cả các bài tập.", Toast.LENGTH_LONG).show();
        // You could navigate to a result screen here
    }
}