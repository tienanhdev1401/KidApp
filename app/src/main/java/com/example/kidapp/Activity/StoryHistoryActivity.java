package com.example.kidapp.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kidapp.R;
import com.example.kidapp.Service.GeminiService;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

public class StoryHistoryActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TextView emptyView;
    private FloatingActionButton fabCreateStory;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_story_history);

        // Khởi tạo các thành phần UI
        initializeUI();

        // Thiết lập toolbar
        setupToolbar();

        // Thiết lập RecyclerView
        setupRecyclerView();

        // Thiết lập nút tạo truyện mới
        setupCreateButton();
    }

    private void initializeUI() {
        recyclerView = findViewById(R.id.recyclerViewStories);
        emptyView = findViewById(R.id.emptyView);
        fabCreateStory = findViewById(R.id.fabCreateStory);
        toolbar = findViewById(R.id.toolbar);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Truyện AI");
        }
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        
        // Hiện tại chưa có adapter và dữ liệu, hiển thị emptyView
        recyclerView.setVisibility(View.GONE);
        emptyView.setVisibility(View.VISIBLE);
    }

    private void setupCreateButton() {
        fabCreateStory.setOnClickListener(v -> {
            // Mở AIStoryCreatorActivity khi nhấn nút tạo truyện mới
            Intent intent = new Intent(StoryHistoryActivity.this, AIStoryCreatorActivity.class);
            startActivity(intent);
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}