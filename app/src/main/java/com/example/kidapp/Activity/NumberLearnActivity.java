package com.example.kidapp.Activity;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.BounceInterpolator;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.kidapp.Model.Number;
import com.example.kidapp.R;
import com.example.kidapp.ViewModel.NumberViewModel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class NumberLearnActivity extends AppCompatActivity {

    private ImageView ivMainNumber;
    private TextView tvEquation;
    private RecyclerView rvNumbers;
    private NumberAdapter numberAdapter;
    private int currentPage = 0;
    private List<List<Number>> pagedNumbers = new ArrayList<>();
    private MediaPlayer mediaPlayer;
    private int currentIndex = 0;
    private NumberViewModel numberViewModel;
    private List<Number> numberList = new ArrayList<>();
    private ImageView btnPrev, btnNext;
    private GestureDetector gestureDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.number_learn);

        // Initialize views
        ivMainNumber = findViewById(R.id.ivMainNumber);
        tvEquation = findViewById(R.id.tvEquation);
        rvNumbers = findViewById(R.id.rvNumbers);

        GridLayoutManager layoutManager = new GridLayoutManager(this, 5, GridLayoutManager.VERTICAL, false);
        rvNumbers.setLayoutManager(layoutManager);

        // Set up the back button
        ImageView btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> finish());

        // Lấy dữ liệu từ Firebase qua ViewModel
        numberViewModel = new ViewModelProvider(this).get(NumberViewModel.class);
        numberViewModel.getAllNumbers().observe(this, numbers -> {
            if (numbers != null && !numbers.isEmpty()) {
                numberList.clear();
                numberList.addAll(numbers);

                // Sắp xếp lại theo số nguyên
                numberList.sort((n1, n2) -> n1.getId() - n2.getId());

                // Chia thành các trang 10 số
                pagedNumbers.clear();
                for (int i = 0; i < numberList.size(); i += 10) {
                    int end = Math.min(i + 10, numberList.size());
                    pagedNumbers.add(new ArrayList<>(numberList.subList(i, end)));
                }
                // Hiển thị trang đầu tiên
                showPage(0);
            }
        });

        gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            private static final int SWIPE_THRESHOLD = 100;
            private static final int SWIPE_VELOCITY_THRESHOLD = 100;

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                if (e1 == null || e2 == null) return false;
                float diffX = e2.getX() - e1.getX();
                if (Math.abs(diffX) > Math.abs(e2.getY() - e1.getY())) {
                    if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                        if (diffX > 0) {
                            // Vuốt sang phải: trang trước
                            if (currentPage > 0) showPage(currentPage - 1);
                        } else {
                            // Vuốt sang trái: trang sau
                            if (currentPage < pagedNumbers.size() - 1) showPage(currentPage + 1);
                        }
                        return true;
                    }
                }
                return false;
            }
        });
        rvNumbers.setOnTouchListener((v, event) -> gestureDetector.onTouchEvent(event));
    }

    private void showPage(int page) {
        if (page < 0 || page >= pagedNumbers.size()) return;
        currentPage = page;
        numberAdapter = new NumberAdapter(pagedNumbers.get(page), number -> {
            // Khi click vào số, cập nhật main number
            updateMainNumber(numberList.indexOf(number));
        });
        rvNumbers.setAdapter(numberAdapter);
    }

    private void updateMainNumber(int index) {
        if (numberList == null || numberList.isEmpty() || index < 0 || index >= numberList.size()) return;
        currentIndex = index;
        Number number = numberList.get(index);

        // Load ảnh từ URL (dùng Glide)
        Glide.with(this).load(number.getImageUrl()).into(ivMainNumber);

        // Set up animations for the main number image
        ivMainNumber.setAlpha(0f);
        ivMainNumber.setScaleX(0.5f);
        ivMainNumber.setScaleY(0.5f);

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

        // Hiển thị tên số
        String equationText = "( " + number.getId() + " = " + number.getName() + " )";
        tvEquation.setText(equationText);

        // Animate the equation text
        ObjectAnimator equationAnim = ObjectAnimator.ofFloat(tvEquation, "alpha", 0f, 1f);
        equationAnim.setDuration(500);
        equationAnim.start();

        // Phát âm thanh
        playNumberSound(number.getSoundUrl());
    }

    private void playNumberSound(String soundUrl) {
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }
        mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(soundUrl);
            mediaPlayer.prepare();
            mediaPlayer.start();
            mediaPlayer.setOnCompletionListener(mp -> {
                mp.release();
                mediaPlayer = null;
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
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