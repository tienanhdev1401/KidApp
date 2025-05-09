package com.example.kidapp.Activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kidapp.Adapter.PuzzleAdapter;
import com.example.kidapp.R;
import com.example.kidapp.ViewModel.PuzzleViewModel;
import com.example.kidapp.models.Puzzle;

import java.util.ArrayList;

public class PuzzleSelectionActivity extends AppCompatActivity {

    private RecyclerView puzzleRecyclerView;
    private PuzzleAdapter puzzleAdapter;
    private PuzzleViewModel puzzleViewModel;
    private CardView loadingCard;
    private CardView emptyStateCard;
    private TextView emptyTextView;
    private Button refreshButton;
    private ImageView btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_puzzle_selection);

        initViews();
        setupRecyclerView();
        setupViewModel();
        setupListeners();
    }

    private void initViews() {
        puzzleRecyclerView = findViewById(R.id.puzzleRecyclerView);
        loadingCard = findViewById(R.id.loadingCard);
        emptyStateCard = findViewById(R.id.emptyStateCard);
        emptyTextView = findViewById(R.id.emptyTextView);
        refreshButton = findViewById(R.id.refreshButton);
        btnBack = findViewById(R.id.btn_Back);
    }

    private void setupRecyclerView() {
        puzzleAdapter = new PuzzleAdapter(this, new ArrayList<>());
        GridLayoutManager layoutManager = new GridLayoutManager(this, 2);
        puzzleRecyclerView.setLayoutManager(layoutManager);
        puzzleRecyclerView.setAdapter(puzzleAdapter);
    }

    private void setupViewModel() {
        puzzleViewModel = new ViewModelProvider(this).get(PuzzleViewModel.class);
        
        // Hiển thị loading animation
        loadingCard.setVisibility(View.VISIBLE);
        
        // Lấy danh sách puzzle từ ViewModel
        fetchPuzzles();
    }
    
    private void fetchPuzzles() {
        puzzleViewModel.getAllPuzzles().observe(this, puzzles -> {
            loadingCard.setVisibility(View.GONE);
            
            if (puzzles != null && !puzzles.isEmpty()) {
                // Hiển thị danh sách puzzle
                emptyStateCard.setVisibility(View.GONE);
                puzzleRecyclerView.setVisibility(View.VISIBLE);
                puzzleAdapter.updatePuzzleList(puzzles);
            } else {
                // Hiển thị thông báo nếu không có puzzle nào
                emptyStateCard.setVisibility(View.VISIBLE);
                puzzleRecyclerView.setVisibility(View.GONE);
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
            puzzleViewModel.refreshPuzzles();
        });
    }
} 