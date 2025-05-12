package com.example.kidapp.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kidapp.Adapter.storyAiHistoryAdapter;
import com.example.kidapp.R;
import com.example.kidapp.ViewModel.storyAiHistoryViewModel;
import com.example.kidapp.models.storyAiHistoryModel;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import java.util.ArrayList;

public class StoryHistoryActivity extends AppCompatActivity implements storyAiHistoryAdapter.OnStoryClickListener, storyAiHistoryAdapter.OnDeleteClickListener {

    private RecyclerView recyclerViewStories;
    private storyAiHistoryAdapter adapter;
    private storyAiHistoryViewModel viewModel;
    private LinearLayout emptyView;
    private ProgressBar progressBar;
    private ExtendedFloatingActionButton fabCreateStory;
    private Button FirstStory;
    private static final int REQUEST_CODE_STORY_READER = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_story_history);
        FirstStory = findViewById(R.id.firstStory);
        FirstStory.setOnClickListener(v -> {
            navigateToStoryCreation(); // gọi lại hàm tạo truyện
        });
        // Khởi tạo Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        // Ánh xạ các view
        recyclerViewStories = findViewById(R.id.recyclerViewStories);
        emptyView = findViewById(R.id.emptyView);
        progressBar = findViewById(R.id.progressBar);
        fabCreateStory = findViewById(R.id.fabCreateStory);

        // Thiết lập RecyclerView
        recyclerViewStories.setLayoutManager(new LinearLayoutManager(this));
        adapter = new storyAiHistoryAdapter(this, this, this);
        recyclerViewStories.setAdapter(adapter);

        // Khởi tạo ViewModel
        viewModel = new ViewModelProvider(this).get(storyAiHistoryViewModel.class);

        // Theo dõi trạng thái loading
        viewModel.getIsLoading().observe(this, isLoading -> {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });

        // Nút tạo truyện mới
        fabCreateStory.setOnClickListener(v -> {
            // Chuyển đến màn hình tạo truyện mới
            navigateToStoryCreation();
        });

        // Load dữ liệu
        loadStories();
    }


    @Override
    protected void onResume() {
        super.onResume();
        // Làm mới danh sách truyện khi quay lại từ màn hình khác
        viewModel.refreshStories();
    }

    private void loadStories() {
        viewModel.getAllStories().observe(this, stories -> {
            adapter.setStories(stories);

            // Hiển thị emptyView nếu không có truyện nào
            if (stories == null || stories.isEmpty()) {
                recyclerViewStories.setVisibility(View.GONE);
                emptyView.setVisibility(View.VISIBLE);
            } else {
                recyclerViewStories.setVisibility(View.VISIBLE);
                emptyView.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void onStoryClick(storyAiHistoryModel story) {
        // Chuyển đến màn hình đọc truyện với story đã chọn
        navigateToStoryReader(story);
    }

    @Override
    public void onDeleteClick(storyAiHistoryModel story) {
        // Hiển thị dialog xác nhận xóa
        new AlertDialog.Builder(this)
                .setTitle("Xóa truyện")
                .setMessage("Bạn có chắc muốn xóa truyện này?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    deleteStory(story);
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void deleteStory(storyAiHistoryModel story) {
        viewModel.deleteStory(story.getId()).observe(this, success -> {
            if (success) {
                Toast.makeText(this, "Đã xóa truyện", Toast.LENGTH_SHORT).show();
                // Xóa truyện khỏi adapter trước để cập nhật UI ngay lập tức
                adapter.removeStory(story.getId());
                // Sau đó làm mới danh sách từ Firebase
                viewModel.refreshStories();
            } else {
                Toast.makeText(this, "Không thể xóa truyện", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void navigateToStoryReader(storyAiHistoryModel story) {
        Intent intent = new Intent(this, StoryReaderActivity.class);
        // Truyền ID của truyện để StoryReaderActivity có thể load dữ liệu
        intent.putExtra("STORY_ID", story.getId());
        intent.putExtra("FROM_HISTORY", true);
        startActivityForResult(intent, REQUEST_CODE_STORY_READER);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == REQUEST_CODE_STORY_READER && resultCode == RESULT_OK) {
            if (data != null && data.getBooleanExtra("REFRESH_HISTORY", false)) {
                // Làm mới danh sách nếu có thay đổi (lưu hoặc xóa)
                viewModel.refreshStories();
            }
        }
    }

    private void navigateToStoryCreation() {
        // Chuyển đến màn hình tạo truyện mới (trước khi đến StoryReaderActivity)
        // Có thể sẽ là màn hình nhập prompt hoặc các thông tin ban đầu
        Intent intent = new Intent(this, StoryCreationActivity.class);
        startActivity(intent);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}