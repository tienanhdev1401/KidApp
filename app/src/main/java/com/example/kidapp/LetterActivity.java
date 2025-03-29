package com.example.kidapp;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import java.util.HashMap;
import java.util.Map;

public class LetterActivity extends AppCompatActivity {

    private LinearLayout letterContainer;
    private Map<String, String> letterInfoMap;
    private Map<String, Integer> letterImageMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_letter);

        initializeViews();
        setupLetterResources();
        setupClickListeners();
    }

    private void initializeViews() {
        letterContainer = findViewById(R.id.letterContainer);
    }

    private void setupLetterResources() {
        // Map chứa thông tin mô tả chữ cái theo định dạng "A = AXE"
        letterInfoMap = new HashMap<>();
        letterInfoMap.put("A", "A = An");
        letterInfoMap.put("B", "B = Bé");
        letterInfoMap.put("C", "C = Con");
        letterInfoMap.put("D", "D = Dê");
        letterInfoMap.put("E", "E = Em");
        letterInfoMap.put("F", "F = Phở");
        letterInfoMap.put("G", "G = Gà");
        letterInfoMap.put("H", "H = Hoa");
        letterInfoMap.put("I", "I = Ích");
        letterInfoMap.put("J", "J = Judo");
        letterInfoMap.put("K", "K = Kẹo");
        letterInfoMap.put("L", "L = Lá");
        letterInfoMap.put("M", "M = Mèo");
        letterInfoMap.put("N", "N = Nước");
        letterInfoMap.put("O", "O = Ong");
        letterInfoMap.put("P", "P = Phim");
        letterInfoMap.put("Q", "Q = Qụa");
        letterInfoMap.put("R", "R = Rồng");
        letterInfoMap.put("S", "S = Sách");
        letterInfoMap.put("T", "T = Tôm");
        letterInfoMap.put("U", "U = Ướt");
        letterInfoMap.put("V", "V = Voi");
        letterInfoMap.put("W", "W = Windows");
        letterInfoMap.put("X", "X = Xe");
        letterInfoMap.put("Y", "Y = Yến");
        letterInfoMap.put("Z", "Z = Zoo");

        // Map chứa ID drawable của các hình ảnh chữ cái
        letterImageMap = new HashMap<>();
        letterImageMap.put("A", R.drawable.a);
        letterImageMap.put("B", R.drawable.b);
        letterImageMap.put("C", R.drawable.c);
        letterImageMap.put("D", R.drawable.d);
        letterImageMap.put("E", R.drawable.e);
        letterImageMap.put("F", R.drawable.f);
        letterImageMap.put("G", R.drawable.g);
        letterImageMap.put("H", R.drawable.h);
        letterImageMap.put("I", R.drawable.i);
        letterImageMap.put("J", R.drawable.j);
        letterImageMap.put("K", R.drawable.k);
        letterImageMap.put("L", R.drawable.l);
        letterImageMap.put("M", R.drawable.m);
        letterImageMap.put("N", R.drawable.n);
        letterImageMap.put("O", R.drawable.o);
        letterImageMap.put("P", R.drawable.p);
        letterImageMap.put("Q", R.drawable.q);
        letterImageMap.put("R", R.drawable.r);
        letterImageMap.put("S", R.drawable.s);
        letterImageMap.put("T", R.drawable.t);
        letterImageMap.put("U", R.drawable.u);
        letterImageMap.put("V", R.drawable.v);
        letterImageMap.put("W", R.drawable.w);
        letterImageMap.put("X", R.drawable.x);
        letterImageMap.put("Y", R.drawable.y);
        letterImageMap.put("Z", R.drawable.z);
    }

    private void setupClickListeners() {
        // Thiết lập click listener cho tất cả các ImageView chữ cái
        for (String letter : letterImageMap.keySet()) {
            int resId = getResources().getIdentifier("image" + letter, "id", getPackageName());
            ImageView imageView = findViewById(resId);
            if (imageView != null) {
                imageView.setOnClickListener(v -> handleLetterClick(letter));
            }
        }
    }

    private void handleLetterClick(String letter) {
        ImageView source = findViewById(getResources()
                .getIdentifier("image" + letter, "id", getPackageName()));

        if (source != null) {
            createNewLetterView(source, letter);
        }
    }

    private void createNewLetterView(ImageView source, String letter) {
        // Tạo layout chứa hình ảnh và text
        LinearLayout itemLayout = new LinearLayout(this);
        itemLayout.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(0, 0, 0, 32); // Bottom margin 32dp
        itemLayout.setLayoutParams(layoutParams);
        itemLayout.setGravity(android.view.Gravity.CENTER_HORIZONTAL);

        // Tạo ImageView mới
        ImageView newImageView = new ImageView(this);
        LinearLayout.LayoutParams imageParams = new LinearLayout.LayoutParams(250, 250);
        imageParams.gravity = android.view.Gravity.CENTER_HORIZONTAL;
        newImageView.setLayoutParams(imageParams);
        newImageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        newImageView.setImageDrawable(source.getDrawable());

        // Tạo TextView mới với định dạng "A = AXE"
        TextView newTextView = new TextView(this);
        LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        textParams.gravity = android.view.Gravity.CENTER_HORIZONTAL;
        textParams.setMargins(0, 16, 0, 0); // Top margin 16dp
        newTextView.setLayoutParams(textParams);
        newTextView.setText(letterInfoMap.get(letter));
        newTextView.setTextSize(20);
        newTextView.setTextColor(ContextCompat.getColor(this, android.R.color.black));
        newTextView.setAlpha(0f); // Ban đầu ẩn để hiệu ứng fade in

        // Thêm views vào layout
        itemLayout.addView(newImageView);
        itemLayout.addView(newTextView);

        // Thêm vào container
        letterContainer.addView(itemLayout);

        // Chạy animation
        playJumpAnimation(source, newImageView, newTextView);
    }

    private void playJumpAnimation(ImageView source, ImageView target, TextView infoText) {
        // Tạo ImageView tạm thời cho animation
        final ImageView tempImage = new ImageView(this);
        tempImage.setImageDrawable(source.getDrawable());
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(
                source.getWidth(),
                source.getHeight());
        tempImage.setLayoutParams(params);
        ((ViewGroup) source.getParent()).addView(tempImage);

        // Ẩn ảnh gốc tạm thời
        source.setVisibility(View.INVISIBLE);

        // Lấy vị trí trên màn hình
        int[] sourceLoc = new int[2];
        int[] targetLoc = new int[2];
        source.getLocationOnScreen(sourceLoc);
        target.getLocationOnScreen(targetLoc);

        // Thiết lập animation
        ObjectAnimator moveX = ObjectAnimator.ofFloat(tempImage, "translationX",
                0, targetLoc[0] - sourceLoc[0]);
        ObjectAnimator moveY = ObjectAnimator.ofFloat(tempImage, "translationY",
                0, targetLoc[1] - sourceLoc[1]);
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(tempImage, "scaleX", 1f, 1.5f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(tempImage, "scaleY", 1f, 1.5f, 1f);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(moveX, moveY, scaleX, scaleY);
        animatorSet.setDuration(800);

        animatorSet.start();

        // Sau khi animation hoàn thành
        tempImage.postDelayed(() -> {
            // Hiển thị text với hiệu ứng fade in
            infoText.animate().alpha(1f).setDuration(300).start();

            // Dọn dẹp temp image
            ((ViewGroup) tempImage.getParent()).removeView(tempImage);
            source.setVisibility(View.VISIBLE);
        }, 800);
    }
}