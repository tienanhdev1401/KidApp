package com.example.kidapp.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kidapp.Adapter.FlipCardLevelAdapter; // Cần tạo Adapter này
import com.example.kidapp.R;
import com.example.kidapp.ViewModel.FlipCardLevelViewModel; // Cần ViewModel này
import com.example.kidapp.ViewModel.UserViewModel;
import com.example.kidapp.models.FlipCardLevel; // Sử dụng model FlipCardLevel
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

public class FlipCardLevelSelectionActivity extends AppCompatActivity implements FlipCardLevelAdapter.OnLevelClickListener {

    private RecyclerView recyclerViewLevels;
    private FlipCardLevelAdapter adapter;
    private FlipCardLevelViewModel levelViewModel;
    private UserViewModel userViewModel;

    private CardView loadingCard;
    private CardView emptyStateCard;
    private TextView emptyTextView;
    private Button refreshButton;
    private ImageView btnBack;

    private String userEmail = null;
    private int levelReached = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flip_card_level_selection); // Sử dụng layout mới

        initViews();
        setupRecyclerView();
        setupViewModel();
        setupListeners();

        // Lấy email user hiện tại
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            userEmail = currentUser.getEmail();
        }

        // Lấy levelReached của user cho game "flipcard"
        if (userEmail != null) {
            userViewModel.getUserByEmail(userEmail).observe(this, user -> {
                if (user != null && user.getGameProgress() != null && user.getGameProgress().containsKey("flipcard")) {
                    levelReached = user.getGameProgress().get("flipcard").getLevelReached();
                    Log.d("FlipCardLevelSelect", "Level reached: " + levelReached);
                } else {
                    levelReached = 0;
                }
                adapter.setLevelReached(levelReached); // Cập nhật levelReached cho adapter
            });
        } else {
            adapter.setLevelReached(0);
        }

        // Lấy danh sách level từ ViewModel
        fetchLevels();
    }

    private void initViews() {
        recyclerViewLevels = findViewById(R.id.recyclerViewLevels);
        loadingCard = findViewById(R.id.loadingCard);
        emptyStateCard = findViewById(R.id.emptyStateCard);
        emptyTextView = findViewById(R.id.emptyTextView);
        refreshButton = findViewById(R.id.refreshButton);
        btnBack = findViewById(R.id.btn_Back);
    }

    private void setupRecyclerView() {
        adapter = new FlipCardLevelAdapter(this); // Sử dụng adapter mới
        // Sử dụng GridLayoutManager để hiển thị dạng lưới
        GridLayoutManager layoutManager = new GridLayoutManager(this, 2); // 2 cột, có thể điều chỉnh
        recyclerViewLevels.setLayoutManager(layoutManager);
        recyclerViewLevels.setAdapter(adapter);
    }

    private void setupViewModel() {
        levelViewModel = new ViewModelProvider(this).get(FlipCardLevelViewModel.class); // Sử dụng ViewModel mới
        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);
    }

    private void fetchLevels() {
        // Hiển thị loading animation
        loadingCard.setVisibility(View.VISIBLE);

        levelViewModel.getAllLevels().observe(this, levels -> {
            loadingCard.setVisibility(View.GONE);

            if (levels != null && !levels.isEmpty()) {
                // Hiển thị danh sách level
                emptyStateCard.setVisibility(View.GONE);
                recyclerViewLevels.setVisibility(View.VISIBLE);
                adapter.setLevels(levels); // Cập nhật danh sách level cho adapter
            } else {
                // Hiển thị thông báo nếu không có level nào
                emptyStateCard.setVisibility(View.VISIBLE);
                recyclerViewLevels.setVisibility(View.GONE);
                emptyTextView.setText("Không tìm thấy màn chơi nào!");
            }
        });
    }

    private void setupListeners() {
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
            // Cần thêm phương thức refreshLevels() trong FlipCardLevelViewModel nếu cần
            // levelViewModel.refreshLevels(); 
            fetchLevels(); // Tạm thời gọi lại fetchLevels
        });
    }

    @Override
    public void onLevelClick(FlipCardLevel level) {
        // Xử lý khi click vào một level
        // Kiểm tra xem level có bị khóa không dựa trên levelReached
        if (level.getId() <= levelReached + 1) { // Logic mở khóa
            Intent intent = new Intent(this, GameLatTheActivity.class); // Chuyển sang màn hình chơi game
            intent.putExtra("level", level); // Truyền dữ liệu level sang màn hình chơi
            startActivity(intent);
        } else {
            Toast.makeText(this, "Vui lòng hoàn thành màn chơi trước!", Toast.LENGTH_SHORT).show();
        }
    }
} 