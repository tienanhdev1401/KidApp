package com.example.kidapp.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kidapp.Adapter.GuessWordLevelAdapter;
import com.example.kidapp.R;
import com.example.kidapp.ViewModel.UserViewModel;
import com.example.kidapp.ViewModel.WordGuessLevelViewModel;
import com.example.kidapp.models.WordGuessLevel;
import com.example.kidapp.models.WordGuessStage;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class GuessWordLevelListActivity extends AppCompatActivity implements GuessWordLevelAdapter.OnLevelClickListener {

    private WordGuessLevelViewModel viewModel;
    private GuessWordLevelAdapter adapter;
    private UserViewModel userViewModel;
    private int levelReached = 0;
    private String userEmail = null;

    // Khai báo các View mới từ layout activity_flip_card_level_selection
    private CardView loadingCard;
    private CardView emptyStateCard;
    private TextView emptyTextView;
    private Button refreshButton;
    private ImageView btnBack;
    private TextView titleTextView;
    private TextView subtitleTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_level_list);

        // Ánh xạ các View từ layout
        RecyclerView recyclerView = findViewById(R.id.recyclerViewLevels);
        loadingCard = findViewById(R.id.loadingCard);
        emptyStateCard = findViewById(R.id.emptyStateCard);
        emptyTextView = findViewById(R.id.emptyTextView);
        refreshButton = findViewById(R.id.refreshButton);
        btnBack = findViewById(R.id.btn_Back);
        titleTextView = findViewById(R.id.titleTextView);
        subtitleTextView = findViewById(R.id.subtitleTextView);

        // Cài đặt tiêu đề và phụ đề
        titleTextView.setText("Chọn Màn Đoán Chữ");
        subtitleTextView.setText("Hãy chọn một màn chơi để bắt đầu!");

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new GuessWordLevelAdapter(this);
        recyclerView.setAdapter(adapter);

        viewModel = new ViewModelProvider(this).get(WordGuessLevelViewModel.class);
        
        // Lấy email user hiện tại từ FirebaseAuth
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            userEmail = currentUser.getEmail();
        }

        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);

        // Lấy levelReached của user cho game "guessword"
        if (userEmail != null) {
            userViewModel.getUserByEmail(userEmail).observe(this, user -> {
                if (user != null && user.getGameProgress() != null && user.getGameProgress().containsKey("guessword")) {
                    levelReached = user.getGameProgress().get("guessword").getLevelReached();
                } else {
                    levelReached = 0;
                }
                adapter.setLevelReached(levelReached);
            });
        } else {
            adapter.setLevelReached(0);
        }

        viewModel.getAllLevels().observe(this, levels -> {
            // Logic hiển thị/ẩn loading và empty state
            loadingCard.setVisibility(View.GONE);
            if (levels != null && !levels.isEmpty()) {
                emptyStateCard.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
                adapter.setLevels(levels);
            } else {
                emptyStateCard.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
                emptyTextView.setText("Không tìm thấy màn chơi nào!");
            }
        });

        // Thêm Listener cho nút Back và Refresh
        btnBack.setOnClickListener(v -> {
            // Thêm hiệu ứng khi nhấn nút
            btnBack.animate().scaleX(0.9f).scaleY(0.9f).setDuration(100).withEndAction(() -> {
                btnBack.animate().scaleX(1f).scaleY(1f).setDuration(100);
                finish();
            }).start();
        });

        refreshButton.setOnClickListener(v -> {
            emptyStateCard.setVisibility(View.GONE);
            loadingCard.setVisibility(View.VISIBLE);
            // Cần thêm phương thức refreshLevels() trong WordGuessLevelViewModel nếu cần
            // viewModel.refreshLevels();
            // Tạm thời gọi lại getAllLevels
            viewModel.getAllLevels();
        });
    }

    @Override
    public void onLevelClick(WordGuessLevel level) {
        // Xử lý khi click vào một level (logic chuyển màn chơi đã có)
        // Kiểm tra xem level có bị khóa không dựa trên levelReached
        if (level.getId() <= levelReached + 1) { // Logic mở khóa tương tự
            Intent intent = new Intent(this, GameDoanChuActivity.class);
            intent.putExtra("level", level);
            startActivity(intent);
        } else {
            Toast.makeText(this, "Vui lòng hoàn thành màn chơi trước!", Toast.LENGTH_SHORT).show();
        }
    }
} 