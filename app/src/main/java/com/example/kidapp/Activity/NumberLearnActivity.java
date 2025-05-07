package com.example.kidapp.Activity;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.view.animation.BounceInterpolator;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.kidapp.R;

public class NumberLearnActivity extends AppCompatActivity {

    private ImageView ivMainNumber;
    private TextView tvEquation;
    private ImageButton[] numberButtons = new ImageButton[10];
    private MediaPlayer mediaPlayer;
    private int currentNumber = 1; // Default starting number

    // Array of number names - add "TEN" to the array
    private final String[] numberNames = {"ZERO", "ONE", "TWO", "THREE", "FOUR",
            "FIVE", "SIX", "SEVEN", "EIGHT", "NINE", "TEN"};

    // Array of sound resource IDs - adjust ordering to match index
    private final int[] numberSounds = {
            R.raw.sound_one, R.raw.sound_one, R.raw.sound_two, R.raw.sound_three,
            R.raw.sound_four, R.raw.sound_five, R.raw.sound_six,
            R.raw.sound_seven, R.raw.sound_eight, R.raw.sound_nine, R.raw.sound_ten
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.number_learn);

        // Initialize views
        ivMainNumber = findViewById(R.id.ivMainNumber);
        tvEquation = findViewById(R.id.tvEquation);

        // Set up the back button
        ImageView btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> finish());

        // Initialize number buttons
        initializeNumberButtons();

        // Set the initial number
        updateMainNumber(currentNumber);
    }

    private void initializeNumberButtons() {
        // Initialize button IDs
        int[] buttonIds = {
                R.id.btnNum1, R.id.btnNum2, R.id.btnNum3, R.id.btnNum4,
                R.id.btnNum5, R.id.btnNum6, R.id.btnNum7, R.id.btnNum8, R.id.btnNum9, R.id.btnNum10
        };

        // Set up each button
        for (int i = 0; i < 10; i++) {
            final int number = i+1;
            numberButtons[i] = findViewById(buttonIds[i]);

            // Add animations for button press
            numberButtons[i].setOnClickListener(v -> {
                // Animate the button
                animateButtonPress(v);

                // Update the main display
                updateMainNumber(number);

                // Play sound
                playNumberSound(number);
            });
        }
    }

    private void animateButtonPress(View view) {
        // Scale down
        ObjectAnimator scaleDownX = ObjectAnimator.ofFloat(view, "scaleX", 1f, 0.8f);
        ObjectAnimator scaleDownY = ObjectAnimator.ofFloat(view, "scaleY", 1f, 0.8f);
        scaleDownX.setDuration(100);
        scaleDownY.setDuration(100);

        // Scale up
        ObjectAnimator scaleUpX = ObjectAnimator.ofFloat(view, "scaleX", 0.8f, 1f);
        ObjectAnimator scaleUpY = ObjectAnimator.ofFloat(view, "scaleY", 0.8f, 1f);
        scaleUpX.setDuration(100);
        scaleUpY.setDuration(100);

        // Play animations in sequence
        AnimatorSet scaleDown = new AnimatorSet();
        scaleDown.play(scaleDownX).with(scaleDownY);

        AnimatorSet scaleUp = new AnimatorSet();
        scaleUp.play(scaleUpX).with(scaleUpY);

        AnimatorSet set = new AnimatorSet();
        set.play(scaleDown).before(scaleUp);
        set.start();
    }

    private void updateMainNumber(int number) {
        currentNumber = number;

        // Get the resource ID for the number image
        int resourceId = getResources().getIdentifier(
                "number" + number,
                "drawable",
                getPackageName()
        );

        // Set up animations for the main number image
        ivMainNumber.setAlpha(0f);
        ivMainNumber.setScaleX(0.5f);
        ivMainNumber.setScaleY(0.5f);

        // Set the new image
        ivMainNumber.setImageResource(resourceId);

        // Animate the main number with bounce effect
        AnimatorSet animatorSet = new AnimatorSet();

        ObjectAnimator alpha = ObjectAnimator.ofFloat(ivMainNumber, "alpha", 0f, 1f);
        alpha.setDuration(500);

        ObjectAnimator scaleX = ObjectAnimator.ofFloat(ivMainNumber, "scaleX", 0.5f, 1.1f, 1f);
        scaleX.setDuration(800);
        scaleX.setInterpolator(new BounceInterpolator());

        ObjectAnimator scaleY = ObjectAnimator.ofFloat(ivMainNumber, "scaleY", 0.5f, 1.1f, 1f);
        scaleY.setDuration(800);
        scaleY.setInterpolator(new BounceInterpolator());

        animatorSet.playTogether(alpha, scaleX, scaleY);
        animatorSet.start();

        // Update the equation text - make sure we don't go out of bounds
        String equationText = "( " + number + " = " + numberNames[number] + " )";
        tvEquation.setText(equationText);

        // Animate the equation text
        ObjectAnimator equationAnim = ObjectAnimator.ofFloat(tvEquation, "alpha", 0f, 1f);
        equationAnim.setDuration(500);
        equationAnim.start();
    }

    private void playNumberSound(int number) {
        // Release any previously playing sound
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }

        // Play the sound for the current number
        mediaPlayer = MediaPlayer.create(this, numberSounds[number]);
        mediaPlayer.start();

        // Release when done
        mediaPlayer.setOnCompletionListener(mp -> {
            mp.release();
            mediaPlayer = null;
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Release MediaPlayer resources
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}